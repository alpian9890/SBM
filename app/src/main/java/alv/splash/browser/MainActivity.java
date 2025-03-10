package alv.splash.browser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.Manifest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import alv.splash.browser.SlidingUpPanelLayout;

public class MainActivity extends AppCompatActivity {



    private static final int FILE_CHOOSER_REQUEST_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int REQUEST_CODE_102 = 102;
    ValueCallback<Uri[]> filePathCallback;
    private boolean storagePermission = false;
    private SharedPreferences preferences;
    private static final String APP_PREFERENCES = "AppPreferences";
    private static final String STORAGE_PERMISSION_KEY = "storage_permission_granted";

    // Pagination variables
    private int currentPage = 1;
    private int itemsPerPage = 50;
    private int totalPages = 1;
    private int totalItems = 0;
    private RecyclerView captchaRecyclerView;
    private TextView emptyStateText;
    private TextView textPageIndicator;
    private TextView textTotalItems, tvShowingItems;
    private CaptchaAdapter captchaAdapter;
    private SwipeRefreshLayout swipeRefreshLayoutCDB;
    private Button buttonPrevPage, buttonNextPage;
    private Button buttonShowDatabase, buttonShowCaptcha;
    CaptchaDataManager captchaDataManager;
    ExternalDatabaseManager externalDatabaseManager;
    private boolean isUsingExternalDatabase = false;
    private String basePath = ""; //Environment.getExternalStorageDirectory() + "/Datasets";
    private String imagesPath = "";// basePath + "/Images";


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

