package nubia.cn.cityaqi.db;

import org.litepal.crud.DataSupport;

/**
 * Created by nubia on 2017/9/25.
 */

public class AllCities extends DataSupport{
    private int id;
    private String CityName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return CityName;
    }

    public void setCityName(String cityName) {
        CityName = cityName;
    }
}
