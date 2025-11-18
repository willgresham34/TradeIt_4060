package edu.uga.cs.tradeit.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.uga.cs.tradeit.R;


public class SplashFragment extends Fragment {

    private Button btnLogin, btnSignup;

    public SplashFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);

        btnLogin = view.findViewById(R.id.btnLogin);
        btnSignup = view.findViewById(R.id.btnSignup);

        // Check auth status
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            goToHome();
        }

        btnLogin.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main, new LoginFragment())
                        .addToBackStack(null)
                        .commit());

        btnSignup.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main, new SignUpFragment())
                        .addToBackStack(null)
                        .commit());

        return view;
    }

    private void goToHome() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, new HomeFragment())
                .commit();
    }
}
