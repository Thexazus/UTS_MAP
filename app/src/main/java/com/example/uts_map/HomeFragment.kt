package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup click listener untuk icon notifikasi
        view.findViewById<ImageView>(R.id.imageViewBell).setOnClickListener {
            // Navigasi ke ReminderActivity
            val intent = Intent(requireContext(), ReminderActivity::class.java)
            startActivity(intent)
        }

        // Inisialisasi komponen UI lainnya di sini
        setupUI()
    }

    private fun setupUI() {
        // Setup komponen UI lainnya di sini
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}