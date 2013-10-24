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


public class SensorGraph extends Activity {
//	ArrayList<Integer> myArrayList = new ArrayList<Integer>();
	List<Integer> myArrayList = new ArrayList<Integer>();
	List<Long>myTimeList = new ArrayList<Long>();
	boolean captureData = false;
	
	private static final String TAG = "SensorGraph";
	
	// change this to your Bluetooth device address 
	private static final String DEVICE_ADDRESS =  "00:06:66:4F:E2:5D"; //"00:06:66:03:73:7B";
	
	
	private GraphView mGraph; 
	private TextView mValueTV;
//	private TextView mRecordState; 
	private ArduinoReceiver arduinoReceiver = new ArduinoReceiver();

	DBAdapter myDb;
//	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        setContentView(R.layout.sensor_graph);
        
        Bundle extras = getIntent().getExtras();
        long idInDB = extras.getLong("Id");
        
    	
    	String message = "ID:" + idInDB;
//    	Toast.makeText(SensorGraph.this, message, Toast.LENGTH_LONG).show();
        
        // get handles to Views defined in our layout file
        mGraph = (GraphView)findViewById(R.id.graph);
        mValueTV = (TextView) findViewById(R.id.value);
//        mRecordState = (TextView)findViewById(R.id.recordstate);
        mGraph.setMaxValue(300);
        
        openDB();
		populateInfo(idInDB);
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
				Long tsLong = System.currentTimeMillis();
//				String ts = tsLong.toString();
				
				if (data != null){
					mValueTV.setText(data);
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
		}
		public void dataStart(View view){
			if(captureData){
				captureData = false;
//				mRecordState.setText("Not Recording");
				dataSave();
			}
			else{
				captureData = true;
//				mRecordState.setText("Recording");
		}
		}
		
		// Saving data to a text file
		public void dataSave(){
			
			 File dir = Environment.getExternalStorageDirectory();
		    
				
				try{
					// Getting the ID that was sent by the activity (Calibrate)
					Bundle extras = getIntent().getExtras();
			        long idInDB = extras.getLong("Id");
					Cursor cursor = myDb.getRow(idInDB);
					Date now = new Date(); // Time stamp for the saved data
					String datetimeStr = now.toString(); // The first thing saved in our text file is the time stamp
					String fileNameStr = datetimeStr +".txt"; // Name the file the timestamp.txt
					
					// Patient ID
					int userId = cursor.getInt(DBAdapter.COL_ROWID);
					String ptID = Integer.toString(userId);
					
					// Patient Age
					int userAge = cursor.getInt(DBAdapter.COL_AGE);
					String ptAge = Integer.toString(userAge);
					
					// Patient Height
					int userHeight = cursor.getInt(DBAdapter.COL_HT);
					String ptHeight = Integer.toString(userHeight);
					
					// Patient Weight
					int userWeight = cursor.getInt(DBAdapter.COL_WT);
					String ptWeight = Integer.toString(userWeight);
					
					// This is creating a new text file, and adding patient info
					FileWriter writer = new FileWriter(new File(dir, fileNameStr));
					writer.append(datetimeStr+"\n");
					writer.append("Patient ID: ");
					writer.append(ptID+"\n");
					writer.append("Patient Age: ");
					writer.append(ptAge+"\n");
					writer.append("Patient Height: ");
					writer.append(ptHeight+"\n");
					writer.append("Patient Weight: ");
					writer.append(ptWeight+"\n");
					
					// This adds all the data saved to the .txt file
					// Every data point is associated with a time stamp
					for (int i=0; i<myArrayList.size() ; i++)
					{
					int var = myArrayList.get(i); // Value of sensor
					Long timeDate = myTimeList.get(i); // Time stamp (long)
					String timeVar = timeDate.toString(); // Time stamp as string
					
					StringBuilder sb = new StringBuilder(); //Stringbuilder for value
					StringBuilder ts = new StringBuilder(); //Stringbuilder for timestamp
					ts.append(""); // Initialize 
					ts.append(timeVar); // Add timestamp
					sb.append(""); // Initialize
					sb.append(var); // Add value
					String strTime = ts.toString(); //Time stamp stringbuilder to string
					
					String strI = sb.toString(); // Value stringbulder to string
					writer.append(strTime+ " " +strI + "\n"); // Add to .txt file
					}
				       writer.flush(); 
				       writer.close();
				       cursor.close(); // Close the cursor
				}
				 catch (IOException ioe) 
			      {ioe.printStackTrace();}
		

				
		}
		
		public void onToggleClicked(View view){
		}
		
		private void openDB() {
			// TODO Auto-generated method stub
			myDb = new DBAdapter(this);
			myDb.open();
		}
		
		@Override
		protected void onDestroy(){
			super.onDestroy();
			closeDB();
		}

		private void closeDB() {
			// TODO Auto-generated method stub
			myDb.close();
		}
		
		// Like in ViewUsers and ChooseUsers, the database is used to populate
		// information about the chosen profile
		private void populateInfo(long id) {
			// This is getting the location of the patient in our database
			Cursor cursor = myDb.getRow(id);
			
			// Patient ID
			int userId = cursor.getInt(DBAdapter.COL_ROWID);
			TextView idView = (TextView) findViewById(R.id.userID);
			idView.setText(Integer.toString(userId));
			
			// Patient Icon
			
			int image = cursor.getInt(DBAdapter.COL_IMAGE);
			ImageView mfView = (ImageView)findViewById(R.id.userImage);
			mfView.setImageResource(image);
			
			// Hospital Name
			String hospital = cursor.getString(DBAdapter.COL_HOSPITAL);
			TextView hospitalView = (TextView)findViewById(R.id.idHospital);
			hospitalView.setText(hospital);
			
			// Hospital Name
			String doctor = cursor.getString(DBAdapter.COL_DOCTOR);
			TextView doctorView = (TextView)findViewById(R.id.idDoctor);
			doctorView.setText(doctor);
			
			// Patient Age
			int userAge = cursor.getInt(DBAdapter.COL_AGE);
			TextView ageView = (TextView) findViewById(R.id.userAge);
			ageView.setText(Integer.toString(userAge));
			
			// Patient Height
			int userHeight = cursor.getInt(DBAdapter.COL_HT);
			TextView heightView = (TextView) findViewById(R.id.userHeight);
			heightView.setText(Integer.toString(userHeight));
			
			// Patient Weight
			int userWeight = cursor.getInt(DBAdapter.COL_WT);
			TextView weightView = (TextView) findViewById(R.id.userWeight);
			weightView.setText(Integer.toString(userWeight));
			
			// Close the cursor
			cursor.close();

		}
		
		// Sometimes the bluetooth connection does not work immediately. This button refreshes the connection
		public void refresh(View view){
			onStop();
			onStart();
		}
		
		// This button returns to the homescreen
		public void goHomeScreen(View view){
			onDestroy();
			Intent myIntent = new Intent(SensorGraph.this, HomeScreen.class);
			SensorGraph.this.startActivity(myIntent);
		}
}



