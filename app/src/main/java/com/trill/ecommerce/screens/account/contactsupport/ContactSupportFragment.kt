package com.trill.ecommerce.screens.account.contactsupport

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.navigation.Navigation
import com.andremion.counterfab.CounterFab
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.trill.ecommerce.R
import com.trill.ecommerce.databinding.FragmentContactSupportBinding

class ContactSupportFragment : Fragment() {

    private var _binding: FragmentContactSupportBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        //Hide Bottom Nav View from the main activity
        hideBottomNavView()
        hideFAB()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentContactSupportBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //On Close Button Pressed
        onBackButtonPressed(root)

        //Send Feedback to Email
        sendFeedbackToEmail(root)



        return root
    }

    override fun onDetach() {
        super.onDetach()
        showBottomNavView()
    }

    private fun hideBottomNavView() {
        val navbar: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
        navbar.visibility = View.GONE

    }

    private fun showBottomNavView() {
        val navbar: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
        navbar.visibility = View.VISIBLE

    }

    private fun sendFeedbackToEmail(root: View) {
        val button: Button = binding.button
        val editText: EditText = binding.editText
        val feedback = editText.text
        button.setOnClickListener{
            if (feedback.isEmpty()){
                editText.error = (getString(R.string.account_contact_support_empty_message_error))
                Snackbar.make(root, getString(R.string.account_contact_support_empty_message_error), Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun hideFAB() {
        val fab : CounterFab = requireActivity().findViewById(R.id.counterFab)
        fab.visibility = View.GONE
    }


    private fun onBackButtonPressed(root: View) {
        val backPressedButton: ImageView = root.findViewById(R.id.backPressed)
        backPressedButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }


}