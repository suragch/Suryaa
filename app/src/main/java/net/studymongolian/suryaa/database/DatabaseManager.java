package net.studymongolian.suryaa.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import net.studymongolian.suryaa.Vocab;
import net.studymongolian.suryaa.VocabList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DatabaseManager {

    private DatabaseHelper mHelper;

    public DatabaseManager(Context context) {
        this.mHelper = new DatabaseHelper(context);
        //this.mContext = context;
    }


    public ArrayList<Vocab> getAllVocabInList(long listId) {

        ArrayList<Vocab> vocabList = new ArrayList<>();

        SQLiteDatabase db = mHelper.getReadableDatabase();
        String[] columns = {
                VocabEntry.VOCAB_ID,
                VocabEntry.NEXT_PRACTICE_DATE,
                VocabEntry.MONGOL,
                VocabEntry.DEFINITION,
                VocabEntry.PRONUNCIATION,
                VocabEntry.AUDIO_FILE};
        String selection = VocabEntry.LIST + " LIKE ?";
        String[] selectionArgs = {String.valueOf(listId)};
        String orderBy = VocabEntry.NEXT_PRACTICE_DATE + " DESC";
        Cursor cursor = db.query(VocabEntry.VOCAB_TABLE, columns, selection, selectionArgs,
                null, null, orderBy, null);
        int indexId = cursor.getColumnIndex(VocabEntry.VOCAB_ID);
        int indexDate = cursor.getColumnIndex(VocabEntry.NEXT_PRACTICE_DATE);
        int indexMongol = cursor.getColumnIndex(VocabEntry.MONGOL);
        int indexDefinition = cursor.getColumnIndex(VocabEntry.DEFINITION);
        int indexPronunciation = cursor.getColumnIndex(VocabEntry.PRONUNCIATION);
        int indexAudioLocation = cursor.getColumnIndex(VocabEntry.AUDIO_FILE);

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

    public Queue<Vocab> getTodaysVocab(long listId) {

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
                VocabEntry.VOCAB_ID,
                VocabEntry.NEXT_PRACTICE_DATE,
                VocabEntry.MONGOL,
                VocabEntry.DEFINITION,
                VocabEntry.PRONUNCIATION,
                VocabEntry.AUDIO_FILE};
        String selection = VocabEntry.LIST + " LIKE ? AND " +
                VocabEntry.NEXT_PRACTICE_DATE + " < " + midnightTonight;
        String[] selectionArgs = {String.valueOf(listId)};
        String orderBy = VocabEntry.NEXT_PRACTICE_DATE;
        Cursor cursor = db.query(VocabEntry.VOCAB_TABLE, columns, selection, selectionArgs, null,
                null, orderBy, null);
        int indexId = cursor.getColumnIndex(VocabEntry.VOCAB_ID);
        int indexDate = cursor.getColumnIndex(VocabEntry.NEXT_PRACTICE_DATE);
        int indexMongol = cursor.getColumnIndex(VocabEntry.MONGOL);
        int indexDefinition = cursor.getColumnIndex(VocabEntry.DEFINITION);
        int indexPronunciation = cursor.getColumnIndex(VocabEntry.PRONUNCIATION);
        int indexAudioLocation = cursor.getColumnIndex(VocabEntry.AUDIO_FILE);

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

    public Vocab getVocabItem(long vocabId) {

        SQLiteDatabase db = mHelper.getReadableDatabase();
        String[] columns = {
                VocabEntry.VOCAB_ID,
                VocabEntry.NEXT_PRACTICE_DATE,
                VocabEntry.LIST,
                VocabEntry.MONGOL,
                VocabEntry.DEFINITION,
                VocabEntry.PRONUNCIATION,
                VocabEntry.AUDIO_FILE};
        String selection = VocabEntry.VOCAB_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(vocabId)};
        Cursor cursor = db.query(VocabEntry.VOCAB_TABLE, columns, selection, selectionArgs,
                null, null, null, null);

        int indexId = cursor.getColumnIndex(VocabEntry.VOCAB_ID);
        int indexDate = cursor.getColumnIndex(VocabEntry.NEXT_PRACTICE_DATE);
        int indexList = cursor.getColumnIndex(VocabEntry.LIST);
        int indexMongol = cursor.getColumnIndex(VocabEntry.MONGOL);
        int indexDefinition = cursor.getColumnIndex(VocabEntry.DEFINITION);
        int indexPronunciation = cursor.getColumnIndex(VocabEntry.PRONUNCIATION);
        int indexAudioLocation = cursor.getColumnIndex(VocabEntry.AUDIO_FILE);

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

    public VocabList getList(long listId) {

        SQLiteDatabase db = mHelper.getReadableDatabase();
        String[] columns = {
                ListsEntry.LIST_ID,
                ListsEntry.LIST_NAME,
                ListsEntry.DATE_ACCESSED};
        String selection = ListsEntry.LIST_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(listId)};
        Cursor cursor = db.query(ListsEntry.LIST_TABLE, columns, selection, selectionArgs, null,
                null, null, null);

        int indexId = cursor.getColumnIndex(ListsEntry.LIST_ID);
        int indexList = cursor.getColumnIndex(ListsEntry.LIST_NAME);
        int indexDate = cursor.getColumnIndex(ListsEntry.DATE_ACCESSED);

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

    public ArrayList<VocabList> getAllLists() {

        SQLiteDatabase db = mHelper.getReadableDatabase();
        String[] columns = {
                ListsEntry.LIST_ID,
                ListsEntry.LIST_NAME,
                ListsEntry.DATE_ACCESSED};
        String orderBy = ListsEntry.DATE_ACCESSED + " DESC";
        Cursor cursor = db.query(ListsEntry.LIST_TABLE, columns, null, null, null, null, orderBy);


        int indexId = cursor.getColumnIndex(ListsEntry.LIST_ID);
        int indexList = cursor.getColumnIndex(ListsEntry.LIST_NAME);
        int indexDate = cursor.getColumnIndex(ListsEntry.DATE_ACCESSED);

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

    public long createNewList(String name) {

        if (TextUtils.isEmpty(name)) return -1;

        ContentValues contentValues = new ContentValues();
        contentValues.put(ListsEntry.LIST_NAME, name);
        contentValues.put(ListsEntry.DATE_ACCESSED, System.currentTimeMillis());

        SQLiteDatabase db = mHelper.getWritableDatabase();
        long id = db.insertOrThrow(ListsEntry.LIST_TABLE, null, contentValues);
        db.close();
        return id;
    }

    public long updateList(VocabList list) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(ListsEntry.LIST_NAME, list.getName());
        contentValues.put(ListsEntry.DATE_ACCESSED, list.getDateAccessed());

        String selection = ListsEntry.LIST_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(list.getListId())};

        SQLiteDatabase db = mHelper.getWritableDatabase();
        int id = db.update(ListsEntry.LIST_TABLE, contentValues, selection, selectionArgs);
        db.close();
        return id;
    }

    public long updateListAccessDate(long listId) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(ListsEntry.DATE_ACCESSED, System.currentTimeMillis());
        String selection = ListsEntry.LIST_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(listId)};

        SQLiteDatabase db = mHelper.getWritableDatabase();
        int id = db.update(ListsEntry.LIST_TABLE, contentValues, selection, selectionArgs);
        db.close();
        return id;
    }

    public int deleteList(long rowId) {

        // NOTE: this does not delete the audio files
        // You should delete the audio files separately

        SQLiteDatabase db = mHelper.getWritableDatabase();

        // delete from List Table
        String whereClause = ListsEntry.LIST_ID + " =?";
        String[] whereArgs = {Long.toString(rowId)};
        int count = db.delete(ListsEntry.LIST_TABLE, whereClause, whereArgs);

        // delete from vocab table
        whereClause = VocabEntry.LIST + " =?";
        db.delete(VocabEntry.VOCAB_TABLE, whereClause, whereArgs);

        db.close();
        return count;
    }

    public long addVocabItem(Vocab entry) {

        long date = System.currentTimeMillis();

        ContentValues contentValues = new ContentValues();
        contentValues.put(VocabEntry.NEXT_PRACTICE_DATE, date);
        contentValues.put(VocabEntry.LIST, entry.getList());
        contentValues.put(VocabEntry.MONGOL, entry.getMongol());
        if (!TextUtils.isEmpty(entry.getDefinition())) {
            contentValues.put(VocabEntry.DEFINITION, entry.getDefinition());
        }
        if (!TextUtils.isEmpty(entry.getPronunciation())) {
            contentValues.put(VocabEntry.PRONUNCIATION, entry.getPronunciation());
        }
        if (!TextUtils.isEmpty(entry.getAudioFileName())) {
            contentValues.put(VocabEntry.AUDIO_FILE, entry.getAudioFileName());
        }

        SQLiteDatabase db = mHelper.getWritableDatabase();
        long id = db.insertOrThrow(VocabEntry.VOCAB_TABLE, null, contentValues);
        db.close();
        return id;
    }

    public void bulkInsert(List<Vocab> vocabItems) {
        long date = System.currentTimeMillis();

        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (Vocab item : vocabItems) {
                values.put(VocabEntry.NEXT_PRACTICE_DATE, date);
                values.put(VocabEntry.LIST, item.getList());
                values.put(VocabEntry.MONGOL, item.getMongol());
                //values.put(VocabEntry.MONGOL, item.getMongol());
                if (!TextUtils.isEmpty(item.getDefinition())) {
                    values.put(VocabEntry.DEFINITION, item.getDefinition());
                }
                if (!TextUtils.isEmpty(item.getPronunciation())) {
                    values.put(VocabEntry.PRONUNCIATION, item.getPronunciation());
                }
                if (!TextUtils.isEmpty(item.getAudioFileName())) {
                    values.put(VocabEntry.AUDIO_FILE, item.getAudioFileName());
                }
                db.insert(VocabEntry.VOCAB_TABLE, null, values);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e("TAG", "bulkInsert: " + e.getMessage());
            throw e;
        } finally {
            db.endTransaction();
        }
    }

    public int updateVocabItem(Vocab updatedItem) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(VocabEntry.NEXT_PRACTICE_DATE, updatedItem.getDate());
        contentValues.put(VocabEntry.LIST, updatedItem.getList());
        contentValues.put(VocabEntry.MONGOL, updatedItem.getMongol());
        contentValues.put(VocabEntry.DEFINITION, updatedItem.getDefinition());
        contentValues.put(VocabEntry.PRONUNCIATION, updatedItem.getPronunciation());
        contentValues.put(VocabEntry.AUDIO_FILE, updatedItem.getAudioFileName());

        String selection = VocabEntry.VOCAB_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(updatedItem.getId())};

        SQLiteDatabase db = mHelper.getWritableDatabase();
        int id = db.update(VocabEntry.VOCAB_TABLE, contentValues, selection, selectionArgs);
        db.close();
        return id;
    }

    public int deleteVocabItem(long rowId) {

        SQLiteDatabase db = mHelper.getWritableDatabase();
        String whereClause = VocabEntry.VOCAB_ID + " =?";
        String[] whereArgs = {Long.toString(rowId)};
        int count = db.delete(VocabEntry.VOCAB_TABLE, whereClause, whereArgs);
        db.close();
        return count;
    }



}