package com.example.votree.users.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.votree.R
import com.example.votree.databinding.FragmentUserProfileSettingBinding
import com.example.votree.products.fragments.PasswordFragment
import com.example.votree.products.fragments.ProfileFragment
import com.example.votree.tips.AdManager
import com.google.android.gms.ads.AdView
import com.google.android.material.tabs.TabLayoutMediator

class UserProfileSettingFragment : Fragment() {
    private var _binding: FragmentUserProfileSettingBinding? = null
    private val binding get() = _binding!!

    private val profileFragment = ProfileFragment()
    private val passwordFragment = PasswordFragment()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileSettingBinding.inflate(inflater, container, false)

        val adView = binding.root.findViewById<AdView>(R.id.adView)
        AdManager.addAdView(adView, requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupNavigation()
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(profileFragment, "Profile")
        adapter.addFragment(passwordFragment, "Password")

        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.profileTl, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Profile"
                1 -> "Password"
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }.attach()
    }

    private inner class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        private val fragmentList = mutableListOf<Fragment>()
        private val fragmentTitleList = mutableListOf<String>()

        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }
    }

    private fun setupNavigation() {
        // Set the navigation icon to the back button
        binding.toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.arrow_back_24px)
        binding.toolbar.title = getString(R.string.user_profile_setting)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}