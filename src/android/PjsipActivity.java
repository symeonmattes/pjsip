package gr.navarino.cordova.plugin;

import org.apache.cordova.CallbackContext;
import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.AuthCredInfoVector;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.StringVector;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_role_e;
import org.pjsip.pjsua2.pjsip_status_code;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import gr.navarino.cordova.plugin.Utils;
import gr.navarino.cordova.plugin.MyApp;
import gr.navarino.cordova.plugin.MyAccount;
import gr.navarino.cordova.plugin.MyCall;
import gr.navarino.cordova.plugin.MyAppObserver;
import gr.navarino.cordova.plugin.MyBuddy;


import gr.navarino.cordova.plugin.scAudioManager;

/**
 * Created by infuser on 30/03/17.
 */

public class PjsipActivity extends Activity implements Handler.Callback,MyAppObserver {

    static private String TAG = "PjsipActivity";
    static private String outGoingCallNumber="";
    static private String inComingCallNumber="";

    public static MyApp app = null;
    public static MyAccount account = null;
    public static AccountConfig accCfg = null;
    public static MyCall currentCall = null;

    private static Map<String,String> userSettings = new HashMap<String,String>();

    private static Utils utils = null;

    private static String lastRegStatus = "";

    private final Handler handler = new Handler(this);


    public class MSG_TYPE
    {
        public final static int INCOMING_CALL = 1;
        public final static int CALL_STATE = 2;
        public final static int REG_STATE = 3;
        public final static int BUDDY_STATE = 4;
        public final static int CALL_MEDIA_STATE = 5;
    }

    public void PjSipActivity(){



    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }


    public void initialise(String absPath){

        if (app == null){

            app = new MyApp();
            Log.d("PJSIP","Logs:"+absPath);
            app.init(this, absPath);

            utils = new Utils();

        }

        if (app.accList.size() == 0) {
            accCfg = new AccountConfig();
            accCfg.setIdUri("sip:localhost");
            accCfg.getNatConfig().setIceEnabled(true);
            accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
            accCfg.getVideoConfig().setAutoShowIncoming(true);
            account = app.addAcc(accCfg);

//            this.connect("MyProfile","712","712","172.26.41.1",null);
//            this.connect("1","712","712","172.26.41.1",null);
        } else {
            account = app.accList.get(0);
            accCfg = account.cfg;
        }
    }



    public synchronized void disconnect(final CallbackContext callbackContext) {


        String acc_id 	 = "sip:localhost";
        String registrar = "";
        String proxy 	 = "";
        String username  = "";
        String password  = "";

        userSettings.put("user","");
        userSettings.put("password","");
        userSettings.put("systemIP","");
        userSettings.put("proxyIP","");

        Log.d(TAG, "Registration with the following settings: (acc_id,"+acc_id+"),(registrar,"+registrar+"),(proxy,"+proxy+"),(username,"+username+"),(password,"+password+")");

        app.checkThread();
        accCfg.setIdUri(acc_id);
        accCfg.getRegConfig().setRegistrarUri(registrar);
        AuthCredInfoVector creds = accCfg.getSipConfig().getAuthCreds();
        creds.clear();
        if (username.length() != 0) {
            creds.add(new AuthCredInfo("Digest", "*", username, 0, password));
        }
        StringVector proxies = accCfg.getSipConfig().getProxies();
        proxies.clear();
        if (proxy.length() != 0) {
            proxies.add(proxy);
        }

        /* Enable ICE */
        accCfg.getNatConfig().setIceEnabled(true);

        /* Finally */
        lastRegStatus = "";
        try {
            account.modify(accCfg);
            callbackContext.success("Successfully registered");
        } catch (Exception e) {

            callbackContext.error("Logs:"+e.toString());
            Log.e("PJSIP","Logs:"+e.toString());

        }

    }



