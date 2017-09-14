package net.studymongolian.suryaa;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class DatabaseManager {

    private MyDatabaseHelper mHelper;

    DatabaseManager(Context context) {
        this.mHelper = new MyDatabaseHelper(context);
        //this.mContext = context;
    }


    ArrayList<Vocab> getAllVocabInList(long listId) {

        ArrayList<Vocab> vocabList = new ArrayList<>();

        SQLiteDatabase db = mHelper.getReadableDatabase();
        String[] columns = {
                MyDatabaseHelper.VOCAB_ID,
                MyDatabaseHelper.NEXT_PRACTICE_DATE,
                MyDatabaseHelper.MONGOL,
                MyDatabaseHelper.DEFINITION,
                MyDatabaseHelper.PRONUNCIATION,
                MyDatabaseHelper.AUDIO_FILE};
        String selection = MyDatabaseHelper.LIST + " LIKE ?";
        String[] selectionArgs = {String.valueOf(listId)};
        String orderBy = MyDatabaseHelper.NEXT_PRACTICE_DATE + " DESC";
        Cursor cursor = db.query(MyDatabaseHelper.VOCAB_TABLE, columns, selection, selectionArgs,
                null, null, orderBy, null);
        int indexId = cursor.getColumnIndex(MyDatabaseHelper.VOCAB_ID);
        int indexDate = cursor.getColumnIndex(MyDatabaseHelper.NEXT_PRACTICE_DATE);
        int indexMongol = cursor.getColumnIndex(MyDatabaseHelper.MONGOL);
        int indexDefinition = cursor.getColumnIndex(MyDatabaseHelper.DEFINITION);
        int indexPronunciation = cursor.getColumnIndex(MyDatabaseHelper.PRONUNCIATION);
        int indexAudioLocation = cursor.getColumnIndex(MyDatabaseHelper.AUDIO_FILE);

        while (cursor.moveToNext()) {
            Vocab vocabItem = new Vocab();
            vocabItem.setId(cursor.getLong(indexId));
            vocabItem.setDate(cursor.getLong(indexDate));
            vocabItem.setList(listId);
            vocabItem.setMongol(cursor.getString(indexMongol));
            vocabItem.setDefinition(cursor.getString(indexDefinition));
            vocabItem.setPronunciation(cursor.getString(indexPronunciation));
            vocabItem.setAudioFileName(cursor.getString(indexAudioLocation));
            vocabList.add(vocabItem);
        }

        cursor.close();
        db.close();

        return vocabList;
    }

    Queue<Vocab> getTodaysVocab(long listId) {

        Queue<Vocab> vocabList = new LinkedList<>();


        // midnight tonight
        Calendar date = new GregorianCalendar();
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        date.add(Calendar.DAY_OF_MONTH, 1);
        long midnightTonight = date.getTimeInMillis();


        SQLiteDatabase db = mHelper.getReadableDatabase();
        String[] columns = {
                MyDatabaseHelper.VOCAB_ID,
                MyDatabaseHelper.NEXT_PRACTICE_DATE,
                MyDatabaseHelper.MONGOL,
                MyDatabaseHelper.DEFINITION,
                MyDatabaseHelper.PRONUNCIATION,
                MyDatabaseHelper.AUDIO_FILE};
        String selection = MyDatabaseHelper.LIST + " LIKE ? AND " +
                MyDatabaseHelper.NEXT_PRACTICE_DATE + " < " + midnightTonight;
        String[] selectionArgs = {String.valueOf(listId)};
        String orderBy = MyDatabaseHelper.NEXT_PRACTICE_DATE;
        Cursor cursor = db.query(MyDatabaseHelper.VOCAB_TABLE, columns, selection, selectionArgs, null,
                null, orderBy, null);
        int indexId = cursor.getColumnIndex(MyDatabaseHelper.VOCAB_ID);
        int indexDate = cursor.getColumnIndex(MyDatabaseHelper.NEXT_PRACTICE_DATE);
        int indexMongol = cursor.getColumnIndex(MyDatabaseHelper.MONGOL);
        int indexDefinition = cursor.getColumnIndex(MyDatabaseHelper.DEFINITION);
        int indexPronunciation = cursor.getColumnIndex(MyDatabaseHelper.PRONUNCIATION);
        int indexAudioLocation = cursor.getColumnIndex(MyDatabaseHelper.AUDIO_FILE);

        while (cursor.moveToNext()) {
            Vocab vocabItem = new Vocab();
            vocabItem.setId(cursor.getLong(indexId));
            vocabItem.setDate(cursor.getLong(indexDate));
            vocabItem.setList(listId);
            vocabItem.setMongol(cursor.getString(indexMongol));
            vocabItem.setDefinition(cursor.getString(indexDefinition));
            vocabItem.setPronunciation(cursor.getString(indexPronunciation));
            vocabItem.setAudioFileName(cursor.getString(indexAudioLocation));
            vocabList.add(vocabItem);
        }

        cursor.close();
        db.close();

        return vocabList;

    }

    Vocab getVocabItem(long vocabId) {

        SQLiteDatabase db = mHelper.getReadableDatabase();
        String[] columns = {
                MyDatabaseHelper.VOCAB_ID,
                MyDatabaseHelper.NEXT_PRACTICE_DATE,
                MyDatabaseHelper.LIST,
                MyDatabaseHelper.MONGOL,
                MyDatabaseHelper.DEFINITION,
                MyDatabaseHelper.PRONUNCIATION,
                MyDatabaseHelper.AUDIO_FILE};
        String selection = MyDatabaseHelper.VOCAB_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(vocabId)};
        Cursor cursor = db.query(MyDatabaseHelper.VOCAB_TABLE, columns, selection, selectionArgs,
                null, null, null, null);

        int indexId = cursor.getColumnIndex(MyDatabaseHelper.VOCAB_ID);
        int indexDate = cursor.getColumnIndex(MyDatabaseHelper.NEXT_PRACTICE_DATE);
        int indexList = cursor.getColumnIndex(MyDatabaseHelper.LIST);
        int indexMongol = cursor.getColumnIndex(MyDatabaseHelper.MONGOL);
        int indexDefinition = cursor.getColumnIndex(MyDatabaseHelper.DEFINITION);
        int indexPronunciation = cursor.getColumnIndex(MyDatabaseHelper.PRONUNCIATION);
        int indexAudioLocation = cursor.getColumnIndex(MyDatabaseHelper.AUDIO_FILE);

        Vocab vocabItem = new Vocab();
        if (cursor.moveToFirst()) {
            vocabItem.setId(cursor.getLong(indexId));
            vocabItem.setDate(cursor.getLong(indexDate));
            vocabItem.setList(cursor.getLong(indexList));
            vocabItem.setMongol(cursor.getString(indexMongol));
            vocabItem.setDefinition(cursor.getString(indexDefinition));
            vocabItem.setPronunciation(cursor.getString(indexPronunciation));
            vocabItem.setAudioFileName(cursor.getString(indexAudioLocation));
        }

        cursor.close();
        db.close();

        return vocabItem;
    }

    VocabList getList(long listId) {

        SQLiteDatabase db = mHelper.getReadableDatabase();
        String[] columns = {
                MyDatabaseHelper.LIST_ID,
                MyDatabaseHelper.LIST_NAME,
                MyDatabaseHelper.DATE_ACCESSED};
        String selection = MyDatabaseHelper.LIST_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(listId)};
        Cursor cursor = db.query(MyDatabaseHelper.LIST_TABLE, columns, selection, selectionArgs, null,
                null, null, null);

        int indexId = cursor.getColumnIndex(MyDatabaseHelper.LIST_ID);
        int indexList = cursor.getColumnIndex(MyDatabaseHelper.LIST_NAME);
        int indexDate = cursor.getColumnIndex(MyDatabaseHelper.DATE_ACCESSED);

        VocabList newList = new VocabList();
        if (cursor.moveToFirst()) {
            newList.setListId(cursor.getLong(indexId));
            newList.setName(cursor.getString(indexList));
            newList.setDateAccessed(cursor.getLong(indexDate));
        }

        cursor.close();
        db.close();

        return newList;
    }

    ArrayList<VocabList> getAllLists() {

        SQLiteDatabase db = mHelper.getReadableDatabase();
        String[] columns = {
                MyDatabaseHelper.LIST_ID,
                MyDatabaseHelper.LIST_NAME,
                MyDatabaseHelper.DATE_ACCESSED};
        String orderBy = MyDatabaseHelper.DATE_ACCESSED + " DESC";
        Cursor cursor = db.query(MyDatabaseHelper.LIST_TABLE, columns, null, null, null, null, orderBy);


        int indexId = cursor.getColumnIndex(MyDatabaseHelper.LIST_ID);
        int indexList = cursor.getColumnIndex(MyDatabaseHelper.LIST_NAME);
        int indexDate = cursor.getColumnIndex(MyDatabaseHelper.DATE_ACCESSED);

        ArrayList<VocabList> listNames = new ArrayList<>();
        while (cursor.moveToNext()) {
            VocabList newList = new VocabList();
            newList.setListId(cursor.getLong(indexId));
            newList.setName(cursor.getString(indexList));
            newList.setDateAccessed(cursor.getLong(indexDate));
            listNames.add(newList);
        }

        cursor.close();
        db.close();

        return listNames;
    }

    long createNewList(String name) {

        if (TextUtils.isEmpty(name)) return -1;

        ContentValues contentValues = new ContentValues();
        contentValues.put(MyDatabaseHelper.LIST_NAME, name);
        contentValues.put(MyDatabaseHelper.DATE_ACCESSED, System.currentTimeMillis());

        SQLiteDatabase db = mHelper.getWritableDatabase();
        long id = db.insertOrThrow(MyDatabaseHelper.LIST_TABLE, null, contentValues);
        db.close();
        return id;
    }

    long updateList(VocabList list) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(MyDatabaseHelper.LIST_NAME, list.getName());
        contentValues.put(MyDatabaseHelper.DATE_ACCESSED, list.getDateAccessed());

        String selection = MyDatabaseHelper.LIST_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(list.getListId())};

        SQLiteDatabase db = mHelper.getWritableDatabase();
        int id = db.update(MyDatabaseHelper.LIST_TABLE, contentValues, selection, selectionArgs);
        db.close();
        return id;
    }

    long updateListAccessDate(long listId) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(MyDatabaseHelper.DATE_ACCESSED, System.currentTimeMillis());
        String selection = MyDatabaseHelper.LIST_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(listId)};

        SQLiteDatabase db = mHelper.getWritableDatabase();
        int id = db.update(MyDatabaseHelper.LIST_TABLE, contentValues, selection, selectionArgs);
        db.close();
        return id;
    }

    int deleteList(long rowId) {

        // NOTE: this does not delete the audio files
        // You should delete the audio files separately

        SQLiteDatabase db = mHelper.getWritableDatabase();

        // delete from List Table
        String whereClause = MyDatabaseHelper.LIST_ID + " =?";
        String[] whereArgs = {Long.toString(rowId)};
        int count = db.delete(MyDatabaseHelper.LIST_TABLE, whereClause, whereArgs);

        // delete from vocab table
        whereClause = MyDatabaseHelper.LIST + " =?";
        db.delete(MyDatabaseHelper.VOCAB_TABLE, whereClause, whereArgs);

        db.close();
        return count;
    }

    long addVocabItem(Vocab entry) {

        long date = System.currentTimeMillis();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MyDatabaseHelper.NEXT_PRACTICE_DATE, date);
        contentValues.put(MyDatabaseHelper.LIST, entry.getList());
        contentValues.put(MyDatabaseHelper.MONGOL, entry.getMongol());
        if (!TextUtils.isEmpty(entry.getDefinition())) {
            contentValues.put(MyDatabaseHelper.DEFINITION, entry.getDefinition());
        }
        if (!TextUtils.isEmpty(entry.getPronunciation())) {
            contentValues.put(MyDatabaseHelper.PRONUNCIATION, entry.getPronunciation());
        }
        if (!TextUtils.isEmpty(entry.getAudioFileName())) {
            contentValues.put(MyDatabaseHelper.AUDIO_FILE, entry.getAudioFileName());
        }

        SQLiteDatabase db = mHelper.getWritableDatabase();
        long id = db.insertOrThrow(MyDatabaseHelper.VOCAB_TABLE, null, contentValues);
        db.close();
        return id;
    }

    void bulkInsert(List<Vocab> vocabItems) {
        long date = System.currentTimeMillis();

        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (Vocab item : vocabItems) {
                values.put(MyDatabaseHelper.NEXT_PRACTICE_DATE, date);
                values.put(MyDatabaseHelper.LIST, item.getList());
                values.put(MyDatabaseHelper.MONGOL, item.getMongol());
                //values.put(MyDatabaseHelper.MONGOL, item.getMongol());
                if (!TextUtils.isEmpty(item.getDefinition())) {
                    values.put(MyDatabaseHelper.DEFINITION, item.getDefinition());
                }
                if (!TextUtils.isEmpty(item.getPronunciation())) {
                    values.put(MyDatabaseHelper.PRONUNCIATION, item.getPronunciation());
                }
                if (!TextUtils.isEmpty(item.getAudioFileName())) {
                    values.put(MyDatabaseHelper.AUDIO_FILE, item.getAudioFileName());
                }
                db.insert(MyDatabaseHelper.VOCAB_TABLE, null, values);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e("TAG", "bulkInsert: " + e.getMessage());
            throw e;
        } finally {
            db.endTransaction();
        }
    }

    int updateVocabItem(Vocab updatedItem) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(MyDatabaseHelper.NEXT_PRACTICE_DATE, updatedItem.getDate());
        contentValues.put(MyDatabaseHelper.LIST, updatedItem.getList());
        contentValues.put(MyDatabaseHelper.MONGOL, updatedItem.getMongol());
        contentValues.put(MyDatabaseHelper.DEFINITION, updatedItem.getDefinition());
        contentValues.put(MyDatabaseHelper.PRONUNCIATION, updatedItem.getPronunciation());
        contentValues.put(MyDatabaseHelper.AUDIO_FILE, updatedItem.getAudioFileName());

        String selection = MyDatabaseHelper.VOCAB_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(updatedItem.getId())};

        SQLiteDatabase db = mHelper.getWritableDatabase();
        int id = db.update(MyDatabaseHelper.VOCAB_TABLE, contentValues, selection, selectionArgs);
        db.close();
        return id;
    }

    int deleteVocabItem(long rowId) {

        SQLiteDatabase db = mHelper.getWritableDatabase();
        String whereClause = MyDatabaseHelper.VOCAB_ID + " =?";
        String[] whereArgs = {Long.toString(rowId)};
        int count = db.delete(MyDatabaseHelper.VOCAB_TABLE, whereClause, whereArgs);
        db.close();
        return count;
    }

    static class MyDatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "suryaa.db";
        private static final int DATABASE_VERSION = 1;

        // List table
        private static final String LIST_TABLE = "lists";
        // Column names
        private static final String LIST_ID = "_id";
        private static final String LIST_NAME = "lists";
        //private static final String NUMBER_OF_ENTRIES = "quantity";
        private static final String DATE_ACCESSED = "date_accessed";
        // SQL statements
        private static final String CREATE_LIST_TABLE = "CREATE TABLE " + LIST_TABLE + " ("
                + LIST_ID + " INTEGER PRIMARY KEY,"
                + LIST_NAME + " TEXT NOT NULL UNIQUE,"
                //+ NUMBER_OF_ENTRIES + " INTEGER NOT NULL,"
                + DATE_ACCESSED + " INTEGER NOT NULL)";
        private static final String DROP_LIST_TABLE = "DROP TABLE IF EXISTS "
                + LIST_TABLE;

        // Vocab table
        private static final String VOCAB_TABLE = "vocab";
        // Column names
        private static final String VOCAB_ID = "_id";
        private static final String NEXT_PRACTICE_DATE = "date";
        private static final String LIST = "list_id";
        private static final String MONGOL = "mongol";
        private static final String DEFINITION = "definition";
        private static final String PRONUNCIATION = "pronunciation";
        private static final String AUDIO_FILE = "audio";
        // SQL statements
        private static final String CREATE_VOCAB_TABLE = "CREATE TABLE " + VOCAB_TABLE + " ("
                + VOCAB_ID + " INTEGER PRIMARY KEY,"
                + NEXT_PRACTICE_DATE + " INTEGER,"
                + LIST + " TEXT NOT NULL,"
                + MONGOL + " TEXT NOT NULL,"
                + DEFINITION + " TEXT,"
                + PRONUNCIATION + " TEXT,"
                + AUDIO_FILE + " TEXT,"
                + "FOREIGN KEY(" + LIST + ") REFERENCES " + LIST_TABLE + "(" + LIST_ID + "))";
        private static final String DROP_VOCAB_TABLE = "DROP TABLE IF EXISTS "
                + VOCAB_TABLE;


        private Context context;

        MyDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            try {
                db.execSQL(CREATE_LIST_TABLE);
                db.execSQL(CREATE_VOCAB_TABLE);
                insertDefaultInitialData(db);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            try {
                db.execSQL(DROP_LIST_TABLE);
                db.execSQL(DROP_VOCAB_TABLE);
                onCreate(db);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void insertDefaultInitialData(SQLiteDatabase db) {

            String defaultListName = context.getString(R.string.default_list_name);
            String[] mongolEntries = context.getResources().getStringArray(R.array.default_data_mongol);
            String[] definitions = context.getResources().getStringArray(R.array.default_data_definition);
            String[] pronunciations = context.getResources().getStringArray(R.array.default_data_pronunciation);
            int numberOfItems = mongolEntries.length;

            // list table
            ContentValues contentValues = new ContentValues();
            contentValues.put(LIST_NAME, defaultListName);
            contentValues.put(DATE_ACCESSED, System.currentTimeMillis());
            long listId = db.insert(LIST_TABLE, null, contentValues);
            if (listId == -1) return;

            // vocab table
            contentValues = new ContentValues();
            long date = System.currentTimeMillis();
            for (int i = 0; i < numberOfItems; i++) {
                contentValues.put(NEXT_PRACTICE_DATE, date);
                contentValues.put(LIST, listId);
                contentValues.put(MONGOL, mongolEntries[i]);
                contentValues.put(DEFINITION, definitions[i]);
                contentValues.put(PRONUNCIATION, pronunciations[i]);
                db.insert(VOCAB_TABLE, null, contentValues);
                date++;
            }
        }
    }

}