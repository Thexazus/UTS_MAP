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
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class AchievementActivity : AppCompatActivity() {
    private lateinit var ivEmoji: ImageView
    private lateinit var tvDate: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var tvStatus: TextView
    private lateinit var ivConfetti: ImageView
    private lateinit var tvAchievementMsg: TextView
    private lateinit var tvMotivationalMsg: TextView
    private lateinit var ivTrophy: ImageView
    private lateinit var btnHome: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievement)

        initializeViews()
        setupDate()
        setupGreeting()
        checkAndDisplayAchievement()
        checkForMidnightAnimation()
    }

    private fun initializeViews() {
        ivEmoji = findViewById(R.id.ivEmoji)
        tvDate = findViewById(R.id.tvDate)
        tvGreeting = findViewById(R.id.tvGreeting)
        tvStatus = findViewById(R.id.tvStatus)
        ivConfetti = findViewById(R.id.ivConfetti)
        tvAchievementMsg = findViewById(R.id.tvAchievementMsg)
        tvMotivationalMsg = findViewById(R.id.tvMotivationalMsg)
        ivTrophy = findViewById(R.id.ivTrophy)
        btnHome = findViewById(R.id.btnHome)

        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("openHome", true)
            startActivity(intent)
            finish()
        }
    }

    private fun setupDate() {
        val dateFormat = SimpleDateFormat("dd MMMM", Locale.getDefault())
        val currentDate = dateFormat.format(Calendar.getInstance().time)
        tvDate.text = "Today - $currentDate"
    }

    private fun setupGreeting() {
        // You might want to get the user's name from SharedPreferences or Intent
        val userName = "Mesaya" // Replace with actual user name retrieval
        tvGreeting.text = "Hi, $userName."
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
            ivConfetti.visibility = View.VISIBLE
            tvAchievementMsg.text = "Mesaya has achieve your goal today"
            tvMotivationalMsg.text = "You can get everything in life you want\nif you will just help enough other\npeople get what they want."
            ivTrophy.setImageResource(R.drawable.trophy_gold)
        } else {
            ivEmoji.setImageResource(R.drawable.sad_emoji)
            tvStatus.text = "Oops!"
            ivConfetti.visibility = View.GONE
            tvAchievementMsg.text = "Mesaya hasn't achieved your goal today"
            tvMotivationalMsg.text = "Success is not final, failure is not fatal.\nIt is the courage to continue that counts."
            ivTrophy.setImageResource(R.drawable.trophy_gold)
        }
    }

    private fun startAnimations(isAchieved: Boolean) {
        // Emoji animation
        val emojiScale = ObjectAnimator.ofFloat(ivEmoji, View.SCALE_X, 0f, 1f)
        val emojiScaleY = ObjectAnimator.ofFloat(ivEmoji, View.SCALE_Y, 0f, 1f)

        // Status container animations
        val statusAlpha = ObjectAnimator.ofFloat(tvStatus, View.ALPHA, 0f, 1f)
        val statusTranslateY = ObjectAnimator.ofFloat(tvStatus, View.TRANSLATION_Y, 50f, 0f)

        // Achievement message animations
        val achievementAlpha = ObjectAnimator.ofFloat(tvAchievementMsg, View.ALPHA, 0f, 1f)
        val achievementTranslateY = ObjectAnimator.ofFloat(tvAchievementMsg, View.TRANSLATION_Y, 50f, 0f)

        // Motivational message animations
        val motivationalAlpha = ObjectAnimator.ofFloat(tvMotivationalMsg, View.ALPHA, 0f, 1f)
        val motivationalTranslateY = ObjectAnimator.ofFloat(tvMotivationalMsg, View.TRANSLATION_Y, 50f, 0f)

        // Trophy animations
        val trophyScale = ObjectAnimator.ofFloat(ivTrophy, View.SCALE_X, 0f, 1f)
        val trophyScaleY = ObjectAnimator.ofFloat(ivTrophy, View.SCALE_Y, 0f, 1f)

        // Button animations
        val buttonAlpha = ObjectAnimator.ofFloat(btnHome, View.ALPHA, 0f, 1f)
        val buttonTranslateY = ObjectAnimator.ofFloat(btnHome, View.TRANSLATION_Y, 50f, 0f)

        AnimatorSet().apply {
            playTogether(
                emojiScale, emojiScaleY,
                statusAlpha, statusTranslateY,
                achievementAlpha, achievementTranslateY,
                motivationalAlpha, motivationalTranslateY,
                trophyScale, trophyScaleY,
                buttonAlpha, buttonTranslateY
            )
            duration = 1000
            interpolator = OvershootInterpolator()
            start()
        }

        if (isAchieved) {
            // Trophy wobble animation
            ObjectAnimator.ofFloat(ivTrophy, View.ROTATION, 0f, 20f, -20f, 10f, -10f, 0f).apply {
                duration = 1000
                startDelay = 500
                start()
            }

            // Confetti animation
            if (ivConfetti.visibility == View.VISIBLE) {
                ObjectAnimator.ofFloat(ivConfetti, View.ALPHA, 0f, 1f).apply {
                    duration = 500
                    start()
                }
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