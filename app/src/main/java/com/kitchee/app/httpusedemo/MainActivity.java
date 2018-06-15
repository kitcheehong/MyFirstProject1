package com.kitchee.app.httpusedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.kitchee.app.httpusedemo.movieModule.Movie;
import com.kitchee.app.httpusedemo.network.ApiService;
import com.kitchee.app.httpusedemo.network.MyObserver;
import com.kitchee.app.httpusedemo.network.RetrofitClient;
import com.kitchee.app.httpusedemo.network.RetrofitUtil;
import com.orhanobut.logger.Logger;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "kitcheehong";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = findViewById(R.id.text);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                RetrofitClient.execute(RetrofitClient.newInstance().create(ApiService.class).getTopMovie(0, 20), new Observer<Movie>() {
//                            @Override
//                            public void onSubscribe(Disposable d) {
////                                d.dispose();
//                                Log.d(TAG, "onSubscribe: ---->");
//                            }
//
//                            @Override
//                            public void onNext(Movie movie) {
//                                Log.d("kitcheehong","movie = "+movie.title);
//                                Log.d("kitcheehong","movie = "+movie.count);
//                                Log.d("kitcheehong","movie = "+movie.list.toArray());
//                                Log.d("kitcheehong","movie = "+movie.list.size());
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//                                Log.d(TAG, "onError: --->");
//                            }
//
//                            @Override
//                            public void onComplete() {
//                                Log.d(TAG, "onComplete: ---->");
//                            }
//                        }
//
//                );

                RetrofitUtil.getTopMovie(0,10,new MyObserver<Movie>(MainActivity.this, new MyObserver.ObserverOnNextListener() {
                    @Override
                    public void onNext(Object o) {
                        Toast.makeText(MainActivity.this, "已经运行到这里", Toast.LENGTH_SHORT).show();
                        if (o instanceof Movie){
                            Logger.d(((Movie)o).title);
                            Logger.d(((Movie)o).count);
                            Logger.d(((Movie)o).list.toArray());
                            Logger.d(((Movie)o).list.size());
                        }
                    }
                }));

//                RetrofitUtil.getTopMovie(0,10,new MyObserver<Movie>(new Consumer<Movie>() {
//                    @Override
//                    public void accept(Movie o) throws Exception {
//                        Toast.makeText(MainActivity.this, "已经运行到这里", Toast.LENGTH_LONG).show();
//                        if (o instanceof Movie){
//                            Logger.d(((Movie)o).title);
//                            Logger.d(((Movie)o).count);
//                            Logger.d(((Movie)o).list.toArray());
//                            Logger.d(((Movie)o).list.size());
//                        }
//                    }
//                }));


            }
        });
    }
}
