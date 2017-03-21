package com.facebook.commom.soloader;

/**
 * Created by Administrator on 2017/3/13 0013.
 */
/**
 * 一个So加载器
 * A shim for loading shared libraries that the app can override.
 */
public class SoLoaderShim {

    /**
     * Handler that can be overridden by the application.
     */
    public interface Handler {

        void loadLibrary(String libraryName);
    }

    /**
     * Default handler for loading libraries.
     */
    public static class DefaultHandler implements Handler {

        @Override
        public void loadLibrary(String libraryName) {
            System.loadLibrary(libraryName);
        }
    }

    private static volatile Handler sHandler = new DefaultHandler();

    /**
     * Sets the handler.
     *
     * @param handler the new handler
     */
    public static void setHandler(Handler handler) {
        if (handler == null) {
            throw new NullPointerException("Handler cannot be null");
        }
        sHandler = handler;
    }

    /**
     * See {@link Runtime#loadLibrary}.
     *
     * @param libraryName the library to load
     */
    public static void loadLibrary(String libraryName) {
        sHandler.loadLibrary(libraryName);
    }

    public static void setInTestMode() {
        setHandler(
                new Handler() {
                    @Override
                    public void loadLibrary(String libraryName) {
                    }
                });
    }
}
