package com.facebook.cache.disk.impl;

/**
 * Created by Administrator on 2017/3/11 0011.
 */

import android.os.Environment;
import android.support.annotation.VisibleForTesting;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.commom.CacheErrorLogger;
import com.facebook.cache.commom.WriterCallback;
import com.facebook.cache.disk.DiskStorage;
import com.facebook.commom.file.FileTree;
import com.facebook.commom.file.FileTreeVisitor;
import com.facebook.commom.file.FileUtils;
import com.facebook.commom.internal.CountingOutputStream;
import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.time.Clock;
import com.facebook.commom.time.impl.SystemClock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

/**
 * 默认的DiskStorage的实现，通过一个SubdirectorySupplier创建
 * The default disk storage implementation. Subsumes both 'simple' and 'sharded' implementations
 * via a new SubdirectorySupplier.
 */
public class DefaultDiskStorage implements DiskStorage {

    private static final Class<?> TAG = DefaultDiskStorage.class;

    //内容文件的扩展名，一个缓存条目在Inserter.commit()之后会改成这个后缀
    private static final String CONTENT_FILE_EXTENSION = ".cnt";
    //临时文件的扩展名Inserter.writeData()之后Inserter.commit()之前，为这个后缀
    private static final String TEMP_FILE_EXTENSION = ".tmp";
    //默认的磁盘存储版本前缀
    private static final String DEFAULT_DISK_STORAGE_VERSION_PREFIX = "v2";

    /*
     * 我们通过分片来避免三星的RFS问题，和避免一个目录包含成千上万的文件
     * We use sharding to avoid Samsung's RFS problem, and to avoid having one big directory
     * containing thousands of files.
     *
     * 这里目录的数量是否足够大基于以下几个原因：
     * 1.高频率使用：150张图片每天
     * 2.
     * This number of directories is large enough based on the following reasoning:
     * - high usage: 150 photos per day
     * - such usage will hit Samsung's 6,500 photos cap in 43 days
     * - 100 buckets will extend that period to 4,300 days which is 11.78 years
     */
    private static final int SHARDING_BUCKET_COUNT = 100;

    /**
     * 我们将清理存在时间比这个时间更长的临时文件
     * We will allow purging of any temp files older than this.
     */
    static final long TEMP_FILE_LIFETIME_MS = TimeUnit.MINUTES.toMillis(30);

    /**
     * 缓存使用的缓存根目录
     * The base directory used for the cache
     */
    private final File mRootDirectory;

    /**
     * 缓存是否在外部储存器
     * True if cache is external
     */
    private final boolean mIsExternal;

    /**
     * 所有的切分在version-directory中石使用。它允许简单的版本升级
     * 当找到了一个基本的目录而没有version-directory在里面的时候，那就意味着者是一个不同版本的
     * 所有我们需要删掉整个目录，原因有两个：
     * 1.清理所有的不可使用的文件
     * 2.避免老式三星的RFS问题，这个问题是将所有的文件都放在一个根文件里
     * All the sharding occurs inside a version-directory. That allows for easy version upgrade.
     * When we find a base directory with no version-directory in it, it means that it's a different
     * version and we should delete the whole directory (including itself) for both reasons:
     * 1) clear all unusable files 2) avoid Samsung RFS problem that was hit with old implementations
     * of DiskStorage which used a single directory for all the files.
     */
    private final File mVersionDirectory;

    private final CacheErrorLogger mCacheErrorLogger;
    private final Clock mClock;

