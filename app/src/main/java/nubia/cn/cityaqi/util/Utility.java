package nubia.cn.cityaqi.util;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import nubia.cn.cityaqi.db.AllCities;
import nubia.cn.cityaqi.gson.CityAQI;
import nubia.cn.cityaqi.gson.CityAQIRank;
import nubia.cn.cityaqi.gson.CityAQIRankOneCity;

/**
 * Created by nubia on 2017/9/22.
 */

public class Utility {

    /*将返回的城市空气质量JSON数据解析成CityAQI类*/
    public static CityAQI handleCityAQIResponse(String response){
        if (!TextUtils.isEmpty(response)) {
            try {
                CityAQI cityAQI = new CityAQI();
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    cityAQI.area = jsonObject.getString("area");
                    cityAQI.aqi = jsonObject.getString("aqi");
                    cityAQI.quality = jsonObject.getString("quality");
                    cityAQI.pm2_5 = jsonObject.getString("pm2_5");
                    cityAQI.pm10 = jsonObject.getString("pm10_24h");
                    cityAQI.so2 = jsonObject.getString("so2_24h");
                    cityAQI.no2 = jsonObject.getString("no2_24h");
                    cityAQI.o3 = jsonObject.getString("o3_24h");
                    cityAQI.co = jsonObject.getString("co_24h");
                }
                return cityAQI;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /*将返回的城市空气质量排行榜JSON数据解析成CityAQIRank类*/
    @Nullable
    public static CityAQIRank handleCityAQIRankResponse(String response){
        if (!TextUtils.isEmpty(response)) {
            try {
                CityAQIRank cityAQIRank = new CityAQIRank();
                JSONArray jsonArray = new JSONArray(response);
                cityAQIRank.CityAQIRankArray.clear();
                for (int i = 0, j = 1; i < jsonArray.length(); i++, j++) {   //将jsonArray.length()改为20，取前20名
                    CityAQIRankOneCity cityAQIRankOneCity = new CityAQIRankOneCity();
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    cityAQIRankOneCity.setAqi(jsonObject.getString("aqi"));
                    cityAQIRankOneCity.setArea(jsonObject.getString("area"));
                    cityAQIRankOneCity.setQuality(jsonObject.getString("quality"));
                    cityAQIRankOneCity.setI(j + "");
                    cityAQIRank.CityAQIRankArray.add(cityAQIRankOneCity);
                }
                return cityAQIRank;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //解析和处理服务器返回的所有城市列表数据
    public static void handleAllCitiesResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("cities");
                for (int i = 0; i < jsonArray.length(); i++){
                    String city = jsonArray.get(i).toString();
                    AllCities allCities = new AllCities();
                    allCities.setCityName(city);
                    allCities.setId(i);
                    allCities.save();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}