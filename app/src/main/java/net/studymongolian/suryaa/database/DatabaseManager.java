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
                VocabEntry.PRONUNCIATION
        };
        String selection = VocabEntry.LIST_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(listId)};
        Cursor cursor = db.query(VocabEntry.VOCAB_TABLE, columns, selection, selectionArgs,
                null, null, null, null);
        int indexId = cursor.getColumnIndex(VocabEntry.ID);
        int indexMongol = cursor.getColumnIndex(VocabEntry.MONGOL);
        int indexDefinition = cursor.getColumnIndex(VocabEntry.DEFINITION);
        int indexPronunciation = cursor.getColumnIndex(VocabEntry.PRONUNCIATION);

        while (cursor.moveToNext()) {
            Vocab vocabItem = new Vocab(null);
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


        String nextDueDateColumn = getNextDueDateColumnName(studyMode);
        String consecutiveCorrectColumn = getConsecutiveCorrectColumnName(studyMode);
        //String intervalColumn = getIntervalColumnName(studyMode);
        String eFactorColumn = getEasinessFactorColumnName(studyMode);
        SQLiteDatabase db = mHelper.getReadableDatabase();
        String[] columns = {
                VocabEntry.ID,
                VocabEntry.MONGOL,
                VocabEntry.DEFINITION,
                VocabEntry.PRONUNCIATION,
                VocabEntry.AUDIO_FILENAME,
                nextDueDateColumn,
                consecutiveCorrectColumn,
                eFactorColumn
        };
        String selection = VocabEntry.LIST_ID + " LIKE ? AND " +
                nextDueDateColumn + " < " + midnightTonight;
        String[] selectionArgs = {String.valueOf(listId)};
        String orderBy = nextDueDateColumn;
        Cursor cursor = db.query(VocabEntry.VOCAB_TABLE, columns, selection, selectionArgs,
                null,null, orderBy, null);
        int indexId = cursor.getColumnIndex(VocabEntry.ID);
        int indexMongol = cursor.getColumnIndex(VocabEntry.MONGOL);
        int indexDefinition = cursor.getColumnIndex(VocabEntry.DEFINITION);
        int indexPronunciation = cursor.getColumnIndex(VocabEntry.PRONUNCIATION);
        int indexAudio = cursor.getColumnIndex(VocabEntry.AUDIO_FILENAME);
        int indexDate = cursor.getColumnIndex(nextDueDateColumn);
        int indexConsecutiveCorrect = cursor.getColumnIndex(consecutiveCorrectColumn);
        int indexEF = cursor.getColumnIndex(eFactorColumn);

        while (cursor.moveToNext()) {
            Vocab vocabItem = new Vocab(studyMode);
            vocabItem.setId(cursor.getLong(indexId));
            vocabItem.setListId(listId);
            vocabItem.setMongol(cursor.getString(indexMongol));
            vocabItem.setDefinition(cursor.getString(indexDefinition));
            vocabItem.setPronunciation(cursor.getString(indexPronunciation));
            vocabItem.setAudioFilename(cursor.getString(indexAudio));
            vocabItem.setNextDueDate(cursor.getLong(indexDate));
            vocabItem.setConsecutiveCorrect(cursor.getInt(indexConsecutiveCorrect));
            vocabItem.setEasinessFactor(cursor.getFloat(indexEF));
            vocabList.add(vocabItem);
        }

        cursor.close();
        db.close();

        return vocabList;

    }

    private String getNextDueDateColumnName(StudyMode studyMode) {
        switch (studyMode) {
            case DEFINITION:
                return VocabEntry.DEFINITION_NEXT_DUE_DATE;
            case PRONUNCIATION:
                return VocabEntry.PRONUNCIATION_NEXT_DUE_DATE;
            default:
                return VocabEntry.MONGOL_NEXT_DUE_DATE;
        }
    }

    private String getConsecutiveCorrectColumnName(StudyMode studyMode) {
        switch (studyMode) {
            case DEFINITION:
                return VocabEntry.DEFINITION_CONSECUTIVE_CORRECT;
            case PRONUNCIATION:
                return VocabEntry.PRONUNCIATION_CONSECUTIVE_CORRECT;
            default:
                return VocabEntry.MONGOL_CONSECUTIVE_CORRECT;
        }
    }

