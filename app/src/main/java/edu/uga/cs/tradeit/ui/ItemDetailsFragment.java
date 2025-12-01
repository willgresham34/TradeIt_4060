package edu.uga.cs.tradeit.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.DateFormat;
import java.util.Date;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.models.Item;

public class ItemDetailsFragment extends Fragment {

    private static final String ARG_ITEM_ID = "item_id";
    private static final String ARG_ITEM_NAME = "item_name";
    private static final String ARG_ITEM_DESCRIPTION = "item_description";
    private static final String ARG_ITEM_PRICE = "item_price";
    private static final String ARG_ITEM_SELLER = "item_seller";
    private static final String ARG_ITEM_CREATED_AT = "item_created_at";
    private static final String ARG_CATEGORY_NAME = "category_name";

    private String itemId;
    private String itemName;
    private String itemDescription;
    private double itemPrice;
    private String itemSeller;
    private long itemCreatedAt;
    private String categoryName;

    public ItemDetailsFragment() {}

    public static ItemDetailsFragment newInstance(Item item, String categoryName) {
        ItemDetailsFragment fragment = new ItemDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ITEM_ID, item.getId());
        args.putString(ARG_ITEM_NAME, item.getName());
        args.putString(ARG_ITEM_DESCRIPTION, item.getDescription());
        args.putDouble(ARG_ITEM_PRICE, item.getPrice() != null ? item.getPrice() : 0.0);
        args.putString(ARG_ITEM_SELLER, item.getSellerName());
        args.putLong(ARG_ITEM_CREATED_AT, item.getCreatedAt());
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            itemId = getArguments().getString(ARG_ITEM_ID);
            itemName = getArguments().getString(ARG_ITEM_NAME);
            itemDescription = getArguments().getString(ARG_ITEM_DESCRIPTION);
            itemPrice = getArguments().getDouble(ARG_ITEM_PRICE, 0.0);
            itemSeller = getArguments().getString(ARG_ITEM_SELLER);
            itemCreatedAt = getArguments().getLong(ARG_ITEM_CREATED_AT, 0L);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_details, container, false);

        TextView nameTextView = view.findViewById(R.id.tvItemDetailName);
        TextView categoryTextView = view.findViewById(R.id.tvItemDetailCategory);
        TextView priceTextView = view.findViewById(R.id.tvItemDetailPrice);
        TextView descriptionTextView = view.findViewById(R.id.tvItemDetailDescription);
        TextView sellerTextView = view.findViewById(R.id.tvItemDetailSeller);
        TextView postedDateTextView = view.findViewById(R.id.tvItemDetailPostedDate);
        Button acceptBuyButton = view.findViewById(R.id.btnAcceptBuy);

        nameTextView.setText(itemName != null ? itemName : "");
        categoryTextView.setText(categoryName != null ? categoryName : "");

        if (itemPrice == 0.0) {
            priceTextView.setText(getString(R.string.label_free));
        } else {
            priceTextView.setText(getString(R.string.label_price_format, itemPrice));
        }

        if (itemDescription != null && !itemDescription.isEmpty()) {
            descriptionTextView.setText(itemDescription);
        } else {
            descriptionTextView.setText("");
        }

        if (itemSeller != null && !itemSeller.isEmpty()) {
            sellerTextView.setText(getString(R.string.seller_label, itemSeller));
        } else {
            sellerTextView.setText("");
        }

        if (itemCreatedAt > 0) {
            DateFormat df = DateFormat.getDateInstance();
            String dateStr = df.format(new Date(itemCreatedAt));
            postedDateTextView.setText(getString(R.string.posted_date_label, dateStr));
        } else {
            postedDateTextView.setText("");
        }

        acceptBuyButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "TODO: Accept/Buy logic for item: " + itemName,
                    Toast.LENGTH_LONG).show();
        });

        return view;
    }
}
