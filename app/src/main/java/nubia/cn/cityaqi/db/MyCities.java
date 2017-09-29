package nubia.cn.cityaqi.db;

import org.litepal.crud.DataSupport;

/**
 * Created by nubia on 2017/9/25.
 */

public class MyCities extends DataSupport{

    private String CityName;

    public String getCityName() {
        return CityName;
    }

    public void setCityName(String cityName) {
        CityName = cityName;
    }
}
