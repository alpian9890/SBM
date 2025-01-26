package alv.splash.browser;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;

import java.util.List;

public class StartWorking extends AppCompatActivity {

    String KBEarn = "https://kolotibablo.com";
    ValueCallback<Uri[]> filePathCallback;
    String getUrl1, getUrl2, pageTitle1, pageTitle2;
    private static final int FILE_CHOOSER_REQUEST_CODE = 100;
    FloatingActionButton fabControl;
    ImageView pageSecure1, pageSecure2, refreshTab1, refreshTab2;
    TextView titleTab1, titleTab2;
    ProgressBar pBarTab1, pBarTab2;
    TextInputLayout etLayout1, etLayout2;
    TextInputEditText etSearch1, etSearch2;
    WebView webViewTab1;
    private static GeckoRuntime sRuntime;
    GeckoView viewGecko;
    GeckoSession sessionGecko;
    boolean canGoBack = false;



    // Buat instance UrlValidator
    private UrlValidator urlValidator = new UrlValidator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Menghilangkan judul aplikasi
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Membuat layar menjadi fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start_working);

        webViewTab1 = findViewById(R.id.webViewTab1);
        new AdBlockerWebView.init(this).initializeWebView(webViewTab1);

        fabControl = findViewById(R.id.fab_control);
        // FAB click listener
        fabControl.setOnClickListener(v -> showBottomSheet());

        pBarTab1 = findViewById(R.id.pBarTab1);
        pBarTab2 = findViewById(R.id.pBarTab2);

        View bottomSheetView = LayoutInflater.from(this).inflate(
                R.layout.sw_bottomsheet_control, null);

        titleTab1 = bottomSheetView.findViewById(R.id.titleTab1);
        titleTab2 = bottomSheetView.findViewById(R.id.titleTab2);

        pageSecure1 = bottomSheetView.findViewById(R.id.pageSecure1);
        pageSecure2 = bottomSheetView.findViewById(R.id.pageSecure2);
        refreshTab1 = bottomSheetView.findViewById(R.id.refreshTab1);
        refreshTab2 = bottomSheetView.findViewById(R.id.refreshTab2);

        etLayout1 = bottomSheetView.findViewById(R.id.etLayout1);
        etLayout2 = bottomSheetView.findViewById(R.id.etLayout2);
        etSearch1 = bottomSheetView.findViewById(R.id.et_search1);
        etSearch2 = bottomSheetView.findViewById(R.id.et_search2);


        viewGecko = findViewById(R.id.webTab2);
        sessionGecko = new GeckoSession();

        // Workaround for Bug 1758212
        sessionGecko.setContentDelegate(new GeckoSession.ContentDelegate() {});
        setupGeckoSession();

        if (sRuntime == null) {
            // GeckoRuntime can only be initialized once per process
            sRuntime = GeckoRuntime.create(this);
        }

        sessionGecko.open(sRuntime);
        viewGecko.setSession(sessionGecko);


        setupWebViewSettings();
        setupWebViewClient();
        setupWebChromeClient();
        webViewTab1.loadUrl(KBEarn);

        sessionGecko.loadUri(KBEarn); // URL...

    }// akhir onCreate

    @Override
    public void onBackPressed() {
        // Cek apakah WebView terlihat (visible) dan mendapatkan fokus
        if (webViewTab1.isShown() && webViewTab1.hasFocus()) {
            if (webViewTab1.canGoBack()) {
                webViewTab1.goBack(); // Kembali ke URL sebelumnya jika ada riwayat navigasi
            } else {
                webViewTab1.clearCache(false);
                webViewTab1.clearFocus();
               // webViewTab1.setVisibility(View.GONE);
                //Menonaktifkan WebView
               // webViewCSP.setEnabled(false);
                super.onBackPressed();
                showExitConfirmationDialog();
            }
        } else {
            showExitConfirmationDialog();
        }

        if (viewGecko.isShown() && viewGecko.hasFocus()) {
            if (canGoBack == true) {
                sessionGecko.goBack();
            } else {
                viewGecko.clearFocus();
                super.onBackPressed();
                showExitConfirmationDialog();
            }
        } else {
            showExitConfirmationDialog();
        }
        super.onBackPressed();
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi Keluar");
        builder.setMessage("Apakah Anda yakin ingin keluar dari aplikasi?");
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish(); // Tutup aplikasi jika pengguna menekan "Ya"
            }
        });
        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Tutup dialog jika pengguna menekan "Tidak"
            }
        });
        builder.show();
    }

    private void setupGeckoSession () {

        sessionGecko.setProgressDelegate(new GeckoSession.ProgressDelegate() {

            @Override
            public void onProgressChange(GeckoSession session, int progress) {
                //textViewProgress.setText(progress + "%"); // Tampilkan progress di TextView
                pBarTab2.setProgress(progress);
                if (progress == 100) {
                    pBarTab2.setVisibility(View.GONE);
                } else {
                    pBarTab2.setVisibility(View.VISIBLE);
                }
            }
        });

        sessionGecko.setContentDelegate(new GeckoSession.ContentDelegate() {
            @Override
            public void onCloseRequest(GeckoSession session) {
                System.out.println("Permintaan untuk menutup sesi.");
            }

            @Override
            public void onTitleChange(GeckoSession session, String title) {
                pageTitle2 = title;
                titleTab2.setText(title); // Perbarui title di TextView
            }


        });

        sessionGecko.setNavigationDelegate(new GeckoSession.NavigationDelegate() {
           /* @Override
            public void onLoadRequest(GeckoSession session, GeckoSession.NavigationDelegate.LoadRequest request) {
               // System.out.println("Navigasi ke URL: " + request.uri);
            }*/

            @Override
            public void onCanGoBack(GeckoSession session, boolean GcanGoBack) {
                canGoBack = GcanGoBack;
            }

            @Override
            public void onLocationChange(GeckoSession session, String url, List<GeckoSession.PermissionDelegate.ContentPermission> perms, Boolean hasUserGesture) {
                getUrl2 = url;
                etSearch2.setText(url); // Perbarui URL di EditText
            }


        });
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

    }

    private void showBottomSheet() {
        // Inflate ulang layout setiap kali FAB ditekan
        View bottomSheetView = LayoutInflater.from(this).inflate(
                R.layout.sw_bottomsheet_control, null);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(bottomSheetView);

        LinearLayout rootBsSW = bottomSheetView.findViewById(R.id.rootBottomsheetSW);

        BottomSheetBehavior behaviorBsSW = BottomSheetBehavior.from(rootBsSW);

        behaviorBsSW.setState(BottomSheetBehavior.STATE_EXPANDED);

        titleTab1 = bottomSheetView.findViewById(R.id.titleTab1);
        titleTab2 = bottomSheetView.findViewById(R.id.titleTab2);

        pageSecure1 = bottomSheetView.findViewById(R.id.pageSecure1);
        pageSecure2 = bottomSheetView.findViewById(R.id.pageSecure2);
        refreshTab1 = bottomSheetView.findViewById(R.id.refreshTab1);
        refreshTab2 = bottomSheetView.findViewById(R.id.refreshTab2);

        etLayout1 = bottomSheetView.findViewById(R.id.etLayout1);
        etLayout2 = bottomSheetView.findViewById(R.id.etLayout2);
        etSearch1 = bottomSheetView.findViewById(R.id.et_search1);
        etSearch2 = bottomSheetView.findViewById(R.id.et_search2);

        titleTab1.setOnClickListener(v -> switchViewSearch1());
        titleTab2.setOnClickListener(v -> switchViewSearch2());

        // Event saat tombol enter ditekan
        etSearch1.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                String userInput = etSearch1.getText().toString();
                String processedUrl = urlValidator.processInput(userInput);

                // Memuat URL yang sudah diproses ke WebView
                webViewTab1.loadUrl(processedUrl);

                // Sembunyikan keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etSearch1.getWindowToken(), 0);
                return true;
            }
            return false;
        });
        // Event saat tombol enter ditekan
        etSearch2.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                String userInput = etSearch2.getText().toString();
                String processedUrl = urlValidator.processInput(userInput);

                // Memuat URL yang sudah diproses ke WebView
                sessionGecko.loadUri(processedUrl);

                // Sembunyikan keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etSearch2.getWindowToken(), 0);
                return true;
            }
            return false;
        });
        // Event saat kehilangan fokus
        etSearch1.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                etLayout1.setVisibility(View.GONE);

                pageSecure1.setVisibility(View.VISIBLE);
                titleTab1.setVisibility(View.VISIBLE);
                refreshTab1.setVisibility(View.VISIBLE);
            }
        });

        // Event saat kehilangan fokus
        etSearch2.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                etLayout2.setVisibility(View.GONE);

                pageSecure2.setVisibility(View.VISIBLE);
                titleTab2.setVisibility(View.VISIBLE);
                refreshTab2.setVisibility(View.VISIBLE);
            }
        });

        etSearch1.setText(getUrl1);
        etSearch2.setText(getUrl2);
        titleTab1.setText(pageTitle1);
        titleTab2.setText(pageTitle2);
        if (pageTitle1.length() > 6) {
            // Judul memiliki lebih dari 10 karakter, atur animasi marquee
            titleTab1.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            titleTab1.setMarqueeRepeatLimit(-1);
            titleTab1.setSelected(true);
        } else {
            // Judul memiliki 10 karakter atau kurang, nonaktifkan animasi marquee
            titleTab1.setEllipsize(TextUtils.TruncateAt.END);
            titleTab1.setSelected(false);
        }

        if (pageTitle2.length() > 6) {
            // Judul memiliki lebih dari 10 karakter, atur animasi marquee
            titleTab2.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            titleTab2.setMarqueeRepeatLimit(-1);
            titleTab2.setSelected(true);
        } else {
            // Judul memiliki 10 karakter atau kurang, nonaktifkan animasi marquee
            titleTab2.setEllipsize(TextUtils.TruncateAt.END);
            titleTab2.setSelected(false);
        }

        refreshTab1.setOnClickListener(v -> sessionGecko.reload());
        refreshTab2.setOnClickListener(v -> webViewTab1.reload());


        bottomSheetDialog.show();
    }


    private void switchViewSearch1(){
        pageSecure1.setVisibility(View.GONE);
        titleTab1.setVisibility(View.GONE);
        refreshTab1.setVisibility(View.GONE);

        etLayout1.setVisibility(View.VISIBLE);
        etSearch1.selectAll();
        etSearch1.requestFocus();
    }

    private void switchViewSearch2(){
        pageSecure2.setVisibility(View.GONE);
        titleTab2.setVisibility(View.GONE);
        refreshTab2.setVisibility(View.GONE);

        etLayout2.setVisibility(View.VISIBLE);
        etSearch2.selectAll();
        etSearch2.requestFocus();
    }

    /*
    private void performSearch1(String query) {
        if (!query.isEmpty()) {
            // Lakukan pencarian ke Google
            String searchGoogle = "https://www.google.com/search?q=" + Uri.encode(query);

            if (webViewTab1.getVisibility()==View.GONE){
                webViewTab1.setVisibility(View.VISIBLE);
            }
            if (isURL(query) && !isNotHTTP(query) || isDomain(query) && isNotHTTP(query)) {
                memuatLink(query, webViewTab1);
            } else {
                webViewTab1.loadUrl(searchGoogle);
            }

        }
    }

    private void performSearch2 (String query){
        if (!query.isEmpty()) {
            String searchGoogle = "https://www.google.com/search?q=" + Uri.encode(query);

            if (viewGecko.getVisibility()==View.GONE){
                viewGecko.setVisibility(View.VISIBLE);
            }

            if (isURL(query)){

            }
        }
    }

    private boolean isURL(String text) {
        return text.startsWith("https://");
    }

    private boolean isNotHTTP (String text) {
        return !text.startsWith("http://") || !text.startsWith("https://");
    }

    private boolean isDomain (String text) {
        return text.contains(".");
    }*/

    /*
    private void memuatLink(String link, final WebView targetWebView) {
        String addressURL = link;
        if (!link.startsWith("https://")) {
            addressURL = "https://" + link;
        } else if (link.startsWith("http://")) {
            addressURL = link;
        } else if (link.contains(".") && isNotHTTP(link)) {
            addressURL = "https://" + link;
        } else if (link.startsWith("https://"))


        setupWebViewSettings();
        setupWebViewClient();
        setupWebChromeClient();
        //setupOnScrollChange();
        //setupJavascriptInteeface();
        targetWebView.loadUrl(addressURL);
    }*/

    private void setupWebViewClient() {

        webViewTab1.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // Untuk development/testing, bisa menggunakan handler.proceed()
                // Untuk production, sebaiknya handle dengan proper certificate validation
                handler.proceed(); // Gunakan dengan hati-hati di production
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(final WebView view, WebResourceRequest request) {
                final String url = request.getUrl().toString();


                return AdBlockerWebView.blockAds(view,url) ? AdBlocker.createEmptyResource() :
                        super.shouldInterceptRequest(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Tampilkan ProgressBar saat halaman dimuat


            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);


            }



            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Mendapatkan URL baru dari permintaan navigasi
                String newUrl = request.getUrl().toString();
                getUrl1 = newUrl;

					/*if (pageTitle.equals("Pop")){
						if (getLink.equals(newUrl)){
							wcYtPlayBtn();
							//CallbackToast("newUrl");
						}
					}*/




                // Memperbarui TextView dengan URL baru
                etSearch1.setText(getUrl1);

                if (AdBlocker.isAd(request.getUrl().toString())) {
                    // Block the ad by returning true
                    return true;
                } else {
                    // Allow regular URLs to be loaded
                    return false;
                }
            }
        });

    }

    private void setupWebViewSettings() {
        WebSettings webSettings = webViewTab1.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        // Mengatur dukungan untuk zoom
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true); // Tampilkan kontrol zoom bawaan
        webSettings.setDisplayZoomControls(false); // Sembunyikan kontrol zoom built-in jika diperlukan

        // Mengatur pengaturan tampilan
        webSettings.setUseWideViewPort(true); // Mendukung tampilan halaman lebar
        webSettings.setLoadWithOverviewMode(true); // Muat halaman dengan mode tinjauan

        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);


        // Enable debugging untuk development
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webViewTab1.setWebContentsDebuggingEnabled(true);
        }

    }

    private void setupWebChromeClient() {
        webViewTab1.setWebChromeClient(new WebChromeClient() {
            private View mCustomView;
            private CustomViewCallback mCustomViewCallback;
            private View customView;
            private WebChromeClient.CustomViewCallback customViewCallback;
            private ViewGroup.LayoutParams originalLayoutParams;


            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (StartWorking.this.filePathCallback != null) {
                    StartWorking.this.filePathCallback.onReceiveValue(null);
                }
                StartWorking.this.filePathCallback = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*"); // Mengatur tipe file yang bisa dipilih, "*" untuk semua jenis
                startActivityForResult(Intent.createChooser(intent, "Pilih File"), FILE_CHOOSER_REQUEST_CODE);
                return true;
            }


            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                // Mendapatkan URL saat ini dari WebView
                String currentUrl = view.getUrl();
                getUrl1 = currentUrl;

                pBarTab1.setProgress(newProgress);
                if (newProgress == 100) {
                    pBarTab1.setVisibility(View.GONE);
                } else {
                    pBarTab1.setVisibility(View.VISIBLE);
                }

                // Lakukan sesuatu dengan URL saat ini, misalnya memperbarui TextView
                etSearch1.setText(getUrl1);


            }
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                // Simpan judul dalam string
                pageTitle1 = title;
                titleTab1.setText(pageTitle1);



                if (pageTitle1.length() > 6) {
                    // Judul memiliki lebih dari 10 karakter, atur animasi marquee
                    titleTab1.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                    titleTab1.setMarqueeRepeatLimit(-1);
                    titleTab1.setSelected(true);
                } else {
                    // Judul memiliki 10 karakter atau kurang, nonaktifkan animasi marquee
                    titleTab1.setEllipsize(TextUtils.TruncateAt.END);
                    titleTab1.setSelected(false);
                }
            }


        });
    }



}