package com.breakreasi.voip_android_2.sip

import org.pjsip.pjsua2.Endpoint
import org.pjsip.pjsua2.OnIpChangeProgressParam
import org.pjsip.pjsua2.OnTransportStateParam
import org.pjsip.pjsua2.pj_constants_
import org.pjsip.pjsua2.pjsua_ip_change_op

class SipEndpoint(
    private val sipService: SipService
): Endpoint() {
    override fun onTransportState(prm: OnTransportStateParam?) {
        super.onTransportState(prm)
    }

    override fun onIpChangeProgress(prm: OnIpChangeProgressParam?) {
        super.onIpChangeProgress(prm)
        if (prm?.status != pj_constants_.PJ_SUCCESS) {
            hangupAllCalls()
            return
        }
        if (prm.op == pjsua_ip_change_op.PJSUA_IP_CHANGE_OP_COMPLETED) {
            if (sipService.sipAccount != null) {
                if (sipService.sipAccount!!.call != null) {
                    sipService.sipAccount!!.call?.sendReinvite()
                }
            }
        }
    }
}