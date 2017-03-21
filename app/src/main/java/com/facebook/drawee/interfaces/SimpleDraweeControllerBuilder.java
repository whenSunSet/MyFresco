package com.facebook.drawee.interfaces;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Interface for simple Drawee controller builders.
 */
public interface SimpleDraweeControllerBuilder {

    /** Sets the caller context. */
    SimpleDraweeControllerBuilder setCallerContext(Object callerContext);

    /** Sets the uri. */
    SimpleDraweeControllerBuilder setUri(Uri uri);

    /** Sets the uri from a string. */
    SimpleDraweeControllerBuilder setUri(@Nullable String uriString);

    /** Sets the old controller to be reused if possible. */
    SimpleDraweeControllerBuilder setOldController(@Nullable DraweeController oldController);

    /** Builds the specified controller. */
    DraweeController build();
}
