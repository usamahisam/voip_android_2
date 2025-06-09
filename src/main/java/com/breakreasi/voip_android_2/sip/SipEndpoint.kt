package com.breakreasi.voip_android_2.sip

import org.pjsip.pjsua2.Endpoint
import org.pjsip.pjsua2.OnIpChangeProgressParam
import org.pjsip.pjsua2.OnTransportStateParam
import org.pjsip.pjsua2.pj_constants_

class SipEndpoint: Endpoint() {
    override fun onTransportState(prm: OnTransportStateParam?) {
        super.onTransportState(prm)
    }

    override fun onIpChangeProgress(prm: OnIpChangeProgressParam?) {
        super.onIpChangeProgress(prm)
        if (prm?.status != pj_constants_.PJ_SUCCESS) {
            hangupAllCalls()
            return
        }
    }
}