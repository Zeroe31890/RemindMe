package com.remindme;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.CameraManager;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";
	private static final int RUN_CAMERA = 0;
	private RelativeLayout cardParent;
	private ImageView mImageView;
	private String rememberItem;	
	FileObserver observer;
	private Timer timer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		cardParent = (RelativeLayout)findViewById(R.id.reminder_card_holder);
		mImageView = (ImageView)findViewById(R.id.image_to_remember);
		ArrayList<String> voiceResults = getIntent().getExtras()
		        .getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
		rememberItem = voiceResults.get(0);
		
		fireReminderPicture();

	}//end of onCreate
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Log.d(TAG, "Result code is: "+resultCode);
		
		if(resultCode == RESULT_OK) {
			
			if(requestCode == RUN_CAMERA) {
				RemindMeDatabase db = new RemindMeDatabase(this);
				//db.deleteItems();
				db.closeDatabase();
				
				String filePath = data.getStringExtra(CameraManager.EXTRA_PICTURE_FILE_PATH);
				
				Log.d(TAG, "Successfully got filePath: "+ filePath);
				
				Intent intent = new Intent(this, TestService.class);
				intent.putExtra("pic_file_path", filePath);
				intent.putExtra("remember_item", rememberItem);
				startService(intent);
				
				
				Card card = new Card(this);
				card.setText("Remembering: "+rememberItem);
				setContentView(card.toView());
				
				// Unfortunately there is no way to reliably assume we can access the image
				// right after the photo is taken. This will be done by the service.
				timer = new Timer();
				timer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						MainActivity.this.finish();
						
					}
				}, 5000);
				
				
			}			
			
		}//end of check for RESULT_OK
		else {
			Log.d(TAG, "Oh no! Result code from Camera Intent returned not ok! The code is: "+ resultCode);
		}
		
		
	}//end of onActivityResult
	
	private void fireReminderPicture() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);      
        startActivityForResult(cameraIntent, RUN_CAMERA);
		
	}//end of fireReminderPicture

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("MainActivity", "onDestory is running.");
		if(observer != null)
			observer.stopWatching();
	}
	
}//end of class
