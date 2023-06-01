package com.mad_lab.videocallapp

import android.Manifest
import android.Manifest.permission.RECORD_AUDIO
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaRecorder.VideoSource.CAMERA
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import kotlin.contracts.CallsInPlace

class MainActivity : AppCompatActivity() {

    val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS)
    val requestcode = 1

    lateinit var loginBtn : Button
    lateinit var userName_et : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loginBtn =findViewById(R.id.loginBtn)
        userName_et = findViewById(R.id.userName_et)

        if (!isPermissionGranted()) {
            askPermissions()
        }

        Firebase.initialize(this);

        loginBtn.setOnClickListener {
            val userName = userName_et.text.toString()

//            val sharedPreferences : SharedPreferences = this.getSharedPreferences("videocallinfo", Context.MODE_PRIVATE )
//            val editor : SharedPreferences.Editor = sharedPreferences.edit()
//            editor.putBoolean("isLogin",false)
//            editor.putString("userName","")

            val intent = Intent(this, CallActivity::class.java)
            intent.putExtra("userName", userName)
            startActivity(intent);

        }
    }

    private fun askPermissions() {
        ActivityCompat.requestPermissions(this, permissions, requestcode)
    }

    private fun isPermissionGranted(): Boolean {

        permissions.forEach {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            )
                return false;
        }

        return true;
    }
}