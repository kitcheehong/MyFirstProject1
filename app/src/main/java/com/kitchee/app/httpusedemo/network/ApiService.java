package com.kitchee.app.httpusedemo.network;





import com.kitchee.app.httpusedemo.movieModule.Movie;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by kitchee on 2018/6/11.
 * desc :
 */

public interface ApiService {
@GET("top250")
Observable<Movie> getTopMovie(@Query("start") int start, @Query("count") int count);


}
