package li.ruoshi.playground;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class FloatingFragmentActivity extends FragmentActivity {

    final Handler handler = new Handler();
    Fragment floatingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        final ImageView showFloating = (ImageView) findViewById(R.id.show_floating);
        showFloating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                floatingFragment = FrontFragment.newInstance();
                fragmentTransaction.add(R.id.floating_container, floatingFragment, "floating");


                fragmentTransaction.commit();
                findViewById(R.id.floating_container).bringToFront();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideFragment();
                    }
                }, 5000);
            }
        });

        final Button hideFloating = (Button) findViewById(R.id.hide_floating);
        hideFloating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    void hideFragment() {
        if (floatingFragment == null) {
            return;
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.remove(floatingFragment);

        floatingFragment = null;
        fragmentTransaction.commit();
    }

}
