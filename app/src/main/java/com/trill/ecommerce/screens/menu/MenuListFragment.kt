package com.trill.ecommerce.screens.menu

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LayoutAnimationController
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andremion.counterfab.CounterFab
import com.trill.ecommerce.R
import com.trill.ecommerce.adapter.MenuListAdapter
import com.trill.ecommerce.databinding.FragmentMenuListBinding
import com.trill.ecommerce.util.Common
import com.trill.ecommerce.util.LoadingFragment
import com.trill.ecommerce.util.SpaceItemDecoration
import com.trill.ecommerce.viewmodel.MenuListViewModel


class MenuListFragment : Fragment() {

    private var _binding: FragmentMenuListBinding? = null
    private val binding get() = _binding!!

    private lateinit var menuListViewModel: MenuListViewModel
    var layoutAnimationController: LayoutAnimationController? = null
    private var adapter: MenuListAdapter? = null
    var recyclerView: RecyclerView? = null

    private lateinit var loadingFragmentHelper: LoadingFragment.LoadingFragmentHelper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        showFAB()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMenuListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        menuListViewModel = ViewModelProvider(this)[MenuListViewModel::class.java]

        loadingFragmentHelper =
            LoadingFragment.LoadingFragmentHelper(requireActivity().supportFragmentManager)

        navbar(root)

        initViews()

        recyclerView = binding.recyclerView

        menuListViewModel.getMutableModelMenuList().observe(viewLifecycleOwner, Observer {
            adapter = MenuListAdapter(requireContext(), it)
            recyclerView!!.adapter = adapter
            showLoading(false)
        })


        return root
    }

    private fun navbar(root: View) {
        val backPressedButton: ImageView = root.findViewById(R.id.backPressed)
        backPressedButton.setOnClickListener {
            onBackPressed(root)
        }

        val navBarTitle = root.findViewById<TextView>(R.id.title)
        navBarTitle.text = Common.categorySelected!!.name
    }

    private fun showFAB() {
        val fab : CounterFab = requireActivity().findViewById(R.id.counterFab)
        fab.visibility = View.VISIBLE
    }

    private fun initViews() {
        showLoading(true)
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

    private fun onBackPressed(view: View) {
        //Navigation.findNavController(it).navigate(R.id.route_to_menu_fragment)
        requireActivity().onBackPressed()
    }

    override fun onStop() {
        if (adapter != null)
            adapter!!.onStop()
        super.onStop()
    }
}