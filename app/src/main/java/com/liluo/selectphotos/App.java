package com.liluo.selectphotos;

import android.app.Application;

import com.liluo.library.util.Utils;

/**
 * Description:
 * Copyright  : Copyright (c) 2018
 * Company    : mixiong
 * Author     : zhanglei
 * Date       : 2018/5/7 13:59
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(getApplicationContext());
    }
}
