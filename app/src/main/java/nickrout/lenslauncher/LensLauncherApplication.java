package beenalongday.lenslauncher;

import com.orm.SugarApp;

import java.util.Observable;
import java.util.Observer;

import beenalongday.lenslauncher.background.SortAppsTask;
import beenalongday.lenslauncher.background.EditedObservable;
import beenalongday.lenslauncher.background.UpdatedObservable;
import beenalongday.lenslauncher.background.UpdateAppsTask;

/**
 * Created by nicholasrout on 2016/06/12.
 */
public class LensLauncherApplication extends SugarApp implements Observer {

    @Override
    public void onCreate() {
        super.onCreate();
        UpdatedObservable.getInstance().addObserver(this);
        EditedObservable.getInstance().addObserver(this);
        updateApps();
    }

    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof UpdatedObservable) {
            updateApps();
        } else if (observable instanceof EditedObservable) {
            editApps();
        }
    }

    private void updateApps() {
        new UpdateAppsTask(
                getPackageManager(),
                getApplicationContext(),
                this)
                .execute();
    }

    private void editApps() {
        new SortAppsTask(
                getApplicationContext(),
                this)
                .execute();
    }
}
