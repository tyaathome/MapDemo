package tyaathome.com.mapdemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.amap.api.maps.model.VisibleRegion;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private AMap aMap;
    private boolean isShowCoordLine = false;
    private List<Polyline> coordLineList = new ArrayList<>();
    private List<Text> textList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMap(savedInstanceState);
        CheckBox checkBox = findViewById(R.id.button);
        checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    private void initMap(Bundle savedInstanceState) {
        mapView = findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        aMap.setOnCameraChangeListener(onCameraChangeListener);
        UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setTiltGesturesEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * 显示经纬线
     */
    private void showCoord() {
        hideCoord();
        VisibleRegion visibleRegion = aMap.getProjection().getVisibleRegion();
        LatLng farLeft = visibleRegion.farLeft;
        LatLng farRight = visibleRegion.farRight;
        LatLng nearLeft = visibleRegion.nearLeft;
        LatLng nearRight = visibleRegion.nearRight;
        CameraPosition currentCamera = aMap.getCameraPosition();
        float zoom = currentCamera.zoom;
        float flag;
        if(zoom >= 6f) { // 一级 区间1°
            flag = 1f;
        } else if (zoom >= 5f) { // 二级 区间2°
            flag = 2f;
        } else if (zoom >= 4f) { // 三级 区间5°
            flag = 5f;
        } else if (zoom >= 3f) { // 四级 区间10°
            flag = 10f;
        } else { // 五级 区间20°
            flag = 20f;
        }
        List<PolylineOptions> lineList = new ArrayList<>();
        List<TextOptions> textOptionsList = new ArrayList<>();
        // 水平方向
        double hStart = Math.floor(farLeft.latitude/flag) * flag;
        double hEnd = Math.ceil(nearLeft.latitude/flag) * flag;
        if(hStart >= hEnd) {
            double value = hStart;
            while (value >= hEnd) {
                LatLng start = new LatLng(value, farLeft.longitude);
                LatLng end = new LatLng(value, farRight.longitude);
                lineList.add(createPolylineOptions(start, end));
                textOptionsList.add(createHTextOptions(String.valueOf(value), start));
                value -= flag;
            }
        }
        // 垂直方向
        double vStart = Math.ceil(farLeft.longitude/flag) * flag;
        double vEnd = Math.floor(farRight.longitude/flag) * flag;
        if(vStart <= vEnd) {
            double value = vStart;
            while (value <= vEnd) {
                LatLng start = new LatLng(farLeft.latitude, value);
                LatLng end = new LatLng(nearLeft.latitude, value);
                lineList.add(createPolylineOptions(start, end));
                textOptionsList.add(createVTextOptions(String.valueOf(value), start));
                value += flag;
            }
        }
        coordLineList.clear();
        for (PolylineOptions polylineOptions : lineList) {
            coordLineList.add(aMap.addPolyline(polylineOptions));
        }
        textList.clear();
        for (TextOptions textOptions : textOptionsList) {
            textList.add(aMap.addText(textOptions));
        }
    }

    private PolylineOptions createPolylineOptions(LatLng latLng, LatLng latLng2) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(latLng, latLng2).color(Color.RED).setDottedLine(true).width(5f).zIndex(1);
        return polylineOptions;
    }

    private TextOptions createTextOptions(String content, LatLng latlng) {
        TextOptions textOptions = new TextOptions();
        textOptions.text(content).position(latlng).fontColor(Color.RED).backgroundColor(Color.TRANSPARENT).fontSize(30).zIndex(1);
        return textOptions;
    }

    private TextOptions createHTextOptions(String content, LatLng latLng) {
        return createTextOptions(content, latLng).align(Text.ALIGN_LEFT, Text.ALIGN_BOTTOM);
    }

    private TextOptions createVTextOptions(String content, LatLng latLng) {
        return createTextOptions(content, latLng).align(Text.ALIGN_LEFT, Text.ALIGN_TOP);
    }

    /**
     * 隐藏经纬线
     */
    private void hideCoord() {
        if(coordLineList != null) {
            for(Polyline polyline : coordLineList) {
                polyline.remove();
            }
        }
        if(textList != null) {
            for(Text text : textList) {
                text.remove();
            }
        }
    }

    private AMap.OnCameraChangeListener onCameraChangeListener = new AMap.OnCameraChangeListener() {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {

        }

        @Override
        public void onCameraChangeFinish(CameraPosition cameraPosition) {
            if(isShowCoordLine) {
                showCoord();
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {


        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.button:
                    isShowCoordLine = isChecked;
                    if(isChecked) {
                        showCoord();
                    } else {
                        hideCoord();
                    }
                    break;
            }
        }
    };
}
