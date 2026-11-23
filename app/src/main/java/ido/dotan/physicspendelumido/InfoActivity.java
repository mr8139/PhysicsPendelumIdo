package ido.dotan.physicspendelumido;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        // as soon as the application opens the first fragment should
        // be displayed to the user
        getSupportFragmentManager().beginTransaction().replace(R.id.info_fragment_container, new GuideAppFragment()).commit();
    }

    private final BottomNavigationView.OnItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();
        if (itemId == R.id.nav_guide) {
            selectedFragment = new GuideAppFragment();
        } else if (itemId == R.id.nav_ido_dotan) {
            selectedFragment = new IdoDotanFragment();
        } else if (itemId == R.id.nav_sites) {
            selectedFragment = new SitesFragment();
        } else if (itemId == R.id.nav_videos) {
            selectedFragment = new VideosFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.info_fragment_container, selectedFragment).commit();
        }
        return true;
    };
}