package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.adapter.ArchiveAdapter;
import com.chaemil.hgms.R;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.service.MyRequestService;

import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by chaemil on 2.12.15.
 */
public class ArchiveFragment extends BaseFragment {

    private ArrayList<ArchiveItem> archive = new ArrayList<>();
    private RecyclerView archiveRecyclerView;
    private ProgressBar progress;
    private ArchiveAdapter archiveAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        getData(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.archive_fragment, container, false);


        getUI(rootView);
        setupUI();

        return rootView;

    }

    private void getData(Bundle savedInstanceState) {
        if (archive.size() == 0 || savedInstanceState == null) {
            JsonObjectRequest getArchive = RequestFactory.getArchive(this, 0);
            MyRequestService.getRequestQueue().add(getArchive);
        }
    }

    private void setupUI() {
        archiveAdapter = new ArchiveAdapter(getContext(), archive);
        archiveRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        archiveRecyclerView.setAdapter(archiveAdapter);

    }

    private void getUI(ViewGroup rootView) {
        archiveRecyclerView = (RecyclerView) rootView.findViewById(R.id.archive_recycler_view);
        progress = (ProgressBar) rootView.findViewById(R.id.progress);
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        super.onSuccessResponse(response, requestType);

        switch (requestType) {
            case GET_ARCHIVE:

                ArrayList<ArchiveItem> newItems;
                newItems = ResponseFactory.parseArchive(response);

                if (newItems != null) {
                    archive.addAll(newItems);
                    archiveAdapter.notifyDataSetChanged();
                }

                break;

        }
    }

    @Override
    public void onErrorResponse(VolleyError exception) {
        super.onErrorResponse(exception);
    }
}
