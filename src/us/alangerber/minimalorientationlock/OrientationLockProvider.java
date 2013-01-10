package us.alangerber.minimalorientationlock;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class OrientationLockProvider extends AppWidgetProvider {
	private static final String TAG = "RotationLockProvider";
	public static final String INTENT_TOGGLE_ORIENTATION_LOCK =
			"us.alangerber.minimalorientationlock.INTENT_TOGGLE_ORIENTATION_LOCK";
	
	/**
	 * Catch incoming orientation-lock intents from the widget; toggle orientation lock if needed.
	 */
	@Override
	public void onReceive(Context ctx, Intent intent) {
        if (intent.getAction().equals(INTENT_TOGGLE_ORIENTATION_LOCK)) {
        	int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
			try {
				//Toggle the orientation lock & set the icon appropriately
				boolean result = toggleOrientationLock(ctx);
				Toast.makeText(ctx, result? R.string.orientation_locked : R.string.orientation_unlocked,
						Toast.LENGTH_SHORT).show();
				setLockIcon(ctx, appWidgetId, result);
			} catch (SettingNotFoundException e) {
				//If the screen orientation setting isn't found, just show an error.
				Toast.makeText(ctx, R.string.orientation_missing, Toast.LENGTH_LONG).show();
				Log.e(TAG, "Cannot access screen orientation setting :(");
			}
		} else {
			super.onReceive(ctx, intent);
		}
	}

	/**
	 * Initialize the widget with the lock set appropriately.
	 */
	@Override
	public void onUpdate(Context ctx, AppWidgetManager awm, int[] widgetIds) {
		//iterate through widget instances
		for (int widgetId : widgetIds) {
			//Initialize a pending widget to tell this provider to lock/unlock the screen orientation setting
			Intent mIntent = new Intent(ctx, OrientationLockProvider.class);
			mIntent.setAction(INTENT_TOGGLE_ORIENTATION_LOCK);
			mIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
			PendingIntent mPendingIntent = PendingIntent.getBroadcast(ctx, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			//Setup the widget UI, checking whether the rotation setting is set already
			RemoteViews mRemoteViews = new RemoteViews(ctx.getPackageName(), R.layout.orientation_lock_button);
			if (getOrientationLock(ctx)) {
				mRemoteViews.setImageViewResource(R.id.lock_button, R.drawable.lock_open);
			}
			mRemoteViews.setOnClickPendingIntent(R.id.lock_button, mPendingIntent);
			
			awm.updateAppWidget(widgetId, mRemoteViews);

		}
		super.onUpdate(ctx, awm, widgetIds);
	}
	
	/**
	 * Toggle the orientation lock setting.
	 */
	private boolean toggleOrientationLock(Context ctx) throws SettingNotFoundException{
		ContentResolver mContentResolver = ctx.getContentResolver();
		if (Settings.System.getInt(mContentResolver, Settings.System.ACCELEROMETER_ROTATION) == 1){
			Settings.System.putInt(mContentResolver, Settings.System.ACCELEROMETER_ROTATION, 0);
			return true;
		} else {
			Settings.System.putInt(mContentResolver, Settings.System.ACCELEROMETER_ROTATION, 1);	
			return false;
		}
	}
	
	/**
	 * Check the orientation lock setting.
	 */
	private boolean getOrientationLock(Context ctx) {
		ContentResolver mContentResolver = ctx.getContentResolver();
		try {
			return Settings.System.getInt(mContentResolver, Settings.System.ACCELEROMETER_ROTATION) == 1;
		} catch (SettingNotFoundException e) {
			return false;
		}
	}
	
	/**
	 * Change the lock icon on the widget.
	 */
	private void setLockIcon(Context ctx, int appWidgetId, boolean locked) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
		RemoteViews mRemoteViews = new RemoteViews(ctx.getPackageName(), R.layout.orientation_lock_button);
		
		if (locked) {
			mRemoteViews.setImageViewResource(R.id.lock_button, R.drawable.lock);
		} else {
			mRemoteViews.setImageViewResource(R.id.lock_button, R.drawable.lock_open);
		}
		
		mgr.updateAppWidget(appWidgetId, mRemoteViews);
	}
}
