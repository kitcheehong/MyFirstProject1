package com.kitchee.app.httpusedemo.network;

import android.content.Context;

import com.kitchee.app.httpusedemo.movieModule.Movie;
import com.orhanobut.logger.Logger;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by kitchee on 2018/6/15.
 * desc :
 */

public class MyObserver<T> implements Observer<T> {

    private ObserverOnNextListener listener;
    private Context context;
    private Consumer<T> consumer;


    public MyObserver(Context context,ObserverOnNextListener listener){
        this.listener = listener;
        this.context = context;
    }

    public MyObserver(Consumer<T> consumer){
        this.consumer = consumer;
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(Object o) {
        listener.onNext(o);
        Logger.d(o.toString());
    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }

    public interface ObserverOnNextListener<T>{
        void onNext(T t);
    }
}
