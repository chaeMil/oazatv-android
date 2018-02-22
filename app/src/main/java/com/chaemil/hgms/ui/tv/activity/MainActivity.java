package com.chaemil.hgms.ui.tv.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.chaemil.hgms.R;
import com.chaemil.hgms.ui.tv.fragment.MainFragment;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_main);
        addFragment(MainFragment.newInstance());
    }

    public void addFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.tv_frame_content, fragment).addToBackStack(fragment.getTag());
        fragmentTransaction.commit();
    }

    public void goBack() {
        if (getFragmentManager().getBackStackEntryCount() > 0)
            getFragmentManager().popBackStackImmediate();
        else super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        goBack();
    }
}
