package edu.uga.cs.tradeit.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.repository.AuthRepository;

public class ForgotPasswordFragment extends Fragment {

    private EditText emailEditText;
    private Button sendResetButton, backToLoginButton;
    private AuthRepository authRepository;

    public ForgotPasswordFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        emailEditText = view.findViewById(R.id.etResetEmail);
        sendResetButton = view.findViewById(R.id.btnSendResetEmail);
        backToLoginButton = view.findViewById(R.id.btnBackToLogin);

        authRepository = new AuthRepository();

        sendResetButton.setOnClickListener(v -> sendPasswordReset());
        backToLoginButton.setOnClickListener(v -> goToLogin());

        return view;
    }


    private void sendPasswordReset() {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_email_required));
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.error_email_invalid));
            return;
        }

        sendResetButton.setEnabled(false);

        authRepository.sendPasswordResetEmail(email, task -> {
            sendResetButton.setEnabled(true);

            if (task.isSuccessful()) {
                Toast.makeText(
                        requireContext(),
                        getString(R.string.reset_email_sent, email),
                        Toast.LENGTH_LONG
                ).show();
                goToLogin();
            } else {
                String message = task.getException() != null
                        ? task.getException().getMessage()
                        : "Unknown error";
                Toast.makeText(
                        requireContext(),
                        getString(R.string.reset_email_failed, message),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }


    private void goToLogin() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, new LoginFragment())
                .addToBackStack(null)
                .commit();
    }
}
