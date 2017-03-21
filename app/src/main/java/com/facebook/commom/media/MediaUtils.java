package com.facebook.commom.media;

/**
 * Created by heshixiyang on 2017/3/12.
 */

import android.webkit.MimeTypeMap;

import com.facebook.commom.internal.ImmutableMap;

import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Utility class.
 */
public class MediaUtils {
    //额外的mime类型,我们知道一个特定的媒体类型,但可能不会设备上的本地支持
    // Additional mime types that we know to be a particular media type but which may not be
    // supported natively on the device.
    public static final Map<String, String> ADDITIONAL_ALLOWED_MIME_TYPES =
            ImmutableMap.of("mkv", "video/x-matroska");

    public static boolean isPhoto(@Nullable String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public static boolean isVideo(@Nullable String mimeType) {
        return mimeType != null && mimeType.startsWith("video/");
    }

    public @Nullable static String extractMime(String path) {
        String extension = extractExtension(path);
        if (extension == null) {
            return null;
        }
        extension = extension.toLowerCase(Locale.US);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        // If we did not find a mime type for the extension specified, check our additional
        // extension/mime-type mappings.
        if (mimeType == null) {
            mimeType = ADDITIONAL_ALLOWED_MIME_TYPES.get(extension);
        }
        return mimeType;
    }

    private @Nullable static String extractExtension(String path) {
        int pos = path.lastIndexOf('.');
        if (pos < 0 || pos == path.length() - 1) {
            return null;
        }
        return path.substring(pos + 1);
    }

    /**
     * @return 返回true如果mime类型是我们的一个白名单mimetype,我们支持超出*本机平台支持
     * true if the mime type is one of our whitelisted mimetypes that we support beyond
     *         what the native platform supports.
     */
    public static boolean isNonNativeSupportedMimeType(String mimeType) {
        return ADDITIONAL_ALLOWED_MIME_TYPES.containsValue(mimeType);
    }
}
