package net.studymongolian.suryaa.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import net.studymongolian.suryaa.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "suryaa.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = DatabaseHelper.class.getName();



    private final Context context;

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(ListsEntry.CREATE_LIST_TABLE);
            db.execSQL(VocabEntry.CREATE_VOCAB_TABLE);
            insertDefaultInitialData(db);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // method from https://riggaroo.co.za/android-sqlite-database-use-onupgrade-correctly/
        Log.e(TAG, "Updating database from " + oldVersion + " to " + newVersion);
        // For database upgrade add file like from_1_to_2.sql to the assets folder
        try {
            String relativePath = "databases/suryaa/";
            for (int i = oldVersion; i < newVersion; ++i) {
                String migrationName = String.format("from_%d_to_%d.sql", i, (i + 1));
                String pathName = relativePath + migrationName;
                readAndExecuteSQLScript(db, context, pathName);
            }
        } catch (Exception exception) {
            Log.e(TAG, "Exception running upgrade script:", exception);
        }

    }

    private void readAndExecuteSQLScript(SQLiteDatabase db, Context ctx, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            Log.d(TAG, "SQL script file name is empty");
            return;
        }

        AssetManager assetManager = ctx.getAssets();
        BufferedReader reader = null;

        try {
            InputStream is = assetManager.open(fileName);
            InputStreamReader isr = new InputStreamReader(is);
            reader = new BufferedReader(isr);
            executeSQLScript(db, reader);
        } catch (IOException e) {
            Log.e(TAG, "IOException:", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException:", e);
                }
            }
        }

    }

    private void executeSQLScript(SQLiteDatabase db, BufferedReader reader) throws IOException {
        String line;
        StringBuilder statement = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            statement.append(line);
            statement.append("\n");
            if (line.endsWith(";")) {
                db.execSQL(statement.toString());
                statement = new StringBuilder();
            }
        }
    }

    private void insertDefaultInitialData(SQLiteDatabase db) {

        String defaultListName = context.getString(R.string.default_list_name);
        String[] mongolEntries = context.getResources().getStringArray(R.array.default_data_mongol);
        String[] definitions = context.getResources().getStringArray(R.array.default_data_definition);
        String[] pronunciations = context.getResources().getStringArray(R.array.default_data_pronunciation);
        int numberOfItems = mongolEntries.length;

        // list table
        ContentValues contentValues = new ContentValues();
        contentValues.put(ListsEntry.LIST_NAME, defaultListName);
        contentValues.put(ListsEntry.DATE_ACCESSED, System.currentTimeMillis());
        long listId = db.insert(ListsEntry.LIST_TABLE, null, contentValues);
        if (listId == -1) return;

        // vocab table
        contentValues = new ContentValues();
        long date = System.currentTimeMillis();
        for (int i = 0; i < numberOfItems; i++) {
            contentValues.put(VocabEntry.NEXT_PRACTICE_DATE, date);
            contentValues.put(VocabEntry.LIST, listId);
            contentValues.put(VocabEntry.MONGOL, mongolEntries[i]);
            contentValues.put(VocabEntry.DEFINITION, definitions[i]);
            contentValues.put(VocabEntry.PRONUNCIATION, pronunciations[i]);
            db.insert(VocabEntry.VOCAB_TABLE, null, contentValues);
            date++;
        }
    }
}
