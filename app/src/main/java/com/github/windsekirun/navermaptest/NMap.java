package com.github.windsekirun.navermaptest;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nhn.android.maps.NMapCompassManager;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapCalloutOverlay;
import com.nhn.android.mapviewer.overlay.NMapMyLocationOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * NMap
 * Created by DongGilSeo on 2017-01-20.
 */

public class NMap extends RelativeLayout implements NMapView.OnMapStateChangeListener, NMapPOIdataOverlay.OnStateChangeListener, NMapOverlayManager.OnCalloutOverlayListener {
    private static final String NAVER_CLIENT_ID = "woR7sft4ZoNQBs7els0c";
    private static final String LOGTAG = "NAVERMAPTEST";
    public static final double DEFAULT_LATITUDE = 37.505577;
    public static final double DEFAULT_LONGITUDE = 127.025545;
    private double lat = DEFAULT_LATITUDE;
    private double lng = DEFAULT_LONGITUDE;
    private String mCateCode;

    private NMapView mMapView;
    private NMapController mMapController;
    private NMapViewerResourceProvider mMapViewerResourceProvider;
    private NMapOverlayManager mMapOverlayManager;
    private NMapPOIdata mPOIData;
    private NMapPOIdataOverlay mMapPOIdataOverlay;
    private int markerId = NMapPOIflagType.PIN;
    private NMapCompassManager mMapCompassManager;
    private NMapLocationManager mMapLocationManager;
    private NMapMyLocationOverlay mMyLocationOverlay;

    private LinearLayout mBaseSearch;
    private TextView mBtnMyPlace;
    private EditText mEdtSearch;
    private ImageButton mBtnSearch;
    private ImageButton mBtnExpand;

    private OnMapExpandListener mMapExpandListener;
    private OnMapReloadListener mMapReloadListener;
    private boolean mEnableMarkerClick;
    private OnMapInitListener mMapInitListener;

    private ArrayList<NMapMarker> markers = new ArrayList<NMapMarker>();
    private NMapMarker myLocationMarker;

    public NMap(Context context) {
        super(context);
    }

