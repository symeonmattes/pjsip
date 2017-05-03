package gr.navarino.cordova.plugin;

import org.pjsip.pjsua2.Buddy;
import org.pjsip.pjsua2.BuddyConfig;
import org.pjsip.pjsua2.BuddyInfo;
import org.pjsip.pjsua2.pjsip_evsub_state;
import org.pjsip.pjsua2.pjsua_buddy_status;


import gr.navarino.cordova.plugin.MyApp;

/**
 * Created by infuser on 30/03/17.
 */

public class MyBuddy extends Buddy
{
    public BuddyConfig cfg;

    MyBuddy(BuddyConfig config)
    {
        super();
        cfg = config;
    }

    public String getStatusText()
    {
        BuddyInfo bi;

        try {
            bi = getInfo();
        } catch (Exception e) {
            return "?";
        }

        String status = "";
        if (bi.getSubState() == pjsip_evsub_state.PJSIP_EVSUB_STATE_ACTIVE) {
            if (bi.getPresStatus().getStatus() ==
                    pjsua_buddy_status.PJSUA_BUDDY_STATUS_ONLINE)
            {
                status = bi.getPresStatus().getStatusText();
                if (status == null || status.length()==0) {
                    status = "Online";
                }
            } else if (bi.getPresStatus().getStatus() ==
                    pjsua_buddy_status.PJSUA_BUDDY_STATUS_OFFLINE)
            {
                status = "Offline";
            } else {
                status = "Unknown";
            }
        }
        return status;
    }

    @Override
    public void onBuddyState()
    {
        MyApp.observer.notifyBuddyState(this);
    }

}
