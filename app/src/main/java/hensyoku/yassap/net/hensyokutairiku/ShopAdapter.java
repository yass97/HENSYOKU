package hensyoku.yassap.net.hensyokutairiku;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by matsudayasunori on 2018/02/19.
 */

public class ShopAdapter extends ArrayAdapter<Shop> {

    private LayoutInflater layoutInflater;

    public ShopAdapter(Context context, int id, ArrayList<Shop> shops) {

        super(context, id, shops);
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {

            convertView = layoutInflater.inflate(R.layout.list, parent, false);
        }

        Shop shop = (Shop) getItem(position);

        ((ImageView) convertView.findViewById(R.id.listImageView)).setImageBitmap(shop.getIcon());
        ((TextView) convertView.findViewById(R.id.shopName)).setText(shop.getShopName());
        ((TextView) convertView.findViewById(R.id.shopAddress)).setText(shop.getShopAddress());

        return convertView;

    }
}
