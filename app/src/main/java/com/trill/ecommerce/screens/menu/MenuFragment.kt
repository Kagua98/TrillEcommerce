package com.trill.ecommerce.screens.menu

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LayoutAnimationController
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andremion.counterfab.CounterFab
import com.google.android.material.snackbar.Snackbar

import com.trill.ecommerce.R
import com.trill.ecommerce.adapter.MenuCategoriesAdapter
import com.trill.ecommerce.databinding.FragmentMenuBinding
import com.trill.ecommerce.util.LoadingFragment
import com.trill.ecommerce.util.SpaceItemDecoration
import com.trill.ecommerce.viewmodel.MenuCategoriesViewModel

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var menuGroupViewModel: MenuCategoriesViewModel
    private lateinit var dialog: AlertDialog
    private lateinit var layoutAnimationController: LayoutAnimationController
    private var adapter: MenuCategoriesAdapter? = null
    private var recyclerView: RecyclerView? = null

    private lateinit var loadingFragmentHelper: LoadingFragment.LoadingFragmentHelper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        showFAB()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {

        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        val root: View = binding.root

        menuGroupViewModel = ViewModelProvider(this)[MenuCategoriesViewModel::class.java]
        loadingFragmentHelper =
            LoadingFragment.LoadingFragmentHelper(requireActivity().supportFragmentManager)

        val backPressedButton: ImageView = root.findViewById(R.id.backPressed)
        backPressedButton.setOnClickListener {
            onBackPressed(it)
        }

        initViews()

        recyclerView = binding.recyclerView

        menuGroupViewModel.getMessageError().observe(viewLifecycleOwner, Observer {
            Snackbar.make(requireView(), ""+it, Snackbar.LENGTH_LONG).show()
            })

        menuGroupViewModel.getMenuCategoriesList().observe(viewLifecycleOwner, Observer {
            adapter = MenuCategoriesAdapter(requireContext(), it)
            recyclerView!!.adapter = adapter
            showLoading(false)

        })


        return root
    }

    private fun onBackPressed(view: View) {
        //Navigation.findNavController(view).navigate(R.id.route_to_home_fragment)
        requireActivity().onBackPressed()
    }

    private fun showFAB() {
        val fab : CounterFab = requireActivity().findViewById(R.id.counterFab)
        fab.visibility = View.VISIBLE
    }
    private fun hideFAB() {
        val fab : CounterFab = requireActivity().findViewById(R.id.counterFab)
        fab.visibility = View.GONE
    }

    private fun initViews() {
        showLoading(true)
      //  layoutAnimationController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)

        recyclerView = binding.recyclerView

        recyclerView!!.setHasFixedSize(true)

        val layoutManager = GridLayoutManager(context, 2)
        layoutManager.orientation = RecyclerView.VERTICAL


        recyclerView!!.addItemDecoration(SpaceItemDecoration(8))
        recyclerView!!.layoutManager = layoutManager

    }

    private fun showLoading(isLoading: Boolean) {
        // binding.buttonPrimary.isEnabled = !isLoading
        loadingFragmentHelper.showLoading(isLoading)
    }

    override fun onDetach() {
        super.onDetach()
        hideFAB()
    }
}