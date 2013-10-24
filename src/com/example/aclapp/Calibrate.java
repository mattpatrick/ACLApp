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

import android.animation.AnimatorListenerAdapter;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ArrayAdapter;
import android.widget.ListView;



import android.R.string;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;
import android.widget.SeekBar;


public class Calibrate extends Activity {
	
	// Initializng the arrays to store values and timestamps for calibration
	List<Integer> myArrayList = new ArrayList<Integer>();
	List<Long>myTimeList = new ArrayList<Long>();
	
	//Boolean for data storage 
	boolean captureData = false;
	
	private static final String TAG = "SensorGraph";
	
	// Bluetooth device address
	private static final String DEVICE_ADDRESS =  "00:06:66:4F:E2:5D";
	
	private GraphView mGraph; // Small visualization screen for data
	private TextView mValueTV; // Value of data from sensor as text
	private Button mButton; 
//	private TextView mRecordState; 
	private ArduinoReceiver arduinoReceiver = new ArduinoReceiver(); 

	private CountDownTimer countDownTimer; // Countdown for calibration
	private boolean timerHasStarted = false; //Boolean for calibration
	private Button startB; //Button for calibration
	public TextView text; //Visualize countdown
	private final long startTime = 3 * 1000; //Start time
	private final long interval = 1 * 1000; //Interval
	
	DBAdapter myDb; // Not used currently

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calibrate);
        
        // Setting up the countdown timer
        text = (TextView) this.findViewById(R.id.timer);
        countDownTimer = new MyCountDownTimer(startTime, interval);
        text.setText(text.getText());
        
        //Getting the intent with user ID 
        Bundle extras = getIntent().getExtras();
        long idInDB = extras.getLong("Id");
    	String message = "ID:" + idInDB;

    	// Graph visualization of sensor data
        mGraph = (GraphView)findViewById(R.id.calibrateGraph);
        mValueTV = (TextView) findViewById(R.id.calibrateValue);
        mGraph.setMaxValue(300);// Max value for graph
        
        openDB(); 

        

    }
    
	@Override
	protected void onStart() {
		super.onStart();

		// In order to receive broadcasted intents we need to register our receiver
		registerReceiver(arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));
		
		// This is how you tell Amarino to connect to a specific BT device from within your own code
		Amarino.connect(this, DEVICE_ADDRESS);
	}


	@Override
	protected void onStop() {
		super.onStop();
		
		// If you connect in onStart() you must not forget to disconnect when your app is closed
		Amarino.disconnect(this, DEVICE_ADDRESS);
		
		// Do not forget to unregister a registered receiver
		unregisterReceiver(arduinoReceiver);

	}
	

	// ArduinoReceiver is responsible for acquiring data from the Arduino via a Bluetooth connection
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

				
				if (data != null){
					mValueTV.setText(data);
					try {
						// since we know that our string value is an int number we can parse it to an integer
						final int sensorReading = Integer.parseInt(data);
						final int timeStamp = Integer.valueOf(tsLong.intValue());
						Date now = new Date();
						String dateTimeStr = now.toString();
						Long dataTimeLong = System.currentTimeMillis();
						mGraph.addDataPoint(sensorReading);// Add data to graph
						//Updating data arrays
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


		public void dataSave(){
			
			 File dir = Environment.getExternalStorageDirectory();
		    
				
				try{
					Bundle extras = getIntent().getExtras();
			        long idInDB = extras.getLong("Id");
					Cursor cursor = myDb.getRow(idInDB);
					Date now = new Date();
					String datetimeStr = now.toString();
					String fileNameStr = datetimeStr +"_CALIBRATE" + ".txt";
					
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
					
					// This writes patient info to the text file
					FileWriter writer = new FileWriter(new File(dir, fileNameStr));
					writer.append("CALIBRATION VALUES");
					writer.append(datetimeStr+"\n");
					writer.append("Patient ID: ");
					writer.append(ptID+"\n");
					writer.append("Patient Age: ");
					writer.append(ptAge+"\n");
					writer.append("Patient Height: ");
					writer.append(ptHeight+"\n");
					writer.append("Patient Weight: ");
					writer.append(ptWeight+"\n");
					
					for (int i=0; i<myArrayList.size() ; i++)
					{
					int var = myArrayList.get(i);
					Long timeDate = myTimeList.get(i);
					String timeVar = timeDate.toString();
					

					
					
					StringBuilder sb = new StringBuilder();
					StringBuilder ts = new StringBuilder();
					ts.append("");
					ts.append(timeVar);
					sb.append("");
					sb.append(var);
					String strTime = ts.toString();
					
					String strI = sb.toString();
					writer.append(strTime+ "|" +strI + "\n");
					}
				       writer.flush();
				       writer.close();
				    // Close the cursor
						cursor.close();
				}
				 catch (IOException ioe) 
			      {ioe.printStackTrace();}
		

				
		}
		

		// The database is used to add patient info to the .txt file
		private void openDB() {
			// TODO Auto-generated method stub
			myDb = new DBAdapter(this);
			myDb.open();
		}
		
		// Close the database when the activity is destroyed
		@Override
		protected void onDestroy(){
			super.onDestroy();
			closeDB();
		}
		
		// Closing the database
		private void closeDB() {
			// TODO Auto-generated method stub
			myDb.close();
		}
		
		// To refresh the connection
		public void calibrateRefresh(View view){
			// if you connect in onStart() you must not forget to disconnect when your app is closed
			onStop();
			onStart();	
		}
		
		// Calibration begins when the button is pushed and countdown starts
		public void calibrateButton(View view){
			if (!timerHasStarted) {
				countDownTimer.start();
				timerHasStarted = true;
				} else {
				countDownTimer.cancel();
				timerHasStarted = false;
				}	
			
		}
		
		// Button to return to home screen
		public void goHome(View view){
			Intent myIntent = new Intent(Calibrate.this, HomeScreen.class);
			Calibrate.this.startActivity(myIntent);
		}
		
		// This defines the countdown timer
		public class MyCountDownTimer extends CountDownTimer {
			public MyCountDownTimer(long startTime, long interval) {
			super(startTime, interval);
			captureData = true;// Data is captured when the countdown starts
			}
			
			// When the countdown has finished
			@Override
			public void onFinish() {
			text.setText("Calibration Successful");
			text.setTextSize(50);
			timerHasStarted = false;
			captureData = false; //Data capture stops on finish
			}
			
			// Every interval change the displayed number
			@Override
			public void onTick(long millisUntilFinished) {
			long sec;
			sec = (millisUntilFinished / 1000) % 60;
			text.setText(""+ sec);
			text.setTextSize(100);
			}
		
			}
		
		// This button goes to the next screen to begin acquisition of data in real time
		public void calibrateStartData (View view){
	        Bundle extras = getIntent().getExtras();
	        long idInDB = extras.getLong("Id"); // Sending the id as an extra
			Intent myIntent = new Intent(Calibrate.this, SensorGraph.class);
			myIntent.putExtra("Id", idInDB); //This variable will be used to identify the chosen user in the new activity
			Calibrate.this.startActivity(myIntent); //Start a new activity!
		}
		

}



