package com.trill.ecommerce.screens.account.profile

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.andremion.counterfab.CounterFab
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.trill.ecommerce.R
import com.trill.ecommerce.databinding.FragmentProfileBinding
import com.trill.ecommerce.model.UserModel
import com.trill.ecommerce.util.ui.LoadingFragment


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var textInputEditTextName: TextInputEditText? = null
    private var textInputEditTextPhone: TextInputEditText? = null
    private var textInputEditTextEmail: TextInputEditText? = null
    private var textInputEditTextAddress: TextInputEditText? = null
    private var textInputEditTextContactPerson: TextInputEditText? = null
    private var textInputEditTextPin: TextInputEditText? = null
    private var button: MaterialButton? = null

    private lateinit var loadingFragmentHelper: LoadingFragment.LoadingFragmentHelper

    private lateinit var auth: FirebaseAuth

    override fun onAttach(context: Context) {
        super.onAttach(context)
        hideBottomNavView()
        hideFAB()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        loadingFragmentHelper =
            LoadingFragment.LoadingFragmentHelper(requireActivity().supportFragmentManager)

        // Initialize Firebase Auth
        auth = Firebase.auth

        //On Close Button Pressed
        onBackButtonPressed(root)


        initViews(root)


        return root
    }

    private fun initViews(root: View) {
        textInputEditTextName = root.findViewById(R.id.textInputEditTextName)
        textInputEditTextPhone = root.findViewById(R.id.textInputEditTextPhone)
        textInputEditTextEmail = root.findViewById(R.id.textInputEditTextEmail)
        textInputEditTextAddress = root.findViewById(R.id.textInputEditTextAddress)
        textInputEditTextContactPerson = root.findViewById(R.id.textInputEditTextContactPerson)
        textInputEditTextPin = root.findViewById(R.id.textInputEditTextPin)
        button = root.findViewById(R.id.button)
        
        populateFields(root)

        button!!.setOnClickListener {
            if (textInputEditTextName!!.text.toString().isEmpty() &&
                textInputEditTextPhone!!.text.toString().isEmpty() &&
                textInputEditTextContactPerson!!.text.toString().isEmpty() &&
                textInputEditTextAddress!!.text.toString().isEmpty()){
                Toast.makeText(requireContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }else if(textInputEditTextName!!.text.toString().isEmpty()){
                Toast.makeText(requireContext(), "Organization name is required", Toast.LENGTH_SHORT).show()
            }else if(textInputEditTextPhone!!.text.toString().isEmpty()){
                Toast.makeText(requireContext(), "Phone number is required", Toast.LENGTH_SHORT).show()
            }else if(textInputEditTextContactPerson!!.text.toString().isEmpty()){
                Toast.makeText(requireContext(), "Contact person name is required", Toast.LENGTH_SHORT).show()
            }else if(textInputEditTextAddress!!.text.toString().isEmpty()){
                Toast.makeText(requireContext(), "Location is required", Toast.LENGTH_SHORT).show()
            }else {

                var builder = AlertDialog.Builder(requireContext())

                val itemView =
                    LayoutInflater.from(context).inflate(R.layout.profile_confirm_dialog, null)
                builder.setView(itemView)

                val dialog = builder.show()
                val buttonCancel = itemView.findViewById<MaterialButton>(R.id.cancelButton)
                buttonCancel.setOnClickListener { dialogInterface ->
                    dialog.dismiss()
                }

                val buttonOk = itemView.findViewById<MaterialButton>(R.id.okButton)
                buttonOk.setOnClickListener {
                    pushDataToFirebase(root)
                    dialog.dismiss()
                }

                dialog.show()

            }

        }

    }

    private fun pushDataToFirebase(root: View) {
        val uid = auth.currentUser!!.uid
        val database = Firebase.database
        val reference = database.getReference("Users").child(uid)

        val user = UserModel()
        user.uid = auth.currentUser!!.uid
        user.name = textInputEditTextName!!.text.toString()
        user.phone = textInputEditTextPhone!!.text.toString()
        user.email = textInputEditTextEmail!!.text.toString()
        user.contact = textInputEditTextContactPerson!!.text.toString()
        user.address = textInputEditTextAddress!!.text.toString()
        user.pin = textInputEditTextPin!!.text.toString()

        reference.setValue(user)
            .addOnSuccessListener {
                Snackbar.make(root, "Profile Updated Successfully!", Snackbar.LENGTH_LONG).show()
                //Toast.makeText(requireContext(), "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT).show()
            }
            .addOnCanceledListener {
                Toast.makeText(requireContext(), "Operation cancelled. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun populateFields(root: View) {
        // Write a message to the database

        val uid = auth.currentUser!!.uid
        val database = Firebase.database
        val reference = database.getReference("Users").child(uid)

        showLoading(true)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                showLoading(false)
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                val map: Map<String, Any> = snapshot.value as Map<String, Any>
                val name = map["name"]
                val phone = map["phone"]
                val email = map["email"]
                val address = map["address"]
                val contact = map["contact"]
                val pin = map["pin"]

                textInputEditTextName!!.setText(name.toString())
                textInputEditTextPhone!!.setText(phone.toString())
                textInputEditTextEmail!!.setText(email.toString())
                textInputEditTextContactPerson!!.setText(contact.toString())
                textInputEditTextPin!!.setText(pin.toString())

                if (address.toString() == "null") {
                    textInputEditTextAddress!!.setText("")
                } else
                    textInputEditTextAddress!!.setText(address.toString())

                if (pin.toString() == "null") {
                    textInputEditTextPin!!.setText("")
                } else
                    textInputEditTextPin!!.setText(pin.toString())
                
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value. Error details -> ${error.toException().message}")
                Snackbar.make(root, "Failed to fetch profile details", Snackbar.LENGTH_LONG).show()
            }

        })
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

    private fun showLoading(isLoading: Boolean) {
        // binding.buttonPrimary.isEnabled = !isLoading
        loadingFragmentHelper.showLoading(isLoading)
    }
}