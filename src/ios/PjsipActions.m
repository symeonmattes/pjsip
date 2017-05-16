#import "PjsipActions.h"
#import "Utils.h"
#import "scAudioManager.h"
#import <Foundation/Foundation.h>


#import <pjlib.h>
#import <pjsua.h>
#import <pj/log.h>



@implementation PjsipActions: NSObject

    NSDictionary * pjuser;

static pjsua_acc_id acc_id;
static pjsua_call_id callId;


const size_t MAX_SIP_ID_LENGTH = 50;
const size_t MAX_SIP_REG_URI_LENGTH = 50;


static NSString* outGoingCallNumber = @"";
static NSString* inComingCallNumber = @"";


static void on_incoming_call(pjsua_acc_id acc_id, pjsua_call_id call_id, pjsip_rx_data *rdata);
static void on_call_state(pjsua_call_id call_id, pjsip_event *e);
static void on_call_media_state(pjsua_call_id call_id);
static void error_exit(const char *title, pj_status_t status);

static Utils *utils = NULL;


//https://xianwen.me/2017/04/08/how-to-make-an-ios-voip-app-with-pjsip-part-1/

/* Callback called by the library upon receiving incoming call */
static void on_incoming_call(pjsua_acc_id acc_id, pjsua_call_id call_id,
                             pjsip_rx_data *rdata)
{
    pjsua_call_info ci;
    
    PJ_UNUSED_ARG(acc_id);
    PJ_UNUSED_ARG(rdata);
    
    scAudioManager *scaudio = [scAudioManager sharedInstance];
    [scaudio startRingtone];
    
    pjsua_call_get_info(call_id, &ci);
    
    NSLog(@"on_incoming_call: Incoming call from %.*s!!",(int)ci.remote_info.slen,ci.remote_info.ptr);
    
    
    [utils executeJavascript:[NSString stringWithFormat:@"cordova.plugins.PJSIP.callState({state:'incall',inComingCallNumber:'%s'});",ci.remote_info.ptr]];
    
    
    callId = call_id;
    //    /* Automatically answer incoming calls with 200/OK */
    //    pjsua_call_answer(call_id, 200, NULL, NULL);
    
}

/* Callback called by the library when call's state has changed */
static void on_call_state(pjsua_call_id call_id, pjsip_event *e)
{
    pjsua_call_info ci;
    
    PJ_UNUSED_ARG(e);
    
    pjsua_call_get_info(call_id, &ci);
    
    NSLog(@"on_call_state: Call %d state=%.*s",call_id,(int)ci.state_text.slen,ci.state_text.ptr);
    
    
    if (strcmp(ci.state_text.ptr,"CALLING") == 0){
        scAudioManager *scaudio = [scAudioManager sharedInstance];
        [scaudio startRingbackTone];
        [utils executeJavascript:[NSString stringWithFormat:@"cordova.plugins.PJSIP.callState({state:'outcall',outGoingCallNumber:'%@'});",outGoingCallNumber]];
    }else if (strcmp(ci.state_text.ptr,"CONFIRMED") == 0){
        scAudioManager *scaudio = [scAudioManager sharedInstance];
        [scaudio stopTone];
        
        [utils executeJavascript:@"cordova.plugins.PJSIP.callState({state:'established'});"];
    }else if (strcmp(ci.state_text.ptr,"DISCONNCTD") == 0){
        
        scAudioManager *scaudio = [scAudioManager sharedInstance];
        [scaudio stopTone];
        [utils executeJavascript:@"cordova.plugins.PJSIP.callState({state:'endcall'});"];
    }
    
    
    
    
}

/* Callback called by the library when call's media state has changed */
static void on_call_media_state(pjsua_call_id call_id)
{
    
    pjsua_call_info ci;
    
    pjsua_call_get_info(call_id, &ci);
    
    NSLog(@"on_call_media_state: Call %d state=%.*s",call_id,(int)ci.state_text.slen,ci.state_text.ptr);
    
    if (ci.media_status == PJSUA_CALL_MEDIA_ACTIVE) {
        // When media is active, connect call to sound device.
        pjsua_conf_connect(ci.conf_slot, 0);
        pjsua_conf_connect(0, ci.conf_slot);
    }
}

