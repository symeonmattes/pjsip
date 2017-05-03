# PJSIP cordova plugin
**NOTICE!** This project currently supports only Android devices and it has been tested only in devices with armabi architecture.

This plugin is based on [PJSIP library] (http://www.pjsip.org) version 2.5.5. PJSIP is an open source multimedia communication library writte in C language that supports many protocols including VoIP communications. The current implementation has used SIP, SDP, RTP protocols for VoIP telephony.

# How to use

The PJSIP javascript object has four prototypes methods that can be overriden for the four states of the device, i.e. Call Out, Call In, Call End and Call established.

- PJSIP.stateCallOut(arg0): Event triggered after calling PJSIP.makeCall(CallingNumber). arg0 is the calling number.
- PJSIP.stateCallEstablished(): Event triggered after establishing the call.
- PJSIP.stateCallEnd(): Event triggered when a call has ended.
- PJSIP.stateCallIn(arg0): Event triggered when there is an incoming call. arg0 is the number of the device that calls.

This methods can be used for changing the layout of the application.

Further functions have been implemented in www/PJSIP.js, each of which is responsible for different actions in VoIP communications. This include:

- PJSIP.connect(arg0,arg1,arg2,arg3): User arg0 with password ar1 connects to system with IP arg2 and Proxy IP arg3. arg3 most of the times is not necessary and '' can be used.
- PJSIP.disconnect: Unregistering from the system. 
- PJSIP.endCall: Ending a call
- PJSIP.muteCall: Muting a call
- PSJIP.activateSpeaker: Activating the speaker
- PJSIP.dtmfCall(arg0): Sending dtmf number arg0
- PJSIP.declineCall: Declining an incoming call
- PJSIP.answerCall: Answering an incoming call


# Known issues

You might encounter some issues after the installation.

- All wav files used for the phone calls have been installed in res/raw folder. There might be necessary to correct in scAudioManager.java the corresponding import library (import com.ionicframework.infinity4uandroidv2841245.R;).

# Acknowledgement

This plugin has been supported by [Navarino](http://navarino.gr).


