package com.example.blanche.go4lunch.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
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
}
