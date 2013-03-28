/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <stdio.h>
#include <jni.h>
#include "jeremy.h"
#include <android/log.h>

#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,"recorder5",__VA_ARGS__)

typedef struct tag1 {
	int time;
	double vals[6];
/*	double Ay;
	double Az;
	double Ox;
	double Oy;
	double Oz;
*/} Cdatastructure;

typedef struct tag2
{
	int mNfields;
	int mN_samples_in_driving_analysis;
	int mN_samples_in_2s_analysis;
	double mVzStdMinWalkingThreshold;
	double mOyStd2sMinWalkingThreshold;
	double mOyStd20sMinWalkingThreshold;
	double mdriving_velocity_threshold;
	double mVzStdMaxDrivingThreshold;
	double mVzAvgMaxDrivingThreshold;
	double mOyStd20sMaxDrivingThreshold;
	double mOyStd2sMaxDrivingThreshold;
	int mN_samples_taken_initial_value;
	int mN_samples_taken_current_value;
	int mN_samples_taken;
	Cdatastructure mSensor_history[200];  //mN_samples_in_driving_analysis - itay didnt want #defines and I dont want to have to malloc
	int mEnough_driving_samples;
	double V2[3];
	double V[3];
	double mVz[200];  //mN_samples_in_driving_analysis =
	int mSampleCount20s;
	int mSampleCount2s;
	double mOy_20s[200]; //mN_samples_in_driving_analysis
	double mOy_2s[20]; //mN_samples_in_2s_analysis
	int mWalking;
	int mDriving;
	int mTimeofLastWalkingDetection;
	int mSamplingRate;
	double mBeta;

} jeremystruct;

static jeremystruct jstructure;
static int jmN_samples_taken=0;

jstring
Java_com_example_recorder5_MainActivity_stringFromJNI( JNIEnv* env,
                                                  jobject thiz )
{
    return (*env)->NewStringUTF(env, "JRRR");
}

float
Java_com_example_recorder5_MainActivity_floatFromJNI( JNIEnv* env,
                                                  jobject thiz )
{
	float answer=(float)0.123f;
//    return (*env)->(float)answer;
	return answer;
}

int
Java_com_example_recorder5_MainActivity_intFromJNI( JNIEnv* env,
                                                  jobject thiz )
{
	int answer=321;
//    return (*env)->(int)answer;
	return answer;
}

int
Java_com_example_recorder5_MainActivity_intFromJNIwithIntInput( JNIEnv* env,
                                                  jobject thiz, int input )
{
	int answer=(int)input*2;
//    return (*env)->(int)answer;
	return answer;
}

int
Java_com_example_recorder5_MainActivity_intFromJNIwithFloatInput( JNIEnv* env,
                                                  jobject thiz, float input )
{
	int answer=(int)((float)input)*2;
//    return (*env)->(int)answer;
	return answer;
}

int
Java_com_example_recorder5_MainActivity_intFromJNIwithDoubleInput( JNIEnv* env,
                                                  jobject thiz, double input )
{
	int answer=(int)((float)input)*2;
//    return (*env)->(int)answer;
	return answer;
}


int
Java_com_example_recorder5_MainActivity_CalculateDrivingLikelihoodCCode( JNIEnv* env,
                                                  jobject thiz, double Ax,double Ay,double Az,double Ox,double Oy,double Oz,double R1,double R2,double R3,double R4,double R5,double R6,double R7,double R8,double R9)
                                                  //double A,double O[],double R[] )
