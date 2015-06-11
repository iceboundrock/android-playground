package li.ruoshi.playground;

import android.app.Activity;
import android.os.Handler;
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
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;


public class RxDemoActivity extends Activity {
    private static final String TAG = RxDemoActivity.class.getSimpleName();

    final PublishSubject<Integer> onClickObservable = PublishSubject.create();

    AnimatedButton animatedButton;

    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_demo);
        Button button = (Button) findViewById(R.id.test_button);

        final Subscription subscription =  new Timer().schedule(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "On timer in non-UI Threads");
            }
        }, 1, TimeUnit.SECONDS);

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
                subscription.unsubscribe();

                Log.d(TAG, "onClick, count: " + (++count));
                onClickObservable.onNext(count);
            }
        });

        new Timer(new Handler()).schedule(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "On timer in UI Threads");
            }
        }, 1, TimeUnit.SECONDS);


    }


    public static class Timer {
        private final Handler handler;

        public Timer() {
            this(null);
        }

        public Timer(Handler handler) {
            this.handler = handler;
        }

        public Subscription schedule(final Runnable runnable, final int interval, final TimeUnit timeUnit) {
            return rx.Observable.timer(0, interval, timeUnit).subscribe(new Action1<Long>() {
                @Override
                public void call(Long aLong) {
                    if(handler != null) {
                        handler.post(runnable);
                    } else {
                        runnable.run();
                    }
                }
            });
        }
    }

}
