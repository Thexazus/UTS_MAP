package com.example.uts_map

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class AchieveDayGoalFragment : Fragment() {
    private lateinit var ivEmoji: ImageView
    private lateinit var tvDate: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var tvStatus: TextView
    private lateinit var ivConfetti: ImageView
    private lateinit var tvAchievementMsg: TextView
    private lateinit var tvMotivationalMsg: TextView
    private lateinit var ivTrophy: ImageView
    private lateinit var btnHome: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_achieve_day_goal, container, false)

        initializeViews(view)
        setupDate()
        setupGreeting()
        displayAchievement()

        return view
    }

    private fun initializeViews(view: View) {
        ivEmoji = view.findViewById(R.id.ivEmoji)
        tvDate = view.findViewById(R.id.tvDate)
        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvStatus = view.findViewById(R.id.tvStatus)
        ivConfetti = view.findViewById(R.id.ivConfetti)
        tvAchievementMsg = view.findViewById(R.id.tvAchievementMsg)
        tvMotivationalMsg = view.findViewById(R.id.tvMotivationalMsg)
        ivTrophy = view.findViewById(R.id.ivTrophy)
        btnHome = view.findViewById(R.id.btnHome)

        btnHome.setOnClickListener {
            parentFragmentManager.popBackStack() // Close this fragment and return to the previous one
        }
    }

    private fun setupDate() {
        val dateFormat = SimpleDateFormat("dd MMMM", Locale.getDefault())
        val currentDate = dateFormat.format(Calendar.getInstance().time)
        tvDate.text = "Today - $currentDate"
    }

    private fun setupGreeting() {
        // Replace with actual user name retrieval
        val userName = "Mesaya"
        tvGreeting.text = "Hi, $userName."
    }

    private fun displayAchievement() {
        ivEmoji.setImageResource(R.drawable.happy_emoji)
        tvStatus.text = "Congratulations!"
        ivConfetti.visibility = View.VISIBLE
        tvAchievementMsg.text = "User has achieved your goal today"
        tvMotivationalMsg.text = "You can get everything in life you want\nif you will just help enough other\npeople get what they want."
        ivTrophy.setImageResource(R.drawable.trophy_gold)
        startAnimations()
    }

    private fun startAnimations() {
        val emojiScale = ObjectAnimator.ofFloat(ivEmoji, View.SCALE_X, 0f, 1f)
        val emojiScaleY = ObjectAnimator.ofFloat(ivEmoji, View.SCALE_Y, 0f, 1f)

        val statusAlpha = ObjectAnimator.ofFloat(tvStatus, View.ALPHA, 0f, 1f)
        val statusTranslateY = ObjectAnimator.ofFloat(tvStatus, View.TRANSLATION_Y, 50f, 0f)

        val achievementAlpha = ObjectAnimator.ofFloat(tvAchievementMsg, View.ALPHA, 0f, 1f)
        val achievementTranslateY = ObjectAnimator.ofFloat(tvAchievementMsg, View.TRANSLATION_Y, 50f, 0f)

        val motivationalAlpha = ObjectAnimator.ofFloat(tvMotivationalMsg, View.ALPHA, 0f, 1f)
        val motivationalTranslateY = ObjectAnimator.ofFloat(tvMotivationalMsg, View.TRANSLATION_Y, 50f, 0f)

        val trophyScale = ObjectAnimator.ofFloat(ivTrophy, View.SCALE_X, 0f, 1f)
        val trophyScaleY = ObjectAnimator.ofFloat(ivTrophy, View.SCALE_Y, 0f, 1f)

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

        ObjectAnimator.ofFloat(ivTrophy, View.ROTATION, 0f, 20f, -20f, 10f, -10f, 0f).apply {
            duration = 1000
            startDelay = 500
            start()
        }

        ObjectAnimator.ofFloat(ivConfetti, View.ALPHA, 0f, 1f).apply {
            duration = 500
            start()
        }
    }
}
