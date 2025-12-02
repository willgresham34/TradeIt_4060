package edu.uga.cs.tradeit.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.models.Item;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    // extended listener for long-click actions to show edit/delete
    public interface OnItemLongClickListener extends OnItemClickListener {
        void onItemLongClick(Item item, View anchorView);
    }

    private List<Item> items;
    private OnItemClickListener listener;

    public ItemAdapter(List<Item> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);
        holder.nameTextView.setText(item.getName());

        String priceText;
        Double price = item.getPrice();
        if (price == null || price == 0.0) {
            priceText = holder.itemView.getContext().getString(R.string.label_free);
        } else {
            priceText = holder.itemView.getContext().getString(R.string.label_price_format, price);
        }

        String seller = item.getSellerName();
        if (seller == null || seller.isEmpty()) {
            seller = "";
        }

        String dateText = "";
        if (item.getCreatedAt() > 0) {
            java.text.DateFormat df = java.text.DateFormat.getDateInstance();
            dateText = df.format(new java.util.Date(item.getCreatedAt()));
        }

        String meta;
        if (!seller.isEmpty() && !dateText.isEmpty()) {
            meta = priceText + " • " + seller + " • " + dateText;
        } else if (!seller.isEmpty()) {
            meta = priceText + " • " + seller;
        } else if (!dateText.isEmpty()) {
            meta = priceText + " • " + dateText;
        } else {
            meta = priceText;
        }

        holder.metaTextView.setText(meta);

        // normal click will open details
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

        // a long click will give fragment a chance to show edit/delete
        holder.itemView.setOnLongClickListener(v -> {
            if (listener instanceof OnItemLongClickListener) {
                ((OnItemLongClickListener) listener).onItemLongClick(item, v);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView descriptionTextView;
        TextView metaTextView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tvItemName);
            descriptionTextView = itemView.findViewById(R.id.tvItemDescription);
            metaTextView = itemView.findViewById(R.id.tvItemMeta);
        }
    }

}
