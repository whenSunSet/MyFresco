package com.facebook.drawee.controller;

/**
 * Created by heshixiyang on 2017/3/10.
 */
/**
 * A listener for {@link AbstractDraweeController} that listens to events regarding visibility of
 * the drawee in the viewport. As Android does not provide these events, the client must call
 * {@link AbstractDraweeController#onViewportVisibilityHint(boolean)} accordingly.
 */
public interface ControllerViewportVisibilityListener {

    /**
     * Called after a client has given the {@link AbstractDraweeController} a hint that the view
     * became visible in the viewport.
     *
     * @param id controller id
     */
    void onDraweeViewportEntry(String id);

    /**
     * Called after a client has given the {@link AbstractDraweeController} a hint that the view
     * is no longer visible in the viewport.
     *
     * @param id controller id
     */
    void onDraweeViewportExit(String id);
}
