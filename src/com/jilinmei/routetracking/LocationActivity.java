package com.jilinmei.routetracking;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LocationActivity extends Activity {
	
	DBAdapter db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		
		//Intent intent = getIntent();
		//double lat = intent.getDoubleExtra("latitude", 1.0);
		//double lon = intent.getDoubleExtra("longitude", 1.0);
		
		ListView listView = (ListView)findViewById(R.id.dataListView);
		ArrayList<String> locationItems = new ArrayList<String>();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_expandable_list_item_1, locationItems);
		listView.setAdapter(adapter);
		
		db = new DBAdapter(this);
		db.open();
		
		Cursor cursor = db.getAllLocations();
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
		{
			double lat = cursor.getDouble(cursor.getColumnIndex(DBAdapter.COLUMN_LATITUDE));
			double lon = cursor.getDouble(cursor.getColumnIndex(DBAdapter.COLUMN_LONGITIDE));
			locationItems.add(lat + ", " + lon);
		}
		//adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.location, menu);
		return true;
	}

}
