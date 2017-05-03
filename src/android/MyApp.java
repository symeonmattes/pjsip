package gr.navarino.cordova.plugin;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import org.pjsip.pjsua2.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import gr.navarino.cordova.plugin.Utils;
import gr.navarino.cordova.plugin.MyAppObserver;
import gr.navarino.cordova.plugin.MyAccount;
import gr.navarino.cordova.plugin.MyBuddy;


class MyLogWriter extends LogWriter
{
    static private String TAG = "MyLogWriter";
    @Override
    public void write(LogEntry entry) {

        Log.d(TAG,entry.getMsg());

    }
}


class MyAccountConfig
{
    public AccountConfig accCfg = new AccountConfig();
    public ArrayList<BuddyConfig> buddyCfgs = new ArrayList<BuddyConfig>();
    static private String TAG = "MyAccountConfig";

    public void readObject(ContainerNode node)
    {
        try {
            ContainerNode acc_node = node.readContainer("Account");
            accCfg.readObject(acc_node);
            ContainerNode buddies_node = acc_node.readArray("buddies");
            buddyCfgs.clear();
            while (buddies_node.hasUnread()) {
                BuddyConfig bud_cfg = new BuddyConfig();
                bud_cfg.readObject(buddies_node);
                buddyCfgs.add(bud_cfg);
            }
        } catch (Exception e) {}
    }

    public void writeObject(ContainerNode node)
    {
        try {
            ContainerNode acc_node = node.writeNewContainer("Account");
            accCfg.writeObject(acc_node);
            ContainerNode buddies_node = acc_node.writeNewArray("buddies");
            for (int j = 0; j < buddyCfgs.size(); j++) {
                buddyCfgs.get(j).writeObject(buddies_node);
            }
        } catch (Exception e) {}
    }
}


public class MyApp {
    static private String TAG = "MyApp";
    static {
        try{
            System.loadLibrary("openh264");
            // Ticket #1937: libyuv is now included as static lib
            //System.loadLibrary("yuv");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG,"UnsatisfiedLinkError: " + e.getMessage());
            Log.e(TAG,"This could be safely ignored if you " + "don't need video.");
        }
        System.loadLibrary("pjsua2");
        Log.d(TAG,"Library loaded");
    }

    public static Endpoint ep = new Endpoint();
    public static MyAppObserver observer;
    public ArrayList<MyAccount> accList = new ArrayList<MyAccount>();

    private ArrayList<MyAccountConfig> accCfgs = new ArrayList<MyAccountConfig>();
    private EpConfig epConfig = new EpConfig();
    private TransportConfig sipTpConfig = new TransportConfig();
    private String appDir;

    /* Maintain reference to log writer to avoid premature cleanup by GC */
    private MyLogWriter logWriter;

    private final String configName = "pjsua2.json";
    private final int SIP_PORT  = 6000;
    private final int LOG_LEVEL = 4;

    public void init(MyAppObserver obs, String app_dir)
    {

        init(obs, app_dir, false);
    }



    public static synchronized void checkThread(){

        try {
            Log.w(TAG,"Thread name:"+Thread.currentThread().getName());
            if (ep != null && !ep.libIsThreadRegistered())

                ep.libRegisterThread(Thread.currentThread().getName());

            Log.w(TAG,"Thread name:"+Thread.currentThread().getName());

        } catch (Exception e) {
            Log.w(TAG, "Threading: libRegisterThread failed: " + e.getMessage());
        }

    }

    public void init(MyAppObserver obs, String app_dir, boolean own_worker_thread) {
        observer = obs;
        appDir = app_dir;

	/* Create endpoint */
        try {
            ep.libCreate();
        } catch (Exception e) {
            return;
        }


	/* Load config */
        String configPath = appDir + "/" + configName;
        File f = new File(configPath);
        if (f.exists()) {
            loadConfig(configPath);
        } else {
	    /* Set 'default' values */
            sipTpConfig.setPort(SIP_PORT);
        }

	/* Override log level setting */
        epConfig.getLogConfig().setLevel(LOG_LEVEL);
        epConfig.getLogConfig().setConsoleLevel(LOG_LEVEL);

	/* Set log config. */
        LogConfig log_cfg = epConfig.getLogConfig();
        logWriter = new MyLogWriter();
        log_cfg.setWriter(logWriter);
        log_cfg.setDecor(log_cfg.getDecor() &
                ~(pj_log_decoration.PJ_LOG_HAS_CR.swigValue() |
                        pj_log_decoration.PJ_LOG_HAS_NEWLINE.swigValue()));

	/* Set ua config. */
        UaConfig ua_cfg = epConfig.getUaConfig();
        ua_cfg.setUserAgent("Pjsua2 Android " + ep.libVersion().getFull());
        StringVector stun_servers = new StringVector();
        stun_servers.add("stun.pjsip.org");
        ua_cfg.setStunServer(stun_servers);
        if (own_worker_thread) {
            ua_cfg.setThreadCnt(0);
            ua_cfg.setMainThreadOnly(true);
        }

	/* Init endpoint */
        try {
            ep.libInit(epConfig);
        } catch (Exception e) {
            return;
        }

	/* Create transports. */
        try {
            ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP,
                    sipTpConfig);
        } catch (Exception e) {
            Log.e(TAG,"Error:"+e.toString());

        }

