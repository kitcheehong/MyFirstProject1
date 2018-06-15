package com.kitchee.app.httpusedemo.movieModule;

import android.databinding.ObservableField;

/**
 * Created by kitchee on 2018/6/11.
 * desc :
 */

public class Subjects {
//    private String title, year, id;
    public final ObservableField<String> title = new ObservableField<>();
    public final ObservableField<String> year = new ObservableField<>();
    public final ObservableField<String> id = new ObservableField<>();
}
