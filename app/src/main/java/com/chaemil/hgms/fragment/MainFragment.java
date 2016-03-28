package com.chaemil.hgms.fragment;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.SearchAdapter;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.MyRequestService;
import com.chaemil.hgms.utils.SharedPrefUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.clans.fab.FloatingActionButton;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaemil on 4.12.15.
 */
public class MainFragment extends BaseFragment implements TabLayout.OnTabSelectedListener,
        View.OnClickListener, MaterialSearchView.SearchViewListener,
        MaterialSearchView.OnQueryTextListener, RequestFactoryListener, AdapterView.OnItemClickListener {

    public static final String TAG = "main_fragment";
    private TabLayout tabLayout;
    private ViewPager pager;
    private FragmentActivity context;
    private HomeFragment homeFragment;
    private ArchiveFragment archiveFragment;
    private DownloadedFragment downloadedFragment;
    private Toolbar toolbar;
    private RelativeLayout appBar;
    private FrameLayout photoalbumWrapper;
    private PhotoAlbumFragment photoAlbumFragment;
    private MaterialSearchView searchView;
    private FloatingActionButton searchFab;
    private Toolbar toolbarSecondary;
    private TextView toolbarSecondaryTitle;
    private ArrayList<ArchiveItem> searchResult = new ArrayList<>();
    private SearchAdapter searchAdapter;
    private FrameLayout searchContainer;
    private RelativeLayout backWrapper;
    private ImageButton back;
    private FloatingActionButton settingsFab;
    private SharedPrefUtils sharedPreferences;
    private CardView settingsCard;
    private SwitchCompat downloadOnWifiSwitch;
    private RelativeLayout settingsCardBg;
    private SwitchCompat streamOnWifiSwitch;
    private SwitchCompat streamOnlyAudioSwitch;

    @Override
    public void onAttach(Activity activity) {
        context = (FragmentActivity) activity;
        super.onAttach(activity);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            SmartLog.Log(SmartLog.LogLevel.DEBUG, TAG + " savedInstanceState", "null");
            homeFragment = new HomeFragment();
            archiveFragment = new ArchiveFragment();
            downloadedFragment = new DownloadedFragment();
            photoAlbumFragment = new PhotoAlbumFragment();
            searchAdapter = new SearchAdapter(getActivity(), R.layout.search_item,
                    ((MainActivity) getActivity()), searchResult);
            sharedPreferences = SharedPrefUtils.getInstance(getContext());
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.main_fragment, container, false);

        getUI(rootView);
        setupUI(savedInstanceState);

        return rootView;
    }

    private void getUI(ViewGroup rootView) {
        tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
        appBar = (RelativeLayout) rootView.findViewById(R.id.app_bar);
        pager = (ViewPager) rootView.findViewById(R.id.pager);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        photoalbumWrapper = (FrameLayout) rootView.findViewById(R.id.photoalbum_wrapper);
        searchView = (MaterialSearchView) rootView.findViewById(R.id.search_view);
        searchFab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        toolbarSecondary = (Toolbar) rootView.findViewById(R.id.toolbar_secondary);
        toolbarSecondaryTitle = (TextView) rootView.findViewById(R.id.toolbar_secondary_title);
        searchContainer = (FrameLayout) rootView.findViewById(R.id.search_container);
        backWrapper = (RelativeLayout) rootView.findViewById(R.id.back_wrapper);
        back = (ImageButton) rootView.findViewById(R.id.back);
        settingsFab = (FloatingActionButton) rootView.findViewById(R.id.settings_fab);
        settingsCard = (CardView) rootView.findViewById(R.id.settings_card);
        settingsCardBg = (RelativeLayout) rootView.findViewById(R.id.settings_card_bg);
        downloadOnWifiSwitch = (SwitchCompat) rootView.findViewById(R.id.download_on_wifi_switch);
        streamOnWifiSwitch = (SwitchCompat) rootView.findViewById(R.id.stream_on_wifi_switch);
        streamOnlyAudioSwitch = (SwitchCompat) rootView.findViewById(R.id.stream_only_audio_switch);
    }

    private void setupUI(Bundle savedInstanceState) {

        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_home_white));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_view_list));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_downloaded));
        tabLayout.setOnTabSelectedListener(this);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.white));
        searchFab.setOnClickListener(this);
        searchView.setOnSearchViewListener(this);
        searchView.setOnQueryTextListener(this);
        searchView.setAdapter(searchAdapter);
        searchView.setOnItemClickListener(this);
        searchView.setHintTextColor(getResources().getColor(R.color.md_grey_500));
        searchView.setHint(getResources().getString(R.string.search_hint_oaza));
        searchView.setBackIcon(getResources().getDrawable(R.drawable.ic_search_back));
        searchContainer.setOnClickListener(this);
        back.setOnClickListener(this);
        settingsFab.setOnClickListener(this);
        settingsCardBg.setOnClickListener(this);

        if (savedInstanceState == null) {
            pager.setAdapter(new MainFragmentsAdapter(context.getSupportFragmentManager()));
            pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            pager.setOffscreenPageLimit(2);
            settingsFab.hide(false);
            downloadOnWifiSwitch.setChecked(sharedPreferences.loadDownloadOnWifi());
            streamOnlyAudioSwitch.setChecked(sharedPreferences.loadStreamAudio());
            streamOnWifiSwitch.setChecked(sharedPreferences.loadStreamOnWifi());
        }

        downloadOnWifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferences.saveDownloadOnWifi(isChecked);
            }
        });

        streamOnWifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferences.saveStreamOnWifi(isChecked);
            }
        });

        streamOnlyAudioSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferences.saveStreamAudio(isChecked);
            }
        });
    }

    public HomeFragment getHomeFragment() {
        return homeFragment;
    }

    public ArchiveFragment getArchiveFragment() {
        return archiveFragment;
    }

    public DownloadedFragment getDownloadedFragment() {
        return downloadedFragment;
    }

    public PhotoAlbumFragment getPhotoAlbumFragment() {
        return photoAlbumFragment;
    }

    public TabLayout getTabLayout() {
        return tabLayout;
    }

    public MaterialSearchView getSearchView() {
        return searchView;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public RelativeLayout getAppBar() {
        return appBar;
    }

    public FrameLayout getPhotoalbumWrapper() {
        return photoalbumWrapper;
    }

    public CardView getSettingsCard() {
        return settingsCard;
    }

    public ViewPager getPager() {
        return pager;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        pager.setCurrentItem(tab.getPosition());

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_home);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_view_list);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_downloaded);

        switch (tab.getPosition()) {
            case 0:
                tabLayout.getTabAt(0).setIcon(R.drawable.ic_home_white);
                settingsFab.hide(true);
                hideSettings();
                break;
            case 1:
                tabLayout.getTabAt(1).setIcon(R.drawable.ic_view_list_white);
                settingsFab.hide(true);
                hideSettings();
                break;
            case 2:
                tabLayout.getTabAt(2).setIcon(R.drawable.ic_downloaded_white);
                settingsFab.show(true);
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    public boolean isAlbumOpened() {
        return photoalbumWrapper.getVisibility() == View.VISIBLE;
    }

    public void openAlbum(PhotoAlbum album) {
        backWrapper.setVisibility(View.VISIBLE);
        searchFab.hide(true);
        toolbarSecondaryTitle.setText(album.getName());
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.photoalbum_wrapper, photoAlbumFragment, PhotoAlbumFragment.TAG);
        transaction.commit();
        photoAlbumFragment.setAlbum(album);

        photoalbumWrapper.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.SlideInUp).duration(300).playOn(photoalbumWrapper);
        YoYo.with(Techniques.FadeOut).duration(300).playOn(tabLayout);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tabLayout.setVisibility(View.GONE);
                pager.setVisibility(View.GONE);
            }
        }, 300);

    }

    public void closeAlbum() {
        backWrapper.setVisibility(View.GONE);
        searchFab.show(true);
        final FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.remove(getFragmentManager().findFragmentByTag(PhotoAlbumFragment.TAG));

        tabLayout.setVisibility(View.VISIBLE);
        pager.setVisibility(View.VISIBLE);

        YoYo.with(Techniques.FadeIn).duration(300).playOn(tabLayout);
        YoYo.with(Techniques.SlideOutDown).duration(300).playOn(photoalbumWrapper);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                photoalbumWrapper.setVisibility(View.GONE);
                transaction.commit();
            }
        }, 350);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                searchFab.hide(false);
                searchView.showSearch();
                break;
            case R.id.search_container:
                searchView.closeSearch();
                break;
            case R.id.back:
                closeAlbum();
                break;
            case R.id.settings_fab:
                if (settingsCard.getVisibility() == View.GONE) {
                    showSettings();
                } else {
                    hideSettings();
                }
                break;
            case R.id.settings_card_bg:
                hideSettings();
                break;
        }
    }

    public void showSettings() {
        settingsCard.setVisibility(View.VISIBLE);
        settingsCardBg.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.FadeInUp).duration(200).playOn(settingsCard);
        YoYo.with(Techniques.FadeIn).duration(200).playOn(settingsCardBg);
        settingsFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_close));
    }

    public void hideSettings() {
        YoYo.with(Techniques.FadeOutDown).duration(200).playOn(settingsCard);
        YoYo.with(Techniques.FadeOut).duration(200).playOn(settingsCard);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                settingsCard.setVisibility(View.GONE);
                settingsCardBg.setVisibility(View.GONE);
            }
        }, 200);
        settingsFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_settings));
    }

    @Override
    public void onSearchViewShown() {
        searchFab.hide(false);
        searchContainer.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.FadeIn).duration(200).playOn(searchContainer);
    }

    @Override
    public void onSearchViewClosed() {
        searchFab.show(true);
        YoYo.with(Techniques.FadeOut).duration(200).playOn(searchContainer);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                searchContainer.setVisibility(View.GONE);
            }
        }, 200);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        JsonObjectRequest search = RequestFactory.search(this, newText);
        MyRequestService.getRequestQueue().add(search);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final ArchiveItem item = searchResult.get(position);

        switch (item.getType()) {
            case ArchiveItem.Type.VIDEO:
                searchView.closeSearch();
                Handler videoHandler = new Handler();
                videoHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Video video = item.getVideo();
                        ((MainActivity) getActivity()).playNewVideo(video);
                    }
                }, 200);

                break;
            case ArchiveItem.Type.ALBUM:
                searchView.closeSearch();
                Handler albumHandler = new Handler();
                albumHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PhotoAlbum album = item.getAlbum();
                        ((MainActivity) getActivity()).getMainFragment().openAlbum(album);
                    }
                }, 200);
                break;
        }
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        super.onSuccessResponse(response, requestType);

        switch (requestType) {
            case SEARCH:
                ArrayList<ArchiveItem> searchItems = ResponseFactory.parseSearch(response);
                if (searchItems != null) {
                    SmartLog.Log(SmartLog.LogLevel.DEBUG, "searchResultSize", String.valueOf(searchItems.size()));

                    searchResult.clear();
                    searchResult.addAll(searchItems);
                    searchAdapter.notifyDataSetChanged();
                    searchView.showSuggestions();

                }
        }
    }

    private class MainFragmentsAdapter extends FragmentPagerAdapter {
        public MainFragmentsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return getHomeFragment();
                case 1:
                    return getArchiveFragment();
                case 2:
                    return getDownloadedFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
