package gr.navarino.cordova.plugin;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.BuddyConfig;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnInstantMessageParam;
import org.pjsip.pjsua2.OnRegStateParam;


import gr.navarino.cordova.plugin.MyBuddy;
import gr.navarino.cordova.plugin.MyApp;
import gr.navarino.cordova.plugin.MyCall;

import java.util.ArrayList;

import android.util.Log;

/**
 * Created by infuser on 30/03/17.
 */

public class  MyAccount extends Account
{
    public ArrayList<MyBuddy> buddyList = new ArrayList<MyBuddy>();
    public AccountConfig cfg;
    static private String TAG = "MyAccount";

    MyAccount(AccountConfig config)
    {
        super();
        cfg = config;
    }

    public MyBuddy addBuddy(BuddyConfig bud_cfg)
    {
	/* Create Buddy */
        MyBuddy bud = new MyBuddy(bud_cfg);
        try {
            bud.create(this, bud_cfg);
        } catch (Exception e) {
            bud.delete();
            bud = null;
        }

        if (bud != null) {
            buddyList.add(bud);
            if (bud_cfg.getSubscribe())
                try {
                    bud.subscribePresence(true);
                } catch (Exception e) {}
        }

        return bud;
    }

    public void delBuddy(MyBuddy buddy)
    {
        buddyList.remove(buddy);
        buddy.delete();
    }

    public void delBuddy(int index)
    {
        MyBuddy bud = buddyList.get(index);
        buddyList.remove(index);
        bud.delete();
    }

    @Override
    public void onRegState(OnRegStateParam prm)
    {
        MyApp.observer.notifyRegState(prm.getCode(), prm.getReason(),
                prm.getExpiration());
    }

    @Override
    public void onIncomingCall(OnIncomingCallParam prm)
    {
        Log.d(TAG,"======== Incoming call ======== ");
        MyCall call = new MyCall(this, prm.getCallId());
        MyApp.observer.notifyIncomingCall(call);
    }

    @Override
    public void onInstantMessage(OnInstantMessageParam prm) {
        Log.d(TAG,"======== Incoming pager ======== ");
        Log.d(TAG,"From     : " + prm.getFromUri());
        Log.d(TAG,"To       : " + prm.getToUri());
        Log.d(TAG,"Contact  : " + prm.getContactUri());
        Log.d(TAG,"Mimetype : " + prm.getContentType());
        Log.d(TAG,"Body     : " + prm.getMsgBody());
    }
}
