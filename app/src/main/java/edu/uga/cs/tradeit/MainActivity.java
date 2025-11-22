package edu.uga.cs.tradeit;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import edu.uga.cs.tradeit.ui.SplashFragment;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
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

        if (id == R.id.action_logout) {
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