/* Display error and exit application */
static void error_exit(const char *title, pj_status_t status)
{
    pjsua_perror("PJSIP.m",title,status);
    pjsua_destroy();
    exit(1);
}

int startPjsip()
{
    pj_status_t status;
    
    // Create pjsua first
    status = pjsua_create();
    if (status != PJ_SUCCESS) error_exit("Error in pjsua_create()", status);
    
    // Init pjsua
    {
        //http://stackoverflow.com/questions/37836219/make-call-programmatically-using-pjsip-2-5-in-ios
        
        // Init the config structure
        pjsua_config cfg;
        pjsua_config_default (&cfg);
        
        cfg.cb.on_incoming_call = &on_incoming_call;
        cfg.cb.on_call_media_state = &on_call_media_state;
        cfg.cb.on_call_state = &on_call_state;
        
        // Init the logging config structure
        pjsua_logging_config log_cfg;
        pjsua_logging_config_default(&log_cfg);
        log_cfg.console_level = 4;
        
        // Init the pjsua
        status = pjsua_init(&cfg, &log_cfg, NULL);
        if (status != PJ_SUCCESS) error_exit("Error in pjsua_init()", status);
    }
    
    // Add UDP transport.
    {
        // Init transport config structure
        pjsua_transport_config cfg;
        pjsua_transport_config_default(&cfg);
        cfg.port = 5080;
        
        // Add TCP transport.
        status = pjsua_transport_create(PJSIP_TRANSPORT_UDP, &cfg, NULL);
        if (status != PJ_SUCCESS) error_exit("Error creating transport", status);
    }
    
    // Add TCP transport.
    {
        // Init transport config structure
        pjsua_transport_config cfg;
        pjsua_transport_config_default(&cfg);
        cfg.port = 5080;
        
        // Add TCP transport.
        status = pjsua_transport_create(PJSIP_TRANSPORT_TCP, &cfg, NULL);
        if (status != PJ_SUCCESS) error_exit("Error creating transport", status);
    }
    
    // Initialization is done, now start pjsua
    status = pjsua_start();
    if (status != PJ_SUCCESS) error_exit("Error starting pjsua", status);
    
    // Register the account on local sip server
    {
        pjsua_acc_config cfg;
        
        pjsua_acc_config_default(&cfg);
        
        char sipId[MAX_SIP_ID_LENGTH];
        sprintf(sipId, "sip:localhost");
        cfg.id = pj_str(sipId);
        
        char regUri[MAX_SIP_REG_URI_LENGTH];
        sprintf(regUri, "");
        cfg.reg_uri = pj_str(regUri);
        
        // Account cred info
        cfg.cred_count = 1;
        //        cfg.cred_info[0].scheme = pj_str("digest");
        //cfg.cred_info[0].realm = pj_str(domain);
        cfg.cred_info[0].realm = pj_str((char *)"*");
        cfg.cred_info[0].scheme = pj_str((char *)"digest");
        cfg.cred_info[0].username = pj_str("");
        cfg.cred_info[0].data_type = PJSIP_CRED_DATA_PLAIN_PASSWD;
        cfg.cred_info[0].data = pj_str("");
        
        status = pjsua_acc_add(&cfg, PJ_TRUE, &acc_id);
        if (status != PJ_SUCCESS) error_exit("Error adding account", status);
    }
    
    return 0;
}


