package edu.uga.cs.tradeit.repository;

import edu.uga.cs.tradeit.models.Transaction;

public class TransactionRepository {

    //startTransaction
    public int startTransaction(){
        return 1; //return transaction id
    }

    public Transaction[] pendingTransactionByUser(int userId){
        return new Transaction[]{};
    }

    public Transaction[] completedTransactionByUser(int userId){
        return new Transaction[]{};
    }

    public void confirmTransaction(int transactionId){
    }
}
