package com.example.blanche.go4lunch.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.adapters.RecyclerViewAdapter;
import com.example.blanche.go4lunch.adapters.RecyclerViewAdapterThirdFragment;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.models.User;
import com.example.blanche.go4lunch.utils.ItemClickSupport;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ThirdPageFragment extends Fragment {
    private static final String COLLECTION_NAME = "users";
    public static final String KEY_POSITION = "position";
    private static final String KEY_RESTAURANT = "restaurant";
    public static final String RESTAURANT_NAME = "name";
    public static final String APP_PREFERENCES = "appPreferences";
    public static final String CURRENT_USER_UID = "currentUserUid";
    Bundle bundle;
    SharedPreferences preferences;
    private RecyclerViewAdapterThirdFragment adapter;
    @Nullable private User currentUser;


    @BindView(R.id.fragment_page_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.fragment_page_swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_third_page, container, false);
        ButterKnife.bind(this, result);
        preferences = getActivity().getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        //getCurrentUserFromFirestore();
        configureRecyclerView();

        return result;
    }

    //--------------------
    //CONFIGURATION
    //-------------------
    private void configureRecyclerView() {
        //here we fetch an arrayList of objects restaurants and set the adapter to the
        //recycler view, something like:
        //restaurantResultsList = new ArrayList<>();
        /*String userUid = preferences.getString(CURRENT_USER_UID, null);
        UserHelper.getUser(userUid).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                currentUser = documentSnapshot.toObject(User.class);
            }
        });*/

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

    private void configureSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //refresh the page, request the api
            }
        });
    }

    private void configureOnClickRecyclerView() {
        ItemClickSupport.addTo(recyclerView, R.layout.fragment_third_item)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        //LAUNCH RESTAURANT ACTIVITY
                        //which displays picture of restaurant and some informations
                    }
                });
    }

    private FirestoreRecyclerOptions<User> generateOptionsForAdapter(Query query) {
        return new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .setLifecycleOwner(this)
                .build();
    }


}
