package com.trill.ecommerce.screens.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.trill.ecommerce.R
import com.trill.ecommerce.adapter.HomeAdapter
import com.trill.ecommerce.databinding.FragmentHomeBinding
import com.trill.ecommerce.util.SpaceItemDecoration
import com.trill.ecommerce.util.ui.LoadingFragment
import com.trill.ecommerce.viewmodel.HomeViewModel
import java.util.*


class HomeFragment : Fragment() {


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    private lateinit var homeViewModel: HomeViewModel
    private var adapter: HomeAdapter? = null
    private var recyclerView: RecyclerView? = null
    private lateinit var loadingFragmentHelper: LoadingFragment.LoadingFragmentHelper

    private lateinit var auth: FirebaseAuth

    override fun onAttach(context: Context) {
        super.onAttach(context)

      //  val container: View? = requireActivity().window.decorView.findViewById<View>(R.id.container)
      //  container!!.fitsSystemWindows = true
      //  container.requestApplyInsets()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        loadingFragmentHelper =
            LoadingFragment.LoadingFragmentHelper(requireActivity().supportFragmentManager)

        // Initialize Firebase Auth
        auth = Firebase.auth

        recyclerView = binding.recyclerView

        homeViewModel.getMessageError().observe(viewLifecycleOwner, Observer {
            Snackbar.make(requireView(), "" + it, Snackbar.LENGTH_LONG).show()
        })


        homeViewModel.getHomeAdsList().observe(viewLifecycleOwner, Observer {
            adapter = HomeAdapter(requireContext(), it)
            recyclerView!!.adapter = adapter
            showLoading(false)

        })


        setupGreeting(root)

        setUserName(root)

        routes(root)

        initViews(root)

        return root
    }

    private fun initViews(root: View) {
        showLoading(true)
        //  layoutAnimationController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)

        recyclerView = binding.recyclerView

        recyclerView!!.setHasFixedSize(true)

        val layoutManager = GridLayoutManager(context, 1)
        layoutManager.orientation = RecyclerView.VERTICAL

        recyclerView!!.addItemDecoration(SpaceItemDecoration(8))
        recyclerView!!.layoutManager = layoutManager


    }

    private fun showLoading(isLoading: Boolean) {
        // binding.buttonPrimary.isEnabled = !isLoading
        loadingFragmentHelper.showLoading(isLoading)
    }

    private fun routes(root: View) {
        val imageView: ImageView = root.findViewById(R.id.profile)
        imageView.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.route_to_account_fragment)
        }

        val seeAllImage: ImageView = root.findViewById(R.id.seeMore)
        seeAllImage.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.route_to_menu_fragment)
        }

        val seeAllText: TextView = root.findViewById(R.id.seeAll)
        seeAllText.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.route_to_menu_fragment)
        }
    }

    private fun setUserName(root: View) {
        val userNameTextView = root.findViewById<TextView>(R.id.userName)
        val name = auth.currentUser!!.displayName
        userNameTextView.text = "${name.toString()}!"

    }

    private fun setupGreeting(root: View) {
        val date = Date()
        val cal: Calendar = Calendar.getInstance()
        cal.time = date
        val hour: Int = cal.get(Calendar.HOUR_OF_DAY)

        var greeting: String? = null
        if (hour in 4..11) {
            greeting = getString(R.string.home_greeting_morning)
        } else if (hour in 12..16) {
            greeting = getString(R.string.home_greeting_afternoon)
        } else if (hour in 17..21) {
            greeting = getString(R.string.home_greeting_evening)
        } else if (hour in 21..24) {
            greeting = getString(R.string.home_greeting_evening)
        } else if (hour in 0..4) {
            greeting = getString(R.string.home_greeting_evening)
        } else if (hour in 4..6) {
            greeting = getString(R.string.home_greeting_morning)
        }

        val textGreeting = root.findViewById<TextView>(R.id.textGreeting)
        textGreeting.text = greeting

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}