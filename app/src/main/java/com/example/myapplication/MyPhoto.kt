package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class MyPhoto : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_photo)

        val bitmap = BitmapFactory.decodeFile(intent.getStringExtra("image_file"))
        findViewById<ImageView>(R.id.ImgVw).setImageBitmap(bitmap)
    }
}