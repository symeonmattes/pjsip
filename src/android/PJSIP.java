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

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.net.Uri;
import java.io.*;
import java.net.*;
import java.util.*;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.app.PendingIntent;

import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import static android.telephony.PhoneStateListener.*;

import android.app.Activity;

import gr.navarino.cordova.plugin.scAudioManager;

import android.util.Log;
import gr.navarino.cordova.plugin.Utils;

import gr.navarino.cordova.plugin.PjsipActions;
import gr.navarino.cordova.plugin.PjsipDiagnostic;
import org.pjsip.pjsua2.*;


public class PJSIP extends CordovaPlugin {


  private boolean mRingbackToneEnabled = true;
  private boolean mRingtoneEnabled = true;
  private ToneGenerator mRingbackTone;
  private static final String TAG = "PjSip";

  private static Utils utils = null;

  private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO= 55000;

  //private SipManager mSipManager = null;
  //private SipProfile mSipProfile = null;
  //private SipAudioCall call = null;

  private CordovaWebView appView = null;

  public static TelephonyManager telephonyManager = null;

  private static PjsipActions  actions = null;
  private static PjsipDiagnostic diagnostic = new PjsipDiagnostic();

  private Intent incomingCallIntent = null;
  private PendingIntent pendingCallIntent = null;
  private Ringtone mRingtone = null;


  String pjsipUser = "";
  String pjsipPass = "";
  String pjsipDomain = "";
  String pjsipProxy = "";


  //public SIPReceiver callReceiver = null;

  static {
    try {
      System.loadLibrary("pjsua2");

    } catch (UnsatisfiedLinkError e) {
      Log.e(TAG,"UnsatisfiedLinkError: " + e.getMessage());


    }

  }
  public PJSIP() {

    Log.d("PJSIP", "constructor");



  }

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {

    Log.d("PJSIP", "initialize");

    Utils utils = new Utils();
    utils.initialise(cordova,webView);

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
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {

    switch (requestCode) {
      case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {

        Log.d(TAG,"Permission request MY_PERMISSIONS_REQUEST_RECORD_AUDIO");
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

          Log.d(TAG,"Permission granted MY_PERMISSIONS_REQUEST_RECORD_AUDIO");


          if (actions.connect(pjsipUser,pjsipPass,pjsipDomain,pjsipProxy, null).equals("true")) {
            utils.executeJavascript("cordova.plugins.PJSIP.actions({action:'requestpermission',success:true})");
          }else{
            utils.executeJavascript("cordova.plugins.PJSIP.actions({action:'requestpermission',success:false})");
          }



        } else {
          utils.executeJavascript("cordova.plugins.PJSIP.actions({action:'requestpermission',success:false})");
          Log.d(TAG,"Permission denied MY_PERMISSIONS_REQUEST_RECORD_AUDIO");
          // permission denied, boo! Disable the
          // functionality that depends on this permission.
        }
        return;
      }

      // other 'case' lines to check for other
      // permissions this app might request
    }
  }

  public Boolean requestPermissions(){


    if (!permissionsActive()) {
      cordova.requestPermissions(this, MY_PERMISSIONS_REQUEST_RECORD_AUDIO, new String[]{Manifest.permission.RECORD_AUDIO});
      return false;
    }else
      return true;

  }

  public Boolean permissionsActive(){

    final Activity thisActivity =  cordova.getActivity();

    try{

      int recordAudio = ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.RECORD_AUDIO);

      Log.d(TAG,"The RECORD_AUDIO is:"+((recordAudio == PackageManager.PERMISSION_DENIED)?"not active":"active"));
      if (recordAudio == PackageManager.PERMISSION_DENIED) {
        return false;
      }else
        return true;

    }catch(Exception e){
      Log.d(TAG,"Error in requesting Permission:"+e.getMessage());
      return false;
    }

  }

  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

    Log.d(TAG,"Execute:"+action);

    if (!(action.equals("connect") || action.equals("issupported") || permissionsActive())){
      callbackContext.error("Not appropriate permissions.");
      return false;
    }

    actions = PjsipActions.getInstance();
    if (action.equals("connect")) {
      pjsipUser = args.getString(0);
      pjsipPass = args.getString(1);
      pjsipDomain = args.getString(2);
      pjsipProxy = args.getString(3);

      if (requestPermissions()){
        actions.connect(pjsipUser,pjsipPass,pjsipDomain,pjsipProxy, callbackContext);
      }else{
        callbackContext.error("Not appropriate permissions.");
      };


    }if (action.equals("disconnect")) {

      actions.disconnect(callbackContext);

    }

    else if (action.equals("issupported")) {
      //this.isSupported(callbackContext);
      callbackContext.success();
    }else if (action.equals("declinecall")){

      actions.declineCall(callbackContext);

    } else if (action.equals("endcall")){

      actions.endCall(callbackContext);

    } else if (action.equals("makecall")) {
      final String number = args.getString(0);

      actions.setSpeakerMode(false,null);
      actions.makeCall(number,callbackContext);

    } else if (action.equals("acceptcall")) {

      actions.acceptCall(callbackContext);

    } else if (action.equals("activatespeaker")){

      Boolean isActive = Boolean.valueOf(args.getString(0));

      actions.setSpeakerMode(isActive,callbackContext);


    }else if (action.equals("mutemicrophone")){
      Boolean isActive = Boolean.valueOf(args.getString(0));

      actions.muteMicrophone(isActive,callbackContext);

    }else if (action.equals("holdcall")){
      Boolean isActive = Boolean.valueOf(args.getString(0));

      actions.holdCall(isActive,callbackContext);


    }else if (action.equals("dtmfcall")){
      String num = args.getString(0);

      actions.sendDTMF(num,callbackContext);

    }else if (action.equals("checkarchitecture")){

      diagnostic.checkArchitecture(callbackContext);

    }else if (action.equals("checkclientip")){

      diagnostic.checkClientIP(callbackContext);

    }else if (action.equals("checkpbxconnectivity")){

        pjsipUser = args.getString(0);
        pjsipPass = args.getString(1);
        pjsipDomain = args.getString(2);
        pjsipProxy = args.getString(3);

        if (actions.isConnected()){
          callbackContext.success("ALREADY CONNECTED");
        }else{

          //it should be used in combination with javascript (stateRegRegistered, and stateRegTimeout) to check connectivity
          actions.connect(pjsipUser,pjsipPass,pjsipDomain,pjsipProxy, null);
          callbackContext.success("CHECKING");

        }
    }else if (action.equals("checkaudio")){
      diagnostic.checkAudio(callbackContext);
    }else if (action.equals("getwifissid")){

      String ssid = utils.getWifiSSID();
      if (ssid.contains("error:")){
          callbackContext.error(ssid);
      }else{
        callbackContext.success(ssid);
      }

    }else if (action.equals("checkpbxfirewall")){
      final String host= args.getString(0);
      final int port  = Integer.parseInt(args.getString(1));
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {

          diagnostic.checkPBXfirewall(host,port,callbackContext);

        }
      });

    }

    return true;

  }
}
