package beenalongday.lenslauncher.ui;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import beenalongday.lenslauncher.R;
import beenalongday.lenslauncher.background.BackgroundChangedObservable;
import beenalongday.lenslauncher.background.NightModeObservable;
import beenalongday.lenslauncher.background.VisibilityChangedObservable;
import beenalongday.lenslauncher.model.App;
import beenalongday.lenslauncher.model.AppPersistent;
import beenalongday.lenslauncher.AppsSingleton;
import beenalongday.lenslauncher.background.LoadedObservable;

/**
 * Created by beenalongday on 2016/04/02.
 */
public class HomeActivity extends BaseActivity implements Observer {

    private final static String TAG = "HomeActivity";

    @BindView(R.id.lens_view_apps)
    LensView mLensView;

    @BindView(R.id.progress_home)
    MaterialProgressBar mProgress;

    private PackageManager mPackageManager;
    private ArrayList<App> mApps;
    private ArrayList<Bitmap> mAppIcons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        mPackageManager = getPackageManager();
        mLensView.setPackageManager(mPackageManager);
        mLensView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        assignApps(AppsSingleton.getInstance().getApps(), AppsSingleton.getInstance().getAppIcons());
        LoadedObservable.getInstance().addObserver(this);
        VisibilityChangedObservable.getInstance().addObserver(this);
        BackgroundChangedObservable.getInstance().addObserver(this);
        NightModeObservable.getInstance().addObserver(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupTransparentSystemBarsForLollipop();
    }

    /**
     * Sets up transparent navigation and status bars in Lollipop.
     * This method is a no-op for other platform versions.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupTransparentSystemBarsForLollipop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.getAttributes().systemUiVisibility |=
                    (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    private void setBackground() {
        mLensView.invalidate();
    }

    private void assignApps(ArrayList<App> apps, ArrayList<Bitmap> appIcons) {
        if (apps.size() == 0 || appIcons.size() == 0) {
            return;
        }
        mProgress.setVisibility(View.INVISIBLE);
        mLensView.setVisibility(View.VISIBLE);
        mApps = apps;
        mAppIcons = appIcons;
        removeHiddenApps();
        mLensView.setApps(mApps, mAppIcons);
    }

    private void removeHiddenApps() {
        for (int i = 0; i < mApps.size(); i++) {
            if (!AppPersistent.getAppVisibility(
                    mApps.get(i).getPackageName().toString(), mApps.get(i).getName().toString())) {
                mApps.remove(i);
                mAppIcons.remove(i);
                i--;
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Do Nothing
    }

    @Override
    protected void onDestroy() {
        LoadedObservable.getInstance().deleteObserver(this);
        super.onDestroy();
    }

    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof LoadedObservable || observable instanceof VisibilityChangedObservable) {
            assignApps(AppsSingleton.getInstance().getApps(), AppsSingleton.getInstance().getAppIcons());
        } else if (observable instanceof BackgroundChangedObservable) {
            setBackground();
        } else if (observable instanceof NightModeObservable) {
            updateNightMode();
        }
    }
}
