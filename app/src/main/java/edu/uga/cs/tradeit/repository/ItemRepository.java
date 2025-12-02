package edu.uga.cs.tradeit.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.uga.cs.tradeit.models.Item;
import edu.uga.cs.tradeit.models.enums.ItemStatus;

public class ItemRepository {

    private static final String TAG = "ITEM_REPO";
    private final DatabaseReference itemsRef;
    private final DatabaseReference categoriesRef;
    private final List<Item> cachedItems = new ArrayList<>();
    public interface ItemsListener {
        void onItemsChanged(List<Item> items);
        void onError(DatabaseError error);
    }
    private ItemsListener itemsListener;
    public void setListener(ItemsListener listener) {
        this.itemsListener = listener;
    }

    public ItemRepository() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        itemsRef = db.getReference("items");
        categoriesRef = db.getReference("categories");

        itemsRef.orderByChild("createdAt")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        cachedItems.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Item item = child.getValue(Item.class);
                            if (item != null) {
                                item.setId(child.getKey());
                                // add at front so list is newest-first
                                cachedItems.add(0,item);
                            }
                        }
                        if (itemsListener != null) {
                            itemsListener.onItemsChanged(new ArrayList<>(cachedItems));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w(TAG, error.toString());
                    }
                });
    }

    // getItems - may not need
    public List<Item> getItems() {
        return cachedItems;
    }

    // getItemsByCategory
    public List<Item> getItemsByCategory(String catId) {
        List<Item> result = new ArrayList<>();
        if (catId == null) {
            return result;
        }

        for (Item item : cachedItems) {
            if (!catId.equals(item.getCategoryId())) {
                continue;
            }
            if (!ItemStatus.AVAILABLE.name().equals(item.getStatus())) {
                continue;
            }
            result.add(item);
        }
        return result;
    }

    // getItemsByUser
    public List<Item> getItemsByUser(String userId) {
        List<Item> result = new ArrayList<>();
        if (userId == null) {
            return result;
        }

        for (Item item: cachedItems) {
            if (userId.equals(item.getSellerId())) {
                result.add(item);
            }
        }
        return result;
    }

    public interface ItemLoadCallback {
        void onItemLoaded(@Nullable Item item);
    }

    public void getItemByIdAsync(String itemId, ItemLoadCallback callback) {
        if (itemId == null) {
            callback.onItemLoaded(null);
            return;
        }

        for (Item cached : cachedItems) {
            if (itemId.equals(cached.getId())) {
                callback.onItemLoaded(cached);
                return;
            }
        }

        itemsRef.child(itemId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                        callback.onItemLoaded(null);
                        return;
                    }

                    Item item = task.getResult().getValue(Item.class);
                    if (item != null) {
                        item.setId(task.getResult().getKey());
                    }
                    callback.onItemLoaded(item);
                });
    }

    // getItemById
    public Item getItemById(String itemId) {
        if (itemId == null) {
            Log.w(TAG, "ItemId null, no item returned");
            return null;
        }
        for (Item item : cachedItems) {
            if (itemId.equals(item.getId())) {
                return item;
            }
        }
        Log.w(TAG, "Item not found");
        Log.w(TAG, cachedItems.toString());
        return null;
    }

    /*
     * addItem
     */
    public int addItem(Item item) {
        if (item == null) {
            return 0;
        }

        String key = itemsRef.push().getKey();
        if (key == null) {
            return 0;
        }

        long now = System.currentTimeMillis();
        item.setId(key);
        if (item.getCreatedAt() == 0L) {
            item.setCreatedAt(now);
        }
        if (item.getStatus() == null) {
            item.setStatus(ItemStatus.AVAILABLE.name());
        }

        itemsRef.child(key).setValue(item);
        return 1;
    }


    /*
     * deletes Item
     */
    public void deleteItem(String itemId) {
        if (itemId == null) {
            return;
        }

        Item existing = getItemById(itemId);
        if (existing == null) {
            return;
        }

        if (existing.getStatus() != null &&
                existing.getStatus().equals(ItemStatus.PENDING.name())) {
            return;
        }

        String categoryId = existing.getCategoryId();

        itemsRef.child(itemId).removeValue();

        incrementCategoryItemCount(categoryId, -1);
    }

    /**
     * update an items name or price
     */
    public void updateItem(Item item) {
        if (item == null || item.getId() == null) {
            return;
        }

        Item existing = getItemById(item.getId());
        if (existing == null) {
            return;
        }

        item.setCategoryId(existing.getCategoryId());
        item.setCreatedAt(existing.getCreatedAt());
        item.setSellerId(existing.getSellerId());

        itemsRef.child(item.getId()).setValue(item);
    }

    /*
     * increments categories item count
     */
    private void incrementCategoryItemCount(String categoryId, int delta) {
        if (categoryId == null || delta == 0) {
            return;
        }

        categoriesRef.child(categoryId).child("itemCount")
                .runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        Integer current = currentData.getValue(Integer.class);
                        if (current == null) {
                            current = 0;
                        }
                        currentData.setValue(current + delta);
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                    }
                });
    }

    public ValueEventListener listenToItemsForCategory(String categoryId, final ItemsListener listener) {
        if (categoryId == null) {
            return null;
        }

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Item> items = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Item item = child.getValue(Item.class);
                    if (item != null) {
                        item.setId(child.getKey());
                        items.add(item);
                    }
                }

                // sort from newest to oldest based on createdAt
                items.sort((i1, i2) -> Long.compare(i2.getCreatedAt(), i1.getCreatedAt()));

                listener.onItemsChanged(items);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error);
            }
        };

        // filter by categoryId in the query
        return itemsRef.orderByChild("categoryId")
                .equalTo(categoryId)
                .addValueEventListener(valueEventListener);
    }

}
