package hensyoku.yassap.net.hensyokutairiku;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * メニューを全部取ってきて、Spinnerに設定する処理を行う非同期処理クラス。。
 * もはや偏食ではなくなったのではないだろうか。
 */
public class FoodMenuGetAsyncTask extends AsyncTask<String, Object, Object> {

    // 弱参照。ガベージコレクションにやられやすいので、メモリを節約できる。
    private final WeakReference<Activity> w_Activity;

    FoodMenuGetAsyncTask(Activity activity) {

        this.w_Activity = new WeakReference<>(activity);
    }

    /**
     * バックグラウンド処理。
     *
     * @param strings MainActivityより渡されたAPIのURLとAPI Key。
     * @return result 取得したJSONデータをonPostExecuteへ渡す。
     */
    @Override
    protected Object doInBackground(String[] strings) {

        String apiUrl = strings[0];
        String searchArea = strings[1];
        String foodMenu = strings[2];

        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(apiUrl + searchArea + foodMenu)
                .get()
                .build();

        Call call = okHttpClient.newCall(request);
        String result = "";

        try {

            Response response = call.execute();
            ResponseBody responseBody = response.body();

            if (responseBody != null) {

                result = responseBody.string();
            }

        } catch (IOException ioe) {

            ioe.printStackTrace();
        }

        return result;
    }


    /**
     * レスポンス結果を画面に表示する。
     */
    @Override
    protected void onPostExecute(Object result) {

        super.onPostExecute(result);

        List<String> foodNameList = new ArrayList<>();
        JSONArray foodCodeList = new JSONArray();

        try {

            JSONObject json = new JSONObject((String) result);
            JSONObject results = json.getJSONObject("results");
            JSONArray foods = results.getJSONArray("food");

            for (int i = 0; i < foods.length(); i++) {

                JSONObject foodData = foods.getJSONObject(i);
                foodCodeList.put(foodData.getString("code"));
                foodNameList.add(foodData.getString("name"));
            }

        } catch (JSONException je) {

            je.printStackTrace();
        }

        Activity activity = w_Activity.get();

        if (activity == null || activity.isFinishing()) {

            return;
        }

        SharedPreferences.Editor editor
                = activity.getApplicationContext().getSharedPreferences("foodCodeName", Context.MODE_PRIVATE).edit();

        editor.putString("foodCodeList", foodCodeList.toString());
        editor.apply();

        // FoodMenuのSpinnerへ取得した食事のメニューを設定する。
        ArrayAdapter<String> foodMenuAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item);
        foodMenuAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Adapterにアイテムをセット。
        for (String foodName : foodNameList) {

            foodMenuAdapter.add(foodName);
        }

        Spinner foodSpinner = activity.findViewById(R.id.menu);
        foodSpinner.setAdapter(foodMenuAdapter);
    }
}
