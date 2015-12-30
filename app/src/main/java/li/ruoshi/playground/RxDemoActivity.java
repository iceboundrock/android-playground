package li.ruoshi.playground;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.TimeUnit;

import li.ruoshi.playground.models.RxUploadDemo;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;


public class RxDemoActivity extends Activity {
    private static final String TAG = RxDemoActivity.class.getSimpleName();

    final RxUploadDemo rxUploadDemo = new RxUploadDemo();
    final SparseArray<Subscription> subscriptionArray = new SparseArray<>();
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_demo);
        Button button = (Button) findViewById(R.id.test_button);

        Observable<Long> o = rx
                .Observable
                .timer(1, TimeUnit.SECONDS)
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "doOnUnsubscribe from timer");
                    }
                });
        final Subscription subscription = o.observeOn(AndroidSchedulers.mainThread()).subscribe(
                new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        Log.d(TAG, "on timer: " + aLong);
                        throw new NullPointerException();
                    }
                }
        );
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (subscription.isUnsubscribed()) {
                    subscription.unsubscribe();
                }
                final int key = count++;
                Log.d(TAG, "onClick, count: " + key);

                final Subscription s = rxUploadDemo.postUploadTask(String.valueOf(key))
                        .subscribe(new Action1<RxUploadDemo.UploadTask>() {
                                       @Override
                                       public void call(RxUploadDemo.UploadTask uploadTask) {
                                           Log.d(TAG, "upload succeed, key: " + key + ", task: " + uploadTask);
                                       }
                                   },
                                new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        Log.d(TAG, "upload failed, key: " + key, throwable);
                                    }
                                },
                                new Action0() {
                                    @Override
                                    public void call() {
                                        Log.d(TAG, "onCompleted for key: " + key);
                                        Subscription subscription = subscriptionArray.get(key);
                                        if (subscription != null) {
                                            Log.d(TAG, "un-subscribe for key: " + key);
                                            subscription.unsubscribe();
                                            subscriptionArray.remove(key);
                                        }
                                    }
                                });

                subscriptionArray.put(key, s);


            }
        });


    }


    @Override
    public void finish() {
        super.finish();
        rxUploadDemo.stop();
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
                    if (handler != null) {
                        handler.post(runnable);
                    } else {
                        runnable.run();
                    }
                }
            });
        }
    }

}
