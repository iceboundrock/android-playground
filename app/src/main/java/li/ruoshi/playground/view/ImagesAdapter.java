package li.ruoshi.playground.view;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import li.ruoshi.playground.R;

/**
 * Created by ruoshili on 4/13/15.
 */
public class ImagesAdapter extends PagerAdapter {
    Context context;
    ArrayList<ImageView> imageViews;
    private int[] GalImages = new int[]{
            R.drawable.splash_logo,
            R.drawable.student,
            R.drawable.teacher,
            R.drawable.face_default,
            R.drawable.camera
    };

    Queue<ImageView> unusedViews = new ArrayBlockingQueue<ImageView>(3);

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
        Log.d("ImagesAdapter", "instantiateItem, pos: " + position);
        ImageView iv = imageViews.get(position);
        container.addView(iv, 0);
        return iv;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.d("ImagesAdapter", "destroyItem, pos: " + position);
        container.removeView((View) object);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Log.d("ImagesAdapter", "setPrimaryItem, pos: " + position);
        super.setPrimaryItem(container, position, object);
    }
}