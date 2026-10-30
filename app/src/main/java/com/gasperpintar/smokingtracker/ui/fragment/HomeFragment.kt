package com.gasperpintar.smokingtracker.ui.fragment

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.viewmodel.HomeViewModel
import com.gasperpintar.smokingtracker.databinding.FragmentHomeBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.utils.ElapsedTime
import com.gasperpintar.smokingtracker.utils.ElapsedTimePreferences
import com.gasperpintar.smokingtracker.utils.TimeHelper
import com.gasperpintar.smokingtracker.utils.WidgetHelper
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class HomeFragment : Base<FragmentHomeBinding>(bindingInflater = FragmentHomeBinding::inflate) {
    private val viewModel: HomeViewModel by viewModels {
        ModelFactory(container = (requireActivity().application as Application).container)
    }
    private val elapsedTimePreferences by lazy { ElapsedTimePreferences(requireContext()) }
    private var lastEntry: HistoryEntity? = null
    private var dayEndMinutes = 0
    private var currentDay: LocalDate? = null
    private var thresholdMinutes = ElapsedTime.DEFAULT_THRESHOLD_MINUTES
    private var refreshJob: Job? = null

    @Override
    override fun initialize() {
        binding.buttonAddCigarette.setOnClickListener { viewModel.insertNow() }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.historyChanges.collect { refreshHome() } }
                launch {
                    viewModel.isAdding.collect { isAdding -> binding.buttonAddCigarette.isEnabled = !isAdding }
                }
                launch {
                    viewModel.addEvents.collect { success ->
                        if (success) {
                            WidgetHelper.updateAllWidgets(context = requireContext())
                            Snackbar.make(binding.root, R.string.home_add_success, Snackbar.LENGTH_SHORT).show()
                        } else {
                            Snackbar.make(binding.root, R.string.home_add_error, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (isActive) {
                    val now = LocalDateTime.now()
                    if (currentDay != null && currentDay != TimeHelper.dayDate(now, dayEndMinutes)) refreshHome()
                    updateElapsedTime(now)
                    delay(1_000)
                }
            }
        }
    }

    @Override
    override fun onResume() {
        super.onResume()
        thresholdMinutes = elapsedTimePreferences.getThresholdMinutes()
        refreshHome()
    }

    private fun refreshHome() {
        if (refreshJob?.isActive == true || view == null) return
        refreshJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val state = viewModel.getMinimalState()
                lastEntry = state.lastEntry
                dayEndMinutes = state.dayEndMinutes
                currentDay = TimeHelper.dayDate(LocalDateTime.now(), dayEndMinutes)
                binding.todayCount.text = state.smokedToday.toString()
                updateElapsedTime(LocalDateTime.now())
                showContent()
            } catch (_: Exception) {
                if (view != null) Snackbar.make(binding.root, R.string.home_load_error, Snackbar.LENGTH_LONG).show()
            } finally {
                refreshJob = null
            }
        }
    }

    private fun updateElapsedTime(now: LocalDateTime) {
        val duration = ElapsedTime.durationSince(lastSmoked = lastEntry?.createdAt, now = now)
        binding.timerLabel.text = duration?.let(ElapsedTime::format) ?: getString(R.string.home_no_smoked_entries)
        binding.timerLabel.setTextColor(
            if (ElapsedTime.isBelowThreshold(duration, thresholdMinutes)) {
                ContextCompat.getColor(requireContext(), R.color.home_timer_warning)
            } else {
                MaterialColors.getColor(binding.timerLabel, com.google.android.material.R.attr.colorOnSurface)
            }
        )
    }
}
