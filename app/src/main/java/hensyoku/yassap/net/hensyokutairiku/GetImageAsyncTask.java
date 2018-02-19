package hensyoku.yassap.net.hensyokutairiku;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by matsudayasunori on 2018/02/19.
 */

public class GetImageAsyncTask extends AsyncTask<Object, Void, ArrayList<Shop>> {

    private ImageView imageView;
    private Bitmap mask;
    private final WeakReference<Activity> w_Activity;


    GetImageAsyncTask(Activity activity) {

        this.w_Activity = new WeakReference<>(activity);
    }


    @Override
    protected ArrayList<Shop> doInBackground(Object[] shopUrl) {

        ArrayList<Shop> shopList = new ArrayList<>();
        ArrayList<Shop> iconSetShopList = new ArrayList<>();

        for (int i = 0; i < shopUrl.length; i++) {

            shopList.add((Shop) shopUrl[i]);
        }

        try {

            for (int i = 0; i < shopList.size(); i++) {

                Shop shop = shopList.get(i);
                URL url = new URL(shop.getUrl());
                InputStream inputStream = url.openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                shop.setIcon(bitmap);
                iconSetShopList.add(shop);
            }

        } catch (MalformedURLException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }

        return iconSetShopList;
    }

    @Override
    protected void onPostExecute(ArrayList<Shop> iconSetShopList) {

        Activity activity = w_Activity.get();
        // お店を表示するリストのインスタンスを取得する。
        ListView listView = activity.findViewById(R.id.resultList);
        ShopAdapter shopAdapter = new ShopAdapter(activity, 0, iconSetShopList);
        listView.setAdapter(shopAdapter);

    }
}
