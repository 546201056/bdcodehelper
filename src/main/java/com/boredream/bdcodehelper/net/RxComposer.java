package com.boredream.bdcodehelper.net;

import com.boredream.bdcodehelper.entity.CloudResponse;
import com.boredream.bdcodehelper.entity.ListResponse;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Rx組件
 */
public class RxComposer {

    public static <T> ObservableTransformer<CloudResponse<T>, T> handleCloudResponse() {
        return new ObservableTransformer<CloudResponse<T>, T>() {
            @Override
            public ObservableSource<T> apply(@NonNull Observable<CloudResponse<T>> upstream) {
                return upstream.flatMap(new Function<CloudResponse<T>, ObservableSource<T>>() {
                    @Override
                    public ObservableSource<T> apply(@NonNull CloudResponse<T> response) throws Exception {
                        if (response.success()) {
                            return Observable.just(response.getResult());
                        } else {
                            // 云引擎error会走200成功回调，要特殊处理下
                            return Observable.error(new LeanCloudError(response));
                        }
                    }
                });
            }
        };
    }

    public static <T> ObservableTransformer<ListResponse<T>, ArrayList<T>> handleListResponse() {
        return new ObservableTransformer<ListResponse<T>, ArrayList<T>>() {
            @Override
            public ObservableSource<ArrayList<T>> apply(@NonNull Observable<ListResponse<T>> upstream) {
                return upstream.flatMap(new Function<ListResponse<T>, ObservableSource<ArrayList<T>>>() {
                    @Override
                    public ObservableSource<ArrayList<T>> apply(@NonNull ListResponse<T> response) throws Exception {
                        ArrayList<T> results = response.getResults();
                        return Observable.just(results == null ? new ArrayList<T>() : results);
                    }
                });
            }
        };
    }

    public static <T> ObservableTransformer<T, T> schedulers() {
        return new ObservableTransformer<T, T>(){
            @Override
            public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
}
