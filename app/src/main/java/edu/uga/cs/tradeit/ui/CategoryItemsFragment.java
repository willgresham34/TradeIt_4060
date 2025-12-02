package edu.uga.cs.tradeit.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.models.Category;
import edu.uga.cs.tradeit.models.Item;
import edu.uga.cs.tradeit.models.enums.ItemStatus;
import edu.uga.cs.tradeit.repository.CategoryRepository;
import edu.uga.cs.tradeit.repository.ItemRepository;

public class CategoryItemsFragment extends Fragment implements ItemAdapter.OnItemClickListener {

    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_CATEGORY_NAME = "category_name";

    private Category currentCategory;
    private String categoryId;
    private String categoryName;

    private TextView titleTextView;
    private Button addItemButton;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private RecyclerView recyclerView;

    private ItemAdapter adapter;
    private List<Item> items = new ArrayList<>();

    private ItemRepository itemRepository;
    private CategoryRepository categoryRepository;

    private ValueEventListener itemsListener;
    private ValueEventListener categoryListener;

    public CategoryItemsFragment() {}

    public static CategoryItemsFragment newInstance(String categoryId, String categoryName) {
        CategoryItemsFragment fragment = new CategoryItemsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
        }

        itemRepository = new ItemRepository();
        categoryRepository = new CategoryRepository();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_items, container, false);

        titleTextView = view.findViewById(R.id.tvCategoryItemsTitle);
        addItemButton = view.findViewById(R.id.btnAddItem);
        progressBar = view.findViewById(R.id.progressItems);
        emptyTextView = view.findViewById(R.id.tvEmptyItems);
        recyclerView = view.findViewById(R.id.recyclerItems);

        if (categoryName != null) {
            String title = getString(R.string.title_items_in_category, categoryName);
            titleTextView.setText(title);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ItemAdapter(items, this);
        recyclerView.setAdapter(adapter);

        addItemButton.setOnClickListener(v -> showAddItemDialog());

        subscribeToItems();
        subscribeToCategory();

        Button btnCategoryActions = view.findViewById(R.id.btnCategoryActions);
        btnCategoryActions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            popup.getMenu().add(0, 1, 0, "Update category");
            popup.getMenu().add(0, 2, 1, "Delete category");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    // Update category
                    if (currentCategory != null) {
                        showUpdateCategoryDialog(currentCategory);
                    } else {
                        Toast.makeText(requireContext(),
                                "Category not loaded yet",
                                Toast.LENGTH_SHORT).show();
                    }
                    return true;
                } else if (item.getItemId() == 2) {
                    // Delete category
                    if (currentCategory != null) {
                        handleDeleteCategory(currentCategory);
                    } else {
                        Toast.makeText(requireContext(),
                                "Category not loaded yet",
                                Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            });

            popup.show();
        });

        return view;
    }

    private void subscribeToItems() {
        showLoading(true);

        itemsListener = itemRepository.listenToItemsForCategory(categoryId, new ItemRepository.ItemsListener() {
            @Override
            public void onItemsChanged(List<Item> loadedItems) {
                showLoading(false);
                items.clear();
                // Only show AVAILABLE items
                for (Item item : loadedItems) {
                    if (ItemStatus.AVAILABLE.name().equals(item.getStatus())) {
                        items.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onError(DatabaseError error) {
                showLoading(false);
                Toast.makeText(requireContext(),
                        "Failed to load items: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void subscribeToCategory() {
        categoryListener = categoryRepository.listenToCategories(new CategoryRepository.CategoryListener() {
            @Override
            public void onCategoriesChanged(List<Category> categories) {
                for (Category c : categories) {
                    if (c.getId() != null && c.getId().equals(categoryId)) {
                        currentCategory = c;

                        // update title if name changed
                        if (c.getName() != null) {
                            categoryName = c.getName();
                            String title = getString(R.string.title_items_in_category, categoryName);
                            titleTextView.setText(title);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onError(DatabaseError error) {
                Toast.makeText(requireContext(),
                        "Failed to load category: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(loading ? View.GONE : View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
    }

    private void updateEmptyState() {
        if (items.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddItemDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_item, null);

        EditText nameEditText = dialogView.findViewById(R.id.etItemName);
        EditText priceEditText = dialogView.findViewById(R.id.etItemPrice);
        CheckBox freeCheckBox = dialogView.findViewById(R.id.cbItemFree);

        freeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                priceEditText.setEnabled(false);
                priceEditText.setText("");
            } else {
                priceEditText.setEnabled(true);
            }
        });

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_add_item_title)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String name = nameEditText.getText().toString().trim();
                    String priceText = priceEditText.getText().toString().trim();
                    boolean isFree = freeCheckBox.isChecked();
                    handleAddItem(name, priceText, isFree);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void handleAddItem(String name, String priceText, boolean isFree) {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(requireContext(),
                    getString(R.string.error_item_name_required),
                    Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(),
                    getString(R.string.error_must_be_logged_in_to_add_item),
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
        } else if (!isFree && TextUtils.isEmpty(priceText)) {
            isFree = true;
            price = 0.0;
        }

        Item item = new Item();
        item.setName(name);
        item.setCategoryId(categoryId);
        item.setSellerId(user.getUid());
        item.setPrice(price);
        item.setStatus(ItemStatus.AVAILABLE.name());

        int result = itemRepository.addItem(item);
        if (result == 0) {
            Toast.makeText(requireContext(),
                    getString(R.string.error_add_item_failed, "could not create item"),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(Item item) {
        ItemDetailsFragment fragment = ItemDetailsFragment.newInstance(item, categoryName);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
         if (categoryListener != null) {
             categoryRepository.removeCategoriesListener(categoryListener);
             categoryListener = null;
         }
    }

    private void showUpdateCategoryDialog(@NonNull Category category) {
        final EditText input = new EditText(requireContext());
        input.setHint(getString(R.string.hint_category_name));
        input.setSingleLine(true);

        if (category.getName() != null) {
            input.setText(category.getName());
            input.setSelection(input.getText().length());
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_update_category_title)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    handleUpdateCategory(category, name);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void handleUpdateCategory(@NonNull Category category, String name) {
        if (!items.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Cannot update a category with items.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(requireContext(),
                    getString(R.string.error_category_name_required),
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (name.equals(category.getName())) {
            return;
        }

        category.setName(name);
        categoryRepository.updateCategory(category);

        Toast.makeText(requireContext(),
                "Category updated",
                Toast.LENGTH_SHORT).show();
    }

    private void handleDeleteCategory(@NonNull Category category) {
        if (!items.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Cannot delete a category with items.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete category")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    categoryRepository.deleteCategory(category.getId());
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
