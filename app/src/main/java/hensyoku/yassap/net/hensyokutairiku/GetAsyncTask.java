package hensyoku.yassap.net.hensyokutairiku;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 飲食店情報の検索、画面表示を行う非同期処理クラス。
 */
public class GetAsyncTask extends AsyncTask<String, Void, Object> {

    private final WeakReference<Activity> w_Activity;

    private ProgressDialog m_ProgressDialog;

    GetAsyncTask(Activity activity) {

        this.w_Activity = new WeakReference<>(activity);
    }

    /**
     * 実行前の事前処理。
     */
    @Override
    protected void onPreExecute() {

        Activity activity = w_Activity.get();
        // プログレスダイアログの生成。
        this.m_ProgressDialog = new ProgressDialog(activity);
        // プログレスダイアログの設定。
        this.m_ProgressDialog.setMessage("検索中...L(' 皿 ')」ウィー");
        // プログレスダイアログの表示。
        this.m_ProgressDialog.show();
    }

    /**
     * バックグラウンド実行処理。
     */
    @Override
    protected Object doInBackground(String[] strings) {

        // 検索したい地域を取得。
        String apiUrl = strings[0];

        // 検索したい中エリアコードを取得。
        String middleEreaCodeParameter = strings[1];

        // 検索したいメニューを取得。
        String menuWordParameter = strings[2];

        // HTTP処理用オブジェクトを生成。
        OkHttpClient okHttpClient = new OkHttpClient();

        // リクエストオブジェクトを生成。
        Request request = new Request.Builder()
                .url(apiUrl + middleEreaCodeParameter + menuWordParameter)
                .get()
                .build();

        // 受信用オブジェクトを生成。
        Call call = okHttpClient.newCall(request);
        String result = "";

        // 送信と受診を行う。
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
     * 取得したJSONデータを編集してListViewへ表示する。
     *
     * @param result リクエストにより取得したJSONデータ。
     */
    @Override
    protected void onPostExecute(Object result) {

        super.onPostExecute(result);

        // お店の名前と住所を格納するList。
        List<Map<String, String>> shopNameAddress = new ArrayList<>();

        // お店のURLを格納する。
        JSONArray shopUrls = new JSONArray();

        try {

            JSONObject json = new JSONObject((String) result);
            JSONObject results = json.getJSONObject("results");

            JSONArray shops = results.getJSONArray("shop");

            for (int i = 0; i < shops.length(); i++) {

                JSONObject shopData = shops.getJSONObject(i);

                JSONObject url = shopData.getJSONObject("urls");
                shopUrls.put(url.getString("pc"));

                // ここで毎回newしてデータを打ち込まないといけんよ。参照に紐づくデータを上書きしてしまうよ。
                Map<String, String> saMap = new HashMap<>();
                saMap.put("shopName", shopData.getString("name"));
                saMap.put("shopAddress", shopData.getString("address"));
                shopNameAddress.add(saMap);
            }

        } catch (JSONException je) {

            je.getStackTrace();
        }

        Activity activity = w_Activity.get();

        if (activity == null || activity.isFinishing()) {

            return;
        }

        // 検索したお店のURLをプリファレンスに保存する。
        // リスト（お店）を選んだ時のWebViewへの画面遷移に使用する。
        SharedPreferences.Editor editor
                = activity.getApplicationContext().getSharedPreferences("shopUrls", Context.MODE_PRIVATE).edit();

        editor.putString("urlList", shopUrls.toString());
        editor.apply();

        // お店を表示するリストのインスタンスを取得する。
        ListView listView = activity.findViewById(R.id.resultList);

        // 検索して取得できたお店が0件だった場合、メッセージを表示する。
        if (shopNameAddress.isEmpty()) {

            Toast.makeText(activity, "該当するお店はありませんでした", Toast.LENGTH_LONG).show();
        }

        // リストに表示するタイトルとサブタイトルを設定する。
        SimpleAdapter adapter = new SimpleAdapter(activity, shopNameAddress, R.layout.list, new String[]{"shopName", "shopAddress"},
                new int[]{R.id.shopName, R.id.shopAddress});

        // リストに上記で設定したデータをリストを表示するオブジェクトに設定する。
        listView.setAdapter(adapter);

        // お店情報の検索、表示の処理が終了した場合、読み込みグルングルンを終了する。
        if (this.m_ProgressDialog != null && this.m_ProgressDialog.isShowing()) {

            this.m_ProgressDialog.dismiss();
        }
    }
}
