package com.example.reminderapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.viewpager.widget.ViewPager
import com.example.reminderapp.databinding.FragmentLandingBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private var _binding: FragmentLandingBinding? = null
private val binding get() = _binding!!

/**
 * A simple [Fragment] subclass.
 * Use the [LandingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LandingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var onBoardingViewPagerAdapter: OnBoardingViewPagerAdapter? = null
    private var tabLayout: TabLayout? = null
    private var onBoardingViewPager: ViewPager? = null
    private var nextBtn: TextView? = null
    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLandingBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loggedInUser = FirebaseAuth.getInstance().getCurrentUser()

        if (loggedInUser != null) {
            requireView().findNavController()!!.navigate(R.id.action_landingFragment_to_homeActivity)
            return
        }
        
        binding.tvAboutUs.setOnClickListener {
            view.findNavController().navigate(R.id.action_landingFragment_to_aboutUsActivity)
        }

        tabLayout = view.findViewById(R.id.tabLayout)

        val onBoardingData = arrayListOf<OnBoardingData>()
        onBoardingData.add(OnBoardingData("Never Forget Your Meeting", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla mauris ex, fringilla id sapien eu, sollicitudin ullamcorper quam. Praesent laoreet massa ipsum, in gravida ante malesuada ultricies.", R.drawable.onboarding1))
        onBoardingData.add(OnBoardingData("Neat and Structured ", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla mauris ex, fringilla id sapien eu, sollicitudin ullamcorper quam. Praesent laoreet massa ipsum, in gravida ante malesuada ultricies.", R.drawable.onboarding2))
        onBoardingData.add(OnBoardingData("Let's go!", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla mauris ex, fringilla id sapien eu, sollicitudin ullamcorper quam. Praesent laoreet massa ipsum, in gravida ante malesuada ultricies.", R.drawable.onboarding3))

        setOnBoardingViewPagerAdapter(onBoardingData)

        position = onBoardingViewPager!!.currentItem
        nextBtn = view.findViewById(R.id.tvNext)
        nextBtn?.setOnClickListener {
            if (position < onBoardingData.size) {
                position++
                onBoardingViewPager!!.currentItem = position
            }
        }

        tabLayout!!.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                position =  tab!!.position
                if (tab.position == onBoardingData.size - 1) {
                    nextBtn!!.text = "Register Now"
                    binding.tvNext.setOnClickListener {
                        view.findNavController().navigate(R.id.action_landingFragment_to_registerFragment)
                    }
                } else {
                    nextBtn!!.text = "Next"
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

    }

    private fun setOnBoardingViewPagerAdapter(onBoardingData: List<OnBoardingData>) {
        onBoardingViewPager = view?.findViewById(R.id.onboardingPager)
        onBoardingViewPagerAdapter = OnBoardingViewPagerAdapter(requireActivity().applicationContext, onBoardingData)
        onBoardingViewPager!!.adapter = onBoardingViewPagerAdapter
        tabLayout?.setupWithViewPager(onBoardingViewPager)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LandingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LandingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}