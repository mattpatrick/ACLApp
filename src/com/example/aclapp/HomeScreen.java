package com.example.aclapp;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class HomeScreen extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_screen);
		
		// Using onClick to receive button input to start a new study
		// The 'buttons' here are relativeViews defined as R.id.button1 - button3
		findViewById(R.id.button1).setOnClickListener( new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		        Intent intent = new Intent(getBaseContext(), ChooseUser.class);
		        startActivity(intent);
		    }
		});
		
		// Using onClick to receive button input to start a new study
		findViewById(R.id.button2).setOnClickListener( new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		        Intent intent = new Intent(getBaseContext(), ViewUsers.class);
		        startActivity(intent);
		    }
		});
		
		// Using onClick to receive button input to start a new study
		findViewById(R.id.button3).setOnClickListener( new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {;
		        Intent intent = new Intent(getBaseContext(), Scope.class);
		        startActivity(intent);
		    }
		});	
	}


	    
	    
}

