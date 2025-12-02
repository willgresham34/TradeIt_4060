package edu.uga.cs.tradeit.repository;

import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    private final DatabaseReference usersRef;

    public UserRepository() {
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public interface UserCallback {
        void onUserLoaded(@Nullable String email);
    }

    public void getUserEmailById(String userId, UserCallback callback) {
        if (userId == null) {
            callback.onUserLoaded(null);
            return;
        }

        usersRef.child(userId).child("email")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || !task.getResult().exists()) {
                        callback.onUserLoaded(null);
                    } else {
                        callback.onUserLoaded(task.getResult().getValue(String.class));
                    }
                });
    }

    public void saveUserOnSignUp(String uid, String email) {
        if (uid == null || email == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("email", email);

        usersRef.child(uid).setValue(data);
    }

    public void ensureUserRecordExists(String uid, String email) {
        usersRef.child(uid).child("email").get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) return;

                    if (!task.getResult().exists()) {
                        usersRef.child(uid).child("email").setValue(email);
                    }
                });
    }

}

