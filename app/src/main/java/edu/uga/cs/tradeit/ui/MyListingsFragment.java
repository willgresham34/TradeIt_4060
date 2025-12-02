package edu.uga.cs.tradeit.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

public class MyListingsFragment extends Fragment implements MyListingsAdapter.OnItemInteractionListener {

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

        adapter = new MyListingsAdapter(myItems, categoryNameById, this);
        recyclerView.setAdapter(adapter);

        loadData();

        return view;
    }

    private void loadData() {

        itemRepository.setListener(new ItemRepository.ItemsListener() {
            @Override
            public void onItemsChanged(List<Item> allItems) {
                if (!isAdded()) {
                    return;
                }

                myItems.clear();
                for (Item item : allItems) {
                    if (currentUserId.equals(item.getSellerId()) &&
                            ItemStatus.AVAILABLE.name().equals(item.getStatus())) {
                        myItems.add(item);
                    }
                }

                updateUI();
            }

            @Override
            public void onError(com.google.firebase.database.DatabaseError error) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(),
                        "Failed to load items: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

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


    // =======================
    //  Item interactions
    // =======================

    @Override
    public void onItemClick(Item item, View anchorView) {
        // Optional: navigate to details if you want tap behavior here.
        // For example:
        // ItemDetailsFragment fragment = ItemDetailsFragment.newInstance(item, categoryNameById.get(item.getCategoryId()));
        // requireActivity().getSupportFragmentManager()
        //         .beginTransaction()
        //         .replace(R.id.main, fragment)
        //         .addToBackStack(null)
        //         .commit();
    }

    // a long click will show edit/delete for seller on AVAILABLE items
    @Override
    public void onItemLongClick(Item item, View anchorView) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(),
                    "You must be logged in to modify items.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // only the seller can modify
        if (!user.getUid().equals(item.getSellerId())) {
            Toast.makeText(requireContext(),
                    "You can only edit or delete your own items.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // we can allow modifying AVAILABLE items
        if (item.getStatus() == null ||
                !ItemStatus.AVAILABLE.name().equals(item.getStatus())) {
            Toast.makeText(requireContext(),
                    "Only available items can be edited or deleted.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String[] options = {"Edit item", "Delete item"};
        new AlertDialog.Builder(requireContext())
                .setTitle(item.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditItemDialog(item);
                    } else if (which == 1) {
                        confirmDeleteItem(item);
                    }
                })
                .show();
    }

    private void showEditItemDialog(Item item) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_item, null);

        EditText nameEditText = dialogView.findViewById(R.id.etItemName);
        EditText priceEditText = dialogView.findViewById(R.id.etItemPrice);
        CheckBox freeCheckBox = dialogView.findViewById(R.id.cbItemFree);

        // prefill fields from existing item
        nameEditText.setText(item.getName());

        Double existingPrice = item.getPrice();
        boolean isFree = (existingPrice == null || existingPrice == 0.0);
        freeCheckBox.setChecked(isFree);
        if (isFree) {
            priceEditText.setEnabled(false);
            priceEditText.setText("");
        } else {
            priceEditText.setEnabled(true);
            priceEditText.setText(String.valueOf(existingPrice));
        }

        freeCheckBox.setOnCheckedChangeListener((buttonView, checked) -> {
            if (checked) {
                priceEditText.setEnabled(false);
                priceEditText.setText("");
            } else {
                priceEditText.setEnabled(true);
            }
        });

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_edit_item_title)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String newName = nameEditText.getText().toString().trim();
                    String priceText = priceEditText.getText().toString().trim();
                    boolean nowFree = freeCheckBox.isChecked();
                    handleEditItem(item, newName, priceText, nowFree);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
    private void handleEditItem(Item originalItem, String newName,
                                String priceText, boolean isFree) {
        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(requireContext(),
                    getString(R.string.error_item_name_required),
                    Toast.LENGTH_LONG).show();
            return;
        }

        Double price = 0.0;
        if (!isFree && !TextUtils.isEmpty(priceText)) {
            try {
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(),
                        getString(R.string.error_price_invalid),
                        Toast.LENGTH_LONG).show();
                return;
            }
        }

        Item updated = new Item();
        updated.setId(originalItem.getId());
        updated.setName(newName);
        updated.setPrice(price);
        updated.setStatus(originalItem.getStatus());

        itemRepository.updateItem(updated);

        Toast.makeText(requireContext(),
                "Item updated.",
                Toast.LENGTH_SHORT).show();
    }

    private void confirmDeleteItem(Item item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    itemRepository.deleteItem(item.getId());
                    Toast.makeText(requireContext(),
                            "Item deleted.",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

    }

}
