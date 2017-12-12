package gr.navarino.cordova.plugin;

import android.util.Log;
import org.apache.cordova.CallbackContext;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import gr.navarino.cordova.plugin.scAudioManager;
import gr.navarino.cordova.plugin.Utils;


public class PjsipDiagnostic {

    final static scAudioManager scAudio = scAudioManager.getInstance();
    private static Utils utils = new Utils();

    public void checkArchitecture(final CallbackContext callbackContext){

        try{

            String arch = System.getProperty("os.arch");
            callbackContext.success(arch);
        } catch(Exception e){
            callbackContext.error(e.toString());
        }

    }

    public void checkClientIP(final CallbackContext callbackContext){

        String ipAddress =Utils.getIPAddress(true);

        if (ipAddress.equals(""))
            callbackContext.error("NOT_CONNECTED");
        else
            callbackContext.success(ipAddress);

    }

    public void checkAudio(final CallbackContext callbackContext){

        scAudio.checkAudio(callbackContext);

    }

    public void checkPBXfirewall(String host,int port,final CallbackContext callbackContext){

        Socket s = null;
        String answer="";
        try
        {
//        InetAddress local = InetAddress.getByName(host);
//        s = new DatagramSocket(port,local);
//            s = new Socket(host, port);
            s = new Socket();
            SocketAddress saddress = new InetSocketAddress(host,port);
            s.connect(saddress,5000);

            callbackContext.success("true");
        }
        catch (Exception e) {
            callbackContext.error(e.toString());
        }
        finally
        {
            if(s != null)
                try {s.close();}
                catch(Exception e){}


        }

    }


}
