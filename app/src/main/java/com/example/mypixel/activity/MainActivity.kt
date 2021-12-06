package com.example.mypixel.activity

import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.mypixel.R
import com.example.mypixel.camera.view.CameraPreview
import me.relex.circleindicator.CircleIndicator3

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var mIndicator: CircleIndicator3
    private var numPage = 3
    val bgColors: MutableList<String> = mutableListOf(
            "#638596",
            "#638596",
            "#95554f",
    )
    val pixelList: MutableList<Int> = mutableListOf(
            R.drawable.pixel1,
            R.drawable.pixel2,
            R.drawable.pixel3,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager_id)

        numPage = bgColors.size

        mIndicator = findViewById(R.id.indicator)
        mIndicator.setViewPager(viewPager)
        mIndicator.createIndicators(bgColors.size, 0)

        val mAdapter = CustomPagerAdapter()
        viewPager.adapter = mAdapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                mIndicator.animatePageSelected(position)
            }
        })
    }

    inner class CustomPagerAdapter : RecyclerView.Adapter<CustomPagerAdapter.MyPagerViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPagerViewHolder { //ViewHolder 새로 만들 때 호출
            val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.page_layout1, parent, false)
            return MyPagerViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyPagerViewHolder, position: Int) { //데이터와 연결
            holder.itemView.requestLayout()
            //val lp = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 100)
            holder.bind(bgColors[position], position)
        }

        override fun getItemCount(): Int { //데이터 세트 크기
            return numPage
        }

        inner class MyPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val pixelImage: ImageView = itemView.findViewById<ImageView>(R.id.imageView)
            val display = windowManager.defaultDisplay

            val size = Point()

            fun bind(bgColor: String, position: Int) {
                display.getRealSize(size)
                val widthPixels = resources.displayMetrics.widthPixels
                val heightPixels = resources.displayMetrics.heightPixels
                pixelImage.maxHeight = (heightPixels - (widthPixels / 3 * 3))
                pixelImage.setImageResource(pixelList[position])
                itemView.setBackgroundColor(Color.parseColor(bgColor))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupCameraPreview()
    }

    private fun setupCameraPreview() {
        val cameraPreview = findViewById<CameraPreview>(R.id.camera_preview)
        cameraPreview.setLifecycleOwner(this)
        cameraPreview.setupCamera()
    }
}