package li.ruoshi.playground;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import li.ruoshi.playground.view.AnimatedButton;
import li.ruoshi.playground.view.DotProgressBar;
import rx.functions.Action1;
import rx.subjects.PublishSubject;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    Handler handler;

    final PublishSubject<Integer> onClickObservable = PublishSubject.create();

    AnimatedButton animatedButton;

    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        setContentView(R.layout.activity_main);
        animatedButton = (AnimatedButton) findViewById(R.id.animated_button);

        Button button = (Button) findViewById(R.id.test_button);
//        button.setButtonSrcResId(R.drawable.cam_button_teacher);

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



}
