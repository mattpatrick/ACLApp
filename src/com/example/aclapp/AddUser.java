package com.example.aclapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


public class AddUser extends Activity{

	// Defining the radio buttons for patient gender
	private RadioGroup radioSexGroup;
	private RadioButton radioSexButton;
	
	// This is an array of the image IDs, so we can reference them later
	int[] imageIDs ={
			R.drawable.male,
			R.drawable.female
	};
	
	// This creates an adapter to edit the database of pt info
	DBAdapter myDb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_user); //Using the layout add_user
		openDB(); //This opens DB myDb
	}
	
	//  This is only used for testing database function
	private void displayText(String message){
		TextView textView = (TextView) findViewById(R.id.recordView);
		textView.setText(message);
	}
	
	// Initializing the database
	private void openDB() {
		myDb = new DBAdapter(this);
		myDb.open();
	}
	
	// Database has to be closed every time this activity is destroyed
	@Override
	protected void onDestroy(){
		super.onDestroy();
		closeDB();
	}
	
	// Function to close DB
	private void closeDB() {
		// TODO Auto-generated method stub
		myDb.close();
	}
	
	
	public void addProfile(View view) {
		
		// Get the patient gender from the radio button selected
		radioSexGroup = (RadioGroup) findViewById(R.id.radioSex);
		int selectedId = radioSexGroup.getCheckedRadioButtonId();
		radioSexButton = (RadioButton) findViewById(selectedId);
		
		// Store gender as a string to be used in to store the patient gender
		String sexId = radioSexButton.getText().toString();
		displayText(sexId); // No longer used
		
		// Store the chosen gender as an int (the image id)
		int imageId;
		if( sexId.equals("male")){
			imageId = imageIDs[0];
			}
			else{
			imageId = imageIDs[1];
			}
		
		// Get entered data from other forms
		// Age info
		EditText ageEdit = (EditText)findViewById(R.id.pt_age);
		int age = Integer.parseInt(ageEdit.getText().toString());
		
		// Height info
		EditText heightEdit = (EditText)findViewById(R.id.pt_height);
		int height = Integer.parseInt(heightEdit.getText().toString());
	
		// Weight info
		EditText weightEdit = (EditText)findViewById(R.id.pt_weight);
		int weight = Integer.parseInt(weightEdit.getText().toString());
		
		// Hospital info
		EditText hospitalEdit = (EditText)findViewById(R.id.hospital);
		String hospital = hospitalEdit.getText().toString();
		
		// Doctor info
		EditText doctorEdit = (EditText)findViewById(R.id.doctor);
		String doctor = doctorEdit.getText().toString();
		
		// Insert new row in database
		long newId = myDb.insertRow(age,imageId, height, weight, hospital, doctor);
		
		// This intent returns to the activity ViewUsers, to view the list of users
		Intent myIntent = new Intent(AddUser.this, ViewUsers.class);
		AddUser.this.startActivity(myIntent);
	}
	
	// The cancel button returns to the list of Users
	public void cancel(View view){
		Intent myIntent = new Intent(AddUser.this, ViewUsers.class);
		AddUser.this.startActivity(myIntent);
	}
	
	
	
		
		
	
	
}