        // Inisialisasi SharedPreferences
        preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);

        // Ambil nilai permission yang tersimpan
        storagePermission = preferences.getBoolean(STORAGE_PERMISSION_KEY, false);

        setupStorage();
        //nav_view
        navigationView = findViewById(R.id.nav_view);
        mainDrawer = findViewById(R.id.mainDrawer);

        // Toolbar
        myMenu = findViewById(R.id.myMenu);
        myHome = findViewById(R.id.myHome);

        initializeCaptchaViewer();

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

    private void setupStorage() {
        // Log untuk debugging
        Log.d("StorageDebug", "setupStorage() dipanggil");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ - Gunakan direktori yang dikelola aplikasi
            File externalFilesDir = getExternalFilesDir(null);
            if (externalFilesDir != null) {
                basePath = externalFilesDir.getAbsolutePath() + "/Datasets";
                Log.d("StorageDebug", "Path untuk Android 10+: " + basePath);
            } else {
                Log.e("StorageDebug", "getExternalFilesDir() mengembalikan null");
                //updateConsoleLog("[!] Error: Tidak dapat mendapatkan direktori eksternal");
            }
        } else {
            // Android 9 dan di bawahnya
            basePath = Environment.getExternalStorageDirectory() + "/Datasets";
            Log.d("StorageDebug", "Path untuk Android < 10: " + basePath);
        }

        imagesPath = basePath + "/Images";
        //csvPath = basePath + "/labels.csv";

        // Log info path
        Log.d("StorageDebug", "imagesPath: " + imagesPath);
        //Log.d("StorageDebug", "csvPath: " + csvPath);

        // Buat direktori jika belum ada
        createDirectories();

        // Minta izin penyimpanan saat aplikasi dibuka
        requestStoragePermissions();
    }

    private void requestStoragePermissions() {
        Log.d("StorageDebug", "requestStoragePermissions(), SDK: " + Build.VERSION.SDK_INT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            boolean hasAllFilesPermission = Environment.isExternalStorageManager();
            Log.d("StorageDebug", "Android 11+, hasAllFilesPermission: " + hasAllFilesPermission);

            if (!hasAllFilesPermission) {
                //updateConsoleLog("[ Meminta izin MANAGE_EXTERNAL_STORAGE untuk Android 11+ ]");
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, REQUEST_CODE_102);
                } catch (Exception e) {
                    Log.e("StorageDebug", "Error meminta MANAGE_EXTERNAL_STORAGE: " + e.getMessage(), e);
                    //updateConsoleLog("[!] Error meminta izin penyimpanan: " + e.getMessage());

                    // Fallback jika cara spesifik gagal
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, REQUEST_CODE_102);
                }
            } else {
                Log.d("StorageDebug", "MANAGE_EXTERNAL_STORAGE sudah diberikan");
                //updateConsoleLog("[ Izin MANAGE_EXTERNAL_STORAGE sudah diberikan ]");
                storagePermission = true;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(STORAGE_PERMISSION_KEY, true);
                editor.apply();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6+ (API 23+)
            int writePermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            Log.d("StorageDebug", "Android 6-10, writePermission status: " +
                    (writePermission == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));

            if (writePermission != PackageManager.PERMISSION_GRANTED) {
                //updateConsoleLog("[ Meminta izin WRITE_EXTERNAL_STORAGE ]");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            } else {
                Log.d("StorageDebug", "WRITE_EXTERNAL_STORAGE sudah diberikan");
                //updateConsoleLog("[ Izin WRITE_EXTERNAL_STORAGE sudah diberikan ]");
                // Izin sudah diberikan, update jika belum tersimpan
                if (!storagePermission) {
                    storagePermission = true;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(STORAGE_PERMISSION_KEY, true);
                    editor.apply();
                }
            }
        } else {
            // Android 5.1 dan di bawahnya
            Log.d("StorageDebug", "SDK < 23, izin dianggap sudah diberikan");
           // updateConsoleLog("[ Izin penyimpanan otomatis diberikan untuk Android 5.1- ]");
            storagePermission = true;
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            try {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!storagePermission) {
                        // Update nilai di variabel
                        storagePermission = true;

                        // Simpan nilai ke SharedPreferences
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(STORAGE_PERMISSION_KEY, true);
                        editor.apply();

                        //updateConsoleLog("[ Izin penyimpanan diberikan ]");
                        Toast.makeText(this, "Izin penyimpanan diberikan", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (storagePermission) {
                        // Update nilai di variabel
                        storagePermission = false;

                        // Simpan nilai ke SharedPreferences
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(STORAGE_PERMISSION_KEY, false);
                        editor.apply();

                        Toast.makeText(this, "Aplikasi membutuhkan izin penyimpanan", Toast.LENGTH_LONG).show();
                        //updateConsoleLog("[ Aplikasi membutuhkan izin penyimpanan ]");
                    }
                }
            } catch (Exception e) {
                Log.e("StorageDebug", "Error: " + e.getMessage(), e);
                //updateConsoleLog("[!] Error: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (filePathCallback != null) {
                Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                filePathCallback.onReceiveValue(result);
                filePathCallback = null;
            }
        }

        if (requestCode == REQUEST_CODE_102) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                boolean hasAllFilesPermission = Environment.isExternalStorageManager();
                Log.d("StorageDebug", "onActivityResult, hasAllFilesPermission: " + hasAllFilesPermission);

                if (hasAllFilesPermission) {
                    //updateConsoleLog("[ Izin MANAGE_EXTERNAL_STORAGE diberikan ]");
                    storagePermission = true;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(STORAGE_PERMISSION_KEY, true);
                    editor.apply();

                    // Buat direktori setelah izin diberikan
                    createDirectories();
                } else {
                    //updateConsoleLog("[!] Izin MANAGE_EXTERNAL_STORAGE ditolak");
                    storagePermission = false;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(STORAGE_PERMISSION_KEY, false);
                    editor.apply();
                }
            }
        }

    }

    private void createDirectories() {
        /*
        try {
            File baseDir = new File(basePath);
            if (!baseDir.exists()) {
                boolean baseDirCreated = baseDir.mkdirs();
                Log.d("StorageDebug", "Membuat basePath: " + basePath + " - Hasil: " + baseDirCreated);
                //updateConsoleLog("[ Mencoba membuat direktori base: " + (baseDirCreated ? "berhasil" : "gagal") + " ]");
            }

            File imagesDir = new File(imagesPath);
            if (!imagesDir.exists()) {
                boolean imagesDirCreated = imagesDir.mkdirs();
                Log.d("StorageDebug", "Membuat imagesPath: " + imagesPath + " - Hasil: " + imagesDirCreated);
                //updateConsoleLog("[ Mencoba membuat direktori images: " + (imagesDirCreated ? "berhasil" : "gagal") + " ]");
            }
        } catch (Exception e) {
            Log.e("StorageDebug", "Error membuat direktori: " + e.getMessage(), e);
            //updateConsoleLog("[!] Error membuat direktori: " + e.getMessage());
        }
        */
    }

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

    private void initializeCaptchaViewer() {
        captchaDataManager = new CaptchaDataManager(this);

        // Find views
        captchaRecyclerView = findViewById(R.id.captchaRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        swipeRefreshLayoutCDB = findViewById(R.id.swipeRefreshLayoutCDB);
        textPageIndicator = findViewById(R.id.textPageIndicator);
        textTotalItems = findViewById(R.id.textTotalItems);
        tvShowingItems = findViewById(R.id.tvShowingItems);
        buttonPrevPage = findViewById(R.id.buttonPrevPage);
        buttonNextPage = findViewById(R.id.buttonNextPage);
        buttonShowDatabase = findViewById(R.id.buttonShowDatabase);
        buttonShowCaptcha = findViewById(R.id.buttonShowCaptcha);

        if (captchaRecyclerView != null) {
            captchaRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

            // Initialize adapter with empty list
            captchaAdapter = new CaptchaAdapter(this, new ArrayList<>());
            captchaRecyclerView.setAdapter(captchaAdapter);

            // Set up pagination controls
            setupPaginationControls();

            // Set up swipe refresh
            setupSwipeRefresh();

            // Set up navigation buttons
            setupNavigationButtons();

            // Load data
            loadData();
        }
    }

    private void setupPaginationControls() {
        buttonPrevPage.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                loadData();
            }
        });

        buttonNextPage.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadData();
            }
        });

        updatePaginationControls();
    }

    private void updatePaginationControls() {
        buttonPrevPage.setEnabled(currentPage > 1);
        buttonNextPage.setEnabled(currentPage < totalPages);
        textPageIndicator.setText("Page " + currentPage + " of " + totalPages);
        textTotalItems.setText("Total entries: " + totalItems);

        // Hitung rentang item yang ditampilkan
        int startItem = (currentPage - 1) * itemsPerPage + 1;
        int endItem = Math.min(currentPage * itemsPerPage, totalItems);

        // Menangani kasus ketika database kosong
        if (totalItems == 0) {
            startItem = 0;
            endItem = 0;
        }

        String tvShowingItemsText = " | Showing " + startItem + "-" + endItem + " of " + totalItems;
        tvShowingItems.setText(tvShowingItemsText);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayoutCDB.setOnRefreshListener(() -> {
            Toast.makeText(MainActivity.this, "Refreshing data...", Toast.LENGTH_SHORT).show();
            loadData();
        });

        // Set refresh indicator colors
        swipeRefreshLayoutCDB.setColorSchemeResources(
                R.color.purple_500,
                R.color.teal_200,
                R.color.purple_700
        );
    }

    private void setupNavigationButtons() {
        buttonShowDatabase.setOnClickListener(v -> showDatabaseManager());
        buttonShowCaptcha.setOnClickListener(v -> showCaptchaViewer());
    }

    public void loadData() {
        // Show refresh indicator
        if (swipeRefreshLayoutCDB != null) {
            swipeRefreshLayoutCDB.setRefreshing(true);
        }

        // Load data in background
        new Thread(() -> {
            // First get total count for pagination
            int count;
            if (isUsingExternalDatabase && externalDatabaseManager != null) {
                count = externalDatabaseManager.getEntryCount();
            } else {
                count = captchaDataManager.getEntryCount();
            }

            totalItems = count;
            totalPages = (count == 0) ? 1 : (int) Math.ceil((double) count / itemsPerPage);

            // Ensure current page is within bounds
            if (currentPage > totalPages) {
                currentPage = totalPages > 0 ? totalPages : 1;
            }

            // Calculate offset
            int offset = (currentPage - 1) * itemsPerPage;

            // Get paginated entries
            List<CaptchaDataManager.CaptchaEntry> entries;
            if (isUsingExternalDatabase && externalDatabaseManager != null) {
                entries = externalDatabaseManager.getEntriesWithPagination(offset, itemsPerPage);
            } else {
                entries = captchaDataManager.getEntriesWithPagination(offset, itemsPerPage);
            }

            runOnUiThread(() -> {
                if (captchaAdapter == null) {
                    captchaAdapter = new CaptchaAdapter(this, entries);
                    captchaRecyclerView.setAdapter(captchaAdapter);
                } else {
                    captchaAdapter.updateData(entries);
                }

                // Show empty state if needed
                if (emptyStateText != null) {
                    if (totalItems == 0) {
                        emptyStateText.setVisibility(View.VISIBLE);
                        captchaRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyStateText.setVisibility(View.GONE);
                        captchaRecyclerView.setVisibility(View.VISIBLE);
                    }
                }

                // Update pagination controls
                updatePaginationControls();

                // Hide refresh indicator
                if (swipeRefreshLayoutCDB != null) {
                    swipeRefreshLayoutCDB.setRefreshing(false);
                }
            });
        }).start();
    }

    public CaptchaDataManager getCaptchaDataManager() {
        return isUsingExternalDatabase ? null : captchaDataManager;
    }

    public ExternalDatabaseManager getExternalDatabaseManager() {
        return isUsingExternalDatabase ? externalDatabaseManager : null;
    }

    // Handle external database selection
    public void onExternalDatabaseSelected(String dbPath) {
        // Close current connections
        closeCurrentDatabaseConnections();

        try {
            externalDatabaseManager = new ExternalDatabaseManager(this, dbPath);
            isUsingExternalDatabase = true;

            // Update title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("CAPTCHA Viewer (External DB)");
            }

            // Reload data
            loadData();

        } catch (Exception e) {
            Toast.makeText(this, "Failed to open external database: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();

            // Fallback to internal
            useInternalDatabase();
        }
    }

    // Switch back to internal database
    public void useInternalDatabase() {
        if (isUsingExternalDatabase) {
            closeCurrentDatabaseConnections();

            isUsingExternalDatabase = false;

            // Update title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("CAPTCHA Database Viewer");
            }

            // Reload data
            loadData();

            Toast.makeText(this, "Using internal database", Toast.LENGTH_SHORT).show();
        }
    }

    private void closeCurrentDatabaseConnections() {
        if (externalDatabaseManager != null) {
            externalDatabaseManager.close();
            externalDatabaseManager = null;
        }
    }


    // Menu handling
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_view_captchas) {
            showCaptchaViewer();
            return true;
        } else if (id == R.id.action_database_manager) {
            showDatabaseManager();
            return true;
        } else if (id == R.id.action_refresh) {
            loadData();
            Toast.makeText(this, "Refreshing data...", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showCaptchaViewer() {
        getSupportFragmentManager().popBackStack();
    }

    private void showDatabaseManager() {
        Fragment dbManagerFragment = new DatabaseManagerFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, dbManagerFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (captchaDataManager != null) {
            captchaDataManager.close();
        }
        if (externalDatabaseManager != null) {
            externalDatabaseManager.close();
        }
    }

}// akhir class