/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_monitorend_WVSSView */

#ifndef _Included_com_monitorend_WVSSView
#define _Included_com_monitorend_WVSSView
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_monitorend_WVSSView
 * Method:    InitDecoder
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_monitorend_WVSSView_InitDecoder
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     com_monitorend_WVSSView
 * Method:    UninitDecoder
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_monitorend_WVSSView_UninitDecoder
  (JNIEnv *, jobject);

/*
 * Class:     com_monitorend_WVSSView
 * Method:    DecodeNal
 * Signature: ([BI[B)I
 */
JNIEXPORT jint JNICALL Java_com_monitorend_WVSSView_DecodeNal
  (JNIEnv *, jobject, jbyteArray, jint, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif