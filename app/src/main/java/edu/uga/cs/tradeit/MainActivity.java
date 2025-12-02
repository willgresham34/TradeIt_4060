package edu.uga.cs.tradeit;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import edu.uga.cs.tradeit.ui.ChangePasswordFragment;
import edu.uga.cs.tradeit.ui.HomeFragment;
import edu.uga.cs.tradeit.ui.MyListingsFragment;
import edu.uga.cs.tradeit.ui.MyTransactionsFragment;
import edu.uga.cs.tradeit.ui.SplashFragment;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setClickable(true);
        toolbar.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, new HomeFragment())
                    .addToBackStack(null)
                    .commit();
        });
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, new SplashFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_my_transactions) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, new MyTransactionsFragment())
                    .addToBackStack(null)
                    .commit();
            return true;

        } else if (id == R.id.action_my_listings) {
             getSupportFragmentManager()
                     .beginTransaction()
                     .replace(R.id.main, new MyListingsFragment())
                     .addToBackStack(null)
                     .commit();
            return true;

        } else if (id == R.id.action_change_password) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, new ChangePasswordFragment())
                    .addToBackStack(null)
                    .commit();
            return true;

        } else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, new SplashFragment())
                    .commit();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}