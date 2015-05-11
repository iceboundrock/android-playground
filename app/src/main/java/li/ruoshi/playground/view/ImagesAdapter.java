package li.ruoshi.playground.view;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import li.ruoshi.playground.R;

/**
 * Created by ruoshili on 4/13/15.
 */
public class ImagesAdapter extends PagerAdapter {
    private static final String TAG = ImagesAdapter.class.getSimpleName();

    Context context;
    ArrayList<ImageView> imageViews;
    private int[] GalImages = new int[]{
            R.drawable.splash_logo,
            R.drawable.student,
            R.drawable.teacher,
            R.drawable.face_default,
            R.drawable.camera
    };

    Queue<WeakReference<ImageView>> unusedViews = new ArrayBlockingQueue<>(3);

    public ImagesAdapter(Context context) {
        this.context = context;


    }

    @Override
    public int getCount() {
        return GalImages.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.d(TAG, "instantiateItem, pos: " + position);
        WeakReference<ImageView> cachedImgView = unusedViews.poll();
        ImageView iv = null;
        if (cachedImgView != null) {
            iv = cachedImgView.get();
        }
        if (iv == null) {
            Log.d(TAG, "instantiateItem, no cached view, pos: " + position);
            iv = new ImageView(context);
        } else {
            Log.d(TAG, "instantiateItem, use cached view, pos: " + position);
        }
        iv.setImageResource(GalImages[position]);
        container.addView(iv, 0);
        return iv;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.d(TAG, "destroyItem, pos: " + position);
        container.removeView((View) object);
        unusedViews.offer(new WeakReference<>((ImageView) object));
    }
}