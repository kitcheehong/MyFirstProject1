package com.kitchee.app.httpusedemo.network;

import com.kitchee.app.httpusedemo.movieModule.Movie;

/**
 * Created by kitchee on 2018/6/15.
 * desc :
 */

public class RetrofitUtil {

    // 获取top250的电影
    public static void getTopMovie(int start,int count,MyObserver<Movie> observer){
        RetrofitClient.execute(RetrofitClient.newInstance().create(ApiService.class).getTopMovie(start,count),observer);
    }
}
