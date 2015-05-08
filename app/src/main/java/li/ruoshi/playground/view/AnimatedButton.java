package li.ruoshi.playground.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import li.ruoshi.playground.R;

/**
 * Created by ruoshili on 4/3/15.
 */
public class AnimatedButton extends RelativeLayout {

    private ImageButton button;
    private View halo;
    private boolean isButtonUp;
    private boolean isZoomOutEnd;
    private int buttonDownAnimationResId;
    private int buttonUpAnimationResId;

    private int buttonSrcResId;

    public int getButtonSrcResId() {
        return buttonSrcResId;
    }

    public void setButtonSrcResId(final int src) {
        if(this.buttonSrcResId == src){
            return;
        }
        this.buttonSrcResId = src;
        final Drawable buttonDrawable = getResources().getDrawable(buttonSrcResId);
        if (buttonDrawable != null) {
            final int width = buttonDrawable.getIntrinsicWidth();
            final int height = buttonDrawable.getIntrinsicHeight();
            final ViewGroup.LayoutParams lp = halo.getLayoutParams();

            if(lp.height != height || lp.width != width){
                lp.height = height;
                lp.width = width;
                halo.setLayoutParams(lp);
            }

            button.setBackgroundDrawable(buttonDrawable);
        }
    }

    private void initControls() {
        final LayoutInflater inflater = (LayoutInflater)
                getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.animated_button_layout, this);
        button = (ImageButton) findViewById(R.id.button);
        halo = findViewById(R.id.halo);
    }

    private void startButtonUpAnimation() {
        if (buttonUpAnimationResId == 0) {
            return;
        }
        final Animation buttonUpAnimation = AnimationUtils.loadAnimation(this.getContext(), buttonUpAnimationResId);
        if (isButtonUp && isZoomOutEnd)
            halo.startAnimation(buttonUpAnimation);
    }

    private void startButtonDownAnimation() {
        if (buttonDownAnimationResId == 0) {
            return;
        }

        final Animation buttonDownAnimation =
                AnimationUtils.loadAnimation(this.getContext(),
                        buttonDownAnimationResId);

        isButtonUp = false;
        isZoomOutEnd = false;
        halo.startAnimation(buttonDownAnimation);
        buttonDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isZoomOutEnd = true;
                startButtonUpAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void setupUIEvents() {

        button.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int action = event.getActionMasked();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        startButtonDownAnimation();
                        break;
                    case MotionEvent.ACTION_UP:
                        isButtonUp = true;
                        startButtonUpAnimation();
                        break;
                }

                return false;
            }
        });
    }

    public AnimatedButton(Context context) {
        super(context);
        initControls();
    }

    public AnimatedButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimatedButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initControls();
        initCustomAttrs(attrs, defStyleAttr, 0);
        setupUIEvents();
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        //button.setOnClickListener(l);
    }

    private void initCustomAttrs(AttributeSet attrs, int defStyle, int defStyleRes) {
        final TypedArray a = getContext()
                .obtainStyledAttributes(attrs,
                        R.styleable.AnimatedButton,
                        defStyle,
                        defStyleRes);

        final int btnSrc = a.getResourceId(R.styleable.AnimatedButton_buttonSrc, 0);
        final int haloColor = a.getColor(R.styleable.AnimatedButton_haloColor, 0);
        buttonUpAnimationResId = a.getResourceId(R.styleable.AnimatedButton_buttonUpAnimation, 0);
        buttonDownAnimationResId = a.getResourceId(R.styleable.AnimatedButton_buttonDownAnimation, 0);
        a.recycle();

        final GradientDrawable haloDrawable = new GradientDrawable();
        haloDrawable.setShape(GradientDrawable.OVAL);
        haloDrawable.setColor(haloColor);

        halo.setBackgroundDrawable(haloDrawable);

        setButtonSrcResId(btnSrc);
    }


}
