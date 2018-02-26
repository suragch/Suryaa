package net.studymongolian.suryaa.database;

public class VocabEntry {

    // Vocab table
    static final String VOCAB_TABLE = "vocab";
    // Column names
    static final String VOCAB_ID = "_id";
    static final String NEXT_PRACTICE_DATE = "date";
    static final String LIST = "list_id";
    static final String MONGOL = "mongol";
    static final String DEFINITION = "definition";
    static final String PRONUNCIATION = "pronunciation";
    static final String AUDIO_FILE = "audio";
    // SQL statements
    static final String CREATE_VOCAB_TABLE = "CREATE TABLE " + VOCAB_TABLE + " ("
            + VOCAB_ID + " INTEGER PRIMARY KEY,"
            + NEXT_PRACTICE_DATE + " INTEGER,"
            + LIST + " TEXT NOT NULL,"
            + MONGOL + " TEXT NOT NULL,"
            + DEFINITION + " TEXT,"
            + PRONUNCIATION + " TEXT,"
            + AUDIO_FILE + " TEXT,"
            + "FOREIGN KEY(" + LIST + ") REFERENCES " + ListsEntry.LIST_TABLE + "(" + ListsEntry.LIST_ID + "))";
    static final String DROP_VOCAB_TABLE = "DROP TABLE IF EXISTS "
            + VOCAB_TABLE;

}
