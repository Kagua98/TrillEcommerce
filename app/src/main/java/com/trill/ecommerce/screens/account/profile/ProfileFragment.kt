package com.trill.ecommerce.screens.account.profile

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.andremion.counterfab.CounterFab
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.trill.ecommerce.R
import com.trill.ecommerce.databinding.FragmentProfileBinding
import com.trill.ecommerce.util.LoadingFragment
import java.util.Objects


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var textInputLayoutName: TextInputLayout? = null
    private var textInputLayoutPhone: TextInputLayout? = null
    private var textInputLayoutEmail: TextInputLayout? = null
    private var textInputLayoutAddress: TextInputLayout? = null

    private var textInputEditTextName: TextInputEditText? = null
    private var textInputEditTextPhone: TextInputEditText? = null
    private var textInputEditTextEmail: TextInputEditText? = null
    private var textInputEditTextAddress: TextInputEditText? = null

    private lateinit var loadingFragmentHelper: LoadingFragment.LoadingFragmentHelper

    private lateinit var auth: FirebaseAuth

    override fun onAttach(context: Context) {
        super.onAttach(context)
        hideBottomNavView()
        hideFAB()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        loadingFragmentHelper =
            LoadingFragment.LoadingFragmentHelper(requireActivity().supportFragmentManager)

        // Initialize Firebase Auth
        auth = Firebase.auth

        //On Close Button Pressed
        onBackButtonPressed(root)

        populateFields(root)

        initViews(root)


        return root
    }

    private fun initViews(root: View){
        textInputLayoutName = root.findViewById(R.id.textInputName)
        textInputLayoutPhone = root.findViewById(R.id.textInputPhone)
        textInputLayoutEmail = root.findViewById(R.id.textInputLayoutEmail)
        textInputLayoutAddress = root.findViewById(R.id.textInputAddress)

        textInputEditTextName = root.findViewById(R.id.textInputEditTextName)
        textInputEditTextPhone = root.findViewById(R.id.textInputEditTextPhone)
        textInputEditTextEmail = root.findViewById(R.id.textInputEditTextEmail)
        textInputEditTextAddress = root.findViewById(R.id.textInputEditTextAddress)


    }

    private fun populateFields(root: View) {
        // Write a message to the database

        val uid = auth.currentUser!!.uid
        val database = Firebase.database
        val reference = database.getReference("Users").child(uid)

        showLoading(true)
        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                showLoading(false)
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                val map : Map<String, Any> = snapshot.value as Map<String, Any>
                val name = map["name"]
                val phone = map["phone"]
                val email = map["email"]
                val address = map["address"]

                textInputEditTextName!!.setText(name.toString())
                textInputEditTextPhone!!.setText(phone.toString())
                textInputEditTextEmail!!.setText(email.toString())

                if (address.toString() == "null"){
                    textInputEditTextAddress!!.setHint("")
                }else
                    textInputEditTextAddress!!.setText(address.toString())


            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value. Error details -> ${error.toException().message}")
                Snackbar.make(root, "Failed to fetch profile details", Snackbar.LENGTH_LONG).show()
            }

        })
    }

    private fun hideFAB() {
        val fab : CounterFab = requireActivity().findViewById(R.id.counterFab)
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

    private fun showLoading(isLoading: Boolean) {
        // binding.buttonPrimary.isEnabled = !isLoading
        loadingFragmentHelper.showLoading(isLoading)
    }
}