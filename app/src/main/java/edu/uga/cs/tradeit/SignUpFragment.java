package edu.uga.cs.tradeit;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SignUpFragment extends Fragment {

    private EditText emailEditText, passwordEditText;
    private Button signUpButton, goToLoginButton;
    private FirebaseAuth mAuth;

    public SignUpFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        emailEditText = view.findViewById(R.id.etEmail);
        passwordEditText = view.findViewById(R.id.etPassword);
        signUpButton = view.findViewById(R.id.btnSignUp);
        goToLoginButton = view.findViewById(R.id.btnGoToLogin);

        mAuth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(v -> registerUser());
        goToLoginButton.setOnClickListener(v -> goToLogin());

        return view;
    }

    private void registerUser() {
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


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(),
                                "Registered user: " + email,
                                Toast.LENGTH_SHORT).show();

                        goToLogin();
                    } else {
                        Exception e = task.getException();

                        if (e instanceof FirebaseAuthUserCollisionException) {
                            // DUPLICATE EMAIL CHECK
                            emailEditText.setError("Email already exists");
                            Toast.makeText(requireContext(),
                                    "This email is already registered.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Registration failed: " + (e != null ? e.getMessage() : "Unknown error"),
                                    Toast.LENGTH_LONG).show();
                        }
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
