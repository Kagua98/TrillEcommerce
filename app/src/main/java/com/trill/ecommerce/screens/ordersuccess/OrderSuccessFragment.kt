package com.trill.ecommerce.screens.ordersuccess

import android.content.ContentValues
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.trill.ecommerce.R
import com.trill.ecommerce.databinding.FragmentOrderSuccessBinding
import com.trill.ecommerce.screens.account.contactsupport.ContactsModalFragment
import com.trill.ecommerce.util.ui.LoadingFragment
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


class OrderSuccessFragment : Fragment() {

    private var _binding: FragmentOrderSuccessBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingFragmentHelper: LoadingFragment.LoadingFragmentHelper

    private lateinit var auth: FirebaseAuth

    private var timeText: TextView? = null
    private var addressText: TextView? = null
    private var progressBar: LinearProgressIndicator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrderSuccessBinding.inflate(inflater, container, false)
        val root: View = binding.root

        onBackButtonPressed(root)

        loadingFragmentHelper =
            LoadingFragment.LoadingFragmentHelper(requireActivity().supportFragmentManager)

        // Initialize Firebase Auth
        auth = Firebase.auth

        initViews(root)


        return root
    }

    private fun initViews(root: View) {
        timeText = root.findViewById(R.id.etaTime)
        addressText = root.findViewById(R.id.addressText)
        progressBar = root.findViewById(R.id.progressBar)


        val countDownTimer: CountDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                progressBar!!.progress = abs(millisUntilFinished.toInt() / 100 - 100)
            }

            override fun onFinish() {}
        }
        countDownTimer.start()

        setETATime(root)

        binding.buttonBackToMenu.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.route_to_menu_fragment)
        }

        binding.buttonBackToHome.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.route_to_home_fragment)
        }

        val callButton: View? = root.findViewById(R.id.buttonPhone)
        callButton!!.setOnClickListener {
            val contactsModal = ContactsModalFragment.getInstance()
            contactsModal.show(requireActivity().supportFragmentManager, "ContactsModalFragment")
        }


    }

    private fun setETATime(root: View) {
        val date = Date()
        val simpleDateFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault())

        val currentTime = simpleDateFormat.format(date)

        timeText!!.text = currentTime.toString()


    }


    private fun checkTimeOfDay(): String {
        val calendar = Calendar.getInstance()
        val timeOfDay = calendar[Calendar.AM_PM]
        return if (timeOfDay == 1) {
            "PM"
        } else
            "AM"
    }

    private fun onBackButtonPressed(root: View?) {
        val backPressedButton: ImageView = root!!.findViewById(R.id.backPressed)
        backPressedButton.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.route_to_home_fragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showLoading(isLoading: Boolean) {
        // binding.buttonPrimary.isEnabled = !isLoading
        loadingFragmentHelper.showLoading(isLoading)
    }
}