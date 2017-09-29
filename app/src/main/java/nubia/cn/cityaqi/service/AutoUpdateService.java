package nubia.cn.cityaqi.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import java.io.IOException;
import nubia.cn.cityaqi.gson.CityAQI;
import nubia.cn.cityaqi.gson.CityAQIRank;
import nubia.cn.cityaqi.util.HttpUtil;
import nubia.cn.cityaqi.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateCityAQI();
        updateCityAQIRank();
        updateBingPic();

        //创建定时任务
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 1 * 60 * 60 * 1000; //1小时
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent intent1 = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, intent1, 0);
        alarmManager.cancel(pi);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /* 更新城市AQI信息*/
    private void updateCityAQI(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String cityNameSelectedUpdate = prefs.getString("cityNameSelected", null);
        String cityNameAQIData = prefs.getString(cityNameSelectedUpdate, null);//
        if (cityNameAQIData != null){
            final CityAQI cityAQI = Utility.handleCityAQIResponse(cityNameAQIData);
            final String cityNameUp = cityAQI.area;
            String requestAQIUrl = "http://www.pm25.in/api/querys/aqi_details.json?city=" + cityNameUp + "&token=5j1znBVAsnSf5xQyNQyq&stations=no";
            HttpUtil.sendOkhttpRequest(requestAQIUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    CityAQI cityAQI1 = Utility.handleCityAQIResponse(responseText);
                    if (cityAQI1 != null){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString(cityNameUp, responseText);
                        editor.apply();
                    }
                }
            });
        }

    }

    /* 更新城市空气质量排行榜信息*/
    private void updateCityAQIRank(){
        String requestAQIRankUrl = "http://www.pm25.in/api/querys/aqi_ranking.json?&token=5j1znBVAsnSf5xQyNQyq";
        HttpUtil.sendOkhttpRequest(requestAQIRankUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                final CityAQIRank cityAQIRank1 = Utility.handleCityAQIRankResponse(responseText);
                if (cityAQIRank1 != null){
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                    editor.putString("cityAQIRank", responseText);
                    editor.apply();
                }
            }
            });

    }

    /* 更新必应每日一图*/
    private void updateBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkhttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
            }
        });
    }
}
