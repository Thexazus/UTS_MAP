package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import android.widget.Button
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

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

        adapter = OnboardingAdapter(layouts)
        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = adapter



        // Initialize button
        nextButton = findViewById(R.id.nextButton)

        // Set up a page change callback to handle button text change
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Log the current page
                android.util.Log.d("OnboardingActivity", "Page selected: $position")

                if (position == layouts.size - 1) {
                    nextButton.text = "Get Started"
                } else {
                    nextButton.text = "Next"
                }
            }
        })

        // Set up the next button click event
        nextButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < layouts.size - 1) {
                // Move to the next page
                viewPager.currentItem = currentItem + 1
            } else {
                // If on the last page, navigate to LoginActivity
                startActivity(Intent(this, com.example.umnstory.LoginActivity::class.java))
                finish()  // Close OnboardingActivity
            }
        }
    }
}
