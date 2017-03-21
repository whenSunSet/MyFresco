package com.facebook.commom.disk;

/**
 * Created by heshixiyang on 2017/3/12.
 */
/**
 * Implementation of {@link DiskTrimmableRegistry} that does not do anything.
 */
public class NoOpDiskTrimmableRegistry implements DiskTrimmableRegistry {
    private static NoOpDiskTrimmableRegistry sInstance = null;

    private NoOpDiskTrimmableRegistry() {
    }

    public static synchronized NoOpDiskTrimmableRegistry getInstance() {
        if (sInstance == null) {
            sInstance = new NoOpDiskTrimmableRegistry();
        }
        return sInstance;
    }

    @Override
    public void registerDiskTrimmable(DiskTrimmable trimmable) {
    }

    @Override
    public void unregisterDiskTrimmable(DiskTrimmable trimmable) {
    }
}
