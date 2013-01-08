package us.alangerber.minimalorientationlock;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class RotationLockProvider extends AppWidgetProvider {
	private static final String TAG = "RotationLockProvider";

	public void onUpdate(Context ctx, AppWidgetManager awm, int[] widgetIds) {
		//iterate through widget instances
		Log.v(TAG, "Iterating thru widgets");
		for (int widgetId : widgetIds) {
			Log.v(TAG, "Widget: "+widgetId);
			Intent mIntent = new Intent(ctx, OrientationLockActivity.class);
			mIntent.putExtra("us.alangerber.minimalorientationlock.WidgetClicked", true);
			PendingIntent mPendingIntent = PendingIntent.getActivity(ctx, 0, mIntent, 0);
			
			RemoteViews mRemoteViews = new RemoteViews(ctx.getPackageName(), R.layout.rotation_button);
			mRemoteViews.setOnClickPendingIntent(R.id.lock_button, mPendingIntent);
			
			awm.updateAppWidget(widgetId, mRemoteViews);

		}
	}
}
