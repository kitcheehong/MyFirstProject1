package com.kitchee.app.httpusedemo.movieModule;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;

import java.util.List;

/**
 * Created by kitchee on 2018/6/11.
 * desc :
 */

public class Movie {
    public final ObservableField<Integer> count = new ObservableField<>();
    public final ObservableField<String> title = new ObservableField<>();
    public final List<Subjects> list = new ObservableArrayList<>();

}
