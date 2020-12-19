package com.veltech.firebase.ravi.veltechiantalks;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;


public class WelcomePage extends AppCompatActivity {

    private TextView textView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        textView = (TextView) findViewById(R.id.tv);
        imageView = (ImageView)findViewById(R.id.iv);

        Animation animation = AnimationUtils.loadAnimation(this,R.anim.bounce);

        Animation anim=AnimationUtils.loadAnimation(this,R.anim.transistion);
        textView.startAnimation(anim);
        imageView.startAnimation(animation);

        Thread time = new Thread()
        {
            public void run()
            {
                try{
                    sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                finally {
                    startActivity(new Intent(WelcomePage.this,MainActivity.class));
                    finish();
                }
            }
        };

        time.start();



    }
}
