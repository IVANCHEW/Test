package com.example.ivan.myapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v4.graphics.ColorUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;

public class MainActivity extends Activity {

    Bitmap bitmapSample, bitmapSample2;
    LayoutInflater controlInflater = null;
    TextView text1,text2;
    FrameLayout
            frame1, frame2, frame3;
    Button button,buttonSample;
    Boolean targetOn;
    int x, y;

    int ImageWidth, ImageHeight;

    int[] colorValues=new int[3];
    DrawTarget DT;
    TrackTarget TT;
    DrawSample DS;
    ImageView imageView1;
    Camera mainCamera;
    Preview mPreview;
    String receiveMessage;
    String[] instruction = new String[11];
    Boolean trackingStatus = false;
    double[] labColour = new double[3];
    int x1, x2, y1, y2, n;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Overlay of control XML
        controlInflater = LayoutInflater.from(getBaseContext());
        View viewControl = controlInflater.inflate(R.layout.control,null);
        LinearLayout.LayoutParams layoutParamsControl = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
        this.addContentView(viewControl,layoutParamsControl);

        //Text1 located on activity_main
        text1 = (TextView) findViewById(R.id.textView);
        text2 = (TextView) findViewById(R.id.textView2);
        button = (Button) findViewById(R.id.buttonControl);
        buttonSample = (Button) findViewById(R.id.buttonSample);
        frame1 = (FrameLayout) findViewById(R.id.Frame1);
        frame2 = (FrameLayout) findViewById(R.id.Frame2);
        frame3 = (FrameLayout) findViewById(R.id.Frame3);
        text2.setTextColor(Color.RED);

        //For subsequent image sample
        imageView1 = new ImageView(this);
        frame3.addView(imageView1);

        //DRAW TARGET ON THE CENTER OF THE FRAME
        DT = new DrawTarget(this);
        targetOn = false;

        //GET SAMPLE OF TARGET PIXEL'S COLOR
        DS = new DrawSample(this);

        //DRAW TARGET ON TRACKED COLOR
        TT = new TrackTarget(this);

        //SET STRING ARRAY
        n=0;
        instruction[0] = "Hello";
        instruction[1]="0";
        instruction[2]="1";
        instruction[3]="2";
        instruction[4]="3";
        instruction[5]="4";
        instruction[6]="5";
        instruction[7]="6";
        instruction[8]="7";
        instruction[9]="8";
        instruction[10]="9";

        //INITIATE THE CAMERA
        try{

            mainCamera=Camera.open();
        }catch(Exception e){
            Log.d("NEUTRAL","Error opening camera");

        }

        //START THE PREVIEW
        mPreview = new Preview(this, mainCamera);
        try{

            //Initiate the preview
            FrameLayout frame = (FrameLayout)findViewById(R.id.camera_preview);
            frame.addView(mPreview);

            Log.d("NEUTRAL", "Camera Preview Class Added");

        }catch(RuntimeException e){

            Log.d("NEUTRAL","Error in OnCreate");
            System.err.println(e);
            return;

        }

        //DEFINITION OF HANDLERS AND LISTENERS

        //Define the handler to listen for messages from the Preview Class
        mPreview.callHandler(new Handler(){

            public void handleMessage(Message msg){

                //Message contains the Hex RGB color

                receiveMessage =(String) msg.obj;
                text1.setText(receiveMessage);

                /*
                Bundle bundle = msg.getData();
                receiveMessage = bundle.getString("TargetPosition");
                text1.setText(receiveMessage);
                */

                //Receives target position - draw target on position
                int targetPosition = Integer.parseInt(receiveMessage);
                int targetX = ImageHeight - (int) Math.ceil(targetPosition/ImageWidth);
                int targetY= (targetPosition % ImageWidth);

                //Converts position to percentage
                double targetX2 = (double)targetX/(double)ImageHeight;
                double targetY2 = (double)targetY/(double)ImageWidth;

                //Log.d("NEUTRAL2", "Coord x: " + targetX2 + " Coord y: " + targetY2 + " Position: " + targetPosition);

                if (trackingStatus==false){

                    frame2.removeAllViews();
                    TT.GetPosition(targetX2,targetY2);
                    frame2.addView(TT);
                    trackingStatus=true;
                    targetOn = false;
                    x1= targetX;
                    x1 = targetY;

                }else{

                    x2 = targetX;
                    y2 = targetY;

                    double Delta = Math.sqrt(Math.pow((double)y1,2)-Math.pow((double)y2,2));

                    if (Delta>50){

                        text2.setText(instruction[n]);
                        n=n+1;
                    }

                    x1 = x2;
                    y1 = y2;

                    TT.GetPosition(targetX2,targetY2);
                    TT.invalidate();

                }

            }

        });

