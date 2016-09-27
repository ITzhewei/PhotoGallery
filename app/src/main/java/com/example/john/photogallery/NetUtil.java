package com.example.john.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZheWei on 2016/9/27.
 */
public class NetUtil {

    private static final String TAG = "aaa";
    private static final String API_KEY = "95b8e0b2238f94a69feb06aca4148645";

    public byte[] getUrlBytes(String specUrl) throws Exception {
        URL url = new URL(specUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException();
            }

            int byteRead = 0;
            byte[] buffer = new byte[1024];
            while ((byteRead = inputStream.read(buffer)) > 0) {
                out.write(buffer, 0, byteRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String specUrl) throws Exception {
        return new String(getUrlBytes(specUrl));
    }

    public List<GalleryItem> fetchItems() {
        List<GalleryItem> galleryItems = new ArrayList<>();
        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/").buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "fetchItems: " + jsonString);

            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(galleryItems, jsonBody);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return galleryItems;
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws Exception {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoArray = photosJsonObject.getJSONArray("photo");
        for (int i = 0; i < photoArray.length(); i++) {
            JSONObject object = photoArray.getJSONObject(i);
            GalleryItem galleryItem = new GalleryItem();
            galleryItem.setCaption(object.getString("title"));
            galleryItem.setId(object.getString("id"));
            if (!object.has("url_s")) {
                continue;
            }
            galleryItem.setMurl(object.getString("url_s"));
            items.add(galleryItem);
        }
    }
}
