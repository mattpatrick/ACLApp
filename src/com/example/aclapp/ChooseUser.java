package com.example.aclapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ChooseUser extends Activity {
	
	// New DBAdapter myDb
	DBAdapter myDb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_user);
		openDB();
		// This populates the list of users
		populateListView();
		// This opens the calibration activity when a user is chosen
		registerListClickCallback();
		
	}
	
	//Open the database
	private void openDB() {
		myDb = new DBAdapter(this);
		myDb.open();
	}
	
	//When the activity is destroyed, close the database
	@Override
	protected void onDestroy(){
		super.onDestroy();
		closeDB();
	}
	
	private void closeDB() {
		myDb.close();
	}
	
	private void populateListView() {
		// New cursor to manage the database 
		Cursor cursor = myDb.getAllRows();
		
		// Allow activity to manage lifetime of the cursor
		// DEPRECATED 
		startManagingCursor(cursor);
		
		// Setup mapping of database to the activity view, every data input is stored in the array
		String[] fromFieldNames = new String[]
				{DBAdapter.KEY_AGE, DBAdapter.KEY_IMAGE, DBAdapter.KEY_ROWID, DBAdapter.KEY_HOSPITAL, DBAdapter.KEY_DOCTOR};
		int[] toViewIDs = new int[]
				{R.id.itemAge, R.id.itemSex, R.id.itemId, R.id.itemHospital, R.id.itemDoctor};
		
		// Create adapter to map columns of database onto element of ui (this is deprecated)
		SimpleCursorAdapter myCursorAdapter = 
				new SimpleCursorAdapter(
						this,
						R.layout.item_layout,
						cursor,			// Set of db records
						fromFieldNames, // DB column names
						toViewIDs    	// View IDs to put information in
					
				);
		// Set the adapter for listview
		ListView myList = (ListView) findViewById(R.id.chooseList);
		myList.setAdapter(myCursorAdapter);	
	}
		// This is used to open a new activity when a user is selected. Specific user info is then passed to the new activity
		private void registerListClickCallback() {
			// The list of users is set as myList
			ListView myList = (ListView) findViewById(R.id.chooseList);
			// An onItemClickListener is used to receive the selected user 
			myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				
				// When an item is clicked, the user idInDB is taken and used to send info to the new activity via an intent
				@Override
				public void onItemClick(AdapterView<?> parent, 
						View viewClicked,
						int position, 
						long idInDB) {
					
					// The cursor is assigned to the user chosen (by idInDB)
					Cursor cursor = myDb.getRow(idInDB);
					// If there is available data, 
					if (cursor.moveToFirst()){
						long idDB = cursor.getLong(DBAdapter.COL_ROWID);
						int age = cursor.getInt(DBAdapter.COL_AGE);
						
						// Not used
						String message = "ID:" + idDB + "\n"
								+ "Age: " + age;
						
						// Clicking on a user creates a new intent, and sends the user ID to the new activity
						Intent myIntent = new Intent(ChooseUser.this, Calibrate.class);
						myIntent.putExtra("Id", idDB); //This variable will be used to identify the chosen user in the new activity
						ChooseUser.this.startActivity(myIntent); //Start a new activity!
//					Toast.makeText(ViewUsers.this, message, Toast.LENGTH_LONG).show();
					}
					cursor.close(); // We should close the cursor so that there is no resource leak 
				}
			});
			
	
	}
		// This button returns to the home screen
		public void goHome(View view){
			onDestroy();
			Intent myIntent = new Intent(ChooseUser.this, HomeScreen.class);
			ChooseUser.this.startActivity(myIntent);
		}
}