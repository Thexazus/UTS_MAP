package com.example.uts_map

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uts_map.databinding.FragmentHomeBinding
import com.example.uts_map.viewmodel.WaterViewModel
import java.util.*

class Home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: WaterViewModel
    private lateinit var adapter: WaterIntakeAdapter
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var recyclerViewWaterAmount: RecyclerView
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
        setupBottomNavigation()
        loadDataFromDatabase()
    }

    private fun setupRecyclerView() {
        recyclerViewWaterAmount = binding.recyclerViewWaterAmount
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewWaterAmount.layoutManager = layoutManager

        adapter = WaterIntakeAdapter()
        recyclerViewWaterAmount.adapter = adapter

        recyclerViewWaterAmount.postDelayed({
            recyclerViewWaterAmount.smoothScrollToPosition(1)
        }, 100)

        recyclerViewWaterAmount.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
            val screenCenter = recyclerViewWaterAmount.width / 2
            val distanceFromCenter = Math.abs(screenCenter - childCenter)
            val scale = Math.max(0.8f, 1 - distanceFromCenter.toFloat() / screenCenter)
            child?.scaleX = scale
            child?.scaleY = scale
            child?.alpha = scale
        }
    }

    private fun setupUI() {
        binding.apply {
            buttonDecrease.setOnClickListener { viewModel.adjustWaterAmount(-50) }
            buttonIncrease.setOnClickListener { viewModel.adjustWaterAmount(50) }
            buttonDrinkNow.setOnClickListener {
                viewModel.addWater()
                updateHistory()
            }

            textViewGreeting.text = getString(R.string.greeting, "Mesaya")
            textViewToday.text = getString(R.string.today_date, AppUtils.getCurrentDate())
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(WaterViewModel::class.java)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.currentIntake.observe(viewLifecycleOwner) { intake ->
            binding.apply {
                val goal = viewModel.goal.value ?: 2000
                val progress = (intake.toFloat() / goal * 100).toInt()
                animateProgress(progress)
                textViewProgress.text = getString(R.string.progress_percentage, progress)
                textViewCurrentIntake.text = getString(R.string.current_intake, intake)

                databaseHelper.addOrUpdateWaterIntake(AppUtils.getCurrentDate(), intake, goal)
            }
        }


    }

    private fun animateProgress(newProgress: Int) {
        val animator = ObjectAnimator.ofInt(binding.progressCircular, "progress",
            binding.progressCircular.progress, newProgress)
        animator.duration = 500 // Duration in milliseconds
        animator.start()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
