package gr.navarino.cordova.plugin;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.ToneGenerator;
import android.media.MediaRecorder;
import android.util.Log;
import android.media.MediaPlayer;

import com.navarino.infinity4u.R;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import java.io.IOException;

/**
 * Created by infuser on 11/04/17.
 */
public class scAudioManager {

    private MediaPlayer mPlayer = null;
    private MediaRecorder mRecorder = null;
    private ToneGenerator mRingbackTone;
    private Ringtone mRingtone = null;
    private Context mContext;
    private static CordovaInterface cordova = null;
    private static CordovaWebView webView =  null;
    private static String TAG = "scAudioManager";
    String tempFileName;


    private static scAudioManager ourInstance = new scAudioManager();

    public static scAudioManager getInstance() {
        return ourInstance;
    }

    private scAudioManager() {
    }




    void initialise(CordovaInterface crd, CordovaWebView wbview){


        cordova = crd;
        webView = wbview;

        mContext = cordova.getActivity();
        tempFileName=mContext.getExternalCacheDir().getAbsolutePath()+"/audiorecordtest.3gp";

    }



    public void setSpeakerMode(final Boolean isActive) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                Log.d("SIP", "setSpeakerMode");
                AudioManager am = ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE));
                am.setMode(AudioManager.MODE_NORMAL);
                am.setSpeakerphoneOn(isActive);
            }
        });
    }

    public void muteMicrophone(Boolean state) {
        Log.d("SIP", "muteMicrophone: " + state.toString());
        AudioManager am = ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE));
        am.setMicrophoneMute(state);
    }


    public synchronized void startRingbackTone() {

        this.playTone(R.raw.ringbacktone,true);
    }

    public synchronized void startRingtone() {
        this.playTone(R.raw.ringtone,true);
    }

    public synchronized void stopRingtone() {

        this.stopTone();
    }

    public synchronized void startBusyTone() {

        this.playTone(R.raw.busytone,false);

    }

    public void playDTMF(String num){

        Log.i(TAG,"The dtmf number '"+num+"' will be called.");
        int sound=0;
        if (num.equals("0")){
            sound = R.raw.dtmf0;
        }else if (num.equals("1")){
            sound = R.raw.dtmf1;
        }else if (num.equals("2")){
            sound = R.raw.dtmf2;
        }else if (num.equals("3")){
            sound = R.raw.dtmf3;
        }else if (num.equals("4")){
            sound = R.raw.dtmf4;
        }else if (num.equals("5")){
            sound = R.raw.dtmf5;
        }else if (num.equals("6")){
            sound = R.raw.dtmf6;
        }else if (num.equals("7")){
            sound = R.raw.dtmf7;
        }else if (num.equals("8")){
            sound = R.raw.dtmf8;
        }else if (num.equals("9")){
            sound = R.raw.dtmf9;
        }else if (num.equals("#")){
            sound = R.raw.dtmfhash;
        }else if (num.equals("*")){
            sound = R.raw.dtmfstar;
        }

        if (sound!=0)
            this.playTone(sound,false);

    }


    public void checkAudio(final CallbackContext callbackContext){

        this.setSpeakerMode(true);

        mPlayer = MediaPlayer.create(mContext,R.raw.ringbacktone);
        mPlayer.setLooping(false);
        this.startRecording();

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                try {
                    //stop recording
                    mRecorder.stop();
                    mRecorder.release();
                    mRecorder = null;

                    //play back the recorded file
                    mPlayer = new MediaPlayer();
                    mPlayer.setDataSource(tempFileName);
                    mPlayer.prepare();
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            callbackContext.success("OK");
                        }
                    });
                    mPlayer.start();


                } catch (IOException e) {
                    callbackContext.success("Failed - "+e.toString());
                    Log.e("startPlayRecordedFile", "failed");
                }



            }
        });

        mPlayer.start();

    }

    public synchronized void stopTone() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer = null;
        }
    }

    private synchronized void playTone(int tone,boolean loop){

        if (mPlayer!=null)
            this.stopTone();

        mPlayer = MediaPlayer.create(mContext,tone);
        mPlayer.setLooping(loop);
        mPlayer.start();

    }

    public void startRecording(){
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(tempFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("startRecording", "prepare() failed");
        }

        mRecorder.start();

    }

}
