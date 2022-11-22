package com.trill.ecommerce.screens.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.andremion.counterfab.CounterFab
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.trill.ecommerce.R
import com.trill.ecommerce.data.AuthViewModel
import com.trill.ecommerce.database.CartDataSource
import com.trill.ecommerce.database.CartDatabase
import com.trill.ecommerce.database.LocalCartDataSource
import com.trill.ecommerce.databinding.ActivityHomeBinding
import com.trill.ecommerce.eventbus.CountCartEvent
import com.trill.ecommerce.screens.authentication.LoginActivity
import com.trill.ecommerce.util.LoadingFragment
import com.trill.ecommerce.util.MenuCategoryClick
import com.trill.ecommerce.util.MenuListItemClick
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var cartDataSource: CartDataSource
    private var authViewModel: AuthViewModel? = null
    private lateinit var loadingFragmentHelper: LoadingFragment.LoadingFragmentHelper
    private var fab : CounterFab? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

         cartDataSource = LocalCartDataSource(CartDatabase.getInstance(this).cartDAO())

        val navView: BottomNavigationView = binding.navView

        fab = binding.counterFab


        val navController = findNavController(R.id.nav_host_fragment_activity_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_cart, R.id.navigation_account
            )
        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        val firebaseAuth = FirebaseAuth.getInstance().currentUser
        if (firebaseAuth == null){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            Toast.makeText(this, "🧐🧐🧐", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        countCartItem()
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMenuCategorySelected(event: MenuCategoryClick) {
        if (event.isSuccess) {
            //   Toast.makeText(this, ""+ Common.categorySelected!!.name, Toast.LENGTH_SHORT).show()
            findNavController(R.id.nav_host_fragment_activity_home).navigate(R.id.navigation_menu_list)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMenuListItemSelected(event: MenuListItemClick) {
        if (event.isSuccess) {
            //   Toast.makeText(this, ""+ Common.categorySelected!!.name, Toast.LENGTH_SHORT).show()
            findNavController(R.id.nav_host_fragment_activity_home).navigate(R.id.navigation_item_details)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCountCartEvent(event: CountCartEvent){
        if (event.isSuccess){
            countCartItem()
        }
    }

    private fun countCartItem() {
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(this).cartDAO())
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        cartDataSource.countCartItems(uid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Int> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: Int) {
                    fab = binding.counterFab
                    fab!!.count = t
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(this@HomeActivity, "[COUNT CART] ${e.message}", Toast.LENGTH_LONG).show()
                    Log.i("HomeActivity", "${e.message}")
                }

            })
    }

    private fun showLoading(isLoading: Boolean) {
        // binding.buttonPrimary.isEnabled = !isLoading
        loadingFragmentHelper.showLoading(isLoading)
    }

}