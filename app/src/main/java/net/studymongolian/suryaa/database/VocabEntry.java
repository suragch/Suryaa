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
    static final String EXAMPLE_SENTENCES = "example_sentences";
    static final String MONGOL_NEXT_DUE_DATE = "mongol_next_due_date";
    static final String MONGOL_CONSECUTIVE_CORRECT = "mongol_consecutive_correct";
    static final String MONGOL_EF = "mongol_easiness_factor";
    static final String DEFINITION_NEXT_DUE_DATE = "definition_next_due_date";
    static final String DEFINITION_CONSECUTIVE_CORRECT = "definition_consecutive_correct";
    static final String DEFINITION_EF = "definition_easiness_factor";
    static final String PRONUNCIATION_NEXT_DUE_DATE = "pronunciation_next_due_date";
    static final String PRONUNCIATION_CONSECUTIVE_CORRECT = "pronunciation_consecutive_correct";
    static final String PRONUNCIATION_EF = "pronunciation_easiness_factor";

    // SQL statements
    static final String CREATE_VOCAB_TABLE = "CREATE TABLE " + VOCAB_TABLE + " ("
            + ID + " INTEGER PRIMARY KEY,"
            + LIST_ID + " INTEGER NOT NULL,"
            + MONGOL + " TEXT,"
            + DEFINITION + " TEXT,"
            + PRONUNCIATION + " TEXT,"
            + AUDIO_FILENAME + " TEXT,"
            + EXAMPLE_SENTENCES + " TEXT,"
            + MONGOL_NEXT_DUE_DATE + " INTEGER DEFAULT 0,"
            + MONGOL_CONSECUTIVE_CORRECT + " INTEGER DEFAULT 0,"
            + MONGOL_EF + " REAL DEFAULT 2.5,"
            + DEFINITION_NEXT_DUE_DATE + " INTEGER DEFAULT 0,"
            + DEFINITION_CONSECUTIVE_CORRECT + " INTEGER DEFAULT 0,"
            + DEFINITION_EF + " REAL DEFAULT 2.5,"
            + PRONUNCIATION_NEXT_DUE_DATE + " INTEGER DEFAULT 0,"
            + PRONUNCIATION_CONSECUTIVE_CORRECT + " INTEGER DEFAULT 0,"
            + PRONUNCIATION_EF + " REAL DEFAULT 2.5,"
            + "FOREIGN KEY(" + LIST_ID + ") "
            + "REFERENCES " + ListsEntry.LIST_TABLE + "(" + ListsEntry.LIST_ID + "))";

}
