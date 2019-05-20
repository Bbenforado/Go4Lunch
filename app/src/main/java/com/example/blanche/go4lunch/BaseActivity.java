package com.example.blanche.go4lunch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.example.blanche.go4lunch.activities.RestaurantDetailsActivity;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import io.reactivex.annotations.NonNull;

public abstract class BaseActivity extends AppCompatActivity {

    Toolbar toolbar;
    ActionBar actionBar;
    public static final int SIGN_OUT_TASK = 10;
    public static final int DELETE_USER_TASK = 20;
    int AUTOCOMPLETE_REQUEST_CODE = 1;
    public static final String RESTAURANT_ID = "idRestaurant";
    public static final String APP_PREFERENCES = "appPreferences";


    protected OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "unknown error", Toast.LENGTH_LONG).show();
            }
        };
    }


    protected OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin) {
        System.out.println("and enter here?");
        System.out.println("origin = " + origin);
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin) {
                    case SIGN_OUT_TASK:
                        finish();
                        //startSignInActivity();
                        break;
                    case DELETE_USER_TASK:
                        System.out.println("and here?");
                        //finishAffinity();
                        //BaseActivity.this.finish();
                        System.exit(0);
                        break;
                    default:
                        break;
                }
            }
        };
    }


    // --------------------
    //
    // --------------------
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_item:
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.OVERLAY, fields)
                        .setTypeFilter(TypeFilter.ESTABLISHMENT)
                        .build(this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void handleResponseAfterAutocompleteSearch(int requestCode, int resultCode, Intent data) {
        System.out.println("request = " + requestCode);
        System.out.println("result = " + resultCode);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                String placeId = place.getId();
                Bundle bundle = new Bundle();
                bundle.putString(RESTAURANT_ID, placeId);
                System.out.println("id in base = " + placeId);
                Intent yourLunchActivity = new Intent(this, RestaurantDetailsActivity.class);
                yourLunchActivity.putExtras(bundle);
                startActivity(yourLunchActivity);


            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, "error " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();

            } else if (requestCode == RESULT_CANCELED) {
                Toast.makeText(this, "you cancelled operation", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
