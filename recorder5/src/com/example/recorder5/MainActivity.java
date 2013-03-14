//TODO 
//prevent false driving when u stop walking by checking if walked in last 5  - 10 sec or whatever
//check walking north vs. walking west
//true walking detection eg by fourier
//walking vs. playing with phone
//watch out for roadsw w increasing height )if using Vz as threshold)
//try using dOy/dt -  exclude ] driving if too high

//DONE 
//use orientation changes to check if driving or not i.e. change in orientation asround horizontal axis should be low (no rotation up / down)

//How to use NDK (to get java to run C code)
// - Your application's source code will declare one or more methods
//    with the 'native' keyword to indicate that they are implemented through
//    native code. E.g.:
//
//      native byte[]  loadFile(String  filePath);
//
//  - You must provide a native shared library that contains the
//    implementation of these methods, which will be packaged into your
//    application's .apk. This library must be named according to standard
//    Unix conventions as lib<something>.so, and shall contain a standard JNI
//    entry point (more on this later). For example:
//
//      libFileLoader.so
//
//  - Your application must explicitly load the library. For example, to load
//    it at application start-up, simply add the following to its source code:
//
//      static {
//        System.loadLibrary("FileLoader");
//      }
//
//    Note that you should not use the 'lib' prefix and '.so' suffix here.
//
// this is a change to test git

package com.example.recorder5;

//import android.opengl.Matrix;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

//import android.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.recorder5.R;


public class MainActivity extends Activity implements SensorEventListener {

	private static final boolean debug = false;
	private static final boolean android=true;
	private static  boolean bool1=true;
	private static boolean mGot_Orientation=false;
	private static boolean mGot_Acceleration=false  ;
	private static boolean mGot_Gyro=false;
	private static boolean mGot_Magnetometer=false;
	private static boolean mFileIsOpen=false;
	private static boolean mWalking=false;
	private static boolean mDriving=false;
	private static long timeStamp;
	private static float[] Rotation_matrix ;
	private static float[] Inclination_matrix ;
	private static float[] mOrientationVector;
	private static float[] mAccelerationVector;	
	private static float[] mLinearAccelerationVector;
	private static float[] mAccelerationVectorWC;
	private static float[] mLinearAccelerationVectorWC;
	private static float[] mOrientationVectorWC;
	private static float[]V={(float)0.0,(float)0.0,(float)0.0};
	private static float[]V2={(float)0.0,(float)0.0,(float)0.0};
	private static float[]mVz;
	private static float mVz1,mVz2;
	private static float[]mVz2s;
	private static float[]mOy_2s,mOy_20s;
//	private static float mOy1,mOy2;
	private static float mVzAvg;
	private static float Vhmax=(float)0.0;
	private static float Vh2max=(float)0.0;
//	private float[] msd={(float)0.0,(float)0.0,(float)0.0,(float)0.0,(float)0.0};
//	private float[] linear_acceleration_offsets={(float)0.0,(float)0.0,(float)0.0};

	private static float mLastX, mLastY, mLastZ, mLastA, mLastP, mLastR,mLastGx,mLastGy,mLastGz;
	private static float mLastHx,mLastHy,mLastHz;
	private float mLastAnorm,mLastOnorm,mLastGnorm;
//	private static float mAvgGabs;
	private static int mVzCount=0,mSampleCount2s=0,mSampleCount20s=0;
//	private static float mAa_avg=0,mAa_avgsq=0,mAa_std=0;
//	private static float mGa_avg=0,mGa_avgsq=0,mGa_std=0;
//	private float mOx_avg1=0,mOx_avgsq1=0,mOx_std1=0;
//	private float mOx_avg2=0,mOx_avgsq2=0,mOx_std2=0;
//	private static float mdOx;
//	private static float mdt;
	private static float mT0;
//	private static float mOy_avg=0,mOy_std=0;
	private static float mOy_avg20s=0,mOy_std20s=0;
	private static float mOy_avg2s=0,mOy_std2s=0;

	private float mVhCurrent, mVh2Current;

	private static float ppscore,ppscore1,ppscore2,ppscore3,ppscore4,ppscore5,ppscore6,ppscore7,ppscore8=(float)0.0;
//	private static float mdOx_start_vs_end;
//	private float ppscore_running_avg=0;
//	private static float highest_ppscore=(float)0.0;
	private long mLastTimeStamp=0;
	private long mTimeofLastWalkingDetection=0;//in ms
	private static String mFilename="";
	private FileOutputStream mFOS=null;
	private String mMode="";
	private String abbrev;
//	private int mi=0;
	private static int i;
	private float mVzStd;
	//	Magic numbers - expected values of various parameters during driving/parking/walking
	private float mVzStdMinWalkingThreshold=(float).08; //expectd min std of Vz walking
	private float mVzStdMaxDrivingThreshold=(float).2; //expectd max std of Vz driving
	private float mVzAvgMaxDrivingThreshold=(float)0.7;//expectd max  of avg Vz (absolute) driving
	private float mtime_after_walking_to_filter_driving=5;//in sec
	private static float mOyStd2sMaxDrivingThreshold=5;  //Std(Oy)  maximum for driving
	private static float mOyStd20sMaxDrivingThreshold=5;  //Std(Oy)  maximum for driving
	private static float mOyStd2sMinWalkingThreshold=10;  //Std(Oy)  minimum for driving
	private static float mOyStd20sMinWalkingThreshold=10;  //Std(Oy)  minimum for driving
	private static float mdriving_velocity_threshold=(float).5;  //horizontal velocity minimum for driving
	
	
//	private float mAa_std_desiredavg=(float)0.45;   	//expected value of stdev(|mA|)
//	private static float mAa_std_desiredstd=(float)0.6;	//expected stdev of stdev(|mA|)
//	private static float mGa_std_desired=(float)0.3;   	//expected value of stdev(|G|)
//	private static float mGa_std_std=1;	//expected stdev of stdev(|G|)
//	private static float mmsd_desired=0;
//	private static float mmsd_std=5;
//	private static float mmsd1_desired=0;
//	private static float mmsd1_std=5;
//	private static float mmsd2_desired=0;
//	private static float mmsd2_std=5;
//	private static float mmsd3_desired=0;
//	private static float mmsd3_std=5;

	//changed from 2 to 1e-6
//	private static float mAvgGabsAlpha=(float)0.9;
//	private static float mGyro_Walking_Threshold=(float)1.5;
	private float beta=(float)0.95;//factor by which velocity is decreased, to prevent drift
//	private static float m2C_desired=(float).000001;
//	private static float m2C_std=(float).000002;
//	private static float m2C_Ea=(float)0.00000002;
//	private static float mdOx_desired=31;
//	private static float mdOx_std=20;
//	private static float mdt_desired=(float)10.000;  //Seconds
//	private static float mdt_std=(float)20.000; //Seconds
//	private float mmin_parking_time=6; //seconds
//	private static float mmin_dt_Ea=6; //seconds
//	private static float mMin_zerocross_time=5000;//mseconds
//	private float mOx_std1_desiredavg=(float)6.0;		//expected value of stdev(mOx) over timescale1
//	private float mOx_std1_desiredstd=(float)3.0;		//expected stdev of stdev(mOx) over timescale1
//	private float mOx_std2_desiredavg=(float)25.0;	//expected value of stdev(mOx) over timescale2
//	private float mOx_std2_desiredstd=(float)10.0;	//expected stdev of stdev(mOx) over timescale2
	private float alpha1=(float)0.5,alpha2=(float)0.2,alpha3=(float)0.1;
	private int mMinimumLogInterval=100;  //in ms
	private static float mPeriod_of_analysis=20,mPeriod_of_driving_analysis=20;  //in seconds
	private static float mTime_between_analyses=(float).5;  //in seconds	
	private static int mSampling_rate=10; //in seconds
//	private static float mdOx_start_vs_end_desired=0;
//	private static float mdOx_start_vs_end_std=15;
//	private static float m1B_desired=0;
//	private static float m3B_desired=0;
//	private static float m1B_std=(float)0.001;
//	private static float m3B_std=(float)0.001;
//	private static float alpha=(float)0.7;
//	private static int mN_linear_tries=3;
//	private static int mN_fit1start,mN_fit1end,mN_fit2start,mN_fit2end,mN_fit3start,mN_fit3end;
// static int mN_values=13; 
	private static int mFields_in_sensorvector=16; //number of values in data record:
	//mtime, 3 accel, 3 gyro, 3 orientation,3world coordinate accels (and 1 char for type), 3wc acc. wout offsets

	private static int mN_samples_taken=0;
	private static boolean mEnough_driving_samples=false;

	private static int mN_samples_in_driving_analysis;
	private static int mN_samples_in_analysis;
	private static int mN_samples_in_2s_analysis=20;
	private static int mN_samples_in_20s_analysis=200;
	private static int	mN_samples_between_analyses; //in seconds
//	private static double[] y;
//	private static double[] x;
	private static float[] values;
	private static float[] mtime,mOx,mlnOx,mAa,mGa;
	private static float[] mOx_fit2,mOx_fit2_time,mOx_fit1,mOx_fit1_time,mOx_fit3,mOx_fit3_time;
//	private static float[] fmOx_fit2,fmOx_fit2_time,fmOx_fit1,fmOx_fit1_time,fmOx_fit3,fmOx_fit3_time;
	private static float mOx_high,mOx_high_time,mOx_low,mOx_low_time;
	private static float[][] allvals;
	private static float m1A=0,m2A=0,m3A=0;
	private static float m1B=0,m2B=0,m3B=0;
	private static float m1C=0,m2C=0,m3C=0;
	private static float[] coeffs;
	private static String coeff_str="undefined";

	private static boolean zerocross=false;

	private static int mN_between_analyses_count=0;
	private static boolean mFlag_did_analysis=false;
	private static float[] mOx_copy;
	private static int mN=0;

	private static float mmsd1=0,mmsd2=0,mmsd3=0;
	private static datastructure d;
	private static datastructure[] mSensor_history;

	//
	//	private static PolynomialFitter myPolynome1;
	//	private static PolynomialFitter myPolynome2;
	//	private static PolynomialFitter myPolynome3;
	//	private static PolynomialFitter.Polynomial myPoly1;
	//	private static PolynomialFitter.Polynomial myPoly2;
	//	private static PolynomialFitter.Polynomial myPoly3;

	private static float[] mReduced_time;
	private static float mmsd_avg=1000;

	//	   //android specific
	private static TextView tvOx,tvOy,tvOz,tvOzz;
	private SensorManager mSensorManager=null;
	private Sensor mAccelerometer=null;
	private Sensor mLinearAccelerometer=null;
	private Sensor mOrientation=null;
	private Sensor mGyro=null;
	private Sensor mMagneticField =null;

	private boolean mLoggerStatus=false;
	private boolean mListenersRegistered=false;
	private boolean mFirstRun=true;
	private WakeLock mWakeLock=null; 

	static BufferedReader stdin = new BufferedReader
			(new InputStreamReader(System.in));


