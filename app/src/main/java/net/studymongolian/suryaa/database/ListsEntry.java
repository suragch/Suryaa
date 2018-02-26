package net.studymongolian.suryaa.database;

/**
 * Created by yonghu on 2/26/18.
 */

public class ListsEntry {

    // List table
    static final String LIST_TABLE = "lists";
    // Column names
    static final String LIST_ID = "_id";
    static final String LIST_NAME = "lists";
    static final String DATE_ACCESSED = "date_accessed";
    // SQL statements
    static final String CREATE_LIST_TABLE = "CREATE TABLE " + LIST_TABLE + " ("
            + LIST_ID + " INTEGER PRIMARY KEY,"
            + LIST_NAME + " TEXT NOT NULL UNIQUE,"
            //+ NUMBER_OF_ENTRIES + " INTEGER NOT NULL,"
            + DATE_ACCESSED + " INTEGER NOT NULL)";
    static final String DROP_LIST_TABLE = "DROP TABLE IF EXISTS "
            + LIST_TABLE;

}
