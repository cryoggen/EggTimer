package com.cryoggen.eggtimer.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.cryoggen.eggtimer.databinding.FragmentScreenSlidePageBinding
import com.cryoggen.eggtimer.R


class ScreenSlidePageFragment(private var eggNum: Int, private val viewPager: ViewPager2) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentScreenSlidePageBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_screen_slide_page, container, false
        )

        val application = requireNotNull(this.activity).application
        val viewModelFactory = EggTimerViewModelFactory(eggNum, application)
        val viewModel =
            ViewModelProvider(
                this, viewModelFactory
            )[EggTimerViewModel::class.java]
        binding.eggTimerViewModel = viewModel
        binding.lifecycleOwner = this.viewLifecycleOwner



        binding.eggNameTextView.text = viewModel.eggs[eggNum].name
        binding.imageEgg.setImageResource(viewModel.eggs[eggNum].picture)

        viewModel.fixTheCurrentEggPage.observe(viewLifecycleOwner, {
            viewPager.isUserInputEnabled = it != false
        })

        return binding.root
    }



}