void modifyPjsipUser(char *user, char *password, char* domain, char *proxy){
  
    pj_status_t status;
    // Register the account on local sip server
    {
        pjsua_acc_config cfg;
        
        pjsua_acc_config_default(&cfg);
        
        char sipId[MAX_SIP_ID_LENGTH];
        char regUri[MAX_SIP_REG_URI_LENGTH];
        if (strcmp(user,"")==0){
            sprintf(sipId, "sip:localhost");
            sprintf(regUri, "");
            
        }else{
            sprintf(sipId, "sip:%s@%s", user, domain);
            sprintf(regUri, "sip:%s@%s",user, domain);
        }
        cfg.id = pj_str(sipId);
        cfg.reg_uri = pj_str(regUri);
        
        // Account cred info
        cfg.cred_count = 1;
        //        cfg.cred_info[0].scheme = pj_str("digest");
        //cfg.cred_info[0].realm = pj_str(domain);
        cfg.cred_info[0].realm = pj_str((char *)"*");
        cfg.cred_info[0].scheme = pj_str((char *)"digest");
        cfg.cred_info[0].username = pj_str(user);
        cfg.cred_info[0].data_type = PJSIP_CRED_DATA_PLAIN_PASSWD;
        cfg.cred_info[0].data = pj_str(password);
        
        status = pjsua_acc_modify(acc_id,&cfg);
        
        
        if (status != PJ_SUCCESS) error_exit("Error adding account", status);
    }
}

-(void) initialise:(id)dlg{
    
    utils = [[Utils alloc] init ];
    [utils initialise:dlg];
    
    pjuser = @{@"user" : @"",@"password":@"",@"domain":@"",@"proxy":@""};
    
    startPjsip();
    
};

-(void) acceptCall{
    
    /* Automatically answer incoming calls with 200/OK */
    pjsua_call_answer(callId, 200, NULL, NULL);
    
};

-(void) registerPBX:(NSString *)user Password:(NSString *) pass systemIpAddress:(NSString *) smIp proxyIpAddress:(NSString *) prxIp{

    
    pjuser = @{@"user" : user,@"password":pass,@"domain":smIp,@"proxy":prxIp};
    
    
    modifyPjsipUser((char *) [user UTF8String],(char *) [pass UTF8String],(char *) [smIp UTF8String],(char *) [prxIp UTF8String]);
}

-(void) unregisterPBX{
    
    modifyPjsipUser("","","localhost","");
}

-(void) makeCall:(NSString *) number{
    
    outGoingCallNumber = number;
    
    
    NSString *duri = [NSString stringWithFormat:@"sip:%@@%@",number, pjuser[@"domain"]];
    
    char *destUri = (char *) [duri UTF8String];
    
    pj_status_t status;
    pj_str_t uri = pj_str(destUri);
    
    status = pjsua_call_make_call(acc_id, &uri, 0, NULL, NULL, &callId);
    
    
    if (status != PJ_SUCCESS) error_exit("Error making call", status);
    
}

-(void) holdCall:(BOOL) isActive {
    
    
    pj_status_t status = 0;
    if (isActive){
        status = pjsua_call_set_hold(acc_id,NULL);
    }else{
        status = pjsua_call_reinvite(acc_id,PJSUA_CALL_UNHOLD,NULL);
    }
    
    if (status != PJ_SUCCESS)
        error_exit("Error making hold call", status);

    
}


-(void) rejectCall{
    
    pjsua_call_hangup_all();
}



-(void) playDTMF:(NSString *) number{


    pj_status_t status = 0;
    
    if ([number  isEqual: @"*"]){
        number = @"10";
    }else if ([number isEqual:@"#"]){
        number = @"11";
    }
    
    
    char * dtmf = (char *) [number UTF8String];
    pj_str_t pjdtmf = pj_str(dtmf);
    //PJ_DECL(pj_status_t) pjsua_call_dial_dtmf(pjsua_call_id call_id,const pj_str_t *digits);
    //DTMF string digits to be sent as described on RFC 2833 section 3.10.
    status = pjsua_call_dial_dtmf(callId,&pjdtmf);
    
    if (status != PJ_SUCCESS)
      error_exit("Error making hold call", status);
    

}

@end
