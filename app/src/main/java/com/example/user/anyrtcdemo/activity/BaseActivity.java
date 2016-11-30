package com.example.user.anyrtcdemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
    }

    @Override
    protected void onResume() {
        super.onResume();
      }

    @Override
    protected void onStart() {
        super.onStart();
      }

    public void back(View view) {
        finish();
    }

}
