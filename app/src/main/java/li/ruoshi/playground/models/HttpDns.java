package li.ruoshi.playground.models;

import android.util.Log;

import com.squareup.okhttp.Dns;
import com.squareup.okhttp.OkHttpClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Query;
import retrofit.mime.TypedInput;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by ruoshili on 12/27/15.
 */
public class HttpDns implements Dns {
    private static final String TAG = "HttpDns";

    @Override
    public List<InetAddress> lookup(final String hostname) throws UnknownHostException {
        final List<InetAddress> ret = new ArrayList<>();
        final Func1<Throwable, List<InetAddress>> resumeFunction = new Func1<Throwable, List<InetAddress>>() {
            @Override
            public List<InetAddress> call(Throwable throwable) {
                return Collections.emptyList();
            }
        };
        final Observable<List<InetAddress>> lookUpViaSystem =
                lookupViaSystemDns(hostname)
                        .doOnError(new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Log.d(TAG, "lookupViaSystemDns doOnError", throwable);
                            }
                        }).onErrorReturn(resumeFunction)
                        .doOnSubscribe(new Action0() {
                            @Override
                            public void call() {
                                Log.d(TAG, "lookUpViaSystem doOnSubscribe");
                            }
                        })
                        .doOnNext(new Action1<List<InetAddress>>() {
                            @Override
                            public void call(List<InetAddress> inetAddresses) {
                                for (InetAddress addr : inetAddresses) {
                                    Log.d(TAG, "lookUpViaSystem, addr: " + addr.getHostAddress());
                                }
                            }
                        });
        final Observable<List<InetAddress>> lookupViaHttp =
                lookupViaHttp(hostname).onErrorReturn(resumeFunction)
                        .doOnSubscribe(new Action0() {
                            @Override
                            public void call() {
                                Log.d(TAG, "lookupViaHttp doOnSubscribe");

                            }
                        }).doOnNext(new Action1<List<InetAddress>>() {
                    @Override
                    public void call(List<InetAddress> inetAddresses) {
                        for (InetAddress addr : inetAddresses) {
                            Log.d(TAG, "lookupViaHttp, addr: " + addr.getHostAddress());
                        }
                    }
                });

        Observable.concatEager(lookupViaHttp, lookUpViaSystem)
                .first(new Func1<List<InetAddress>, Boolean>() {
                    @Override
                    public Boolean call(List<InetAddress> inetAddresses) {
                        return inetAddresses != null && !inetAddresses.isEmpty();
                    }
                })
                .subscribe(new Action1<List<InetAddress>>() {
                    @Override
                    public void call(List<InetAddress> inetAddresses) {
                        Log.d(TAG, "on next.");
                        synchronized (ret) {
                            ret.addAll(inetAddresses);
                            ret.notifyAll();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, "something wrong...", throwable);
                        synchronized (ret) {
                            ret.notifyAll();
                        }
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "on completed.");
                        synchronized (ret) {
                            ret.notifyAll();
                        }
                    }
                });

        synchronized (ret) {
            if (!ret.isEmpty()) {
                return ret;
            }
            try {
                ret.wait(5000);
            } catch (Throwable ignore) {
                Log.d(TAG, "something wrong...", ignore);
            }
            return ret;
        }
    }

    private Observable<List<InetAddress>> lookupViaHttp(String hostname) {
        OkHttpClient c = new OkHttpClient();
        c.setConnectTimeout(4, TimeUnit.SECONDS);
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(c))
                .setEndpoint("http://119.29.29.219")
                .build();
        return restAdapter.create(HttpDnsService.class)
                .lookup(hostname)
                .flatMap(new Func1<Response, Observable<List<InetAddress>>>() {
                    @Override
                    public Observable<List<InetAddress>> call(final Response response) {
                        Log.d(TAG, "http dns on response");

                        final int status = response.getStatus();
                        if (status < 200 || status >= 300) {
                            Log.w(TAG, "response status not ok, status: " + status);
                            return Observable.empty();
                        }

                        final TypedInput ti = response.getBody();
                        if (ti == null || ti.length() == 0) {
                            Log.w(TAG, "response body is empty");
                            return Observable.empty();
                        }

                        final List<InetAddress> ret = new ArrayList<>();
                        try {
                            java.util.Scanner scanner = new java.util.Scanner(ti.in()).useDelimiter(";");
                            while (scanner.hasNext()) {
                                final String addr = scanner.next();
                                ret.add(InetAddress.getByName(addr));
                            }
                        } catch (Throwable e) {
                            Log.d(TAG, "lookUpViaHttp, something wrong...", e);
                            return Observable.empty();
                        }
                        return Observable.from(Collections.singletonList(ret));
                    }
                });
    }

    private Observable<List<InetAddress>> lookupViaSystemDns(final String hostname) {
        return Observable.create(new Observable.OnSubscribe<List<InetAddress>>() {
            @Override
            public void call(Subscriber<? super List<InetAddress>> subscriber) {
                Log.d(TAG, "lookUpViaSystem, enter call");
                subscriber.onStart();
                try {
                    Log.d(TAG, "lookUpViaSystem, onNext");
                    subscriber.onNext(Dns.SYSTEM.lookup(hostname));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    Log.d(TAG, "lookUpViaSystem, something wrong...", e);
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    private interface HttpDnsService {
        @GET("/d")
        Observable<Response> lookup(@Query("dn") final String name);
    }
}
