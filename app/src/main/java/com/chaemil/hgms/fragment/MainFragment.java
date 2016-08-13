package com.chaemil.hgms.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.MainFragmentsAdapter;
import com.chaemil.hgms.adapter.SearchAdapter;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.RequestService;
import com.chaemil.hgms.utils.Constants;
import com.chaemil.hgms.utils.ShareUtils;
import com.chaemil.hgms.utils.SharedPrefUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.clans.fab.FloatingActionButton;
import com.github.ybq.android.spinkit.SpinKitView;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONObject;

import java.util.ArrayList;

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
    private RelativeLayout splash;
    private CategoriesFragment categoriesFragment;
    private LinearLayout logoWrapper;
    private TextView continueWithoutData;
    private SpinKitView loadingView;

    private MainFragmentsAdapter mainFragmentsAdapter;
    private TabLayout.TabLayoutOnPageChangeListener tabLayoutChangeListener;
    private boolean init;

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
            categoriesFragment = new CategoriesFragment();
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
        setupUI();

        return rootView;
    }


    public void hideSplash(boolean animate) {
        if (splash != null) {
            if (animate) {
                YoYo.with(Techniques.FadeOut).duration(350).playOn(splash);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        splash.setVisibility(View.GONE);
                    }
                }, 350);
            } else {
                splash.setVisibility(View.GONE);
            }
            init = true;
        }
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
        splash = (RelativeLayout) rootView.findViewById(R.id.splash);
        logoWrapper = (LinearLayout) rootView.findViewById(R.id.logo_wrapper);
        continueWithoutData = (TextView) rootView.findViewById(R.id.continue_without_data);
        loadingView = (SpinKitView) rootView.findViewById(R.id.loading_view);
    }

    private void setupUI() {
        setupTabLayout();
        setupSearch();

        splash.setPadding(0, ((MainActivity) getActivity()).getStatusBarHeight(), 0, 0);
        back.setOnClickListener(this);
        settingsFab.setOnClickListener(this);
        settingsCardBg.setOnClickListener(this);
        share.setOnClickListener(this);
        continueWithoutData.setOnClickListener(this);

        if (mainFragmentsAdapter == null) {
            mainFragmentsAdapter = new MainFragmentsAdapter(this, context.getSupportFragmentManager());
        }
        if (tabLayoutChangeListener == null) {
            tabLayoutChangeListener = new TabLayout.TabLayoutOnPageChangeListener(tabLayout);
        }
        pager.setAdapter(mainFragmentsAdapter);
        pager.addOnPageChangeListener(tabLayoutChangeListener);
        pager.setOffscreenPageLimit(3);
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

        if (init) {
            hideSplash(false);
        }
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
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_downloaded));
        tabLayout.setOnTabSelectedListener(this);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.white));
    }

    public HomeFragment getHomeFragment() {
        return homeFragment;
    }

    public CategoriesFragment getCategoriesFragment() {
        return categoriesFragment;
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

    public MainFragmentsAdapter getMainFragmentsAdapter() {
        return mainFragmentsAdapter;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        pager.setCurrentItem(tab.getPosition());

        tabLayout.getTabAt(1).setIcon(R.drawable.ic_categories);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_view_list);
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_downloaded);

        switch (tab.getPosition()) {
            case 0:
                settingsFab.hide(true);
                hideSettings();
                break;
            case 1:
                tabLayout.getTabAt(1).setIcon(R.drawable.ic_categories_white);
                settingsFab.hide(true);
                hideSettings();
                break;
            case 2:
                tabLayout.getTabAt(2).setIcon(R.drawable.ic_view_list_white);
                settingsFab.hide(true);
                hideSettings();
                break;
            case 3:
                tabLayout.getTabAt(3).setIcon(R.drawable.ic_downloaded_white);
                settingsFab.show(true);
                break;
        }
    }

    public void goToDownloaded() {
        pager.setCurrentItem(3);
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
        backWrapper.setVisibility(View.GONE);
        searchFab.show(true);
        final FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.remove(getFragmentManager().findFragmentByTag(PhotoAlbumFragment.TAG));

        tabLayout.setVisibility(View.VISIBLE);
        pager.setVisibility(View.VISIBLE);
        logoWrapper.setVisibility(View.VISIBLE);

        YoYo.with(Techniques.FadeIn).duration(300).playOn(tabLayout);
        YoYo.with(Techniques.SlideOutDown).duration(300).playOn(photoalbumWrapper);
        YoYo.with(Techniques.FadeIn).duration(300).playOn(logoWrapper);

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
            case R.id.continue_without_data:
                hideSplash(true);
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

    public void showContinue() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingView.setVisibility(View.VISIBLE);
                continueWithoutData.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeIn).duration(500).playOn(continueWithoutData);
                YoYo.with(Techniques.FadeIn).duration(500).playOn(loadingView);
            }
        }, 750);
    }


}
