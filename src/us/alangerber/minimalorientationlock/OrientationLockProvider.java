package us.alangerber.minimalorientationlock;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.widget.RemoteViews;

public class OrientationLockProvider extends AppWidgetProvider {
	private static final String TAG = "RotationLockProvider";
	private static AccelerometerRotationState rotation_state = new AccelerometerRotationState();
	private static final ComponentName WIDGET_NAME = new ComponentName("us.alangerber.minimalorientationlock",
            "us.alangerber.minimalorientationlock.OrientationLockProvider");

	public static final String INTENT_ROTATION_TOGGLE =
	"us.alangerber.minimalorientationlock.INTENT_ROTATION_TOGGLE";

		
	/*
	 * This class provides local storage of the expected state of the accelerometer setting--
	 * reading/writing the system setting seems to be an expensive call, so avoid it for UI-blocking events
	 */
	private static class AccelerometerRotationState {
		private static final int ROTATION_ON = 1;
		private static final int ROTATION_OFF = 0;
		private static final int UNINITIALIZED = -1;
		
		//don't do a callback to determine the state until needed
		int accelerometer_rotation = UNINITIALIZED;
		
		/** 
		 * Get the system setting for accelerometer rotation
		 * @param ctx the Context for the setting
		 * @return ROTATION_ON, ROTATION_OFF, or UNINITIALIZED if the setting is not available
		 */
		private static int getActualState(Context ctx){
			ContentResolver mContentResolver = ctx.getContentResolver();
			try {
				//The setting is whether rotation is allowed, not whether it's locked, therefore invert
				int system_value = Settings.System.getInt(mContentResolver, Settings.System.ACCELEROMETER_ROTATION);
				if (system_value == ROTATION_ON) {
					return ROTATION_ON;
				} else if  (system_value == ROTATION_OFF) {
					return ROTATION_ON;
				} else {
					return UNINITIALIZED;
				}
					
			} catch (SettingNotFoundException e) {
				return UNINITIALIZED;
			}
		}
		
		/**
		 * Flip the rotation lock setting
		 * @param ctx The context in which to set the rotation lock on/off
		 */
		public void toggleState(Context ctx) {
			int new_state;
			
			if (this.accelerometer_rotation == ROTATION_ON){
				new_state = ROTATION_OFF;
			} else {
				new_state = ROTATION_ON;
			}
			writebackState(ctx, new_state);
			Log.v(TAG, "setting state to " + new_state);
		}
		
		/**
		 * Write the rotation lock setting to the system (in the background, to keep things snappy)
		 * @param ctx The context in which to set the rotation lock on/off
		 * @param new_state ROTATION_ON or ROTATION_OFF
		 */
		public void writebackState(Context ctx, int new_state) {
			accelerometer_rotation = new_state;
			
			//Check for a vaild new_state arg before writeback
			if (new_state != ROTATION_ON && new_state != ROTATION_OFF) {
				return;
			}
			
			//change the setting in an async task, as it can take some time
			final ContentResolver mContentResolver = ctx.getContentResolver();
			new AsyncTask<Integer, Void, Void>() {
                @Override
                protected Void doInBackground(Integer... args) {
        			Settings.System.putInt(mContentResolver, Settings.System.ACCELEROMETER_ROTATION, args[0]);
        			return null;
                }
            }.execute(new_state);
		}
		
		/**
		 * Get the accelerometer rotation state (locally if accessed previously, check against system if needed)
		 * @param ctx The context for the setting
		 * @return ROTATION_ON or ROTATION_OFF
		 */
		public int getState(Context ctx) {
			if (accelerometer_rotation == UNINITIALIZED) {
				accelerometer_rotation = getActualState(ctx);
			}
			return accelerometer_rotation;
		}
	}	
	
	/**
	 * Catch incoming orientation-lock intents from the widget; toggle orientation lock if needed.
	 */
	@Override
	public void onReceive(Context ctx, Intent intent) {
        if (intent.getAction().equals(INTENT_ROTATION_TOGGLE)) {
	    	int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
	                AppWidgetManager.INVALID_APPWIDGET_ID);
			//Toggle the orientation lock & set the icon appropriately
	    	rotation_state.toggleState(ctx);
			updateWidgetViews(ctx, appWidgetId);
		} else {
			super.onReceive(ctx, intent);
		}
	}

	/**
	 * Initialize the widget with the lock set appropriately.
	 */
	@Override
	public void onUpdate(Context ctx, AppWidgetManager awm, int[] widgetIds) {

		//iterate through widget instances & set them up
		for (int widgetId : widgetIds) {
			updateWidgetViews(ctx, widgetId);
		}
		super.onUpdate(ctx, awm, widgetIds);
	}
	
	/**
	 * Rebuild the widget to show the appropriate icon
	 * @param ctx
	 * @param appWidgetId
	 */
	private void updateWidgetViews(Context ctx, int appWidgetId) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        
        //Inflate the layout
		RemoteViews mRemoteViews = new RemoteViews(ctx.getPackageName(), R.layout.orientation_lock_button);
		
		//Show a lock if accelerometer rotation is off; show an open lock if on
		if (rotation_state.getState(ctx) == rotation_state.ROTATION_OFF) {
			mRemoteViews.setImageViewResource(R.id.lock_button, R.drawable.lock_button);
		} else {
			mRemoteViews.setImageViewResource(R.id.lock_button, R.drawable.lock_open_button);
		}
		
		//Initialize a pending widget to tell this provider to lock/unlock the screen orientation setting
		Intent mIntent = new Intent(ctx, OrientationLockProvider.class);
		mIntent.setAction(INTENT_ROTATION_TOGGLE);
		PendingIntent mPendingIntent = PendingIntent.getBroadcast(ctx, 0, mIntent, 0);//no requestcode or flags
		mRemoteViews.setOnClickPendingIntent(R.id.lock_button, mPendingIntent);
		
		//Write the inflate views to all instances of this widget
		mgr.updateAppWidget(WIDGET_NAME, mRemoteViews);
	}
}
