package li.ruoshi.playground;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import li.ruoshi.playground.view.AnimatedButton;
import rx.functions.Action1;
import rx.subjects.PublishSubject;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ActivitiesAdapter adapter = new ActivitiesAdapter(this,
                R.layout.activity_item,
                R.id.lanuch_activity_button);

        ListView lv = (ListView)findViewById(R.id.activities_list);
        lv.setAdapter(adapter);
    }
}