	//    private static Canvas mCanvas;
	//    private static Paint mPaint;


	//		mAa=new float[mN_analysis_samples+mN_samples_in_analysis_between_analyses];


//	public static void Main(String[] args) 
//	{
//		//		mAa=new float[mN_analysis_samples+mN_samples_between_analyses+1];
//		//	mOx=new float[mN_analysis_samples+mN_samples_between_analyses+1];
//		//mGa=new float[mN_analysis_samples+mN_samples_between_analyses+1];
//		//	mCanvas=new Canvas;	
//		//       Paint myPaint = new Paint();
//
//// warning it appears main never gets called 
//		String str="";	
//		coeffs=new float[4];
//		values=new float[10];
//		allvals=new float[3][1000];
//
//		mN_samples_in_analysis=(int)(mPeriod_of_analysis*mSampling_rate);
//		mN_samples_in_driving_analysis=(int)(mPeriod_of_driving_analysis*mSampling_rate);
//		mN_samples_between_analyses=(int)(mTime_between_analyses*mSampling_rate);
//		System.out.println("Nsamples"+mN_samples_in_analysis+" Nbet.analyses "+mN_samples_between_analyses);
//		mtime=new float[mN_samples_in_analysis];
//		mOx=new float[mN_samples_in_analysis];
//		mlnOx=new float[mN_samples_in_analysis];
//		mOx_fit2=new float[mN_samples_in_analysis];
//		mOx_fit2_time=new float[mN_samples_in_analysis];
//		mOx_fit1=new float[mN_samples_in_analysis];
//		mOx_fit1_time=new float[mN_samples_in_analysis];
//		mOx_fit3=new float[mN_samples_in_analysis];
//		mOx_fit1_time=new float[mN_samples_in_analysis];
//		mAa=new float[mN_samples_in_analysis];
//		mN_between_analyses_count=0;
//		mFlag_did_analysis=false;
//		mOx_copy=new float[mN_samples_in_analysis];
//		mN=0;
//		//	d=new datastructure(mN_values);
//		mReduced_time=new float[mN_samples_in_analysis];
//		mOrientationVector=new float[4];
//		mOrientationVectorWC=new float[4];
//		mAccelerationVector=new float[4];
//		mAccelerationVectorWC=new float[4];
//		mLinearAccelerationVector=new float[4];
//		mLinearAccelerationVectorWC=new float[4];
//		Rotation_matrix = new float[16]; //mLastX
//		Inclination_matrix = new float[16]; //mLastX
//
//		mLastX = 0;
//		mLastY = 0;
//		mLastZ = 0;
//		mLastA = 0;
//		mLastP = 0;
//		mLastR = 0;
//		mLastGx =0;
//		mLastGy =0;
//		mLastGz = 0;
//	}