    public NMap(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NMap(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NMap(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBaseSearch = (LinearLayout) findViewById(R.id.baseSearch);
        mBtnMyPlace = (TextView) findViewById(R.id.btnMyPlace);
        mEdtSearch = (EditText) findViewById(R.id.edtSearch);
        mBtnSearch = (ImageButton) findViewById(R.id.btnSearch);
        mBtnExpand = (ImageButton) findViewById(R.id.btnExpand);
        mMapView = (NMapView) findViewById(R.id.mapView);

        initMap();
        listener();
    }

    protected void initMap() {
        mMapView.setClientId(NAVER_CLIENT_ID);
        mMapView.setClickable(true);
        mMapView.setEnabled(true);
        mMapView.setFocusable(true);
        mMapView.setFocusableInTouchMode(true);
        mMapView.requestFocus();

        mMapView.setOnMapStateChangeListener(this);
        mMapView.setScalingFactor(2.0f, true);
        mMapController = mMapView.getMapController();

        mMapViewerResourceProvider = new NMapViewerResourceProvider(getContext());
        mMapOverlayManager = new NMapOverlayManager(getContext(), mMapView, mMapViewerResourceProvider);

        mPOIData = new NMapPOIdata(11000, mMapViewerResourceProvider);

        mMapController.setMapCenter(new NGeoPoint(DEFAULT_LONGITUDE, DEFAULT_LATITUDE), 12);

        mMapCompassManager = new NMapCompassManager((Activity) getContext());
        mMapLocationManager = new NMapLocationManager(getContext());
        mMapLocationManager.enableMyLocation(true);
        mMyLocationOverlay = mMapOverlayManager.createMyLocationOverlay(mMapLocationManager, mMapCompassManager);

        mMapController.setMapCenter(new NGeoPoint(DEFAULT_LONGITUDE, DEFAULT_LATITUDE), 11);

        // TEST DATA MARKER DATA!! ----- START ------
        try {
            JSONObject jsonObject = MapVirtualData.getJSONArray();
            JSONArray array = jsonObject.getJSONArray("entity");
            for (int i = array.length() - 1; i >= 0; i--) {
                JSONObject json = array.getJSONObject(i);
                addMarker(new NMapMarker(json.getDouble("latitude"), json.getDouble("longitude"), json.getString("name")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // TEST DATA MARKER DATA!! ----- END ------
    }

    private void searchMap() {
        if (mMapReloadListener != null) {
            mMapReloadListener.onReload(1, mEdtSearch.getText().toString());
        }
    }

    public void setCate(boolean show, String code) {
        if (code != null)
            mCateCode = code;

        mBtnExpand.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setSearch(boolean show) {
        mBaseSearch.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void listener() {
        /*
        mBtnMyPlace.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertMyLocation location = new AlertMyLocation();
                location.setOnCloseListener(new OnCloseListener() {
                    @Override
                    public void onClose(DialogInterface dialog, int which, Object data) {
                        dialog.dismiss();

                        if(which == Alert.BUTTON1) {
                            clearAllMarker();

                            JSONObject locationObj = _app.getCurrentLocation();

                            double lat = Json.Obj.getDouble(locationObj, Constants.KEY_LATITUDE);
                            double lng = Json.Obj.getDouble(locationObj, Constants.KEY_LONGITUDE);

                            NGeoPoint location = new NGeoPoint(lng, lat);

                            // mMapController.animateTo(location);
                            mMapController.setMapCenter(location);

                            if(mMapReloadListener != null)
                                mMapReloadListener.onReload(2, location);
                        }
                    }
                });

                location.show(getContext());
            }
        });
        */

        mBtnExpand.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMapExpandListener != null) {
                    mMapExpandListener.onExpand(1, null);
                }
            }
        });

        mEdtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,KeyEvent event) {
                searchMap();
                return false;
            }
        });

        mBtnSearch.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                searchMap();
            }
        });
    }

    public void addMarker(NMapMarker marker) {
        mPOIData.beginPOIdata(1);
        mPOIData.addPOIitem(marker.getPoint(), marker.getTitle(), markerId, marker.getSnippet(), marker.getId());
        mPOIData.endPOIdata();

        markers.add(marker);
    }

    public void addMarker(double lng, double lat, @DrawableRes int markerRes) {
        NMapMarker marker = new NMapMarker(lat, lng, "위치");
        //marker.setMarker(ContextCompat.getDrawable(getContext(), markerRes));
        addMarker(marker);
    }

    public void addMarkers(ArrayList<NMapMarker> markerArrayList) {
        for (NMapMarker marker : markerArrayList) {
            addMarker(marker);
        }
    }

    /*
    public void addMarkers(boolean clear, JSONArray array) {
        ArrayList<NMapMarker> markers = new ArrayList<NMap.NMapMarker>();
        if (clear)
            clearAllMarker();

        for (int i = 0; i < array.length(); i++) {
            JSONObject json = Json.Arr.getJSONObject(array, i);
            NMapMarker marker = new NMapMarker(
                    Json.Obj.getDouble(json, Constants.KEY_LONGITUDE, 0.0),
                    Json.Obj.getDouble(json, Constants.KEY_LATITUDE, 0.0),
                    Json.Obj.getString(json,  "name"));
            marker.setSnippet(json.toString()).setId(i);

            boolean isPromotion = Constants.SHOP_PROMOTION.equalsIgnoreCase(Json.Obj.getString(json, "promotion"));
            marker.setMarker(ContextCompat.getDrawable(getContext(), isPromotion ? MARKER_RED : MARKER_GRAY));

            markers.add(marker);
        }

        if (markers != null && !markers.isEmpty())
            addMarkers(markers);
    }
    */

    public void clearAllMarker() {
        if (mMapPOIdataOverlay != null)
            mMapPOIdataOverlay.removeAllPOIdata();
    }

    public String getSearchStr() {
        return mEdtSearch.getText().toString();
    }

    public void setOnMapExpandListener(OnMapExpandListener mapExpandListener) {
        this.mMapExpandListener = mapExpandListener;
    }

    public void setOnMapReloadListener(OnMapReloadListener mapReloadListener) {
        this.mMapReloadListener = mapReloadListener;
    }

    public void setEnableMarkerClick(boolean enable) {
        mEnableMarkerClick = enable;
    }

    public void setOnMapInitListener(OnMapInitListener listener) {
        this.mMapInitListener = listener;
    }

    @Override
    public NMapCalloutOverlay onCreateCalloutOverlay(NMapOverlay nMapOverlay, NMapOverlayItem nMapOverlayItem, Rect rect) {
        return null;
    }

    @Override
    public void onFocusChanged(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {

    }

    @Override
    public void onCalloutClick(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {

    }

    @Override
    public void onMapInitHandler(NMapView nMapView, NMapError nMapError) {

    }

    @Override
    public void onMapCenterChange(NMapView nMapView, NGeoPoint nGeoPoint) {

    }

    @Override
    public void onMapCenterChangeFine(NMapView nMapView) {

    }

    @Override
    public void onZoomLevelChange(NMapView nMapView, int i) {

    }

    @Override
    public void onAnimationStateChange(NMapView nMapView, int i, int i1) {

    }

    public interface OnMapExpandListener {
        void onExpand(int which, Object data);
    }

    public interface OnMapReloadListener {
        void onReload(int which, Object data);
    }

    public interface OnMapInitListener {
        void onInit(NMapView arg0, NMapError arg1);
    }


    public class NMapMarker implements Serializable {
        private static final long serialVersionUID = 5294335241519103532L;
        private int id = 0;
        private double latitude = 0.0;
        private double longitude = 0.0;
        private String title = "";
        private String snippet = "";
        private Drawable marker = null;
        private NGeoPoint point = null;

        public NMapMarker(double lat, double lng, String title) {
            setLatitude(lat);
            setLongitude(lng);
            setTitle(title);
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public Drawable getMarker() {
            return marker;
        }

        public int getId() {
            return id;
        }

        public NGeoPoint getPoint() {
            return point;
        }

        public String getSnippet() {
            return snippet;
        }

        public String getTitle() {
            return title;
        }

        public NMapMarker setId(int id) {
            this.id = id;
            return this;
        }

        public NMapMarker setLatitude(double latitude) {
            this.latitude = latitude;
            settingPoint();
            return this;
        }

        public NMapMarker setLongitude(double longitude) {
            this.longitude = longitude;
            settingPoint();
            return this;
        }

        public NMapMarker setMarker(Drawable marker) {
            this.marker = marker;
            return this;
        }

        public NMapMarker setSnippet(String snippet) {
            this.snippet = snippet;
            return this;
        }

        public NMapMarker setTitle(String title) {
            this.title = title;
            return this;
        }

        private void settingPoint() {
            if (getLatitude() != 0.0 && getLongitude() != 0.0) {
                point = new NGeoPoint(getLongitude(), getLatitude());
            }
        }
    }
}