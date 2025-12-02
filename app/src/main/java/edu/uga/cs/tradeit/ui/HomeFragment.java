package edu.uga.cs.tradeit.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import edu.uga.cs.tradeit.repository.CategoryRepository;

public class HomeFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private Button addCategoryButton;

    private CategoryAdapter adapter;
    private List<Category> categoryList = new ArrayList<>();
    private CategoryRepository categoryRepository;
    private ValueEventListener categoryListener;

    public HomeFragment() {
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        setHasOptionsMenu(true);
        categoryRepository = new CategoryRepository();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerCategories);
        progressBar = view.findViewById(R.id.progressCategories);
        emptyTextView = view.findViewById(R.id.tvEmptyCategories);
        addCategoryButton = view.findViewById(R.id.btnAddCategory);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CategoryAdapter(categoryList, this);
        recyclerView.setAdapter(adapter);

        addCategoryButton.setOnClickListener(v -> showAddCategoryDialog());

        subscribeToCategories();

        return view;
    }

    private void subscribeToCategories() {
        showLoading(true);
        categoryListener = categoryRepository.listenToCategories(new CategoryRepository.CategoryListener() {
            @Override
            public void onCategoriesChanged(List<Category> categories) {
                if (!isAdded()) {
                    return;
                }

                showLoading(false);
                categoryList.clear();
                categoryList.addAll(categories);
                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onError(DatabaseError error) {
                showLoading(false);
                Toast.makeText(requireContext(),
                        "Failed to load categories: " + error.getMessage(),
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
        if (categoryList.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddCategoryDialog() {
        final EditText input = new EditText(requireContext());
        input.setHint(getString(R.string.hint_category_name));
        input.setSingleLine(true);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_add_category_title)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    handleAddCategory(name);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void handleAddCategory(String name) {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(requireContext(),
                    getString(R.string.error_category_name_required),
                    Toast.LENGTH_LONG).show();
            return;
        }

        for (Category c : categoryList) {
            if (c.getName() != null &&
                    c.getName().trim().equalsIgnoreCase(name)) {
                Toast.makeText(requireContext(),
                        getString(R.string.error_category_already_exists),
                        Toast.LENGTH_LONG).show();
                return;
            }
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(),
                    "You must be logged in to add a category",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String userId = user.getUid();
        categoryRepository.addCategory(name, userId, task -> {
            if (task.isSuccessful()) {
            } else {
                String message = task.getException() != null
                        ? task.getException().getMessage()
                        : "Unknown error";
                Toast.makeText(requireContext(),
                        getString(R.string.error_add_category_failed, message),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onCategoryClick(Category category) {
        CategoryItemsFragment fragment = CategoryItemsFragment.newInstance(
                category.getId(),
                category.getName()
        );

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
}
