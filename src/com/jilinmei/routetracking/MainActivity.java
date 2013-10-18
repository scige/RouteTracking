package com.jilinmei.routetracking;

import android.app.Activity;
import android.location.LocationManager;
import android.os.Bundle;
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
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class MainActivity extends Activity {

	BMapManager mBMapMan = null;  
	MapView mMapView = null; 
	MapController mMapController = null;
	
	Button gpsButton = null;
	Button networkButton = null;
	Button stopButton = null;
	TextView locationText = null;
	LocationManager locationManager = null;
	MyLocationListener myLocationListener = null;
	
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
	
	LocationData locData = null;
	MyLocationOverlay myLocationOverlay = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
        
        mBMapMan=new BMapManager(getApplication());  
        mBMapMan.init("D5242984b7dd7f4edb08e39c2c160b49", null);    // for debug
        //mBMapMan.init("CAa950947866f17f532b3ae66f1e1c46", null);    // for release
        //注意：请在试用setContentView前初始化BMapManager对象，否则会报错  
        setContentView(R.layout.activity_main);  
        
        mMapView=(MapView)findViewById(R.id.bmapsView);  
        mMapController=mMapView.getController(); 
        mMapView.getController().setZoom(14);
        mMapView.getController().enableClick(true);
        mMapView.setBuiltInZoomControls(true);
        
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        mLocationClient.setAK("D5242984b7dd7f4edb08e39c2c160b49");
        
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        //option.setAddrType("all");//返回的定位结果包含地址信息
        option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(1000);//设置发起定位请求的间隔时间为5000ms
        option.disableCache(true);//禁止启用缓存定位
        //option.setPoiNumber(5);    //最多返回POI个数   
        //option.setPoiDistance(1000); //poi查询距离        
        //option.setPoiExtraInfo(true); //是否需要POI的电话和地址等详细信息        
        mLocationClient.setLocOption(option);
        mLocationClient.start();
        
        //定位图层初始化
		myLocationOverlay = new MyLocationOverlay(mMapView);
		//设置定位数据
        locData = new LocationData();
	    myLocationOverlay.setData(locData);
	    //添加定位图层
		mMapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		//修改定位数据后刷新图层生效
		mMapView.refresh();
        
        //GeoPoint point =new GeoPoint((int)(39.915* 1E6),(int)(116.404* 1E6)); 
        //mMapController.setCenter(point);
        //mMapController.setZoom(16);
        
        locationText = (TextView)findViewById(R.id.locationText);
        gpsButton = (Button)findViewById(R.id.useGPSProvider);
        networkButton = (Button)findViewById(R.id.useNetworkProvider);
        stopButton = (Button)findViewById(R.id.stopListener);
        gpsButton.setOnClickListener(new GPSButtonListener());
        networkButton.setOnClickListener(new NetworkButtonListener());
        stopButton.setOnClickListener(new StopButtonListener());
    }
    
    class GPSButtonListener implements OnClickListener
    {
		@Override
		public void onClick(View v) {
			System.out.println("GPSButtonListener::onClick()");
		}
    }
    
    class NetworkButtonListener implements OnClickListener
    {
		@Override
		public void onClick(View v) {
			System.out.println("NetworkButtonListener::onClick()");
		}
    }
    
    class StopButtonListener implements OnClickListener
    {
		@Override
		public void onClick(View v) {
			System.out.println("StopButtonListener::onClick()");
		}
    }
    
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
        	if (location == null)
        		return ;
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
     
        	System.out.println(sb);
        	
	        double lat = location.getLatitude();
	        double lon = location.getLongitude();
	        GeoPoint point = new GeoPoint((int)(lat * 1E6),(int)(lon * 1E6));  
	        mMapController.setCenter(point);
	        mMapController.setZoom(16);
	        locationText.setText(lat + ", " + lon);
	        
            locData.latitude = location.getLatitude();
            locData.longitude = location.getLongitude();
            //如果不显示定位精度圈，将accuracy赋值为0即可
            locData.accuracy = location.getRadius();
            locData.direction = location.getDerect();
            //更新定位数据
            myLocationOverlay.setData(locData);
            //更新图层数据执行刷新后生效
            mMapView.refresh();
        }

		@Override
		public void onReceivePoi(BDLocation arg0) {
			// TODO Auto-generated method stub
			
		}
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
    protected void onPause(){  
        mMapView.onPause();  
        if(mBMapMan!=null){  
               mBMapMan.stop();  
        }  
        super.onPause();  
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
