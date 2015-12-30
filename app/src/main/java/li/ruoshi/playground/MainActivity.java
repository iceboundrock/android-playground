package li.ruoshi.playground;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import li.ruoshi.playground.models.HttpDns;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ActivitiesAdapter adapter = new ActivitiesAdapter(this
        );

        ListView lv = (ListView) findViewById(R.id.activities_list);
        lv.setAdapter(adapter);

        Button bt = (Button) findViewById(R.id.back_to_front);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onLongClick");
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (int i = 10; i > 0; i--) {
                                Thread.sleep(1000);
                                Log.d(TAG, "Counting down: " + i);
                            }


                        } catch (InterruptedException e) {
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Intent it = new Intent(MainActivity.this, PathViewActivity.class);
                                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getApplicationContext().startActivity(it);
                            }
                        });
                    }
                });
                t.start();
            }
        });

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpDns dns = new HttpDns();
                try {
                    List<InetAddress> inetAddresses = dns.lookup("tutor2.100.com");
                    for (InetAddress addr : inetAddresses) {
                        Log.d(TAG, "HttpDns, return addr: " + addr.getHostAddress());
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });
        t1.start();
    }


    @Override
    protected void onPause() {
        super.onPause();


    }
}
