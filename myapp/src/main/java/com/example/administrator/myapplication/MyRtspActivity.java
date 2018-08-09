package com.example.administrator.myapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

public class MyRtspActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    private TextureView textureView;



    private TextView text1;
   private  FFmpegFrameGrabber grabber = null;
   private  AndroidFrameConverter convertToBitmap = new AndroidFrameConverter();
    private Handler handler = new Handler();
    private Matrix matrix = new Matrix();
    private HandlerThread previewThread ;
    private Handler previewHandler;
    private Handler uiHandler;
    private Handler rtspHandler;
    private Bitmap bitmap;
    private int seq;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rtsp);
        textureView = (TextureView) findViewById(R.id.texture_view);
         textureView.setSurfaceTextureListener(this);

       // textureView.setOpaque(false);
       // textureView.setKeepScreenOn(true);

       text1 = (TextView) findViewById(R.id.text2);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 保持常亮的屏幕的状态
    }

    protected  void onStart() {
        super.onStart();
        previewThread = new HandlerThread("rtsp preview");
        previewThread.start();
        previewHandler = new Handler(previewThread.getLooper());
        uiHandler = new Handler(Looper.getMainLooper());
        rtspHandler = new Handler();
        // 测试获取视频流并截取其中一张图片
        // 其中视频流可以自己在VLC中搭建


    }
    private void startRtsp(){
        if(seq>100){
            seq=0;
        }
        try {
               Frame img = null;
                try {
                       img = grabber.grabImage();
                        if (img != null) {
                            bitmap = convertToBitmap.convert(img);
                           // text1.append("\r\nbitmap:"+bitmap.getWidth()+","+bitmap.getHeight()+"|"+textureView.getHeight()+","+textureView.getWidth());
                            //previewHandler.post(drawPreviewRun);
                            runUiThread();
                          // drawBitmap();
                           // text1.setText("\r\n帧："+bitmap.getWidth()+","+bitmap.getHeight()+"|"+(++seq));
                        } else {
                            grabber.restart();
                            text1.setText("\r\n没有帧："+(++seq));
                       }

                       //Thread.sleep(1000);
                } catch (Exception e) {
                    grabber.restart();
                    text1.setText("\r\n丢帧："+(++seq));
                }
            rtspHandler.post(rtspRun);
           // stop(grabber);
           // text1.append("\r\n结束视频流");
        }catch (Throwable e){
            text1.setText("startRtsp error:"+e.getMessage());
        }
    }
    private boolean initRtsp(){
        String url = "rtsp://admin:adminINLJHY@192.168.10.235:554/h264/ch1/av_stream";
        grabber= new FFmpegFrameGrabber(url);
        grabber.setOption("rtsp_transport", "tcp");
        grabber.setImageHeight(getHeight());
        grabber.setImageWidth(getWidth());
        grabber.setFrameRate(30f);
       return start(grabber);
    }
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
       // text1.append("\r\nonSurfaceTextureAvailable："+width+","+height+","+"|"+textureView.getHeight()+","+textureView.getWidth()+","+textureView.lockCanvas());
        //startRtsp();
        //previewHandler.post(drawPreviewRun);
        if(initRtsp()) {
           rtspHandler.post(rtspRun);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
    private Runnable drawPreviewRun = new Runnable() {
        @Override
        public void run() {
            runUiThread();
           // startRtsp();

        }
    };
    private Runnable rtspRun = new Runnable() {
        @Override
        public void run() {
             startRtsp();

        }
    };
    private void runUiThread() {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                drawBitmap();
            }
        });
    }
    private void drawBitmap(){
        Canvas canvas = textureView.lockCanvas();
        //text1.append("\r\nCanvas:"+canvas+","+ textureView.getWidth()+","+textureView.getHeight());
        if (canvas == null) {
            return;
        }
        matrix.reset();
        float scale = 1.0f * textureView.getWidth() / getWidth();
        matrix.setScale(scale, scale);
        canvas.drawBitmap(bitmap, matrix, null);
        textureView.unlockCanvasAndPost(canvas);
    }
    private int getWidth(){
        int width=1920;
        width=720;
        return width;
    }
    private int getHeight(){
        int heigth=1080;
        heigth=480;
        return heigth;
    }
    protected  void onStop(){
        if(grabber!=null){
            stop(grabber);
        }
         super.onStop();
    }
    public  boolean start(FrameGrabber grabber) {
        try {
            grabber.start();
            return true;
        } catch (FrameGrabber.Exception e2) {
            // System.err.println("第一次打开失败，重新开始");
            text1.append("\r\n第一次打开失败，重新开始,error:"+e2.getLocalizedMessage());
            //Toast.makeText(MainActivity.this, "第一次打开失败，重新开始,error:"+e2.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            try {
                grabber.restart();
                return true;
            } catch (FrameGrabber.Exception e) {
                try {
                    //System.err.println("重启抓取器失败，正在关闭抓取器...");
                    text1.append("\r\n重启抓取器失败，正在关闭抓取器...error:"+e2.getLocalizedMessage());
                    // Toast.makeText(MainActivity.this, "重启抓取器失败，正在关闭抓取器...error:"+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    grabber.stop();
                } catch (FrameGrabber.Exception e1) {
                    //System.err.println("停止抓取器失败！");
                    Toast.makeText(MyRtspActivity.this, "停止抓取器失败！", Toast.LENGTH_LONG).show();
                }
            }
        }
        return false;
    }

    public  boolean stop(FrameGrabber grabber) {
        try {
            grabber.flush();
            grabber.stop();
            return true;
        } catch (FrameGrabber.Exception e) {
            return false;
        } finally {
            try {
                grabber.stop();
            } catch (FrameGrabber.Exception e) {
                //System.err.println("关闭抓取器失败");
                Toast.makeText(MyRtspActivity.this, "关闭抓取器失败", Toast.LENGTH_LONG).show();
            }
        }
    }
}
