package com.example.uts_map

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
            // Navigasi ke ReminderFragment
            val reminderFragment = ReminderFragment.newInstance()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, reminderFragment) // Pastikan ID container sesuai
                .addToBackStack(null)  // Menambahkan ke back stack agar bisa kembali
                .commit()
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