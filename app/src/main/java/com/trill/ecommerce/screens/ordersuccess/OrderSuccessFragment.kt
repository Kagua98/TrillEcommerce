package com.trill.ecommerce.screens.ordersuccess

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.Navigation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.trill.ecommerce.R
import com.trill.ecommerce.databinding.FragmentOrderSuccessBinding
import com.trill.ecommerce.notifications.channelId
import com.trill.ecommerce.notifications.channelName
import com.trill.ecommerce.screens.HomeActivity
import com.trill.ecommerce.screens.account.contactsupport.ContactsModalFragment
import com.trill.ecommerce.util.Common
import com.trill.ecommerce.util.ui.LoadingFragment
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class OrderSuccessFragment : Fragment() {

    private var _binding: FragmentOrderSuccessBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingFragmentHelper: LoadingFragment.LoadingFragmentHelper

    private lateinit var auth: FirebaseAuth

    private var timeText: TextView? = null
    private var dateText: TextView? = null
    private var addressText: TextView? = null
    private var progressBar: LinearProgressIndicator? = null

    private var konfettiView: KonfettiView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sendNotification()
        hideBottomNavView()
    }

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

        handleOnBackPressed(root)


        return root
    }

    private fun handleOnBackPressed(root: View) {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Navigation.findNavController(root).navigate(R.id.route_to_menu_fragment)
                }
            })
    }

    private fun initViews(root: View) {
        timeText = root.findViewById(R.id.etaTime)
        addressText = root.findViewById(R.id.addressText)
        progressBar = root.findViewById(R.id.progressBar)
        dateText = root.findViewById(R.id.etaDate)

        addressText!!.text = Common.order_address
        dateText!!.text = Common.order_date

        konfettiView = root.findViewById(R.id.KonfettiView)

        val countDownTimer: CountDownTimer = object : CountDownTimer(7200000, 60000) {
            override fun onTick(millisUntilFinished: Long) {

                //forward progress
                //forward progress
                val finishedSeconds: Long = 7200000 - millisUntilFinished
                val total = (finishedSeconds.toFloat() / 720000.toFloat() * 100.0).toInt()
                progressBar!!.progress = total

                // progressBar!!.progress = abs(millisUntilFinished.toInt() / 100 - 100)
            }

            override fun onFinish() {}
        }
        countDownTimer.start()

        showCelebration()

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

        timeText!!.text = Common.order_time//currentTime.toString()


    }

    private fun sendNotification() {
        val intent = NavDeepLinkBuilder(requireContext())
            .setComponentName(HomeActivity::class.java)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(R.id.navigation_order_success)
            //    .setArguments(bundle)
            .createPendingIntent()
        //      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)


        //channel id, channel name
        var builder: NotificationCompat.Builder =
            NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(R.drawable.logo)
                .setAutoCancel(true)
                .setVibrate(
                    longArrayOf(
                        1000,
                        1000,
                        1000,
                        1000
                    )
                )//Vibrates for 1s then rests for 1sec and the cycle is repeated
                .setOnlyAlertOnce(true)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_notifications)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        requireContext().resources,
                        R.drawable.ic_notifications
                    )
                )

        builder = builder.setContentTitle(getString(R.string.order_success_notification_title))
        builder = builder.setContentText(getString(R.string.order_success_notification_description))

        val notificationManager =
            requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(0, builder.build())
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
        loadingFragmentHelper.showLoading(isLoading)
    }

    private fun showCelebration() {
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 400, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.3)
        )
        konfettiView?.start(party)
    }

    private fun showBottomNavView() {
        val navbar: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
        navbar.visibility = View.VISIBLE

    }

    private fun hideBottomNavView() {
        val navbar: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
        navbar.visibility = View.GONE

    }

}