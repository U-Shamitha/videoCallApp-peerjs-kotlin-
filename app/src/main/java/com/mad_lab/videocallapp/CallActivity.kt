package com.mad_lab.videocallapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class CallActivity : AppCompatActivity() {

    var userName = ""
    var friendUserName = ""

    var isPeerConnected = false

    var firebaseRef = Firebase.database.getReference("users")

    var isAudio = true
    var isVideo = true

    lateinit var callBtn : Button
    lateinit var toggleAudioBtn : ImageButton
    lateinit var toggleVideoBtn : ImageButton
    lateinit var toggleCameraBtn : ImageButton
    lateinit var callEndBtn : ImageButton
    lateinit var webView : WebView
    lateinit var callLayout : RelativeLayout
    lateinit var inputLayout : RelativeLayout
    lateinit var callControlLayout : LinearLayout
    lateinit var incomingCallTxt : TextView
    lateinit var acceptBtn : ImageButton
    lateinit var rejectButton : ImageButton
    lateinit var friendNameEdit : EditText




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        callBtn = findViewById(R.id.callBtn)
        toggleAudioBtn = findViewById(R.id.toggleAudioBtn)
        toggleVideoBtn = findViewById(R.id.toggleVideoBtn)
        toggleCameraBtn = findViewById(R.id.toggleCameraBtn)
        callEndBtn = findViewById(R.id.callEndBtn)
        webView = findViewById(R.id.webView)
        callLayout = findViewById(R.id.callLayout)
        inputLayout = findViewById(R.id.inputLayout)
        callControlLayout  = findViewById(R.id.callControlLayout)
        incomingCallTxt = findViewById(R.id.incomingCallTxt)
        acceptBtn = findViewById(R.id.acceptBtn)
        rejectButton = findViewById(R.id.rejectBtn)
        friendNameEdit = findViewById(R.id.friendNameEdit)

        userName = intent.getStringExtra("userName").toString()
//        Toast.makeText(this, "user name"+userName, Toast.LENGTH_LONG).show()

        callBtn.setOnClickListener{
            friendUserName = friendNameEdit.text.toString()
            sendCallRequest()
        }

        toggleAudioBtn.setOnClickListener {
            isAudio = !isAudio
            callJavascriptFunction("javascript:toggleAudio(\"${isAudio}\")")
            toggleAudioBtn.setImageResource(if (isAudio) R.drawable.ic_baseline_mic_24 else R.drawable.ic_baseline_mic_off_24)
        }

        toggleVideoBtn.setOnClickListener {
            isVideo = !isVideo
            callJavascriptFunction("javascript:toggleVideo(\"${isVideo}\")")
            toggleVideoBtn.setImageResource(if (isVideo) R.drawable.ic_baseline_videocam_24 else R.drawable.ic_baseline_videocam_off_24)
        }

//        toggleCameraBtn.setOnClickListener {
//            callJavascriptFunction("javascript:switchCamera()")
////            toggleVideoBtn.setImageResource(if (isVideo) R.drawable.ic_baseline_videocam_24 else R.drawable.ic_baseline_videocam_off_24)
//        }

        callEndBtn.setOnClickListener {
            finish()
        }

        setupWebView()

    }

    private fun sendCallRequest() {
        if (!isPeerConnected){
            Toast.makeText(this, "You are not connected. Check your internet", Toast.LENGTH_LONG).show()
            return
        }else{

            firebaseRef.child(friendUserName).child("incoming").setValue(userName)
            firebaseRef.child(friendUserName).child("isAvailable").addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value.toString() === "true"){
                        listenForConnId()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    private fun listenForConnId() {
//        Toast.makeText(this, "in listen for connection id", Toast.LENGTH_LONG).show()
        firebaseRef.child(friendUserName).child("connId").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value == null)
                    return
                switchToControls()
                callJavascriptFunction("javascript:startCall(\"${snapshot.value}\")")
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun setupWebView(){

//        Toast.makeText(this, "in web view", Toast.LENGTH_LONG).show()
        webView.webChromeClient = object : WebChromeClient() {

            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.addJavascriptInterface(JavascriptInterface(this), "Android")

        loadVideoCall()
    }

    private fun loadVideoCall() {
//        Toast.makeText(this, "in load video call", Toast.LENGTH_LONG).show()
        val filePath = "file:android_asset/call.html"
        webView.loadUrl(filePath)

        webView.webViewClient = object: WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                initializePeer()
            }
        }
    }

    var unique_id = ""

    private fun initializePeer() {

//        Toast.makeText(this, "in intialize peer", Toast.LENGTH_LONG).show()

        unique_id = getUniqueId()

//        Toast.makeText(this, "unique id"+unique_id, Toast.LENGTH_LONG).show()

        callJavascriptFunction("javascript:init(\"${unique_id}\")")

        firebaseRef.child(userName).child("incoming").addValueEventListener(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    onCallRequest(snapshot.value as? String)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun onCallRequest(caller: String?) {
        if(caller == null || caller.equals("")) return

        callLayout.visibility = View.VISIBLE
        incomingCallTxt.text = "$caller is calling..."

        acceptBtn.setOnClickListener {
            firebaseRef.child(userName).child("connId").setValue(unique_id)
            firebaseRef.child(userName).child("isAvailable").setValue(true)

            callLayout.visibility = View.GONE
            switchToControls()

        }

        rejectButton.setOnClickListener {
            firebaseRef.child(userName).child("incoming").setValue(null)
            callLayout.visibility = View.GONE
        }
    }

    private fun switchToControls() {
        inputLayout.visibility = View.GONE
        callControlLayout.visibility = View.VISIBLE
    }

    private fun getUniqueId(): String {
//        Toast.makeText(this, "in get unique id", Toast.LENGTH_LONG).show()
        return UUID.randomUUID().toString()
    }

    private  fun callJavascriptFunction(functionString: String){
//        Toast.makeText(this, "in call javscript function", Toast.LENGTH_LONG).show()
        webView.post{ webView.evaluateJavascript(functionString, null)}
//        Toast.makeText(this, "after web view post", Toast.LENGTH_LONG).show()
    }

    fun onPeerConnected() {
        isPeerConnected = true
    }

    override fun onBackPressed() {
        finish()
    }
    override fun onDestroy() {
        firebaseRef.child(userName).setValue(null)
        webView.loadUrl("about:blank")
        super.onDestroy()
    }
}