    /**
     * 实例化一个ShardedDiskStorage将要使用一个目录来储存一个key和file对应的map
     * 此时版本对于一个客户端是非常重要的，如果客户端改变了储存文件的格式
     * ShardedDiskStorage将保证不同版本的文件保存在不同的文件夹里，没有使用的文件最终将被删除
     * Instantiates a ShardedDiskStorage that will use the directory to save a map between
     * keys and files. The version is very important if clients change the format
     * saved in those files. ShardedDiskStorage will assure that files saved with different
     * version will be never used and eventually removed.
     * @param rootDirectory 创建所有内容的基本文件
     * root directory to create all content under
     * @param version 文件使用的格式版本，如果传入了不同的version，之前的文件将不会再被读取，并且最终会被删除
     * version of the format used in the files. If passed a different version
     * files saved with the previous value will not be read and will be purged eventually.
     * @param cacheErrorLogger logger for various events
     */
    public DefaultDiskStorage(
            File rootDirectory,
            int version,
            CacheErrorLogger cacheErrorLogger) {
        Preconditions.checkNotNull(rootDirectory);

        mRootDirectory = rootDirectory;
        mIsExternal = isExternal(rootDirectory, cacheErrorLogger);
        // mVersionDirectory's name identifies:
        // - the cache structure's version (sharded)
        // - the content's version (version value)
        // if structure changes, prefix will change... if content changes version will be different
        // the ideal would be asking mSharding its name, but it's created receiving the directory
        mVersionDirectory = new File(mRootDirectory, getVersionSubdirectoryName(version));
        mCacheErrorLogger = cacheErrorLogger;
        recreateDirectoryIfVersionChanges();
        mClock = SystemClock.get();
    }

    /*
    * 判断这个文件是否在外部储存
    * */
    private static boolean isExternal(File directory, CacheErrorLogger cacheErrorLogger) {
        boolean state = false;
        String appCacheDirPath = null;

        try {
            File extStoragePath = Environment.getExternalStorageDirectory();
            if (extStoragePath != null) {
                String cacheDirPath = extStoragePath.toString();
                try {
                    appCacheDirPath = directory.getCanonicalPath();
                    if (appCacheDirPath.contains(cacheDirPath)) {
                        state = true;
                    }
                } catch (IOException e) {
                    cacheErrorLogger.logError(
                            CacheErrorLogger.CacheErrorCategory.OTHER,
                            TAG,
                            "failed to read folder to check if external: " + appCacheDirPath,
                            e);
                }
            }
        } catch (Exception e) {
            cacheErrorLogger.logError(
                    CacheErrorLogger.CacheErrorCategory.OTHER,
                    TAG,
                    "failed to get the external storage directory!",
                    e);
        }
        return state;
    }

