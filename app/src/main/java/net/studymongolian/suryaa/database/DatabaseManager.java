package net.studymongolian.suryaa.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import net.studymongolian.suryaa.StudyMode;
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
    }


    public ArrayList<Vocab> getAllVocabInList(long listId) {

        ArrayList<Vocab> vocabList = new ArrayList<>();

        SQLiteDatabase db = mHelper.getReadableDatabase();
        String[] columns = {
                VocabEntry.ID,
                VocabEntry.MONGOL,
                VocabEntry.DEFINITION,
                VocabEntry.PRONUNCIATION};
        String selection = VocabEntry.LIST_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(listId)};
        Cursor cursor = db.query(VocabEntry.VOCAB_TABLE, columns, selection, selectionArgs,
                null, null, null, null);
        int indexId = cursor.getColumnIndex(VocabEntry.ID);
        int indexMongol = cursor.getColumnIndex(VocabEntry.MONGOL);
        int indexDefinition = cursor.getColumnIndex(VocabEntry.DEFINITION);
        int indexPronunciation = cursor.getColumnIndex(VocabEntry.PRONUNCIATION);

        while (cursor.moveToNext()) {
            Vocab vocabItem = new Vocab();
            vocabItem.setId(cursor.getLong(indexId));
            vocabItem.setListId(listId);
            vocabItem.setMongol(cursor.getString(indexMongol));
            vocabItem.setDefinition(cursor.getString(indexDefinition));
            vocabItem.setPronunciation(cursor.getString(indexPronunciation));
            vocabList.add(vocabItem);
        }

        cursor.close();
        db.close();

        return vocabList;
    }

    public Queue<Vocab> getTodaysVocab(long listId, StudyMode studyMode) {

        Queue<Vocab> vocabList = new LinkedList<>();


        // midnight tonight
        Calendar date = new GregorianCalendar();
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        date.add(Calendar.DAY_OF_MONTH, 1);
        long midnightTonight = date.getTimeInMillis();


        String nextPracticeDateColumn = getNextPracticeDateColumnName(studyMode);
        String nthTryColumn = getNthTryColumnName(studyMode);
        String intervalColumn = getIntervalColumnName(studyMode);
        String eFactorColumn = getEasinessFactorColumnName(studyMode);
        SQLiteDatabase db = mHelper.getReadableDatabase();
        String[] columns = {
                VocabEntry.ID,
                VocabEntry.MONGOL,
                VocabEntry.DEFINITION,
                VocabEntry.PRONUNCIATION,
                nextPracticeDateColumn,
                nthTryColumn,
                intervalColumn,
                eFactorColumn
        };
        String selection = VocabEntry.LIST_ID + " LIKE ? AND " +
                nextPracticeDateColumn + " < " + midnightTonight;
        String[] selectionArgs = {String.valueOf(listId)};
        String orderBy = nextPracticeDateColumn;
        Cursor cursor = db.query(VocabEntry.VOCAB_TABLE, columns, selection, selectionArgs,
                null,null, orderBy, null);
        int indexId = cursor.getColumnIndex(VocabEntry.ID);
        int indexMongol = cursor.getColumnIndex(VocabEntry.MONGOL);
        int indexDefinition = cursor.getColumnIndex(VocabEntry.DEFINITION);
        int indexPronunciation = cursor.getColumnIndex(VocabEntry.PRONUNCIATION);
        int indexDate = cursor.getColumnIndex(nextPracticeDateColumn);
        int indexNthTry = cursor.getColumnIndex(nthTryColumn);
        int indexInterval = cursor.getColumnIndex(intervalColumn);
        int indexEF = cursor.getColumnIndex(eFactorColumn);

        while (cursor.moveToNext()) {
            Vocab vocabItem = new Vocab();
            vocabItem.setId(cursor.getLong(indexId));
            vocabItem.setListId(listId);
            vocabItem.setMongol(cursor.getString(indexMongol));
            vocabItem.setDefinition(cursor.getString(indexDefinition));
            vocabItem.setPronunciation(cursor.getString(indexPronunciation));
            vocabItem.setNextPracticeDate(cursor.getLong(indexDate));
            vocabItem.setNthTry(cursor.getInt(indexNthTry));
            vocabItem.setInterval(cursor.getInt(indexInterval));
            vocabItem.setEasinessFactor(cursor.getFloat(indexEF));
            vocabList.add(vocabItem);
        }

        cursor.close();
        db.close();

        return vocabList;

    }

    private String getNextPracticeDateColumnName(StudyMode studyMode) {
        switch (studyMode) {
            case DEFINITION:
                return VocabEntry.DEFINITION_NEXT_PRACTICE_DATE;
            case PRONUNCIATION:
                return VocabEntry.PRONUNCIATION_NEXT_PRACTICE_DATE;
            default:
                return VocabEntry.MONGOL_NEXT_PRACTICE_DATE;
        }
    }

    private String getNthTryColumnName(StudyMode studyMode) {
        switch (studyMode) {
            case DEFINITION:
                return VocabEntry.DEFINITION_NTH_TRY;
            case PRONUNCIATION:
                return VocabEntry.PRONUNCIATION_NTH_TRY;
            default:
                return VocabEntry.MONGOL_NTH_TRY;
        }
    }

    private String getIntervalColumnName(StudyMode studyMode) {
        switch (studyMode) {
            case DEFINITION:
                return VocabEntry.DEFINITION_INTERVAL;
            case PRONUNCIATION:
                return VocabEntry.PRONUNCIATION_INTERVAL;
            default:
                return VocabEntry.MONGOL_INTERVAL;
        }
    }

    private String getEasinessFactorColumnName(StudyMode studyMode) {
        switch (studyMode) {
            case DEFINITION:
                return VocabEntry.DEFINITION_EF;
            case PRONUNCIATION:
                return VocabEntry.PRONUNCIATION_EF;
            default:
                return VocabEntry.MONGOL_EF;
        }
    }

    public Vocab getVocabItem(long vocabId, StudyMode studyMode) {

        String nextPracticeDateColumn = getNextPracticeDateColumnName(studyMode);
        String nthTryColumn = getNthTryColumnName(studyMode);
        String intervalColumn = getIntervalColumnName(studyMode);
        String eFactorColumn = getEasinessFactorColumnName(studyMode);

        SQLiteDatabase db = mHelper.getReadableDatabase();
        String[] columns = {
                VocabEntry.ID,
                VocabEntry.LIST_ID,
                VocabEntry.MONGOL,
                VocabEntry.DEFINITION,
                VocabEntry.PRONUNCIATION,
                nextPracticeDateColumn,
                nthTryColumn,
                intervalColumn,
                eFactorColumn
        };
        String selection = VocabEntry.ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(vocabId)};
        Cursor cursor = db.query(VocabEntry.VOCAB_TABLE, columns, selection, selectionArgs,
                null, null, null, null);

        int indexId = cursor.getColumnIndex(VocabEntry.ID);
        int indexListId = cursor.getColumnIndex(VocabEntry.LIST_ID);
        int indexMongol = cursor.getColumnIndex(VocabEntry.MONGOL);
        int indexDefinition = cursor.getColumnIndex(VocabEntry.DEFINITION);
        int indexPronunciation = cursor.getColumnIndex(VocabEntry.PRONUNCIATION);
        int indexDate = cursor.getColumnIndex(nextPracticeDateColumn);
        int indexNthTry = cursor.getColumnIndex(nthTryColumn);
        int indexInterval = cursor.getColumnIndex(intervalColumn);
        int indexEF = cursor.getColumnIndex(eFactorColumn);

        Vocab vocabItem = new Vocab();
        if (cursor.moveToFirst()) {
            vocabItem.setId(cursor.getLong(indexId));
            vocabItem.setListId(cursor.getLong(indexListId));
            vocabItem.setMongol(cursor.getString(indexMongol));
            vocabItem.setDefinition(cursor.getString(indexDefinition));
            vocabItem.setPronunciation(cursor.getString(indexPronunciation));
            vocabItem.setNextPracticeDate(cursor.getLong(indexDate));
            vocabItem.setNthTry(cursor.getInt(indexNthTry));
            vocabItem.setInterval(cursor.getInt(indexInterval));
            vocabItem.setEasinessFactor(cursor.getFloat(indexEF));
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

        SQLiteDatabase db = mHelper.getWritableDatabase();

        // delete from List Table
        String whereClause = ListsEntry.LIST_ID + " =?";
        String[] whereArgs = {Long.toString(rowId)};
        int count = db.delete(ListsEntry.LIST_TABLE, whereClause, whereArgs);

        // delete from vocab table
        whereClause = VocabEntry.LIST_ID + " =?";
        db.delete(VocabEntry.VOCAB_TABLE, whereClause, whereArgs);

        db.close();
        return count;
    }

    public long addVocabItem(Vocab entry) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(VocabEntry.LIST_ID, entry.getListId());
        contentValues.put(VocabEntry.MONGOL, entry.getMongol());
        contentValues.put(VocabEntry.DEFINITION, entry.getDefinition());
        contentValues.put(VocabEntry.PRONUNCIATION, entry.getPronunciation());

        SQLiteDatabase db = mHelper.getWritableDatabase();
        long id = db.insertOrThrow(VocabEntry.VOCAB_TABLE, null, contentValues);
        db.close();
        return id;
    }

    public void bulkInsert(List<Vocab> vocabItems) {

        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues contentValues = new ContentValues();
            for (Vocab item : vocabItems) {
                contentValues.put(VocabEntry.LIST_ID, item.getListId());
                contentValues.put(VocabEntry.MONGOL, item.getMongol());
                contentValues.put(VocabEntry.DEFINITION, item.getDefinition());
                contentValues.put(VocabEntry.PRONUNCIATION, item.getPronunciation());
                db.insert(VocabEntry.VOCAB_TABLE, null, contentValues);
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

        StudyMode studyMode = updatedItem.getStudyMode();
        String nextPracticeDateColumn = getNextPracticeDateColumnName(studyMode);
        String nthTryColumn = getNthTryColumnName(studyMode);
        String intervalColumn = getIntervalColumnName(studyMode);
        String eFactorColumn = getEasinessFactorColumnName(studyMode);

        ContentValues contentValues = new ContentValues();
        contentValues.put(VocabEntry.LIST_ID, updatedItem.getListId());
        contentValues.put(VocabEntry.MONGOL, updatedItem.getMongol());
        contentValues.put(VocabEntry.DEFINITION, updatedItem.getDefinition());
        contentValues.put(VocabEntry.PRONUNCIATION, updatedItem.getPronunciation());
        contentValues.put(nextPracticeDateColumn, updatedItem.getNextPracticeDate());
        contentValues.put(nthTryColumn, updatedItem.getNthTry());
        contentValues.put(intervalColumn, updatedItem.getInterval());
        contentValues.put(eFactorColumn, updatedItem.getEasinessFactor());

        String selection = VocabEntry.ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(updatedItem.getId())};

        SQLiteDatabase db = mHelper.getWritableDatabase();
        int id = db.update(VocabEntry.VOCAB_TABLE, contentValues, selection, selectionArgs);
        db.close();
        return id;
    }

    public int deleteVocabItem(long rowId) {

        SQLiteDatabase db = mHelper.getWritableDatabase();
        String whereClause = VocabEntry.ID + " =?";
        String[] whereArgs = {Long.toString(rowId)};
        int count = db.delete(VocabEntry.VOCAB_TABLE, whereClause, whereArgs);
        db.close();
        return count;
    }


}