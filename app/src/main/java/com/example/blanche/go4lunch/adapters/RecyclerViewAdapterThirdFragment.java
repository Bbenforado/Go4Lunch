package com.example.blanche.go4lunch.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.models.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class RecyclerViewAdapterThirdFragment extends FirestoreRecyclerAdapter<User, WorkmateViewHolder> {

    private final RequestManager glide;
    private Context context;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public RecyclerViewAdapterThirdFragment(@NonNull FirestoreRecyclerOptions<User> options,
                                            RequestManager glide) {
        super(options);
        this.glide = glide;
    }

    @NonNull
    @Override
    public WorkmateViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fragment_third_item, viewGroup, false);
        return new WorkmateViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull WorkmateViewHolder holder, int position, @NonNull User model) {
        holder.updateWithUsers(context, model, this.glide);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }
}
