package com.trill.ecommerce.screens.checkout

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.google.android.material.button.MaterialButton
import com.trill.ecommerce.R
import com.trill.ecommerce.database.CartDataSource
import com.trill.ecommerce.database.CartDatabase
import com.trill.ecommerce.database.LocalCartDataSource
import com.trill.ecommerce.databinding.FragmentCheckoutBinding
import com.trill.ecommerce.databinding.FragmentMPesaCheckoutBinding
import io.reactivex.disposables.CompositeDisposable


class MPesaCheckoutFragment : Fragment() {

    private var _binding: FragmentMPesaCheckoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var root: View

    private var cartDataSource: CartDataSource? = null
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    private var button: MaterialButton? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        _binding = FragmentMPesaCheckoutBinding.inflate(inflater, container, false)
        root = binding.root


        initViews(root)

        return root

    }

    private fun initViews(root: View?) {
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(requireContext()).cartDAO())

        binding.button.setOnClickListener {
            checkout()

        }
    }

    private fun checkout() {
        Navigation.findNavController(root!!)
            .navigate(R.id.route_to_order_success)
    }

}