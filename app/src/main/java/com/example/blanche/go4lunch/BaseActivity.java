package com.example.blanche.go4lunch;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import android.support.annotation.Nullable;

import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import io.reactivex.annotations.NonNull;

public abstract class BaseActivity extends AppCompatActivity {

    protected OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "unknown error", Toast.LENGTH_LONG).show();
            }
        };
    }

    // --------------------
    // UTILS
    // --------------------

    @Nullable
    protected FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    protected Boolean isCurrentUserLogged(){ return (this.getCurrentUser() != null); }



}