        try {
            ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TCP,
                    sipTpConfig);
        } catch (Exception e) {
            Log.e(TAG,"Error:"+e.toString());
        }

	/* Create accounts. */
        for (int i = 0; i < accCfgs.size(); i++) {
            MyAccountConfig my_cfg = accCfgs.get(i);

	    /* Customize account config */
            my_cfg.accCfg.getNatConfig().setIceEnabled(true);
            my_cfg.accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
            my_cfg.accCfg.getVideoConfig().setAutoShowIncoming(true);

            MyAccount acc = addAcc(my_cfg.accCfg);
            if (acc == null)
                continue;

	    /* Add Buddies */
            for (int j = 0; j < my_cfg.buddyCfgs.size(); j++) {
                BuddyConfig bud_cfg = my_cfg.buddyCfgs.get(j);
                acc.addBuddy(bud_cfg);
            }
        }

	/* Start. */
        try {
            ep.libStart();
        } catch (Exception e) {
            return;
        }
    }

    public MyAccount addAcc(AccountConfig cfg)
    {
        MyAccount acc = new MyAccount(cfg);
        try {
            acc.create(cfg);
        } catch (Exception e) {
            acc = null;
            return null;
        }

        accList.add(acc);
        return acc;
    }

    public void delAcc(MyAccount acc)
    {
        accList.remove(acc);
    }

    private void loadConfig(String filename)
    {
        JsonDocument json = new JsonDocument();

        try {
	    /* Load file */
            json.loadFile(filename);
            ContainerNode root = json.getRootContainer();

	    /* Read endpoint config */
            epConfig.readObject(root);

	    /* Read transport config */
            ContainerNode tp_node = root.readContainer("SipTransport");
            sipTpConfig.readObject(tp_node);

	    /* Read account configs */
            accCfgs.clear();
            ContainerNode accs_node = root.readArray("accounts");
            while (accs_node.hasUnread()) {
                MyAccountConfig acc_cfg = new MyAccountConfig();
                acc_cfg.readObject(accs_node);
                accCfgs.add(acc_cfg);
            }
        } catch (Exception e) {
            Log.e(TAG,"Error:"+e.toString());
        }

	/* Force delete json now, as I found that Java somehow destroys it
	* after lib has been destroyed and from non-registered thread.
	*/
        json.delete();
    }

    private void buildAccConfigs()
    {
	/* Sync accCfgs from accList */
        accCfgs.clear();
        for (int i = 0; i < accList.size(); i++) {
            MyAccount acc = accList.get(i);
            MyAccountConfig my_acc_cfg = new MyAccountConfig();
            my_acc_cfg.accCfg = acc.cfg;

            my_acc_cfg.buddyCfgs.clear();
            for (int j = 0; j < acc.buddyList.size(); j++) {
                MyBuddy bud = acc.buddyList.get(j);
                my_acc_cfg.buddyCfgs.add(bud.cfg);
            }

            accCfgs.add(my_acc_cfg);
        }
    }

    private void saveConfig(String filename)
    {
        JsonDocument json = new JsonDocument();

        try {
	    /* Write endpoint config */
            json.writeObject(epConfig);

	    /* Write transport config */
            ContainerNode tp_node = json.writeNewContainer("SipTransport");
            sipTpConfig.writeObject(tp_node);

	    /* Write account configs */
            buildAccConfigs();
            ContainerNode accs_node = json.writeNewArray("accounts");
            for (int i = 0; i < accCfgs.size(); i++) {
                accCfgs.get(i).writeObject(accs_node);
            }

	    /* Save file */
            json.saveFile(filename);
        } catch (Exception e) {}

	/* Force delete json now, as I found that Java somehow destroys it
	* after lib has been destroyed and from non-registered thread.
	*/
        json.delete();
    }

    public void deinit()
    {
        String configPath = appDir + "/" + configName;
        saveConfig(configPath);

	/* Try force GC to avoid late destroy of PJ objects as they should be
	* deleted before lib is destroyed.
	*/
        Runtime.getRuntime().gc();

	/* Shutdown pjsua. Note that Endpoint destructor will also invoke
	* libDestroy(), so this will be a test of double libDestroy().
	*/
        try {
            ep.libDestroy();
        } catch (Exception e) {}

	/* Force delete Endpoint here, to avoid deletion from a non-
	* registered thread (by GC?).
	*/
        ep.delete();
        ep = null;
    }
}