    @VisibleForTesting
    static String getVersionSubdirectoryName(int version) {
        return String.format(
                (Locale) null,
                "%s.ols%d.%d",
                DEFAULT_DISK_STORAGE_VERSION_PREFIX,
                SHARDING_BUCKET_COUNT,
                version);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isExternal() {
        return mIsExternal;
    }

    @Override
    public String getStorageName() {
        String directoryName = mRootDirectory.getAbsolutePath();
        return "_" + directoryName.substring(directoryName.lastIndexOf('/') + 1, directoryName.length())
                + "_" + directoryName.hashCode();
    }

    /**
     * 检查我们是否需要重新创建rootDirectory
     * 这是必须的，因为老版本rootDirectory中会储存大量不同的文件
     * 三星的RFS在13000次创建失败之后有bug
     * 所以缓存如果没有在预期的版本中，需要销毁所有的缓存
     * (如果不是预期的版本，缓存中的东西还有什么用呢)
     * Checks if we have to recreate rootDirectory.
     * This is needed because old versions of this storage created too much different files
     * in the same dir, and Samsung's RFS has a bug that after the 13.000th creation fails.
     * So if cache is not already in expected version let's destroy everything
     * (if not in expected version... there's nothing to reuse here anyway).
     */
    private void recreateDirectoryIfVersionChanges() {
        boolean recreateBase = false;
        if (!mRootDirectory.exists()) {
            recreateBase = true;
        } else if (!mVersionDirectory.exists()) {
            recreateBase = true;
            FileTree.deleteRecursively(mRootDirectory);
        }

        if (recreateBase) {
            try {
                FileUtils.mkdirs(mVersionDirectory);
            } catch (FileUtils.CreateDirectoryException e) {
                // not the end of the world, when saving files we will try to create missing parent dirs
                mCacheErrorLogger.logError(
                        CacheErrorLogger.CacheErrorCategory.WRITE_CREATE_DIR,
                        TAG,
                        "version directory could not be created: " + mVersionDirectory,
                        null);
            }
        }
    }

    private static class IncompleteFileException extends IOException {
        public final long expected;
        public final long actual;

        public IncompleteFileException(long expected, long actual) {
            super("File was not written completely. Expected: " + expected + ", found: " + actual);
            this.expected = expected;
            this.actual = actual;
        }
    }

    /**
     * 通过id返回对应的缓存文件
     * Calculates which should be the CONTENT file for the given key
     */
    @VisibleForTesting
    File getContentFileFor(String resourceId) {
        return new File(getFilename(resourceId));
    }

    /**
     * 通过id获取缓存文件所在的文件夹路径
     * Gets the directory to use to store the given key
     * @param resourceId the id of the file we're going to store
     * @return the directory to store the file in
     */
    private String getSubdirectoryPath(String resourceId) {
        String subdirectory = String.valueOf(Math.abs(resourceId.hashCode() % SHARDING_BUCKET_COUNT));
        return mVersionDirectory + File.separator + subdirectory;
    }

    /**
     * 通过id获取缓存文件所在的文件夹
     * Gets the directory to use to store the given key
     * @param resourceId the id of the file we're going to store
     * @return the directory to store the file in
     */
    private File getSubdirectory(String resourceId) {
        return new File(getSubdirectoryPath(resourceId));
    }

    /**
     * 实现了{@link FileTreeVisitor}去遍历sharded files中所有的文件并且收集其中的有效文件
     * 这里的有效文件是 FileType.CONTENT 后缀的文件，将有效的文件以id和file构成EntryImpl，储存为List。
     * Implementation of {@link FileTreeVisitor} to iterate over all the sharded files and
     * collect those valid content files. It's used in entriesIterator method.
     */
    private class EntriesCollector implements FileTreeVisitor {

        private final List<Entry> result = new ArrayList<>();

        @Override
        public void preVisitDirectory(File directory) {
        }

        @Override
        public void visitFile(File file) {
            FileInfo info = getShardFileInfo(file);
            if (info != null && info.type == FileType.CONTENT) {
                result.add(new EntryImpl(info.resourceId, file));
            }
        }

        @Override
        public void postVisitDirectory(File directory) {
        }

        /** Returns an immutable list of the entries. */
        public List<Entry> getEntries() {
            return Collections.unmodifiableList(result);
        }
    }

    /**
     *
     * 实现了{@link FileTreeVisitor}去访问某文件夹中所有文件，
     * 然后删除所有不需要的文件和文件夹例如：已经达到衰老时间的临时文件。
     * This implements a  {@link FileTreeVisitor} to iterate over all the files in mDirectory
     * and delete any unexpected file or directory. It also gets rid of any empty directory in
     * the shard.
     * 这作为一个便捷的方式来检查mVersionDirectory中的状况。
     * As a shortcut it checks that things are inside (current) mVersionDirectory. If it's not
     * then it's directly deleted. If it's inside then it checks if it's a recognized file and
     * if it's in the correct shard according to its name (checkShard method). If it's unexpected
     * file is deleted.
     */
    private class PurgingVisitor implements FileTreeVisitor {
        private boolean insideBaseDirectory;

        @Override
        public void preVisitDirectory(File directory) {
            if (!insideBaseDirectory && directory.equals(mVersionDirectory)) {
                // if we enter version-directory turn flag on
                insideBaseDirectory = true;
            }
        }

        @Override
        public void visitFile(File file) {
            if (!insideBaseDirectory || !isExpectedFile(file)) {
                file.delete();
            }
        }


        @Override
        public void postVisitDirectory(File directory) {
            //如果不是mRootDirectory那么我们不去接触它
            if (!mRootDirectory.equals(directory)) { // if it's root directory we must not touch it
                if (!insideBaseDirectory) {
                    //如果不是当前版本的目录，我们就将其删除
                    // if not in version-directory then it's unexpected!
                    directory.delete();
                }
            }
            if (insideBaseDirectory && directory.equals(mVersionDirectory)) {
                // if we just finished visiting version-directory turn flag off
                insideBaseDirectory = false;
            }
        }

        private boolean isExpectedFile(File file) {
            FileInfo info = getShardFileInfo(file);
            if (info == null) {
                return false;
            }
            if (info.type == FileType.TEMP) {
                return isRecentFile(file);
            }
            Preconditions.checkState(info.type == FileType.CONTENT);
            return true;
        }

        /**
         * 判断临时文件是否够老
         * @return true if and only if the file is not old enough to be considered an old temp file
         */
        private boolean isRecentFile(File file) {
            return file.lastModified() > (mClock.now() - TEMP_FILE_LIFETIME_MS);
        }
    }

    //清理不需要的文件，FileTree.walkFileTree()将会遍历整个mRootDirectory的文件树
    @Override
    public void purgeUnexpectedResources() {
        FileTree.walkFileTree(mRootDirectory, new PurgingVisitor());
    }

    /**
     * 创建目录(及其父母,如果有必要的话)。
     * Creates the directory (and its parents, if necessary).
     * @param directory the directory to create
     * @param message message to use
     * @throws IOException
     */
    private void mkdirs(File directory, String message) throws IOException {
        try {
            FileUtils.mkdirs(directory);
        } catch (FileUtils.CreateDirectoryException cde) {
            mCacheErrorLogger.logError(
                    CacheErrorLogger.CacheErrorCategory.WRITE_CREATE_DIR,
                    TAG,
                    message,
                    cde);
            throw cde;
        }
    }

    @Override
    public Inserter insert(
            String resourceId,
            Object debugInfo)
            throws IOException {
        //确保父目录的存在
        // ensure that the parent directory exists
        FileInfo info = new FileInfo(FileType.TEMP, resourceId);
        File parent = getSubdirectory(info.resourceId);
        if (!parent.exists()) {
            mkdirs(parent, "insert");
        }

        try {
            File file = info.createTempFile(parent);
            return new InserterImpl(resourceId, file);
        } catch (IOException ioe) {
            mCacheErrorLogger.logError(
                    CacheErrorLogger.CacheErrorCategory.WRITE_CREATE_TEMPFILE,
                    TAG,
                    "insert",
                    ioe);
            throw ioe;
        }
    }

    @Override
    public BinaryResource getResource(String resourceId, Object debugInfo) {
        final File file = getContentFileFor(resourceId);
        if (file.exists()) {
            file.setLastModified(mClock.now());
            return FileBinaryResource.createOrNull(file);
        }
        return null;
    }

    private String getFilename(String resourceId) {
        FileInfo fileInfo = new FileInfo(FileType.CONTENT, resourceId);
        String path = getSubdirectoryPath(fileInfo.resourceId);
        return fileInfo.toPath(path);
    }

    @Override
    public boolean contains(String resourceId, Object debugInfo) {
        return query(resourceId, false);
    }


    @Override
    public boolean touch(String resourceId, Object debugInfo) {
        return query(resourceId, true);
    }

    private boolean query(String resourceId, boolean touch) {
        File contentFile = getContentFileFor(resourceId);
        boolean exists = contentFile.exists();
        if (touch && exists) {
            contentFile.setLastModified(mClock.now());
        }
        return exists;
    }

    @Override
    public long remove(Entry entry) {
        //这里应该返回一个EntryImpl给我们
        // it should be one entry return by us :)
        EntryImpl entryImpl = (EntryImpl) entry;
        FileBinaryResource resource = entryImpl.getResource();
        return doRemove(resource.getFile());
    }

    @Override
    public long remove(final String resourceId) {
        return doRemove(getContentFileFor(resourceId));
    }

    private long doRemove(final File contentFile) {
        if (!contentFile.exists()) {
            return 0;
        }

        final long fileSize = contentFile.length();
        if (contentFile.delete()) {
            return fileSize;
        }

        return -1;
    }

    public void clearAll() {
        FileTree.deleteContents(mRootDirectory);
    }

    @Override
    public DiskDumpInfo getDumpInfo() throws IOException {
        List<Entry> entries = getEntries();

        DiskDumpInfo dumpInfo = new DiskDumpInfo();
        for (Entry entry : entries) {
            DiskDumpInfoEntry infoEntry = dumpCacheEntry(entry);
            String type = infoEntry.type;
            if (!dumpInfo.typeCounts.containsKey(type)) {
                dumpInfo.typeCounts.put(type, 0);
            }
            dumpInfo.typeCounts.put(type, dumpInfo.typeCounts.get(type)+1);
            dumpInfo.entries.add(infoEntry);
        }
        return dumpInfo;
    }

    //获取某一条硬盘缓存的信息
    private DiskDumpInfoEntry dumpCacheEntry(Entry entry) throws IOException {
        EntryImpl entryImpl = (EntryImpl)entry;
        String firstBits = "";
        byte[] bytes = entryImpl.getResource().read();
        String type = typeOfBytes(bytes);
        if (type.equals("undefined") && bytes.length >= 4) {
            firstBits = String.format(
                    (Locale) null, "0x%02X 0x%02X 0x%02X 0x%02X", bytes[0], bytes[1], bytes[2], bytes[3]);
        }
        String path = entryImpl.getResource().getFile().getPath();
        return new DiskDumpInfoEntry(path, type, entryImpl.getSize(), firstBits);
    }

    //用于获取DiskDumpInfoEntry中的type
    private String typeOfBytes(byte[] bytes) {
        if (bytes.length >= 2) {
            if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8) {
                return "jpg";
            } else if (bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50) {
                return "png";
            } else if (bytes[0] == (byte) 0x52 && bytes[1] == (byte) 0x49) {
                return "webp";
            } else if (bytes[0] == (byte) 0x47 && bytes[1] == (byte) 0x49) {
                return "gif";
            }
        }
        return "undefined";
    }

