package com.example.uts_map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.uts_map.databinding.ItemWaterIntakeBinding
import java.text.SimpleDateFormat
import java.util.*

class WaterIntakeAdapter : ListAdapter<WaterIntake, WaterIntakeAdapter.WaterIntakeViewHolder>(WaterIntakeDiffCallback()) {

    interface OnDeleteClickListener {
        fun onDeleteClick(waterIntake: WaterIntake)
    }

    private var onDeleteClickListener: OnDeleteClickListener? = null

    fun setOnDeleteClickListener(listener: OnDeleteClickListener) {
        onDeleteClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaterIntakeViewHolder {
        val binding = ItemWaterIntakeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WaterIntakeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WaterIntakeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WaterIntakeViewHolder(private val binding: ItemWaterIntakeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(waterIntake: WaterIntake) {
            // Display water intake amount
            binding.textViewIntakeAmount.text = binding.root.context.getString(R.string.amount_ml, waterIntake.amount)

            // Format dan tampilkan waktu intake
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            try {
                val date = inputFormat.parse(waterIntake.timestamp)
                binding.textViewTime.text = outputFormat.format(date!!)
            } catch (e: Exception) {
                binding.textViewTime.text = waterIntake.timestamp
            }

            // Set up delete button functionality
            binding.buttonDelete.setOnClickListener {
                onDeleteClickListener?.onDeleteClick(waterIntake)
            }
        }
    }

    class WaterIntakeDiffCallback : DiffUtil.ItemCallback<WaterIntake>() {
        override fun areItemsTheSame(oldItem: WaterIntake, newItem: WaterIntake): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WaterIntake, newItem: WaterIntake): Boolean {
            return oldItem == newItem
        }
    }
}