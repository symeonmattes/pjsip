/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class MediaTransportInfo {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected MediaTransportInfo(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(MediaTransportInfo obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        pjsua2JNI.delete_MediaTransportInfo(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setSrcRtpName(String value) {
    pjsua2JNI.MediaTransportInfo_srcRtpName_set(swigCPtr, this, value);
  }

  public String getSrcRtpName() {
    return pjsua2JNI.MediaTransportInfo_srcRtpName_get(swigCPtr, this);
  }

  public void setSrcRtcpName(String value) {
    pjsua2JNI.MediaTransportInfo_srcRtcpName_set(swigCPtr, this, value);
  }

  public String getSrcRtcpName() {
    return pjsua2JNI.MediaTransportInfo_srcRtcpName_get(swigCPtr, this);
  }

  public MediaTransportInfo() {
    this(pjsua2JNI.new_MediaTransportInfo(), true);
  }

}