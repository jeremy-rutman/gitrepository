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
#include <jni.h>

/* This is a trivial JNI example where we use a native method
 * to return a new VM String. See the corresponding Java source
 * file located at:
 *
 *   apps/samples/hello-jni/project/src/com/example/hellojni/HelloJni.java
 */
jstring
Java_com_example_recorder5_MainActivity_stringFromJNI( JNIEnv* env,
                                                  jobject thiz )
{
    return (*env)->NewStringUTF(env, "JRRRulez");
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
Java_com_example_recorder5_MainActivity_intFromJNIwithDoubleInputs( JNIEnv* env,
                                                  jobject thiz, double Ax,double Ay,double Az,double Ox,double Oy,double Oz, )
{
	int answer=(int)((float)Ax)*2;
//    return (*env)->(int)answer;
	return answer;
}
//jboolean Java_com_example_nativeaudio_NativeAudio_createUriAudioPlayer(JNIEnv* env, jclass clazz,
//jstring uri)