    public synchronized void connect(final String user, final String pass, final String systemIP, final String proxyIP,final CallbackContext callbackContext) {

        String acc_id 	 = "sip:"+user+"@"+systemIP;
        String registrar = "sip:"+user+"@"+systemIP;
        String proxy 	 = proxyIP;
        String username  = user;
        String password  = pass;

        userSettings.put("user",user);
        userSettings.put("password",pass);
        userSettings.put("systemIP",systemIP);
        userSettings.put("proxyIP",proxyIP);

        Log.d(TAG, "Registration with the following settings: (acc_id,"+acc_id+"),(registrar,"+registrar+"),(proxy,"+proxy+"),(username,"+username+"),(password,"+password+")");

        app.checkThread();
        accCfg.setIdUri(acc_id);
        accCfg.getRegConfig().setRegistrarUri(registrar);
        AuthCredInfoVector creds = accCfg.getSipConfig().getAuthCreds();
        creds.clear();
        if (username.length() != 0) {
            creds.add(new AuthCredInfo("Digest", "*", username, 0, password));
        }
        StringVector proxies = accCfg.getSipConfig().getProxies();
        proxies.clear();
        if (proxy.length() != 0) {
            proxies.add(proxy);
        }

        /* Enable ICE */
        accCfg.getNatConfig().setIceEnabled(true);

        /* Finally */
        lastRegStatus = "";
        try {
            account.modify(accCfg);
            callbackContext.success("Successfully registered");
        } catch (Exception e) {

            callbackContext.error("Logs:"+e.toString());
            Log.e("PJSIP","Logs:"+e.toString());

        }

    }

    public void makeCall(final String number, final CallbackContext callbackContext) {

        if (currentCall != null ){
            Log.w(TAG,"There is already a call");
            return;
        }

        String systemIP=userSettings.get("systemIP");
        String buddy_uri = "sip:"+number+"@"+systemIP;

        outGoingCallNumber = number;

        Log.i(TAG,"A call will be made to:"+buddy_uri);

        app.checkThread();
        MyCall call = new MyCall(account, -1);
        CallOpParam prm = new CallOpParam(true);

        try {

            call.makeCall(buddy_uri, prm);

        } catch (Exception e) {
            call.delete();
            e.printStackTrace();
            return;
        }

        currentCall = call;

    }


    public void sendDTMF(final String num) {

        if (currentCall != null){
            app.checkThread();

            try {
                //DTMF string digits to be sent as described on RFC 2833 section 3.10.
                currentCall.dialDtmf(num);
            } catch (Exception e) {
                Log.d(TAG,e.toString());
            }


        }
    }



    public void holdCall(final Boolean isActive) {

        if (currentCall != null){
            app.checkThread();

            CallOpParam prm = new CallOpParam(true);
            if (isActive){
                try {
                    currentCall.setHold(prm);
                } catch (Exception e) {
                    Log.d(TAG,e.toString());
                }
            } else{
                prm.getOpt().setFlag(1);

                try {
                    currentCall.reinvite(prm);
                } catch (Exception e) {
                    Log.d(TAG,e.toString());
                }
            }
        }
    }


