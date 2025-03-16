package alv.splash.browser;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class TabManager {
    private static TabManager instance;
    private List<TabItem> tabs;
    private TabItem activeTab;
    private List<TabChangeListener> listeners;

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
        return newTab;
    }

    public void closeTab(String tabId) {
        TabItem tabToRemove = null;
        for (TabItem tab : tabs) {
            if (tab.getId().equals(tabId)) {
                tabToRemove = tab;
                break;
            }
        }

        if (tabToRemove != null) {
            boolean wasActive = tabToRemove.isActive();
            tabs.remove(tabToRemove);

            if (wasActive && !tabs.isEmpty()) {
                setActiveTab(tabs.get(tabs.size() - 1));
            } else if (tabs.isEmpty()) {
                activeTab = null;
            }

            notifyTabsChanged();
        }
    }

    public void setActiveTab(TabItem tab) {
        if (activeTab != null) {
            activeTab.setActive(false);
        }

        for (TabItem t : tabs) {
            if (t.getId().equals(tab.getId())) {
                t.setActive(true);
                activeTab = t;
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
                if (title != null) tab.setTitle(title);
                if (url != null) tab.setUrl(url);
                if (favicon != null) tab.setFavicon(favicon);
                break;
            }
        }
        notifyTabsChanged();
    }
}