package com.facebook.imagepipeline.producers.base;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.imagepipeline.producers.NetworkFetcher;
import com.facebook.imagepipeline.producers.networkFetchAndProducer.FetchState;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * {@link NetworkFetcher}的默认实现
 * Base class for {@link NetworkFetcher}.
 *
 * <p> Intermediate results are propagated.
 * <p> {#code getExtraMap} returns null.
 */
public abstract class BaseNetworkFetcher<FETCH_STATE extends FetchState>
        implements NetworkFetcher<FETCH_STATE> {

    @Override
    public boolean shouldPropagate(FETCH_STATE fetchState) {
        return true;
    }

    @Override
    public void onFetchCompletion(FETCH_STATE fetchState, int byteSize) {
        // no-op
    }

    @Nullable
    @Override
    public Map<String, String> getExtraMap(FETCH_STATE fetchState, int byteSize) {
        return null;
    }
}
