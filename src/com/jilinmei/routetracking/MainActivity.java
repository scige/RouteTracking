package com.jilinmei.routetracking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.Geometry;
import com.baidu.mapapi.map.Graphic;
import com.baidu.mapapi.map.GraphicsOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.Symbol;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class MainActivity extends Activity {
	
	private static final String DEBUG_TAG = "Debug";
	private static final String EXCEPTION_TAG = "Exception";

	boolean bStatus = true;
	boolean bInitial = true;
	
	BMapManager mBMapMan = null;  
	MapView mMapView = null; 
	MapController mMapController = null;
	
	LocationClient mLocationClient = null;
	BDLocationListener myListener = new MyLocationListener();
	
	//LocationData locData = null;
	LocationData lastLocationData = null;
	MyLocationOverlay myLocationOverlay = null;
	
    GraphicsOverlay mGraphicsOverlay = null;
	
	Button dataButton = null;
	Button clearButton = null;
	Button stopButton = null;
	TextView locationText = null;
	
	DBAdapter db;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
        
        //注意：请在试用setContentView前初始化BMapManager对象，否则会报错  
        mBMapMan=new BMapManager(getApplication());  
        mBMapMan.init("D5242984b7dd7f4edb08e39c2c160b49", null);    // for debug
        //mBMapMan.init("CAa950947866f17f532b3ae66f1e1c46", null);    // for release
        setContentView(R.layout.activity_main);  
        
        mMapView=(MapView)findViewById(R.id.bmapsView);  
        mMapController=mMapView.getController(); 
        mMapView.getController().setZoom(17);
        mMapView.getController().enableClick(true);
        mMapView.setBuiltInZoomControls(true);
        
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener( myListener );
        mLocationClient.setAK("D5242984b7dd7f4edb08e39c2c160b49");
        
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd09ll");	//返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(5000);		//设置发起定位请求的间隔时间为5000ms
        option.disableCache(true);		//禁止启用缓存定位
        //option.setAddrType("all");	//返回的定位结果包含地址信息
        //option.setPoiNumber(5);    	//最多返回POI个数 
        //option.setPoiDistance(1000); 	//poi查询距离 
        //option.setPoiExtraInfo(true); //是否需要POI的电话和地址等详细信息 
        mLocationClient.setLocOption(option);
        mLocationClient.start();
        
		//添加几何图层
        mGraphicsOverlay = new GraphicsOverlay(mMapView);
        mMapView.getOverlays().add(mGraphicsOverlay);
		
        //定位图层初始化
		myLocationOverlay = new MyLocationOverlay(mMapView);
		//设置定位数据
        LocationData locData = new LocationData();
	    myLocationOverlay.setData(locData);
	    //添加定位图层
		mMapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		
		//修改定位数据后刷新图层生效
		mMapView.refresh();
		
		lastLocationData = new LocationData();
        
        GeoPoint point =new GeoPoint((int)(39.915* 1E6),(int)(116.404* 1E6)); 
        mMapController.setCenter(point);
        mMapController.setZoom(17);
        
        locationText = (TextView)findViewById(R.id.locationText);
        dataButton = (Button)findViewById(R.id.locationData);
        clearButton = (Button)findViewById(R.id.clearData);
        stopButton = (Button)findViewById(R.id.stopListener);
        dataButton.setOnClickListener(new DataButtonListener());
        clearButton.setOnClickListener(new ClearButtonListener());
        stopButton.setOnClickListener(new StopButtonListener());
        
        db = new DBAdapter(this);
        db.open();
    }
    
    class DataButtonListener implements OnClickListener
    {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this, LocationActivity.class);
			//intent.putExtra("latitude", locData.latitude);
			//intent.putExtra("longitude", locData.longitude);
			startActivity(intent);
		}
    }
    
    class ClearButtonListener implements OnClickListener
    {
		@Override
		public void onClick(View v) {
			db.removeAllLocations();
		}
    }
    
    class StopButtonListener implements OnClickListener
    {
		@Override
		public void onClick(View v) {
			bStatus = !bStatus;
			if (bStatus) {
				mLocationClient.start();
				locationText.setText("start");
				stopButton.setText("Stop");
			}
			else {
				mLocationClient.stop();
				locationText.setText("stop");
				stopButton.setText("Start");
			}
		}
    }
    
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
        	if (location == null)
        		return;
        	
        	StringBuffer sb = new StringBuffer(256);
        	sb.append("time : ");
        	sb.append(location.getTime());
        	sb.append("\nerror code : ");
        	sb.append(location.getLocType());
        	sb.append("\nlatitude : ");
        	sb.append(location.getLatitude());
        	sb.append("\nlontitude : ");
        	sb.append(location.getLongitude());
        	sb.append("\nradius : ");
        	sb.append(location.getRadius());
        	if (location.getLocType() == BDLocation.TypeGpsLocation){
        		sb.append("\nspeed : ");
        		sb.append(location.getSpeed());
        		sb.append("\nsatellite : ");
        		sb.append(location.getSatelliteNumber());
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
            	sb.append("\naddr : ");
            	sb.append(location.getAddrStr());
            } 
    		Log.v(DEBUG_TAG, sb.toString());
    		
    		Log.d(DEBUG_TAG, "location: " + lastLocationData.latitude + ", "
    							    + lastLocationData.longitude);
        	
        	if (bInitial == false &&
        		lastLocationData.latitude == location.getLatitude() &&
        		lastLocationData.longitude == location.getLongitude())
        		return;
        	
        	float[] results = new float[1];
        	Location.distanceBetween(location.getLatitude(), location.getLongitude(),
        			lastLocationData.latitude, lastLocationData.longitude, results);
    		Log.d(DEBUG_TAG, "distance: " + results[0]);
        	//locationText.setText("Distance: " + results[0]);
        	if (results[0] < 10.0)
        		return;
        	
            //更新定位数据
        	LocationData locData = new LocationData();
            locData.latitude = location.getLatitude();
            locData.longitude = location.getLongitude();
            locData.accuracy = 0;
            locData.direction = location.getDerect();
            myLocationOverlay.setData(locData);
        	
	        double lat = location.getLatitude();
	        double lon = location.getLongitude();
	        GeoPoint point = new GeoPoint((int)(lat * 1E6),(int)(lon * 1E6));  
	        GeoPoint lastPoint = new GeoPoint(
	        		(int)(lastLocationData.latitude * 1E6), 
	        		(int)(lastLocationData.longitude * 1E6));
            
	        //更新路径
	        if (bInitial == false) {
	            Geometry lineGeometry = new Geometry();
	            GeoPoint[] linePoints = new GeoPoint[2];
	            linePoints[0] = lastPoint;
	            linePoints[1] = point;
	            lineGeometry.setPolyLine(linePoints);
	  			Symbol lineSymbol = new Symbol();
	  			Symbol.Color lineColor = lineSymbol.new Color();
	  			lineColor.red = 122;
	  			lineColor.green = 155;
	  			lineColor.blue = 229;
	  			lineColor.alpha = 255;
	  			lineSymbol.setLineSymbol(lineColor, 7);
	  			//生成Graphic对象
	  			Graphic lineGraphic = new Graphic(lineGeometry, lineSymbol);
	            mGraphicsOverlay.setData(lineGraphic);
	        }
            
            //更新图层数据执行刷新后生效
            mMapView.refresh();
            
	        mMapController.setCenter(point);
	        locationText.setText(lat + ", " + lon);
	        
	        //存储到DB中
	        TelephonyManager tm = (TelephonyManager)getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
	        String deviceid = "" + tm.getDeviceId();
	        String serialNumber = "" + tm.getSimSerialNumber();
	        String androidid = "" + Secure.getString(getContentResolver(),Secure.ANDROID_ID);
	        UUID uuidObj = new UUID(androidid.hashCode(), ((long)deviceid.hashCode() << 32) | serialNumber.hashCode());
	        String uuid = uuidObj.toString();
	        db.insertLocation(uuid, deviceid, lat, lon);
	        
	        //发送到服务器
	        Map<String, String> params = new HashMap<String, String>();
	        params.put("uuid", uuid);
	        params.put("deviceid", deviceid);
	        params.put("latitude", String.valueOf(lat));
	        params.put("longitude", String.valueOf(lon));
	        new UploadDataTask().execute("http://api.jilinmei.com:3000/locations/upload",
	        							 composePostData(params));
	        
	        //更新lastLocationData
	        bInitial = false;
            lastLocationData.latitude = location.getLatitude();
            lastLocationData.longitude = location.getLongitude();
            lastLocationData.accuracy = 0;
            lastLocationData.direction = location.getDerect();
        }

		@Override
		public void onReceivePoi(BDLocation arg0) {
			// TODO Auto-generated method stub
			
		}
    }
    
    private String composePostData(Map<String, String> params) {
    	StringBuffer stringBuffer = new StringBuffer();    //存储封装好的请求体信息
        try {
        	for(Map.Entry<String, String> entry : params.entrySet()) {
    	    stringBuffer.append(entry.getKey())
    	                       .append("=")
    	                       .append(entry.getValue())
    	                       .append("&");
    	    }
    	    stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //删除最后的一个"&"
    	} catch (Exception ex) {
    		Log.w(EXCEPTION_TAG, "" + ex.getLocalizedMessage());
    	}
    	return stringBuffer.toString();
    }
    
    private InputStream openHttpConnection(String urlString, String params) throws IOException {
	    InputStream in = null;
    	URL url = new URL(urlString);
    	URLConnection conn = url.openConnection();
    	if (!(conn instanceof HttpURLConnection)) {
    		throw new IOException("Not a Http Connection");
    	}
    	
    	byte[] data = params.getBytes();
    	try {
	    	HttpURLConnection httpConn = (HttpURLConnection)conn;
	    	//httpConn.setAllowUserInteraction(false);
	    	//httpConn.setInstanceFollowRedirects(true);
	    	httpConn.setDoInput(true);
	    	httpConn.setDoOutput(true);
	    	httpConn.setRequestMethod("POST");
	    	httpConn.setUseCaches(false);
	    	httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setRequestProperty("Content-Length", String.valueOf(data.length));
            OutputStream outputStream = httpConn.getOutputStream();
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();
	    	//httpConn.connect();
	    	int response = httpConn.getResponseCode();
	    	if (response == HttpURLConnection.HTTP_OK) {
	    		in = httpConn.getInputStream();
	    	}
    	} catch (Exception ex) {
    		Log.w(EXCEPTION_TAG, "" + ex.getLocalizedMessage());
    		throw new IOException("Error connecting");
    	}
    	
    	return in;
    }
    
    private String uploadLocation(String url, String params) {
    	String result = null;
		InputStream in = null;
		try {
			in = openHttpConnection(url, params);
			//TODO parse response data
			in.close();
		} catch (IOException ex) {
			Log.w(EXCEPTION_TAG, "" + ex.getLocalizedMessage());
		}
		return result;
    }
    
    private class UploadDataTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			return uploadLocation(urls[0], urls[1]);
		}
		
		@Override
		protected void onPostExecute(String result) {
			//TODO update some views
		}
    	
    }
    
    @Override  
    protected void onResume(){  
        mMapView.onResume();  
        if(mBMapMan!=null){  
            mBMapMan.start();  
        }  
       super.onResume();  
    }

    @Override  
    protected void onPause(){  
        mMapView.onPause();  
        if(mBMapMan!=null){  
            mBMapMan.stop();  
        }  
        super.onPause();  
    }
    
    @Override  
    protected void onDestroy(){  
        mMapView.destroy();  
        if(mBMapMan!=null){  
            mBMapMan.destroy();  
            mBMapMan=null;  
        }  
        super.onDestroy();  
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
