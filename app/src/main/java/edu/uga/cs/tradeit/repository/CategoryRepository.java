package edu.uga.cs.tradeit.repository;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uga.cs.tradeit.models.Category;

public class CategoryRepository {

    private static final String TAG = "CAT_REPO";
    private final DatabaseReference categoriesRef;
    private final List<Category> cachedCategories = new ArrayList<>();

    public CategoryRepository() {
        categoriesRef = FirebaseDatabase.getInstance().getReference("categories");

        categoriesRef.orderByChild("name")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        cachedCategories.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Category cat = child.getValue(Category.class);
                            if (cat != null) {
                                cat.setId(child.getKey());
                                cachedCategories.add(cat);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.w(TAG, error.toString());
                    }
                });
    }

    /*
     * returns the current cached list of categories
     */
    public List<Category> getCategories() {
        return cachedCategories;
    }

    /*
    * Creates a category
    */
    public String createCategory(String name, String createdBy) {
        String key = categoriesRef.push().getKey();
        if (key == null) return null;

        Category cat = new Category();
        cat.setId(key);
        cat.setName(name);
        cat.setCreatedBy(createdBy);
        cat.setCreatedAt(System.currentTimeMillis());
        cat.setItemCount(0);

        categoriesRef.child(key).setValue(cat);
        return key;
    }

    /**
     * update a category name
     */
    public void updateCategory(Category cat) {
        if (cat == null || cat.getId() == null) {
            return;
        }

        if (cat.getItemCount() > 0) {
            // only update if category is empty
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", cat.getName());
        updates.put("createdAt", System.currentTimeMillis());

        categoriesRef.child(cat.getId()).updateChildren(updates);
    }

    /**
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

}
