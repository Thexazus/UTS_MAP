package com.example.uts_map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class NotAchieveFragment : Fragment() {
    private lateinit var ivSadEmoji: ImageView
    private lateinit var tvStatus: TextView
    private lateinit var tvMotivationalMsg: TextView
    private lateinit var btnTryAgain: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_not_achieve, container, false)

        ivSadEmoji = view.findViewById(R.id.ivSadEmoji)
        tvStatus = view.findViewById(R.id.tvStatus)
        tvMotivationalMsg = view.findViewById(R.id.tvMotivationalMsg)
        btnTryAgain = view.findViewById(R.id.btnTryAgain)

        setupUI()

        btnTryAgain.setOnClickListener {
            parentFragmentManager.popBackStack() // Close this fragment and return to the previous one
        }

        return view
    }

    private fun setupUI() {
        ivSadEmoji.setImageResource(R.drawable.sad_emoji)
        tvStatus.text = "Oops! You missed your goal today."
        tvMotivationalMsg.text = "Don't give up! Try again tomorrow and stay hydrated!"
    }
}
