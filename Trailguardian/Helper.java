package com.example.trailguardian;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Helper extends SQLiteOpenHelper {
    private static final String TAG = "Helper";
    private static final String DATABASE_NAME = "UserDatabase.db";
    private static final int DATABASE_VERSION = 7;
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    public Helper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_NAME + " TEXT,"
                + COLUMN_EMAIL + " TEXT PRIMARY KEY,"
                + COLUMN_PASSWORD + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    //Inserts data into SQL database
    public boolean insertData(String name, String email, String password) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_EMAIL, email);
            values.put(COLUMN_PASSWORD, password);
            long result = db.insert(TABLE_USERS, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting data", e);
            return false;
        }
    }

    //Changes usernmame in the SQL data base based on column
    public boolean updateUserName(String email, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, newName);
        int affectedRows = db.update(TABLE_USERS, values, COLUMN_EMAIL + "=?", new String[]{email});
        db.close();
        return affectedRows > 0;
    }

    //Changes passwords in the SQL data base based on column
    public boolean updateUserPassword(String email, String newPassword) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_PASSWORD, newPassword);
            int affectedRows = db.update(TABLE_USERS, values, COLUMN_EMAIL + "=?", new String[]{email});
            return affectedRows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating password", e);
            return false;
        }
    }

    //Cheks email if the email exists or not at login
    public boolean checkEmail(String email) {
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_EMAIL}, COLUMN_EMAIL + "=?", new String[]{email}, null, null, null);
            boolean exists = cursor.getCount() > 0;
            cursor.close();
            return exists;
        } catch (Exception e) {
            Log.e(TAG, "Error checking email", e);
            return false;
        }
    }

    //checks if the email and password are a match
    public boolean checkEmailPassword(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_EMAIL}, COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?", new String[]{email, password}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    //Gets username for display on main dashboard through column name
    public String getNameByEmail(String email) {
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_NAME}, COLUMN_EMAIL + "=?", new String[]{email}, null, null, null);
            String name = null;
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            }
            cursor.close();
            return name;
        } catch (Exception e) {
            Log.e(TAG, "Error querying for user name by email", e);
            return null;
        }
    }

    //deletes user based on email from the SQL Database
    public Boolean deleteUserByEmail(String email) {
        SQLiteDatabase MyDatabase = this.getWritableDatabase();

        int affectedRows = MyDatabase.delete(TABLE_USERS,
                COLUMN_EMAIL + " = ?", new String[]{email});

        return affectedRows > 0;
    }
}
