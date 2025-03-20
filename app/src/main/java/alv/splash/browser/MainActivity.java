package alv.splash.browser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.Manifest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {



    private static final int FILE_CHOOSER_REQUEST_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int REQUEST_CODE_102 = 102;
    ValueCallback<Uri[]> filePathCallback;
    private boolean storagePermission = false;
    private SharedPreferences preferences;
    private static final String APP_PREFERENCES = "AppPreferences";
    private static final String STORAGE_PERMISSION_KEY = "storage_permission_granted";

    CaptchaDataManager captchaDataManager;
    private String basePath = ""; //Environment.getExternalStorageDirectory() + "/Datasets";
    private String imagesPath = "";// basePath + "/Images";

    DrawerLayout mainDrawer;
    FrameLayout fMainContainer;

    NavigationView navigationView;

    SlidingUpPanelLayout slidingLayout;
    private AddressBarUtils addressBarUtils;


    private TextInputEditText editAddressBar;

    private TabManager tabManager;
    private Map<String, Fragment> fragmentCache = new HashMap<>();

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
        //myMenu = findViewById(R.id.myMenu);
        //myHome = findViewById(R.id.myHome);

        slidingLayout = findViewById(R.id.sliding_layout);
        fMainContainer = findViewById(R.id.fMainContainer);

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

       // replaceFragment(new HomeFragment());

        initializeAddressBar();
        initializeSubToolbar();

        tabManager = TabManager.getInstance();
        Log.d("MainActivity", "TabManager initialized");
        tabManager.addTabChangeListener(new TabManager.TabChangeListener() {
            @Override
            public void onTabsChanged() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // Handle tab removal for Android 9 (API level 28) and above
                    List<TabItem> currentTabs = tabManager.getAllTabs();
                    Set<String> currentTabIds = currentTabs.stream().map(TabItem::getId).collect(Collectors.toSet());
                    // Remove fragments for tabs that no longer exist
                    new ArrayList<>(fragmentCache.keySet()).forEach(tabId -> {
                        if (!currentTabIds.contains(tabId)) {
                            onTabClosed(tabId);
                        }
                    });
                } else {
                    List<TabItem> currentTabs = tabManager.getAllTabs();
                            Set<String> currentTabIds = new HashSet<>();

                    // Mengganti stream().map().collect(Collectors.toSet())
                    for (TabItem tab : currentTabs) {
                        currentTabIds.add(tab.getId());
                    }

                    // Mengganti forEach()
                    List<String> tabsToRemove = new ArrayList<>();
                    for (String tabId : fragmentCache.keySet()) {
                        if (!currentTabIds.contains(tabId)) {
                            tabsToRemove.add(tabId);
                        }
                    }

                    for (String tabId : tabsToRemove) {
                        onTabClosed(tabId);
                    }
                }
            }
            @Override
            public void onActiveTabChanged(TabItem tab) {
                if (tab != null) {
                    showTab(tab);
                }
                Log.d("MainActivity", "Active tab changed to: " + (tab != null ? tab.getId() : "null"));
            }

        });
		
		if (savedInstanceState == null) {
            /*getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fMainContainer,
                            new HomeFragment())
                    .commit();*/

			// If no tabs exist, create a home tab
			if (tabManager != null && tabManager.getAllTabs().isEmpty()) {
                createNewTab("about:home");
                showTab(tabManager.getActiveTab());
                Log.d("MainActivity", "No tabs exist, creating home tab");
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentSubContent,
                            new TabsManagementFragment())
                    .commit();
        }


        /*
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
                    openWebGecko(url);
                }
                else if (id == R.id.suno_AI) {
                    String url = "https://suno.ai";
                    openWebGecko(url);
                }
                else if (id == R.id.vidiodotcom) {
                    String url = "https://www.vidio.com";
                    openWebGecko(url);
                }
                else if (id == R.id.webLK21) {
                    String url = "https://lk21.com";
                    openWebGecko(url);
                }
                else if (id == R.id.open_AI) {
                    String url = "https://chat.openai.com";
                    openWebGecko(url);
                }
                else if (id == R.id.googleColab) {
                    String url = "https://colab.google.com";
                    openWebGecko(url);
                }
                else if (id == R.id.workercashweb) {
                    String url = "https://worker.cash";
                    openWebGecko(url);
                }
                else if (id == R.id.workDuaTab) {
                    // Handle 2 tab work
                   // startActivity(new Intent(this, DuaTabActivity.class));
                }
                // Survey Panel items
                else if (id == R.id.surveytime) {
                    String url = "https://surveytime.com";
                    openWebGecko(url);
                }
                else if (id == R.id.ysense) {
                    String url = "https://ysense.com";
                    openWebGecko(url);
                }
                else if (id == R.id.grabpoints) {
                    String url = "https://grabpoints.com";
                    openWebGecko(url);
                }
                else if (id == R.id.grapedata) {
                    String url = "https://grapedata.com";
                    openWebGecko(url);
                }
                else if (id == R.id.primeopinion) {
                    String url = "https://primeopinion.com";
                    openWebGecko(url);
                }
                else if (id == R.id.opinionworld) {
                    String url = "https://opinionworld.com";
                    openWebGecko(url);
                }
                else if (id == R.id.lootupsurvey) {
                    String url = "https://lootup.com";
                    openWebGecko(url);
                }
                else if (id == R.id.surveylama) {
                    String url = "https://surveylama.com";
                    openWebGecko(url);
                }
                else if (id == R.id.surveyon) {
                    String url = "https://surveyon.com";
                    openWebGecko(url);
                }
                else if (id == R.id.lifepoints) {
                    String url = "https://lifepoints.com";
                    openWebGecko(url);
                }
                else if (id == R.id.toluna) {
                    String url = "https://toluna.com";
                    openWebGecko(url);
                }
                else if (id == R.id.drawer_logout) {
                    // Handle logout
                    logout();
                }

                mainDrawer.closeDrawer(GravityCompat.START);
                return true;
            }



            // Method untuk logout
            private void logout() {
                // Implement logout logic here
                finish();
            }

        });*/




    }//akhir onCreate

    @Override
    public void onBackPressed() {
        // Check if address bar is expanded
        if (addressBarUtils.isExpanded()) {
            addressBarUtils.collapseAddressBar();
            return;
        }

        // Check if sliding panel is expanded
        if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }

        // Check if drawer is open
        if (mainDrawer.isDrawerOpen(GravityCompat.START)) {
            mainDrawer.closeDrawer(GravityCompat.START);
            return;
        }

        // Check if current fragment is GeckoViewFragment and can go back
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fMainContainer);

        if (currentFragment instanceof GeckoViewFragment && ((GeckoViewFragment) currentFragment).canGoBack()) {
            ((GeckoViewFragment) currentFragment).goBack();
            return;
        }

        // Otherwise, proceed with normal back button behavior
        super.onBackPressed();
    }

    public void updateAddressBarInfo(String title, boolean isSecure) {
        addressBarUtils.updatePageInfo(title, isSecure);
    }
    public void updateEditText(String query) {
        editAddressBar.setText(query);
    }
    private void openWebGecko(String query) {
        String processedUrl = new UrlValidator().processInput(query);
        loadUrl(processedUrl);
        addressBarUtils.collapseAddressBar();
        slidePanelCollapse();
    }

    private void initializeAddressBar() {
        // Initialize components

        ImageButton btnMenu = findViewById(R.id.btnMenu);

        ImageButton btnHome = findViewById(R.id.btnHome);

        ImageButton btnAddTab = findViewById(R.id.btnAddTab);

        ImageButton btnTabs = findViewById(R.id.btnTabs);



        ImageView iconSecurity = findViewById(R.id.iconSecurity);

        TextView txtPageTitle = findViewById(R.id.txtPageTitle);

        CardView addressBarContainer = findViewById(R.id.addressBarContainer);

        editAddressBar = findViewById(R.id.editAddressBar);



        // Initialize AddressBarUtils

        addressBarUtils = new AddressBarUtils(addressBarContainer, iconSecurity, txtPageTitle);



        // Set initial page info

        addressBarUtils.updatePageInfo("Start browsing", true);

        // Setup toggle behavior

        txtPageTitle.setOnClickListener(v -> {
            addressBarUtils.expandAddressBar();
            editAddressBar.requestFocus();
            editAddressBar.selectAll();
            showKeyboard(editAddressBar);
        });



        // Handle address bar keyboard actions

        editAddressBar.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_GO) {

                openWebGecko(editAddressBar.getText().toString());
                hideKeyboard(editAddressBar);
                slidePanelCollapse();
                Log.d("MainActivity", "EditText, IME_ACTION_GO clicked");
                return true;

            }

            return false;

        });

        editAddressBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(editAddressBar);
                addressBarUtils.collapseAddressBar();
            }
        });



        // Setup other buttons

        btnMenu.setOnClickListener(v -> {
            if (!mainDrawer.isDrawerOpen(GravityCompat.START)) {
                mainDrawer.openDrawer(GravityCompat.START); // Buka Navigation Drawer di sisi kiri
            }
            addressBarUtils.collapseAddressBar();
            slidePanelCollapse();
            Log.d("MainActivity", "Menu button clicked");
        });



        btnHome.setOnClickListener(v -> {

            // Handle home click
            Log.d("MainActivity", "Home button clicked");
            addressBarUtils.collapseAddressBar();

        });



        btnAddTab.setOnClickListener(v -> {
            createNewTab("about:home");
            Log.d("MainActivity", "Add Tab button clicked");
        });

        btnTabs.setOnClickListener(v -> {
            loadFragment(new TabsManagementFragment());
            Log.d("MainActivity", "Tabs button clicked");
        });
    }

    private void initializeSubToolbar() {
        View headerView = navigationView.getHeaderView(0);
        // Find CardView elements and set click listeners
        CardView startWorking = headerView.findViewById(R.id.startWorking);
        CardView cardTabs = headerView.findViewById(R.id.card_tabs);
        CardView cardHistory = headerView.findViewById(R.id.card_history);
        CardView cardBookmarks = headerView.findViewById(R.id.card_bookmarks);
        CardView cardDownloads = headerView.findViewById(R.id.card_downloads);
        CardView cardExtensions = headerView.findViewById(R.id.card_extensions);
        CardView cardTranslate = headerView.findViewById(R.id.card_translate);
        CardView cardNotes = headerView.findViewById(R.id.card_notes);
        CardView cardDatabase = headerView.findViewById(R.id.card_database);
        CardView cardFiles = headerView.findViewById(R.id.card_files);
        CardView cardKeyboard = headerView.findViewById(R.id.card_keyboard);
        CardView cardCalculator = headerView.findViewById(R.id.card_calculator);
        CardView cardPhotos = headerView.findViewById(R.id.card_photos);
        CardView cardVideos = headerView.findViewById(R.id.card_videos);
        CardView cardMusic = headerView.findViewById(R.id.card_music);
        CardView cardV2ray = headerView.findViewById(R.id.card_v2ray);
        CardView cardConsole = headerView.findViewById(R.id.card_console);
        CardView cardLogout = headerView.findViewById(R.id.card_logout);

        // Set click listeners for each card
        startWorking.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StartWorking.class);
            startActivity(intent);
            finish();
        });
        cardTabs.setOnClickListener(v -> loadFragment(new TabsManagementFragment()));
        cardHistory.setOnClickListener(v -> loadFragment(new HistoryBrowsingFragment()));
        cardBookmarks.setOnClickListener(v -> loadFragment(new BookmarksFragment()));
        cardDownloads.setOnClickListener(v -> loadFragment(new DownloadsFragment()));
        cardExtensions.setOnClickListener(v -> loadFragment(new ExtensionsFragment()));
        cardTranslate.setOnClickListener(v -> loadFragment(new TranslateFragment()));
        cardNotes.setOnClickListener(v -> loadFragment(new NotesFragment()));
        cardDatabase.setOnClickListener(v -> loadFragment(new CaptchaViewerFragment()));
        cardFiles.setOnClickListener(v -> loadFragment(new FilesManagerFragment()));
        cardKeyboard.setOnClickListener(v -> loadFragment(new KeyboardFragment()));
        cardCalculator.setOnClickListener(v -> loadFragment(new CalculatorFragment()));
        cardPhotos.setOnClickListener(v -> loadFragment(new PhotosFragment()));
        cardVideos.setOnClickListener(v -> loadFragment(new VideosFragment()));
        cardMusic.setOnClickListener(v -> loadFragment(new MusicFragment()));
        cardV2ray.setOnClickListener(v -> loadFragment(new V2RayFragment()));
        cardConsole.setOnClickListener(v -> loadFragment(new ConsoleFragment()));
        cardLogout.setOnClickListener(v -> finish());

    }

    public void slidePanelCollapse() {
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentSubContent, fragment)
                .commit();
        closeDrawer();
        if (slidingLayout.getPanelState() != SlidingUpPanelLayout.PanelState.EXPANDED ||
                slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        }
    }

    private void closeDrawer() {
        if (mainDrawer.isDrawerOpen(GravityCompat.START)) {
            mainDrawer.closeDrawer(GravityCompat.START);
        }
    }


    public void showKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }
    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void createNewTab(String url) {
        TabItem newTab = tabManager.addTab(url);

        showTab(newTab);

        // Update tab counter if needed
        // You might want to add a tab counter in your UI

        Log.d("MainActivity", "New tab created with ID: " + newTab.getId());
    }

    public void showTab(TabItem tab) {
        tabManager.setActiveTab(tab);
        Fragment fragment = fragmentCache.get(tab.getId());
        if (fragment == null) {
            if (tab.getUrl().equals("about:home")) {
                fragment = new HomeFragment();
            } else {
                fragment = GeckoViewFragment.newInstance(tab.getId(), tab.getUrl());
            }
            fragmentCache.put(tab.getId(), fragment);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fMainContainer, fragment, tab.getId())
                    .hide(fragment)
                    .commit();
        }

        // Hide all other fragments and show the current one
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .hide(currentFragment)
                    .show(fragment)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .show(fragment)
                    .commit();
        }

        // Update UI
        if (tab.getUrl().equals("about:home")) {
            addressBarUtils.updatePageInfo("Start browsing", true);
        } else {
            addressBarUtils.updatePageInfo(tab.getTitle(), true);
        }
        slidePanelCollapse();
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fMainContainer);
    }

    public void onTabClosed(String tabId) {
        Fragment fragment = fragmentCache.remove(tabId);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    public void loadUrl(String url) {
        TabItem activeTab = tabManager.getActiveTab();

        if (activeTab == null) {
            // If no active tab, create a new one
            createNewTab(url);
            return;
        }

        // Get the current fragment
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fMainContainer);

        if (currentFragment instanceof GeckoViewFragment) {
            // If GeckoViewFragment is already showing, just load the URL
            ((GeckoViewFragment) currentFragment).loadUrl(url);
        } else {
            // Otherwise replace with a new GeckoViewFragment
            GeckoViewFragment geckoViewFragment = GeckoViewFragment.newInstance(activeTab.getId(), url);
            replaceFragment(geckoViewFragment);
        }
        slidePanelCollapse();
    }

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
        //createDirectories();

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
                    //createDirectories();
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


    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (captchaDataManager != null) {
            captchaDataManager.close();
        }
    }

}// akhir class