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

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.models.Transaction;
import edu.uga.cs.tradeit.repository.ItemRepository;
import edu.uga.cs.tradeit.repository.TransactionRepository;

public class MyTransactionsFragment extends Fragment {

    private RecyclerView pendingRecyclerView;
    private RecyclerView completedRecyclerView;
    private TextView pendingEmptyTextView;
    private TextView completedEmptyTextView;

    private TransactionAdapter pendingAdapter;
    private TransactionAdapter completedAdapter;

    private TransactionRepository transactionRepository;
    private ItemRepository itemRepository;
    private String currentUserId;

    public MyTransactionsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_transactions, container, false);

        pendingRecyclerView = view.findViewById(R.id.rvPendingTransactions);
        completedRecyclerView = view.findViewById(R.id.rvCompletedTransactions);
        pendingEmptyTextView = view.findViewById(R.id.tvPendingEmpty);
        completedEmptyTextView = view.findViewById(R.id.tvCompletedEmpty);

        pendingRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
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

        pendingAdapter = new TransactionAdapter(
                new ArrayList<>(),
                currentUserId,
                true,
                this::confirmTransaction,
                itemRepository
        );

        completedAdapter = new TransactionAdapter(
                new ArrayList<>(),
                currentUserId,
                false,
                null,
                itemRepository
        );

        pendingRecyclerView.setAdapter(pendingAdapter);
        completedRecyclerView.setAdapter(completedAdapter);

        refreshTransactions();

        return view;
    }

    private void refreshTransactions() {
        List<Transaction> pending = transactionRepository.pendingTransactionByUser(currentUserId);
        List<Transaction> completed = transactionRepository.completedTransactionByUser(currentUserId);

        pendingAdapter.setTransactions(pending);
        completedAdapter.setTransactions(completed);

        pendingEmptyTextView.setVisibility(pending.isEmpty() ? View.VISIBLE : View.GONE);
        completedEmptyTextView.setVisibility(completed.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void confirmTransaction(Transaction tx) {
        transactionRepository.confirmTransaction(tx.getId());
        Toast.makeText(requireContext(),
                "Transaction confirmed.",
                Toast.LENGTH_SHORT).show();

        // refresh lists after confirmation
        refreshTransactions();
    }
}
