package hensyoku.yassap.net.hensyokutairiku;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

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

    // お店を調べたい地域、食べたいメニューを格納する配列。
    String[] getParams = new String[3];
    String chikiStr;
    String menuStr;

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
            //getParams[2] = FOOD + foodMenuMap.get(menuStr);

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
}
