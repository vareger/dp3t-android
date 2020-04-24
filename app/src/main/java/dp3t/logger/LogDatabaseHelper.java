package dp3t.logger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import dp3t.database.DatabaseHelper;

public class LogDatabaseHelper {

	public static void copyLogDatabase(Context context) {
		SQLiteDatabase database = DatabaseHelper.getWritableDatabase(context);
		database.beginTransaction();
		database.execSQL("drop table if exists " + LogDatabase.LogSpec.TABLE_NAME);
		LogDatabase.LogDatabaseHelper.executeCreate(database);
		for (LogEntry logEntry : Logger.getLogs(0)) {
			LogDatabase
					.insert(database, logEntry.getLevel().getKey(), logEntry.getTag(), logEntry.getMessage(), logEntry.getTime());
		}
		database.setTransactionSuccessful();
		database.endTransaction();
	}

}
