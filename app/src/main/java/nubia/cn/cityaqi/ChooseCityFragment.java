package nubia.cn.cityaqi;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nubia.cn.cityaqi.db.AllCities;
import nubia.cn.cityaqi.db.MyCities;
import nubia.cn.cityaqi.util.HttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by nubia on 2017/9/22.
 */

public class ChooseCityFragment extends Fragment{

    public LocationClient mLocationClient;
    public TextView locationText;
    public ListView myCitesList;
    public ImageButton addCity;
    private ArrayAdapter<String> adapter;
    private List<MyCities> myAddCitiesList;


    final ArrayList<String> mycitiesListArry = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.choose_city, container, false);
        myCitesList = (ListView) view.findViewById(R.id.myCites_view);
        addCity = (ImageButton) view.findViewById(R.id.add_city);

        /*获取当前城市*/
        locationText = (TextView) view.findViewById(R.id.location);
        mLocationClient = new LocationClient(getContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        requestLocation();

        //我已添加的城市列表
        mycitiesListArry.add("西安");
        requireAddCities();
        adapter = new ArrayAdapter<>(getActivity().getApplication(), android.R.layout.simple_list_item_1, mycitiesListArry);
        myCitesList.setAdapter(adapter);
        myCitesList.setSelection(0);

        return view;
    }

    //查询所有已添加的城市
    public  void requireAddCities(){
        myAddCitiesList = DataSupport.findAll(MyCities.class);
        if (myAddCitiesList.size() > 0){
            mycitiesListArry.clear();
            mycitiesListArry.add("西安");
            for (MyCities myCities : myAddCitiesList){
                mycitiesListArry.add(myCities.getCityName());
            }
        }
    }

    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(1000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation Location) {
            String currentPosition = Location.getCity().toString();
            locationText.setText(currentPosition);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        myCitesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                String cityName = mycitiesListArry.get(i);
                Intent intent = new Intent(getActivity(), CityAQIActivity.class);
                intent.putExtra("cityNameSelected", cityName);
                startActivity(intent);
                getActivity().finish();
            }
        });

        myCitesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int i, long l) {
                final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("添加城市");
                dialog.setMessage("确认删除该城市吗？" + "\n" + mycitiesListArry.get(i));
                dialog.setCancelable(false);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        DataSupport.deleteAll(MyCities.class, "cityName = ?", mycitiesListArry.get(i));
                        requireAddCities();
                        adapter.notifyDataSetChanged();
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

        addCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddCityActivity.class);
                startActivity(intent);
            }
        });
    }

}
