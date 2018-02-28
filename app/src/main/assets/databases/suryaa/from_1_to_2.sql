-- version 1 of the database vocab table was
--
-- CREATE TABLE vocab
-- ( _id INTEGER PRIMARY KEY,
--   date INTEGER,
--   list_id TEXT NOT NULL, -- this should have been INTEGER
--   mongol TEXT NOT NULL,
--   definition TEXT,
--   pronunciation TEXT,
--   audio TEXT,
--   FOREIGN KEY(list_id) REFERENCES lists(_id)
-- );

PRAGMA foreign_keys=off;

BEGIN TRANSACTION;

ALTER TABLE vocab RENAME TO _vocab_old;

CREATE TABLE vocab
( _id INTEGER PRIMARY KEY,
  list_id INTEGER NOT NULL,
  mongol TEXT,
  definition TEXT,
  pronunciation TEXT,
  audio_filename TEXT,
  mongol_next_practice_date INTEGER DEFAULT 0,
  mongol_nth_try INTEGER DEFAULT 1,
  mongol_interval INTEGER DEFAULT 1,
  mongol_easiness_factor REAL DEFAULT 2.5,
  definition_next_practice_date INTEGER DEFAULT 0,
  definition_nth_try INTEGER DEFAULT 1,
  definition_interval INTEGER DEFAULT 1,
  definition_easiness_factor REAL DEFAULT 2.5,
  pronunciation_next_practice_date INTEGER DEFAULT 0,
  pronunciation_nth_try INTEGER DEFAULT 1,
  pronunciation_interval INTEGER DEFAULT 1,
  pronunciation_easiness_factor REAL DEFAULT 2.5,
  FOREIGN KEY(list_id) REFERENCES lists(_id)
);

INSERT INTO vocab (list_id, mongol, definition, pronunciation, audio_filename, mongol_next_practice_date)
  SELECT list_id, mongol, definition, pronunciation, audio, date
  FROM _vocab_old;

DROP TABLE IF EXISTS _vocab_old;

COMMIT;

PRAGMA foreign_keys=on;