package edu.uga.cs.tradeit.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.uga.cs.tradeit.R;

public class ChangePasswordFragment extends Fragment {

    private EditText currentPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmNewPasswordEditText;
    private Button changePasswordButton;
    private Button cancelButton;

    private FirebaseAuth auth;

    public ChangePasswordFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        currentPasswordEditText = view.findViewById(R.id.etCurrentPassword);
        newPasswordEditText = view.findViewById(R.id.etNewPassword);
        confirmNewPasswordEditText = view.findViewById(R.id.etConfirmNewPassword);
        changePasswordButton = view.findViewById(R.id.btnChangePassword);
        cancelButton = view.findViewById(R.id.btnCancelChangePassword);

        auth = FirebaseAuth.getInstance();

        changePasswordButton.setOnClickListener(v -> handleChangePassword());
        cancelButton.setOnClickListener(v -> goBack());

        return view;
    }

    private void handleChangePassword() {
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(currentPassword)) {
            currentPasswordEditText.setError(getString(R.string.error_password_required));
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            newPasswordEditText.setError(getString(R.string.error_password_required));
            return;
        }

        if (newPassword.length() < 6) {
            newPasswordEditText.setError(getString(R.string.error_password_too_short));
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            confirmNewPasswordEditText.setError(getString(R.string.error_passwords_do_not_match));
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(),
                    getString(R.string.error_not_logged_in),
                    Toast.LENGTH_LONG).show();
            return;
        }

        String email = user.getEmail();
        if (email == null) {
            Toast.makeText(requireContext(),
                    getString(R.string.error_not_logged_in),
                    Toast.LENGTH_LONG).show();
            return;
        }

        changePasswordButton.setEnabled(false);

        user.reauthenticate(EmailAuthProvider.getCredential(email, currentPassword))
                .addOnCompleteListener(reauthTask -> {
                    if (reauthTask.isSuccessful()) {
                        user.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    changePasswordButton.setEnabled(true);
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(requireContext(),
                                                getString(R.string.change_password_success),
                                                Toast.LENGTH_LONG).show();
                                        goBack();
                                    } else {
                                        String message = updateTask.getException() != null
                                                ? updateTask.getException().getMessage()
                                                : "Unknown error";
                                        Toast.makeText(
                                                requireContext(),
                                                getString(R.string.change_password_failed, message),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                });
                    } else {
                        changePasswordButton.setEnabled(true);
                        String message = reauthTask.getException() != null
                                ? reauthTask.getException().getMessage()
                                : "Unknown error";
                        Toast.makeText(
                                requireContext(),
                                getString(R.string.change_password_failed, message),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void goBack() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}
