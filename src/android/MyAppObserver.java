package gr.navarino.cordova.plugin;

import org.pjsip.pjsua2.pjsip_status_code;


import gr.navarino.cordova.plugin.MyCall;
import gr.navarino.cordova.plugin.MyBuddy;

/**
 * Created by infuser on 30/03/17.
 */

/* Interface to separate UI & engine a bit better */
interface MyAppObserver
{
    abstract void notifyRegState(pjsip_status_code code, String reason, int expiration);
    abstract void notifyIncomingCall(MyCall call);
    abstract void notifyCallState(MyCall call);
    abstract void notifyCallMediaState(MyCall call);
    abstract void notifyBuddyState(MyBuddy buddy);
}
