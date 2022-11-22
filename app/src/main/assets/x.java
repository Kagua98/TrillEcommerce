package com.trill.ecommerce.util;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

@

public class x {
    DatabaseReference databaseReference;

    private void readData(){
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.child("userId").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()){

                    if (task.getResult().exists()){
                        //Everything works to plan
                        DataSnapshot dataSnapshot = task.getResult();
                        String name = String.valueOf(dataSnapshot.child("name").getValue());
                        //assign value to textview

                    }else{
                        //Set username to customer. Means username is null which shouldn't be the case
                    }

                }else
                    Toast.makeText(this, "An error occurred, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

