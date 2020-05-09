package dp3t.backend;


import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class RxUtil {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public static <S> void mainThreadConsumer(S object, Consumer<S> consumer) {
        mainThreadConsumer(object, consumer, null);
    }

    public static <S> void mainThreadConsumer(S object, Consumer<? super S> consumer, @Nullable Consumer<Throwable> errorConsumer) {
        Observable
                .just(object)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer, errorConsumer == null ? new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {

                    }
                } : errorConsumer);
    }

    public static void delayedConsumer(long delay, Consumer<Long> consumer) {
        delayedConsumer(delay, consumer, null);
    }

    public static void delayedConsumer(long delay, Consumer<Long> consumer, @Nullable Consumer<Throwable> errorConsumer) {
        Observable
                .timer(delay, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer, errorConsumer == null ? new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                } : errorConsumer);
    }

    public static <S> Disposable networkConsumer(Observable<S> observable, Consumer<S> consumer, @Nullable Consumer<Throwable> errorConsumer) {

        return
                observable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(consumer, errorConsumer == null ? new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        } : errorConsumer);
    }

    public static <S> Disposable networkConsumer(Observable<S> observable, Consumer<S> consumer) {
        return networkConsumer(observable, consumer, null);
    }

    public static <S> void asyncConsumer(Observable<S> observable, final Consumer<S> consumer, @Nullable Consumer<Throwable> errorConsumer) {
        observable
                .flatMap(new Function<S, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(S s) throws Exception {
                        consumer.accept(s);
                        return Observable.just(0L);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {

                    }
                }, errorConsumer == null ? new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                } : errorConsumer);
    }

    public static <S> void asyncConsumer(Observable<S> observable, Consumer<S> consumer) {
        asyncConsumer(observable, consumer, null);
    }
}
