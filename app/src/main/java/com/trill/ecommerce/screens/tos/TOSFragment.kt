package com.trill.ecommerce.screens.tos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.trill.ecommerce.R
import com.trill.ecommerce.screens.account.contactsupport.ContactsModalFragment


class TOSFragment : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tos, container, false)
    }

    companion object {
        private var instance: TOSFragment? = null

        fun getInstance(): TOSFragment {
            if (instance == null)
                instance = TOSFragment()
            return instance!!
        }
    }

}