    @Override
    /**
     * 返回一个Entries的列表，这里只返回 CONTENT_FILE_EXTENSION 后缀的缓存文件Entry，临时文件对客户端不可见。
     * Returns a list of entries.
     *
     * 这个列表是不可变的
     * <p>This list is immutable.
     */
    public List<Entry> getEntries() throws IOException {
        EntriesCollector collector = new EntriesCollector();
        FileTree.walkFileTree(mVersionDirectory, collector);
        return collector.getEntries();
    }

    /**
     * Entry的唯一实现
     * Implementation of Entry listed by entriesIterator.
     */
    @VisibleForTesting
    static class EntryImpl implements Entry {
        private final String id;
        private final FileBinaryResource resource;
        private long size;
        private long timestamp;

        private EntryImpl(String id, File cachedFile) {
            Preconditions.checkNotNull(cachedFile);
            this.id = Preconditions.checkNotNull(id);
            this.resource = FileBinaryResource.createOrNull(cachedFile);
            this.size = -1;
            this.timestamp = -1;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public long getTimestamp() {
            if (timestamp < 0) {
                final File cachedFile = resource.getFile();
                timestamp = cachedFile.lastModified();
            }
            return timestamp;
        }

        @Override
        public FileBinaryResource getResource() {
            return resource;
        }

        @Override
        public long getSize() {
            if (size < 0) {
                size = resource.size();
            }
            return size;
        }
    }

