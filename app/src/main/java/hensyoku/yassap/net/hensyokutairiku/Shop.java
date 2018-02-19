package hensyoku.yassap.net.hensyokutairiku;

import android.graphics.Bitmap;

/**
 * 取得した飲食店のデータを格納するBeanクラス。
 */
public class Shop {

    private Bitmap icon;
    private String shopName;
    private String shopAddress;
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setIcon(Bitmap icon) {

        this.icon = icon;
    }

    public void setShopName(String shopName) {

        this.shopName = shopName;
    }

    public void setShopAddress(String shopAddress) {

        this.shopAddress = shopAddress;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public String getShopName() {

        return shopName;
    }

    public String getShopAddress() {

        return shopAddress;
    }
}
