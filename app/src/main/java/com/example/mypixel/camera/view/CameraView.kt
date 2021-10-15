package com.example.mypixel.camera.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class CameraView : FrameLayout {
    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
    }
}