    static {
  //    System.loadLibrary("hello-jni");
      System.loadLibrary("jeremy");
  }
    
	
	public static FileOutputStream initializeLogFile(String FileName, boolean ClearFileIfExists){
		FileOutputStream fos=null;
		try {
			fos = new FileOutputStream(FileName, !ClearFileIfExists);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return fos;
	}
	public static void CloseLogFile(FileOutputStream fos){
		try {
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// need acceleration (with gravity) in order to get rotation matrix
	//need linear acceleration (without gravity) to get velocities. in principle velocities can come from acc. w. gravity but there is an implicit assumption that the phone is at rest. 
	//therefore when calculating acceleration in real world coordinates, ax=ay=0, az=9.8
	
	//	@SuppressLint({ "NewApi", "NewApi" })
	private void registerListeners(){
		if (!mListenersRegistered){
			if (mSensorManager==null){
				mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
				mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				mLinearAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
				mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
				mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//				mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
			}
			mListenersRegistered=true;
			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			mSensorManager.registerListener(this, mLinearAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
//			mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);			
			mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);			
		}
	}
	private void unregisterListeners(){
		if (mListenersRegistered){
			mListenersRegistered=false;
			mSensorManager.unregisterListener(this);
		}
	}
	private void takeWakeLock(){
		if (mWakeLock==null){
			mWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "6ax wakelock");
		}
		if (!mWakeLock.isHeld()){
			mWakeLock.acquire();
		}
	}
	private void ReleaseWakeLock(){
		if (mWakeLock!=null && mWakeLock.isHeld()){
			mWakeLock.release();
		}
		mWakeLock=null;
	}



	private void initializeUI(){
		//		final Spinner modeSpinner = ((Spinner)findViewById(R.id.addToDictionary)); 
		//		setContentView(R.id);
		final Spinner modeSpinner = ((Spinner)findViewById(R.id.modeSelector)); 
		modeSpinner.setSelection(0);
		mMode = modeSpinner.getSelectedItem().toString();
		modeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			//			@Override
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				mMode = modeSpinner.getSelectedItem().toString();
			}

			//		@Override
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// Do nothing
			}

		});

		//        final ToggleButton logStatus = ((ToggleButton)findViewById(R.id.background));
		final ToggleButton logStatus = ((ToggleButton)findViewById(R.id.logStatus));
		logStatus.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mLoggerStatus=isChecked;
				if (isChecked){
					registerListeners();
				}else{
					unregisterListeners();
				}
			}
		});

		//        final Button clearLog = ((Button)findViewById(R.id.button1));
		final Button clearLog = ((Button)findViewById(R.id.clearLog));
		clearLog.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mFOS!=null){
					CloseLogFile(mFOS);
					mFOS=null;
					mFOS=initializeLogFile(mFilename, true);
				}
			}
		});
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		registerListeners();
		//		      takeWakeLock();

		initializeUI();

		mFilename=Environment.getExternalStorageDirectory().toString()+"/ParkoLogggger.log";
		//mFilename="/mnt/extSdCard/sixaxlogger.log";
		mFilename="/mnt/sdcard/sixaxlogger.log";
		mFOS=initializeLogFile(mFilename, false);
		mFileIsOpen=true;
	}

	//	@Override
	//	protected void onCreate(Bundle savedInstanceState) {
	//		super.onCreate(savedInstanceState);
	//		setContentView(R.layout.activity_main);
	//	}
	//
	//	

	@Override
	protected void onResume() {
		super.onResume();
		registerListeners();        
		//	        takeWakeLock();
		mFOS = initializeLogFile(mFilename, false);
		mFileIsOpen=true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		//	       ReleaseWakeLock();
		unregisterListeners();
		CloseLogFile(mFOS);
		mFileIsOpen=false;
		mFOS=null;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//	        ReleaseWakeLock();
		unregisterListeners();
		if(mFileIsOpen)  CloseLogFile(mFOS);
		mFOS=null;
	}

	//	
	//@Override
	@SuppressLint("NewApi")
	@Override
	public void onSensorChanged(SensorEvent event) {


		 timeStamp=System.currentTimeMillis();
		if (mFirstRun==true)
		{
			
		
//	      System.loadLibrary("hello-jni");
	      System.loadLibrary("jeremy");
		 

			mFirstRun=false;
			coeffs=new float[4];
			values=new float[10];
			allvals=new float[3][1000];

			mN_samples_in_analysis=(int)(mPeriod_of_analysis*mSampling_rate);
			mN_samples_taken=0;
			mN_samples_in_driving_analysis=(int)(mPeriod_of_driving_analysis*mSampling_rate);
			mN_samples_between_analyses=(int)(mTime_between_analyses*mSampling_rate);
			mN_samples_in_2s_analysis=2*mSampling_rate;
			mN_samples_in_20s_analysis=20*mSampling_rate;
			System.out.println("Nsamples"+mN_samples_in_analysis+" Nbet.analyses "+mN_samples_between_analyses);
			mtime=new float[mN_samples_in_analysis];
			mOx=new float[mN_samples_in_analysis];
			mAa=new float[mN_samples_in_analysis];
			mGa=new float[mN_samples_in_analysis];
			mlnOx=new float[mN_samples_in_analysis];
			mOx_fit2=new float[mN_samples_in_analysis];
			mOx_fit2_time=new float[mN_samples_in_analysis];
			mOx_fit1=new float[mN_samples_in_analysis];
			mOx_fit1_time=new float[mN_samples_in_analysis];
			mOx_fit3=new float[mN_samples_in_analysis];
			mOx_fit1_time=new float[mN_samples_in_analysis];
			mVz2s=new float[mN_samples_in_2s_analysis];   //circular buffer
			mVz=new float[mN_samples_in_2s_analysis];   //circular buffer
//			mVz2s=new float[mN_samples_in_2s_analysis+1];   //circular buffer
			mOy_2s=new float[mN_samples_in_2s_analysis];   //circular buffer
			mOy_20s=new float[mN_samples_in_20s_analysis];   //circular buffer
			mN_between_analyses_count=0;
			mFlag_did_analysis=false;
			mOx_copy=new float[mN_samples_in_analysis];
			mN=0;
			d=new datastructure(mFields_in_sensorvector);
			mSensor_history=new datastructure[mN_samples_in_analysis];		
			for(int i=0;i<mN_samples_in_analysis;i++)
				mSensor_history[i]=new datastructure(mFields_in_sensorvector);
			mReduced_time=new float[mN_samples_in_analysis];
			mOrientationVector=new float[4];
			mOrientationVectorWC=new float[4];
			mAccelerationVector=new float[4];
			mAccelerationVectorWC=new float[4];
			mLinearAccelerationVector=new float[4];
			mLinearAccelerationVectorWC=new float[4];

			
			//			mMagnetometerVector=new float[4];
			Rotation_matrix = new float[16]; //mLastX
			Inclination_matrix = new float[16]; //mLastX
			mVzCount=0;
			for (int i=0;i<mN_samples_in_2s_analysis;i++)
			{
				mVz[i]=0;
				mOy_2s[i]=0;
			}
			for (int i=0;i<mN_samples_in_20s_analysis;i++)
			{
				mOy_20s[i]=0;
			}

			//					mAa=new float[mN_samples_in_analysis];
			mFlag_did_analysis=false;
			//					mOx_copy=new float[mN_samples_in_analysis];
			mN=0;
			ppscore=0;
			mLastX = 0;
			mLastY = 0;
			mLastZ = 0;
			mLastA = 0;
			mLastP = 0;
			mLastR = 0;
			mLastGx =0;
			mLastGy =0;
			mLastGz = 0;

			tvOx= (TextView)findViewById(R.id.o_x);
			tvOy= (TextView)findViewById(R.id.o_y);
			tvOz= (TextView)findViewById(R.id.o_z);
			tvOx.setText("na");
			tvOy.setText("na");
			tvOz.setText("na");
			tvOx= (TextView)findViewById(R.id.h_x);
			tvOy= (TextView)findViewById(R.id.h_y);
			tvOz= (TextView)findViewById(R.id.h_z);
			tvOx.setText("na");
			tvOy.setText("na");
			tvOz.setText("na");
//			tvOx= (TextView)findViewById(R.id.a_x);
//			tvOy= (TextView)findViewById(R.id.a_y);
//			tvOz= (TextView)findViewById(R.id.a_z);
//			tvOx.setText("na");
//			tvOy.setText("na");
//			tvOz.setText("na");
			tvOx= (TextView)findViewById(R.id.g_x);
			tvOy= (TextView)findViewById(R.id.g_y);
			tvOz= (TextView)findViewById(R.id.g_z);
			tvOx.setText("na");
			tvOy.setText("na");
			tvOz.setText("na");			        
//			tvOx= (TextView)findViewById(R.id.r_z1);
//			tvOy= (TextView)findViewById(R.id.r_z2);
//			tvOz= (TextView)findViewById(R.id.r_z3);
//			tvOx.setText("na");
//			tvOy.setText("na");
//			tvOz.setText("na");
//			tvOx= (TextView)findViewById(R.id.r_x1);
//			tvOy= (TextView)findViewById(R.id.r_x2);
//			tvOz= (TextView)findViewById(R.id.r_x3);
//			tvOx.setText("na");
//			tvOy.setText("na");
//			tvOz.setText("na");     
//			tvOx= (TextView)findViewById(R.id.r_y1);
//			tvOy= (TextView)findViewById(R.id.r_y2);
//			tvOz= (TextView)findViewById(R.id.r_y3);
//			tvOx.setText("na");
//			tvOy.setText("na");
//			tvOz.setText("na");    

		}
		if (event.sensor == mAccelerometer){

			mGot_Acceleration=true  ;

			mLastX = event.values[0];
			mLastY = event.values[1];
			mLastZ = event.values[2];
			mAccelerationVector[0]=event.values[0];
			mAccelerationVector[1]=event.values[1];
			mAccelerationVector[2]=event.values[2];
			mAccelerationVector[3]=(float)0.0;
			mLastAnorm=(float)Math.sqrt(mLastX*mLastX+mLastY*mLastY+mLastZ*mLastZ);
//			TextView tvAx= (TextView)findViewById(R.id.a_x);
//			TextView tvAy= (TextView)findViewById(R.id.a_y);
//			TextView tvAz= (TextView)findViewById(R.id.a_z);
//			tvAx.setText(Float.toString((float)(Math.round(mLastX*100.0)/100.0)));
//			tvAy.setText(Float.toString((float)(Math.round(mLastY*100.0)/100.0)));
//			tvAz.setText(Float.toString((float)(Math.round(mLastZ*100.0)/100.0)));

		}
		else if (event.sensor == mLinearAccelerometer){

			mGot_Acceleration=true  ;
			mLinearAccelerationVector[0]=event.values[0];
			mLinearAccelerationVector[1]=event.values[1];
			mLinearAccelerationVector[2]=event.values[2];
			mLinearAccelerationVector[3]=(float)0.0;
			//		mLastAnorm=(float)Math.sqrt(mLastX*mLastX+mLastY*mLastY+mLastZ*mLastZ);

			 TextView tvAx= (TextView)findViewById(R.id.a_xlin);
	//		 tvAx.setTextColor(getResources().getColor(R.color.red));
			TextView tvAy= (TextView)findViewById(R.id.a_ylin);
			TextView tvAz= (TextView)findViewById(R.id.a_zlin);
			tvAx.setText(Float.toString((float)(Math.round(event.values[0]*100.0)/100.0)));
			tvAy.setText(Float.toString((float)(Math.round(event.values[1]*100.0)/100.0)));
			tvAz.setText(Float.toString((float)(Math.round(event.values[2]*100.0)/100.0)));

		}
		else if (event.sensor == mOrientation){
			mGot_Orientation=true  ;

			//use values[3..5] to avoid being affected by portrait/landscape mode
			mLastA = event.values[0];
			mLastP = event.values[1];
			mLastR = event.values[2];
			mOrientationVector[0]=mLastA;
			mOrientationVector[1]=mLastP;
			mOrientationVector[2]=mLastR;
			mOrientationVector[3]=(float)0.0;
			//			mLastOnorm=(float)Math.sqrt(mLastA*mLastA+mLastP*mLastP+mLastR*mLastR);
			tvOx= (TextView)findViewById(R.id.o_x);
			tvOy= (TextView)findViewById(R.id.o_y);
			tvOz= (TextView)findViewById(R.id.o_z);

			tvOx.setText(Float.toString((float)(Math.round(mLastA*100.0)/100.0)));
			tvOy.setText(Float.toString((float)(Math.round(mLastP*100.0)/100.0)));
			tvOz.setText(Float.toString((float)(Math.round(mLastR*100.0)/100.0)));
		}

		else if (event.sensor == mMagneticField){
			mGot_Magnetometer=true  ;

			//use values[3..5] to avoid being affected by portrait/landscape mode
			mLastHx = event.values[0];
			mLastHy = event.values[1];
			mLastHz= event.values[2];
			//			mLastOnorm=(float)Math.sqrt(mLastA*mLastA+mLastP*mLastP+mLastR*mLastR);
			tvOx= (TextView)findViewById(R.id.h_x);
			tvOy= (TextView)findViewById(R.id.h_y);
			tvOz= (TextView)findViewById(R.id.h_z);

			tvOx.setText(Float.toString((float)(Math.round(mLastHx*100.0)/100.0)));
			tvOy.setText(Float.toString((float)(Math.round(mLastHy*100.0)/100.0)));
			tvOz.setText(Float.toString((float)(Math.round(mLastHz*100.0)/100.0)));
		}
		else if (event.sensor == mGyro){
			mGot_Gyro=true  ;

			mLastGx = event.values[0];
			mLastGy = event.values[1];
			mLastGz = event.values[2];
			mLastGnorm=(float)Math.sqrt(mLastGx*mLastGx+mLastGy*mLastGy+mLastGz*mLastGz);

			tvOx= (TextView)findViewById(R.id.g_x);
			tvOy= (TextView)findViewById(R.id.g_y);
			tvOz= (TextView)findViewById(R.id.g_z);

			tvOx.setText(Float.toString((float)(Math.round(mLastGx*100.0)/100.0)));
			tvOy.setText(Float.toString((float)(Math.round(mLastGy*100.0)/100.0)));
			tvOz.setText(Float.toString((float)(Math.round(mLastGz*100.0)/100.0)));
		}
		else {
			//assert should not get here
		}



		if(mGot_Acceleration && mGot_Magnetometer) {
			float [] gravity = new float[4]; 
			gravity[0]=mLastX;
			gravity[1]=mLastY;		
			gravity[2]=mLastZ;
			gravity[3]=(float)0.0;
			float[] geomagnetic = new float[4]; 
			geomagnetic[0]=mLastHx;
			geomagnetic[1]=mLastHy;		
			geomagnetic[2]=mLastHz;		
			geomagnetic[3]=(float)0.0;		

			bool1=SensorManager.getRotationMatrix ( Rotation_matrix,  Inclination_matrix, gravity,  geomagnetic);

			if(bool1){
//				tvOx= (TextView)findViewById(R.id.r_x1);
//				tvOy= (TextView)findViewById(R.id.r_x2);
//				tvOz= (TextView)findViewById(R.id.r_x3);
//				tvOzz= (TextView)findViewById(R.id.r_x4);
//				tvOx.setText(Float.toString((float)(Math.round(Rotation_matrix[0]*100.0)/100.0)));
//				tvOy.setText(Float.toString((float)(Math.round(Rotation_matrix[1]*100.0)/100.0)));
//				tvOz.setText(Float.toString((float)(Math.round(Rotation_matrix[2]*100.0)/100.0)));
//				tvOzz.setText(Float.toString((float)(Math.round(Rotation_matrix[3]*100.0)/100.0)));

//				tvOx= (TextView)findViewById(R.id.r_y1);
//				tvOy= (TextView)findViewById(R.id.r_y2);
//				tvOz= (TextView)findViewById(R.id.r_y3);
//				tvOzz= (TextView)findViewById(R.id.r_y4);
//				tvOx.setText(Float.toString((float)(Math.round(Rotation_matrix[4]*100.0)/100.0)));
//				tvOy.setText(Float.toString((float)(Math.round(Rotation_matrix[5]*100.0)/100.0)));
//				tvOz.setText(Float.toString((float)(Math.round(Rotation_matrix[6]*100.0)/100.0)));
//				tvOzz.setText(Float.toString((float)(Math.round(Rotation_matrix[7]*100.0)/100.0)));

//				tvOx= (TextView)findViewById(R.id.r_z1);
//				tvOy= (TextView)findViewById(R.id.r_z2);
//				tvOz= (TextView)findViewById(R.id.r_z3);
//				tvOzz= (TextView)findViewById(R.id.r_z4);
//				tvOx.setText(Float.toString((float)(Math.round(Rotation_matrix[8]*100.0)/100.0)));
//				tvOy.setText(Float.toString((float)(Math.round(Rotation_matrix[9]*100.0)/100.0)));
//				tvOz.setText(Float.toString((float)(Math.round(Rotation_matrix[10]*100.0)/100.0)));		
//				tvOzz.setText(Float.toString((float)(Math.round(Rotation_matrix[11]*100.0)/100.0)));

//				tvOx= (TextView)findViewById(R.id.r_zz1);
//				tvOy= (TextView)findViewById(R.id.r_zz2);
//				tvOz= (TextView)findViewById(R.id.r_zz3);
//				tvOzz= (TextView)findViewById(R.id.r_zz4);
//				tvOx.setText(Float.toString((float)(Math.round(Rotation_matrix[12]*100.0)/100.0)));
//				tvOy.setText(Float.toString((float)(Math.round(Rotation_matrix[13]*100.0)/100.0)));
//				tvOz.setText(Float.toString((float)(Math.round(Rotation_matrix[14]*100.0)/100.0)));		
//				tvOzz.setText(Float.toString((float)(Math.round(Rotation_matrix[15]*100.0)/100.0)));		


				//				SensorManager.getOrientation(Rotation_matrix,  mOrientationVector);
				//				tvOx= (TextView)findViewById(R.id.o_x);
				//				tvOy= (TextView)findViewById(R.id.o_y);
				//				tvOz= (TextView)findViewById(R.id.o_z);
				//				tvOx.setText(Float.toString((float)(Math.round(mOrientationVector[0]*100.0)/100.0)));
				//				tvOy.setText(Float.toString((float)(Math.round(mOrientationVector[1]*100.0)/100.0)));
				//				tvOz.setText(Float.toString((float)(Math.round(mOrientationVector[2]*100.0)/100.0)));

				Matrix.flatmultiply(mAccelerationVectorWC, Rotation_matrix, mAccelerationVector);
				//				System.out.print("acc vectorwc");
				//				Matrix.vectorprint(mAccelerationVectorWC);
				//				System.out.print("rot matrix");
				//			Matrix.arrayprint(Rotation_matrix);

				//Matrix.multiply(mAccelerationVectorWC, Rotation_matrix, mAccelerationVector);

				tvOx= (TextView)findViewById(R.id.a_xp);
				tvOy= (TextView)findViewById(R.id.a_yp);
				tvOz= (TextView)findViewById(R.id.a_zp);
				tvOx.setText(Float.toString((float)(Math.round(mAccelerationVectorWC[0]*1000.0)/1000.0)));
				tvOy.setText(Float.toString((float)(Math.round(mAccelerationVectorWC[1]*1000.0)/1000.0)));
				tvOz.setText(Float.toString((float)(Math.round(mAccelerationVectorWC[2]*1000.0)/1000.0)));

				Matrix.flatmultiply(mLinearAccelerationVectorWC, Rotation_matrix, mLinearAccelerationVector);
//				System.out.print("linacc vector, wc");
//				Matrix.vectorprint(mLinearAccelerationVectorWC);
				tvOx= (TextView)findViewById(R.id.a_xpl);
				tvOy= (TextView)findViewById(R.id.a_ypl);
				tvOz= (TextView)findViewById(R.id.a_zpl);
				tvOx.setText(Float.toString((float)(Math.round(mLinearAccelerationVectorWC[0]*1000.0)/1000.0)));
				tvOy.setText(Float.toString((float)(Math.round(mLinearAccelerationVectorWC[1]*1000.0)/1000.0)));
				tvOz.setText(Float.toString((float)(Math.round(mLinearAccelerationVectorWC[2]*1000.0)/1000.0)));
			}
		}

		if(mGot_Acceleration && mGot_Orientation) {
			//			mOrientationVector[0]=mLastA;
			//			mOrientationVector[1]=mLastP;
			//			mOrientationVector[2]=mLastR;
			//					SensorManager.getRotationMatrixFromVector (Rotation_matrix,mOrientationVector);
			//	//		   		SensorManager.getOrientation(Rotation_matrix,  mOrientationVector);
			//			            tvOx= (TextView)findViewById(R.id.r_x1);
			//			            tvOy= (TextView)findViewById(R.id.r_x2);
			//			            tvOz= (TextView)findViewById(R.id.r_x3);
			//			           tvOx.setText(Float.toString((float)(Math.round(Rotation_matrix[0]*100.0)/100.0)));
			//			           tvOy.setText(Float.toString((float)(Math.round(Rotation_matrix[1]*100.0)/100.0)));
			//			           tvOz.setText(Float.toString((float)(Math.round(Rotation_matrix[2]*100.0)/100.0)));
			//			            tvOx= (TextView)findViewById(R.id.r_y1);
			//			            tvOy= (TextView)findViewById(R.id.r_y2);
			//			            tvOz= (TextView)findViewById(R.id.r_y3);
			//			           tvOx.setText(Float.toString((float)(Math.round(Rotation_matrix[3]*100.0)/100.0)));
			//			           tvOy.setText(Float.toString((float)(Math.round(Rotation_matrix[4]*100.0)/100.0)));
			//			           tvOz.setText(Float.toString((float)(Math.round(Rotation_matrix[5]*100.0)/100.0)));
			//			            tvOx= (TextView)findViewById(R.id.r_z1);
			//			            tvOy= (TextView)findViewById(R.id.r_z2);
			//			            tvOz= (TextView)findViewById(R.id.r_z3);
			//			           tvOx.setText(Float.toString((float)(Math.round(Rotation_matrix[6]*100.0)/100.0)));
			//			           tvOy.setText(Float.toString((float)(Math.round(Rotation_matrix[7]*100.0)/100.0)));
			//			           tvOz.setText(Float.toString((float)(Math.round(Rotation_matrix[8]*100.0)/100.0)));		
			//			           tvOx= (TextView)findViewById(R.id.o_x);
			//			           tvOy= (TextView)findViewById(R.id.o_y);
			//			           tvOz= (TextView)findViewById(R.id.o_z);

		}

 //       TextView  tv = new TextView(this);

		
	  tvOz= (TextView)findViewById(R.id.unclaimed2);
      tvOz.setText( stringFromJNI() );
	  tvOz= (TextView)findViewById(R.id.a_c4r12);
  //    tvOz.setText(Float.toString((float)floatFromJNI()));		
  //    tvOz.setText(Double.toString(intFromJNI()/100.0));		
//      tvOz.setText(Double.toString(intFromJNIwithInput((int)321)/100.0));		
//      tvOz.setText(Double.toString(intFromJNIwithFloatInput((float)321.2)/100.0));		
      tvOz.setText(Double.toString(intFromJNIwithDoubleInput((double)321.2)));		
      
		
		//        setContentView(tv);


		//write file and do calculations 
		abbrev=mMode.substring(0,0);//.charAt(0);
		if (mLoggerStatus && mFOS!=null && timeStamp-mLastTimeStamp > mMinimumLogInterval)
		{
			String s=String.format("%d %s %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.3f %.3f %.3f %b %b \r\n", timeStamp%1000000, abbrev, mLastX, mLastY, mLastZ, mLastA, mLastP, mLastR,mLastGx, mLastGy, mLastGz,mLinearAccelerationVectorWC[0],mLinearAccelerationVectorWC[1],mLinearAccelerationVectorWC[2],mWalking,mDriving );
			byte[] data = (s).getBytes();
			float[] vs = new float[mFields_in_sensorvector];
			try {
				mFOS.write(data);
				mFOS.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

			datastructure dd=new datastructure(mFields_in_sensorvector); 
			mLastTimeStamp=timeStamp;


			vs[0]=timeStamp;
			vs[1]=mLastX;vs[2]=mLastY;vs[3]=mLastZ;
			vs[4]=mLastA;vs[5]=mLastP;vs[6]=mLastR;
			vs[7]=mLastGx;vs[8]=mLastGy;vs[9]=mLastGz;
			vs[10]=mLinearAccelerationVectorWC[0];vs[11]=mLinearAccelerationVectorWC[1];vs[12]=mLinearAccelerationVectorWC[2];
			dd.fillchar(abbrev);
			dd.set_values(vs);
//			System.out.println("got here 823");
			calculate_driving_likelihood(dd);
				
///begin copy
			//calculate gyration
//			if(mGot_Gyro) mAvgGabs=mAvgGabsAlpha*mAvgGabs+mLastGnorm;
//			tvOx= (TextView)findViewById(R.id.g_norm_avg);
//			tvOx.setText(Float.toString((float)(Math.round(mAvgGabs*100.0)/100.0)));
//
//
//			//calculate velocity and printout
//			V2=integrate(mLinearAccelerationVectorWC,V2,beta);
//			V=integrate(mLinearAccelerationVectorWC,V,1);
//			mVhCurrent=(float)Math.sqrt(V[0]*V[0]+V[1]*V[1]);
//			mVh2Current=(float)Math.sqrt(V2[0]*V2[0]+V2[1]*V2[1]);
//			if(mVhCurrent>Vhmax) Vhmax=mVhCurrent;
//			if(mVh2Current>Vh2max) Vh2max=mVh2Current;

//			// calculate Vz avg and std
//			mVz[mVzCount++]=V2[2];	
//			mVzStd=0;
//			mVzAvg=0;
//			if (mVzCount>mN_samples_in_2s_analysis-1)mVzCount=0;
//
//			for (int i=0;i<mN_samples_in_2s_analysis;i++)
//			{
//				mVzAvg+=mVz[i];
//			}
//			mVzAvg=mVzAvg/mN_samples_in_2s_analysis;
//			for (int i=0;i<mN_samples_in_2s_analysis;i++)
//			{
//				mVzStd+=(mVz[i]-mVzAvg)*(mVz[i]-mVzAvg);
//			}
//			mVzStd=(float) Math.sqrt((double)mVzStd/mN_samples_in_2s_analysis);
//
//			// calculate Oy avg and std for 2s
//			mSampleCount2s++; if(mSampleCount2s>mN_samples_in_2s_analysis-1) mSampleCount2s=0;
//			mOy[mSampleCount2s]=mLastP;
//			mOy_std2s=0;
//			mOy_avg2s=0;
//			for (int i=0;i<mN_samples_in_2s_analysis;i++)
//			{
//				mOy_avg2s+=mOy[i];
//			}
//			mOy_avg2s=mOy_avg2s/mN_samples_in_2s_analysis;
//			for (int i=0;i<mN_samples_in_2s_analysis;i++)
//			{
//				mOy_std2s+=(mOy[i]-mOy_avg2s)*(mOy[i]-mOy_avg2s);
//			}
//			mOy_std2s=(float) Math.sqrt((double)mOy_std2s/mN_samples_in_2s_analysis);
//
//			// calculate Oy avg and std for 20s
//			mSampleCount20s++; if(mSampleCount20s>mN_samples_in_20s_analysis-1) mSampleCount20s=0;
//			mOy[mSampleCount20s]=mLastP;
//			mOy_std20s=0;
//			mOy_avg20s=0;
//			for (int i=0;i<mN_samples_in_20s_analysis;i++)
//			{
//				mOy_avg20s+=mOy[i];
//			}
//			mOy_avg20s=mOy_avg20s/mN_samples_in_20s_analysis;
//			for (int i=0;i<mN_samples_in_20s_analysis;i++)
//			{
//				mOy_std20s+=(mOy[i]-mOy_avg20s)*(mOy[i]-mOy_avg20s);
//			}
//			mOy_std20s=(float) Math.sqrt((double)mOy_std20s/mN_samples_in_20s_analysis);
//
//			
//			
//			/// are we walking???
//			if (mAvgGabs>mGyro_Walking_Threshold && 
//					mVzStd>mVzStdMinWalkingThreshold &&
//					mOy_std2s>mOyStd2sMinWalkingThreshold &&
//					mOy_std20s>mOyStd20sMinWalkingThreshold ) 
//			{
//				mWalking=true;
//				playWalk();
//				mTimeofLastWalkingDetection=timeStamp;
//			}
//			else mWalking=false;
//
//			/// are we driving???
//			if (!mWalking&&
//					mVh2Current>mdriving_velocity_threshold &&
//					(timeStamp-mTimeofLastWalkingDetection)>mtime_after_walking_to_filter_driving*1000 && 
//					mVzStd<mVzStdMaxDrivingThreshold && 
//					Math.abs(mVzAvg)<mVzAvgMaxDrivingThreshold &&
//					mOy_std2s<mOyStd2sMaxDrivingThreshold &&
//					mOy_std20s<mOyStd20sMaxDrivingThreshold)
//			{
//				mDriving=true;
//				playHonk();
//			}
//			else mDriving=false;
//
//			tvOx= (TextView)findViewById(R.id.drv_wlk);
//			if(mWalking)   tvOx.setText("Walking");
//			else if(mDriving)   tvOx.setText("Driving");
//			else  tvOx.setText("neither");
//
//		
//			
//			tvOx= (TextView)findViewById(R.id.v_x);
//			tvOy= (TextView)findViewById(R.id.v_y);
//			tvOz= (TextView)findViewById(R.id.v_z);
//			tvOzz= (TextView)findViewById(R.id.v_h2);
//			tvOx.setText(Float.toString((float)(Math.round(V[0]*100.0)/100.0)));
//			tvOy.setText(Float.toString((float)(Math.round(V[1]*100.0)/100.0)));
//			tvOz.setText(Float.toString((float)(Math.round(V[2]*100.0)/100.0)));
//			tvOzz.setText(Float.toString((float)(Math.round(Math.sqrt(V2[0]*V2[0]+V2[1]*V2[1])*100.0)/100.0)));
//			tvOx= (TextView)findViewById(R.id.v_x2);
//			tvOy= (TextView)findViewById(R.id.v_y2);
//			tvOz= (TextView)findViewById(R.id.v_z2);
//			tvOzz= (TextView)findViewById(R.id.v_zz2);
//			tvOx.setText(Float.toString((float)(Math.round(V2[0]*100.0)/100.0)));
//			tvOy.setText(Float.toString((float)(Math.round(V2[1]*100.0)/100.0)));
//			tvOz.setText(Float.toString((float)(Math.round(V2[2]*100.0)/100.0)));
//			tvOzz.setText(Float.toString((float)(Math.round(Vh2max*100.0)/100.0)));
//
//			tvOx= (TextView)findViewById(R.id.v_z_avg);
//			tvOy= (TextView)findViewById(R.id.v_z_std);
//			tvOx.setText(Float.toString((float)(Math.round(mVzAvg*100.0)/100.0)));
//			tvOy.setText(Float.toString((float)(Math.round(mVzStd*100.0)/100.0)));
//
//			tvOx= (TextView)findViewById(R.id.o_y_avg);
//			tvOy= (TextView)findViewById(R.id.o_y_std);
//			tvOx.setText(Float.toString((float)(Math.round(mOy_avg2s*100.0)/100.0)));
//			tvOy.setText(Float.toString((float)(Math.round(mOy_std2s*100.0)/100.0)));
			
////end copy
			//	        ppscore=calculate_parking_likelihood(dd);


			//ppscore=(float)6.66;
			// print results to screen
			//        TextView tvAa_avg= (TextView)findViewById(R.id.tAa_avg);
			//		    TextView tvAa_std= (TextView)findViewById(R.id.tAa_std);
			//	    tvAa_avg.setText(Float.toString((float)(Math.round(mAa_avg*100.0)/100.0)));
			//	    tvAa_std.setText(Float.toString((float)(Math.round(mAa_std*100.0)/100.0)));

			/*		    TextView tvOx_avg1= (TextView)findViewById(R.id.tOx_avg1);
		    TextView tvOx_std1= (TextView)findViewById(R.id.tOx_std1);
		    tvOx_avg1.setText(Float.toString((float)(Math.round(mOx_avg1*100.0)/100.0)));
		    tvOx_std1.setText(Float.toString((float)(Math.round(mOx_std1*100.0)/100.0)));

		    TextView tvOx_avg2= (TextView)findViewById(R.id.tOx_avg2);
		    TextView tvOx_std2= (TextView)findViewById(R.id.tOx_std2);
		    tvOx_avg2.setText(Float.toString((float)(Math.round(mOx_avg2*100.0)/100.0)));
		    tvOx_std2.setText(Float.toString((float)(Math.round(mOx_std2*100.0)/100.0)));

		    TextView tvpp= (TextView)findViewById(R.id.tpp);
		    tvpp.setText(Float.toString((float)(Math.round(ppscore*1000.0)/1000.0)));

		    TextView tvpp_hi= (TextView)findViewById(R.id.tpp_hi);
		    tvpp_hi.setText(Float.toString((float)(Math.round(highest_ppscore*1000.0)/1000.0)));

			 */


			//		    TextView tvStatus= (TextView)findViewById(R.id.tStatus1);
			//			tvStatus.setText("point 1");

			//		    mPaint.setColor(Color.rgb((int)(ppscore*255), 0, 0));
			//		    mPaint.setStrokeWidth(10);
			//		    mCanvas.drawRect(100, 100, 200, 200, mPaint);

		}
	}

	////	@Override

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Do nothing.
	}



	// ----------------------ANALYSIS-----------------------------

	//	
	//	private static float meansqdiff(float[]y1,float[]y2)
	//	{
	//float answer=0;
	//		for(int i=0;i<y1.length;i++)	{	
	//	answer+=(y1[i]-y2[i])*(y1[i]-y2[i]);
	//	}
	//		answer=answer/y1.length;
	//		answer=(float)Math.sqrt(answer);
	//return answer;
	//}
	//	
	//	private static float meansqdiff(float[]y1,float[]y2,int start, int end)
	//	{
	//		float answer=0;
	//		for(int i=start;i<end;i++)	{	
	//			answer+=(y1[i]-y2[i])*(y1[i]-y2[i]);
	//		}
	//		answer=answer/(end-start);
	//		answer=(float)Math.sqrt(answer);
	//		return answer;
	//	}
	//


	//	public float calculate_parking_likelihood(datastructure dat) {
	//
	//ppscore1=0;
	//ppscore2=0;m2C=0;
	//ppscore3=0;mdOx=0;
	//ppscore4=0;mmsd_avg=0;
	//ppscore5=0;mdt=0;
	//ppscore6=0;mGa_std=0;
	//ppscore7=0;mdOx_start_vs_end=0;
	//ppscore8=0;m1B=0;m3B=0;
	//ppscore=0;ppscore_running_avg=0;
	//highest_ppscore=0;
	//
	//			
	//			mmsd1=0;
	//		mmsd2=0;
	//		mmsd3=0;
	//
	//		mtime[mN]=dat.vals[0]; //mtime   (t,Ax,Ay,Az,mOx,Oy,Oz,Gx,Gy,Gz)
	//		mOx[mN]=dat.vals[4]; //mOx
	//		mAa[mN]=(float)Math.sqrt(dat.vals[1]*dat.vals[1]+dat.vals[2]*dat.vals[2]+dat.vals[3]*dat.vals[3]); //accel magnitude
	//		mGa[mN]=(float)Math.sqrt(dat.vals[7]*dat.vals[7]+dat.vals[8]*dat.vals[8]+dat.vals[9]*dat.vals[9]); //gyro magnitude 
	//
	//		
	//		TextView tvStatus= (TextView)findViewById(R.id.tmN);
	//		String s=String.format("%d",mN);
	//	    tvStatus.setText(s);
	//
	//  		//if buffer is full	do analysis 			
	//		if(mN==mN_samples_in_analysis-1 || (mN_between_analyses_count==mN_samples_between_analyses-1 && mFlag_did_analysis)){
	//
	//			
	//			 tvStatus= (TextView)findViewById(R.id.tStatus2);
	//			 s="AAA";
	//		    tvStatus.setText(s);
	//
	//		    mN=mN_samples_in_analysis-mN_samples_between_analyses-1;
	//
	//		    System.out.println("hit sample buffer length, doing analysis");
	//			myPolynome1 =new PolynomialFitter(1);
	//			myPolynome2 =new PolynomialFitter(2);
	//			myPolynome3 =new PolynomialFitter(1);
	//			mT0=mtime[0];
	//			mOx_high=0;mOx_low=360;
	//			mOx_high_time=0;mOx_low_time=0;
	//			
	//			
	//			tvStatus= (TextView)findViewById(R.id.tStatus2);
	//			s="BBB";
	//		    tvStatus.setText(s);
	//
	////			find_best_fit();
	//			mGa_std=0;
	//			for (int i=0;i<mN_samples_in_analysis;i++) {
	//				mGa_std+=(mGa[i]-0)*(mGa[i]-0);
	//			}
	//			mGa_std=(float)Math.sqrt(mGa_std/mN_samples_in_analysis);
	//			mN_between_analyses_count=mN_samples_in_analysis-mN_samples_between_analyses-1;
	//			mFlag_did_analysis=true;
	//
	//			if(debug) {for(int i=0;i<20;i++){
	//				System.out.print("t"+i+":"+mtime[i]+" mOx"+mOx[i]);
	//			}
	//			System.out.println();}
	//
	//
	//			m1C=Math.abs(m1C);
	//			mdOx=Math.abs(mOx_high-mOx_low);
	//			System.out.println("dOx "+mdOx+" Oxhi:"+mOx_high+" Oxlo:"+"mOx_low");
	//
	//		//mdt=Math.abs(mOx_high_time-mOx_low_time);
	//			mdt=(mtime[mN_fit2end]-mtime[mN_fit1end])/1000;
	//
	//			// calculate parallel parking score - product of Gaussians for Aa, Oxstd1, and Oxstd2
	//			ppscore=1;
	//
	//			//magnitude of std(Aa)
	//			ppscore1=(float)Math.exp(-(mAa_std-mAa_std_desiredstd)*(mAa_std-mAa_std_desiredstd)/(2*mAa_std_desiredstd*mAa_std_desiredstd));
	//			//	        ppscore=ppscore*(float)Math.exp(-(mOx_std1-mOx_std1_desiredavg)*(mOx_std1-mOx_std1_desiredavg)/(2*mOx_std1_desiredstd*mOx_std1_desiredstd));
	//			//       ppscore=ppscore*(float)Math.exp(-(mOx_std2-mOx_std2_desiredavg)*(mOx_std2-mOx_std2_desiredavg)/(2*mOx_std2_desiredstd*mOx_std2_desiredstd));
	//			// the coefficient of t^2 (Ox=A+Bt+Ct^2) , times a factor killing too-small values
	//
	//			//value of quadratic curvature - gaussian 
	//			ppscore2=(float)Math.exp(-(m2C-m2C_desired)*(m2C-m2C_desired)/(2*m2C_std*m2C_std));
	//			//times sigmoid
	//			ppscore2=(float)(ppscore2*Math.exp(-m2C_Ea/Math.abs(m2C)));
	//
	//			//max change in Ox
	//			ppscore3=(float)Math.exp(-(mdOx-mdOx_desired)*(mdOx-mdOx_desired)/(2*mdOx_std*mdOx_std));
	//
	//			//goodness of fit of parabola
	//			ppscore4=(float)Math.exp(-(mmsd2-mmsd2_desired)*(mmsd2-mmsd2_desired)/(2*mmsd2_std*mmsd2_std));
	//			//goodness of fit of lines
	//			ppscore4=(float)Math.exp(-(mmsd1-mmsd1_desired)*(mmsd1-mmsd1_desired)/(2*mmsd1_std*mmsd1_std));
	//			ppscore4=(float)Math.exp(-(mmsd3-mmsd3_desired)*(mmsd3-mmsd1_desired)/(2*mmsd3_std*mmsd3_std));
	//
	//			//time interval bet. highest and lowest point
	//			ppscore5=(float)Math.exp(-(mdt-mdt_desired)*(mdt-mdt_desired)/(2*mdt_std*mdt_std));
	//			ppscore5=ppscore5*(float)(1/(1+Math.exp(-(mdt-mmin_dt_Ea))));
	//			//if(mdt<mmin_parking_time/2)ppscore5=(float)0;
	//
	//			//gyro std
	//			ppscore6=(float)Math.exp(-(mGa_std-mGa_std_desired)*(mGa_std-mGa_std_desired)/(2*mGa_std_std*mGa_std_std));
	//					
	//			//diff bet left and right line heights
	//			float mdOx_start_vs_end=(float)Math.abs(m1A-m3A);
	//			float avgOx1=(float)(m1A+(mReduced_time[(int)((mN_fit1end-mN_fit1start)/2.0+mN_fit1start)])*m1B);
	//			float avgOx3=(float)(m3A+(mReduced_time[(int)((mN_fit3end-mN_fit3start)/2.0+mN_fit3start)])*m3B);
	//			mdOx_start_vs_end=Math.abs(avgOx3-avgOx1);
	//			System.out.println ("avgox1:"+avgOx1+" avgOx2:"+avgOx3);
	//
	//			ppscore7=(float)Math.exp(-(mdOx_start_vs_end-mdOx_start_vs_end_desired)*(mdOx_start_vs_end-mdOx_start_vs_end_desired)/(2*mdOx_start_vs_end_std*mdOx_start_vs_end_std));
	//			// USE START VS END oX AVG INSTEAD OF M1A-M3A
	//
	//			//how flat are initial and final segments
	//			ppscore8=(float)Math.exp(-(m1B-m1B_desired)*(m1B-m1B_desired)/(2*(m1B_std*m1B_std)));
	//			ppscore8=(float)Math.exp(-(m3B-m3B_desired)*(m3B-m3B_desired)/(2*(m3B_std*m3B_std)));
	//
	//			//ppscore1,6 taken out
	//			ppscore=ppscore2*ppscore3*ppscore4*ppscore5*ppscore7*ppscore8;
	//			ppscore_running_avg=ppscore*alpha+ppscore_running_avg*(1-alpha);
	//			if(ppscore_running_avg>highest_ppscore) highest_ppscore=ppscore_running_avg;
	//			//				 System.out.println(String.format("%6.3e",223.45654543434));
	//			if (debug){
	//				String str="msd1="+((int)(mmsd1*100))/100.0+" A1:"+String.format("%3.0f",m1A)+" B1:"+String.format("%1.1e",m1B);
	//				str=str+"msd2="+((int)(mmsd2*100))/100.0+" A2:"+String.format("%3.0f",m2A)+" B2:"+String.format("%1.1e ",m2B)+"C2:"+String.format("%1.1e",m2C)+"\n";
	//				str=str+"msd3="+((int)(mmsd3*100))/100.0+" A3:"+String.format("%3.0f",m3A)+" B3:"+String.format("%1.1e ",m3B);
	//				str=str+"pp:"+String.format("%.2f",ppscore)+" hi:"+String.format("%.2f ",highest_ppscore)+String.format("Pavg %.2f",ppscore_running_avg)+"state:"+d.s+"\n"+"p1:"+String.format("%.1e(%.1e)",ppscore1,mAa_std)+" p2:"+String.format("%.2f(%1.1e)",ppscore2,m2C)+" p3:"+String.format("%.2f(%2.1f)",ppscore3,mdOx)+" p4:"+String.format("%.2f(%.2f)",ppscore4,mmsd2)+"\n";
	//				str=str+" p5:"+String.format("%.2f(%2.0f)",ppscore5,mdt)+" p6:"+String.format("%.2f(%1.1f)",ppscore6,mGa_std)+" p7:"+String.format("%.2f(%1.1f)",ppscore7,mdOx_start_vs_end)+" p8:"+String.format("%.2f (%1.1f,%1.1f)",ppscore8,m1B,m3B);
	//				System.out.println ("title string="+str);
	////				demo.updateplot("mOx curve",mtime,mOx,mOx_fit1,mOx_fit2,mOx_fit3,mN_fit2start,mN_fit3start,str);
	////				demo.pack();
	////				RefineryUtilities.centerFrameOnScreen(demo);
	////				demo.setVisible(true);			        
	//			}
	//			 tvStatus= (TextView)findViewById(R.id.tStatus2);
	//			 s="CCC";
	//		    tvStatus.setText(s);
	//
	//			printresults();
	////			if(android){
	////				printresults(); }
	//
	//			System.arraycopy(mOx, mN_samples_between_analyses,mOx, 0, mN_samples_in_analysis-mN_samples_between_analyses);
	//			System.arraycopy(mtime, mN_samples_between_analyses,mtime, 0, mN_samples_in_analysis-mN_samples_between_analyses);
	//		
	//		}
	//	
	//		mN=mN+1;
	//		mN_between_analyses_count+=1;
	//		//				System.out.println("db3");
	//
	//		//		mA=(double)myPoly.getY;
	//		//	     CloseLogFile(mFOS);
	//
	//		return(ppscore);
	//	}
	//

	static void find_best_fit(){
		/* 	float best_msd=1000;
		int fmN_fit1start=0, fmN_fit1end=0;
		int fmN_fit2start=0, fmN_fit2end=0;
		int fmN_fit3start=0, fmN_fit3end=0;
		float fmOx_high, fmOx_high_time;
		float fmOx_low, fmOx_low_time;
		double fm1A, fm1B,fm2A,fm2B,fm2C,fm3A,fm3B;
		float fmmsd1=0,fmmsd2=0,fmmsd3=0;
		fmOx_high=0;fmOx_low=360;
		fmOx_high_time=0;fmOx_low_time=0;
		int i=0;
		mmsd_avg=1000;

		if(debug)
			try {
				stdin.readLine();
			} catch (IOException e1) {
				// 
				e1.printStackTrace();
			}


		/*for (int left_tries=0;left_tries<mN_linear_tries;left_tries++){
			fmN_fit1start=0;
			fmN_fit1end=(int)(((2.0/9.0)+(2.0/9.0)/(mN_linear_tries-1)*left_tries)*mN_samples_in_analysis);				
			//				mN_fit1end=(int)mN_samples_in_analysis/4;
			fmN_fit2start=fmN_fit1end+1;
			for (int right_tries=0;right_tries<mN_linear_tries;right_tries++){
				fmN_fit2end=(int)(((5.0/9.0)+(2.0/9.0)/(mN_linear_tries-1)*right_tries)*mN_samples_in_analysis);

				//				mN_fit2end=(int)(2*mN_samples_in_analysis/3);
				fmN_fit3start=fmN_fit2end+1;
				fmN_fit3end=mN_samples_in_analysis;
				System.out.println("fit1.s:"+fmN_fit1start+" fit1.e:"+fmN_fit1end+" fit2s:"+fmN_fit2start+" fit2.e:"+fmN_fit2end+" fit3s:"+fmN_fit3start+" fit3.e:"+fmN_fit3end);
				for ( i=0;i<mN_samples_in_analysis;i++) {
					mReduced_time[i]=mtime[i]-mT0;
					if(mOx[i]>fmOx_high) {
						fmOx_high=mOx[i];
						fmOx_high_time=mtime[i];
						//					System.out.println("new high:"+Ox_high);
					}
					else if(mOx[i]<fmOx_low) {
						fmOx_low=mOx[i];
						fmOx_low_time=mtime[i];
						//					System.out.println("new low:"+Ox_low);
					}
					if(fmOx_high-fmOx_low>340 && Math.abs(fmOx_high_time-fmOx_low_time)<mMin_zerocross_time )
						{
							zerocross=true;   //if more than 350 degrees in 0.5s then zero was crossed - do something about it...
							System.out.println("ZEROCROSS!!!");
						for (i=0;i<mN_samples_in_analysis;i++){
								if(mOx[i]<180) mOx[i]+=360;
						}
						}
					//to fix - add 360 to anything less than 180?
				}
				for ( i=fmN_fit1start;i<fmN_fit1end;i++) {
					myPolynome1.addPoint(mReduced_time[i],mOx[i]); //add point (t,mOx) to fit
				}
				for ( i=fmN_fit2start;i<fmN_fit2end;i++) {
					myPolynome2.addPoint(mReduced_time[i],mOx[i]); //add point (t,mOx) to fit
				}
				for ( i=fmN_fit3start;i<fmN_fit3end;i++) {
					myPolynome3.addPoint(mReduced_time[i],mOx[i]); //add point (t,mOx) to fit
				}

				myPoly1=myPolynome1.getBestFit();   	
				System.out.println(myPoly1);
				fm1A=myPoly1.get(0);
				fm1B=myPoly1.get(1);

				myPoly2=myPolynome2.getBestFit();   	
				System.out.println(myPoly2);
				fm2A=myPoly2.get(0);
				fm2B=myPoly2.get(1);
				fm2C=myPoly2.get(2);	 

				myPoly3=myPolynome3.getBestFit();   	
				System.out.println(myPoly3);
				fm3A=myPoly3.get(0);
				fm3B=myPoly3.get(1);

				coeff_str="m1A:"+((int)(fm1A*100))/100.0+" m1B:"+((int)(fm1B*1000))/1000.0;
				coeff_str=coeff_str+"m2A:"+((int)(fm2A*100))/100.0+" m2B:"+((int)(fm2B*1000))/1000.0+" m2C:"+fm2C;
				coeff_str=coeff_str+"m3A:"+((int)(fm3A*100))/100.0+" m3B:"+((int)(fm3B*1000))/1000.0;
				if(debug) System.out.println(coeff_str);
				int count=0;
				for ( i=fmN_fit1start;i<fmN_fit1end;i++) {
					fmOx_fit1[count]=(float)(fm1A+m1B*mReduced_time[i]);  //computed values for curve
					fmOx_fit1_time[count]=(float)(fm1A+m1B*mReduced_time[i]);
					count++;
				}
				for ( i=fmN_fit2start;i<fmN_fit2end;i++) {
					fmOx_fit2[i]=(float)(fm2A+fm2B*mReduced_time[i]+fm2C*mReduced_time[i]*mReduced_time[i]);  //computed values for curve
				}
				for ( i=fmN_fit3start;i<fmN_fit3end;i++) {
					fmOx_fit3[i]=(float)(fm3A+fm3B*mReduced_time[i]);  //computed values for curve
					//					mOx_fit3_time[count]=(float)(m1A+m1B*mReduced_time[i]);
				}

				fmmsd1=meansqdiff(mOx,fmOx_fit1,fmN_fit1start,fmN_fit1end);
				fmmsd2=meansqdiff(mOx,fmOx_fit2,fmN_fit2start,fmN_fit2end);
				fmmsd3=meansqdiff(mOx,fmOx_fit3,fmN_fit3start,fmN_fit3end);
				mmsd_avg=(float)((fmmsd1+fmmsd2+fmmsd3)/3.0);
				if(mmsd_avg<best_msd) {
					best_msd=mmsd_avg;
					m1A=fm1A;m1B=fm1B;
					m2A=fm2A;m2B=fm2B;m2C=fm2C;
					m3A=fm3A;m3B=fm3B;
					mN_fit1start=fmN_fit1start;mN_fit1end=fmN_fit1end;
					mN_fit2start=fmN_fit2start;mN_fit2end=fmN_fit2end;
					mN_fit3start=fmN_fit3start;mN_fit3end=fmN_fit3end;
					mOx_high=fmOx_high;mOx_high_time=fmOx_high_time;
					mOx_low=fmOx_low;mOx_low_time=fmOx_low_time;
					mmsd1=fmmsd1;mmsd2=fmmsd2;mmsd3=fmmsd3;
					System.arraycopy(fmOx_fit1, 0,mOx_fit1, 0, mN_samples_in_analysis);
					System.arraycopy(fmOx_fit2, 0,mOx_fit2, 0, mN_samples_in_analysis);
					System.arraycopy(fmOx_fit3, 0,mOx_fit3, 0, mN_samples_in_analysis);
	//				System.arraycopy(mtime, mN_samples_between_analyses,mtime, 0, mN_samples_in_analysis-mN_samples_between_analyses);
					System.out.println("msd current:"+mmsd_avg+" lowest:"+best_msd);
				}
			}
		}
		 */
	}


	//	public  float printresults() {
	////ppscore1=0;
	//		TextView tvStatus= (TextView)findViewById(R.id.tStatus2);
	//		String s="DDDD";
	//		tvStatus.setText(s);
	//
	////        TextView tvAz= (TextView)findViewById(R.id.a_z);
	////           tvAx.setText(Float.toString((float)(Math.round(mLastX*100.0)/100.0)));
	//
	//		
	////		 tvStatus= (TextView)findViewById(R.id.tP1);
	//		tvStatus= (TextView)findViewById(R.id.tStatus2);
	//		s="EEE";
	//		tvStatus.setText(s);
	//
	//		
	//		tvStatus= (TextView)findViewById(R.id.tStatus2);
	//	    tvStatus.setText(Float.toString((float)(Math.round(ppscore*100.0)/100.0)));
	//		//s=String.format("%0.2f",1.35);
	//		//tvStatus.setText(s);
	//
	//	    tvStatus= (TextView)findViewById(R.id.tP2);
	//		s=Float.toString((float)(Math.round(ppscore2*100.0)/100.0));
	//		s=s+" "+Float.toString((Math.round(m2C*1000000.0)))+"e6";
	//	    tvStatus.setText(s);
	//
	//	    tvStatus= (TextView)findViewById(R.id.tP3);
	//		s=Float.toString((float)(Math.round(ppscore3*100.0)/100.0));
	//		s=s+" "+Float.toString((float)(Math.round(mdOx*10.0)/10.0));
	////		s=String.format("%0.2f %.1f",ppscore3,mdOx);
	//	    tvStatus.setText(s);
	//
	//        tvStatus= (TextView)findViewById(R.id.tP4);
	//		s=Float.toString((float)(Math.round(ppscore4*100.0)/100.0));
	//		s=s+" "+Float.toString((float)(Math.round(mmsd_avg*10.0)/10.0));
	////		s=String.format("%0.2f %.1f",ppscore4,mmsd_avg);
	//	    tvStatus.setText(s);
	//	    
	//        tvStatus= (TextView)findViewById(R.id.tP5);
	//		s=String.format("%0.2f %.1f",ppscore5,mdt);
	//	    tvStatus.setText(s);
	//	    
	//        tvStatus= (TextView)findViewById(R.id.tP6);
	//		s=String.format("%0.2f %.1f",ppscore6,mGa_std);
	//	    tvStatus.setText(s);
	//
	//	    tvStatus= (TextView)findViewById(R.id.tP7);
	//		s=String.format("%0.2f %.1f",ppscore7,mdOx_start_vs_end);
	//	    tvStatus.setText(s);
	//	    
	//        tvStatus= (TextView)findViewById(R.id.tP8);
	//		s=String.format("%0.2f %.1f %.1f",ppscore8,m1B,m3B);
	//	    tvStatus.setText(s);
	//	    
	////	    tvStatus= (TextView)findViewById(R.id.tStatus8);
	////	    s=String.format("msd:%.1f:%.2f",mmsd_avg,ppscore5);
	////		tvStatus.setText(s);
	//		
	//	    tvStatus= (TextView)findViewById(R.id.tpp);
	//	    s=String.format("%0.2f %0.2f",ppscore,ppscore_running_avg);
	//		tvStatus.setText(s);
	//		
	//        tvStatus= (TextView)findViewById(R.id.tpp_hi);
	//		s=String.format("%0.2f",highest_ppscore);
	//	    tvStatus.setText(s);
	//	    
	//	       tvStatus= (TextView)findViewById(R.id.tmN);
	//			s=String.format("%d",mN);
	//		    tvStatus.setText(s);
	//		    
	//	    return((float)1.0);
	////		demo.updateplot("Ox curve",time,Ox,Ox_curve,"msd="+((int)(msd*100))/100.0+" "+coeffs+" "+ds.strn);
	////		demo.pack();
	////		RefineryUtilities.centerFrameOnScreen(demo);
	////		demo.setVisible(true);			        
	//		
	//	}

	public  float calculate_driving_likelihood(datastructure sensor_data_vector) {

//		vs[0]=timeStamp;
//		vs[1]=mLastX;vs[2]=mLastY;vs[3]=mLastZ;
//		vs[4]=mLastA;vs[5]=mLastP;vs[6]=mLastR;
//		vs[7]=mLastGx;vs[8]=mLastGy;vs[9]=mLastGz;
//		vs[10]=mLinearAccelerationVectorWC[0];vs[11]=mLinearAccelerationVectorWC[1];vs[12]=mLinearAccelerationVectorWC[2];

//		mN_samples_taken 
//		mN_samples_in_driving_analysis 

//		//magnitude of std(Aa)
//		//value of quadratic curvature - gaussian 
///			//times sigmoid
//		//max change in Ox
//		//goodness of fit of parabola
//		//goodness of fit of lines
//		//time interval bet. highest and lowest point
//		//if(mdt<mmin_parking_time/2)ppscore5=(float)0;
//		//gyro std
//		//diff bet left and right line heights
//		// USE START VS END oX AVG INSTEAD OF M1A-M3A
//		//how flat are initial and final segments
//		//ppscore1,6 taken out
		
		mN_samples_taken++;
		
		//find average linear accelerations - this is an offset which 'should be' zero
		float ax1=calc_datastructure_avg(mSensor_history,mN_samples_in_driving_analysis,10);
//		System.out.println("ax1 "+ax1);
		float ax2=calc_datastructure_avg(mSensor_history,mN_samples_in_driving_analysis,11);
//		System.out.println("ax1 "+ax2);
		float ax3=calc_datastructure_avg(mSensor_history,mN_samples_in_driving_analysis,12);
//		System.out.println("ax1 "+ax3);
		sensor_data_vector.vals[13]=sensor_data_vector.vals[10]-ax1;
		sensor_data_vector.vals[14]=sensor_data_vector.vals[11]-ax2;
		sensor_data_vector.vals[15]=sensor_data_vector.vals[12]-ax3;
		mSensor_history[mN_samples_in_driving_analysis-1].mycopy(sensor_data_vector);

		
		//		System.out.print(mN_samples_in_driving_analysis-1);mSensor_history[mN_samples_in_driving_analysis-1].printer();
//		System.out.print(mN_samples_in_driving_analysis-2);mSensor_history[mN_samples_in_driving_analysis-2].printer();
//		System.out.print(mN_samples_in_driving_analysis-3);mSensor_history[mN_samples_in_driving_analysis-3].printer();
		for(i=0;i<mN_samples_in_driving_analysis-1;i++)
		{
			mSensor_history[i].mycopy(mSensor_history[i+1]);
		}

		if (mN_samples_taken>mN_samples_in_driving_analysis) mEnough_driving_samples=true;
		if (!mEnough_driving_samples)  return (0);
			
		
		//calculate velocity and printout - velocity from accels. without offset
		float[] mlinaccvector={sensor_data_vector.vals[13],sensor_data_vector.vals[14],sensor_data_vector.vals[15]};
		V2=integrate(mlinaccvector,V2,beta);
		V=integrate(mlinaccvector,V,1);
		mVhCurrent=(float)Math.sqrt(V[0]*V[0]+V[1]*V[1]);
		mVh2Current=(float)Math.sqrt(V2[0]*V2[0]+V2[1]*V2[1]);
		if(mVhCurrent>Vhmax) Vhmax=mVhCurrent;
		if(mVh2Current>Vh2max) Vh2max=mVh2Current;

		// calculate Vz avg and std
		mVz1=mVz1*alpha1+V2[2]*(1-alpha1);	
		mVzStd=0;
		mVzAvg=0;
		mVz[mVzCount++]=V2[2];
		if (mVzCount>mN_samples_in_2s_analysis-1) mVzCount=0;

		for (int i=0;i<mN_samples_in_2s_analysis;i++)
		{
			mVzAvg+=mVz[i];
		}
		mVzAvg=mVzAvg/mN_samples_in_2s_analysis;
		for (int i=0;i<mN_samples_in_2s_analysis;i++)
		{
			mVzStd+=(mVz[i]-mVzAvg)*(mVz[i]-mVzAvg);
		}
		mVzStd=(float) Math.sqrt((double)mVzStd/mN_samples_in_2s_analysis);

		// calculate Oy avg and std for 20s of samples
		mSampleCount20s++; if(mSampleCount20s>mN_samples_in_20s_analysis-1) mSampleCount20s=0;
		mOy_20s[mSampleCount20s]=sensor_data_vector.vals[5];
		mOy_std20s=0;
		mOy_avg20s=0;
		for (int i=0;i<mN_samples_in_20s_analysis;i++)
		{
			mOy_avg20s+=mOy_20s[i];
		}
		mOy_avg20s=mOy_avg20s/mN_samples_in_20s_analysis;
		for (int i=0;i<mN_samples_in_20s_analysis;i++)
		{
			mOy_std20s+=(mOy_20s[i]-mOy_avg20s)*(mOy_20s[i]-mOy_avg20s);
		}
		mOy_std20s=(float) Math.sqrt((double)mOy_std20s/mN_samples_in_20s_analysis);

		// calculate Oy avg and std for 2s of samples
		mSampleCount2s++; if(mSampleCount2s>mN_samples_in_2s_analysis-1) mSampleCount2s=0;
		mOy_2s[mSampleCount2s]=sensor_data_vector.vals[5];
		mOy_std2s=0;
		mOy_avg2s=0;
		for (int i=0;i<mN_samples_in_2s_analysis;i++)
		{
			mOy_avg2s+=mOy_2s[i];
		}
		mOy_avg2s=mOy_avg2s/mN_samples_in_2s_analysis;
		for (int i=0;i<mN_samples_in_2s_analysis;i++)
		{
			mOy_std2s+=(mOy_2s[i]-mOy_avg2s)*(mOy_2s[i]-mOy_avg2s);
		}
		mOy_std2s=(float) Math.sqrt((double)mOy_std2s/mN_samples_in_2s_analysis);

		//are we walking??
		if (//mAvgGabs>mGyro_Walking_Threshold && 
				mVzStd>mVzStdMinWalkingThreshold &&
				mOy_std2s>mOyStd2sMinWalkingThreshold &&
				mOy_std20s>mOyStd20sMinWalkingThreshold) 
		{
			mWalking=true;
			playWalk();
			mTimeofLastWalkingDetection=timeStamp;
		}
		else mWalking=false;

		//are we driving??
		if (!mWalking&&
				mVh2Current>mdriving_velocity_threshold &&
				(timeStamp-mTimeofLastWalkingDetection)>mtime_after_walking_to_filter_driving*1000 && 
				mVzStd<mVzStdMaxDrivingThreshold && 
				Math.abs(mVzAvg)<mVzAvgMaxDrivingThreshold &&
				mOy_std20s<mOyStd20sMaxDrivingThreshold &&
				mOy_std2s<mOyStd2sMaxDrivingThreshold)
		{
			mDriving=true;
			playHonk();
		}
		else mDriving=false;

		
		//print results
		tvOx= (TextView)findViewById(R.id.drv_wlk);
		if(mWalking)   tvOx.setText("Walking");
		else if(mDriving)   tvOx.setText("Driving");
		else  tvOx.setText("neither");
		
		tvOx= (TextView)findViewById(R.id.v_x);
		tvOy= (TextView)findViewById(R.id.v_y);
		tvOz= (TextView)findViewById(R.id.v_z);
		tvOzz= (TextView)findViewById(R.id.v_h2);  //put linaccvector for testing 
		tvOx.setText(Float.toString((float)(Math.round(V[0]*100.0)/100.0)));
		tvOy.setText(Float.toString((float)(Math.round(V[1]*100.0)/100.0)));
		tvOz.setText(Float.toString((float)(Math.round(V[2]*100.0)/100.0)));
		tvOzz.setText(Float.toString((float)(Math.round(mVh2Current*100.0)/100.0)));
		if(mVh2Current>mdriving_velocity_threshold) tvOzz.setTextColor(getResources().getColor(R.color.green));
		else tvOzz.setTextColor(getResources().getColor(R.color.red));
		
		tvOx= (TextView)findViewById(R.id.v_x2);
		tvOy= (TextView)findViewById(R.id.v_y2);
		tvOz= (TextView)findViewById(R.id.v_z2);
		tvOx.setText(Float.toString((float)(Math.round(V2[0]*100.0)/100.0)));
		tvOy.setText(Float.toString((float)(Math.round(V2[1]*100.0)/100.0)));
		tvOz.setText(Float.toString((float)(Math.round(V2[2]*100.0)/100.0)));

		tvOx= (TextView)findViewById(R.id.v_z_avg);
		tvOy= (TextView)findViewById(R.id.v_z_std);
		tvOx.setText(Float.toString((float)(Math.round(mVzAvg*100.0)/100.0)));
		if(Math.abs(mVzAvg)<mVzAvgMaxDrivingThreshold) tvOx.setTextColor(getResources().getColor(R.color.green));
		else tvOx.setTextColor(getResources().getColor(R.color.red));

		
		tvOy.setText(Float.toString((float)(Math.round(mVzStd*100.0)/100.0)));
		if(mVzStd>mVzStdMinWalkingThreshold) tvOy.setTextColor(getResources().getColor(R.color.purple));
		else if (mVzStd<mVzStdMaxDrivingThreshold) tvOy.setTextColor(getResources().getColor(R.color.green));
		else tvOy.setTextColor(getResources().getColor(R.color.red));

		 
		//o_y_avg
		
		tvOx= (TextView)findViewById(R.id.o_y_avg);
		tvOy= (TextView)findViewById(R.id.o_y_std20);
		tvOx.setText(Float.toString((float)(Math.round(mOy_avg20s*100.0)/100.0)));
		tvOy.setText(Float.toString((float)(Math.round(mOy_std20s*100.0)/100.0)));
		if(mOy_std20s>mOyStd20sMinWalkingThreshold)tvOy.setTextColor(getResources().getColor(R.color.purple));
		else if(mOy_std20s<mOyStd20sMaxDrivingThreshold)tvOy.setTextColor(getResources().getColor(R.color.green));
		else tvOy.setTextColor(getResources().getColor(R.color.red));

		tvOy= (TextView)findViewById(R.id.o_y_std2);
		tvOy.setText(Float.toString((float)(Math.round(mOy_std2s*100.0)/100.0)));
		if(mOy_std2s>mOyStd2sMinWalkingThreshold)tvOy.setTextColor(getResources().getColor(R.color.purple));
		else if(mOy_std2s<mOyStd2sMaxDrivingThreshold)tvOy.setTextColor(getResources().getColor(R.color.green));
		else tvOy.setTextColor(getResources().getColor(R.color.red));

		
		tvOx= (TextView)findViewById(R.id.a_x_offset);
		tvOy= (TextView)findViewById(R.id.a_y_offset);
		tvOz= (TextView)findViewById(R.id.a_z_offset);
		tvOx.setText(Float.toString((float)(Math.round(ax1*1000.0)/1000.0)));
		tvOy.setText(Float.toString((float)(Math.round(ax2*1000.0)/1000.0)));
		tvOz.setText(Float.toString((float)(Math.round(ax3*1000.0)/1000.0)));
	
		tvOx= (TextView)findViewById(R.id.a_x_no_offset);
		tvOy= (TextView)findViewById(R.id.a_y_no_offset);
		tvOz= (TextView)findViewById(R.id.a_z_no_offset);
		tvOx.setText(Float.toString((float)(Math.round(sensor_data_vector.vals[13]*1000.0)/1000.0)));
		tvOy.setText(Float.toString((float)(Math.round(sensor_data_vector.vals[14]*1000.0)/1000.0)));
		tvOz.setText(Float.toString((float)(Math.round(sensor_data_vector.vals[15]*1000.0)/1000.0)));

		mtime[mN]=sensor_data_vector.vals[0]; //mtime   (t,Ax,Ay,Az,mOx,Oy,Oz,Gx,Gy,Gz)
		mOx[mN]=sensor_data_vector.vals[4]; //mOx
//		mAa[mN]=(float)Math.sqrt(sensor_data_vector.vals[1]*sensor_data_vector.vals[1]+sensor_data_vector.vals[2]*sensor_data_vector.vals[2]+sensor_data_vector.vals[3]*sensor_data_vector.vals[3]); //accel magnitude
//		mGa[mN]=(float)Math.sqrt(sensor_data_vector.vals[7]*sensor_data_vector.vals[7]+sensor_data_vector.vals[8]*sensor_data_vector.vals[8]+sensor_data_vector.vals[9]*sensor_data_vector.vals[9]); //gyro magnitude 

		TextView tvStatus= (TextView)findViewById(R.id.tmN);
		String s=String.format("%d",mN);
		tvStatus.setText(s);


		return(ppscore);
	}






	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}


	private float[] integrate(float[] A,float[] V,float beta) {
		float [] Vp=new float[3];
		for (int i=0;i<3;i++)
		{
			Vp[i]=V[i]*beta+A[i]/mSampling_rate;	
		}
		return Vp;
	}

	public void playHonk(){
		try{
			MediaPlayer mp = MediaPlayer.create(getBaseContext(), R.raw.honk);
			if (mp != null) { 
				mp.start(); 
			}
			mp.setOnCompletionListener(mCompletionListener);
		} catch (Exception e){
			//Log.d("error",e.getMessage());
		}
	}

	public void playWalk(){
		try{
			MediaPlayer mp = MediaPlayer.create(getBaseContext(), R.raw.walk);
			if (mp != null) { 
				mp.start(); 
			}
			mp.setOnCompletionListener(mCompletionListener);
		} catch (Exception e){
			//Log.d("error",e.getMessage());
		}
	}


	private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			mp.release();
		}
	};


	private float[] calc_msd(float[] vector,int stdlength){
		float[]ans=new float [2];
		float mean=0;
		float stdev=0;
		if (stdlength>vector.length) {ans[0]=-1;ans[1]=-1;return ans;}
		for (int i=vector.length-stdlength;i<vector.length;i++) 
		{
			mean+=vector[i];
		}
		mean=mean/stdlength;
		for (int i=vector.length-stdlength;i<vector.length;i++)
		{
			stdev+=(vector[i]-mean)*(vector[i]-mean);
		}
		stdev=(float) Math.sqrt((double)stdev/stdlength);
		
		ans[0]=mean;ans[1]=stdev;
		return ans;
	}

	private float[] calc_msd(float[] vector,int stdlength, int startindex){
		float[]ans=new float [2];
		float mean=0;
		float stdev=0;
		if (stdlength+startindex>vector.length) {
			ans[0]=-1;ans[1]=-1;return ans;
		}
		for (int i=startindex;i<startindex+stdlength;i++) 
		{
			mean+=vector[i];
		}
		mean=mean/stdlength;
		for (int i=startindex;i<startindex+stdlength;i++)
		{
			stdev+=(vector[i]-mean)*(vector[i]-mean);
		}
		stdev=(float) Math.sqrt((double)stdev/stdlength);
		
		ans[0]=mean;ans[1]=stdev;
		return ans;
	}

	private float calc_avg(float[] vector,int stdlength){
		float mean=0;
		if (stdlength>vector.length) {
			mean=-1;return mean;
		}
		for (int i=vector.length-stdlength;i<vector.length;i++) 
		{
			mean+=vector[i];
		}
		mean=mean/stdlength;
		return mean;
	}
	
	private float calc_avg(float[] vector,int stdlength, int startindex){
		float mean=0;
		if (stdlength+startindex>vector.length) {
			mean=-1;return mean;
		}
		for (int i=startindex;i<startindex+stdlength;i++) 
		{
			mean+=vector[i];
		}
		mean=mean/stdlength;
		return mean;
	}

	private float calc_datastructure_avg(datastructure[] datastruct,int stdlength, int field){
		float mean=0;
		if (stdlength>datastruct.length) {
			mean=-1;return mean;
		}
		for (int i=datastruct.length-stdlength;i<datastruct.length;i++) 
		{
			mean+=datastruct[i].vals[field];
		}
		mean=mean/stdlength;
		return mean;
	}
	
	private float calc_datastructure_avg(datastructure[] datastruct,int stdlength, int startindex,int field){
		float mean=0;
		if (stdlength+startindex>datastruct.length) {
			mean=-1;return mean;
		}
		for (int i=startindex;i<startindex+stdlength;i++) 
		{
			mean+=datastruct[i].vals[field];
		}
		mean=mean/stdlength;
		return mean;
	}
	
//	 native float Cfunction(datastructure datastruct);
//	private  native float print(datastructure datastruct);
//     private native void print();
	 
    public native String  stringFromJNI();	
    public native float  floatFromJNI();	
    public native int  intFromJNI();    
    public native int  intFromJNIwithInput(int input);    
    public native int  intFromJNIwithFloatInput(float finput);
    public native int intFromJNIwithDoubleInput(double finput);
 //   static {
 //   	 System.loadLibrary("HelloWorld");
  //  	}
    
}




