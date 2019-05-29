package com.example.blanche.go4lunch.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.support.v7.app.ActionBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.UserCallback;
import com.example.blanche.go4lunch.activities.RestaurantDetailsActivity;
import com.example.blanche.go4lunch.adapters.RecyclerViewAdapterThirdFragment;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.models.User;
import com.example.blanche.go4lunch.utils.ItemClickSupport;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ThirdPageFragment extends BaseFragment {

    //private static final String COLLECTION_NAME = "users";
    public static final String KEY_POSITION = "position";
    /*private static final String KEY_RESTAURANT = "restaurant";
    public static final String RESTAURANT_NAME = "name";*/
    public static final String APP_PREFERENCES = "appPreferences";
    //public static final String CURRENT_USER_UID = "currentUserUid";
    public static final String REST_ID = "restId";
    public static final String KEY_ACTIVITY = "keyActivity";
    //Bundle bundle;
    private SharedPreferences preferences;
    private RecyclerViewAdapterThirdFragment adapter;
    private List<User> userList;
    private FirebaseFirestore firestoreRootRef;
    private CollectionReference itemsRef;

    //------------------
    //BIND VIEWS
    //---------------------
    @BindView(R.id.bar) ProgressBar bar;
    @BindView(R.id.fragment_page_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    //---------------------
    //CONSTRUCTOR
    //------------------------
    public ThirdPageFragment() {
        // Required empty public constructor
    }

    public static ThirdPageFragment newInstance(int position) {
        ThirdPageFragment fragment = new ThirdPageFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    //-------------------

    //-------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_third_page, container, false);
        ButterKnife.bind(this, result);
        preferences = getActivity().getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);

        toolbar = result.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(R.string.toolbar_title_for_third_fragment);

        configureNavigationView(navigationView, getActivity(), drawerLayout, getContext(), preferences, KEY_ACTIVITY);
        configureDrawerLayout(drawerLayout, toolbar, getActivity());
        configureRecyclerView();
        configureOnClickRecyclerView();
        return result;
    }

    //--------------------
    //CONFIGURATION
    //-------------------
    private void configureRecyclerView() {
        adapter = new RecyclerViewAdapterThirdFragment(generateOptionsForAdapter(UserHelper.getAllUsers()),
                Glide.with(this));
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(adapter.getItemCount()); // Scroll to bottom on new messages
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void configureOnClickRecyclerView() {
        ItemClickSupport.addTo(recyclerView, R.layout.fragment_third_item)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        //LAUNCH RESTAURANT ACTIVITY
                        userList = new ArrayList<>();
                        firestoreRootRef = FirebaseFirestore.getInstance();
                        itemsRef = firestoreRootRef.collection("users");

                        readData(new UserCallback() {
                            @Override
                            public void onCallback(List<User> list) {
                                if (list.get(position).isHasChosenRestaurant()) {
                                preferences.edit().putInt(KEY_ACTIVITY, 3).apply();
                                Bundle bundle = new Bundle();
                                bundle.putString(REST_ID, list.get(position).getRestaurantId());
                                launchRestaurantDetailsActivity(bundle);
                            } else {
                                Toast.makeText(getContext(), getContext().getString(R.string.this_user_didnt_chose_restaurant), Toast.LENGTH_SHORT).show();
                            }
                            }
                        });
                    }
                });
    }

    //-----------------
    //GET DATA
    //----------------------
    private void readData(UserCallback callback) {
        itemsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        User user = document.toObject(User.class);
                        userList.add(user);
                    }
                    callback.onCallback(userList);
                } else {
                    Log.d("TAG", "Error");
                }
            }
        });
    }


    private FirestoreRecyclerOptions<User> generateOptionsForAdapter(Query query) {
        return new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .setLifecycleOwner(this)
                .build();
    }

    //-------------------------------------
    //LAUNCH ACTIVITIES
    //--------------------------------------
    private void launchRestaurantDetailsActivity(Bundle bundle) {
        Intent restaurantDetailActivity = new Intent(getContext(), RestaurantDetailsActivity.class);
        restaurantDetailActivity.putExtras(bundle);
        startActivity(restaurantDetailActivity);
    }
}
