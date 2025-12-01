package edu.uga.cs.tradeit.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.models.Category;
import edu.uga.cs.tradeit.models.Item;
import edu.uga.cs.tradeit.models.enums.ItemStatus;
import edu.uga.cs.tradeit.repository.CategoryRepository;
import edu.uga.cs.tradeit.repository.ItemRepository;

public class MyListingsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyTextView;

    private MyListingsAdapter adapter;
    private ItemRepository itemRepository;
    private CategoryRepository categoryRepository;

    private String currentUserId;
    private List<Item> myItems = new ArrayList<>();
    private Map<String, String> categoryNameById = new HashMap<>();

    public MyListingsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_listings, container, false);

        recyclerView = view.findViewById(R.id.rvMyListings);
        emptyTextView = view.findViewById(R.id.tvMyListingsEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(),
                    "You must be logged in to view your listings.",
                    Toast.LENGTH_LONG).show();
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
            return view;
        }

        currentUserId = user.getUid();

        itemRepository = new ItemRepository();
        categoryRepository = new CategoryRepository();

        adapter = new MyListingsAdapter(myItems, categoryNameById);
        recyclerView.setAdapter(adapter);

        loadData();

        return view;
    }

    private void loadData() {
        List<Item> allMyItems = itemRepository.getItemsByUser(currentUserId);
        myItems.clear();
        for (Item item : allMyItems) {
            if (ItemStatus.AVAILABLE.name().equals(item.getStatus())) {
                myItems.add(item);
            }
        }

        categoryRepository.listenToCategories(new CategoryRepository.CategoryListener() {
            @Override
            public void onCategoriesChanged(List<Category> categories) {
                categoryNameById.clear();
                for (Category c : categories) {
                    if (c.getId() != null && c.getName() != null) {
                        categoryNameById.put(c.getId(), c.getName());
                    }
                }
                updateUI();
            }

            @Override
            public void onError(DatabaseError error) {
                Toast.makeText(requireContext(),
                        "Failed to load categories: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
                updateUI();
            }
        });
    }

    private void updateUI() {
        adapter.setData(myItems, categoryNameById);
        if (myItems.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
