package com.magnde.gfycatviewer;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private RequestQueue mVolleyQueue;
    private ListView mListView;
    private GfycatListAdapter mListAdapter;
    private SwipeRefreshLayout mSwipeView;
    private String mCursor = null;
    private boolean mListUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVolleyQueue = Volley.newRequestQueue(this);

        mSwipeView = (SwipeRefreshLayout) findViewById(R.id.refresh_view);
        mSwipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mListAdapter.clear();
                mCursor = null;
                updateListView();
            }
        });

        mListAdapter = new GfycatListAdapter(this, new ArrayList<JSONObject>());
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final View poster = view.findViewById(R.id.poster);
                poster.setVisibility(View.GONE);

                JSONObject item = (JSONObject) mListAdapter.getItem(i);
                final VideoView videoView = (VideoView) view.findViewById(R.id.video);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(poster.getWidth(),
                        poster.getHeight());
                videoView.setLayoutParams(lp);
                videoView.setVisibility(View.VISIBLE);

                final MediaController mediaController = new MediaController(MainActivity.this);
                mediaController.setAnchorView(videoView);
                videoView.setMediaController(null);

                try {
                    videoView.setVideoURI(Uri.parse(item.getString("mobileUrl")));
                    videoView.start();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        videoView.setVisibility(View.GONE);
                        poster.setVisibility(View.VISIBLE);
                    }
                });

            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                        && (mListView.getLastVisiblePosition() - mListView.getHeaderViewsCount() -
                        mListView.getFooterViewsCount()) >= (mListAdapter.getCount() - 1)
                        && !mListUpdating) {

                    mListUpdating = true;
                    updateListView();
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            }
        });

        updateListView();
    }

    private void updateListView() {
        final JSONObject params = new JSONObject();
        try {
            params.put("count", "20");
            if (mCursor != null) params.put("cursor", mCursor);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = getEncodedURL(params);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mCursor = response.getString("cursor");
                            mListAdapter.addToList(response.getJSONArray("gfycats"));
                            mSwipeView.setRefreshing(false);
                            mListUpdating = false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Failed to retrieve new gfycats =(",
                        Toast.LENGTH_SHORT).show();
            }
        });

        mVolleyQueue.add(jsonRequest);
    }

    private String getEncodedURL(JSONObject params) {
        String url = "https://api.gfycat.com/v1/gfycats/trending?";

        Iterator<?> keys = params.keys();
        while( keys.hasNext() ) {
            String key = (String)keys.next();
            try {
                url += key + "=" + params.getString(key) + "&";
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return url;
    }
}