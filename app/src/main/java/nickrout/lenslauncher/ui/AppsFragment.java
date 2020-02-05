package beenalongday.lenslauncher.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import beenalongday.lenslauncher.R;
import beenalongday.lenslauncher.adapter.AppRecyclerAdapter;
import beenalongday.lenslauncher.model.App;
import beenalongday.lenslauncher.util.AppSorter;
import beenalongday.lenslauncher.AppsSingleton;
import beenalongday.lenslauncher.background.BroadcastReceivers;
import beenalongday.lenslauncher.util.Settings;

/**
 * Created by nicholasrout on 2016/06/08.
 */
public class AppsFragment extends Fragment implements SettingsActivity.AppsInterface {

    private static final String TAG = "AppsFragment";

    @BindView(R.id.recycler_apps)
    RecyclerView mRecycler;

    @BindView(R.id.progress_apps)
    MaterialProgressBar mProgress;

    private Settings mSettings;
    private AppRecyclerAdapter mAppRecyclerAdapter;
    private int mScrolledItemIndex;

    public AppsFragment() {
    }

    public static AppsFragment newInstance() {
        AppsFragment appsFragment = new AppsFragment();
        // Include potential bundle extras here
        return appsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apps, container, false);
        ButterKnife.bind(this, view);
        mSettings = new Settings(getActivity());
        setupRecycler(AppsSingleton.getInstance().getApps());
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() != null && getActivity() instanceof SettingsActivity) {
            ((SettingsActivity) getActivity()).setAppsInterface(this);
        }
    }

    private void sendEditAppsBroadcast() {
        if (getActivity() == null) {
            return;
        }
        Intent editAppsIntent = new Intent(getActivity(), BroadcastReceivers.AppsEditedReceiver.class);
        getActivity().sendBroadcast(editAppsIntent);
    }

    private void setupRecycler(ArrayList<App> apps) {
        if (getActivity() == null || apps.size() == 0) {
            return;
        }
        if (mRecycler.getLayoutManager() != null) {
            mScrolledItemIndex = ((LinearLayoutManager) mRecycler.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }
        mProgress.setVisibility(View.INVISIBLE);
        mRecycler.setVisibility(View.VISIBLE);
        mAppRecyclerAdapter = new AppRecyclerAdapter(getActivity(), apps);
        mRecycler.setAdapter(mAppRecyclerAdapter);
        mRecycler.setLayoutManager(new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.columns_apps)));
        mRecycler.setItemAnimator(new DefaultItemAnimator());
        mRecycler.scrollToPosition(mScrolledItemIndex);
        mScrolledItemIndex = 0;
    }

    @Override
    public void onDefaultsReset() {
        if (mSettings.getSortType() != AppSorter.SortType.values()[Settings.DEFAULT_SORT_TYPE]) {
            mSettings.save(Settings.KEY_SORT_TYPE, Settings.DEFAULT_SORT_TYPE);
            sendEditAppsBroadcast();
        }
    }

    @Override
    public void onAppsUpdated(ArrayList<App> apps) {
        setupRecycler(apps);
    }
}
