package com.mad_lab.videocallapp

import android.webkit.JavascriptInterface
import com.mad_lab.videocallapp.CallActivity

class JavascriptInterface(val callActivity: CallActivity) {

    @JavascriptInterface
    public fun onPeerConnected(){
        callActivity.onPeerConnected()
    }
}