    /**
     * 检查文件是否放置在正确的切分根据其*文件名(以及键)来表示。如果它的正确的文件返回信息
     * Checks that the file is placed in the correct shard according to its
     * filename (and hence the represented key). If it's correct its FileInfo is returned.
     * @param file the file to check
     * @return the corresponding FileInfo object if shard is correct, null otherwise
     */
    private FileInfo getShardFileInfo(File file) {
        FileInfo info = FileInfo.fromFile(file);
        if (info == null) {
            return null; // file with incorrect name/extension
        }
        File expectedDirectory = getSubdirectory(info.resourceId);
        boolean isCorrect = expectedDirectory.equals(file.getParentFile());
        return isCorrect ? info : null;
    }

    /**
     * Categories for the different internal files a ShardedDiskStorage maintains.
     * CONTENT: the file that has the content
     * TEMP: temporal files, used to write the content until they are switched to CONTENT files
     */
    private static enum FileType {
        CONTENT(CONTENT_FILE_EXTENSION),
        TEMP(TEMP_FILE_EXTENSION);

        public final String extension;

        FileType(String extension) {
            this.extension = extension;
        }

        public static FileType fromExtension(String extension) {
            if (CONTENT_FILE_EXTENSION.equals(extension)) {
                return CONTENT;
            } else if (TEMP_FILE_EXTENSION.equals(extension)) {
                return TEMP;
            }
            return null;
        }
    }

    /**
     * 持有不同的文件信息，这个存储使用(content、tmp)这两个文件扩展名。
     * 所有文件名称解析都应该通过这里。创建临时文件也在这里处理,封装命名
     * Holds information about the different files this storage uses (content, tmp).
     * All file name parsing should be done through here.
     * Temp files creation is also handled here, to encapsulate naming.
     */
    private static class FileInfo {

        public final FileType type;
        public final String resourceId;

        private FileInfo(FileType type, String resourceId) {
            this.type = type;
            this.resourceId = resourceId;
        }

        @Override
        public String toString() {
            return type + "(" + resourceId + ")";
        }

