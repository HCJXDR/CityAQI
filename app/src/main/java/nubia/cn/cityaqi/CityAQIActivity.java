package nubia.cn.cityaqi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.io.IOException;

import nubia.cn.cityaqi.gson.CityAQI;
import nubia.cn.cityaqi.gson.CityAQIRank;
import nubia.cn.cityaqi.gson.CityAQIRankOneCity;
import nubia.cn.cityaqi.service.AutoUpdateService;
import nubia.cn.cityaqi.util.HttpUtil;
import nubia.cn.cityaqi.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CityAQIActivity extends AppCompatActivity {

    private TextView titleCity;
    private ImageView bingPicImg;
    private TextView aqi;
    private TextView quality;
    private TextView pm25;
    private TextView pm10;
    private TextView so2;
    private TextView no2;
    private TextView o3;
    private TextView co;
    private ScrollView cityAQILayout;
    private Button navButton;
    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout cityranklayou;
    private LinearLayout currentCityLayout;
    private TextView city_Rank;
    //private ImageButton addcityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_aqi);

        //初始化各控件
        //addcityButton = (ImageButton) findViewById(R.id.addcity_button);
        city_Rank = (TextView) findViewById(R.id.city_rank);
        currentCityLayout = (LinearLayout) findViewById(R.id.currentCityLayout);
        cityranklayou = (LinearLayout) findViewById(R.id.cityrank_layout);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
        titleCity = (TextView) findViewById(R.id.title_city);
        aqi = (TextView) findViewById(R.id.aqi_text);
        quality = (TextView) findViewById(R.id.quality_text);
        pm25 = (TextView) findViewById(R.id.pm25_text);
        pm10 = (TextView) findViewById(R.id.pm10_text);
        so2 = (TextView) findViewById(R.id.so2_text);
        no2 = (TextView) findViewById(R.id.no2_text);
        o3 = (TextView) findViewById(R.id.o3_text);
        co = (TextView) findViewById(R.id.co_text);
        cityAQILayout = (ScrollView) findViewById(R.id.cityAQI_layout);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);

        //导航button的监听事件，打开侧滑栏
        navButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //加载必应每日一图
        //loadBingPic();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }

        //显示当前城市空气质量
        String currentCity = "西安";
        String currentCityString = prefs.getString(currentCity, null);
        if(currentCityString != null){
            //有缓存时直接解析天气数据
            CityAQI cityAQI = Utility.handleCityAQIResponse(currentCityString);
            showCityAQI(cityAQI);
        }else {
            cityAQILayout.setVisibility(View.VISIBLE);
            requestCityAQI(currentCity);
        }
        //显示选择的其他城市空气质量信息
        String cityName = getIntent().getStringExtra("cityNameSelected");

        //用于服务中获取当前选择的城市名称
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CityAQIActivity.this).edit();
        editor.putString("cityNameSelected", cityName);
        editor.apply();

        if (!"西安".equals(cityName)){
            String cityaqiString = prefs.getString(cityName, null);
            if(cityaqiString != null){
                //有缓存时直接解析空气质量数据
                CityAQI cityAQI = Utility.handleCityAQIResponse(cityaqiString);
                showCityAQI(cityAQI);
            }else {
                requestCityAQI(cityName);
            }
        }

        //获取并显示城市空气质量排行榜
        String cityRank = prefs.getString("cityAQIRank", null);
        if (cityRank != null){
            //有缓存时直接解析城市空气质量排名
            Utility.handleCityAQIRankResponse(cityRank);
            showCityAQIRank();
        }else{
            requestCityAQIRank();
        }

        //手动刷新
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String mCityName = getIntent().getStringExtra("cityNameSelected");
                requestCityAQI(mCityName);
                requestCityAQIRank();
                loadBingPic();
            }
        });

        //addcityButton监听响应事件
        /*addcityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CityAQIActivity.this, AddCityActivity.class);
                startActivity(intent);
            }
        });*/

    }

    //根据城市名称查询城市AQI信息
    public void requestCityAQI(final String cityName){
        String requestAQI = "http://www.pm25.in/api/querys/aqi_details.json?city=" + cityName + "&token=5j1znBVAsnSf5xQyNQyq&stations=no";
        HttpUtil.sendOkhttpRequest(requestAQI, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(CityAQIActivity.this, "获取城市空气质量信息失败", Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                Log.d("cityAQI", responseData);
                final CityAQI cityAQI = Utility.handleCityAQIResponse(responseData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (cityAQI != null){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CityAQIActivity.this).edit();
                            editor.putString(cityName, responseData);
                            editor.apply();
                            showCityAQI(cityAQI);
                        }else {
                            Toast.makeText(CityAQIActivity.this, "获取城市空气质量信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    //处理并展示CityAQI实体类中的数据
    private void showCityAQI(CityAQI cityAQI){
        Intent intentAutoUpdate = new Intent(this, AutoUpdateService.class);
        startService(intentAutoUpdate);
        String cityNameI = cityAQI.area;
        String qualityI = cityAQI.quality;
        String aqiI = cityAQI.aqi;
        String coI = cityAQI.co;
        String no2I = cityAQI.no2;
        String o3I = cityAQI.o3;
        String pm10I = cityAQI.pm10;
        String pm2_5I = cityAQI.pm2_5;
        String so2I = cityAQI.so2;
        titleCity.setText(cityNameI);
        aqi.setText(aqiI);
        quality.setText(qualityI);
        pm25.setText(pm2_5I);
        pm10.setText(pm10I);
        so2.setText(so2I);
        no2.setText(no2I);
        o3.setText(o3I);
        co.setText(coI);
        switch (qualityI){
            case "优":
                currentCityLayout.setBackgroundColor(Color.GREEN);
                currentCityLayout.getBackground().setAlpha(50);
                break;
            case "良":
                currentCityLayout.setBackgroundColor(Color.YELLOW);
                currentCityLayout.getBackground().setAlpha(50);
                break;
            case "轻度污染":
                currentCityLayout.setBackgroundColor(Color.GRAY);
                currentCityLayout.getBackground().setAlpha(50);
                break;
            case "严重污染":
                currentCityLayout.setBackgroundColor(Color.RED);
                currentCityLayout.getBackground().setAlpha(50);
                break;
            default:
                break;
        }
        /*//显示当前城市排名
        for (CityAQIRankOneCity cityAQIRankOneCity : CityAQIRank.CityAQIRankArray){
            if(cityAQIRankOneCity.getArea() == titleCity.getText()){
                city_Rank.setText(cityAQIRankOneCity.getI());
            }
        }*/
        cityAQILayout.setVisibility(View.VISIBLE);
    }

    //查询城市空气质量排行榜
    public void requestCityAQIRank(){
        String cityRankUrl = "http://www.pm25.in/api/querys/aqi_ranking.json?&token=5j1znBVAsnSf5xQyNQyq";
        HttpUtil.sendOkhttpRequest(cityRankUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(CityAQIActivity.this, "获取城市空气质量排行榜失败", Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                Log.d("CityAQIRank",responseData);
                final CityAQIRank cityAQIRank = Utility.handleCityAQIRankResponse(responseData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (cityAQIRank != null){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CityAQIActivity.this).edit();
                            editor.putString("cityAQIRank", responseData);
                            editor.apply();
                            showCityAQIRank();
                        }else {
                            Toast.makeText(CityAQIActivity.this, "获取城市空气质量排行榜失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    //显示城市空气质量排行榜
    public void showCityAQIRank(){
        cityranklayou.removeAllViews();
        int cityNumber = 0;
        for (CityAQIRankOneCity cityAQIRankOneCity : CityAQIRank.CityAQIRankArray){
            View view = LayoutInflater.from(this).inflate(R.layout.city_rank_item, cityranklayou, false);
            TextView city = (TextView) view.findViewById(R.id.city_text);
            city.setText(cityAQIRankOneCity.getArea());
            TextView aqi = (TextView) view.findViewById(R.id.aqi_text);
            aqi.setText(cityAQIRankOneCity.getAqi());
            TextView quality = (TextView) view.findViewById(R.id.quality_text);
            quality.setText(cityAQIRankOneCity.getQuality());
            TextView sort = (TextView) view.findViewById(R.id.sort_text);
            sort.setText(cityAQIRankOneCity.getI());
            cityranklayou.addView(view);
            cityNumber++;
            if (cityNumber == 20){
                break;
            }
            cityranklayou.setVisibility(View.VISIBLE);
        }
        //显示当前城市排名
        for (CityAQIRankOneCity cityAQIRankOneCity : CityAQIRank.CityAQIRankArray){
            if(cityAQIRankOneCity.getArea().equals(titleCity.getText())){
                city_Rank.setText(cityAQIRankOneCity.getI());
            }
        }
    }

    /*加载必应每日一图*/
    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkhttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                swipeRefresh.setRefreshing(false);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CityAQIActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(CityAQIActivity.this).load(bingPic).into(bingPicImg);
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }
}

