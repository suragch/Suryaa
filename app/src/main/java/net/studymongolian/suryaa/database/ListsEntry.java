package net.studymongolian.suryaa.database;

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
            + DATE_ACCESSED + " INTEGER NOT NULL)";

}
