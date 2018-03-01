package net.studymongolian.suryaa.database;

public class VocabEntry {

    // Vocab table
    static final String VOCAB_TABLE = "vocab";

    // Column names

    static final String ID = "_id";
    static final String LIST_ID = "list_id";
    static final String MONGOL = "mongol";
    static final String DEFINITION = "definition";
    static final String PRONUNCIATION = "pronunciation";
    static final String AUDIO_FILENAME = "audio_filename";
    static final String MONGOL_NEXT_PRACTICE_DATE = "mongol_next_practice_date";
    static final String MONGOL_NTH_TRY = "mongol_nth_try";
    static final String MONGOL_INTERVAL = "mongol_interval";
    static final String MONGOL_EF = "mongol_easiness_factor";
    static final String DEFINITION_NEXT_PRACTICE_DATE = "definition_next_practice_date";
    static final String DEFINITION_NTH_TRY = "definition_nth_try";
    static final String DEFINITION_INTERVAL = "definition_interval";
    static final String DEFINITION_EF = "definition_easiness_factor";
    static final String PRONUNCIATION_NEXT_PRACTICE_DATE = "pronunciation_next_practice_date";
    static final String PRONUNCIATION_NTH_TRY = "pronunciation_nth_try";
    static final String PRONUNCIATION_INTERVAL = "pronunciation_interval";
    static final String PRONUNCIATION_EF = "pronunciation_easiness_factor";

//    private static final int DEFAULT_NEXT_PRACTICE_DATE = 0;
//    private static final int DEFAULT_NTH_TRY = 1;
//    private static final int DEFAULT_INTERVAL = 1;
//    private static final float DEFAULT_EASINESS_FACTOR = 2.5f;

//    static final String VOCAB_ID = "_id";
//    static final String NEXT_PRACTICE_DATE = "date";
//    static final String LIST = "list_id";
//    static final String MONGOL = "mongol";
//    static final String DEFINITION = "definition";
//    static final String PRONUNCIATION = "pronunciation";
//    static final String AUDIO_FILE = "audio";

    // SQL statements
    static final String CREATE_VOCAB_TABLE = "CREATE TABLE " + VOCAB_TABLE + " ("
            + ID + " INTEGER PRIMARY KEY,"
            + LIST_ID + " INTEGER NOT NULL,"
            + MONGOL + " TEXT,"
            + DEFINITION + " TEXT,"
            + PRONUNCIATION + " TEXT,"
            + AUDIO_FILENAME + " TEXT,"
            + MONGOL_NEXT_PRACTICE_DATE + " INTEGER DEFAULT 0,"
            + MONGOL_NTH_TRY + " INTEGER DEFAULT 1,"
            + MONGOL_INTERVAL + " INTEGER DEFAULT 1,"
            + MONGOL_EF + " REAL DEFAULT 2.5,"
            + DEFINITION_NEXT_PRACTICE_DATE + " INTEGER DEFAULT 0,"
            + DEFINITION_NTH_TRY + " INTEGER DEFAULT 1,"
            + DEFINITION_INTERVAL + " INTEGER DEFAULT 1,"
            + DEFINITION_EF + " REAL DEFAULT 2.5,"
            + PRONUNCIATION_NEXT_PRACTICE_DATE + " INTEGER DEFAULT 0,"
            + PRONUNCIATION_NTH_TRY + " INTEGER DEFAULT 1,"
            + PRONUNCIATION_INTERVAL + " INTEGER DEFAULT 1,"
            + PRONUNCIATION_EF + " REAL DEFAULT 2.5,"
            + "FOREIGN KEY(" + LIST_ID + ") "
            + "REFERENCES " + ListsEntry.LIST_TABLE + "(" + ListsEntry.LIST_ID + "))";

}
