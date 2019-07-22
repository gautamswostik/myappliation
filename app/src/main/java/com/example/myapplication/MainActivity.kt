package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.log.logcat
import io.fotoapparat.log.loggers
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.back
import io.fotoapparat.selector.front
import io.fotoapparat.selector.off
import io.fotoapparat.selector.torch
import io.fotoapparat.view.CameraView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {
    val permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
    val current = LocalDateTime.now()
    val fileName = "$current.png"
    val sd = Environment.getExternalStorageDirectory()
    val dest = File(sd , fileName)

    var fotoapparatState : FotoapparatState? = null
    var cameraStatus : CameraState? = null
    var flashState: FlashState? = null

    var fotoapparat : Fotoapparat? = null

    private fun createFotoapparat(){
        val cameraView = findViewById<CameraView>(R.id.cam_view)

        fotoapparat = Fotoapparat(
            context = this ,
            view = cameraView ,
            scaleType = ScaleType.CenterCrop,
            lensPosition = back(),
            logger = loggers(
                logcat()
            ),
            cameraErrorCallback = { error->
                println("Recorder errors : $error")
            }
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createFotoapparat()

        cameraStatus = CameraState.BACK

        Cam_capture.setOnClickListener {
            takePhoto()


        }

        Cam_flash.setOnClickListener{
            changeFlashState()
        }
        Cam_switch.setOnClickListener {
            switchCam()
        }

    }

    private fun takePhoto(){
        if(hasNoPermissions()){
            requestPermission()
        }
        else{
            fotoapparat
                ?.takePicture()
                ?.saveToFile(dest)
        }

        val int1 = Intent(this , MyPhoto::class.java)
        int1.putExtra("image_file", dest.absoluteFile)
        startActivity(int1)
    }

    private fun switchCam(){
        fotoapparat?.switchTo(
            lensPosition = if(cameraStatus == CameraState.BACK) front() else back(),
            cameraConfiguration = CameraConfiguration()
        )
            if(cameraStatus == CameraState.BACK) cameraStatus = CameraState.FRONT
            else cameraStatus = CameraState.BACK
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()

        println("Onstart")

        if (hasNoPermissions()) {
            requestPermission()
        }else{
            fotoapparat?.start()
            fotoapparatState = FotoapparatState.ON
        }
    }

    private fun changeFlashState(){
        fotoapparat?.updateConfiguration(
            CameraConfiguration(
                flashMode = if(flashState == FlashState.TORCH) off()
                            else torch()
            )
        )
            if(flashState == FlashState.TORCH) flashState = FlashState.OFF
            else flashState = FlashState.TORCH
    }

    private fun hasNoPermissions(): Boolean{
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(){
        ActivityCompat.requestPermissions(this, permissions,0)
    }
    override fun onStop() {
        super.onStop()
        fotoapparat?.stop()
        FotoapparatState.OFF
    }

    override fun onPause() {
        super.onPause()
        println("OnPause")
    }

    override fun onResume() {
        super.onResume()
        println("OnResume")

        println(fotoapparatState)

        if(!hasNoPermissions() && fotoapparatState == FotoapparatState.OFF){
            val intent = Intent(baseContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}

enum class CameraState{
    FRONT, BACK
}

enum class FlashState{
    TORCH, OFF
}

enum class FotoapparatState{
    ON, OFF
}

