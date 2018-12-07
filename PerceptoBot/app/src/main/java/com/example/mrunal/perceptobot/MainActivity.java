package com.example.mrunal.perceptobot;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //declaring member variable
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private Switch mReverse, mRobotOn, mFaceDetection;
    private WebView mWebview;
    private boolean mOn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //creating a firebase database reference
        database =  FirebaseDatabase.getInstance();
        myRef = database.getReference();


        mWebview = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = mWebview.getSettings();   //sets default setting for a webview
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);           //sets zoom in controls
        /**
         * Method sets the WebViewClient that will receive requests
         * @param WebViewClient request sent to the method
         * */
        mWebview.setWebViewClient(new WebViewClient() {
            /**
             * Method control when a URL is about to be loaded in the WebView
             * @param view webView for loading url
             * @param request url to be loaded in WebView
             * @return false causes the WebView to continue loading the URL as usual */
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE); //setting up the sensor service
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);   //accessing type gravity sensor

        mRobotOn = (Switch) findViewById(R.id.switch2);
        /**
         * Method setOnCheckedChangeListener registers a callback to be invoked
         * when the checked state of robotOn switch changes*/
        mRobotOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myRef.child("robotOn").setValue(isChecked); //sends value of robotOn to firebase
                mOn = isChecked;                            //sets a flag when robotOn checked
                if (isChecked){
                    mFaceDetection.setChecked(false);       //sets faceDetection to false when robotOn ischecked
                }
            }
        });

        mReverse = (Switch) findViewById(R.id.switch1);
        /**
         * Method setOnCheckedChangeListener registers a callback to be invoked
         * when the checked state of Reverse switch changes*/
        mReverse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myRef.child("direction").setValue(isChecked); //sends value of direction to firebase
            }
        });

        mFaceDetection = (Switch) findViewById(R.id.switch3);
        mFaceDetection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myRef.child("faceDetection").setValue(isChecked);  //sends value of faceDetection to firebase
                if (isChecked){
                    mRobotOn.setChecked(false);                    //sets RobotOn to false when faceDetection ischecked
                }
            }
        });


        /**
         * Method addListenerForSingleValueEvent() executes onDataChange method
         * immediately and after executing that method once at start */
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            /**
             * Method will be called with a snapshot of the data at this location.*/
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mWebview.loadUrl("http://" + dataSnapshot.child("IP").getValue(String.class) + ":8082");
                mReverse.setChecked((Boolean) dataSnapshot.child("direction").getValue());
                mFaceDetection.setChecked((Boolean) dataSnapshot.child("faceDetection").getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    /**
     * Methods sets robotOn to false when user  leaves the activity */
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        myRef.child("robotOn").setValue(false);  //sets robotOn to false on db
    }

    @Override
    /**
     * Method called when sensor values have changed
     * @param event SensorEvent object passed
     * */
    public void onSensorChanged(SensorEvent event) {
        float x, y;
        x = event.values[0];

        if ((x > 0 && x < 9) && (mOn)) {//checks for x value in a specific range and robotOn value.
            x = 1-(x/9);                //sends x value after converting (will be set as robot speed) in forward and reverse direction
        } else {
            x = 0;                      //else set speed to 0
        }

        y = event.values[1];
        if (((y < -1 && y > -6 ) || (y > 1 && y < 6 )) && (mOn)) { //checks for y value in a specific range and robotOn value.
            y = y;                      //sets y value without converting (speed to remain same in lateral movement)
            x = 0;                      //sets linear motion to 0 during lateral motion of the bot
        } else {
            y = 0;                      //else set lateral value to 0
        }

        myRef.child("linearAcc").setValue(x);       //sends linear movement speed to firebase when robot is On
        myRef.child("lateralAcc").setValue(y);      //sends lateral movement to firebase when robot is On
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }

}
