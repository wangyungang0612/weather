package com.example.administrator.weather;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.administrator.weather.gson.Forecast;
import com.example.administrator.weather.gson.Weather;
import com.example.administrator.weather.util.HttpUtitl;
import com.example.administrator.weather.util.Utility;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    @InjectView(R.id.title_city)
    TextView titleCity;
    @InjectView(R.id.title_update_time)
    TextView titleUpdateTime;
    @InjectView(R.id.degree_text)
    TextView degreeText;
    @InjectView(R.id.weather_info_text)
    TextView weatherInfoText;
    @InjectView(R.id.forecast_layout)
    LinearLayout forecastLayout;
    @InjectView(R.id.aqi_text)
    TextView aqiText;
    @InjectView(R.id.pm25_text)
    TextView pm25Text;
    @InjectView(R.id.comfort_text)
    TextView comfortText;
    @InjectView(R.id.car_wash_text)
    TextView carWashText;
    @InjectView(R.id.sport_text)
    TextView sportText;
    @InjectView(R.id.weather_layout)
    ScrollView weatherLayout;
    @InjectView(R.id.bing_pic_img)
    ImageView bingPicImg;
    @InjectView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    @InjectView(R.id.nav_button)
    Button navButton;
    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if(Build.VERSION.SDK_INT >= 21){
//            View decorView = getWindow().getDecorView();
//            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//        }
        setContentView(R.layout.activity_weather);
        ButterKnife.inject(this);

        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);//手动更新

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather", null);
        final String weatherId;

        //加载必应的背景图片
        String bingPic = preferences.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }

        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        //更新天气
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

        //实现滑动菜单
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    //加载必应每日一图
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtitl.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });

            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    //根据天气Id请求城市天气信息
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=04143088eaf5446cb8d0e19dd574c0ce";
        HttpUtitl.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_LONG).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_LONG).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }


    //处理并展示Weather实体类中的数据
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;

        titleCity.setText(cityName);
        titleUpdateTime.setText("最新更新时间:" + " " + updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);

            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度: " + weather.suggestion.comfort.info;
        String carWash = "洗车指数: " + weather.suggestion.carWash.info;
        String sport = "运动建议: " + weather.suggestion.sport.info;

        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        loadBingPic();
    }
}
