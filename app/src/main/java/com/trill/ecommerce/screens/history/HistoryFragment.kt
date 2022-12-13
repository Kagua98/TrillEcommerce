package com.trill.ecommerce.screens.history

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andremion.counterfab.CounterFab
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.trill.ecommerce.R
import com.trill.ecommerce.adapter.HistoryAdapter
import com.trill.ecommerce.callback.ILoadOrderCallBackListener
import com.trill.ecommerce.databinding.FragmentHistoryBinding
import com.trill.ecommerce.model.HistoryModel
import com.trill.ecommerce.model.OrderModel
import com.trill.ecommerce.util.Common
import com.trill.ecommerce.util.ui.LoadingFragment
import java.util.Collections


class HistoryFragment : Fragment(), ILoadOrderCallBackListener {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyModel : HistoryModel

    private var recyclerView: RecyclerView? = null
    internal lateinit var listener: ILoadOrderCallBackListener
    private lateinit var auth: FirebaseAuth

    private lateinit var loadingFragmentHelper: LoadingFragment.LoadingFragmentHelper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        hideBottomNavView()
        hideFAB()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        historyModel = ViewModelProvider(this)[HistoryModel::class.java]

        loadingFragmentHelper =
            LoadingFragment.LoadingFragmentHelper(requireActivity().supportFragmentManager)

        initViews(root)

        onBackButtonPressed(root)

        // Initialize Firebase Auth
        auth = Firebase.auth
        loadHistoryFromFirebase(root)

        historyModel.mutableLiveDataOrderList.observe(viewLifecycleOwner, Observer {
            Collections.reverse(it)
            val adapter = HistoryAdapter(requireContext(), it)
            recyclerView!!.adapter = adapter
        })

        return root
    }

    private fun initViews(root: View){
        listener = this

        recyclerView = binding.recyclerView
        recyclerView!!.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView!!.layoutManager = layoutManager

        loadingFragmentHelper =
            LoadingFragment.LoadingFragmentHelper(requireActivity().supportFragmentManager)

    }

    private fun loadHistoryFromFirebase(root: View) {
        val uid = auth.currentUser!!.uid
        showLoading(true)

        val orderList = ArrayList<OrderModel>()

        FirebaseDatabase.getInstance().getReference(Common.ORDER_REFERNCE)
            .orderByChild("userId")
            .equalTo(uid)
            .limitToLast(100)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (orderSnapshot in snapshot.children){
                        val order = orderSnapshot.getValue(OrderModel::class.java)
                        order!!.orderNumber = orderSnapshot.key
                        orderList.add(order)
                    }

                    listener.onLoadOrderSuccess(orderList)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener.onLoadOrderFailure(error.message.toString())
                }

            })
    }


    override fun onLoadOrderSuccess(orderList: List<OrderModel>) {
        showLoading(false)
        historyModel.setMutableLiveDatOrderList(orderList)
    }

    override fun onLoadOrderFailure(message: String) {
       showLoading(false)
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun showLoading(isLoading: Boolean) {
        loadingFragmentHelper.showLoading(isLoading)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hideFAB() {
        val fab: CounterFab = requireActivity().findViewById(R.id.counterFab)
        fab.visibility = View.GONE
    }

    private fun onBackButtonPressed(root: View?) {
        val backPressedButton: ImageView = root!!.findViewById(R.id.backPressed)
        backPressedButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onDetach() {
        super.onDetach()
        showBottomNavView()
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