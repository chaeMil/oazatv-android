package com.chaemil.hgms.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.View;
import android.widget.RelativeLayout;

import com.android.volley.VolleyError;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.BaseActivity;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.utils.DimensUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.orm.SugarContext;

import org.json.JSONObject;

/**
 * Created by chaemil on 18.12.15.
 */
public class BaseFragment extends Fragment implements RequestFactoryListener {

    private FragmentManager fragmentManager;
    public RelativeLayout progress;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SugarContext.init(getActivity());
    }

    public void showProgress() {
        showProgress(0, 0);
    }

    public void showProgress(int delay, int duration) {
        if (progress != null) {
            delay(new Runnable() {
                @Override
                public void run() {
                    progress.setVisibility(View.VISIBLE);
                }
            }, delay);
            if (delay != 0) {
                YoYo.with(Techniques.FadeIn).delay(delay).duration(duration).playOn(progress);
            } else {
                YoYo.with(Techniques.FadeIn).playOn(progress);
            }
        }
    }

    public void hideProgress() {
        hideProgress(0);
    }

    public void hideProgress(int delay) {
        if (progress != null) {
            if (delay != 0) {
                YoYo.with(Techniques.FadeOut).delay(delay).duration(350).playOn(progress);
                delay(new Runnable() {
                    @Override
                    public void run() {
                        progress.setVisibility(View.GONE);
                    }
                }, 350 + delay);
            } else {
                YoYo.with(Techniques.FadeOut).playOn(progress);
                progress.setVisibility(View.GONE);
            }

        }
    }

    public void goBack() {
        fragmentManager = getActivity().getFragmentManager();
        fragmentManager.popBackStack();
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        //SmartLog.Log(SmartLog.LogLevel.DEBUG, "response", String.valueOf(response));
    }

    public void delay(Runnable runnable, int time) {
        final Handler handler = new Handler();
        handler.postDelayed(runnable, time);
    }

    public static boolean isTablet(Context context) {
        boolean isTablet = (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "isTablet", String.valueOf(isTablet));
        return isTablet;
    }

    public int calculateColumns() {
        int width = (int) DimensUtils.dpFromPx(getActivity(), getResources().getDimension(R.dimen.column_width));
        if (width > 580) {
            return 2;
        } else {
            return 1;
        }
    }

    public static int getScreenOrientation(Activity activity) {
        Display getOrient = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();

        getOrient.getSize(size);

        int orientation;
        if (size.x < size.y) {
            orientation = Configuration.ORIENTATION_PORTRAIT;
        } else {
            orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        return orientation;
    }

    @Override
    public void onErrorResponse(VolleyError exception, RequestType requestType) {
        SmartLog.Log(SmartLog.LogLevel.ERROR,
                "jsonResponse",
                String.valueOf(BaseActivity.responseError(exception, getActivity())));

        JSONObject jsonError = BaseActivity.responseError(exception, getActivity());

        SmartLog.Log(SmartLog.LogLevel.ERROR, "jsonError", String.valueOf(jsonError));
    }
}
