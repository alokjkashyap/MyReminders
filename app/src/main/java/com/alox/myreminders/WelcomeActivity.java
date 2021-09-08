package com.alox.myreminders;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

public class WelcomeActivity extends AppCompatActivity {

    SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mPreferences = getSharedPreferences("rems",MODE_PRIVATE);

        Boolean notNew = mPreferences.getBoolean("notNew",false);
        FrameLayout continueBtn = findViewById(R.id.continueBtn);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean("notNew",true);
                editor.apply();

                startActivity(new Intent(WelcomeActivity.this,MainActivity.class));
                finish();
            }
        });
    }
}