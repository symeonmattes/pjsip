var exec = require('cordova/exec');

function PJSIP() {
}

PJSIP.prototype.isSupported = function(success, error) {
    exec(success, error, "PJSIP", "issupported", []);
};

PJSIP.prototype.connect = function(arg0, arg1, arg2, arg3, success, error) {
    exec(success, error, "PJSIP", "connect", [arg0, arg1, arg2, arg3]);
};

PJSIP.prototype.makeCall = function(arg0, success, error) {
    arg0 = arg0.trim().replace(/\.| /g, '');
    exec(success, error, "PJSIP", "makecall", [arg0]);
};

PJSIP.prototype.endCall = function(success, error) {
    exec(success, error, "PJSIP", "endcall", []);
};

PJSIP.prototype.disconnect = function(success, error) {
    exec(success, error, "PJSIP", "disconnect", []);
};

PJSIP.prototype.isConnected = function(success, error) {
    exec(success, error, "PJSIP", "isconnected", []);
};

PJSIP.prototype.muteCall = function(arg0, success, error) {
    exec(success, error, "PJSIP", "mutecall", [arg0]);
};

PJSIP.prototype.activateSpeaker = function(arg0, success, error) {
    exec(success, error, "PJSIP", "activatespeaker", [arg0]);
};

PJSIP.prototype.dtmfCall = function(arg0, success, error) {
    exec(success, error, "PJSIP", "dtmfcall", [arg0]);
};

PJSIP.prototype.listenCall = function(success, error) {
    exec(success, error, "PJSIP", "listen", []);
};

PJSIP.prototype.stopListenCall = function(success, error) {
    exec(success, error, "PJSIP", "stoplisten", []);
};

PJSIP.prototype.muteMicrophone = function(arg0, success, error) {
    exec(success, error, "PJSIP", "mutemicrophone", [arg0]);
};

PJSIP.prototype.holdCall = function(arg0, success, error) {
    exec(success, error, "PJSIP", "holdcall", [arg0]);
};

PJSIP.prototype.declineCall = function(success, error) {
    exec(success, error, "PJSIP", "declinecall", []);
};

PJSIP.prototype.acceptCall = function(success, error) {
    exec(success, error, "PJSIP", "acceptcall", []);
};


PJSIP.prototype.incommingCall = function(success, error) {
    exec(success, error, "PJSIP", "incommingcall", []);
};

PJSIP.prototype.answerCall = function(success, error) {
    exec(success, error, "PJSIP", "answercall", []);
};

PJSIP.prototype.callState = function(arg0,success, error) {

  switch (arg0.state){
    case "outcall":
      this.stateCallOut(arg0.outGoingCallNumber);
      break;
    case "established":
      this.stateCallEstablished();
      break;
    case "endcall":
      this.stateCallEnd();
      break;
    case "incall":
      this.stateCallIn(arg0.inComingCallNumber);
      break;
  }


};

PJSIP.prototype.stateCallOut = function(arg0){}
PJSIP.prototype.stateCallEstablished = function(){}
PJSIP.prototype.stateCallEnd = function(){}
PJSIP.prototype.stateCallIn = function(arg0){}



module.exports = new PJSIP();
