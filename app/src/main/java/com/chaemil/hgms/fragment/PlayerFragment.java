package com.chaemil.hgms.fragment;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.utils.BitmapUtils;
import com.chaemil.hgms.utils.SmartLog;

/**
 * Created by chaemil on 2.12.15.
 */
public class PlayerFragment extends Fragment {

    public static final String TAG = "player_fragment";
    private static final String IMAGES_ALREADY_BLURRED = "images_already_blurred";
    private static final String MINI_PLAYER_DRAWABLE = "mini_player_drawable";
    private static final String BG_DRAWABLE = "bg_drawable";
    private ImageView playerBg;
    private RelativeLayout miniPlayer;
    private ImageView miniPlayerImageView;
    private TextView playerText;
    private Toolbar playerToolbar;
    private boolean imagesAlreadyBlurred = false;
    private BitmapDrawable miniPlayerDrawable;
    private BitmapDrawable bgDrawable;
    private TextView miniPlayerText;
    private TextView playerTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.player_fragment, container, false);

        if (savedInstanceState != null) {
            imagesAlreadyBlurred = savedInstanceState.getBoolean(IMAGES_ALREADY_BLURRED);

            miniPlayerDrawable = new BitmapDrawable(getResources(),
                    (Bitmap) savedInstanceState.getParcelable(MINI_PLAYER_DRAWABLE));

            bgDrawable = new BitmapDrawable(getResources(),
                    (Bitmap) savedInstanceState.getParcelable(BG_DRAWABLE));
        }

        getUI(rootView);
        setupUI();
        adjustLayout();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IMAGES_ALREADY_BLURRED, imagesAlreadyBlurred);
        outState.putParcelable(MINI_PLAYER_DRAWABLE, BitmapUtils.drawableToBitmap(miniPlayerDrawable));
        outState.putParcelable(BG_DRAWABLE, BitmapUtils.drawableToBitmap(bgDrawable));
    }

    private void getUI(ViewGroup rootView) {
        miniPlayer = (RelativeLayout) rootView.findViewById(R.id.mini_player);
        playerBg = (ImageView) rootView.findViewById(R.id.player_bg);
        miniPlayerImageView = (ImageView) rootView.findViewById(R.id.mini_player_image);
        miniPlayerText = (TextView) rootView.findViewById(R.id.mini_player_text);
        playerText = (TextView) rootView.findViewById(R.id.player_text);
        playerToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        playerTitle = (TextView) rootView.findViewById(R.id.player_title);

    }

    private void setupUI() {
        resizeAndBlurBg();
    }

    private void adjustLayout() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            playerText.setVisibility(View.GONE);
        }
        else {
            playerText.setVisibility(View.VISIBLE);
        }
    }

    public void switchMiniPlayer(boolean show) {
        if (show) {
            playerToolbar.setVisibility(View.GONE);
            miniPlayer.setVisibility(View.VISIBLE);
        } else {
            playerToolbar.setVisibility(View.VISIBLE);
            miniPlayer.setVisibility(View.GONE);
        }
    }

    private void resizeAndBlurBg() {
        new ComputeImage().execute(null);
    }

    public Toolbar getPlayerToolbar() {
        return playerToolbar;
    }

    public RelativeLayout getMiniPlayer() {
        return miniPlayer;
    }


    private class ComputeImage extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {

            if (!imagesAlreadyBlurred && miniPlayerDrawable == null || playerBg == null) {
                SmartLog.Log(SmartLog.LogLevel.DEBUG, "resizeAndBlurBg", "blurring bg image");
                Bitmap originalBitmap = BitmapUtils.drawableToBitmap(getResources().getDrawable(R.drawable.placeholder));
                Bitmap blurredPlayerBitmap = BitmapUtils.blur(getContext(), originalBitmap, 25);
                Bitmap resizedBitmap = BitmapUtils.resizeImageForImageView(blurredPlayerBitmap, 255);
                miniPlayerDrawable = new BitmapDrawable(getResources(), BitmapUtils.resizeImageForImageView(originalBitmap, 255));
                bgDrawable = new BitmapDrawable(getResources(), resizedBitmap);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            playerBg.setImageDrawable(bgDrawable);
            miniPlayerImageView.setImageDrawable(miniPlayerDrawable);

            imagesAlreadyBlurred = true;
        }
    }
}
