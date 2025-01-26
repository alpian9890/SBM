package alv.splash.browser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import alv.splash.browser.SlidingUpPanelLayout;

public class MainActivity extends AppCompatActivity {

    DrawerLayout mainDrawer;
    FrameLayout fMainContainer;


    //Toolbar
    LinearLayout navContainer;
    ImageView myMenu, myHome, addNewTab;

    NavigationView navigationView;

    SlidingUpPanelLayout slidingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //nav_view
        navigationView = findViewById(R.id.nav_view);
        mainDrawer = findViewById(R.id.mainDrawer);

        // Toolbar
        myMenu = findViewById(R.id.myMenu);
        myHome = findViewById(R.id.myHome);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fMainContainer, new HomeFragment()).commit();

        }
        replaceFragment(new HomeFragment());

        slidingLayout = findViewById(R.id.sliding_layout);

        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        slidingLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                // Anda bisa menambahkan logika tambahan di sini jika diperlukan
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });

        slidingLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });


        myMenu.setOnClickListener(v -> {
            if (!mainDrawer.isDrawerOpen(GravityCompat.START)) {
                mainDrawer.openDrawer(GravityCompat.START); // Buka Navigation Drawer di sisi kiri
            }
        });

        // Set listener untuk item menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.start_working) {
                    // Handle start working
                    Intent intent = new Intent(MainActivity.this, StartWorking.class);
                    startActivity(intent);
                    finish();
                }
                else if (id == R.id.music_player) {
                    // Handle music player
                    String url = "https://music.youtube.com";
                    openWebView(url);
                }
                else if (id == R.id.suno_AI) {
                    String url = "https://suno.ai";
                    openWebView(url);
                }
                else if (id == R.id.vidiodotcom) {
                    String url = "https://www.vidio.com";
                    openWebView(url);
                }
                else if (id == R.id.webLK21) {
                    String url = "https://lk21.com";
                    openWebView(url);
                }
                else if (id == R.id.open_AI) {
                    String url = "https://chat.openai.com";
                    openWebView(url);
                }
                else if (id == R.id.googleColab) {
                    String url = "https://colab.google.com";
                    openWebView(url);
                }
                else if (id == R.id.workercashweb) {
                    String url = "https://worker.cash";
                    openWebView(url);
                }
                else if (id == R.id.workDuaTab) {
                    // Handle 2 tab work
                   // startActivity(new Intent(this, DuaTabActivity.class));
                }
                // Survey Panel items
                else if (id == R.id.surveytime) {
                    String url = "https://surveytime.com";
                    openWebView(url);
                }
                else if (id == R.id.ysense) {
                    String url = "https://ysense.com";
                    openWebView(url);
                }
                else if (id == R.id.grabpoints) {
                    String url = "https://grabpoints.com";
                    openWebView(url);
                }
                else if (id == R.id.grapedata) {
                    String url = "https://grapedata.com";
                    openWebView(url);
                }
                else if (id == R.id.primeopinion) {
                    String url = "https://primeopinion.com";
                    openWebView(url);
                }
                else if (id == R.id.opinionworld) {
                    String url = "https://opinionworld.com";
                    openWebView(url);
                }
                else if (id == R.id.lootupsurvey) {
                    String url = "https://lootup.com";
                    openWebView(url);
                }
                else if (id == R.id.surveylama) {
                    String url = "https://surveylama.com";
                    openWebView(url);
                }
                else if (id == R.id.surveyon) {
                    String url = "https://surveyon.com";
                    openWebView(url);
                }
                else if (id == R.id.lifepoints) {
                    String url = "https://lifepoints.com";
                    openWebView(url);
                }
                else if (id == R.id.toluna) {
                    String url = "https://toluna.com";
                    openWebView(url);
                }
                else if (id == R.id.drawer_logout) {
                    // Handle logout
                    logout();
                }

                mainDrawer.closeDrawer(GravityCompat.START);
                return true;
            }

            // Method untuk membuka WebView
            private void openWebView(String url) {

            }

            // Method untuk logout
            private void logout() {
                // Implement logout logic here
                finish();
            }

        });







    }//akhir onCreate


    private  void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fMainContainer, fragment);
        fragmentTransaction.commit();
    }

    // Fungsi untuk menampilkan Toast
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}