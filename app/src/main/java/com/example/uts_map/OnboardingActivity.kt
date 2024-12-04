package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import android.widget.Button

class OnboardingActivity : AppCompatActivity() {
    private lateinit var adapter: OnboardingAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var nextButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Array layout onboarding
        val layouts = intArrayOf(
            R.layout.onboarding1,  // Halaman 1
            R.layout.onboarding2,  // Halaman 2
            R.layout.onboarding3   // Halaman 3
        )

        // Initialize ViewPager2 and adapter
        adapter = OnboardingAdapter(layouts)
        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = adapter

        // Initialize button
        nextButton = findViewById(R.id.nextButton)

        // Set up ViewPager2 page change callback
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == layouts.size - 1) {
                    nextButton.text = "GET STARTED"
                } else {
                    nextButton.text = "NEXT"
                }
            }
        })

        // Set up next button click listener
        nextButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < layouts.size - 1) {
                // Move to next page
                viewPager.currentItem = currentItem + 1
            } else {
                // Navigate to LoginActivity on the last page
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}