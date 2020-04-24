package dp3t.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import dp3t.database.DatabaseOpenHelper;

public class DatabaseHelper {

	public static SQLiteDatabase getWritableDatabase(Context context) {
		return DatabaseOpenHelper.getInstance(context).getWritableDatabase();
	}

}
