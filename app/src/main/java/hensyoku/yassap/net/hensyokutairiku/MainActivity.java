package hensyoku.yassap.net.hensyokutairiku;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener,
        OnMapReadyCallback, LocationListener {

    // グルメリサーチAPI。
    private static final String API_URL = "https://webservice.recruit.co.jp/hotpepper/gourmet/v1/?key=";
    // FoodMenuを取得するURL。
    private static final String FOOD_MENU_URL = "https://webservice.recruit.co.jp/hotpepper/food/v1/?key=";
    // API Key。
    private static final String API_KEY = "";
    // レスポンスのjson指定を行うパラメータ。
    private static final String JSON_PARAMETER = "&format=json";
    // 中エリア検索パラメータ。
    private static final String MIDDLE_AREA = "&middle_area=";
    // メニュー検索パラメータ。
    private static final String FOOD = "&food=";
    // lat(緯度)パラメータ。
    private static final String LAT = "&lat=";
    // lng(経度)パラメータ。
    private static final String LNG = "&lng=";
    // lat、lngの検索範囲パラメータ。1000m以内の検索。
    private static final String RANGE = "&range=";

    private LocationManager locationManager;

    private static final int MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;
    private GoogleMap mMap;


    // お店を調べたい地域、食べたいメニューを格納する配列。
    String[] getParams = new String[3];
    String chikiStr;
    String menuStr;

    // 現在地の緯度。
    private String youAreHereLat;
    // 現在地の経度。
    private String youAreHereLng;

    // 検索地域を格納するMap。
    Map<String, String> chikiHashMap = new HashMap<>();
    Map<String, String> foodMenuMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);

        ListView listView = findViewById(R.id.resultList);
        listView.setOnItemClickListener(this);

        locationStart();
    }

    /**
     * Activity実行直前に呼び出される。
     * ユーザーが戻るボタンをクリックして画面が再表示された場合にも呼び出される。
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (chikiHashMap.isEmpty() && foodMenuMap.isEmpty()) {

            // Spinnerに表示する検索地域をMapに格納するメソッド。
            chikiMenuAdd();

            String[] getFoodMenuParameter = new String[3];
            getFoodMenuParameter[0] = FOOD_MENU_URL;
            getFoodMenuParameter[1] = API_KEY;
            getFoodMenuParameter[2] = JSON_PARAMETER;
            new FoodMenuGetAsyncTask(this).execute(getFoodMenuParameter);
        }
    }

    /**
     * Searchボタンをクリックした際の処理。
     * Spinnerで選択した内容をキーに、飲食店の情報を取得する。
     *
     * @param v クリックしたButtonの情報。
     */
    @Override
    public void onClick(View v) {

        if (v != null && v.getId() == R.id.searchButton) {

            Spinner chikiSpinner = findViewById(R.id.chiki);
            Spinner menuSpinner = findViewById(R.id.menu);

            // Spinnerで選択した場所を取得する。
            chikiStr = (String) chikiSpinner.getSelectedItem();
            // Spinnerで選択したメニューを取得する。
            menuStr = (String) menuSpinner.getSelectedItem();
            // メニューのSpinnerで選んだメニューのindexを取得する。
            int menuPosition = menuSpinner.getSelectedItemPosition();

            // プリファレンスよりfoodのCodeを取得する。
            String foodCode
                    = this.getSharedPreferences("foodCodeName", MODE_PRIVATE).getString("foodCodeList", "");

            // メニューのSpinnerに表示されている食べ物に紐づくCodeを格納するList。
            List<String> foodCodeList = new ArrayList<>();

            try {

                JSONArray jsonArray = new JSONArray(foodCode);

                for (int i = 0; i < jsonArray.length(); i++) {

                    // メニューに紐づくCodeをListへ格納する。
                    foodCodeList.add(jsonArray.getString(i));
                }

            } catch (JSONException je) {

                je.printStackTrace();
            }

            // グルメサーチAPIのURLを格納。
            getParams[0] = API_URL + API_KEY + JSON_PARAMETER;
            // 中エリアコードをパラメータ形式に変換した文字列を格納。
            getParams[1] = MIDDLE_AREA + chikiHashMap.get(chikiStr);
            // 検索するメニューを格納。
            getParams[2] = FOOD + foodCodeList.get(menuPosition);

            String range = "";
            // 周辺検索を選択した際の非同期通信呼び出し。
            if (chikiStr.contains("周辺検索")) {

                switch (chikiStr) {

                    case "周辺検索(300m以内)":

                        range = "1";
                        break;

                    case "周辺検索(500m以内)":

                        range = "2";
                        break;

                    case "周辺検索(1000m以内)":

                        range = "3";
                        break;

                    case "周辺検索(2000m以内)":

                        range = "4";
                        break;

                    case "周辺検索(3000m以内)":

                        range = "5";
                        break;

                    default:

                        break;
                }

                // 現在地の緯度、経度、検索範囲を設定。
                getParams[1] = LAT + youAreHereLat + LNG + youAreHereLng + RANGE + range;
            }

            // まずは検索したいワードをキーにじゃらんAPIよりデータを取得する。
            new GetAsyncTask(this).execute(getParams);
        }
    }


    /**
     * ListViewに表示されたお店情報をクリックした際の処理を実装する。
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // プリファレンスより、shopUrlsに保存されているurlListのデータを取得する。
        String json = this.getSharedPreferences("shopUrls", MODE_PRIVATE).getString("urlList", "");

        // URLを格納するリスト。
        List<String> urlList = new ArrayList<>();

        try {

            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {

                // プリファレンスより取得したお店のURLを格納する。
                urlList.add(jsonArray.getString(i));
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }

        // クリックしたお店のindexを取得する。
        String intentUrl = urlList.get(position);

        // WebViewへ画面遷移するIntentを生成する。
        Intent intent = new Intent(this, WebViewActivity.class);
        // intentにお店のURLを設定する。
        intent.putExtra("url", intentUrl);
        startActivity(intent);
    }


    /**
     * Spinnerに表示する地域をMapへ格納するメソッド。
     * 地域名と地域ごとに割り当てられているIDを紐づける。
     */
    private void chikiMenuAdd() {

        chikiHashMap.put("博多", "Y700");
        chikiHashMap.put("天神・西中洲・春吉", "Y706");
        chikiHashMap.put("中洲・中洲川端", "Y705");
        chikiHashMap.put("西新・姪浜・その他西エリア", "Y710");
        chikiHashMap.put("吉塚・香椎・その他東エリア", "Y711");
        chikiHashMap.put("大名・今泉・警固", "Y712");
        chikiHashMap.put("薬院･平尾･高砂", "Y713");
    }

    private void locationStart() {

        // locationManagerインスタンス生成。
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            // 正常

        } else {

            // GPSを設定するように促す。
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 50, this);
    }


    /**
     * 詳細な位置情報へのアクセスを許可するパーミッション。
     */
    private void requestAccessFineLocation() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
    }


    /**
     * LocationProviderが有効になった場合に呼び出される。
     *
     * @param provider
     */
    @Override
    public void onProviderDisabled(String provider) {

        // 処理なし。
    }

    /**
     * LocationProviderの状態が変更された場合に呼び出される
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

        // 処理なし。
    }

    /**
     * LocationProviderが無効になった場合に呼び出される。
     *
     * @param provider
     */
    @Override
    public void onProviderEnabled(String provider) {

        // 処理なし。
    }


    /**
     * 現在地が変わる場合に呼び出される処理。
     */
    @Override
    public void onLocationChanged(Location location) {

        // 現在地から緯度を取得する。
        youAreHereLat = String.valueOf(location.getLatitude());
        // 現在地から経度を取得する。
        youAreHereLng = String.valueOf(location.getLongitude());

    }


    /**
     * リクエスト結果を受け取る。
     * ダイアログで許可か否かを選択した結果を受け取る。
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {

            case MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION:

                // ユーザーが許可した時。
                // 許可が必要な機能を改めて実行する。
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    locationStart();

                } else {
                    // ユーザーが許可しなかった時。
                    // 許可されなかったため機能が実行できないことを表示する。
                    Toast.makeText(this, "許可されていないため使用できません", Toast.LENGTH_LONG).show();
                }
        }
    }


    /**
     * Mapの表示オプションの設定や、マーカーの追加などを行う。
     * GoogleMapに対するハンドルを取得する。
     * マップを使用できるようになると、コールバックがトリガーされる。
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // Dangerousなパーミッションはリクエストして許可をもらわないと使えない。
        // 現在地の表示（青い丸）
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            // shouldShowRequestPermissionRationaleは、アプリがすでにパーミッションをリクエストしていて、ユーザーがその
            // パーミッションを拒否した場合にtrueを返す。
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                // 一度拒否されたとき、Rationale(理論的根拠)を説明して、再度許可ダイアログを出すようにする。
                new AlertDialog.Builder(this)
                        .setTitle("許可が必要です。")
                        .setMessage("移動に合わせて地図を動かすためには、ACCESS_FINE_LOCATIONを許可してください")
                        .setPositiveButton("わかった", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // OKボタンをクリックした時の処理。
                                requestAccessFineLocation();
                            }
                        }).show();

            } else {

                // まだ許可を求める前の時、許可を求めるダイアログを表示する。
                requestAccessFineLocation();
            }

        }
    }
}
