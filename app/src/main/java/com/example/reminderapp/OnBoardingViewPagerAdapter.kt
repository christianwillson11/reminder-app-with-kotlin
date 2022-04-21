package com.example.reminderapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter

    class OnBoardingViewPagerAdapter(private var context: Context, private var onBoardingDataList: List<OnBoardingData>): PagerAdapter() {
    override fun getCount(): Int {
        return onBoardingDataList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)

    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View = LayoutInflater.from(context).inflate(R.layout.onboarding_screen_layout, null)
        val imageView: ImageView
        val title: TextView
        val desc: TextView

        imageView = view.findViewById(R.id.imageViewOnboarding)
        title = view.findViewById(R.id.tvTitle)
        desc = view.findViewById(R.id.tvDescription)

        imageView.setImageResource(onBoardingDataList[position].image)
        title.text = onBoardingDataList[position].title
        desc.text = onBoardingDataList[position].description

        container.addView(view)

        return view
    }
}