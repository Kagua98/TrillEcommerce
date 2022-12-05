package com.trill.ecommerce.screens.account.contactsupport

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.trill.ecommerce.R
import com.trill.ecommerce.screens.menu.comments.CommentsFragment


class ContactsModalFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contacts_modal, container, false)
    }

    companion object {
        private var instance: ContactsModalFragment? = null

        fun getInstance(): ContactsModalFragment {
            if (instance == null)
                instance = ContactsModalFragment()
            return instance!!
        }
    }

}