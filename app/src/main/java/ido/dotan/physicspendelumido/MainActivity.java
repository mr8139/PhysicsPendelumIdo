package ido.dotan.physicspendelumido;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements LogInFragment.OnLoginSuccessListener,
        RegistryFragment.OnRegistrationSuccessListener {

    private Button menuButton;
    private Button loginButton;
    private Button registryButton;
    private Button calculationsButton;
    private Button animationButton;
    private Button formulasButton;
    private Button graphicsButton;
    private Button historyButton;
    private Button videoButton;
    private Button sensorsButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuButton = findViewById(R.id.menu_button);
        loginButton = findViewById(R.id.login_button);
        registryButton = findViewById(R.id.registry_button);
        calculationsButton = findViewById(R.id.calculations_button);
        animationButton = findViewById(R.id.animation_button);
        formulasButton = findViewById(R.id.Formulas_button);
        graphicsButton = findViewById(R.id.graphics_button);
        historyButton = findViewById(R.id.history_button);
        videoButton = findViewById(R.id.video_button);
        sensorsButton = findViewById(R.id.sensors_button);


        menuButton.setOnClickListener(this::showPopupMenu);
        loginButton.setOnClickListener(v -> replaceFragment(new LogInFragment()));
        registryButton.setOnClickListener(v -> replaceFragment(new RegistryFragment()));
        calculationsButton.setOnClickListener(v -> replaceFragment(new CalculatorFragment()));
        formulasButton.setOnClickListener(v -> replaceFragment(new FormulasFragment()));
        graphicsButton.setOnClickListener(v -> replaceFragment(new GraphicsFragment()));
        animationButton.setOnClickListener(v -> replaceFragment(new AnimationFragment()));
        historyButton.setOnClickListener(v -> replaceFragment(new HistoryFragment()));
        videoButton.setOnClickListener(v -> replaceFragment(new VideosFragment()));
        sensorsButton.setOnClickListener(v -> replaceFragment(new SensorMeasurementFragment()));

        // Load LogInFragment by default
        if (savedInstanceState == null) {
            replaceFragment(new LogInFragment());
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_info) {
                startActivity(new Intent(this, InfoActivity.class));
                return true;
            } else if (itemId == R.id.action_back) {
                getSupportFragmentManager().popBackStack();
                return true;
            } else if (itemId == R.id.action_logout) {
                logout();
                return true;
            } else if (itemId == R.id.action_exit) {
                finish();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void logout() {
        // Clear shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("user_code");
        editor.apply();

        // Disable buttons
        disableProtectedButtons();

        // Switch to login fragment
        replaceFragment(new LogInFragment());

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null); // Add to back stack
        fragmentTransaction.commit();
    }

    private void enableAllButtons() {
        loginButton.setEnabled(true);
        registryButton.setEnabled(true);
        calculationsButton.setEnabled(true);
        animationButton.setEnabled(true);
        formulasButton.setEnabled(true);
        graphicsButton.setEnabled(true);
        historyButton.setEnabled(true);
        videoButton.setEnabled(true);
        sensorsButton.setEnabled(true);
    }

    private void disableProtectedButtons() {
        calculationsButton.setEnabled(false);
        animationButton.setEnabled(false);
        formulasButton.setEnabled(false);
        graphicsButton.setEnabled(false);
        historyButton.setEnabled(false);
        videoButton.setEnabled(false);
        sensorsButton.setEnabled(false);
    }

    @Override
    public void onLoginSuccess() {
        enableAllButtons();
    }

    @Override
    public void onRegistrationSuccess() {
        enableAllButtons();
    }
}
