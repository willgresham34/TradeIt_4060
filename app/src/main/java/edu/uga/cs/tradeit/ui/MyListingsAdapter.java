package edu.uga.cs.tradeit.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.models.Item;
import edu.uga.cs.tradeit.models.enums.ItemStatus;

public class MyListingsAdapter extends RecyclerView.Adapter<MyListingsAdapter.MyListingViewHolder> {

    private List<Item> items;
    private Map<String, String> categoryNamesById;
    public interface OnItemInteractionListener {
        void onItemClick(Item item, View anchorView);
        void onItemLongClick(Item item, View anchorView);
    }

    private OnItemInteractionListener listener;

    public MyListingsAdapter(List<Item> items, Map<String, String> categoryNamesById, OnItemInteractionListener listener) {
        this.items = items;
        this.categoryNamesById = categoryNamesById;
        this.listener = listener;
    }

    public void setData(List<Item> items, Map<String, String> categoryNamesById) {
        this.items = items;
        this.categoryNamesById = categoryNamesById;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public MyListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_my_listing, parent, false);
        return new MyListingViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyListingViewHolder holder, int position) {
        Item item = items.get(position);

        holder.nameTextView.setText(item.getName());

        String categoryName = null;
        if (item.getCategoryId() != null && categoryNamesById != null) {
            categoryName = categoryNamesById.get(item.getCategoryId());
        }
        if (categoryName == null) {
            categoryName = "(Unknown category)";
        }

        String statusLabel = item.getStatus();
        if (statusLabel == null) {
            statusLabel = ItemStatus.AVAILABLE.name();
        }

        holder.categoryAndStatusTextView.setText(categoryName + " • " + statusLabel);

        Double price = item.getPrice();
        if (price == null || price == 0.0) {
            holder.priceTextView.setText(holder.itemView.getContext().getString(R.string.label_free));
        } else {
            holder.priceTextView.setText(
                    holder.itemView.getContext().getString(R.string.label_price_format, price)
            );
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onItemLongClick(item, v);
            }
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class MyListingViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView categoryAndStatusTextView;
        TextView priceTextView;

        public MyListingViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tvListingName);
            categoryAndStatusTextView = itemView.findViewById(R.id.tvListingCategoryAndStatus);
            priceTextView = itemView.findViewById(R.id.tvListingPrice);
        }
    }
}
