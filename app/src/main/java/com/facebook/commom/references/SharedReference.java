package com.facebook.commom.references;

/**
 * Created by heshixiyang on 2017/3/12.
 */

import android.support.annotation.VisibleForTesting;

import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.logging.FLog;

import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;

/**
 * 这是一个shared-reference 的类，与c++ 的shared_ptr类似。
 * 这里underlying value是引用的数量，当数量减少到0，underlying value是"disposed"的
 * A shared-reference class somewhat similar to c++ shared_ptr. The underlying value is reference
 * counted, and when the count drops to zero, the underlying value is "disposed"
 * <p>
 * 与c++ 不同的实现是，c++供了很多的语法糖与复制构造函数和析构函数。
 * java没有提供等价的东西，所以我们用addReference() 和 deleteReference()来代替
 * 和我们需要非常小心的使用这些存在异常。
 * Unlike the c++ implementation, which provides for a bunch of syntactic sugar with copy
 * constructors and destructors, Java does not provide the equivalents. So we instead have the
 * explicit addReference() and deleteReference() calls, and we need to be extremely careful
 * about using these in the presence of exceptions, or even otherwise.
 * <p>
 * 尽管需要额外的方法被调用，但是这在许多情况下依然是值得的，为了避免垃圾回收
 * Despite the extra (and clunky) method calls, this is still worthwhile in many cases to avoid
 * the overhead of garbage collection.
 * <p>
 * 这里有一些笨重的规则
 * 1.如果一个方法返回了一个SharedReference，他必须要保证reference的数量最少大于1
 * 在SharedReference被调用的情况下必须提前设置好count为1
 * 2.如果一个方法使用shared-reference调用其他的方法
 *      2.1 调用者必须保证在调用期间这个引用是有用的
 *      2.2 被调用的函数没有责任清除这个引用
 *      2.3 如果调用者想要这个引用在调用返回之后还有用(例如被放在一个map中)
 *      那么其需要"clone" 那个引用，通过调用{@link #addReference()}
 * The somewhat clunky rules are
 * 1. If a function returns a SharedReference, it must guarantee that the reference count
 *    is at least 1. In the case where a SharedReference is being constructed and returned,
 *    the SharedReference constructor will already set the ref count to 1.
 * 2. If a function calls another function with a shared-reference parameter,
 *    2.1 The caller must ensure that the reference is valid for the duration of the
 *        invocation.
 *    2.2 The callee *is not* responsible for the cleanup of the reference.
 *    2.3 If the callee wants to keep the reference around even after the call returns (for
 *        example, stashing it away in a map), then it should "clone" the reference by invoking
 *        {@link #addReference()}
 * <p>
 *   例子1：
 *   Example #1 (function with a shared reference parameter):
 *   void foo(SharedReference r, ...) {
 *     // first assert that the reference is valid
 *     Preconditions.checkArgument(SharedReference.isValid(r));
 *     ...
 *     // do something with the contents of r
 *     ...
 *     // do not increment/decrement the ref count
 *   }
 * <p>
 *   Example #2 (function with a shared reference parameter that keeps around the shared ref)
 *     void foo(SharedReference r, ...) {
 *       // first assert that the reference is valid
 *       Preconditions.checkArgument(SharedReference.isValid(r));
 *       ...
 *       // increment ref count
 *       r.addReference();
 *       // stash away the reference
 *       ...
 *       return;
 *     }
 * <p>
 *   Example #3 (function with a shared reference parameter that passes along the reference to
 *   another function)
 *     void foo(SharedReference r, ...) {
 *       // first assert that the reference is valid
 *       Preconditions.checkArgument(SharedReference.isValid(r));
 *       ...
 *       bar(r, ...); // call to other function
 *       ...
 *     }
 * <p>
 *   Example #4 (function that returns a shared reference)
 *     SharedReference foo(...) {
 *       // do something
 *       ...
 *       // create a new shared reference (refcount automatically at 1)
 *       SharedReference r = new SharedReference(x);
 *       // return this shared reference
 *       return r;
 *     }
 * <p>
 *   Example #5 (function with a shared reference parameter that returns the shared reference)
 *     void foo(SharedReference r, ...) {
 *       // first assert that the reference is valid
 *       Preconditions.checkArgument(SharedReference.isValid(r));
 *       ...
 *       // increment ref count before returning
 *       r.addReference();
 *       return r;
 *     }
 */
@VisibleForTesting
public class SharedReference<T> {
    //保存所有的存活对象的引用，所以那些对象的销毁总是发生在SharedReference第一次将其处理的时候
    //注意：这里并不阻止CloseableReference的被终结，当reference不可达的时候
    // Keeps references to all live objects so finalization of those Objects always happens after
    // SharedReference first disposes of it. Note, this does not prevent CloseableReference's from
    // being finalized when the reference is no longer reachable.

