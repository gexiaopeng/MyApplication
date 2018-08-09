package com.example.administrator.myapplication;


import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.view.WindowManager;
import android.widget.Toast;



public class MyTestActivity extends AppCompatActivity {

    private MySurfaceView  mySurfaceView;
    private int seq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_test);
       // textureView = (TextureView) findViewById(R.id.texture_view);
       // textureView.setSurfaceTextureListener(this);
       // mySurfaceView=new MySurfaceView(this);
        mySurfaceView=findViewById(R.id.mySurfaceView);
        mySurfaceView.setMyCallback(myCallback);
       // setContentView(mySurfaceView);
       // textureView.setOpaque(false);
       // textureView.setKeepScreenOn(true);

       // text1 = (TextView) findViewById(R.id.text2);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 保持常亮的屏幕的状态
    }
    private MySurfaceView.MyCallback  myCallback=new MySurfaceView.MyCallback(){
     @Override
        public void sendMsg(String msg) {
           Toast.makeText(MyTestActivity.this, "sendMsg :"+msg+"("+seq+")", Toast.LENGTH_LONG).show();
        // mySurfaceView.play();

        }
    };
    protected  void onStart() {
        super.onStart();
        seq++;
        if(mySurfaceView!=null) {
            Toast.makeText(MyTestActivity.this, "onStart :" + mySurfaceView.getFFmpegFrameGrabber(), Toast.LENGTH_LONG).show();
        }
    }

}
