/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package dp3t.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import dp3t.crypto.ContactsFactory;
import dp3t.crypto.CryptoModule;
import dp3t.crypto.EphId;
import dp3t.internal.BroadcastHelper;
import dp3t.models.Contact;
import dp3t.models.Handshake;
import dp3t.models.KnownCase;
import dp3t.models.MatchedContact;
import dp3t.util.DayDate;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;

public class Database {

	private DatabaseOpenHelper databaseOpenHelper;
	private DatabaseThread databaseThread;

	public Database(@NonNull Context context) {
		databaseOpenHelper = DatabaseOpenHelper.getInstance(context);
		databaseThread = DatabaseThread.getInstance(context);
	}

	public void addKnownCase(Context context, @NonNull byte[] key, long onsetDate, long bucketTime) {
		SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KnownCases.KEY, key);
		values.put(KnownCases.ONSET, onsetDate);
		values.put(KnownCases.BUCKET_TIME, bucketTime);
		databaseThread.post(() -> {
			long idOfAddedCase = db.insertWithOnConflict(KnownCases.TABLE_NAME, null, values, CONFLICT_IGNORE);
			if (idOfAddedCase == -1) {
				//key was already in the database, so we can ignore it
				return;
			}

			int[] windowCount = new int[] { 0 };
			CryptoModule cryptoModule = CryptoModule.getInstance(context);
			cryptoModule.checkContacts(key, onsetDate, bucketTime, this::getContacts, (contact) -> {
				ContentValues updateValues = new ContentValues();
				updateValues.put(Contacts.ASSOCIATED_KNOWN_CASE, idOfAddedCase);
				db.update(Contacts.TABLE_NAME, updateValues, Contacts.ID + "=" + contact.getId(), null);
				windowCount[0] += contact.getWindowCount();
			});

			if (windowCount[0] > ContactsFactory.NUMBER_OF_WINDOWS_FOR_EXPOSURE) {
				ContentValues insertValues = new ContentValues();
				insertValues.put(MatchedContacts.REPORT_DATE, bucketTime);
				insertValues.put(MatchedContacts.ASSOCIATED_KNOWN_CASE, idOfAddedCase);
				db.insert(MatchedContacts.TABLE_NAME, null, insertValues);
				BroadcastHelper.sendUpdateBroadcast(context);
			}
		});
	}

	public void removeOldKnownCases() {
		databaseThread.post(() -> {
			SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
			DayDate lastDayToKeep = new DayDate().subtractDays(CryptoModule.NUMBER_OF_DAYS_TO_KEEP_DATA);
			db.delete(KnownCases.TABLE_NAME, KnownCases.BUCKET_TIME + " < ?",
					new String[] { Long.toString(lastDayToKeep.getStartOfDayTimestamp()) });
			DayDate lastDayToKeepMatchedContacts =
					new DayDate().subtractDays(CryptoModule.NUMBER_OF_DAYS_TO_KEEP_MATCHED_CONTACTS);
			db.delete(MatchedContacts.TABLE_NAME, MatchedContacts.REPORT_DATE + " < ?",
					new String[] { Long.toString(lastDayToKeepMatchedContacts.getStartOfDayTimestamp()) });
		});
	}

	public ContentValues addHandshake(Context context, Handshake handshake) {
		SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(Handshakes.EPHID, handshake.getEphId().getData());
		values.put(Handshakes.TIMESTAMP, handshake.getTimestamp());
		values.put(Handshakes.TX_POWER_LEVEL, handshake.getTxPowerLevel());
		values.put(Handshakes.RSSI, handshake.getRssi());
		values.put(Handshakes.PHY_PRIMARY, handshake.getPrimaryPhy());
		values.put(Handshakes.PHY_SECONDARY, handshake.getSecondaryPhy());
		values.put(Handshakes.TIMESTAMP_NANOS, handshake.getTimestampNanos());
		databaseThread.post(() -> {
			db.insert(Handshakes.TABLE_NAME, null, values);
			BroadcastHelper.sendUpdateBroadcast(context);
		});
		return values;
	}

	public List<Handshake> getHandshakes() {
		SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();
		Cursor cursor = db.query(Handshakes.TABLE_NAME, Handshakes.PROJECTION, null, null, null, null, Handshakes.ID);
		return getHandshakesFromCursor(cursor);
	}


	public List<Handshake> getHandshakes(long maxTime) {
		SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();
		Cursor cursor = db.query(Handshakes.TABLE_NAME, Handshakes.PROJECTION, Handshakes.TIMESTAMP + " < ?",
				new String[] { "" + maxTime }, null, null, Handshakes.ID);
		return getHandshakesFromCursor(cursor);
	}

	public void getHandshakes(@NonNull ResultListener<List<Handshake>> resultListener) {
		databaseThread.post(new Runnable() {
			List<Handshake> handshakes = new ArrayList<>();

			@Override
			public void run() {
				handshakes = getHandshakes();
				databaseThread.onResult(() -> resultListener.onResult(handshakes));
			}
		});
	}

	private List<Handshake> getHandshakesFromCursor(Cursor cursor) {
		List<Handshake> handshakes = new ArrayList<>();
		while (cursor.moveToNext()) {
			int id = cursor.getInt(cursor.getColumnIndexOrThrow(Handshakes.ID));
			long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(Handshakes.TIMESTAMP));
			EphId ephId = new EphId(cursor.getBlob(cursor.getColumnIndexOrThrow(Handshakes.EPHID)));
			int txPowerLevel = cursor.getInt(cursor.getColumnIndexOrThrow(Handshakes.TX_POWER_LEVEL));
			int rssi = cursor.getInt(cursor.getColumnIndexOrThrow(Handshakes.RSSI));
			String primaryPhy = cursor.getString(cursor.getColumnIndexOrThrow(Handshakes.PHY_PRIMARY));
			String secondaryPhy = cursor.getString(cursor.getColumnIndexOrThrow(Handshakes.PHY_SECONDARY));
			long timestampNanos = cursor.getLong(cursor.getColumnIndexOrThrow(Handshakes.TIMESTAMP_NANOS));
			Handshake handShake = new Handshake(id, timestamp, ephId, txPowerLevel, rssi, primaryPhy, secondaryPhy,
					timestampNanos);
			handshakes.add(handShake);
		}
		cursor.close();
		return handshakes;
	}

	public void generateContactsFromHandshakes(Context context) {
		databaseThread.post(() -> {

			long currentEpochStart = CryptoModule.getInstance(context).getCurrentEpochStart();

			List<Handshake> handshakes = getHandshakes(currentEpochStart);
			List<Contact> contacts = ContactsFactory.mergeHandshakesToContacts(handshakes);
			for (Contact contact : contacts) {
				addContact(contact);
			}

			SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
			DayDate lastDayToKeep = new DayDate().subtractDays(CryptoModule.NUMBER_OF_DAYS_TO_KEEP_DATA);
			db.delete(Contacts.TABLE_NAME, Contacts.DATE + " < ?",
					new String[] { Long.toString(lastDayToKeep.getStartOfDayTimestamp()) });
		});
	}

	private void addContact(Contact contact) {
		SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(Contacts.EPHID, contact.getEphId().getData());
		values.put(Contacts.DATE, contact.getDate());
		values.put(Contacts.WINDOW_COUNT, contact.getWindowCount());
		db.insertWithOnConflict(Contacts.TABLE_NAME, null, values, CONFLICT_IGNORE);
	}

	public List<Contact> getContacts() {
		SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();
		Cursor cursor = db
				.query(Contacts.TABLE_NAME, Contacts.PROJECTION, null, null, null, null, Contacts.ID);
		return getContactsFromCursor(cursor);
	}

	public List<KnownCase> getKnownCases() {
		SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();
		Cursor cursor = db
				.query(KnownCases.TABLE_NAME, KnownCases.PROJECTION, null, null, null, null, KnownCases.ID);
		return getKnownFromCursor(cursor);
	}


	public List<Contact> getContacts(long timeFrom, long timeUntil) {
		SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();
		Cursor cursor = db.query(Contacts.TABLE_NAME, Contacts.PROJECTION, Contacts.DATE + ">=? AND " + Contacts.DATE + "<?",
				new String[] { Long.toString(timeFrom), Long.toString(timeUntil) }, null, null, Contacts.ID);
		return getContactsFromCursor(cursor);
	}

	private List<Contact> getContactsFromCursor(Cursor cursor) {
		List<Contact> contacts = new ArrayList<>();
		while (cursor.moveToNext()) {
			int id = cursor.getInt(cursor.getColumnIndexOrThrow(Contacts.ID));
			long date = cursor.getLong(cursor.getColumnIndexOrThrow(Contacts.DATE));
			EphId ephid = new EphId(cursor.getBlob(cursor.getColumnIndexOrThrow(Contacts.EPHID)));
			int windowCount = cursor.getInt(cursor.getColumnIndexOrThrow(Contacts.WINDOW_COUNT));
			int associatedKnownCase = cursor.getInt(cursor.getColumnIndexOrThrow(Contacts.ASSOCIATED_KNOWN_CASE));
			Contact contact = new Contact(id, date, ephid, windowCount, associatedKnownCase);
			contacts.add(contact);
		}
		cursor.close();
		return contacts;
	}

	private List<KnownCase> getKnownFromCursor(Cursor cursor) {
		List<KnownCase> cases = new ArrayList<>();
		while (cursor.moveToNext()) {
			int id = cursor.getInt(cursor.getColumnIndexOrThrow(KnownCases.ID));
			Long day = cursor.getLong(cursor.getColumnIndexOrThrow(KnownCases.BUCKET_TIME));
			byte[] key = cursor.getBlob(cursor.getColumnIndexOrThrow(KnownCases.KEY));
			KnownCase knownCase = new  KnownCase(id, day, key);
			cases.add(knownCase);
		}
		cursor.close();
		return cases;
	}

	public List<MatchedContact> getMatchedContacts() {
		List<MatchedContact> matchedContacts = new ArrayList<>();
		SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();
		Cursor cursor =
				db.query(MatchedContacts.TABLE_NAME, MatchedContacts.PROJECTION, null, null, null, null, MatchedContacts.ID);
		while (cursor.moveToNext()) {
			int id = cursor.getInt(cursor.getColumnIndexOrThrow(MatchedContacts.ID));
			long reportDate = cursor.getLong(cursor.getColumnIndexOrThrow(MatchedContacts.REPORT_DATE));
			MatchedContact contact = new MatchedContact(id, reportDate);
			matchedContacts.add(contact);
		}
		cursor.close();
		return matchedContacts;
	}

	public void recreateTables(ResultListener<Void> listener) {
		databaseThread.post(() -> {
			databaseOpenHelper.recreateTables(databaseOpenHelper.getWritableDatabase());
			listener.onResult(null);
		});
	}

	public void exportTo(Context context, OutputStream targetOut, ResultListener<Void> listener) {
		databaseThread.post(() -> {
			try {
				databaseOpenHelper.exportDatabaseTo(context, targetOut);
			} catch (IOException e) {
				e.printStackTrace();
			}
			listener.onResult(null);
		});
	}

}
