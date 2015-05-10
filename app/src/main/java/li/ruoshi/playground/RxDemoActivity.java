package li.ruoshi.playground;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.List;
import java.util.concurrent.TimeUnit;

import li.ruoshi.playground.view.AnimatedButton;
import rx.functions.Action1;
import rx.subjects.PublishSubject;


public class RxDemoActivity extends ActionBarActivity {
    private static final String TAG = RxDemoActivity.class.getSimpleName();

    final PublishSubject<Integer> onClickObservable = PublishSubject.create();

    AnimatedButton animatedButton;

    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_demo);
        Button button = (Button) findViewById(R.id.test_button);

        onClickObservable.buffer(2, TimeUnit.SECONDS).subscribe(new Action1<List<Integer>>() {
            @Override
            public void call(List<Integer> integers) {
                if (integers == null || integers.size() < 1) {
                    return;
                }
                Log.d(TAG, "last count in 2 seconds: " + integers.get(integers.size() - 1));
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick, count: " + (++count));
                onClickObservable.onNext(count);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rx_demo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
