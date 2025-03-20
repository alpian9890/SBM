package alv.splash.browser;

import android.graphics.Bitmap;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;


public class TabManager {
    private static TabManager instance;
    private List<TabItem> tabs;
    private TabItem activeTab;
    private List<TabChangeListener> listeners;
    private static final String TAG = "TabManager";

    public interface TabChangeListener {
        void onTabsChanged();
        void onActiveTabChanged(TabItem tab);
    }

    private TabManager() {
        tabs = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    public static synchronized TabManager getInstance() {
        if (instance == null) {
            instance = new TabManager();
        }
        return instance;
    }

    public TabItem addTab(String url) {
        TabItem newTab = new TabItem(url);
        tabs.add(newTab);
        setActiveTab(newTab);
        notifyTabsChanged();
        Log.d(TAG, "New tab added: " + newTab.getId());
        return newTab;
    }

    public void closeTab(String tabId) {
        Log.d(TAG, "Closing tab: " + tabId);

        // Find tab to remove
        TabItem tabToRemove = null;
        int tabIndex = -1;

        for (int i = 0; i < tabs.size(); i++) {
            TabItem tab = tabs.get(i);
            if (tab.getId().equals(tabId)) {
                tabToRemove = tab;
                tabIndex = i;
                break;
            }
        }

        if (tabToRemove != null) {
            boolean wasActive = tabToRemove.isActive();

            // Clean up tab resources
            if (CosmicExplorer.getInstance().isProfileModeEnabled()) {
                CosmicExplorer.getInstance().clearTabProfile(tabId);
            }

            // Remove tab
            tabs.remove(tabToRemove);

            // Ensure we always have at least one tab
            if (tabs.isEmpty()) {
                TabItem homeTab = addTab("about:home");
                setActiveTab(homeTab);
                Log.d(TAG, "Created new home tab as all tabs were closed");
            } else if (wasActive) {
                // Select the closest tab
                int newIndex = Math.min(tabIndex, tabs.size() - 1);
                setActiveTab(tabs.get(newIndex));
                Log.d(TAG, "Set active tab to: " + tabs.get(newIndex).getId());
            }

            notifyTabsChanged();
        } else {
            Log.w(TAG, "Attempted to close non-existent tab: " + tabId);
        }
    }

    public void setActiveTab(TabItem tab) {
        if (tab == null) {
            Log.w(TAG, "Attempted to set null tab as active");
            return;
        }

        if (activeTab != null && activeTab.getId().equals(tab.getId())) {
            Log.d(TAG, "Tab already active: " + tab.getId());
            return;
        }

        // Deactivate current active tab
        if (activeTab != null) {
            activeTab.setActive(false);
        }

        // Find and activate the requested tab
        for (TabItem t : tabs) {
            if (t.getId().equals(tab.getId())) {
                t.setActive(true);
                activeTab = t;
                Log.d(TAG, "Active tab set to: " + activeTab.getId());
                break;
            }
        }

        notifyActiveTabChanged();
    }

    public TabItem getActiveTab() {
        return activeTab;
    }

    public List<TabItem> getAllTabs() {
        return new ArrayList<>(tabs);
    }

    public void addTabChangeListener(TabChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeTabChangeListener(TabChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyTabsChanged() {
        for (TabChangeListener listener : listeners) {
            listener.onTabsChanged();
        }
    }

    private void notifyActiveTabChanged() {
        for (TabChangeListener listener : listeners) {
            listener.onActiveTabChanged(activeTab);
        }
    }

    public void updateTabInfo(String tabId, String title, String url, Bitmap favicon) {
        for (TabItem tab : tabs) {
            if (tab.getId().equals(tabId)) {
                if (title != null && !title.isEmpty()) {
                    tab.setTitle(title);
                }
                if (url != null) {
                    tab.setUrl(url);
                }
                if (favicon != null) {
                    tab.setFavicon(favicon);
                }
                break;
            }
        }
        notifyTabsChanged();
    }
}