package alv.splash.browser;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.MotionEvent;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;

import java.util.List;

public class StartWorking extends AppCompatActivity {

    String KBEarn = "https://kolotibablo.com";
    ValueCallback<Uri[]> filePathCallback;
    String getUrl1 = "";
    String getUrl2 ="";
    String pageTitle1 ="";
    String pageTitle2 = "";
    String title_earning = "Money Earning";
    private static final int FILE_CHOOSER_REQUEST_CODE = 100;
    FloatingActionButton fabControl;
    ImageView pageSecure1, pageSecure2, refreshTab1, refreshTab2;
    TextView titleTab1, titleTab2, textCalculatorMode;
    ProgressBar pBarTab1, pBarTab2;
    LinearLayout layoutAddressBar1, layoutAddressBar2;
    TextInputLayout etLayout1, etLayout2, etLayoutTitleKB;
    TextInputEditText etSearch1, etSearch2, editTextTitleKB;
	ToggleButton btnPointerVisibility;
    Drawable bg_lavender_rounded;
    Button btnSaveTitle;
    WebView webViewTab1;
    private static GeckoRuntime sRuntime;
    GeckoView viewGecko;
    GeckoSession sessionGecko;
    boolean canGoBack = false;

    BottomSheetDialog bottomSheetDialog;

    float lastX_right, lastY_right, lastX_left, lastY_left,
          lastX_pauseR, lastY_pauseR, lastX_pauseL, lastY_pauseL,
          lastX_closeR, lastY_closeR, lastX_closeL, lastY_closeL;;
    ImageView pointerRight, pointerLeft,
            pointerPauseR, pointerPauseL,
            pointerCloseR, pointerCloseL;
    TextView textPointerR, textPointerL;

    private boolean toggleClick = true;
    String injectInputKB =
            "var observer = new MutationObserver(function(mutations) {" +
                    "    mutations.forEach(function(mutation) {" +
                    "        if (mutation.addedNodes) {" +
                    "            mutation.addedNodes.forEach(function(node) {" +
                    "                if (node.nodeType === 1) {" +
                    "if (node.tagName ==='INPUT') { " +
                    "                    node.focus();" +
                    " }"+
                    "const inputs = node.getElementsByTagName('input');"+
                    "if (inputs.length === 0 || inputs.length > 0) {"+
                    "inputs[0].focus();"+
                    "}"+
                    "                }" +
                    "            });" +
                    "        }" +
                    "    });" +
                    "});" +
                    "observer.observe(document.body, { childList: true, subtree: true });";

    String autoFokusInput =
            "(function() { document.getElementsByTagName('input')[0].focus(); })();";

