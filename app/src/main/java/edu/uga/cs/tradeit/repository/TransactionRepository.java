package edu.uga.cs.tradeit.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import edu.uga.cs.tradeit.models.Item;
import edu.uga.cs.tradeit.models.Transaction;
import edu.uga.cs.tradeit.models.enums.ItemStatus;
import edu.uga.cs.tradeit.models.enums.TransactionStatus;


public class TransactionRepository {

    private static final String TAG = "TX_REPO";

    private final DatabaseReference transactionsRef;
    private final DatabaseReference itemsRef;
    private final List<Transaction> cachedTransactions = new ArrayList<>();

    public TransactionRepository() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        transactionsRef = db.getReference("transactions");
        itemsRef = db.getReference("items");

        transactionsRef.orderByChild("createdAt")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        cachedTransactions.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Transaction tx = child.getValue(Transaction.class);
                            if (tx != null) {
                                tx.setId(child.getKey());
                                // add at front so list is newest-first
                                cachedTransactions.add(0, tx);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w(TAG, "transactions listener cancelled", error.toException());
                    }
                });
    }

    /*
     * Start a transaction for an item and a buyer.
     * returns transaction id
     */
    public String startTransaction(Item item, String buyerId) {
        if (item == null || item.getId() == null || item.getSellerId() == null || buyerId == null) {
            return null;
        }

        if (item.getStatus() != null &&
                !item.getStatus().equals(ItemStatus.AVAILABLE.name())) {
            return null;
        }

        String txId = transactionsRef.push().getKey();
        if (txId == null) {
            return null;
        }

        long now = System.currentTimeMillis();

        Transaction tx = new Transaction();
        tx.setId(txId);
        tx.setItemId(item.getId());
        tx.setCategoryId(item.getCategoryId());
        tx.setSellerId(item.getSellerId());
        tx.setBuyerId(buyerId);
        tx.setPrice(item.getPrice());
        tx.setFree(item.getPrice() == null || item.getPrice() == 0.0);
        tx.setCreatedAt(now);
        tx.setStatus(TransactionStatus.PENDING.name());
        tx.setCompletedAt(null);

        tx.setSellerName(item.getSellerName());

        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current != null) {
            String buyerName = current.getDisplayName();
            if (buyerName == null || buyerName.isEmpty()) {
                buyerName = current.getEmail();
            }
            tx.setBuyerName(buyerName);
        }

        transactionsRef.child(txId).setValue(tx);

        itemsRef.child(item.getId()).child("status").setValue(ItemStatus.PENDING.name());

        return txId;
    }


    public List<Transaction> pendingTransactionByUser(String userId) {
        List<Transaction> result = new ArrayList<>();
        if (userId == null) {
            return result;
        }

        for (Transaction tx : cachedTransactions) {
            if (!tx.getStatus().equalsIgnoreCase(TransactionStatus.PENDING.name())) {
                continue;
            }
            if (userId.equals(tx.getBuyerId()) || userId.equals(tx.getSellerId())) {
                result.add(tx);
            }
        }
        return result;
    }

    public List<Transaction> completedTransactionByUser(String userId) {
        List<Transaction> result = new ArrayList<>();
        if (userId == null) {
            return result;
        }

        for (Transaction tx : cachedTransactions) {
            if (tx.getStatus().equalsIgnoreCase(TransactionStatus.PENDING.name())) {
                continue;
            }
            if (userId.equals(tx.getBuyerId()) || userId.equals(tx.getSellerId())) {
                result.add(tx);
            }
        }
        return result;
    }

    /*
     * confirms a pending transaction.
     */
    public void confirmTransaction(String transactionId) {
        if (transactionId == null) {
            return;
        }

        Transaction existing = null;
        for (Transaction tx : cachedTransactions) {
            if (transactionId.equals(tx.getId())) {
                existing = tx;
                break;
            }
        }

        if (existing == null) {
            return;
        }

        if (!existing.getStatus().equalsIgnoreCase(TransactionStatus.PENDING.name())) {
            return;
        }

        transactionsRef.child(transactionId).child("status").setValue(TransactionStatus.COMPLETED.name());
        transactionsRef.child(transactionId).child("completedAt").setValue(System.currentTimeMillis());

        if (existing.getItemId() != null) {
            itemsRef.child(existing.getItemId())
                    .child("status")
                    .setValue(ItemStatus.SOLD.name());
        }
    }
}
