package alv.splash.browser;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;

import java.util.List;

public class GeckoViewFragment extends Fragment {
    private String tabId;
    private GeckoView geckoView;
    private GeckoSession session;
    private GeckoRuntime runtime;
    private ProgressBar progressBar;
    private String initialUrl;
    private boolean canGoBack = false;
    private boolean canGoForward = false;
    private String getUrl = "";
    private String pageTitle = "";
    private boolean pageSecure = false;
    private HistoryManager historyManager;

    public static GeckoViewFragment newInstance(String tabId, String url) {
        GeckoViewFragment fragment = new GeckoViewFragment();
        Bundle args = new Bundle();
        args.putString("tabId", tabId);
        args.putString("url", url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabId = getArguments().getString("tabId");
            initialUrl = getArguments().getString("url");
        }

        historyManager = HistoryManager.getInstance(requireContext());

        // Initialize GeckoRuntime (should be done once per application)
        if (runtime == null) {
            runtime = GeckoRuntime.create(requireContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_gecko, container, false);
        geckoView = view.findViewById(R.id.webGeckoView);
        progressBar = view.findViewById(R.id.progressBarWeb);

        setupGeckoSession();

        return view;
    }

    private void setupGeckoSession() {
        session = new GeckoSession();

        session.setProgressDelegate(new GeckoSession.ProgressDelegate() {
            @Override
            public void onPageStart(GeckoSession session, String url) {
                progressBar.setVisibility(View.VISIBLE);
                getUrl = url;
                GlideFaviconFetcher.fetchFavicon(requireContext(), Uri.parse(getUrl).getHost(), new GlideFaviconFetcher.FaviconCallback() {
                    @Override
                    public void onFaviconLoaded(Bitmap bitmap) {
                        // Update the tab info with the starting URL
                        TabManager.getInstance().updateTabInfo(tabId, "Loading...", url, bitmap);
                    }

                    @Override
                    public void onError(Exception e) {
                        // Update the tab info with the starting URL
                        TabManager.getInstance().updateTabInfo(tabId, e.getMessage(), url, null);
                    }
                });
            }

            @Override
            public void onPageStop(GeckoSession session, boolean success) {
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onProgressChange(GeckoSession session, int progress) {
                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSecurityChange(GeckoSession session, SecurityInformation securityInfo) {
                pageSecure = securityInfo.isSecure;

            }


        });

        session.setContentDelegate(new GeckoSession.ContentDelegate() {
            @Override
            public void onTitleChange(GeckoSession session, String title) {
                // Update the tab title
                pageTitle = title;
                TabManager.getInstance().updateTabInfo(tabId, pageTitle, null, null);
                // Update address bar title
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).updateAddressBarInfo(pageTitle, pageSecure);

                }
            }

        });

        session.setNavigationDelegate(new GeckoSession.NavigationDelegate() {
           /* @Override
            public void onLoadRequest(GeckoSession session, GeckoSession.NavigationDelegate.LoadRequest request) {
               // System.out.println("Navigasi ke URL: " + request.uri);
            }*/

            @Override
            public void onCanGoBack(GeckoSession session, boolean GcanGoBack) {
                canGoBack = GcanGoBack;
            }

            @Override
            public void onCanGoForward(GeckoSession session, boolean GcanGoForward) {
                canGoForward = GcanGoForward;
            }

            @Override
            public void onLocationChange(GeckoSession session, String url, List<GeckoSession.PermissionDelegate.ContentPermission> perms, Boolean hasUserGesture) {
                getUrl = url;
            }



        });

        session.open(runtime);
        geckoView.setSession(session);

        // Load the initial URL
        if (initialUrl != null && !initialUrl.isEmpty()) {
            loadUrl(initialUrl);
        }
    }

    public void loadUrl(String url) {
        String validatedUrl = new UrlValidator().processInput(url);
        session.loadUri(validatedUrl);

        // Save to history
        saveToHistory(validatedUrl);
    }

    private void saveToHistory(String url) {
        // Get current timestamp
        long timestamp = System.currentTimeMillis();

        // Create history entry
        HistoryItem historyItem = new HistoryItem(url, pageTitle, timestamp);

        // Save to database
        historyManager.addHistoryItem(historyItem);
    }

    @Override
    public void onDestroy() {
        if (session != null) {
            session.close();
        }
        super.onDestroy();
    }

    public boolean canGoBack() {
        return canGoBack;
    }

    public void goBack() {
        if (canGoBack) {
            session.goBack();
        }
    }

    public boolean canGoForward() {
        return canGoForward;
    }

    public void goForward() {
        if (canGoForward) {
            session.goForward();
        }
    }

    public void reload() {
        session.reload();
    }

    public boolean isSecure() {
        return pageSecure;
    }
    public String getCurrentUrl() {
        if (session != null) {
            if (getUrl != null && !getUrl.isEmpty()) {
                return getUrl;
            } else {
                return "";
            }
        } else {
            return "";
        }
    }
}