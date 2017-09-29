package nubia.cn.cityaqi;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.lang.String;
import java.util.List;

import nubia.cn.cityaqi.db.AllCities;
import nubia.cn.cityaqi.db.MyCities;
import nubia.cn.cityaqi.util.HttpUtil;
import nubia.cn.cityaqi.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AddCityActivity extends AppCompatActivity {
    private ListView citiesList;
    private ArrayList<String> dataListArray = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private List<AllCities> allCitiesList;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //查询所有的城市
        queryAllCities();
        //将城市列表添加到citiesList_view中
        citiesList = (ListView) findViewById(R.id.citiesList_view);
        adapter = new ArrayAdapter<>(AddCityActivity.this, android.R.layout.simple_list_item_1, dataListArray);
        citiesList.setAdapter(adapter);

        //citiesList中Item长按响应事件
        citiesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                final MyCities myCities = new MyCities();
                final AlertDialog.Builder dialog = new AlertDialog.Builder(AddCityActivity.this);
                dialog.setTitle("添加城市");
                dialog.setMessage("确认添加该城市吗？" + "\n" + dataListArray.get(i));
                dialog.setCancelable(false);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        myCities.setCityName(dataListArray.get(i));
                        myCities.save();
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                    }
                });
                dialog.create().show();
                return true;
            }
        });

    }

    //查询所有的城市
    private void queryAllCities(){
        allCitiesList = DataSupport.findAll(AllCities.class);
        if (allCitiesList.size() > 0){
            dataListArray.clear();
            for (AllCities allCities : allCitiesList){
                dataListArray.add(allCities.getCityName());
            }
        }else {
            String citiesListUrl = "http://www.pm25.in/api/querys.json?&token=5j1znBVAsnSf5xQyNQyq";
            queryAllCitiesFromServer(citiesListUrl);
        }
    }

    //从服务器查询所有的城市
    private void queryAllCitiesFromServer(String citiesListUrl){
        HttpUtil.sendOkhttpRequest(citiesListUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(AddCityActivity.this, "获取城市列表失败", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String allCities = response.body().string();
                Utility.handleAllCitiesResponse(allCities);
            }
        });
    }

}
