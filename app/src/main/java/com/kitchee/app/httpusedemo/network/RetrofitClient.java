package com.kitchee.app.httpusedemo.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.kitchee.app.httpusedemo.base.HttpUseApplication;
import com.kitchee.app.httpusedemo.cookie.CookieJarImpl;
import com.kitchee.app.httpusedemo.cookie.store.PersistentCookieStore;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by kitchee on 2018/6/11.
 * desc : retrofit客服端
 */

public class RetrofitClient {
    //服务端根路径
    public static String baseUrl = "https://api.douban.com/v2/movie/";
    //读超时长，单位：毫秒
    public static final int READ_TIME_OUT = 30;
    //连接时长，单位：毫秒
    public static final int CONNECT_TIME_OUT = 30;

    private static ApiService apiService;
    private static RetrofitClient client;
    private static OkHttpClient okHttpClient;
    private static Retrofit retrofit;

    /**
     * 设缓存有效期为两天
     */
    private static final long CACHE_STALE_SEC = 60 * 60 * 24 * 2;
    /**
     * 查询缓存的Cache-Control设置，为if-only-cache时只查询缓存而不会请求服务器，max-stale可以配合设置缓存失效时间
     * max-stale 指示客户机可以接收超出超时期间的响应消息。如果指定max-stale消息的值，那么客户机可接收超出超时期指定值之内的响应消息。
     */
    private static final String CACHE_CONTROL_CACHE = "only-if-cached, max-stale=" + CACHE_STALE_SEC;
    /**
     * 查询网络的Cache-Control设置，头部Cache-Control设为max-age=0
     * (假如请求了服务器并在a时刻返回响应结果，则在max-age规定的秒数内，浏览器将不会发送对应的请求到服务器，数据由缓存直接返回)时则不会使用缓存而请求服务器
     */
    private static final String CACHE_CONTROL_AGE = "max-age=0";

    public static ApiService getApiService() {
        return apiService;

    }

    public static RetrofitClient newInstance() {
        if (client == null) {
            synchronized (RetrofitClient.class) {
                client = new RetrofitClient();
            }
        }
        return client;
    }

    private RetrofitClient() {
        init();
    }

    private void init() {

        // 添加头部
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json;versions=1");
//        headers.put("Content-Type","application/json");
        // 添加缓存
        File cacheFile = new File(HttpUseApplication.application.getCacheDir(), "cache_retrofit");
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 100); //100Mb

