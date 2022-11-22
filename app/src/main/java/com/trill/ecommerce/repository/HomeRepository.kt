package com.trill.ecommerce.repository

import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.disklrucache.DiskLruCache.Value
import com.google.firebase.database.*
import com.trill.ecommerce.model.HomeMenuItem

class HomeRepository {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference()

    @Volatile private var INSTANCE: HomeRepository ?= null

    fun getInstance() : HomeRepository {

        return INSTANCE ?: synchronized(this){

            val instance = HomeRepository()
            INSTANCE = instance
            instance
        }
    }

    fun loadHomeMenuItems(homeMenuItems: MutableLiveData<List<HomeMenuItem>>) {

        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                try {

                    val mHomeMenuItems : List<HomeMenuItem> = snapshot.children.map { dataSnapshot ->
                        dataSnapshot.getValue(HomeMenuItem::class.java)!!
                    }

                    homeMenuItems.postValue(mHomeMenuItems)

                }catch (_: java.lang.Exception){

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
}