package com.example.administrator.myapplication;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;

public class MainActivity extends AppCompatActivity {

    private SurfaceView videoSurface = null;
    CameraManager cameraManager;
    private VideoView videoView = null;
    private boolean isPlaying = false;
    // private TextView sign;
    private Runnable progressRunnable = null;

    private View finishView = null;
    private Button finishButton = null;
    private Button swithButton = null;
    //Handler handler = null;
    ImageView imageView;
    TextView text1;
    AndroidFrameConverter convertToBitmap = new AndroidFrameConverter();
    FFmpegFrameGrabber grabber = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text1 = (TextView) findViewById(R.id.text1);
        imageView = (ImageView) findViewById(R.id.img1);
        // 测试读取图片
        //Drawable drawable = idToDrawable(R.mipmap.ic_launcher);

        // Bitmap bitmap = this.drawableToBitmap(drawable);
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
//        IplImage iplImage = this.bitmapToIplImage(bitmap);
//        // 反转图像
//        cvFlip(iplImage,iplImage,0);
//        bitmap = this.IplImageToBitmap(iplImage);
//        imageView.setImageBitmap(bitmap);


    }
   protected  void onStart() {
     super.onStart();
       // 测试获取视频流并截取其中一张图片
       // 其中视频流可以自己在VLC中搭建
       try {
           String url = "rtsp://admin:adminINLJHY@192.168.10.235:554/h264/ch1/av_stream";
           //grabber= FFmpegFrameGrabber.createDefault(url);
           grabber= new FFmpegFrameGrabber(url);
           grabber.setOption("rtsp_transport", "tcp");
           //grabber.setFormat("H264");
           //grabber.setFrameRate(30);
          // grabber.setVideoCodec(0x1C);
           //grabber.setImageWidth(720);
          // grabber.setImageHeight(480);
          // grabber = new FFmpegFrameGrabber(url);
           //grabber.setFormat("rtsp");
          // CanvasFrame g;
           Bitmap bitmap = null;
           if (start(grabber)) {
               text1.append("\r\nstart(grabber)");
               Frame img = null;
               try {
                   img = grabber.grabImage();
                   if (img != null) {
                       bitmap = convertToBitmap.convert(img);
                       imageView.setImageBitmap(bitmap);
                       //frame.showImage(img);
                       if (img.keyFrame) {
                           text1.append("\r\n关键帧：");
                           //System.err.println("关键帧：" + img.image);
                       } else {
                           text1.append("\r\nnot 关键帧：");
                       }
                   } else {
                       text1.append("\r\n没有帧：");
                       //System.err.println("没有帧");
                   }
                   Thread.sleep(40);
               } catch (Exception e) {
                   text1.append("\r\n丢帧：");
                   //System.err.println("丢帧");
               }
           } else {
               text1.append("\r\n不能打开视频流");
           }
           stop(grabber);
           text1.append("\r\n结束视频流");
       }catch (Throwable e){
           text1.setText("onStart error:"+e.getMessage());
       }
   }
    /**
     * IplImage转化为Bitmap
     *
     * @param iplImage
     * @return
     */
    public Bitmap IplImageToBitmap(opencv_core.IplImage iplImage) {
        Bitmap bitmap = null;
        bitmap = Bitmap.createBitmap(iplImage.width(), iplImage.height(),
                Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(iplImage.getByteBuffer());
        return bitmap;
    }

    /**
     * Bitmap转化为IplImage
     *
     * @param bitmap
     * @return
     */
    public opencv_core.IplImage bitmapToIplImage(Bitmap bitmap) {
        opencv_core.IplImage iplImage;
        iplImage = opencv_core.IplImage.create(bitmap.getWidth(), bitmap.getHeight(),
                IPL_DEPTH_8U, 4);
        bitmap.copyPixelsToBuffer(iplImage.getByteBuffer());
        return iplImage;
    }

    /**
     * 将资源ID转化为Drawable
     *
     * @param id
     * @return
     */
    public Drawable idToDrawable(int id) {
        return this.getResources().getDrawable(id);
    }

    /**
     * 将Drawable转化为Bitmap
     *
     * @param drawable
     * @return
     */
    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null)
            return null;
        return ((BitmapDrawable) drawable).getBitmap();
    }

    public  boolean start(FrameGrabber grabber) {
        try {
            grabber.start();
            return true;
        } catch (org.bytedeco.javacv.FrameGrabber.Exception e2) {
           // System.err.println("第一次打开失败，重新开始");
            text1.append("\r\n第一次打开失败，重新开始,error:"+e2.getLocalizedMessage());
            //Toast.makeText(MainActivity.this, "第一次打开失败，重新开始,error:"+e2.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            try {
                grabber.restart();
                return true;
            } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
                try {
                    //System.err.println("重启抓取器失败，正在关闭抓取器...");
                    text1.append("\r\n重启抓取器失败，正在关闭抓取器...error:"+e2.getLocalizedMessage());
                   // Toast.makeText(MainActivity.this, "重启抓取器失败，正在关闭抓取器...error:"+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    grabber.stop();
                } catch (org.bytedeco.javacv.FrameGrabber.Exception e1) {
                    //System.err.println("停止抓取器失败！");
                    Toast.makeText(MainActivity.this, "停止抓取器失败！", Toast.LENGTH_LONG).show();
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
        } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
            return false;
        } finally {
            try {
                grabber.stop();
            } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
                //System.err.println("关闭抓取器失败");
                Toast.makeText(MainActivity.this, "关闭抓取器失败", Toast.LENGTH_LONG).show();
            }
        }
    }
}