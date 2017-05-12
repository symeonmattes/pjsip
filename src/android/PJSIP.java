package gr.navarino.cordova.plugin;


import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Iterator;
import java.util.Set;
import java.lang.Thread;

import android.os.Bundle;
import android.net.Uri;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.app.PendingIntent;

import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import static android.telephony.PhoneStateListener.*;

import android.app.Activity;

import gr.navarino.cordova.plugin.scAudioManager;

import android.util.Log;


import gr.navarino.cordova.plugin.PjsipActions;
import org.pjsip.pjsua2.*;


public class PJSIP extends CordovaPlugin {


  private boolean mRingbackToneEnabled = true;
  private boolean mRingtoneEnabled = true;
  private ToneGenerator mRingbackTone;
  private static final String TAG = "PjSip";



  //private SipManager mSipManager = null;
  //private SipProfile mSipProfile = null;
  //private SipAudioCall call = null;

  private CordovaWebView appView = null;

  public static TelephonyManager telephonyManager = null;

  private static PjsipActions  actions = null;

  private Intent incomingCallIntent = null;
  private PendingIntent pendingCallIntent = null;
  private Ringtone mRingtone = null;






  //public SIPReceiver callReceiver = null;

  static {
    try {
      System.loadLibrary("pjsua2");

    } catch (UnsatisfiedLinkError e) {
      Log.e(TAG,"UnsatisfiedLinkError: " + e.getMessage());
      Log.e(TAG,"This could be safely ignored if you " + "don't need video.");

    }

  }
  public PJSIP() {

    Log.d("PJSIP", "constructor");



  }

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {

    Log.d("PJSIP", "initialize");

    super.initialize(cordova, webView);
    appView = webView;

    actions = PjsipActions.getInstance();
    actions.initialise(cordova, webView);

    Intent intent = cordova.getActivity().getIntent();
    dumpIntent(intent);

    if (intent.getAction().equals("gr.navarino.cordova.plugin.PJSIP.INCOMING_CALL")) {
      appView.loadUrl("cordova.fireWindowEvent('incomingCall', {})");

    }

  }

  private PhoneStateListener phoneStateListener = new PhoneStateListener() {
    @Override
    public void onCallStateChanged (int state, String incomingNumber)
    {

      switch (state) {
        case TelephonyManager.CALL_STATE_IDLE:

          break;
        case TelephonyManager.CALL_STATE_RINGING:

          break;
        case TelephonyManager.CALL_STATE_OFFHOOK:

          break;
      }
    }
  };




  private void isSupported(final CallbackContext callbackContext) {


  }


  private void isConnected(final CallbackContext callbackContext) {


  }

  private void showCallActivity(){

    Intent intent = new Intent();

    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    //startActivity(intent);


  }



  public static void dumpIntent(Intent i){

    Log.d(TAG,"dumpIntent - Intent:"+i);

  }

  @Override
  public void onNewIntent(final Intent intent) {

    Log.d(TAG,"onNewIntent - Intent:"+intent);
  }

  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

    Log.d(TAG,"Execute:"+action);


    actions = PjsipActions.getInstance();
    if (action.equals("connect")) {      
      final String user = args.getString(0);
      final String pass = args.getString(1);
      final String domain = args.getString(2);
      final String proxy = args.getString(3);

      actions.connect(user,pass,domain,proxy, callbackContext);

    }if (action.equals("disconnect")) {

      actions.disconnect(callbackContext);

    }

    else if (action.equals("issupported")) {
      //this.isSupported(callbackContext);
      return true;
    }else if (action.equals("declinecall")){

      actions.declineCall(callbackContext);

    } else if (action.equals("endcall")){

        actions.endCall(callbackContext);

    } else if (action.equals("makecall")) {
      final String number = args.getString(0);

      actions.makeCall(number,callbackContext);

    } else if (action.equals("acceptcall")) {

      actions.acceptCall(callbackContext);

    } else if (action.equals("activatespeaker")){

      Boolean isActive = Boolean.valueOf(args.getString(0));

      actions.setSpeakerMode(isActive);
      callbackContext.success(); // Thread-safe.


    }else if (action.equals("mutemicrophone")){
      Boolean isActive = Boolean.valueOf(args.getString(0));

      actions.muteMicrophone(isActive);
      callbackContext.success(); // Thread-safe.

    }else if (action.equals("holdcall")){
      Boolean isActive = Boolean.valueOf(args.getString(0));

      actions.holdCall(isActive,callbackContext);
      callbackContext.success(); // Thread-safe.
    }else if (action.equals("dtmfcall")){
      String num = args.getString(0);

      actions.sendDTMF(num,callbackContext);
      callbackContext.success(); // Thread-safe.
    }


    return true;

  }
}
