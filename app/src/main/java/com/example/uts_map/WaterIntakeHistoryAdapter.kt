package com.example.uts_map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WaterIntakeHistoryAdapter(
    private val historyList: MutableList<HomeFragment.WaterIntakeHistoryItem>,
    private val onDeleteClicked: (HomeFragment.WaterIntakeHistoryItem) -> Unit
) : RecyclerView.Adapter<WaterIntakeHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewIntakeAmount: TextView = view.findViewById(R.id.textViewIntakeAmount)
        val textViewTime: TextView = view.findViewById(R.id.textViewTime)
        val buttonDelete: ImageButton = view.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_water_intake, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]
        holder.textViewIntakeAmount.text = "${item.amount} ml"
        holder.textViewTime.text = item.date // Bisa format waktu spesifik jika disimpan

        // Handle delete action
        holder.buttonDelete.setOnClickListener {
            onDeleteClicked(item)
        }
    }

    override fun getItemCount(): Int = historyList.size

    // Fungsi untuk menambahkan item baru ke daftar
    fun addItem(item: HomeFragment.WaterIntakeHistoryItem) {
        historyList.add(0, item) // Menambahkan item baru di posisi paling atas
        notifyItemInserted(0) // Memberitahu adapter bahwa item baru telah ditambahkan
        notifyItemRangeChanged(0, historyList.size) // Memperbarui tampilan
    }
}