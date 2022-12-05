package com.trill.ecommerce.screens.account

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.andremion.counterfab.CounterFab
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.trill.ecommerce.R
import com.trill.ecommerce.databinding.FragmentAccountBinding
import com.trill.ecommerce.screens.authentication.LoginActivity
import com.trill.ecommerce.screens.tos.TOSFragment
import java.io.File

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onAttach(context: Context) {
        super.onAttach(context)
        hideFAB()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize Firebase Auth
        auth = Firebase.auth

        //Route to Contact Support Page
        routeToContactSupportFragment(root)

        routeToProfileFragment(root)

        initView(root)


        val buttonLogout = binding.buttonLogout
        buttonLogout.setOnClickListener {
            logout()
        }

        return root
    }

    private fun initView(root: View) {
        val buttonInstagram: View? = root.findViewById(R.id.buttonInstagram)
        buttonInstagram!!.setOnClickListener {
            instagramButtonClick()
        }

        val buttonFacebook: View? = root.findViewById(R.id.buttonFacebook)
        buttonFacebook!!.setOnClickListener {
            facebookButtonClick()
        }

        val buttonTwitter: View? = root.findViewById(R.id.twitterButton)
        buttonTwitter!!.setOnClickListener {
            twitterButtonClick()
        }

        val buttonTiktok: View? = root.findViewById(R.id.tiktokButton)
        buttonTiktok!!.setOnClickListener {
            tiktokButtonClick()
        }

        val viewHistory: View? = root.findViewById(R.id.account_history_view)
        viewHistory!!.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.route_to_history)
        }


        val tosButton: View? = root.findViewById(R.id.textReadTerms)
        tosButton!!.setOnClickListener {
            val tosDialog = TOSFragment.getInstance()
            tosDialog.show(requireActivity().supportFragmentManager, "TOSFragment")
        }
    }

    private fun tiktokButtonClick() {
        val uri: Uri = Uri.parse(getString(R.string.tiktok_account_link))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.tiktok.android")

        if (isIntentAvailable(requireContext(), intent)) {
            startActivity(intent)
        } else {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.tiktok_account_link))
                )
            )
        }
    }

    private fun twitterButtonClick() {
        val uri: Uri = Uri.parse(getString(R.string.twitter_account_link))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.twitter.android")

        if (isIntentAvailable(requireContext(), intent)) {
            startActivity(intent)
        } else {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.twitter_account_link))
                )
            )
        }
    }

    private fun facebookButtonClick() {
        val uri: Uri = Uri.parse(getString(R.string.facebook_account_link))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.facebook.android")

        if (isIntentAvailable(requireContext(), intent)) {
            startActivity(intent)
        } else {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.facebook_account_link))
                )
            )
        }
    }

    private fun instagramButtonClick() {
        val uri: Uri = Uri.parse(getString(R.string.instagram_account_link))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.instagram.android")

        if (isIntentAvailable(requireContext(), intent)) {
            startActivity(intent)
        } else {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.instagram_account_link))
                )
            )
        }

    }

    private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        val packageManager: PackageManager = context.packageManager
        val list: List<ResolveInfo> =
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return list.isNotEmpty()
    }


    private fun logout() {
        Firebase.auth.signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
        deleteCache(requireContext())

    }

    private fun routeToProfileFragment(root: View) {
        val view: View? = root.findViewById(R.id.account_profile_view)
        view!!.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.route_to_account_profile)
        }
    }

    private fun routeToContactSupportFragment(root: View) {
        val view: View? = root.findViewById(R.id.account_contact_support_view)
        view!!.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.route_to_account_contact_support)
        }
    }

    private fun hideFAB() {
        val fab: CounterFab = requireActivity().findViewById(R.id.counterFab)
        fab.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun deleteCache(context: Context) {
        try {
            val dir: File = context.cacheDir
            deleteDir(dir)
        } catch (e: Exception) {
        }
    }

    fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory()) {
            val children: Array<String> = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile()) {
            dir.delete()
        } else {
            false
        }
    }
}