//    private String getIntervalColumnName(StudyMode studyMode) {
//        switch (studyMode) {
//            case DEFINITION:
//                return VocabEntry.DEFINITION_INTERVAL;
//            case PRONUNCIATION:
//                return VocabEntry.PRONUNCIATION_INTERVAL;
//            default:
//                return VocabEntry.MONGOL_INTERVAL;
//        }
//    }

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

        String nextDueDateColumn = getNextDueDateColumnName(studyMode);
        String consecutiveCorrectColumn = getConsecutiveCorrectColumnName(studyMode);
        String eFactorColumn = getEasinessFactorColumnName(studyMode);

        SQLiteDatabase db = mHelper.getReadableDatabase();
        String[] columns = {
                VocabEntry.ID,
                VocabEntry.LIST_ID,
                VocabEntry.MONGOL,
                VocabEntry.DEFINITION,
                VocabEntry.PRONUNCIATION,
                VocabEntry.AUDIO_FILENAME,
                nextDueDateColumn,
                consecutiveCorrectColumn,
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
        int indexAudio = cursor.getColumnIndex(VocabEntry.AUDIO_FILENAME);
        int indexDate = cursor.getColumnIndex(nextDueDateColumn);
        int indexNthTry = cursor.getColumnIndex(consecutiveCorrectColumn);
        int indexEF = cursor.getColumnIndex(eFactorColumn);

        Vocab vocabItem = new Vocab(studyMode);
        if (cursor.moveToFirst()) {
            vocabItem.setId(cursor.getLong(indexId));
            vocabItem.setListId(cursor.getLong(indexListId));
            vocabItem.setMongol(cursor.getString(indexMongol));
            vocabItem.setDefinition(cursor.getString(indexDefinition));
            vocabItem.setPronunciation(cursor.getString(indexPronunciation));
            vocabItem.setAudioFilename(cursor.getString(indexAudio));
            vocabItem.setNextDueDate(cursor.getLong(indexDate));
            vocabItem.setConsecutiveCorrect(cursor.getInt(indexNthTry));
            vocabItem.setEasinessFactor(cursor.getFloat(indexEF));
        }

        cursor.close();
        db.close();

        return vocabItem;
    }

    public long addVocabItem(Vocab entry) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(VocabEntry.LIST_ID, entry.getListId());
        contentValues.put(VocabEntry.MONGOL, entry.getMongol());
        contentValues.put(VocabEntry.DEFINITION, entry.getDefinition());
        contentValues.put(VocabEntry.PRONUNCIATION, entry.getPronunciation());
        contentValues.put(VocabEntry.AUDIO_FILENAME, entry.getAudioFilename());

        contentValues.put(VocabEntry.MONGOL_NEXT_DUE_DATE, entry.getNextDueDate());
        contentValues.put(VocabEntry.MONGOL_CONSECUTIVE_CORRECT, entry.getConsecutiveCorrect());
        contentValues.put(VocabEntry.MONGOL_EF, entry.getEasinessFactor());

        contentValues.put(VocabEntry.DEFINITION_NEXT_DUE_DATE, entry.getNextDueDate());
        contentValues.put(VocabEntry.DEFINITION_CONSECUTIVE_CORRECT, entry.getConsecutiveCorrect());
        //contentValues.put(VocabEntry.DEFINITION_INTERVAL, entry.getInterval());
        contentValues.put(VocabEntry.DEFINITION_EF, entry.getEasinessFactor());

        contentValues.put(VocabEntry.PRONUNCIATION_NEXT_DUE_DATE, entry.getNextDueDate());
        contentValues.put(VocabEntry.PRONUNCIATION_CONSECUTIVE_CORRECT, entry.getConsecutiveCorrect());
        //contentValues.put(VocabEntry.PRONUNCIATION_INTERVAL, entry.getInterval());
        contentValues.put(VocabEntry.PRONUNCIATION_EF, entry.getEasinessFactor());

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
                contentValues.put(VocabEntry.AUDIO_FILENAME, item.getAudioFilename());

                contentValues.put(VocabEntry.MONGOL_NEXT_DUE_DATE, item.getNextDueDate());
                contentValues.put(VocabEntry.MONGOL_CONSECUTIVE_CORRECT, item.getConsecutiveCorrect());
                //contentValues.put(VocabEntry.MONGOL_INTERVAL, item.getInterval());
                contentValues.put(VocabEntry.MONGOL_EF, item.getEasinessFactor());

                contentValues.put(VocabEntry.DEFINITION_NEXT_DUE_DATE, item.getNextDueDate());
                contentValues.put(VocabEntry.DEFINITION_CONSECUTIVE_CORRECT, item.getConsecutiveCorrect());
                //contentValues.put(VocabEntry.DEFINITION_INTERVAL, item.getInterval());
                contentValues.put(VocabEntry.DEFINITION_EF, item.getEasinessFactor());

                contentValues.put(VocabEntry.PRONUNCIATION_NEXT_DUE_DATE, item.getNextDueDate());
                contentValues.put(VocabEntry.PRONUNCIATION_CONSECUTIVE_CORRECT, item.getConsecutiveCorrect());
                //contentValues.put(VocabEntry.PRONUNCIATION_INTERVAL, item.getInterval());
                contentValues.put(VocabEntry.PRONUNCIATION_EF, item.getEasinessFactor());

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
        String nextDueDateColumn = getNextDueDateColumnName(studyMode);
        String consecutiveCorrectColumn = getConsecutiveCorrectColumnName(studyMode);
        String eFactorColumn = getEasinessFactorColumnName(studyMode);

        ContentValues contentValues = new ContentValues();
        contentValues.put(VocabEntry.LIST_ID, updatedItem.getListId());
        contentValues.put(VocabEntry.MONGOL, updatedItem.getMongol());
        contentValues.put(VocabEntry.DEFINITION, updatedItem.getDefinition());
        contentValues.put(VocabEntry.PRONUNCIATION, updatedItem.getPronunciation());
        contentValues.put(VocabEntry.AUDIO_FILENAME, updatedItem.getAudioFilename());
        contentValues.put(nextDueDateColumn, updatedItem.getNextDueDate());
        contentValues.put(consecutiveCorrectColumn, updatedItem.getConsecutiveCorrect());
        contentValues.put(eFactorColumn, updatedItem.getEasinessFactor());

        String selection = VocabEntry.ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(updatedItem.getId())};

        SQLiteDatabase db = mHelper.getWritableDatabase();
        int id = db.update(VocabEntry.VOCAB_TABLE, contentValues, selection, selectionArgs);
        db.close();
        return id;
    }

    public void updateVocabItemPracticeData(
            StudyMode studyMode,
            long vocabId,
            long nextPracticeDate,
            int nthTry,
            float eFactor) {

        String nextDueDateColumn = getNextDueDateColumnName(studyMode);
        String consecutiveCorrectColumn = getConsecutiveCorrectColumnName(studyMode);
        String eFactorColumn = getEasinessFactorColumnName(studyMode);

        ContentValues contentValues = new ContentValues();
        contentValues.put(nextDueDateColumn, nextPracticeDate);
        contentValues.put(consecutiveCorrectColumn, nthTry);
        contentValues.put(eFactorColumn, eFactor);

        String selection = VocabEntry.ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(vocabId)};

        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.update(VocabEntry.VOCAB_TABLE, contentValues, selection, selectionArgs);
        db.close();
    }

    public void deleteVocabItem(long rowId) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        String whereClause = VocabEntry.ID + " =?";
        String[] whereArgs = {Long.toString(rowId)};
        db.delete(VocabEntry.VOCAB_TABLE, whereClause, whereArgs);
        db.close();
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

        SQLiteDatabase db = mHelper.getWritableDatabase();
        String uniqueName = getUniqueListName(name, db);

        ContentValues contentValues = new ContentValues();
        contentValues.put(ListsEntry.LIST_NAME, uniqueName);
        contentValues.put(ListsEntry.DATE_ACCESSED, System.currentTimeMillis());

        long id = db.insertOrThrow(ListsEntry.LIST_TABLE, null, contentValues);
        db.close();
        return id;
    }

    private String getUniqueListName(String name, SQLiteDatabase db) {
        String[] columns = {ListsEntry.LIST_ID};
        String selection = ListsEntry.LIST_NAME + " = ?";

        int count = 1;
        String newName = name;
        boolean nameExists = true;
        while (nameExists) {
            String[] selectionArgs = {newName};
            Cursor cursor = db.query(ListsEntry.LIST_TABLE, columns, selection, selectionArgs, null,
                    null, null, null);
            nameExists = cursor.moveToFirst();
            cursor.close();
            if (!nameExists) break;
            newName = name + " (" + count + ")";
            count++;
        }
        return newName;
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
}