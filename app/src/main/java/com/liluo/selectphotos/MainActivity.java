package com.liluo.selectphotos;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.Gson;
import com.liluo.library.SelectPhotoView;
import java.util.List;
public class MainActivity extends AppCompatActivity {
    private SelectPhotoView selectPhotoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectPhotoView = (SelectPhotoView) findViewById(R.id.upv_sporttask);
        selectPhotoView.bind(this);
        selectPhotoView.setMaxPhotos(5);
        selectPhotoView.setCamera(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SelectPhotoView.REQUEST_CODE) {
            selectPhotoView.onActivityResult(requestCode, resultCode, data);
            List<SelectPhotoView.PhotoBean> pics = selectPhotoView.getAttachments();
            Log.e("liluo","pics"+new Gson().toJson(pics));
            //selectPhotoView.getAttachments();这是返回地址
        }
    }
}
