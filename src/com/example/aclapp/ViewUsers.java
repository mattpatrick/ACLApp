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

// This activity opens the list where we can view, add and edit profiles 
public class ViewUsers extends Activity {
	
	// Initialize the database myDb
	DBAdapter myDb;
	@Override
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_users);
		
		// Open the database and populate list view with profiles
		openDB();
		populateListView();
		
		// Listen for clicks on items in the list
		registerListClickCallback();
		
		// Button to add a patient
		findViewById(R.id.newPatientButton).setOnClickListener( new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		        //Inform the user the button has been clicked
		    	Toast.makeText(getBaseContext(), "Button1 clicked.", Toast.LENGTH_SHORT).show();
		        Intent intent = new Intent(getBaseContext(), AddUser.class);
		        startActivity(intent);
		    }
		});
	}
	

	// Open myDb
	private void openDB() {
		// TODO Auto-generated method stub
		myDb = new DBAdapter(this);
		myDb.open();
	}
	
	// When the activity is destroyed, close the database
	@Override
	protected void onDestroy(){
		super.onDestroy();
		closeDB();
	}
	
	// To close the database
	private void closeDB() {
		// TODO Auto-generated method stub
		myDb.close();
	}
	
	// Fill the listview with patient profiles
	private void populateListView() {

		Cursor cursor = myDb.getAllRows();
		
		// Allow activity to manage lifetime of the cursor
		// DEPRECATED
		// This is deprecated but is still used due to lack of a replacement method for cursor management
		startManagingCursor(cursor);
		
		// Setup mapping from database to listview item views
		String[] fromFieldNames = new String[]
				{DBAdapter.KEY_AGE, DBAdapter.KEY_IMAGE, DBAdapter.KEY_ROWID, DBAdapter.KEY_HOSPITAL, DBAdapter.KEY_DOCTOR};
		int[] toViewIDs = new int[]
				{R.id.itemAge, R.id.itemSex, R.id.itemId, R.id.itemHospital, R.id.itemDoctor};
		// Create adapter to map columns of database onto element of ui
		SimpleCursorAdapter myCursorAdapter = 
				new SimpleCursorAdapter(
						this,
						R.layout.item_layout,
						cursor,			// Set of db records
						fromFieldNames, // DB column names
						toViewIDs    	// View IDs to put information in
					
				);
		
		// Set the adapter for listview
		ListView myList = (ListView) findViewById(R.id.listView);
		myList.setAdapter(myCursorAdapter);
		
	}
		// Void to register and direct clicks on items in the list view
		private void registerListClickCallback() {
			ListView myList = (ListView) findViewById(R.id.listView);
			myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, 
						View viewClicked,
						int position, 
						long idInDB) {
					
					Cursor cursor = myDb.getRow(idInDB);
					if (cursor.moveToFirst()){
						long idDB = cursor.getLong(DBAdapter.COL_ROWID);
						int age = cursor.getInt(DBAdapter.COL_AGE);
						
						// This string message is only used for troubleshooting
						String message = "ID:" + idDB + "\n"
								+ "Age: " + age;
						
						// Adding user ID to intent for the EditUser activity
						Intent myIntent = new Intent(ViewUsers.this, EditUser.class);
						myIntent.putExtra("Id", idDB); //Optional parameters
						ViewUsers.this.startActivity(myIntent);
//					Toast.makeText(ViewUsers.this, message, Toast.LENGTH_LONG).show();
					}
					cursor.close();
				}
			});
	}
		// This button returns to the home screen
		public void goHome(View view){
			onDestroy();
			Intent myIntent = new Intent(ViewUsers.this, HomeScreen.class);
			ViewUsers.this.startActivity(myIntent);
		}
}