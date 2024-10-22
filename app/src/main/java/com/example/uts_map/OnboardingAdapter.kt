package com.example.uts_map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class OnboardingAdapter(private val layouts: IntArray) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        // Optionally, add a log to verify the position being loaded
        android.util.Log.d("OnboardingAdapter", "Loading page: $position")
    }

    override fun getItemCount(): Int = layouts.size

    override fun getItemViewType(position: Int): Int {
        // Return the layout resource ID for the current position
        return layouts[position]
    }

    class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}