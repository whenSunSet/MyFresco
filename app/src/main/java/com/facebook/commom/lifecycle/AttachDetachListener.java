package com.facebook.commom.lifecycle;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import android.view.View;

/**
 * Attach detach listener.
 */
public interface AttachDetachListener {

    void onAttachToView(View view);

    void onDetachFromView(View view);
}
