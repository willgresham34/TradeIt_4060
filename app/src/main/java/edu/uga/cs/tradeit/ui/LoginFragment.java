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

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.repository.AuthRepository;

public class LoginFragment extends Fragment {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, goToSignUpButton, forgotPasswordButton;
    private AuthRepository authRepository;

    public LoginFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        emailEditText = view.findViewById(R.id.etEmail);
        passwordEditText = view.findViewById(R.id.etPassword);
        loginButton = view.findViewById(R.id.btnLogin);
        goToSignUpButton = view.findViewById(R.id.btnGoToSignUp);
        forgotPasswordButton = view.findViewById(R.id.btnForgotPassword);

        authRepository = new AuthRepository();

        loginButton.setOnClickListener(v -> loginUser());
        goToSignUpButton.setOnClickListener(v -> goToSignUp());
        forgotPasswordButton.setOnClickListener(v -> goToForgotPassword());

        return view;
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid email format");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password required");
            return;
        }

        authRepository.login(email, password, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(),
                                "Logged in as: " + email,
                                Toast.LENGTH_SHORT).show();
                        goToHome();

                    } else {
                        Exception e = task.getException();

                        if (e instanceof FirebaseAuthInvalidUserException) {
                            emailEditText.setError("No account found with this email");
                            Toast.makeText(requireContext(),
                                    "Unknown email address.",
                                    Toast.LENGTH_LONG).show();
                        }
                        else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            passwordEditText.setError("Incorrect password");
                            Toast.makeText(requireContext(),
                                    "Incorrect email/password combination.",
                                    Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(requireContext(),
                                    "Login failed: " + (e != null ? e.getMessage() : "Unknown error"),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void goToSignUp() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, new SignUpFragment())
                .addToBackStack(null)
                .commit();
    }

    private void goToHome() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, new HomeFragment())
                .addToBackStack(null)
                .commit();
    }
    private void goToForgotPassword() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, new ForgotPasswordFragment())
                .addToBackStack(null)
                .commit();
    }

}