        //SHOW CENTER
        button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){

                if (targetOn==Boolean.FALSE){

                    text1.setText("Target Trigger");

                    x = frame2.getHeight();
                    y = frame2.getWidth();

                    DT.GetSize(y,x);
                    frame2.addView(DT);
                    targetOn = true;
                    trackingStatus = false;

                }
                else{

                    text1.setText("Target Off");
                    text2.setText("");
                    frame2.removeAllViews();
                    targetOn=false;

                }

            }
        });

        //GET SAMPLE
        buttonSample.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                text1.setText("Take Picture");
                mainCamera.takePicture(null,null, mPicture);

            }

        });

    }

    //HANDLER WHEN PICTURE IS TAKEN
    Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            //Convet Byte Array to Bitmap
            bitmapSample = BitmapFactory.decodeByteArray(data,0,data.length);
            ByteArrayOutputStream blob = new ByteArrayOutputStream();
            bitmapSample.compress(Bitmap.CompressFormat.PNG,0,blob);
            ImageWidth = camera.getParameters().getPreviewSize().width;
            ImageHeight = camera.getParameters().getPreviewSize().height;
            Log.d("NEUTRAL3", "Frame Parameters: Width=" + frame2.getWidth() + " Height=" + frame2.getHeight());
            Log.d("NEUTRAL3", "Preview size: Width=" + ImageWidth + " Height=" + ImageHeight);
            TT.GetFrame(frame2.getHeight(),frame2.getWidth());

            //Call pixelColor to store value in colorValues[] and then converts to a hex decimal int value
            getPixelColor();
            int hexColor = 0xff000000 | ((colorValues[0] << 16) & 0xff0000) | ((colorValues[1] << 8) & 0xff00) | ((colorValues[2]) & 0xff);
            ColorUtils.RGBToLAB(colorValues[0],colorValues[1],colorValues[2],labColour);

            String showValue = colorValues[0] + ","+colorValues[1]+","+colorValues[2]+" Lab Colours:" + labColour[0]+","+labColour[1]+","+labColour[2];
            text2.setText(showValue);

            //Send Color data to tracker in Preview Class
            mPreview.sendColor(hexColor, labColour);

            //Set Sample Color

            frame1.removeAllViews();
            DS.GetColor(colorValues[0],colorValues[1],colorValues[2]);
            frame1.addView(DS);


            //Rotate the Bitmap
            /*Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmapSample2 = Bitmap.createBitmap(bitmapSample,0,0,bitmapSample.getWidth(),bitmapSample.getHeight(),matrix,true);

            //Display the Bitmap
            imageView1.setImageBitmap(bitmapSample2);*/

            //Resume Preview
            mainCamera.startPreview();

        }

    };

    //Get pixel color values
    private void getPixelColor(){

        int x,y, pixel;
        x = bitmapSample.getWidth()/2;
        y = bitmapSample.getHeight()/2;

        pixel=bitmapSample.getPixel(x,y);
        colorValues[0]=Color.red(pixel);
        colorValues[1]=Color.green(pixel);
        colorValues[2]=Color.blue(pixel);

    }

}

//Preview class used to start the camera preview
class Preview extends SurfaceView implements SurfaceHolder .Callback, Camera.PreviewCallback{

    SurfaceHolder mHolder;
    Camera mCamera;
    Camera.Size previewSize;
    Camera.Parameters param;
    Boolean TrackTarget =false;
    int[] colorSample = new int[3];
    int[] pixels;
    int count= 0;
    int targetPosition=0;
    int hexColor;
    double[] labColor2= new double[3];
    double[] labColor1= new double[3];

    //Prepare handler to send message
    private Handler previewHandler=null;

    public void callHandler(Handler handler){

        this.previewHandler = handler;

    }