    public static synchronized void acceptCall(final CallbackContext callbackContext) {

        if (currentCall != null) {

            app.checkThread();
            Log.d(TAG,"=====Answer Call=========");
            CallOpParam prm = new CallOpParam();
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_OK);
            try {
                currentCall.answer(prm);
                utils.executeJavascript("cordova.plugins.PJSIP.callState({state:'established'})");
                callbackContext.success();
            } catch (Exception e) {
                Log.d(TAG,e.toString());
            }
        }
    }

    public static synchronized void declineCall(final CallbackContext callbackContext) {

        if (currentCall != null) {

            app.checkThread();
            Log.d(TAG,"=====Decline Call=========");
            CallOpParam prm = new CallOpParam();
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
            try {
                currentCall.answer(prm);
                currentCall = null;
                callbackContext.success();
            } catch (Exception e) {
                Log.d(TAG,e.toString());
            }
        }
    }

    public static synchronized void endCall(final CallbackContext callbackContext) {

        if (currentCall != null) {

            app.checkThread();
            CallOpParam prm = new CallOpParam();
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
            try {
                currentCall.hangup(prm);
                currentCall = null;
            } catch (Exception e) {
                Log.d(TAG,e.toString());
            }
        }

    }






    @Override
    public boolean handleMessage(Message m) {

        if (m.what == 0){
            app.deinit();
        }else if (m.what == MSG_TYPE.CALL_STATE) {
            Log.d(TAG,"Call State");
        }else if (m.what == MSG_TYPE.CALL_MEDIA_STATE){
            Log.d(TAG,"Call Media State");
        }else if (m.what == MSG_TYPE.BUDDY_STATE){
            Log.d(TAG,"BUDDY STATE");
        }else if (m.what == MSG_TYPE.REG_STATE){
            Log.d(TAG,"REG STATE");
        }else if (m.what == MSG_TYPE.INCOMING_CALL){
            Log.d(TAG,"INCOMING CALL");
        }else{
            Log.d(TAG,"Message is not handled.");
            return false;
        }


        return true;
    }

    /***********************
     ***  MyAppObserver ***
     *********************/
    public synchronized void notifyRegState(pjsip_status_code code, String reason, int expiration){

        Log.d(TAG,"========notifyRegState========"+"Code:"+code+", reason:"+reason+", expiration:"+expiration);

    };
    public synchronized void notifyIncomingCall(MyCall call){
        Log.d(TAG,"=====notifyIncomingCall=========");

        app.checkThread();

	    /* Incoming call */
        CallOpParam prm = new CallOpParam();

	    /* Only one call at anytime */
        if (currentCall != null) {
            // TODO: set status code
            call.delete();
            return;
        }





        scAudioManager scAudio = scAudioManager.getInstance();
        scAudio.startRingtone();

	    /* Answer with ringing */
//        prm.setStatusCode(pjsip_status_code.PJSIP_SC_ACCEPTED);
//        prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE); //decline call
//         prm.setStatusCode(pjsip_status_code.PJSIP_SC_OK); //answer call
        prm.setStatusCode(pjsip_status_code.PJSIP_SC_RINGING); //ringing mode

        try {

            inComingCallNumber = call.getInfo().getRemoteUri();
            call.answer(prm);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }


        currentCall = call;

    };
    public synchronized void notifyCallState(MyCall call){


        app.checkThread();

        CallInfo ci;
        try {
            ci = call.getInfo();
        } catch (Exception e) {
            ci = null;
        }

        String call_state = "";


        if (ci.getState().swigValue() < pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.swigValue()) {

            if (ci.getRole() == pjsip_role_e.PJSIP_ROLE_UAS) {
                Log.d(TAG,"======notifyCallState======Incoming call");
                utils.executeJavascript("cordova.plugins.PJSIP.callState({state:'incall',inComingCallNumber:'"+inComingCallNumber+"'})");

		/* Default button texts are already 'Accept' & 'Reject' */
            } else if (ci.getRole() == pjsip_role_e.PJSIP_ROLE_UAC){

                if  (ci.getState().swigValue() == pjsip_inv_state.PJSIP_INV_STATE_CALLING.swigValue()){
                    utils.executeJavascript("cordova.plugins.PJSIP.callState({state:'outcall',outGoingCallNumber:'"+outGoingCallNumber+"'})");
                    Log.d(TAG,"======notifyCallState======PJSIP_INV_STATE_CALLING");
                }else if  (ci.getState().swigValue() == pjsip_inv_state.PJSIP_INV_STATE_EARLY.swigValue()){
                    Log.d(TAG,"======notifyCallState======PJSIP_INV_STATE_EARLY");
                }else if  (ci.getState().swigValue() == pjsip_inv_state.PJSIP_INV_STATE_CONNECTING.swigValue()){
                    Log.d(TAG,"======notifyCallState======PJSIP_INV_STATE_CONNECTING");
                }


            }
        }
        else if (ci.getState().swigValue() >= pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.swigValue()) {

            call_state = ci.getStateText();
            if (ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                Log.d(TAG,"======notifyCallState======PJSIP_INV_STATE_CONFIRMED - state:"+call_state);

                utils.executeJavascript("cordova.plugins.PJSIP.callState({state:'established'})");
                scAudioManager scAudio = scAudioManager.getInstance();
                scAudio.stopTone();


            } else if (ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED && currentCall != null) {
                currentCall = null;
                utils.executeJavascript("cordova.plugins.PJSIP.callState({state:'endcall'})");
                scAudioManager scAudio = scAudioManager.getInstance();
                scAudio.stopRingtone();
                Log.d(TAG,"======notifyCallState======PJSIP_INV_STATE_DISCONNECTED - state"+ci.getLastReason());


            }
        }



    };
    public void notifyCallMediaState(MyCall call){
        Log.d(TAG,"======notifyCallMediaState======"+call.toString());



    };
    public void notifyBuddyState(MyBuddy buddy){
        Log.d(TAG,"=======notifyBuddyState=========");

    };

}
