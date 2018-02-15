package com.chaemil.hgms.ui.mobile.fragment;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.ui.mobile.activity.MainActivity;
import com.chaemil.hgms.ui.mobile.adapter.MainFragmentsAdapter;
import com.chaemil.hgms.ui.mobile.adapter.SearchAdapter;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.RequestService;
import com.chaemil.hgms.utils.AdapterUtils;
import com.chaemil.hgms.utils.Constants;
import com.chaemil.hgms.utils.GAUtils;
import com.chaemil.hgms.utils.ShareUtils;
import com.chaemil.hgms.utils.SharedPrefUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.clans.fab.FloatingActionButton;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONObject;

import java.util.ArrayList;

import hotchemi.android.rate.AppRate;

/**
 * Created by chaemil on 4.12.15.
 */
public class MainFragment extends BaseFragment implements TabLayout.OnTabSelectedListener,
        View.OnClickListener, MaterialSearchView.SearchViewListener,
        MaterialSearchView.OnQueryTextListener, RequestFactoryListener, AdapterView.OnItemClickListener {

    public static final String TAG = "main_fragment";
    private TabLayout tabLayout;
    private ViewPager pager;
    private MainActivity context;
    private Toolbar toolbar;
    private RelativeLayout appBar;
    private FrameLayout photoalbumWrapper;
    private MaterialSearchView searchView;
    private FloatingActionButton searchFab;
    private TextView toolbarSecondaryTitle;
    private ArrayList<ArchiveItem> searchResult = new ArrayList<>();
    private SearchAdapter searchAdapter;
    private FrameLayout searchContainer;
    private RelativeLayout backWrapper;
    private ImageButton back;
    private FloatingActionButton settingsFab;
    private SharedPrefUtils sharedPreferences;
    private CardView settingsCard;
    private RelativeLayout settingsCardBg;
    private SwitchCompat streamOnWifiSwitch;
    private SwitchCompat streamOnlyAudioSwitch;
    private ImageView share;
    private LinearLayout logoWrapper;

    private MainFragmentsAdapter mainFragmentsAdapter;
    private TabLayout.TabLayoutOnPageChangeListener tabLayoutChangeListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = (MainActivity) getActivity();

        if (savedInstanceState == null) {
            SmartLog.d(TAG + " savedInstanceState", "null");
            searchAdapter = new SearchAdapter(getActivity(), R.layout.search_item, searchResult);
        }

        setupAppRate();

    }

    public void exit() {
        context = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.main_fragment, container, false);

        getUI(rootView);
        setupUI();

        return rootView;
    }

    private void setupAppRate() {
        AppRate.with(getActivity())
                .setInstallDays(3)
                .setLaunchTimes(6)
                .setRemindInterval(1)
                .setShowLaterButton(true)
                .monitor();
    }

    private void getUI(ViewGroup rootView) {
        tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
        appBar = (RelativeLayout) rootView.findViewById(R.id.app_bar);
        pager = (ViewPager) rootView.findViewById(R.id.pager);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        photoalbumWrapper = (FrameLayout) rootView.findViewById(R.id.photoalbum_wrapper);
        searchView = (MaterialSearchView) rootView.findViewById(R.id.search_view);
        searchFab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        toolbarSecondaryTitle = (TextView) rootView.findViewById(R.id.toolbar_secondary_title);
        searchContainer = (FrameLayout) rootView.findViewById(R.id.search_container);
        backWrapper = (RelativeLayout) rootView.findViewById(R.id.back_wrapper);
        back = (ImageButton) rootView.findViewById(R.id.back);
        settingsFab = (FloatingActionButton) rootView.findViewById(R.id.settings_fab);
        settingsCard = (CardView) rootView.findViewById(R.id.settings_card);
        settingsCardBg = (RelativeLayout) rootView.findViewById(R.id.settings_card_bg);
        streamOnWifiSwitch = (SwitchCompat) rootView.findViewById(R.id.stream_on_wifi_switch);
        streamOnlyAudioSwitch = (SwitchCompat) rootView.findViewById(R.id.stream_only_audio_switch);
        share = (ImageView) rootView.findViewById(R.id.share);
        logoWrapper = (LinearLayout) rootView.findViewById(R.id.logo_wrapper);
    }

    private void setupUI() {
        setupTabLayout();
        setupSearch();

        back.setOnClickListener(this);
        settingsFab.setOnClickListener(this);
        settingsCardBg.setOnClickListener(this);
        share.setOnClickListener(this);

        sharedPreferences = SharedPrefUtils.getInstance(getActivity());

        if (mainFragmentsAdapter == null) {
            mainFragmentsAdapter = new MainFragmentsAdapter(this, context.getFragmentManager());
        }
        if (tabLayoutChangeListener == null) {
            tabLayoutChangeListener = new TabLayout.TabLayoutOnPageChangeListener(tabLayout);
        }
        pager.setAdapter(mainFragmentsAdapter);
        pager.addOnPageChangeListener(tabLayoutChangeListener);
        pager.setOffscreenPageLimit(4);
        settingsFab.hide(false);
        streamOnlyAudioSwitch.setChecked(sharedPreferences.loadStreamAudio());
        streamOnWifiSwitch.setChecked(sharedPreferences.loadStreamOnWifi());

        streamOnWifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (sharedPreferences != null) {
                    sharedPreferences.saveStreamOnWifi(isChecked);
                }
            }
        });

        streamOnlyAudioSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (sharedPreferences != null) {
                    sharedPreferences.saveStreamAudio(isChecked);
                }
            }
        });
    }

    private void setupSearch() {
        searchFab.setOnClickListener(this);
        searchView.setOnSearchViewListener(this);
        searchView.setOnQueryTextListener(this);
        searchView.setAdapter(searchAdapter);
        searchView.setOnItemClickListener(this);
        searchView.setHintTextColor(getResources().getColor(R.color.md_grey_500));
        searchView.setHint(getResources().getString(R.string.search_hint_oaza));
        searchView.setBackIcon(getResources().getDrawable(R.drawable.ic_search_back));
        searchContainer.setOnClickListener(this);
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setIcon(R.color.transparent));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_categories));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_view_list));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_songs_dark));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_downloaded));
        tabLayout.setOnTabSelectedListener(this);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.white));
    }

    public HomeFragment getHomeFragment() {
        return context.getHomeFragment();
    }

    public CategoriesFragment getCategoriesFragment() {
        return context.getCategoriesFragment();
    }

    public ArchiveFragment getArchiveFragment() {
        return context.getArchiveFragment();
    }

    public DownloadedFragment getDownloadedFragment() {
        return context.getDownloadedFragment();
    }

    public PhotoAlbumFragment getPhotoAlbumFragment() {
        return context.getPhotoAlbumFragment();
    }

    public SongsFragment getSongsFragment() {
        return context.getSongsFragment();
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

        tabLayout.getTabAt(1).setIcon(R.drawable.ic_categories);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_view_list);
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_songs_dark);
        tabLayout.getTabAt(4).setIcon(R.drawable.ic_downloaded);

        switch (tab.getPosition()) {
            case 0:
                settingsFab.hide(true);
                searchFab.show(true);
                hideSettings();

                AnalyticsService.getInstance().setPage(AnalyticsService.Pages.HOME_FRAGMENT);
                GAUtils.sendGAScreen(
                        ((OazaApp) getActivity().getApplication()),
                        "Home");
                break;
            case 1:
                tabLayout.getTabAt(1).setIcon(R.drawable.ic_categories_white);
                settingsFab.hide(true);
                searchFab.show(true);
                hideSettings();

                AnalyticsService.getInstance().setPage(AnalyticsService.Pages.CATEGORIES_FRAGMENT);
                GAUtils.sendGAScreen(
                        ((OazaApp) getActivity().getApplication()),
                        "Categories");
                break;
            case 2:
                tabLayout.getTabAt(2).setIcon(R.drawable.ic_view_list_white);
                settingsFab.hide(true);
                searchFab.show(true);
                hideSettings();

                AnalyticsService.getInstance().setPage(AnalyticsService.Pages.ARCHIVE_FRAGMENT);
                GAUtils.sendGAScreen(
                        ((OazaApp) getActivity().getApplication()),
                        "Archive");
                break;
            case 3:
                tabLayout.getTabAt(3).setIcon(R.drawable.ic_songs_white);
                settingsFab.hide(true);
                searchFab.hide(true);
                hideSettings();

                AnalyticsService.getInstance().setPage(AnalyticsService.Pages.SONGS_FRAGMENT);
                GAUtils.sendGAScreen(
                        ((OazaApp) getActivity().getApplication()),
                        "Songs");
                break;
            case 4:
                tabLayout.getTabAt(4).setIcon(R.drawable.ic_downloaded_white);
                settingsFab.show(true);
                searchFab.hide(true);

                AnalyticsService.getInstance().setPage(AnalyticsService.Pages.DOWNLOADED_FRAGMENT);
                GAUtils.sendGAScreen(
                        (OazaApp) getActivity().getApplication(),
                        "Downloaded");
                break;
        }
    }

    public void goToDownloaded() {
        pager.setCurrentItem(4);
        hideSettings();
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
        searchFab.hide(true);
        searchView.closeSearch();
        toolbarSecondaryTitle.setText(album.getName());
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.photoalbum_wrapper, getPhotoAlbumFragment(), PhotoAlbumFragment.TAG);
        transaction.commit();
        getPhotoAlbumFragment().setAlbum(album);

        backWrapper.setVisibility(View.VISIBLE);
        share.setVisibility(View.VISIBLE);
        photoalbumWrapper.setVisibility(View.VISIBLE);

        YoYo.with(Techniques.FadeIn).duration(300).playOn(backWrapper);
        YoYo.with(Techniques.FadeIn).duration(300).playOn(share);
        YoYo.with(Techniques.SlideInUp).duration(300).playOn(photoalbumWrapper);
        YoYo.with(Techniques.FadeOut).duration(300).playOn(tabLayout);
        YoYo.with(Techniques.FadeOut).duration(300).playOn(logoWrapper);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tabLayout.setVisibility(View.GONE);
                pager.setVisibility(View.GONE);
                logoWrapper.setVisibility(View.GONE);
            }
        }, 300);
    }

    public void closeAlbum() {
        searchFab.show(true);
        final FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.remove(getFragmentManager().findFragmentByTag(PhotoAlbumFragment.TAG));

        tabLayout.setVisibility(View.VISIBLE);
        pager.setVisibility(View.VISIBLE);
        logoWrapper.setVisibility(View.VISIBLE);

        YoYo.with(Techniques.FadeOut).duration(300).playOn(backWrapper);
        YoYo.with(Techniques.FadeOut).duration(300).playOn(share);
        YoYo.with(Techniques.FadeIn).duration(300).playOn(tabLayout);
        YoYo.with(Techniques.SlideOutDown).duration(300).playOn(photoalbumWrapper);
        YoYo.with(Techniques.FadeIn).duration(300).playOn(logoWrapper);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                backWrapper.setVisibility(View.GONE);
                share.setVisibility(View.GONE);

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
                if (isAlbumOpened()) {
                    closeAlbum();
                }
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
            case R.id.share:
                if (isAlbumOpened()) {
                    PhotoAlbum album = getPhotoAlbumFragment().getAlbum();
                    ShareUtils.shareLink(getActivity(),
                            Constants.ALBUM_LINK + album.getHash(),
                            album.getNameCS() + " | " + album.getNameEN(),
                            getString(R.string.share_album));
                }
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
        RequestService.getRequestQueue().add(search);
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
                        if (((MainActivity) getActivity()).isSomethingPlaying()) {
                            AdapterUtils.contextDialog(context, video, false);
                        } else {
                            ((MainActivity) getActivity()).playNewVideo(video);
                        }
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
                    SmartLog.d("searchResultSize", String.valueOf(searchItems.size()));

                    searchResult.clear();
                    searchResult.addAll(searchItems);
                    if (searchAdapter != null) {
                        searchAdapter.notifyDataSetChanged();
                    }
                    searchView.showSuggestions();

                }
        }
    }
}
