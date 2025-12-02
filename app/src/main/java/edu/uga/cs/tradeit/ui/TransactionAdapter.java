package edu.uga.cs.tradeit.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.models.Item;
import edu.uga.cs.tradeit.models.Transaction;
import edu.uga.cs.tradeit.models.enums.TransactionStatus;
import edu.uga.cs.tradeit.repository.ItemRepository;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    public interface OnConfirmClickListener {
        void onConfirm(Transaction transaction);
    }

    private List<Transaction> transactions;
    private final String currentUserId;
    private final boolean showConfirmButton;
    private final OnConfirmClickListener confirmClickListener;
    private final ItemRepository itemRepository;

    private final Map<String, String> categoryMap;

    public TransactionAdapter(List<Transaction> transactions,
                              String currentUserId,
                              boolean showConfirmButton,
                              OnConfirmClickListener confirmClickListener,
                              ItemRepository itemRepository,
                              Map<String, String> categoryMap) {

        this.transactions = transactions;
        this.currentUserId = currentUserId;
        this.showConfirmButton = showConfirmButton;
        this.confirmClickListener = confirmClickListener;
        this.itemRepository = itemRepository;
        this.categoryMap = categoryMap;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_transaction, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction tx = transactions.get(position);

        Item item = itemRepository.getItemById(tx.getItemId());
        String itemName = item != null && item.getName() != null ? item.getName() : "(Unknown item)";

        String role;
        String otherParty;

        if (currentUserId != null && currentUserId.equals(tx.getBuyerId())) {
            role = "Buying";
            otherParty = tx.getSellerName();
        } else if (currentUserId != null && currentUserId.equals(tx.getSellerId())) {
            role = "Selling";
            otherParty = tx.getBuyerName();
        } else {
            role = "";
            otherParty = null;
        }

        String priceText;
        Double price = tx.getPrice();
        if (price == null || price == 0.0 || tx.isFree()) {
            priceText = holder.itemView.getContext().getString(R.string.label_free);
        } else {
            priceText = holder.itemView.getContext().getString(R.string.label_price_format, price);
        }

        StringBuilder roleAndPrice = new StringBuilder();
        if (!role.isEmpty()) {
            roleAndPrice.append(role);
        }
        if (roleAndPrice.length() > 0) {
            roleAndPrice.append(" • ");
        }
        roleAndPrice.append(priceText);

        if (otherParty != null && !otherParty.isEmpty()) {
            roleAndPrice.append(" • ").append(otherParty);
        }

        holder.roleAndPriceTextView.setText(roleAndPrice.toString());

        long timestamp = tx.getCreatedAt();
        if (tx.getCompletedAt() != null && tx.getCompletedAt() > 0) {
            timestamp = tx.getCompletedAt();
        }

        if (timestamp > 0) {
            DateFormat df = DateFormat.getDateInstance();
            holder.dateTextView.setText(df.format(new Date(timestamp)));
        } else {
            holder.dateTextView.setText("");
        }

        if (showConfirmButton &&
                TransactionStatus.PENDING.name().equalsIgnoreCase(tx.getStatus())) {

            holder.confirmButton.setVisibility(View.VISIBLE);
            holder.confirmButton.setOnClickListener(v -> {
                if (confirmClickListener != null) {
                    confirmClickListener.onConfirm(tx);
                }
            });

        } else {
            holder.confirmButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        TextView roleAndPriceTextView;
        TextView dateTextView;
        Button confirmButton;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);

            itemNameTextView = itemView.findViewById(R.id.tvTxItemName);
            roleAndPriceTextView = itemView.findViewById(R.id.tvTxRoleAndPrice);
            dateTextView = itemView.findViewById(R.id.tvTxDate);
            confirmButton = itemView.findViewById(R.id.btnConfirmTx);
        }
    }
}
