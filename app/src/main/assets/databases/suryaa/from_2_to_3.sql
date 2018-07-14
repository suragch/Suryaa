-- version 2 of the database vocab table was
--
-- CREATE TABLE vocab
-- ( _id INTEGER PRIMARY KEY,
--   list_id INTEGER NOT NULL,
--   mongol TEXT,
--   definition TEXT,
--   pronunciation TEXT,
--   audio_filename TEXT,
--   mongol_next_due_date INTEGER DEFAULT 0,
--   mongol_consecutive_correct INTEGER DEFAULT 0,
--   mongol_easiness_factor REAL DEFAULT 2.5,
--   definition_next_due_date INTEGER DEFAULT 0,
--   definition_consecutive_correct INTEGER DEFAULT 0,
--   definition_easiness_factor REAL DEFAULT 2.5,
--   pronunciation_next_due_date INTEGER DEFAULT 0,
--   pronunciation_consecutive_correct INTEGER DEFAULT 0,
--   pronunciation_easiness_factor REAL DEFAULT 2.5,
--   FOREIGN KEY(list_id) REFERENCES lists(_id)
-- ); -- end comment (comment lines cannot end with semicolon)

PRAGMA foreign_keys=off;

BEGIN TRANSACTION;

ALTER TABLE vocab ADD COLUMN example_sentences TEXT DEFAULT '';

COMMIT;

PRAGMA foreign_keys=on;