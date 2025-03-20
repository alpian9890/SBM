package alv.splash.browser;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class TabsManagementFragment extends Fragment implements TabManager.TabChangeListener {
    private RecyclerView tabsRecyclerView;
    private TabsAdapter tabsAdapter;
    private TabManager tabManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tabManager = TabManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tabs_management, container, false);

        tabsRecyclerView = view.findViewById(R.id.tabsRecyclerView);
        tabsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tabsAdapter = new TabsAdapter(tabManager.getAllTabs(), new TabsAdapter.OnTabClickListener() {
            @Override
            public void onTabClick(TabItem tab) {
                tabManager.setActiveTab(tab);
                Log.d("TabsManagementFragment", "Tab clicked, ID: " + tab.getId() + ", [URL: " + tab.getUrl() + "], [Title: " + tab.getTitle()+"]");
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showTab(tab);
                }
            }

            @Override
            public void onTabClose(TabItem tab) {
                tabManager.closeTab(tab.getId());
                Log.d("TabsManagementFragment", "Tab closed, ID: " + tab.getId() + ", [URL: " + tab.getUrl() + "], [Title: " + tab.getTitle()+"]");
            }
        });

        tabsRecyclerView.setAdapter(tabsAdapter);

        // Add New Tab button
        Button btnAddNewTab = view.findViewById(R.id.btnAddNewTab);
        btnAddNewTab.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).createNewTab("about:home");
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        tabManager.addTabChangeListener(this);
        updateTabsList();
    }

    @Override
    public void onPause() {
        super.onPause();
        tabManager.removeTabChangeListener(this);
    }

    @Override
    public void onTabsChanged() {
        updateTabsList();
    }

    @Override
    public void onActiveTabChanged(TabItem tab) {
        updateTabsList();
    }

    private void updateTabsList() {
        if (tabsAdapter != null) {
            tabsAdapter.updateTabs(tabManager.getAllTabs());
        }
    }
}