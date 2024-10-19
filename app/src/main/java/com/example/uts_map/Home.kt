package com.example.uts_map

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uts_map.databinding.FragmentHomeBinding
import com.example.uts_map.viewmodel.WaterViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class Home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: WaterViewModel
    private lateinit var adapter: WaterIntakeAdapter
    private lateinit var recyclerViewHistory: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private val waterAmounts = listOf(200, 50, 550, 100, 600, 500, 400, 300)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupUI()
        setupViewModel()
        loadDataFromDatabase()
    }

    private fun setupRecyclerView() {
        recyclerViewHistory = binding.recyclerViewHistory
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewHistory.layoutManager = layoutManager

        adapter = WaterIntakeAdapter()
        recyclerViewHistory.adapter = adapter

        recyclerViewHistory.postDelayed({
            recyclerViewHistory.smoothScrollToPosition(1)
        }, 100)

        recyclerViewHistory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                updateItemAppearance()
            }
        })
    }

    private fun updateItemAppearance() {
        for (i in 0 until layoutManager.childCount) {
            val child = layoutManager.getChildAt(i)
            val childCenter = (child?.left ?: 0) + (child?.width ?: 0) / 2
            val screenCenter = recyclerViewHistory.width / 2
            val distanceFromCenter = Math.abs(screenCenter - childCenter)
            val scale = Math.max(0.8f, 1 - distanceFromCenter.toFloat() / screenCenter)
            child?.scaleX = scale
            child?.scaleY = scale
            child?.alpha = scale
        }
    }

    private fun setupUI() {
        binding.apply {
            buttonMinus.setOnClickListener { showRemoveQuantityDialog() }
            buttonPlus.setOnClickListener { showInputWaterIntakeDialog() }
            buttonDrinkNow.setOnClickListener {
                viewModel.addWater(200) // Jumlah default atau sesuaikan sesuai kebutuhan
                updateHistory()
            }
            imageViewSync.setOnClickListener { syncToGoogleDrive() }

            textViewGreeting.text = getString(R.string.greeting, "Mesaya")
            textViewToday.text = getString(R.string.today_date, AppUtils.getCurrentDate())
        }
    }

    private fun showRemoveQuantityDialog() {
        val items = waterAmounts.map { "$it ml" }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remove Water Intake")
            .setItems(items) { dialog, which ->
                val amount = waterAmounts[which]
                viewModel.removeWater(amount)
                updateHistory()
                Snackbar.make(binding.root, "$amount ml removed", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showInputWaterIntakeDialog() {
        val items = waterAmounts.map { "$it ml" }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Water Intake")
            .setItems(items) { dialog, which ->
                val amount = waterAmounts[which]
                viewModel.addWater(amount)
                updateHistory()
                Snackbar.make(binding.root, "$amount ml added", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateHistory() {
        viewModel.intakeHistory.observe(viewLifecycleOwner) { history -> // Ganti dari waterIntakeHistory ke intakeHistory
            adapter.submitList(history)
        }
    }

    private fun syncToGoogleDrive() {
        Snackbar.make(binding.root, "Syncing to Google Drive...", Snackbar.LENGTH_SHORT).show()
        // Tambahkan logika sinkronisasi Google Drive Anda di sini
    }

    private fun setupViewModel() {
        // Inisialisasi DatabaseHelper
        val databaseHelper = DatabaseHelper(requireContext())
        viewModel = ViewModelProvider(this).get(WaterViewModel::class.java) // Menggunakan constructor default
        viewModel.init(databaseHelper) // Inisialisasi DatabaseHelper di WaterViewModel
    }

    private fun loadDataFromDatabase() {
        viewModel.loadWaterIntakeDataFromDatabase()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
