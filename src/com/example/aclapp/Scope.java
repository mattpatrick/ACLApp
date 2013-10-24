package com.example.aclapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;



import android.R.string;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;


public class Scope extends Activity {
//	ArrayList<Integer> myArrayList = new ArrayList<Integer>();
	List<Integer> myArrayList = new ArrayList<Integer>();
	List<Long>myTimeList = new ArrayList<Long>();
	boolean captureData = false;
	String data_peak = Integer.toString(0);
	private static final String TAG = "SensorGraph";
	
	// change this to your Bluetooth device address 
	private static final String DEVICE_ADDRESS =  "00:06:66:4F:E2:5D"; 
	
	
	private GraphView mGraph; 
	private TextView mValueTV;
//	private TextView mRecordState; 
	private ArduinoReceiver arduinoReceiver = new ArduinoReceiver();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        setContentView(R.layout.scope);
        
        // get handles to Views defined in our layout file
        mGraph = (GraphView)findViewById(R.id.scope);
        mValueTV = (TextView) findViewById(R.id.scopeValue);
//        mRecordState = (TextView)findViewById(R.id.recordstate);
        mGraph.setMaxValue(1024);
    }
    
	@Override
	protected void onStart() {
		super.onStart();

		// in order to receive broadcasted intents we need to register our receiver
		registerReceiver(arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));
		
		// this is how you tell Amarino to connect to a specific BT device from within your own code
		Amarino.connect(this, DEVICE_ADDRESS);
	}


	@Override
	protected void onStop() {
		super.onStop();
		
		// if you connect in onStart() you must not forget to disconnect when your app is closed
		Amarino.disconnect(this, DEVICE_ADDRESS);
		
		// do never forget to unregister a registered receiver
		unregisterReceiver(arduinoReceiver);
	}
	

	/**
	 * ArduinoReceiver is responsible for catching broadcasted Amarino
	 * events.
	 * 
	 * It extracts data from the intent and updates the graph accordingly.
	 */
	public class ArduinoReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String data = null;
			
		
			// the device address from which the data was sent, we don't need it here but to demonstrate how you retrieve it
			final String address = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
			
			// the type of data which is added to the intent
			final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);
			
			// Amarino tool box only accepts strings, so check that incoming data is a string
			if (dataType == AmarinoIntent.STRING_EXTRA){
				data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
				int data_int = Integer.parseInt(data);
				int peak_int = Integer.parseInt(data_peak); 
				
				if (data_int >= peak_int){
				data_peak = data;
				}
				
				}
				Long tsLong = System.currentTimeMillis();
//				String ts = tsLong.toString();
				
				if (data != null){
					mValueTV.setText(data_peak);
					try {
						// since we know that our string value is an int number we can parse it to an integer
						final int sensorReading = Integer.parseInt(data);
						final int timeStamp = Integer.valueOf(tsLong.intValue());
						Date now = new Date();
						String dateTimeStr = now.toString();
						Long dataTimeLong = System.currentTimeMillis();
						mGraph.addDataPoint(sensorReading);
						if (captureData){
							myArrayList.add(sensorReading);

							myTimeList.add(dataTimeLong);
							} 
						}	
					catch (NumberFormatException e) { /* oh data was not an integer */ }
					
					}
				}
			}
		
	
		
		

		
		public void refresh(View view){
			// if you connect in onStart() you must not forget to disconnect when your app is closed
			onStop();
			onStart();
			
		}
		
		public void goHome(View view){
			
			Intent myIntent = new Intent(Scope.this, HomeScreen.class);
			Scope.this.startActivity(myIntent);
		}
		public void peak(View view){
			data_peak = Integer.toString(0);
		}
}