    // Buat instance UrlValidator
    private UrlValidator urlValidator = new UrlValidator();
    private boolean isCalculationMode = false;
    CalculatorSetress calculatorSetress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Menghilangkan judul aplikasi
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Membuat layar menjadi fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start_working);

        calculatorSetress = new CalculatorSetress(this);

        textCalculatorMode = findViewById(R.id.textCalculatorMode);
        // Set status awal
        updateCalculatorModeStatus();

        webViewTab1 = findViewById(R.id.webViewTab1);
        new AdBlockerWebView.init(this).initializeWebView(webViewTab1);

        pointerPauseR = findViewById(R.id.pointerPauseR);
        pointerPauseL = findViewById(R.id.pointerPauseL);
        pointerCloseR = findViewById(R.id.pointerCloseR);
        pointerCloseL = findViewById(R.id.pointerCloseL);

        fabControl = findViewById(R.id.fab_control);
        // FAB click listener
        fabControl.setOnClickListener(v -> showBottomSheet());
        FloatingActionButton testShortCut = findViewById(R.id.testCalc);
        testShortCut.setOnClickListener(v -> {
            new Thread(() -> {

                try {
                    sendKeyEvent(KeyEvent.KEYCODE_A, true);
                    Thread.sleep(100);
                    sendKeyEvent(KeyEvent.KEYCODE_X, true);
                    Thread.sleep(160);
                    processCalculation();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        });

        pBarTab1 = findViewById(R.id.pBarTab1);
        pBarTab2 = findViewById(R.id.pBarTab2);

        bg_lavender_rounded = getResources().getDrawable(R.drawable.edittext);

        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.sw_bottomsheet_control);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.setCanceledOnTouchOutside(true);

        titleTab1 = bottomSheetDialog.findViewById(R.id.titleTab1);
        titleTab2 = bottomSheetDialog.findViewById(R.id.titleTab2);
        pageSecure1 = bottomSheetDialog.findViewById(R.id.pageSecure1);
        pageSecure2 = bottomSheetDialog.findViewById(R.id.pageSecure2);
        refreshTab1 = bottomSheetDialog.findViewById(R.id.refreshTab1);
        refreshTab2 = bottomSheetDialog.findViewById(R.id.refreshTab2);
        etLayout1 = bottomSheetDialog.findViewById(R.id.etLayout1);
        etLayout2 = bottomSheetDialog.findViewById(R.id.etLayout2);
        etSearch1 = bottomSheetDialog.findViewById(R.id.et_search1);
        etSearch2 = bottomSheetDialog.findViewById(R.id.et_search2);

        layoutAddressBar1 = bottomSheetDialog.findViewById(R.id.layoutAddressBar1);
        layoutAddressBar2 = bottomSheetDialog.findViewById(R.id.layoutAddressBar2);

        etLayoutTitleKB = bottomSheetDialog.findViewById(R.id.etLayoutTitleKB);
        editTextTitleKB = bottomSheetDialog.findViewById(R.id.editTextTitleKB);
        btnSaveTitle = bottomSheetDialog.findViewById(R.id.btnSaveTitle);

        editTextTitleKB.setText(title_earning);

        loadTitleKB(title_earning);

        btnSaveTitle.setOnClickListener(v -> {
            title_earning = editTextTitleKB.getText().toString().trim();
            bottomSheetDialog.dismiss();
            Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();
            saveEditTextTitleKB(title_earning);
        });


        //BottomSheetDialog
        bottomSheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                View bottomSheetInternal = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheetInternal != null) {
                    BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheetInternal);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Munculkan dalam mode penuh
                }
            }
        });

        // Listener untuk mendeteksi saat dialog ditutup
        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // Ubah visibilitas saat dialog ditutup
                etLayout1.setVisibility(View.GONE);
                etLayout2.setVisibility(View.GONE);
                layoutAddressBar1.setBackground(bg_lavender_rounded);
                layoutAddressBar2.setBackground(bg_lavender_rounded);

                pageSecure1.setVisibility(View.VISIBLE);
                titleTab1.setVisibility(View.VISIBLE);
                refreshTab1.setVisibility(View.VISIBLE);

                pageSecure2.setVisibility(View.VISIBLE);
                titleTab2.setVisibility(View.VISIBLE);
                refreshTab2.setVisibility(View.VISIBLE);

            }
        });

        titleTab1.setOnClickListener(v -> switchViewSearch1());
        titleTab2.setOnClickListener(v -> switchViewSearch2());

        // Event saat tombol enter ditekan
        etSearch1.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_SEARCH ||
			(event != null && 
			event.getKeyCode() == KeyEvent.KEYCODE_ENTER && 
			event.getAction() == KeyEvent.ACTION_DOWN)) {
        
        String userInput = etSearch1.getText().toString();
        String processedUrl = urlValidator.processInput(userInput);

        // Memuat URL yang sudah diproses ke WebView
        webViewTab1.loadUrl(processedUrl);

        // Sembunyikan keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearch1.getWindowToken(), 0);
        bottomSheetDialog.dismiss();
        return true;
    }
    return false;
});
        // Event saat tombol enter ditekan
        etSearch2.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null &&
			event.getKeyCode() == KeyEvent.KEYCODE_ENTER && 
			event.getAction() == KeyEvent.ACTION_DOWN)) {
                String userInput = etSearch2.getText().toString();
                String processedUrl = urlValidator.processInput(userInput);

                // Memuat URL yang sudah diproses ke WebView
                sessionGecko.loadUri(processedUrl);

                // Sembunyikan keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etSearch2.getWindowToken(), 0);
                bottomSheetDialog.dismiss();
				Log.d("SearchDebug", "ActionId: " + actionId);
				if (event != null) {
					Log.d("SearchDebug", "KeyCode: " + event.getKeyCode());
					Log.d("SearchDebug", "Action: " + event.getAction());
					}
                return true;
            }
            return false;
        });
        // Event saat kehilangan fokus
        etSearch1.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                etLayout1.setVisibility(View.GONE);
                layoutAddressBar1.setBackground(bg_lavender_rounded);

                pageSecure1.setVisibility(View.VISIBLE);
                titleTab1.setVisibility(View.VISIBLE);
                refreshTab1.setVisibility(View.VISIBLE);
            }
        });

        // Event saat kehilangan fokus
        etSearch2.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                etLayout2.setVisibility(View.GONE);
                layoutAddressBar2.setBackground(bg_lavender_rounded);

                pageSecure2.setVisibility(View.VISIBLE);
                titleTab2.setVisibility(View.VISIBLE);
                refreshTab2.setVisibility(View.VISIBLE);
            }
        });

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

        refreshTab1.setOnClickListener(v -> webViewTab1.reload());
        refreshTab2.setOnClickListener(v -> sessionGecko.reload());
        //End BottomsheetDialog





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


        pointerRight = findViewById(R.id.pointerRight);
        pointerLeft = findViewById(R.id.pointerLeft);
        textPointerR = findViewById(R.id.textPointerR);
        textPointerL = findViewById(R.id.textPointerL);
		
		btnPointerVisibility = bottomSheetDialog.findViewById(R.id.btnPointerVisibilty);
        btnPointerVisibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Jika isChecked adalah true, maka set cursor menjadi VISIBLE
                if (isChecked) {
                    pointerRight.setVisibility(View.VISIBLE);
                    pointerLeft.setVisibility(View.VISIBLE);

                    pointerPauseR.setVisibility(View.VISIBLE);
                    pointerPauseL.setVisibility(View.VISIBLE);

                    pointerCloseR.setVisibility(View.VISIBLE);
                    pointerCloseL.setVisibility(View.VISIBLE);
                } else {
                    // Jika isChecked adalah false, maka set cursor menjadi INVISIBLE
                    pointerRight.setVisibility(View.INVISIBLE);
                    pointerLeft.setVisibility(View.INVISIBLE);
					
					pointerPauseR.setVisibility(View.INVISIBLE);
                    pointerPauseL.setVisibility(View.INVISIBLE);

                    pointerCloseR.setVisibility(View.INVISIBLE);
                    pointerCloseL.setVisibility(View.INVISIBLE);
                }
            }
        });

        setupWebViewSettings();
        setupWebViewClient();
        setupWebChromeClient();
        webViewTab1.loadUrl(KBEarn);

        sessionGecko.loadUri(KBEarn); // URL...

        View rootViewSW = findViewById(android.R.id.content);
        rootViewSW.post(() -> {
            setupPointerMovement();

            float[] pointerRightPos = loadPointerPosition("pointerRightX", "pointerRightY");
            pointerRight.setX(Math.max(0, Math.min(pointerRightPos[0], viewGecko.getWidth() - pointerRight.getWidth())));
            pointerRight.setY(Math.max(0, Math.min(pointerRightPos[1], viewGecko.getHeight() - pointerRight.getHeight())));

            float[] pointerPauseRPos = loadPointerPosition("pointerPauseRX", "pointerPauseRY");
            pointerRight.setX(Math.max(0, Math.min(pointerPauseRPos[0], viewGecko.getWidth() - pointerPauseR.getWidth())));
            pointerRight.setY(Math.max(0, Math.min(pointerPauseRPos[1], viewGecko.getHeight() - pointerPauseR.getHeight())));

            float[] pointerCloseRPos = loadPointerPosition("pointerCloseRX", "pointerCloseRY");
            pointerCloseR.setX(Math.max(0, Math.min(pointerCloseRPos[0], viewGecko.getWidth() - pointerCloseR.getWidth())));
            pointerCloseR.setY(Math.max(0, Math.min(pointerCloseRPos[1], viewGecko.getHeight() - pointerCloseR.getHeight())));

            float[] pointerLeftPos = loadPointerPosition("pointerLeftX", "pointerLeftY");
            pointerLeft.setX(Math.max(0, Math.min(pointerLeftPos[0], webViewTab1.getWidth() - pointerLeft.getWidth())));
            pointerLeft.setY(Math.max(0, Math.min(pointerLeftPos[1], webViewTab1.getHeight() - pointerLeft.getHeight())));

            float[] pointerPauseLPos = loadPointerPosition("pointerPauseLX", "pointerPauseLY");
            pointerPauseL.setX(Math.max(0, Math.min(pointerPauseLPos[0], webViewTab1.getWidth() - pointerPauseL.getWidth())));
            pointerPauseL.setY(Math.max(0, Math.min(pointerPauseLPos[1], webViewTab1.getHeight() - pointerPauseL.getHeight())));

            float[] pointerCloseLPos = loadPointerPosition("pointerCloseLX", "pointerCloseLX");
            pointerCloseL.setX(Math.max(0, Math.min(pointerCloseLPos[0], webViewTab1.getWidth() - pointerCloseL.getWidth())));
            pointerCloseL.setY(Math.max(0, Math.min(pointerCloseLPos[1], webViewTab1.getHeight() - pointerCloseL.getHeight())));


        });



    }// akhir onCreate

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (getUrl1.contains("kolotibablo.com") || getUrl2.contains("kolotibablo.com")){

            if (pageTitle1.contains(title_earning) && pageTitle2.contains(title_earning)) {

                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    // Tangani tombol ENTER dengan ACTION_UP
                    super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                    new Handler().postDelayed(() -> performAutoClick(), 200);
                    return true;
                }

                if (event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE && event.getAction() == KeyEvent.ACTION_UP) {
                    // Tangani tombol ENTER dengan ACTION_UP
                    super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ESCAPE));
                    new Handler().postDelayed(() -> performAutoClick(), 300);
                    return true;
                }

                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                   event.isAltPressed() && event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_7) {
                    simulateTouch(webViewTab1, lastX_pauseL, lastY_pauseL);
                    return true;
                }
                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.isAltPressed() && event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_9) {
                    simulateTouch(viewGecko, lastX_pauseR, lastY_pauseR);
                    return true;
                }

                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.isCtrlPressed() &&
                        event.isAltPressed() &&
                        event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_7) {
                    simulateTouch(webViewTab1, lastX_closeL, lastY_closeL);
                }
                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.isCtrlPressed() &&
                        event.isAltPressed() &&
                        event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_9) {
                    simulateTouch(viewGecko, lastX_closeR, lastY_closeR);
                }

            }

            if (event.getAction() == KeyEvent.ACTION_DOWN &&
                    event.isCtrlPressed() &&
                    event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_DOT ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.isCtrlPressed() &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE ) {
                new Thread(() -> {

                    try {
                        sendKeyEvent(KeyEvent.KEYCODE_A, true);
                        Thread.sleep(100);
                        sendKeyEvent(KeyEvent.KEYCODE_X, true);
                        Thread.sleep(160);
                        processCalculation();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                return true;
            }

        }
        return super.dispatchKeyEvent(event);
    }

    private void updateCalculatorModeStatus() {
        // Update text dan warna background berdasarkan status mode
        if (isCalculationMode) {
            textCalculatorMode.setText("Calculator: ON");
            textCalculatorMode.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            textCalculatorMode.setText("Calculator: OFF");
            textCalculatorMode.setTextColor(getResources().getColor(R.color.red));
        }
    }

    private void handleCalculationModeToggle() {
        isCalculationMode = !isCalculationMode;
        // Set status awal
        updateCalculatorModeStatus();
        if (!isCalculationMode) {
            //processCalculation();
        }
    }

    private void sendKeyEvent(int keyCode, boolean withCtrl) {
        runOnUiThread(() -> { // Bungkus dengan runOnUiThread
            long eventTime = System.currentTimeMillis();

            KeyEvent pressEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0,
                    withCtrl ? KeyEvent.META_CTRL_ON : 0);
            dispatchKeyEvent(pressEvent);

            KeyEvent releaseEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0,
                    withCtrl ? KeyEvent.META_CTRL_ON : 0);
            dispatchKeyEvent(releaseEvent);
        });
    }

    private void processCalculation() {
        if (getClipboardData().isEmpty()) {
            calculatorSetress.showError("Tidak ada perhitungan yang dimasukkan");
            return;
        }

        String input = getClipboardData();
        if (input != null) {
            try {
                String result = calculatorSetress.calculate(input);
                new Thread(() -> {
                    try {
                        Thread.sleep(100);
                        copyToClipboard(result);
                        Thread.sleep(100);
                        sendKeyEvent(KeyEvent.KEYCODE_V, true);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getClipboardData() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip() &&
                clipboard.getPrimaryClipDescription() != null &&
                clipboard.getPrimaryClipDescription().hasMimeType("text/plain")) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            return item.getText().toString(); // Ambil teks dari clipboard
        }
        return ""; // Jika clipboard kosong atau tidak ada data teks
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("hasil_kalkulator", text);
        clipboard.setPrimaryClip(clip);
    }

    private void clearInputField() {
        runOnUiThread(() -> {
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CLEAR));
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CLEAR));
        });
    }

    private void pasteFromClipboard() {
        runOnUiThread(() -> {
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_PASTE));
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_PASTE));
        });
    }

    private void performAutoClick() {

        if (toggleClick) {
            // AutoClick untuk pointerRight
            //webViewTab1.clearFocus();
            viewGecko.requestFocus(); // Root view batas pointerRight
            simulateTouch(viewGecko, lastX_right, lastY_right);
            toggleClick = false;
        } else {
            // AutoClick untuk pointerLeft
            //viewGecko.clearFocus();
            webViewTab1.requestFocus(); // Root view batas pointerLeft
            simulateDoubleClick(webViewTab1, lastX_left, lastY_left);
            toggleClick = true;
        }
    }

    private void simulateTouch(View rootView, float x, float y) {
        // Konversi koordinat ke layar global (jika diperlukan)
        int[] location = new int[2];
        rootView.getLocationOnScreen(location);
        float screenX = location[0] + x;
        float screenY = location[1] + y;

        // Buat MotionEvent ACTION_DOWN
        long downTime = SystemClock.uptimeMillis();
        MotionEvent downEvent = MotionEvent.obtain(
                downTime,
                downTime,
                MotionEvent.ACTION_DOWN,
                screenX,
                screenY,
                0
        );

        // Buat MotionEvent ACTION_UP
        long upTime = SystemClock.uptimeMillis();
        MotionEvent upEvent = MotionEvent.obtain(
                downTime,
                upTime,
                MotionEvent.ACTION_UP,
                screenX,
                screenY,
                0
        );

        // Kirim event ke rootView
        rootView.dispatchTouchEvent(downEvent);
        new Handler().postDelayed(() -> rootView.dispatchTouchEvent(upEvent), 150);

        // Release event
        downEvent.recycle();
        new Handler().postDelayed(() -> upEvent.recycle(), 100);
    }

    private void simulateDoubleClick(View rootView, float x, float y) {
        // Konversi koordinat ke layar global (jika diperlukan)

        float screenX = x;
        float screenY = y;

        // Durasi antar klik untuk double click
        final int doubleClickInterval = 120; // 100ms antara klik pertama dan kedua

        // Simulasi klik pertama
        long downTime = SystemClock.uptimeMillis();
        MotionEvent downEvent1 = MotionEvent.obtain(
                downTime,
                downTime,
                MotionEvent.ACTION_DOWN,
                screenX,
                screenY,
                0
        );

        MotionEvent upEvent1 = MotionEvent.obtain(
                downTime,
                downTime + 50, // 50ms setelah ACTION_DOWN
                MotionEvent.ACTION_UP,
                screenX,
                screenY,
                0
        );

        // Simulasi klik kedua
        long secondClickTime = downTime + doubleClickInterval;
        MotionEvent downEvent2 = MotionEvent.obtain(
                secondClickTime,
                secondClickTime,
                MotionEvent.ACTION_DOWN,
                screenX,
                screenY,
                0
        );

        MotionEvent upEvent2 = MotionEvent.obtain(
                secondClickTime,
                secondClickTime + 50, // 50ms setelah ACTION_DOWN kedua
                MotionEvent.ACTION_UP,
                screenX,
                screenY,
                0
        );

        // Kirim event untuk klik pertama
        rootView.dispatchTouchEvent(downEvent1);
        rootView.dispatchTouchEvent(upEvent1);

        // Delay sebelum klik kedua
        new Handler().postDelayed(() -> {
            rootView.dispatchTouchEvent(downEvent2);
            rootView.dispatchTouchEvent(upEvent2);

            // Release semua event
            downEvent1.recycle();
            upEvent1.recycle();
            downEvent2.recycle();
            upEvent2.recycle();
        }, doubleClickInterval);
    }


    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onBackPressed() {
        // Cek apakah WebView terlihat (visible) dan mendapatkan fokus
        if (webViewTab1.hasFocus()) {
            if (webViewTab1.canGoBack()) {
                webViewTab1.goBack(); // Kembali ke URL sebelumnya jika ada riwayat navigasi
            } else {
                webViewTab1.clearCache(false);
                webViewTab1.clearFocus();
                viewGecko.clearFocus();
               // webViewTab1.setVisibility(View.GONE);
                //Menonaktifkan WebView
               // webViewCSP.setEnabled(false);

                showExitConfirmationDialog();
            }
        }

        if (viewGecko.hasFocus()) {
            if (canGoBack == true) {
                sessionGecko.goBack();
            } else {
                viewGecko.clearFocus();
                webViewTab1.clearFocus();
                showExitConfirmationDialog();
            }
        }

        /*
        if (!webViewTab1.hasFocus()) {
            if (!viewGecko.hasFocus()){
                showExitConfirmationDialog();
            } else {
                if (canGoBack == true) {
                    sessionGecko.goBack();
                } else {
                    showExitConfirmationDialog();
                }
            }
        } else if (!viewGecko.hasFocus()) {
            if (!webViewTab1.hasFocus()) {
                showExitConfirmationDialog();
            } else {
                if (webViewTab1.canGoBack()) {
                    webViewTab1.goBack();
                } else {
                    showExitConfirmationDialog();
                }
            }
        } else */ if (webViewTab1.getVisibility()==View.GONE && viewGecko.getVisibility()==View.GONE) {
            super.onBackPressed();
        }
        //super.onBackPressed();
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
            public void onPageStart(@NonNull @NotNull GeckoSession session, @NonNull @NotNull String url) {
                pBarTab2.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageStop(@NonNull @NotNull GeckoSession session, boolean success) {
                pBarTab2.setVisibility(View.GONE);
                if (pageTitle2.contains("kolotibablo.com")) {
                    session.loadUri("javascript:"+injectInputKB);
                }
            }
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

        /*
        LinearLayout rootBsSW = bottomSheetView.findViewById(R.id.rootBottomsheetSW);

        BottomSheetBehavior behaviorBsSW = BottomSheetBehavior.from(rootBsSW);

        behaviorBsSW.setState(BottomSheetBehavior.STATE_EXPANDED);
        */

        etSearch1.setText(getUrl1);
        etSearch2.setText(getUrl2);
        titleTab1.setText(pageTitle1);
        titleTab2.setText(pageTitle2);

        bottomSheetDialog.show();
    }


    private void switchViewSearch1(){
        pageSecure1.setVisibility(View.GONE);
        titleTab1.setVisibility(View.GONE);
        refreshTab1.setVisibility(View.GONE);

        etLayout1.setVisibility(View.VISIBLE);
        layoutAddressBar1.setBackground(null);

        etSearch1.selectAll();
        etSearch1.requestFocus();
        showKeyboard(etSearch1);
    }

    private void switchViewSearch2(){
        pageSecure2.setVisibility(View.GONE);
        titleTab2.setVisibility(View.GONE);
        refreshTab2.setVisibility(View.GONE);

        etLayout2.setVisibility(View.VISIBLE);
        layoutAddressBar2.setBackground(null);

        etSearch2.selectAll();
        etSearch2.requestFocus();
        showKeyboard(etSearch2);
    }



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

                if (url.contains("kolotibablo.com")) {
                    view.evaluateJavascript(injectInputKB, null);
                }

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
    private TextWatcher titleTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String newTitle = s.toString().trim();
            title_earning = newTitle;
            saveEditTextTitleKB(title_earning);
        }
    };
    private void saveEditTextTitleKB (String titleKB) {

        try {
            SharedPreferences sharedPreferences = getSharedPreferences("titleKB", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("titleKB", titleKB);
            editor.apply();
            title_earning = titleKB;
            // Debugging log
            Log.d("titleKB", "Saved " + " : " + titleKB);
            Toast.makeText(this, "Title saved successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("titleKB", "Error saving title: " + e.getMessage());
            Toast.makeText(this, "Error saving title:  "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void loadTitleKB(String titleKB) {
        SharedPreferences sharedPreferences = getSharedPreferences("titleKB", MODE_PRIVATE);
        title_earning = sharedPreferences.getString("titleKB", titleKB);
        editTextTitleKB.setText(title_earning);
        // Debugging log
        Log.d("titleKB", "Loaded " + " : " + titleKB);

    }

    private void savePointerPosition(String keyX, String keyY, float x, float y) {
        SharedPreferences sharedPreferences = getSharedPreferences("PointerPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(keyX, x);
        editor.putFloat(keyY, y);
        editor.apply();

        // Debugging log
        Log.d("PointerPrefs", "Saved " + keyX + ": " + x + ", " + keyY + ": " + y);
    }

    private float[] loadPointerPosition(String keyX, String keyY) {
        SharedPreferences sharedPreferences = getSharedPreferences("PointerPrefs", MODE_PRIVATE);
        float x = sharedPreferences.getFloat(keyX, 0);
        float y = sharedPreferences.getFloat(keyY, 0);

        // Debugging log
        Log.d("PointerPrefs", "Loaded " + keyX + ": " + x + ", " + keyY + ": " + y);

        return new float[]{x, y};
    }


    private void setupPointerMovement() {

        pointerRight.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Ambil jarak antara titik sentuh dan posisi ImageView
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        textPointerR.setVisibility(View.VISIBLE);
                        textPointerR.setText("X: " + dX + " Y: " + dY);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // Hitung posisi baru berdasarkan titik sentuh
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // Dapatkan batas root layout
                        int rootWidth = viewGecko.getWidth();
                        int rootHeight = viewGecko.getHeight();

                        // Pastikan pointer tetap dalam batas
                        if (newX < 0) newX = 0;
                        if (newX + v.getWidth() > rootWidth) newX = rootWidth - v.getWidth();
                        if (newY < 0) newY = 0;
                        if (newY + v.getHeight() > rootHeight) newY = rootHeight - v.getHeight();

                        // Pindahkan pointer
                        v.setX(newX);
                        v.setY(newY);
                        textPointerR.setText("X: " + newX + " Y: " + newY);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Simpan koordinat saat pointer dilepaskan
                        lastX_right = v.getX();
                        lastY_right = v.getY();
                        savePointerPosition("pointerRightX", "pointerRightY", lastX_right, lastY_right);
                        Log.d("PointerMovement", "Saved pointerRight: X=" + lastX_right + ", Y=" + lastY_right);
                        textPointerR.setText("X: " + lastX_right + " Y: " + lastY_right);
                        // Simpan atau gunakan koordinat untuk kebutuhan Anda
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                textPointerR.setVisibility(View.GONE);
                            }
                        }, 1000);
                        break;
                }
                return true;
            }
        });

        pointerLeft.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Ambil jarak antara titik sentuh dan posisi ImageView
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        textPointerL.setVisibility(View.VISIBLE);
                        textPointerL.setText("X: " + dX + " Y: " + dY);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // Hitung posisi baru berdasarkan titik sentuh
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // Dapatkan batas root layout
                        int rootWidth = webViewTab1.getWidth();
                        int rootHeight = webViewTab1.getHeight();

                        // Pastikan pointer tetap dalam batas
                        if (newX < 0) newX = 0;
                        if (newX + v.getWidth() > rootWidth) newX = rootWidth - v.getWidth();
                        if (newY < 0) newY = 0;
                        if (newY + v.getHeight() > rootHeight) newY = rootHeight - v.getHeight();

                        // Pindahkan pointer
                        v.setX(newX);
                        v.setY(newY);
                        textPointerL.setText("X: " + newX + " Y: " + newY);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Simpan koordinat saat pointer dilepaskan
                        lastX_left = v.getX();
                        lastY_left = v.getY();
                        savePointerPosition("pointerLeftX", "pointerLeftY", lastX_left, lastY_left);
                        Log.d("PointerMovement", "Saved pointerLeft: X=" + lastX_left + ", Y=" + lastY_left);
                        textPointerL.setText("X: " + lastX_left + " Y: " + lastY_left);
                        // Simpan atau gunakan koordinat untuk kebutuhan Anda
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                textPointerL.setVisibility(View.GONE);
                            }
                        }, 1000);

                        break;
                }
                return true;
            }
        });

        pointerPauseR.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Ambil jarak antara titik sentuh dan posisi ImageView
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        textPointerR.setVisibility(View.VISIBLE);
                        textPointerR.setText("X: " + dX + " Y: " + dY);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // Hitung posisi baru berdasarkan titik sentuh
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // Dapatkan batas root layout
                        int rootWidth = viewGecko.getWidth();
                        int rootHeight = viewGecko.getHeight();

                        // Pastikan pointer tetap dalam batas
                        if (newX < 0) newX = 0;
                        if (newX + v.getWidth() > rootWidth) newX = rootWidth - v.getWidth();
                        if (newY < 0) newY = 0;
                        if (newY + v.getHeight() > rootHeight) newY = rootHeight - v.getHeight();

                        // Pindahkan pointer
                        v.setX(newX);
                        v.setY(newY);
                        textPointerR.setText("X: " + newX + " Y: " + newY);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Simpan koordinat saat pointer dilepaskan
                        lastX_pauseR = v.getX();
                        lastY_pauseR = v.getY();
                        savePointerPosition("pointerPauseRX", "pointerPauseRY", lastX_pauseR, lastY_pauseR);
                        Log.d("PointerMovement", "Saved pointerRight: X=" + lastX_pauseR + ", Y=" + lastY_pauseR);
                        textPointerR.setText("X: " + lastX_pauseR + " Y: " + lastY_pauseR);
                        // Simpan atau gunakan koordinat untuk kebutuhan Anda
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                textPointerR.setVisibility(View.GONE);
                            }
                        }, 1000);
                        break;
                }
                return true;
            }
        });

        pointerPauseL.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Ambil jarak antara titik sentuh dan posisi ImageView
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        textPointerL.setVisibility(View.VISIBLE);
                        textPointerL.setText("X: " + dX + " Y: " + dY);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // Hitung posisi baru berdasarkan titik sentuh
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // Dapatkan batas root layout
                        int rootWidth = webViewTab1.getWidth();
                        int rootHeight = webViewTab1.getHeight();

                        // Pastikan pointer tetap dalam batas
                        if (newX < 0) newX = 0;
                        if (newX + v.getWidth() > rootWidth) newX = rootWidth - v.getWidth();
                        if (newY < 0) newY = 0;
                        if (newY + v.getHeight() > rootHeight) newY = rootHeight - v.getHeight();

                        // Pindahkan pointer
                        v.setX(newX);
                        v.setY(newY);
                        textPointerL.setText("X: " + newX + " Y: " + newY);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Simpan koordinat saat pointer dilepaskan
                        lastX_pauseL = v.getX();
                        lastY_pauseL = v.getY();
                        savePointerPosition("pointerPauseLX", "pointerPauseLY", lastX_pauseL, lastY_pauseL);
                        Log.d("PointerMovement", "Saved pointerRight: X=" + lastX_pauseL + ", Y=" + lastY_pauseL);
                        textPointerL.setText("X: " + lastX_pauseL + " Y: " + lastY_pauseL);
                        // Simpan atau gunakan koordinat untuk kebutuhan Anda
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                textPointerL.setVisibility(View.GONE);
                            }
                        }, 1000);
                        break;
                }
                return true;
            }
        });

        pointerCloseR.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Ambil jarak antara titik sentuh dan posisi ImageView
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        textPointerR.setVisibility(View.VISIBLE);
                        textPointerR.setText("X: " + dX + " Y: " + dY);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // Hitung posisi baru berdasarkan titik sentuh
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // Dapatkan batas root layout
                        int rootWidth = viewGecko.getWidth();
                        int rootHeight = viewGecko.getHeight();

                        // Pastikan pointer tetap dalam batas
                        if (newX < 0) newX = 0;
                        if (newX + v.getWidth() > rootWidth) newX = rootWidth - v.getWidth();
                        if (newY < 0) newY = 0;
                        if (newY + v.getHeight() > rootHeight) newY = rootHeight - v.getHeight();

                        // Pindahkan pointer
                        v.setX(newX);
                        v.setY(newY);
                        textPointerR.setText("X: " + newX + " Y: " + newY);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Simpan koordinat saat pointer dilepaskan
                        lastX_closeR = v.getX();
                        lastY_closeR = v.getY();
                        savePointerPosition("pointerCloseRX", "pointerCloseRY", lastX_closeR, lastY_closeR);
                        Log.d("PointerMovement", "Saved pointerRight: X=" + lastX_closeR + ", Y=" + lastY_closeR);
                        textPointerR.setText("X: " + lastX_closeR + " Y: " + lastY_closeR);
                        // Simpan atau gunakan koordinat untuk kebutuhan Anda
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                textPointerR.setVisibility(View.GONE);
                            }
                        }, 1000);
                        break;
                }
                return true;
            }
        });

        pointerCloseL.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Ambil jarak antara titik sentuh dan posisi ImageView
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        textPointerL.setVisibility(View.VISIBLE);
                        textPointerL.setText("X: " + dX + " Y: " + dY);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // Hitung posisi baru berdasarkan titik sentuh
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // Dapatkan batas root layout
                        int rootWidth = webViewTab1.getWidth();
                        int rootHeight = webViewTab1.getHeight();

                        // Pastikan pointer tetap dalam batas
                        if (newX < 0) newX = 0;
                        if (newX + v.getWidth() > rootWidth) newX = rootWidth - v.getWidth();
                        if (newY < 0) newY = 0;
                        if (newY + v.getHeight() > rootHeight) newY = rootHeight - v.getHeight();

                        // Pindahkan pointer
                        v.setX(newX);
                        v.setY(newY);
                        textPointerL.setText("X: " + newX + " Y: " + newY);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Simpan koordinat saat pointer dilepaskan
                        lastX_closeL = v.getX();
                        lastY_closeL = v.getY();
                        savePointerPosition("pointerCloseLX", "pointerCloseLY", lastX_closeL, lastY_closeL);
                        Log.d("PointerMovement", "Saved pointerRight: X=" + lastX_closeL + ", Y=" + lastY_closeL);
                        textPointerL.setText("X: " + lastX_closeL + " Y: " + lastX_closeL);
                        // Simpan atau gunakan koordinat untuk kebutuhan Anda
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                textPointerL.setVisibility(View.GONE);
                            }
                        }, 1000);
                        break;
                }
                return true;
            }
        });


    }





}