package com.example.uts_map

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import com.example.uts_map.R
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class AchievementActivity : AppCompatActivity() {
    private lateinit var ivEmoji: ImageView
    private lateinit var tvDate: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvMessage: TextView
    private lateinit var ivTrophy: ImageView
    private lateinit var btnHome: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievement)

        initializeViews()
        setupDate()
        checkAndDisplayAchievement()
        checkForMidnightAnimation()
    }

    private fun initializeViews() {
        ivEmoji = findViewById(R.id.ivEmoji)
        tvDate = findViewById(R.id.tvDate)
        tvStatus = findViewById(R.id.tvStatus)
        tvMessage = findViewById(R.id.tvMessage)
        ivTrophy = findViewById(R.id.ivTrophy)
        btnHome = findViewById(R.id.btnHome)

        // Navigasi ke HomeFragment
        btnHome.setOnClickListener {
            // Navigate to HomeFragment
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("openHome", true)  // Intent untuk membuka HomeFragment
            startActivity(intent)
            finish()
        }
    }

    private fun setupDate() {
        val dateFormat = SimpleDateFormat("dd MMMM", Locale.getDefault())
        val currentDate = dateFormat.format(Calendar.getInstance().time)
        tvDate.text = "Today - $currentDate"
    }

    private fun checkAndDisplayAchievement() {
        val prefs = getSharedPreferences("WaterTracker", MODE_PRIVATE)
        val currentAmount = prefs.getFloat("todayAmount", 0f)
        val targetAmount = 2000f

        val isAchieved = currentAmount >= targetAmount

        updateUI(isAchieved)
        startAnimations(isAchieved)
    }

    private fun updateUI(isAchieved: Boolean) {
        if (isAchieved) {
            ivEmoji.setImageResource(R.drawable.happy_emoji)
            tvStatus.text = "Congratulations!"
            tvMessage.text = "You can get everything in life you want\nif you just help enough other\npeople get what they want."
            ivTrophy.setImageResource(R.drawable.trophy_gold)
        } else {
            ivEmoji.setImageResource(R.drawable.sad_emoji)
            tvStatus.text = "Oops!"
            tvMessage.text = "Success is not final, failure is not fatal.\nIt is the courage to continue that counts."
            ivTrophy.setImageResource(R.drawable.trophy_gold)
        }
    }

    private fun startAnimations(isAchieved: Boolean) {
        val emojiScale = ObjectAnimator.ofFloat(ivEmoji, View.SCALE_X, 0f, 1f)
        val emojiScaleY = ObjectAnimator.ofFloat(ivEmoji, View.SCALE_Y, 0f, 1f)

        val statusAlpha = ObjectAnimator.ofFloat(tvStatus, View.ALPHA, 0f, 1f)
        val statusTranslateY = ObjectAnimator.ofFloat(tvStatus, View.TRANSLATION_Y, 50f, 0f)

        val messageAlpha = ObjectAnimator.ofFloat(tvMessage, View.ALPHA, 0f, 1f)
        val messageTranslateY = ObjectAnimator.ofFloat(tvMessage, View.TRANSLATION_Y, 50f, 0f)

        val trophyScale = ObjectAnimator.ofFloat(ivTrophy, View.SCALE_X, 0f, 1f)
        val trophyScaleY = ObjectAnimator.ofFloat(ivTrophy, View.SCALE_Y, 0f, 1f)

        val buttonAlpha = ObjectAnimator.ofFloat(btnHome, View.ALPHA, 0f, 1f)
        val buttonTranslateY = ObjectAnimator.ofFloat(btnHome, View.TRANSLATION_Y, 50f, 0f)

        AnimatorSet().apply {
            playTogether(
                emojiScale, emojiScaleY,
                statusAlpha, statusTranslateY,
                messageAlpha, messageTranslateY,
                trophyScale, trophyScaleY,
                buttonAlpha, buttonTranslateY
            )
            duration = 1000
            interpolator = OvershootInterpolator()
            start()
        }

        if (isAchieved) {
            ObjectAnimator.ofFloat(ivTrophy, View.ROTATION, 0f, 20f, -20f, 10f, -10f, 0f).apply {
                duration = 1000
                startDelay = 500
                start()
            }
        }
    }

    private fun checkForMidnightAnimation() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        if (hour == 0 && minute == 0) {
            startMidnightSlideInAnimation()
        }
    }

    private fun startMidnightSlideInAnimation() {
        val slideIn = ObjectAnimator.ofFloat(tvDate, View.TRANSLATION_X, -1000f, 0f).apply {
            duration = 1500
        }
        slideIn.start()
    }
}
