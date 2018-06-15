package com.kitchee.app.httpusedemo.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by kitchee on 2018/6/14.
 * desc :
 */

public class CheckNetWork {

    /**
     * 网络是否连接
     * @param context 上下文对象
     * @return 判断结果
     */
    public static boolean isNetworkConnected(Context context){
        if(context != null){
            // 获取手机的连接管理器
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            // 获取网络信息
            if(cm == null){
                return false;
            }
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()){
                return true;
            }
            return false;
        }else {
            return false;
        }

    }
}