// this function gets Accelerations Ax Ay Az in phone frame, and rotation matrix R
{
	double sensor_data_vector[15];
	double ax_offset,ay_offset,az_offset; //these are the long-timescale biases of the acceleration vector (assumed to be 'really' 0)
	int i;
	int j;
	double mlinaccvector[3];
	double V[3];
	double V2[3];
	double R11,R12,R13,R21,R22,R23,R31,R32,R33;  //rotation matrix elements
//	double Ax,Ay,Az;
//	double Ox,Oy,Oz;
	double Awc[3];

//	Ox=O[0];Oy=O[1];Oz=O[2];
//	CmultiplyVectorMatrix(R, A,Awc);
//	Ax=Awc[0];Ay=Awc[1];Az=Awc[2];

	LOGD("c:ax %.3f ay %.3f az %.3f Ox %.3f Oy %.3f Oz %.3f ",Ax,Ay,Az,Ox,Oy,Oz);
//	LOGD("c:R1 %.3f R2 %.3f R3 %.3f R4 %.3f R5 %.3f R6 %.3f R7 %.3f R8 %.3f R9 %.3f",R[0],R[1],R[2],R[3],R[4],R[5],R[6],R[7],R[8]);
	LOGD("c:R1 %.3f R2 %.3f R3 %.3f R4 %.3f R5 %.3f R6 %.3f R7 %.3f R8 %.3f R9 %.3f",R1,R2,R3,R4,R5,R6,R7,R8,R9);


	//set threshold and other values first time thru (Itay didn't want #defines)
//	mN_samples_taken_current_value=  IS THERE A WAY TO DO THIS (determine initial run) WITHOUT GLOBAL VARIABLE?
	if(jmN_samples_taken==0) {
			jstructure.mNfields=6; //fields in dataq record - Ax Ay Az Ox Oy Oz
			jstructure.mN_samples_in_driving_analysis=200;
			jstructure.mN_samples_in_2s_analysis=20;
			jstructure.mVzStdMinWalkingThreshold=0.08;
			jstructure.mOyStd2sMinWalkingThreshold=10.0;
			jstructure.mOyStd20sMinWalkingThreshold=10.0;
			jstructure.mdriving_velocity_threshold=0.5;
			jstructure.mVzStdMaxDrivingThreshold=0.2;
			jstructure.mVzAvgMaxDrivingThreshold=0.7;
			jstructure.mOyStd20sMaxDrivingThreshold=5.0;
			jstructure.mOyStd2sMaxDrivingThreshold=5.0;
			jstructure.mEnough_driving_samples=0;  //0 false, 1 true
			jstructure.mSamplingRate=10; //in samples/second
			jstructure.mBeta=0.95; //in samples/second
			jstructure.V[0]=0.0;jstructure.V[1]=0.0;jstructure.V[2]=0.0;
			jstructure.V2[0]=0.0;jstructure.V2[1]=0.0;jstructure.V2[2]=0.0;
	}
	jmN_samples_taken++;
	//	jstructure.mN_samples_taken++;
	if (jmN_samples_taken>32000) jmN_samples_taken=jstructure.mN_samples_in_driving_analysis+1;
	//find average linear accelerations - this is an offset which 'should be' zero

	ax_offset=Ccalc_datastructure_avg2(0,jstructure.mN_samples_in_driving_analysis,jstructure.mN_samples_in_driving_analysis);
	ay_offset=Ccalc_datastructure_avg2(1,jstructure.mN_samples_in_driving_analysis,jstructure.mN_samples_in_driving_analysis);
	az_offset=Ccalc_datastructure_avg2(2,jstructure.mN_samples_in_driving_analysis,jstructure.mN_samples_in_driving_analysis);
	LOGD("ax1 %.3f ax2 %.3f ax3 %.3f",ax_offset,ay_offset,az_offset);

	//		System.out.println("ax1 "+ax3);
//	sensor_data_vector[0]=Ax-ax_offset;
//	sensor_data_vector[1]=Ay-ay_offset;
//	sensor_data_vector[2]=Az-az_offset;
	LOGD("after correction: ax1 %.3f ax2 %.3f ax3 %.3f V2[0] %.3f",Ax-ax_offset,Ay-ax_offset,Az-ax_offset,jstructure.V2[0]);
	jstructure.mSensor_history[jstructure.mN_samples_in_driving_analysis-1].vals[0]=Ax-ax_offset;
	jstructure.mSensor_history[jstructure.mN_samples_in_driving_analysis-1].vals[1]=Ay-ay_offset;
	jstructure.mSensor_history[jstructure.mN_samples_in_driving_analysis-1].vals[2]=Az-az_offset;
	jstructure.mSensor_history[jstructure.mN_samples_in_driving_analysis-1].vals[3]=Ox;
	jstructure.mSensor_history[jstructure.mN_samples_in_driving_analysis-1].vals[4]=Oy;
	jstructure.mSensor_history[jstructure.mN_samples_in_driving_analysis-1].vals[5]=Oz;

//	LOGD("Ax %f",Ax);
	//		System.out.print(mN_samples_in_driving_analysis-1);mSensor_history[mN_samples_in_driving_analysis-1].printer();
//		System.out.print(mN_samples_in_driving_analysis-2);mSensor_history[mN_samples_in_driving_analysis-2].printer();
//		System.out.print(mN_samples_in_driving_analysis-3);mSensor_history[mN_samples_in_driving_analysis-3].printer();
	//make this circular buffer for faster performance

//shift buffer back
	for( i=0;i<jstructure.mN_samples_in_driving_analysis-1;i++)
	{
		for( j=0;j<jstructure.mNfields;j++)
		{
		jstructure.mSensor_history[i].vals[j]=jstructure.mSensor_history[i+1].vals[j];
		}
	}

	if (jmN_samples_taken>jstructure.mN_samples_in_driving_analysis) jstructure.mEnough_driving_samples=1;
	if (!jstructure.mEnough_driving_samples)  return (0);

// calculate rotation matrix from orientation angles
//actually dont need to since iphone gets real world acc. data wout gravity

	//calculate velocity and printout - velocity from accels. without offset
	mlinaccvector[0]=sensor_data_vector[0];
	mlinaccvector[1]=sensor_data_vector[1];
	mlinaccvector[2]=sensor_data_vector[2];
	LOGD("V2xbef %.3f V2ybef %.3f V2zbef %.3f",jstructure.V2[0],jstructure.V2[1],jstructure.V2[2]);
	Cintegrate(mlinaccvector,jstructure.V2,jstructure.mBeta);
	//Cintegrate(mlinaccvector,jstructure.V,1);
	LOGD("V2xaft %.3f V2yaft %.3f V2zaft %.3f",jstructure.V2[0],jstructure.V2[1],jstructure.V2[2]);

	/*	mVhCurrent=(float)Math.sqrt(V[0]*V[0]+V[1]*V[1]);
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

	return(mDriving);
}
*/
	return 666;
}

