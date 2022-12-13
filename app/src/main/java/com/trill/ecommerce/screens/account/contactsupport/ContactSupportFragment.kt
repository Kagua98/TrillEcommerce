package com.trill.ecommerce.screens.account.contactsupport

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.andremion.counterfab.CounterFab
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.trill.ecommerce.R
import com.trill.ecommerce.databinding.FragmentContactSupportBinding
import com.trill.ecommerce.util.ui.LoadingFragment
import javax.mail.*
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class ContactSupportFragment : Fragment() {

    private var _binding: FragmentContactSupportBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingFragmentHelper: LoadingFragment.LoadingFragmentHelper

    private var progressBar: CircularProgressIndicator? = null

    private lateinit var auth: FirebaseAuth

    private var userphone: String? = null
    private var userEmail: String? = null
    private var userName: String? = null

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

        initViews(root)

        loadingFragmentHelper =
            LoadingFragment.LoadingFragmentHelper(requireActivity().supportFragmentManager)

        //On Close Button Pressed
        onBackButtonPressed(root)



        return root
    }

    private fun initViews(root: View) {
        // Initialize Firebase Auth
        auth = Firebase.auth

        populateFields()

        val phone: ImageView? = root.findViewById(R.id.phone)
        phone!!.setOnClickListener {
            val contactsDialog = ContactsModalFragment.getInstance()
            contactsDialog.show(requireActivity().supportFragmentManager, "ContactsModalFragment")
        }

        progressBar = binding.progressBar
        val button: Button = binding.button
        val editText: EditText = binding.editText
        val feedback = editText.text
        button.setOnClickListener{
            if (feedback.isEmpty()){
                showLoading(false)
                Snackbar.make(root, getString(R.string.contact_support_empty_message_error), Snackbar.LENGTH_SHORT)
                    .show()
            }else{
                showLoading(true)
                sendMessage(root)
            }
        }


    }

    private fun populateFields() {
        // Write a message to the database
        val uid = auth.currentUser!!.uid
        val database = Firebase.database
        val reference = database.getReference("Users").child(uid)

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

                userphone = phone.toString()
                userEmail = email.toString()
                userName = name.toString()



            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value. Error details -> ${error.toException().message}")
            }

        })
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

    private fun sendMessage(root: View) {
        val editText: EditText = binding.editText
        val message = editText.text!!.toString()

        val recipientEmail = getString(R.string.contact_support_email_recipient)

        try {
            val stringSenderEmail = "trill.feedback1@gmail.com"
            val stringReceiverEmail = recipientEmail
            val stringPasswordSenderEmail = "heldrboppslefvep"
            val stringHost = "smtp.gmail.com"
            val properties = System.getProperties()
            properties["mail.smtp.host"] = stringHost
            properties["mail.smtp.port"] = "465"
            properties["mail.smtp.ssl.enable"] = "true"
            properties["mail.smtp.auth"] = "true"
            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(stringSenderEmail, stringPasswordSenderEmail)
                }
            })
            val mimeMessage = MimeMessage(session)
            mimeMessage.addRecipient(Message.RecipientType.TO, InternetAddress(stringReceiverEmail))
            mimeMessage.subject = "Trill Customer Feedback Android"
            mimeMessage.setText("${message} \n\nCompany Name: $userName\nCompany Email: $userEmail\nCompany Phone: $userphone")
            val thread = Thread {
                try {
                    Transport.send(mimeMessage)
                    editText.setText("")
                    Snackbar.make(root, "Feedback sent successfully!", Snackbar.LENGTH_SHORT).show()
                } catch (e: MessagingException) {
                    Snackbar.make(root, e.message.toString(), Snackbar.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
               showLoading(false)
            }
            thread.start()
        } catch (e: AddressException) {

            showLoading(false)
            Snackbar.make(root, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            e.printStackTrace()
        } catch (e: MessagingException) {
            showLoading(false)
            Snackbar.make(root, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            e.printStackTrace()
        }

    }

    private fun hideFAB() {
        val fab: CounterFab = requireActivity().findViewById(R.id.counterFab)
        fab.visibility = View.GONE
    }


    private fun onBackButtonPressed(root: View) {
        val backPressedButton: ImageView = root.findViewById(R.id.backPressed)
        backPressedButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        // binding.buttonPrimary.isEnabled = !isLoading
        loadingFragmentHelper.showLoading(isLoading)
    }


}