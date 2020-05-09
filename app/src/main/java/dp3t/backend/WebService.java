package dp3t.backend;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import Jampack.Z;
import io.reactivex.Observable;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

import static androidx.constraintlayout.widget.Constraints.TAG;


public interface WebService {

    OkHttpClient client = HttpsUtil.getUnsafeOkHttpClient(new MyInterceptor());
    final String BASE_URL = "http://mdr.dev.syrexcloud.com/";

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    WebService service = retrofit.create(WebService.class);


   //?startDate=2020-05-07T00:00:00.000Z&endDate=2020-05-09T00:00:00.000Z
    @GET("pull")
    Observable<ResponseBody> getCases(@Query("startDate") String startDate,
                                      @Query("endDate") String endDate);

    class MyInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            request = request.newBuilder()
                    .build();
            long t1 = System.nanoTime();
            System.out.println(
                    String.format("Sending request %s on %n%s", request.url(), request.headers()));
            Response response = chain.proceed(request);
            Log.d(TAG, "intercept: " + response.code());
            long t2 = System.nanoTime();
            System.out.println(
                    String.format("Received response for %s in %.1fms%n%s", response.request().url(),
                            (t2 - t1) / 1e6d, response.headers()));
            Log.d(TAG, "intercept: " + response.code());
            return response;
        }
    }

}
