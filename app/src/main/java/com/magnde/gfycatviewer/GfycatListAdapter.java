package com.magnde.gfycatviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by mukga on 12/8/2016.
 */

class GfycatListAdapter extends BaseAdapter {

    private ArrayList<JSONObject> mData;
    private Context mContext;

    GfycatListAdapter(Context context, ArrayList<JSONObject> data) {
        mContext = context;
        mData = data;
    }

    void addToList(JSONArray newData) {
        for (int i = 0; i < newData.length(); i++) {
            try {
                mData.add(newData.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        this.notifyDataSetChanged();
    }

    void clear() {
        mData.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = View.inflate(mContext, R.layout.gfycat_item, null);
        }

        JSONObject item = (JSONObject) getItem(i);

        try {
            ((TextView) view.findViewById(R.id.video_title)).setText(item.getString("title"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            new DownloadImageTask(((ImageView) view.findViewById(R.id.poster)))
                .execute(item.getString("mobilePosterUrl"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        view.findViewById(R.id.video).setVisibility(View.GONE);
        view.findViewById(R.id.poster).setVisibility(View.VISIBLE);

        return view;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap poster = Cache.getImage(url);

            if (poster != null)
                return poster;

            try {
                InputStream in = new java.net.URL(url).openStream();
                poster = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Cache.saveImage(url, poster);
            return poster;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
}