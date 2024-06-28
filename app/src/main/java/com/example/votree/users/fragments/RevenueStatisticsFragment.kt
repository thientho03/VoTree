package com.example.votree.users.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.votree.databinding.FragmentRevenueStatisticsBinding
import com.example.votree.users.repositories.StoreRepository
import com.example.votree.users.repositories.UserRepository
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Date
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class RevenueStatisticsFragment : Fragment()  {
    private var _binding: FragmentRevenueStatisticsBinding? = null
    private val binding get() = _binding!!
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var storeId = ""
    private val userRepository = UserRepository(FirebaseFirestore.getInstance())
    private val storeRepository = StoreRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRevenueStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupOnClick()
    }

    private fun setupView() {
        lifecycleScope.launch {
            val storeId = userRepository.getStoreId(userId)

            val ratings = storeRepository.getAverageProductRating(storeId)
            val totalOrders = storeRepository.getTotalOrders(storeId)
            val approval = storeRepository.getApproval(storeId)
            val cancellation = storeRepository.getCancellation(storeId)

            val dateString = setupDateString(LocalDate.now())
            val date = extractDateFromString(dateString)
            val revenue = storeRepository.getWeeklyRevenue(storeId, date.first, date.second)
            val weeklyOrders = storeRepository.getWeeklyTotalOrders(storeId, date.first, date.second)

            binding.ratingsTxt.text = "${ratings} stars"
            binding.approvalTxt.text = String.format("%.2f", approval/totalOrders*100) + "%"
            binding.cancellationTxt.text = String.format("%.2f", cancellation/totalOrders*100) + "%"
            binding.statisticsDateTxt.text = "Revenue: ${dateString}"
            binding.totalRevenueTxt.text = "$${revenue}"
            binding.totalOrdersTxt.text = String.format("%02d", weeklyOrders)
            setupChart(storeId, date.first, date.second)
        }
    }

    private fun setupDateString(date: LocalDate): String {
        val startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val Formatter = DateTimeFormatter.ofPattern("dd/MM")

        return "${startOfWeek.format(Formatter)} - ${endOfWeek.format(Formatter)}"
    }

    private fun setupChart(storeId: String, startDate: LocalDate, endDate: LocalDate) {
        lifecycleScope.launch {
            val values = storeRepository.getDailyRevenueList(storeId, startDate, endDate)

            val dataSet = BarDataSet(values, "Weekly Data")
            val barData = BarData(dataSet)

            binding.revenueChart.data = barData
            binding.revenueChart.description.isEnabled = false

            val xAxis = binding.revenueChart.xAxis
            xAxis.valueFormatter =
                IndexAxisValueFormatter(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)

            binding.revenueChart.axisLeft.isEnabled = false
            binding.revenueChart.axisRight.isEnabled = false

            barData.setValueTextSize(12f)
            barData.barWidth = 0.9f
            barData.setDrawValues(true)

            binding.revenueChart.setFitBars(true)
            binding.revenueChart.animateY(500)
            binding.revenueChart.invalidate()
        }
    }

    private fun setupOnClick() {
        binding.previousRevenueImg.setOnClickListener {
            lifecycleScope.launch {
                val thisMonday =
                    extractDateFromString(binding.statisticsDateTxt.text.toString()).first
                val lastSunday = thisMonday.minusDays(1)

                val storeId = userRepository.getStoreId(userId)
                val dateString = setupDateString(lastSunday)
                val date = extractDateFromString(dateString)
                val revenue = storeRepository.getWeeklyRevenue(storeId, date.first, date.second)
                val weeklyOrders = storeRepository.getWeeklyTotalOrders(storeId, date.first, date.second)

                binding.statisticsDateTxt.text = "Revenue: ${dateString}"
                binding.totalRevenueTxt.text = "$${revenue}"
                binding.totalOrdersTxt.text = String.format("%02d", weeklyOrders)
                setupChart(storeId, date.first, date.second)
            }
        }

        binding.nextRevenueImg.setOnClickListener {
            lifecycleScope.launch {
                val thisSunday = extractDateFromString(binding.statisticsDateTxt.text.toString()).second
                val nextMonday = thisSunday.plusDays(1)

                val storeId = userRepository.getStoreId(userId)
                val dateString = setupDateString(nextMonday)
                val date = extractDateFromString(dateString)
                val revenue = storeRepository.getWeeklyRevenue(storeId, date.first, date.second)
                val weeklyOrders = storeRepository.getWeeklyTotalOrders(storeId, date.first, date.second)

                if (nextMonday.isBefore(LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)))) {
                    binding.statisticsDateTxt.text = "Revenue: ${dateString}"
                    binding.totalRevenueTxt.text = "$${revenue}"
                    binding.totalOrdersTxt.text = String.format("%02d", weeklyOrders)
                    setupChart(storeId, date.first, date.second)
                }
            }
        }
    }

    private fun extractDateFromString(dateString: String): Pair<LocalDate, LocalDate> {
        val dateRegex = """(\d{2}/\d{2})""".toRegex()
        val matches = dateRegex.findAll(dateString).map { it.value }.toList()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val firstDate = LocalDate.parse(matches[0] + "/2024", formatter)
        val lastDate = LocalDate.parse(matches[1] + "/2024", formatter)

        return Pair(firstDate, lastDate)
    }
}