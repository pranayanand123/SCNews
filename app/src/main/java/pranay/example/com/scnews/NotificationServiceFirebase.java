package pranay.example.com.scnews;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
import java.util.Date;
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
    NotificationTarget notificationTarget;
    private final int notifId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
    @Override
    public boolean onStartJob(JobParameters job) {
        Log.d("NOTIFICATIONJOB", "1");
        loadJSON();
        Log.d("NOTIFICATIONJOB", "2");

        return true;
    }

    private void loadJSON() {
        final ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        String country = Utils.getCountry(getApplicationContext());
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

    private void showNotification(Article article) {
        Log.d("NOTIFICATIONJOB", "3");
        Intent intent = new Intent(this, FullArticle.class);
        intent.putExtra("url", article.getUrl());
        intent.putExtra("title", article.getTitle());
        intent.putExtra("img",  article.getUrlToImage());
        intent.putExtra("date",  article.getPublishedAt());
        intent.putExtra("source",  article.getSource().getName());
        intent.putExtra("author",  article.getAuthor());
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notifId /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        String channelId = "oreo";

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

//        expandedView = new RemoteViews(getPackageName(), R.layout.notification_expanded);
//        expandedView.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
//        expandedView.setTextViewText(R.id.content_title, article.getTitle());
//        expandedView.setTextViewText(R.id.content_text, article.getDescription());
//
//
//        collapsedView = new RemoteViews(getPackageName(), R.layout.notification_collapsed);
//        collapsedView.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
//        collapsedView.setTextViewText(R.id.content_title, article.getTitle());
//        collapsedView.setTextViewText(R.id.content_text, article.getDescription());
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(),channelId)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(article.getTitle())
                        .setContentText(article.getDescription()).setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                        .setContentIntent(pendingIntent);
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(article.getDescription());
        bigText.setBigContentTitle(article.getTitle());
        Notification notification = builder.build();

//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
////                Glide.get(getApplicationContext()).clearMemory();
//
//            }
//        });


        NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    article.getTitle(),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(notifId,notification);
//        notificationTarget = new NotificationTarget(
//                getApplicationContext(),
//                R.id.rl_expanded,
//                expandedView,
//                notification,
//                0);
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
////                Glide.get(getApplicationContext()).clearMemory();
//                Glide.with(getApplicationContext())
//                        .asBitmap()
//                        .load(article.getUrlToImage())
//                        .into( notificationTarget );
//
//            }
//        });






    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return true;
    }

}