    //这里用IdentityHashMap只有两个key指向同一个内存地址的时候，才表示是同一个对象，所以此时可以用来储存一个对象有几个引用指向
    //但是注意这里的每个对象的引用计数不是随时变化，这里只储存存活的对象，对于该对象的引用计数则不是很准确。

    @GuardedBy("itself")
    private static final Map<Object, Integer> sLiveObjects = new IdentityHashMap<>();

    @GuardedBy("this")
    private T mValue;
    @GuardedBy("this")
    private int mRefCount;

    private final ResourceReleaser<T> mResourceReleaser;

    /**
     * Construct a new shared-reference that will 'own' the supplied {@code value}.
     * The reference count will be set to 1. When the reference count decreases to zero
     * {@code resourceReleaser} will be used to release the {@code value}
     * @param value non-null value to manage
     * @param resourceReleaser non-null ResourceReleaser for the value
     */
    public SharedReference(T value, ResourceReleaser<T> resourceReleaser) {
        mValue = Preconditions.checkNotNull(value);
        mResourceReleaser = Preconditions.checkNotNull(resourceReleaser);
        mRefCount = 1;
        addLiveReference(value);
    }

    /**
     * 将一个新创建的SharedReference的指向对象引用储存在map中，如果这个对象已经被储存了，那么给其引用数量加一
     * Increases the reference count of a live object in the static map. Adds it if it's not
     * being held.
     *
     * @param value the value to add.
     */
    private static void addLiveReference(Object value) {
        synchronized (sLiveObjects) {
            Integer count = sLiveObjects.get(value);
            if (count == null) {
                sLiveObjects.put(value, 1);
            } else {
                sLiveObjects.put(value, count + 1);
            }
        }
    }

    /**
     * 将某个对象的引用在map中的计数减去一，如果引用计数在减去之后为0，那么将其移除。
     * Decreases the reference count of live object from the static map. Removes it if it's reference
     * count has become 0.
     *
     * @param value the value to remove.
     */
    private static void removeLiveReference(Object value) {
        synchronized (sLiveObjects) {
            Integer count = sLiveObjects.get(value);
            if (count == null) {
                // Uh oh.
                FLog.wtf(
                        "SharedReference",
                        "No entry in sLiveObjects for value of type %s",
                        value.getClass());
            } else if (count == 1) {
                sLiveObjects.remove(value);
            } else {
                sLiveObjects.put(value, count - 1);
            }
        }
    }

    /**
     *
     * Get the current referenced value. Null if there's no value.
     * @return the referenced value
     */
    public synchronized T get() {
        return mValue;
    }

    /**
     * 判断该SharedReference中的引用是否可用。
     * Checks if this shared-reference is valid i.e. its reference count is greater than zero.
     * @return true if shared reference is valid
     */
    public synchronized boolean isValid() {
        return mRefCount > 0;
    }

    /**
     * 判断某个SharedReference中的引用是否可用。
     * Checks if the shared-reference is valid i.e. its reference count is greater than zero
     * @return true if the shared reference is valid
     */
    public static boolean isValid(SharedReference<?> ref) {
        return ref != null && ref.isValid();
    }

    /**
     * 当SharedReference中的引用指向的对象又多了一个指向引用，先判断是否可用，然后将引用计数加一
     * Bump up the reference count for the shared reference
     * Note: The reference must be valid (aka not null) at this point
     */
    public synchronized void addReference() {
        ensureValid();
        mRefCount++;
    }

    /**
     * 将引用计数减一，如果为0，那么将这个对象释放了，并且将map中的储存删掉
     * Decrement the reference count for the shared reference. If the reference count drops to zero,
     * then dispose of the referenced value
     */
    public void deleteReference() {
        if (decreaseRefCount() == 0) {
            T deleted;
            synchronized (this) {
                deleted = mValue;
                mValue = null;
            }
            mResourceReleaser.release(deleted);
            removeLiveReference(deleted);
        }
    }

    /**
     * 先判断是否可用，然后将引用计数减一，并返回减掉后的引用计数
     * Decrements reference count for the shared reference. Returns value of mRefCount after
     * decrementing
     */
    private synchronized int decreaseRefCount() {
        ensureValid();
        Preconditions.checkArgument(mRefCount > 0);

        mRefCount--;
        return mRefCount;
    }

    /**
     * 判断该SharedReference是否可用，不可用就抛出异常
     * Assert that there is a valid referenced value. Throw a NullReferenceException otherwise
     * @throws NullReferenceException, if the reference is invalid (i.e.) the underlying value is null
     */
    private void ensureValid() {
        if (!isValid(this)) {
            throw new NullReferenceException();
        }
    }

    /**
     * A test-only method to get the ref count
     * DO NOT USE in regular code
     */
    public synchronized int getRefCountTestOnly() {
        return mRefCount;
    }

    /**
     * The moral equivalent of NullPointerException for SharedReference. Indicates that the
     * referenced object is null
     */
    public static class NullReferenceException extends RuntimeException {
        public NullReferenceException() {
            super("Null shared reference");
        }
    }
}
