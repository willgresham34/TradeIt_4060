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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.uga.cs.tradeit.models.enums.ItemStatus;
import edu.uga.cs.tradeit.repository.ItemRepository;
import edu.uga.cs.tradeit.repository.TransactionRepository;


public class ItemDetailsFragment extends Fragment {

    private static final String ARG_ITEM_ID = "item_id";
    private static final String ARG_ITEM_NAME = "item_name";
    private static final String ARG_ITEM_PRICE = "item_price";
    private static final String ARG_ITEM_SELLER = "item_seller";
    private static final String ARG_ITEM_CREATED_AT = "item_created_at";
    private static final String ARG_CATEGORY_NAME = "category_name";

    private String itemId;
    private String itemName;
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
        args.putDouble(ARG_ITEM_PRICE, item.getPrice() != null ? item.getPrice() : 0.0);
        args.putString(ARG_ITEM_SELLER, item.getSellerId());
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

        if (itemSeller != null && !itemSeller.isEmpty()) {
            edu.uga.cs.tradeit.repository.UserRepository userRepo =
                    new edu.uga.cs.tradeit.repository.UserRepository();

            userRepo.getUserEmailById(itemSeller, email -> {
                if (!isAdded()) return;

                if (email != null && !email.isEmpty()) {
                    sellerTextView.setText(getString(R.string.seller_label, email));
                } else {
                    sellerTextView.setText(getString(R.string.seller_label, "Unknown seller"));
                }
            });
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
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(requireContext(),
                        "You must be logged in to accept or buy an item.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            ItemRepository itemRepo = new ItemRepository();

            itemRepo.getItemByIdAsync(itemId, item -> {
                if (item == null) {
                    Toast.makeText(requireContext(),
                            "Error: couldn't find item " + itemId,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // prevent buying your own item
                if (user.getUid().equals(item.getSellerId())) {
                    Toast.makeText(requireContext(),
                            "You cannot accept or buy your own item.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // only allow if item is available
                if (item.getStatus() == null ||
                        !ItemStatus.AVAILABLE.name().equals(item.getStatus())) {
                    Toast.makeText(requireContext(),
                            "This item is no longer available.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                TransactionRepository txRepo = new TransactionRepository();
                String txId = txRepo.startTransaction(item, user.getUid());

                if (txId == null) {
                    Toast.makeText(requireContext(),
                            "Failed to start transaction. Please try again.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(),
                            "Request sent to the seller.",
                            Toast.LENGTH_LONG).show();
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        });

        return view;
    }
}
