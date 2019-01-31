package pranay.example.com.scnews;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;

import android.text.format.DateUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import pranay.example.com.scnews.Models.Article;
import pranay.example.com.scnews.Models.News;
import pranay.example.com.scnews.RetrofitApi.ApiClient;
import pranay.example.com.scnews.RetrofitApi.ApiInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static pranay.example.com.scnews.MainActivity.API_KEY;

public class NotificationServiceFirebase extends JobService {
    private List<Article> articles = new ArrayList<>();
    Bitmap image;
    RemoteViews expandedView;
    RemoteViews collapsedView;
    @Override
    public boolean onStartJob(JobParameters job) {
        Log.d("NOTIFICATIONJOB", "1");
        loadJSON();
        Log.d("NOTIFICATIONJOB", "2");

        return true;
    }

    private void loadJSON() {
        final ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        String country = Utils.getCountry();
        Log.d("NOTIFICATION", country);
        Call<News> call = apiInterface.getNews(country, API_KEY);
        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful() && response.body().getArticle() != null) {

                    if (!articles.isEmpty()) {
                        articles.clear();
                    }

                    articles = response.body().getArticle();
                    showNotification(articles.get(0));





                } else {
//                    Log.d("NEWSARTICLE", String.valueOf(tinydb.getListObject("news",Article.class)));

                    String errorCode;
                    switch (response.code()) {
                        case 404:
                            errorCode = "404 not found";
                            break;
                        case 500:
                            errorCode = "500 server broken";
                            break;
                        default:
                            errorCode = "unknown error";
                            break;
                    }


                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {


            }
        });

    }

    private void showNotification(final Article article) {
        Log.d("NOTIFICATIONJOB", "3");

        expandedView = new RemoteViews(getPackageName(), R.layout.notification_expanded);
        expandedView.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
        expandedView.setTextViewText(R.id.content_title, article.getTitle());
        expandedView.setTextViewText(R.id.content_text, article.getDescription());


        collapsedView = new RemoteViews(getPackageName(), R.layout.notification_collapsed);
        collapsedView.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
        collapsedView.setTextViewText(R.id.content_title, article.getTitle());
        collapsedView.setTextViewText(R.id.content_text, article.getDescription());
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(article.getTitle())
                        .setContentText(article.getDescription()).setAutoCancel(true)
                        .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), 0))
                        .setCustomContentView(collapsedView)
                        .setCustomBigContentView(expandedView)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle());
        Notification notification = builder.build();
        final NotificationTarget notificationTarget = new NotificationTarget(
                getApplicationContext(),
                R.id.rl_expanded,
                expandedView,
                notification,
                0);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Glide.get(getApplicationContext()).clearMemory();
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(article.getUrlToImage())
                        .into( notificationTarget );
            }
        });


        NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0,notification);





    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return true;
    }

}
