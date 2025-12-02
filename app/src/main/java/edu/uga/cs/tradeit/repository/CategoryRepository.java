package edu.uga.cs.tradeit.repository;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uga.cs.tradeit.models.Category;

public class CategoryRepository {

    private final DatabaseReference categoriesRef;
    private List<Category> cachedCategories = new ArrayList<>();

    public CategoryRepository() {
        categoriesRef = FirebaseDatabase.getInstance().getReference("categories");
    }

    // listener interface for live updates
    public interface CategoryListener {
        void onCategoriesChanged(List<Category> categories);
        void onError(DatabaseError error);
    }

    // listener attached to get live updates of categories
    public ValueEventListener listenToCategories(final CategoryListener listener) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Category> categories = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Category category = child.getValue(Category.class);
                    if (category != null) {
                        if (category.getId() == null || category.getId().isEmpty()) {
                            category.setId(child.getKey());
                        }
                        categories.add(category);
                    }
                }

                // sort alphabetically
                Collections.sort(categories, new Comparator<Category>() {
                    @Override
                    public int compare(Category c1, Category c2) {
                        if (c1.getName() == null) return -1;
                        if (c2.getName() == null) return 1;
                        return c1.getName().toLowerCase().compareTo(c2.getName().toLowerCase());
                    }
                });
                cachedCategories = categories;

                listener.onCategoriesChanged(categories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error);
            }
        };

        categoriesRef.addValueEventListener(valueEventListener);
        return valueEventListener;
    }

    // add a new category node
    public void addCategory(String name, String createdBy, OnCompleteListener<Void> listener) {
        String key = categoriesRef.push().getKey();

        if (key == null) {
            if (listener != null) {
                listener.onComplete(
                        Tasks.forException(new Exception("Failed to generate key"))
                );
            }
            return;
        }

        long now = System.currentTimeMillis();
        Category category = new Category();
        category.setId(key);
        category.setName(name);
        category.setCreatedBy(createdBy);
        category.setCreatedAt(now);
        category.setItemCount(0);

        categoriesRef.child(key).setValue(category)
                .addOnCompleteListener(listener);
    }

    public void updateCategory(Category cat) {
        if (cat == null || cat.getId() == null) {
            return;
        }

        if (cat.getItemCount() > 0) {
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", cat.getName());
        updates.put("createdAt", System.currentTimeMillis());

        categoriesRef.child(cat.getId()).updateChildren(updates);
    }

    /*
     * deletes a category.
     */
    public void deleteCategory(String categoryId) {
        if (categoryId == null) {
            return;
        }

        Category cat = null;
        for (Category c : cachedCategories) {
            if (categoryId.equals(c.getId())) {
                cat = c;
                break;
            }
        }

        if (cat == null || cat.getItemCount() > 0) {
            return;
        }

        categoriesRef.child(categoryId).removeValue();
    }
    public void removeCategoriesListener(ValueEventListener listener) {
        if (listener != null) {
            categoriesRef.removeEventListener(listener);
        }
    }
}
