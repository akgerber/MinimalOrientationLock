package us.alangerber.minimalorientationlock;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class OrientationLockActivity extends Activity {
	private static final String TAG="OrientationLockActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_orientation_lock);
		final Button mButton = (Button)findViewById(R.id.button_id);
		mButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					boolean result = toggleScreenOrientationLock();
					Toast.makeText(getApplicationContext(), "HEY, locked =" + result, Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "FAIL", Toast.LENGTH_LONG).show();					
				}
				finish();
				
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Intent mIntent = getIntent();
		boolean extra = mIntent.getBooleanExtra("us.alangerber.minimalorientationlock.WidgetClicked", false);
		Log.v(TAG, "intent extra = " + (extra?"true":"false"));
		if (mIntent.getBooleanExtra("us.alangerber.minimalorientationlock.WidgetClicked", false)) {
			try {
				boolean result = toggleScreenOrientationLock();
				Toast.makeText(getApplicationContext(), "HEY, locked =" + result, Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "FAIL", Toast.LENGTH_LONG).show();					
			}
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_orientation_lock, menu);
		return true;
	}
	
	public boolean toggleScreenOrientationLock() throws SettingNotFoundException{
		ContentResolver mContentResolver = this.getContentResolver();
		if (Settings.System.getInt(mContentResolver, Settings.System.ACCELEROMETER_ROTATION) == 1){
			Settings.System.putInt(mContentResolver, Settings.System.ACCELEROMETER_ROTATION, 0);
			return true;
		} else {
			Settings.System.putInt(mContentResolver, Settings.System.ACCELEROMETER_ROTATION, 1);	
			return false;
		}
	}

}