        // 开启Log
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // 创建OkHttpClient
        okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)//默认连接失败后重试一次/
                .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
                .cache(cache)
                .cookieJar(new CookieJarImpl(new PersistentCookieStore(HttpUseApplication.application.getApplicationContext())))
                .addInterceptor(new HttpHeadInterceptor(headers))
                .addInterceptor(new NetCacheInterceptor(HttpUseApplication.application.getApplicationContext()))
                .addInterceptor(logInterceptor)
                .connectionPool(new ConnectionPool(8, 15, TimeUnit.SECONDS))
                .hostnameVerifier(new HostnameVerifier() {
                    @SuppressLint("BadHostnameVerifier")
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .build();

        // 创建retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

    }


    private class HttpHeadInterceptor implements Interceptor {

        private Map<String, String> headers;

        HttpHeadInterceptor(Map<String, String> headers) {
            super();
            this.headers = headers;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            // 获取请求
            Request request = chain.request();
            // 获取请求的建造者
            Request.Builder builder = request.newBuilder();
            // 判断是否需要添加头部
            if (headers != null && headers.size() > 0) {
                //头部不为空，需要添加
                Set<String> keys = headers.keySet();
                for (String headerKey : keys) {
                    builder.addHeader(headerKey, headers.get(headerKey));
                }
            }
            // 请求链继续
            return chain.proceed(builder.build());
        }
    }

    private class NetCacheInterceptor implements Interceptor {

        private Context context;

        NetCacheInterceptor(Context context) {
            super();
            this.context = context;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {

            CacheControl.Builder cacheBuilder = new CacheControl.Builder();
            cacheBuilder.maxAge(0, TimeUnit.SECONDS);
            cacheBuilder.maxStale(30, TimeUnit.DAYS);
            CacheControl cacheControl = cacheBuilder.build();

            Request request = chain.request();
            if (!CheckNetWork.isNetworkConnected(context)) {
                // 没有网络，重新构建缓存控制,使得相关响应返回

                request = request.newBuilder().cacheControl(cacheControl).build();
            }
            Response originalResponse = chain.proceed(request);
            if (CheckNetWork.isNetworkConnected(context)) {
                // 在线缓存 0s,表示当前时刻接口返回内容在0s内都是生效状态，即无论有网无网都读缓存
                return originalResponse.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public," + CACHE_CONTROL_AGE)
                        .build();
            } else {
                // 离线缓存，2天，
                return originalResponse.newBuilder()
                        //移除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", "public," + CACHE_CONTROL_CACHE)
                        .build();
            }

        }
    }

    private class ReceivedCookiesInterceptor implements Interceptor {
        private Context context;

        ReceivedCookiesInterceptor(Context context) {
            super();
            this.context = context;

        }

        @Override
        public Response intercept(Chain chain) throws IOException {

            Response originalResponse = chain.proceed(chain.request());
            //这里获取请求返回的cookie
            if (!originalResponse.headers("Set-Cookie").isEmpty()) {

                List<String> d = originalResponse.headers("Set-Cookie");
//                Log.e("jing", "------------得到的 cookies:" + d.toString());

                // 返回cookie
                if (!TextUtils.isEmpty(d.toString())) {

                    SharedPreferences sharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editorConfig = sharedPreferences.edit();
                    String oldCookie = sharedPreferences.getString("cookie", "");

                    HashMap<String, String> stringStringHashMap = new HashMap<>();

                    // 之前存过cookie
                    if (!TextUtils.isEmpty(oldCookie)) {
                        String[] substring = oldCookie.split(";");
                        for (String aSubstring : substring) {
                            if (aSubstring.contains("=")) {
                                String[] split = aSubstring.split("=");
                                stringStringHashMap.put(split[0], split[1]);
                            } else {
                                stringStringHashMap.put(aSubstring, "");
                            }
                        }
                    }
                    String join = StringUtils.join(d, ";");
                    String[] split = join.split(";");

                    // 存到Map里
                    for (String aSplit : split) {
                        String[] split1 = aSplit.split("=");
                        if (split1.length == 2) {
                            stringStringHashMap.put(split1[0], split1[1]);
                        } else {
                            stringStringHashMap.put(split1[0], "");
                        }
                    }

                    // 取出来
                    StringBuilder stringBuilder = new StringBuilder();
                    if (stringStringHashMap.size() > 0) {
                        for (String key : stringStringHashMap.keySet()) {
                            stringBuilder.append(key);
                            String value = stringStringHashMap.get(key);
                            if (!TextUtils.isEmpty(value)) {
                                stringBuilder.append("=");
                                stringBuilder.append(value);
                            }
                            stringBuilder.append(";");
                        }
                    }

                    editorConfig.putString("cookie", stringBuilder.toString());
                    editorConfig.apply();
//                    Log.e("jing", "------------处理后的 cookies:" + stringBuilder.toString());
                }
            }

            return originalResponse;
        }
    }

    private class AddCookiesInterceptor implements Interceptor {
        private Context context;

        AddCookiesInterceptor(Context context) {
            super();
            this.context = context;

        }

        @Override
        public Response intercept(Chain chain) throws IOException {

            final Request.Builder builder = chain.request().newBuilder();
            SharedPreferences sharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
            String cookie = sharedPreferences.getString("cookie", "");
            builder.addHeader("Cookie", cookie);
            return chain.proceed(builder.build());
        }
    }

    /**
     * create you ApiService
     * Create an implementation of the API endpoints defined by the {@code service} interface.
     */
    public <T> T create(final Class<T> service) {
        if (service == null) {
            throw new RuntimeException("Api service is null!");
        }
        if (retrofit == null) {
            throw new RuntimeException("retrofit is null,create retrofit first please!");
        }
        return retrofit.create(service);
    }

    /**
     * /**
     * execute your customer API
     * For example:
     * MyApiService service =
     * RetrofitClient.getInstance(MainActivity.this).create(MyApiService.class);
     * <p>
     * RetrofitClient.getInstance(MainActivity.this)
     * .execute(service.lgon("name", "password"), subscriber)
     * * @param subscriber
     */

    public static <T> T execute(Observable<T> observable, Observer<T> subscriber) {
        observable.subscribeOn(Schedulers.io())
//                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);

        return null;
    }


}
