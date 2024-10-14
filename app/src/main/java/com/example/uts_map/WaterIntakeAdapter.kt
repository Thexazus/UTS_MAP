package com.example.uts_map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.uts_map.databinding.ItemWaterIntakeBinding
import java.text.SimpleDateFormat
import java.util.*

class WaterIntakeAdapter(waterAmounts: List<Int>) : ListAdapter<WaterIntake, WaterIntakeAdapter.WaterIntakeViewHolder>(WaterIntakeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaterIntakeViewHolder {
        val binding = ItemWaterIntakeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WaterIntakeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WaterIntakeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class WaterIntakeViewHolder(private val binding: ItemWaterIntakeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(waterIntake: WaterIntake) {
            binding.textViewAmount.text = binding.root.context.getString(R.string.amount_ml, waterIntake.amount)
            binding.textViewTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(waterIntake.timestamp)
        }
    }

    class WaterIntakeDiffCallback : DiffUtil.ItemCallback<WaterIntake>() {
        override fun areItemsTheSame(oldItem: WaterIntake, newItem: WaterIntake): Boolean {
            return oldItem.amount == newItem.amount
        }

        override fun areContentsTheSame(oldItem: WaterIntake, newItem: WaterIntake): Boolean {
            return oldItem == newItem
        }
    }
}

// Hapus definisi WaterIntake di sini jika sudah didefinisikan di file lain