//float  Ccalc_datastructure_avg2( int field);
double  Ccalc_datastructure_avg2( int field,int stdlength,int totlength ){
		double mean=0.0;
//		int stdlength=10;
//		int totlength=10;
		int i=0;
		if (stdlength>totlength) {
			mean=-1.0;return mean;
		}
		for ( i=totlength-stdlength;i<totlength;i++)
		{
			mean+=jstructure.mSensor_history[i].vals[field];
		}
		mean=mean/stdlength;
		return (double)mean;
	}

void Cintegrate(double A[],double V[],double beta) {
		//float [] Vp=new float[3];
	int i;
		for ( i=0;i<3;i++)
		{
			LOGD("in integrate bef: V[%d] %.3f A[%d] %.3f",i,V[i],i,A[i]);
			V[i]=V[i]*beta+(*(A+i))/jstructure.mSamplingRate;
			LOGD("in integrate aft: V[%d] %.3f A[%d] %.3f",i,V[i],i,A[i]);
		}
//		LOGD("V[%d] %.3f A[%d] ",Ax-ax_offset,Ay-ax_offset,Az-ax_offset);

//		V[0]=-666.0; //test for call by ref or value
//		return V;
	}

void Cintegratepoint(double* A,double* V,double beta) {
		//float [] Vp=new float[3];
	int i;
		for ( i=0;i<3;i++)
		{
	//		LOGD("in integrate bef: V[%d] %.3f A[%d] %.3f",i,*(V+i),i,*(A+i));
			*(V+i)=(*(V+i))*beta+(*(A+i))/jstructure.mSamplingRate;
//			LOGD("in integrate aft: V[%d] %.3f A[%d] %.3f",i,*(V+i),i,*(A+i));
		}

	}

//3x3 matrix * 3vector
void CmultiplyVectorMatrix(double matrix[9], double vector[3], double result[3]) {
    // matrix-vector multiplication (y = A * x), assuming square matrix and

    //     double y[3];
         int i,j;

        for ( i = 0; i < 3; i++){
        	result[i]=0;
            for ( j = 0; j < 3; j++)
                result[i] += (matrix[i*3+j] * vector[j]);
        }
   //     return &y[0];
    }
