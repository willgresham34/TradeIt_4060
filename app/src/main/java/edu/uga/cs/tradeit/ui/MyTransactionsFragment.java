package edu.uga.cs.tradeit.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.models.Transaction;
import edu.uga.cs.tradeit.repository.ItemRepository;
import edu.uga.cs.tradeit.repository.TransactionRepository;

public class MyTransactionsFragment extends Fragment {

    private RecyclerView pendingBuysRecyclerView;
    private RecyclerView pendingSalesRecyclerView;
    private RecyclerView completedRecyclerView;
    private TextView pendingBuysEmptyTextView;
    private TextView pendingSalesEmptyTextView;
    private TextView completedEmptyTextView;

    private TransactionAdapter pendingBuysAdapter;
    private TransactionAdapter pendingSalesAdapter;
    private TransactionAdapter completedAdapter;

    private TransactionRepository transactionRepository;
    private ItemRepository itemRepository;
    private String currentUserId;

    private Map<String, String> categoryMap;

    public MyTransactionsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_transactions, container, false);

        pendingBuysRecyclerView = view.findViewById(R.id.rvPendingBuys);
        pendingSalesRecyclerView = view.findViewById(R.id.rvPendingSales);
        completedRecyclerView = view.findViewById(R.id.rvCompletedTransactions);

        pendingBuysEmptyTextView = view.findViewById(R.id.tvPendingBuysEmpty);
        pendingSalesEmptyTextView = view.findViewById(R.id.tvPendingSalesEmpty);
        completedEmptyTextView = view.findViewById(R.id.tvCompletedEmpty);

        pendingBuysRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        pendingSalesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        completedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(),
                    "You must be logged in to view your transactions.",
                    Toast.LENGTH_LONG).show();
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
            return view;
        }

        currentUserId = user.getUid();

        itemRepository = new ItemRepository();
        transactionRepository = new TransactionRepository();

        // Set up adapters
        pendingBuysAdapter = new TransactionAdapter(
                new ArrayList<>(),
                currentUserId,
                false,
                this::confirmTransaction,
                itemRepository,
                categoryMap
        );
        pendingSalesAdapter = new TransactionAdapter(
                new ArrayList<>(),
                currentUserId,
                true,
                this::confirmTransaction,
                itemRepository,
                categoryMap
        );
        completedAdapter = new TransactionAdapter(
                new ArrayList<>(),
                currentUserId,
                false,
                null,
                itemRepository,
                categoryMap
        );

        pendingBuysRecyclerView.setAdapter(pendingBuysAdapter);
        pendingSalesRecyclerView.setAdapter(pendingSalesAdapter);
        completedRecyclerView.setAdapter(completedAdapter);

        transactionRepository.setListener(allTransactions -> {
            if (!isAdded()) return;
            refreshTransactions();
        });

        return view;

    }

    private void refreshTransactions() {
        // Use the repo's helper methods as base, then partition
        List<Transaction> pendingAll = transactionRepository.pendingTransactionByUser(currentUserId);
        List<Transaction> completed = transactionRepository.completedTransactionByUser(currentUserId);

        List<Transaction> pendingBuys = new ArrayList<>();
        List<Transaction> pendingSales = new ArrayList<>();

        for (Transaction tx : pendingAll) {
            if (currentUserId.equals(tx.getBuyerId())) {
                pendingBuys.add(tx);
            } else if (currentUserId.equals(tx.getSellerId())) {
                pendingSales.add(tx);
            }
        }

        pendingBuysAdapter.setTransactions(pendingBuys);
        pendingSalesAdapter.setTransactions(pendingSales);
        completedAdapter.setTransactions(completed);

        pendingBuysEmptyTextView.setVisibility(pendingBuys.isEmpty() ? View.VISIBLE : View.GONE);
        pendingSalesEmptyTextView.setVisibility(pendingSales.isEmpty() ? View.VISIBLE : View.GONE);
        completedEmptyTextView.setVisibility(completed.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void confirmTransaction(Transaction tx) {
        transactionRepository.confirmTransaction(tx.getId());
        Toast.makeText(requireContext(),
                "Transaction confirmed.",
                Toast.LENGTH_SHORT).show();

        refreshTransactions();
    }
}
