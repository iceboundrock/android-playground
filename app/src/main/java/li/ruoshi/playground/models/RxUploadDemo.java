package li.ruoshi.playground.models;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by ruoshili on 6/13/15.
 */
public class RxUploadDemo {
    private static final String TAG = RxUploadDemo.class.getSimpleName();

    // 模拟乱序
    private static final Random random = new Random();
    final Scheduler uploadScheduler;
    final SingleThreadExecutor threadExecutor;

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


    private static class SingleThreadExecutor implements Executor {

        private final AtomicBoolean stopped = new AtomicBoolean(false);
        private final Thread thread;
        private final LinkedBlockingDeque<Runnable> commandQueue = new LinkedBlockingDeque<>();

        public SingleThreadExecutor() {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Enter run()");

                    while (!stopped.get()) {
                        try {
                            final Runnable runnable = commandQueue.poll(1, TimeUnit.SECONDS);
                            if (runnable != null) {
                                Log.d(TAG, "executing a command");
                                runnable.run();
                            }
                        } catch (Throwable e) {
                            Log.w(TAG, "execute command failed.", e);
                        }
                    }

                    Log.d(TAG, "Exit run()");
                }
            });

            thread.start();
        }

        @Override
        public void execute(final Runnable command) {
            if (command == null) {
                return;
            }
            commandQueue.offer(command);
        }

        public boolean stop() {
            return stopped.compareAndSet(false, true);
        }
    }

    public RxUploadDemo() {
        threadExecutor = new SingleThreadExecutor();
        uploadScheduler = Schedulers.from(threadExecutor);
    }

    public void stop() {
        if (threadExecutor.stop()) {
            Log.d(TAG, "threadExecutor stopped.");
        }
    }

    PublishSubject<String> publishSubject = PublishSubject.create();

    public Observable<UploadTask> postUploadTask(final String path) {
        publishSubject.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {

            }
        });
        publishSubject.onNext(path);

        return Observable.create(new Observable.OnSubscribe<UploadTask>() {
            @Override
            public void call(Subscriber<? super UploadTask> subscriber) {
                subscriber.onNext(new UploadTask(path));
                subscriber.onCompleted();
            }
        }).flatMap(new Func1<UploadTask, Observable<UploadTask>>() {
            @Override
            public Observable<UploadTask> call(UploadTask uploadTask) {
                return getBS2Key(uploadTask);
            }
        }).flatMap(new Func1<UploadTask, Observable<UploadTask>>() {
            @Override
            public Observable<UploadTask> call(UploadTask uploadTask) {
                return upload(uploadTask);
            }
        }).observeOn(AndroidSchedulers.mainThread()); // 在UI线程回调
    }

    private Observable<UploadTask> getBS2Key(final UploadTask task) {
        return Observable.create(new Observable.OnSubscribe<UploadTask>() {
            @Override
            public void call(final Subscriber<? super UploadTask> subscriber) {
                Log.d(TAG, "begin getBS2Key");

                final AsyncTask<Void, Void, Void> at = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        task.bs2Key = String.valueOf(System.currentTimeMillis());
                        Log.d(TAG, "got BS2 key: " + task.getBs2Key());

                        try {
                            Thread.sleep(500 + random.nextInt(1000)); // 模拟随机的API调用时间
                        } catch (InterruptedException e) {

                        } finally {
                            subscriber.onNext(task);
                            subscriber.onCompleted();
                        }

                        return null;
                    }
                };

                at.execute();
            }
        }).subscribeOn(uploadScheduler); // 在上传线程执行获取BS2 Key，如果换成其他Scheduler，可以在其他线程执行
    }

    private Observable<UploadTask> upload(final UploadTask task) {
        return Observable.create(new Observable.OnSubscribe<UploadTask>() {
            @Override
            public void call(final Subscriber<? super UploadTask> subscriber) {
                Log.d(TAG, "begin getBS2Key");

                final AsyncTask<Void, Void, Void> at = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        task.url = "http://bs2.yy.com/" + System.currentTimeMillis();
                        Log.d(TAG, "upload to: " + task.getUrl());


                        try {
                            Thread.sleep(500 + random.nextInt(1000)); // 模拟随机的上传时间
                        } catch (InterruptedException e) {

                        } finally {
                            subscriber.onNext(task);
                            subscriber.onCompleted();
                        }
                        return null;
                    }
                };
                at.execute();
            }
        }).subscribeOn(uploadScheduler);
    }
}