        public String toPath(String parentPath) {
            return parentPath + File.separator + resourceId + type.extension;
        }

        public File createTempFile(File parent) throws IOException {
            File f = File.createTempFile(resourceId + ".", TEMP_FILE_EXTENSION, parent);
            return f;
        }

        @Nullable
        public static FileInfo fromFile(File file) {
            String name = file.getName();
            int pos = name.lastIndexOf('.');
            if (pos <= 0) {
                return null; // no name part
            }
            String ext = name.substring(pos);
            FileType type = FileType.fromExtension(ext);
            if (type == null) {
                return null; // unknown!
            }
            String resourceId = name.substring(0, pos);
            if (type.equals(FileType.TEMP)) {
                int numPos = resourceId.lastIndexOf('.');
                if (numPos <= 0) {
                    return null; // no resourceId.number
                }
                resourceId = resourceId.substring(0, numPos);
            }

            return new FileInfo(type, resourceId);
        }
    }

    //Inserter的唯一实现
    @VisibleForTesting
    class InserterImpl implements Inserter {

        private final String mResourceId;

        @VisibleForTesting
        final File mTemporaryFile;

        public InserterImpl(String resourceId, File temporaryFile) {
            mResourceId = resourceId;
            mTemporaryFile = temporaryFile;
        }

        //向WriterCallback提供一个FileOutputStream，以供WriterCallback在客户端中向FileOutputStream写入数据
        @Override
        public void writeData(WriterCallback callback, Object debugInfo) throws IOException {
            FileOutputStream fileStream;
            try {
                fileStream = new FileOutputStream(mTemporaryFile);
            } catch (FileNotFoundException fne) {
                mCacheErrorLogger.logError(
                        CacheErrorLogger.CacheErrorCategory.WRITE_UPDATE_FILE_NOT_FOUND,
                        TAG,
                        "updateResource",
                        fne);
                throw fne;
            }

            long length;
            try {
                CountingOutputStream countingStream = new CountingOutputStream(fileStream);
                callback.write(countingStream);
                //以防底层流的关闭方法不flush，我们手动在try / catch中flush,
                // just in case underlying stream's close method doesn't flush:
                // we flush it manually and inside the try/catch
                countingStream.flush();
                length = countingStream.getCount();
            } finally {
                // if it fails to close (or write the last piece) we really want to know
                // Normally we would want this to be quiet because a closing exception would hide one
                // inside the try, but now we really want to know if something fails at flush or close
                fileStream.close();
            }
            // this code should never throw, but if filesystem doesn't fail on a failing/uncomplete close
            // we want to know and manually fail
            if (mTemporaryFile.length() != length) {
                throw new IncompleteFileException(length, mTemporaryFile.length());
            }
        }

        //将临时文件改名成内容文件，这样一来该缓存就对客户端可见了
        @Override
        public BinaryResource commit(Object debugInfo) throws IOException {
            File targetFile = getContentFileFor(mResourceId);

            try {
                FileUtils.rename(mTemporaryFile, targetFile);
            } catch (FileUtils.RenameException re) {
                CacheErrorLogger.CacheErrorCategory category;
                Throwable cause = re.getCause();
                if (cause == null) {
                    category = CacheErrorLogger.CacheErrorCategory.WRITE_RENAME_FILE_OTHER;
                } else if (cause instanceof FileUtils.ParentDirNotFoundException) {
                    category =
                            CacheErrorLogger.CacheErrorCategory.WRITE_RENAME_FILE_TEMPFILE_PARENT_NOT_FOUND;
                } else if (cause instanceof FileNotFoundException) {
                    category = CacheErrorLogger.CacheErrorCategory.WRITE_RENAME_FILE_TEMPFILE_NOT_FOUND;
                } else {
                    category = CacheErrorLogger.CacheErrorCategory.WRITE_RENAME_FILE_OTHER;
                }
                mCacheErrorLogger.logError(
                        category,
                        TAG,
                        "commit",
                        re);
                throw re;
            }
            if (targetFile.exists()) {
                targetFile.setLastModified(mClock.now());
            }
            return FileBinaryResource.createOrNull(targetFile);
        }

        @Override
        public boolean cleanUp() {
            return !mTemporaryFile.exists() || mTemporaryFile.delete();
        }
    }
}
