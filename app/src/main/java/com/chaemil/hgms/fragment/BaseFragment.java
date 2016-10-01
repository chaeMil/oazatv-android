package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.android.volley.VolleyError;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.BaseActivity;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.model.RequestType;
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
        fragmentManager = getActivity().getSupportFragmentManager();
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


    @Override
    public void onErrorResponse(VolleyError exception, RequestType requestType) {
        SmartLog.Log(SmartLog.LogLevel.ERROR,
                "jsonResponse",
                String.valueOf(BaseActivity.responseError(exception, getActivity())));

        JSONObject jsonError = BaseActivity.responseError(exception, getActivity());

        SmartLog.Log(SmartLog.LogLevel.ERROR, "jsonError", String.valueOf(jsonError));
    }
}
