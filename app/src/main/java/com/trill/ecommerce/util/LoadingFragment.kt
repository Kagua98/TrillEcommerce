package com.trill.ecommerce.util

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.trill.ecommerce.R


class LoadingFragment : Fragment() {
    private var previousStatusBarColor = 0
    override fun onAttach(context: Context) {
        super.onAttach(context)
        previousStatusBarColor = requireActivity().window.statusBarColor
     //   SystemBarUtils.setStatusBarColor(requireActivity().window, resources.getColor(R.color.greyStatusBar), false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnClickListener { v: View? -> } //empty onclick to soak up clicks
    }

    override fun onDetach() {
        super.onDetach()
      //  SystemBarUtils.setStatusBarColor(requireActivity().window, previousStatusBarColor, false)
    }

    class LoadingFragmentHelper(private val fm: FragmentManager) {
        private var loadingFragment: LoadingFragment? = null
        private var pendingRemoval = false
        fun showLoading(show: Boolean) {
            if (show) addLoadingScreen() else removeLoadingScreen()
        }

        fun addLoadingScreen() {
            if (loadingFragment == null) {
                pendingRemoval = false
                loadingFragment = LoadingFragment()
                fm.beginTransaction()
                    .add(android.R.id.content, loadingFragment!!)
                    .runOnCommit {
                        if (pendingRemoval) {
                            removeLoadingScreen()
                        }
                    }
                    .commit()
            }
        }

        fun removeLoadingScreen() {
            if (loadingFragment != null && !loadingFragment!!.isAdded) {
                pendingRemoval = true
                return
            }
            if (loadingFragment != null) {
                fm.beginTransaction().remove(loadingFragment!!).commit()
                loadingFragment = null
            }
        }
    }
}