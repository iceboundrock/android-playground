package li.ruoshi.playground.models;

import android.os.Handler;
import android.util.Log;

import java.util.Random;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by ruoshili on 6/13/15.
 */
public class RxUploadDemo {
    private static final String TAG = RxUploadDemo.class.getSimpleName();

    // 模拟乱序
    private static final Random random = new Random();

    public static class UploadTask {
        public final String path;

        private String bs2Key;

        private String url;

        public UploadTask(String path) {
            this.path = path;
        }

        public String getBs2Key() {
            return bs2Key;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public String toString() {
            return "UploadTask{" +
                    "bs2Key='" + bs2Key + '\'' +
                    ", path='" + path + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

    final Handler handler = new Handler();
    public RxUploadDemo() {
    }



    public Observable<UploadTask> postUploadTask(final String path) {
        return Observable.create(new Observable.OnSubscribe<UploadTask>() {
            @Override
            public void call(Subscriber<? super UploadTask> subscriber) {
                subscriber.onNext(new UploadTask(path));
                subscriber.onCompleted();
            }
        }) .flatMap(new Func1<UploadTask, Observable<UploadTask>>() {
            @Override
            public Observable<UploadTask> call(UploadTask uploadTask) {
                return getBS2KeyAsync(uploadTask);
            }
        }).flatMap(new Func1<UploadTask, Observable<UploadTask>>() {
            @Override
            public Observable<UploadTask> call(UploadTask uploadTask) {
                return uploadAsync(uploadTask);
            }
        });
    }

    private Observable<UploadTask> getBS2KeyAsync(final UploadTask task) {
        return Observable.create(new Observable.OnSubscribe<UploadTask>() {
            @Override
            public void call(final Subscriber<? super UploadTask> subscriber) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        task.bs2Key = String.valueOf(System.currentTimeMillis());
                        Log.d(TAG, "got BS2 key: " + task.getBs2Key());
                        subscriber.onNext(task);
                        subscriber.onCompleted();
                    }
                }, 500 + random.nextInt(1000)); // 模拟乱序
            }
        });
    }

    private Observable<UploadTask> uploadAsync(final UploadTask task) {
        return Observable.create(new Observable.OnSubscribe<UploadTask>() {
            @Override
            public void call(final Subscriber<? super UploadTask> subscriber) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        task.url = "http://bs2.yy.com/" + System.currentTimeMillis();
                        Log.d(TAG, "upload to: " + task.getUrl());

                        subscriber.onNext(task);
                        subscriber.onCompleted();
                    }
                }, 500 + random.nextInt(2000)); // 模拟乱序
            }
        });
    }
}
