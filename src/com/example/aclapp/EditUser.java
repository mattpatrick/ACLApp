package com.example.aclapp;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

// This activity modifies the AddUser activity to edit existing profiles
public class EditUser extends Activity{
	
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
		setContentView(R.layout.edit_user); //Using the layout edit_user
		openDB(); //This opens DB myDb
		
		 Bundle extras = getIntent().getExtras(); // This gets the ID sent from the selected user 
	     long idInDB = extras.getLong("Id"); //Setting the extra from the intent as a patient ID
	     populateInfo(idInDB); //populateInfo populates the activity with information of the selected user
	}
	
	// This is no longer used
	private void displayText(String message){
		TextView textView = (TextView) findViewById(R.id.editRecordView);
		textView.setText(message);
	}
	
	// Opens the database when activity is created
	private void openDB() {
		// TODO Auto-generated method stub
		myDb = new DBAdapter(this);
		myDb.open();
	}
	
	// Closes database when activity is destroyed
	@Override
	protected void onDestroy(){
		super.onDestroy();
		closeDB();
	}

	// Closes database when called
	private void closeDB() {
		// TODO Auto-generated method stub
		myDb.close();
	}
	
	// This takes input in the form of the radio buttons and textedit forms and enters the new values in the database
	public void editProfile(View view) {
		
		Bundle extras = getIntent().getExtras();
	    long idInDB = extras.getLong("Id");
		
		// Sex info (see AddUser.java) for more detailed info
		radioSexGroup = (RadioGroup) findViewById(R.id.editRadioSex);
		int selectedId = radioSexGroup.getCheckedRadioButtonId();
		radioSexButton = (RadioButton) findViewById(selectedId);
	
		String sexId = radioSexButton.getText().toString();
		displayText(sexId);
		int imageId;
		if( sexId.equals("male")){
			imageId = imageIDs[0];
			}
			else{
			imageId = imageIDs[1];
			}
		
		// Age info
		EditText ageEdit = (EditText)findViewById(R.id.editpt_age);
		int age = Integer.parseInt(ageEdit.getText().toString());
		
		// Height info
		EditText heightEdit = (EditText)findViewById(R.id.editpt_height);
		int height = Integer.parseInt(heightEdit.getText().toString());
	
		// Weight info
		EditText weightEdit = (EditText)findViewById(R.id.editpt_weight);
		int weight = Integer.parseInt(weightEdit.getText().toString());
		
		// Hospital info
		EditText hospitalEdit = (EditText)findViewById(R.id.editHospital);
		String hospital = hospitalEdit.getText().toString();
		
		// Doctor info
		EditText doctorEdit = (EditText)findViewById(R.id.editDoctor);
		String doctor = doctorEdit.getText().toString();
		
		// Insert new row in database
		
		boolean editedRow = myDb.updateRow(idInDB, age, imageId, height, weight, hospital, doctor);
	
		
		Intent myIntent = new Intent(EditUser.this, ViewUsers.class);
		EditUser.this.startActivity(myIntent);
		
	}
	
	// This button returns to the user list without changing any data
	public void returnToList(View view){
		Intent myIntent = new Intent(EditUser.this, ViewUsers.class);
		EditUser.this.startActivity(myIntent);
	}
	
	public void deleteUser(View view){
		Bundle extras = getIntent().getExtras();
	    long idInDB = extras.getLong("Id");
		myDb.deleteRow(idInDB);
		Intent myIntent = new Intent(EditUser.this, ViewUsers.class);
		EditUser.this.startActivity(myIntent);
	}
	
	// This is not used
	public void displayRecord(View view){
		displayText("Clicked Record Show");
		Cursor cursor = myDb.getAllRows();
		displayRecordSet(cursor);
	}

	// Not used
	private void displayRecordSet(Cursor cursor) {
		String message = "Hi!";
		// populate message from cursor
		
		//Reset cursor to start check to see if data
		if (cursor.moveToFirst()){
			do{
			// Process the data:
			int id = cursor.getInt(DBAdapter.COL_ROWID);
			int age = cursor.getInt(DBAdapter.COL_AGE);
			int image = cursor.getInt(DBAdapter.COL_IMAGE);
			message += "id=" + id
					+ ", age" + age
					+ ", image" + image;
			}while(cursor.moveToNext());
			}
		// Close the cursor to avoid a resource leak
		cursor.close();
		displayText(message);
		}
		
	// This fills in the textedit forms with database informaiton for the user
	private void populateInfo(long id) {
		// TODO Auto-generated method stub
		Cursor cursor = myDb.getRow(id);
		
		// Patient Age
		int age = cursor.getInt(DBAdapter.COL_AGE);
		TextView ageView = (TextView) findViewById(R.id.editpt_age);
		ageView.setText(Integer.toString(age));
		
		// Patient Height
		int height = cursor.getInt(DBAdapter.COL_HT);
		TextView heightView = (TextView) findViewById(R.id.editpt_height);
		heightView.setText(Integer.toString(height));
		
		// Patient Weight
		int weight = cursor.getInt(DBAdapter.COL_WT);
		TextView weightView = (TextView) findViewById(R.id.editpt_weight);
		weightView.setText(Integer.toString(weight));
		
		// Hospital
		String hospital = cursor.getString(DBAdapter.COL_HOSPITAL);
		TextView hospitalView = (TextView) findViewById(R.id.editHospital);
		hospitalView.setText(hospital);
		
		// Doctor
		String doctor = cursor.getString(DBAdapter.COL_DOCTOR);
		TextView doctorView = (TextView) findViewById(R.id.editDoctor);
		doctorView.setText(doctor);
		

		// Close the cursor
		cursor.close();

		
	}
	
	
}