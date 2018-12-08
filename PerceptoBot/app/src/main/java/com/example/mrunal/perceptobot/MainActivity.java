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

        database =  FirebaseDatabase.getInstance();
        myRef = database.getReference();

        mWebview = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = mWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);

        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        mReverse = (Switch) findViewById(R.id.switch1);
        mRobotOn = (Switch) findViewById(R.id.switch2);
        mFaceDetection = (Switch) findViewById(R.id.switch3);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        mRobotOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myRef.child("robotOn").setValue(isChecked);
                mOn = isChecked;
                if (isChecked){
                    mFaceDetection.setChecked(false);
                }
            }
        });

        mReverse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myRef.child("direction").setValue(isChecked);
            }
        });

        mFaceDetection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myRef.child("faceDetection").setValue(isChecked);
                if (isChecked){
                    //myRef.child("linearAcc").setValue(0);
                    //myRef.child("lateralAcc").setValue(0);
                    mRobotOn.setChecked(false);
                }
            }
        });

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
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

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        myRef.child("robotOn").setValue(false);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x, y;
        x = event.values[0];

        if ((x > 0 && x < 9) && (mOn)) {
            x = 1-(x/9);
        } else {
            x = 0;
        }

        y = event.values[1];
        if (((y < -1 && y > -6 ) || (y > 1 && y < 6 )) && (mOn)) {
            y = y;
            x = 0;
        } else {
            y = 0;
        }

        myRef.child("linearAcc").setValue(x);
        myRef.child("lateralAcc").setValue(y);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }

}
