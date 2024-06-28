
package com.example.votree.tips

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.R
import com.example.votree.databinding.FragmentTipMainScreenBinding
import com.example.votree.tips.adapters.TipAdapter
import com.example.votree.tips.adapters.TipCarouselAdapter
import com.example.votree.tips.models.ProductTip
import com.example.votree.tips.view_models.TipsViewModel
import com.example.votree.utils.AuthHandler
import com.example.votree.utils.RoleManagement
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.android.material.carousel.HeroCarouselStrategy

interface onProductTipClickListener {
    fun onProductTipClick(productTip: ProductTip)
}

class TipMainScreenFragment : Fragment(), onProductTipClickListener {
    private val viewModel: TipsViewModel by viewModels()
    private lateinit var adapter :  TipAdapter
    private lateinit var carouselAdapter : TipCarouselAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTipMainScreenBinding.inflate(inflater, container, false)
        setupRecyclerView(binding)
        setupCarousel(binding)
        setupFabButton(binding)
        setupSortChip(binding, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.topTipList.observe(viewLifecycleOwner) { topTipList ->
            carouselAdapter.submitList(topTipList)
        }
        viewModel.tipList.observe(viewLifecycleOwner) { tipList ->
            adapter.submitList(tipList)
        }
    }

    private fun setupRecyclerView(binding: FragmentTipMainScreenBinding) {
        adapter = TipAdapter(this)
        binding.tipRecyclerView.adapter = adapter
        binding.tipRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun setupCarousel(binding: FragmentTipMainScreenBinding) {
        carouselAdapter = TipCarouselAdapter(this)
        val carouselRecyclerView = binding.carouselRecyclerView
        carouselRecyclerView.layoutManager = CarouselLayoutManager(HeroCarouselStrategy())
        carouselRecyclerView.adapter = carouselAdapter
        carouselRecyclerView.setHasFixedSize(true)
        val snapHelper = CarouselSnapHelper()
        snapHelper.attachToRecyclerView(carouselRecyclerView)
    }

    private fun setupFabButton(binding: FragmentTipMainScreenBinding) {
        RoleManagement.checkUserRole(AuthHandler.firebaseAuth) {
            if (it == "store") {
                binding.fabNavWriteTipAction.visibility = View.VISIBLE
                binding.fabNavWriteTipAction.setOnClickListener {
                    val intent = Intent(requireContext(), WriteTipActivity::class.java)
                    startActivity(intent)
                }
            } else {
                binding.fabNavWriteTipAction.visibility = View.GONE
            }
        }
    }

    private fun setupSortChip(binding: FragmentTipMainScreenBinding, savedInstanceState: Bundle?) {
        val sortChip = binding.tipsSortChip
        val popupMenu = PopupMenu(requireContext(), sortChip)
        popupMenu.menuInflater.inflate(R.menu.tip_sort_options, popupMenu.menu)

        val defaultSortDirection = savedInstanceState?.getInt("sortDirectionId") ?: TipsViewModel.SORT_BY_NEWEST
        val defaultSortText = when(defaultSortDirection){
            R.id.sort_by_newest -> "Newest first"
            R.id.sort_by_vote -> "Most voted"
            R.id.sort_by_oldest -> "Oldest first"
            else -> "Newest first"
        }
        val defaultSortDirectionIcon = when(defaultSortDirection){
            R.id.sort_by_newest -> R.drawable.newest_24px
            R.id.sort_by_vote -> R.drawable.volunteer_activism_24px
            R.id.sort_by_oldest -> R.drawable.oldest_24px
            else -> R.drawable.newest_24px
        }
        binding.tipsSortChip.chipIcon = resources.getDrawable(defaultSortDirectionIcon, context?.theme)
        binding.tipsSortChip.text = defaultSortText

        sortChip.setOnClickListener{
            Log.d("TipMainScreenFragment", "Sort chip clicked")
            popupMenu.show()
        }

        popupMenu.setOnMenuItemClickListener {
            binding.tipsSortChip.text = popupMenu.menu.findItem(it.itemId).title
            binding.tipsSortChip.chipIcon = popupMenu.menu.findItem(it.itemId).icon
            when(it.itemId){
                R.id.sort_by_newest -> {
                    viewModel.sortDirection.value = TipsViewModel.SORT_BY_NEWEST
                    savedInstanceState?.putInt("sortDirectionId", TipsViewModel.SORT_BY_NEWEST)
                }
                R.id.sort_by_vote -> {
                    viewModel.sortDirection.value = TipsViewModel.SORT_BY_VOTE
                    savedInstanceState?.putInt("sortDirectionId", TipsViewModel.SORT_BY_VOTE)
                }
                R.id.sort_by_oldest -> {
                    viewModel.sortDirection.value = TipsViewModel.SORT_BY_OLDEST
                    savedInstanceState?.putInt("sortDirectionId", TipsViewModel.SORT_BY_OLDEST)
                }
            }
            true
        }

        viewModel.sortDirection.observe(viewLifecycleOwner) { direction ->
            Log.d("TipMainScreenFragment", "Sort direction changed: $direction")
            viewModel.queryAllTips(direction)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.queryAllTips(viewModel.sortDirection.value ?: TipsViewModel.SORT_BY_NEWEST)
        viewModel.queryTopTips()
    }

    override fun onProductTipClick(productTip: ProductTip) {
        val destination = TipMainScreenFragmentDirections.actionMainTipFragmentToTipDetailFragment2(productTip)
        findNavController().navigate(destination)
    }
}