    public Preview(Context context, Camera camera) {
        super(context);

        mCamera = camera;

        mHolder=getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try{
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(this);
        }catch(Exception e) {
            Log.d("NEUTRAL", "Error setting holder");
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        //Sets Smallest Preview Size
        param = mCamera.getParameters();
        previewSize = getSmallestPreviewSize(width,height);
        param.setPreviewSize(previewSize.width,previewSize.height);
        //Constant for NV21 format is 17
        param.setPreviewFormat(17);

        /*//Check Exposure Compensation
        Log.d("NEUTRAL3", "Min Exposure: " + param.getMinExposureCompensation());
        Log.d("NEUTRAL3", "Max Exposure: " + param.getMaxExposureCompensation());
        */

        //param.setExposureCompensation(0);

        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(param);
        pixels = new int[previewSize.width* previewSize.height];
        Log.d("NEUTRAL3","Preview Size: Width=" + previewSize.width + " Height=" + previewSize.height);

        try{
            mCamera.startPreview();
        }catch(Exception e){
            Log.d("NEUTRAL","Error starting preview");
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }catch(Exception e){
            Log.d("NEUTRAL","Error stoppipng camera on surface destroy");
        }
    }

    private Camera.Size getBestPreviewSize (int width, int height){

        Camera.Size result = null;
        Camera.Parameters param = mCamera.getParameters();
        for (Camera.Size size : param.getSupportedPreviewSizes()){
            if (result==null){result=size;}
            else{
                int resultArea = result.width*result.height;
                int newArea = size.width*size.height;

                if (newArea>resultArea){
                    result = size;
                }
            }
        }

        return result;
    }

    private Camera.Size getSmallestPreviewSize (int width, int height){

        Camera.Size result = null;
        Camera.Parameters param = mCamera.getParameters();
        for (Camera.Size size : param.getSupportedPreviewSizes()){
            if (result==null){result=size;}
            else{
                int resultArea = result.width*result.height;
                int newArea = size.width*size.height;

                if (newArea<resultArea){
                    result = size;
                }
            }
        }

        return result;
    }

    public void sendColor(int data, double[] getLAB){

        hexColor = data;
        labColor1[0] = getLAB[0];
        labColor1[1] = getLAB[1];
        labColor1[2] = getLAB[2];

        TrackTarget = true;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        //Log.d("NEUTRAL2", "Received Frame");

        if (TrackTarget==true) {

            //Pixel Analysis
            findTarget(pixels, data,previewSize.width, previewSize.height);

            //Log.d("NEUTRAL2", "Track Target Value" + targetPosition);

            if (targetPosition!=0){
                previewHandler.removeCallbacksAndMessages(null);
                Message msg = Message.obtain();


                String message = "" + targetPosition;

                //Original Code used ot send message to UI thread

                msg.obj = message;
                msg.setTarget(previewHandler);
                msg.sendToTarget();


                /*
                Bundle bundle = new Bundle();
                bundle.putIntArray("FramePicture",pixels);
                bundle.putString("TargetPosition",message);
                msg.setData(bundle);
                previewHandler.sendMessage(msg);
                */
            }

        }

    }

    void findTarget(int[] pixels, byte[] yuv420sp, int width, int height) {

        final int frameSize = width * height;
        int rgb,r2, b2, g2;
        double DE;
        targetPosition=0;

        //Log.d("NEUTRAL2", "Processing decode");
        outerLoop:
        for (int j = 0, yp = 0; j < height; j++) {       int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)                  r = 0;               else if (r > 262143)
                    r = 262143;
                if (g < 0)                  g = 0;               else if (g > 262143)
                    g = 262143;
                if (b < 0)                  b = 0;               else if (b > 262143)
                    b = 262143;

                rgb = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);

                //Conversion from HEX to RBG
                r2 = (rgb >> 16)& 0xff;
                g2 = (rgb >> 8)& 0xff;
                b2 = (rgb) & 0xff;

                ColorUtils.RGBToLAB(r2,g2,b2,labColor2);

                //String showValues = "Sample: " + labColor1[0] + "," + labColor1[1] + "," + labColor1[2] + " Pixel: " + labColor2[0] + "," + labColor2[1] + "," + labColor2[2];
                //Log.d("NEUTRAL2", showValues);

                DE = Math.sqrt(Math.pow(labColor2[0]-labColor1[0],2) + Math.pow(labColor2[1]-labColor1[1],2) + Math.pow(labColor2[2]-labColor1[2],2));

                if (DE<8){

                    targetPosition = yp+1;
                    //pixels[yp] = 0xffffffff;
                    //Log.d("NEUTRAL2","Matched Color" + hexColor + "," + " Delta: " + DE + " Position: " + yp);
                    break outerLoop;

                }else{

                    //pixels[yp]=0xff000000;

                }
            }

        }
    }



}

//DRAW CENTER TARGET
class DrawTarget extends View{

    private Rect rect;
    private Paint paint;
    private Boolean sized;
    private int x,y, length;

    public DrawTarget(Context context){
        super(context);

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas c){
        c.drawRect(rect,paint);
    }

    public void GetSize(int frameWidth, int frameHeight){
        length = 20;
        x = (frameHeight- length)/2;
        y = (frameWidth - length)/2;
        rect = new Rect(y,x,y+length,x+length);
    }

}

//DRAW TARGET ON TRACKED PIXEL
class TrackTarget extends View{

    private Rect rect;
    private Paint paint;
    private int x,y;
    private int length=20;
    //Width and height AFTER rotation
    private int width, height;

    public TrackTarget(Context context){
        super(context);

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas c){

        rect = new Rect(x,y,x+length,y+length);
        c.drawRect(rect,paint);
        //Log.d("NEUTRAL2", "Position Updated");
    }

    public void GetPosition(double xc, double yc){

        double xt = ((double)xc*(double)width-length/2);
        double yt = (int) ((double)yc*(double)height-length/2);
        //Log.d("NEUTRAL2", "xt=" + xt + " yt=" + yt);

        x = (int) xt;
        y = (int) yt;
    }

    public void GetFrame(int h, int w){
        width = w;
        height = h;
    }

}

//DRAW SAMPLE OF COLOR
class DrawSample extends View{

    private int[] rgb=new int[3];
    private Paint paint;
    private Color color;
    private Boolean validColor;
    public DrawSample(Context context) {

        super(context);
        paint = new Paint();
        validColor = false;

    }

    @Override
    protected void  onDraw(Canvas c){

        if (validColor==true){

            c.drawColor(Color.argb(100,rgb[0],rgb[1],rgb[2]));

        }

    }

    public void GetColor(int r, int g, int b){

        rgb[0]=r;
        rgb[1]=g;
        rgb[2]=b;

        validColor = true;

    }

}