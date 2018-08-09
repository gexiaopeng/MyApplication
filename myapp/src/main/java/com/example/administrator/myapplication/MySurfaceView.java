package com.example.administrator.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback{
    private SurfaceHolder holder;
    private Matrix matrix = new Matrix();
    private FFmpegFrameGrabber grabber = null;
    private Bitmap bitmap;
    private Handler rtspHandler= new Handler();
    private Handler uiHandler=new Handler(Looper.getMainLooper());
    private AndroidFrameConverter convertToBitmap = new AndroidFrameConverter();
    private MyCallback  myCallback;
    private String msg;
    public MySurfaceView( ){
        super(null);
    }
    public MySurfaceView(Context context) {
           super(context);
            // 通过SurfaceView获得SurfaceHolder对象
            holder = getHolder();
            // 为holder添加回调结构SurfaceHolder.Callback
            holder.addCallback(this);

    }
    public void setMyCallback(MyCallback  myCallback){
        this.myCallback=myCallback;
    }
   public void play(){
        if( start() ){
            rtspHandler.post(rtspRun);
        }
    }
    public void stop(){
         if(grabber!=null){
             try {
                 grabber.flush();
                 grabber.stop();
             } catch (Exception e) {
             }
       }
    }
    public String getFFmpegFrameGrabber(){
        return msg;
    }
    private  void drawBitmap(){
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            return;
        }
        matrix.reset();
        float scale = 1.0f;
        matrix.setScale(scale, scale);
        canvas.drawBitmap(bitmap, matrix, null);
        holder.unlockCanvasAndPost(canvas);
    }
    private void startRtsp(){
        try {
            Frame img = null;
            try {
                img = grabber.grabImage();
                if (img != null) {
                    bitmap = convertToBitmap.convert(img);
                    runUiThread();
                 } else {
                    grabber.restart();

                }
          } catch (Exception e) {
                grabber.restart();

            }
            rtspHandler.post(rtspRun);
       }catch (Throwable e){

        }
    }
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
       @Override
    public void surfaceCreated(SurfaceHolder holder) {
         msg="surfaceCreated";
//           try {
//               String url="rtsp://admin:adminINLJHY@192.168.10.235:554/h264/ch1/av_stream";
//               grabber= new FFmpegFrameGrabber(url);
//               grabber.setOption("rtsp_transport", "tcp");
//               grabber.setImageHeight(getHeight());
//               grabber.setImageWidth(getWidth());
//              // grabber.setFrameRate(30f);
//               msg+=",ok!";
//        } catch (Throwable e) {
//               msg+=",error:"+e.getMessage();
//
//           }
           if(myCallback!=null ){
               myCallback.sendMsg(msg);
           }
    }
    public interface MyCallback{
        public void sendMsg(String msg);
    }
    private  boolean start() {
        try {
            if(grabber!=null) {
                grabber.start();
                return true;
            }
        } catch (FrameGrabber.Exception e2) {
            try {
                grabber.restart();
                return true;
            } catch (FrameGrabber.Exception e) {
                try {
                    grabber.stop();
                } catch (FrameGrabber.Exception e1) {
                 }
            }
        }
        return false;
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
