package li.ruoshi.playground;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;

import li.ruoshi.playground.view.ApptDurationBlocksAdapter;
import li.ruoshi.playground.view.CalView;

public class CalDemoActivity extends Activity {
    private static final String TAG = CalDemoActivity.class.getSimpleName();

    static float getPixels(int unit, float size) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return TypedValue.applyDimension(unit, size, metrics);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cal_demo);

        final TextView currentMonth = (TextView) findViewById(R.id.current_month);

        final DateTime now = DateTime.now();

        currentMonth.setText(String.format("%d · %d", now.getYear(), now.getMonthOfYear()));


        final ImageView folderButton = (ImageView) findViewById(R.id.folder_button);

        final CalView calView = (CalView) findViewById(R.id.cal_view);

        setupFolderCalEvent(folderButton, calView);


        initAvailableTeachers();

    }

    private void setupFolderCalEvent(final ImageView folderButton, final CalView calView) {
        folderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int minH = calView.getMinHeight();
                final int maxH = calView.getMaxHeight();

                final ViewGroup.LayoutParams lp = calView.getLayoutParams();
                Log.d(TAG, String.format("calViewHeight: %d, minH=%dpx, maxH=%dpx",
                        lp.height,
                        minH,
                        maxH));

                final boolean expand = lp.height <= (minH * 1.5f);

                final ValueAnimator anim = expand
                        ? ValueAnimator.ofInt(lp.height, maxH)
                        : ValueAnimator.ofInt(lp.height, minH);


                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        lp.height = (int) valueAnimator.getAnimatedValue();
                        calView.setLayoutParams(lp);
                    }
                });
                anim.setDuration(400);
                anim.start();

                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        anim.removeAllListeners();
                        anim.removeAllUpdateListeners();
                        folderButton.setImageResource(expand
                                ? R.drawable.icon_fewer
                                : R.drawable.icon_unfold);


                        calView.setRowCount(expand
                                ? calView.getMaxRowCount()
                                : calView.getMinRowCount());
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

            }
        });
    }

    private void initAvailableTeachers() {
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        RecyclerView apptDurationsContainer = (RecyclerView) findViewById(R.id.duration_has_available_teachers_container);
        apptDurationsContainer.setLayoutManager(layoutManager);
        apptDurationsContainer.setAdapter(new ApptDurationBlocksAdapter());
    }

    @Override
    protected void onResume() {
        super.onResume();


    }
}
