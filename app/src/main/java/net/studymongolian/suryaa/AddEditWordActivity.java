package net.studymongolian.suryaa;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import net.studymongolian.mongollibrary.ImeContainer;
import net.studymongolian.mongollibrary.Keyboard;
import net.studymongolian.mongollibrary.KeyboardAeiou;
import net.studymongolian.mongollibrary.MongolEditText;
import net.studymongolian.mongollibrary.MongolFont;
import net.studymongolian.mongollibrary.MongolInputMethodManager;
import net.studymongolian.suryaa.database.DatabaseManager;
import net.studymongolian.suryaa.database.ListsEntry;

import java.io.File;
import java.io.IOException;


public class AddEditWordActivity extends AppCompatActivity {

    static final String WORD_ID_KEY = "word_id";
    static final String EDIT_MODE_KEY = "edit_mode";
    static final String WORDS_ADDED_KEY = "words_added";
    private static final String TAG = "AddEditWordActivity";
    private boolean wordsWereAdded = false;

    private static final String LOG_TAG = "AddEditWordActivity";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int ADDING_WOPD = -1;
    private static File mTempAudioFilePathName = null;

    ImageView mRecordButton;
    private MediaRecorder mRecorder = null;
    private Handler mRecordingIndicatorHandler;
    private static final int RECORDING_INDICATOR_DELAY = 500; // milliseconds
    ImageView mPlayButton;
    private MediaPlayer mPlayer = null;

    MongolEditText mongolEditText;
    EditText etDefinition;
    EditText etPronunciation;

    private long mCurrentListId;
    private Vocab mEditingWord;
    private StudyMode mStudyMode = StudyMode.MONGOL;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);

        initToolbar();
        initViews();
        initKeyboard();
        loadInfoFromIntent();
        initTempFile();
        requestAudioRecordingPermission();
        setMongolFont();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void initViews() {
        mRecordButton = findViewById(R.id.ivRecordButton);
        mRecordButton.setOnTouchListener(mRecordListener);
        mPlayButton = findViewById(R.id.ivPlayButton);
        mPlayButton.setOnClickListener(mPlayButtonClickListener);
    }

    private void initKeyboard() {
        mongolEditText = findViewById(R.id.metMongolWord);
        etDefinition = findViewById(R.id.etDefinition);
        etPronunciation = findViewById(R.id.etPronunciation);
        MyInputMethodService imeContainer = findViewById(R.id.keyboard_container);

        MyMimm mimm = new MyMimm();
        mimm.addEditor(etDefinition, true);
        mimm.addEditor(etPronunciation, false);
        mimm.addEditor(mongolEditText, false);
        mimm.setIme(imeContainer);
    }

    private void initTempFile() {
        if (getExternalCacheDir() == null) return;
        mTempAudioFilePathName = new File(
                getExternalCacheDir().getAbsolutePath() + "/"
                        + FileUtils.TEMP_AUDIO_FILE_NAME);
        deleteTempFile();
    }

    private void loadInfoFromIntent() {
        Intent intent = getIntent();
        mCurrentListId = intent.getLongExtra(MainActivity.LIST_ID_KEY, 1);
        long editingWordId = intent.getLongExtra(WORD_ID_KEY, ADDING_WOPD);
        if (editingWordId != ADDING_WOPD) {
            new LoadEditingWord().execute(editingWordId);
        }
    }

    private void setPlayButtonVisibility() {
        if (audioFileExists()) {
            mPlayButton.setVisibility(View.VISIBLE);
        } else {
            mPlayButton.setVisibility(View.INVISIBLE);
        }
    }

    private boolean audioFileExists() {
        if (mTempAudioFilePathName.exists()) return true;
        String audioPath = getAudioPathName(mCurrentListId, mEditingWord.getAudioFilename());
        if (audioPath == null) return false;
        if (!audioPath.endsWith(FileUtils.AUDIO_FILE_EXTENSION)) return false;
        File audioFile = new File(audioPath);
        return audioFile.exists();
    }

    private void requestAudioRecordingPermission() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private void setMongolFont() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String fontStyle = sharedPref.getString(SettingsActivity.KEY_PREF_FONT, SettingsActivity.KEY_PREF_FONT_DEFAULT);
        if (!fontStyle.equals(SettingsActivity.KEY_PREF_FONT_DEFAULT)) {
            mongolEditText.setTypeface(MongolFont.get(SettingsActivity.QIMED, getApplicationContext()));
        }
    }

    private View.OnTouchListener mRecordListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startRecording(mTempAudioFilePathName);
                    v.setBackgroundResource(R.drawable.btn_record_default_pressed);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    stopRecording();
                    v.setBackgroundResource(0);
                    break;
            }
            return true;
        }
    };

    private View.OnClickListener mPlayButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mPlayer != null) {
                mPlayer.release();
                mPlayer = null;
            }
            mPlayer = new MediaPlayer();
            setPlayingImage();
            changeImageBackWhenFinishedPlaying();
            try {
                String dataSource = mTempAudioFilePathName.getAbsolutePath();
                if (!mTempAudioFilePathName.exists() && mEditingWord != null) {
                    dataSource = getAudioPathName(mCurrentListId, mEditingWord.getAudioFilename());
                }
                mPlayer.setDataSource(dataSource);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }
        }

        private void setPlayingImage() {
            mPlayButton.setImageResource(R.drawable.play_button_playing);
        }

        private void changeImageBackWhenFinishedPlaying() {
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mPlayButton.setImageResource(R.drawable.play_button);
                }
            });
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                // fixme this is returning true even when I say no on Android 5.0
                break;
        }
        if (!permissionToRecordAccepted) {
            finish();
        }
        // TODO don't finish, just disable audio recording button
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_word, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.miSaveWord:
                saveWord();


                return true;
            case android.R.id.home:
                deleteTempFile();
                Intent intent = new Intent();
                intent.putExtra(WORDS_ADDED_KEY, wordsWereAdded);
                setResult(RESULT_OK, intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveWord() {
        if (notEnoughDataToSaveWord()) {
            Toast.makeText(this, R.string.addedit_error_need_more_data, Toast.LENGTH_LONG).show();
            return;
        }

        Vocab vocab = (isNewVocab()) ? new Vocab(mStudyMode) : mEditingWord;
        vocab.setListId(mCurrentListId);
        vocab.setMongol(mongolEditText.getText().toString());
        vocab.setDefinition(etDefinition.getText().toString());
        vocab.setPronunciation(etPronunciation.getText().toString());
        if (mTempAudioFilePathName.exists()) {
            deleteAudioFile(vocab);
            vocab.setAudioFilename(createAudioFileName(vocab));
        }

        if (isNewVocab()) {
            vocab.setListId(mCurrentListId);
            vocab.setNextDueDate(System.currentTimeMillis());
            vocab.setConsecutiveCorrect(Vocab.DEFAULT_CONSECUTIVE_CORRECT);
            vocab.setEasinessFactor(Vocab.DEFAULT_EASINESS_FACTOR);
            new AddVocab().execute(vocab);
        } else {
            new UpdateVocab().execute(vocab);
        }
    }

    private String createAudioFileName(Vocab vocab) {
        String name = choosePotentialFileNameString(vocab);
        name = abbreviate(name);
        name = ensureUniqueAudioFile(vocab.getListId(), name);
        return name + FileUtils.AUDIO_FILE_EXTENSION;
    }

    private String choosePotentialFileNameString(Vocab vocab) {
        if (!TextUtils.isEmpty(vocab.getMongol())) {
            return MongolUtils.romanize(vocab.getMongol());
        } else if (!TextUtils.isEmpty(vocab.getDefinition())) {
            return vocab.getDefinition();
        } else {
            return vocab.getPronunciation();
        }
    }

    private String abbreviate(String name) {
        final int abbreviationLength = 16;
        int start = 0;
        int end = Math.min(name.length(), abbreviationLength);
        return name.substring(start, end);
    }

    private String ensureUniqueAudioFile(long listId, String name) {
        int count = 1;
        String newName = name;
        boolean nameExists = true;
        while (nameExists) {
            String filename = newName + FileUtils.AUDIO_FILE_EXTENSION;
            String path = getAudioPathName(listId, filename);
            if (path == null) return name;
            File file = new File(path);
            if (!file.exists()) break;
            newName = name + "(" + count + ")";
            count++;
        }
        return newName;
    }

    private boolean isNewVocab() {
        return mEditingWord == null;
    }

    private boolean notEnoughDataToSaveWord() {
        return (TextUtils.isEmpty(mongolEditText.getText())
                && TextUtils.isEmpty(etDefinition.getText())
                && TextUtils.isEmpty(etPronunciation.getText()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void startRecording(File file) {
        if (mRecorder != null) {
            mRecorder.release();
        }
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        mRecorder.setOutputFile(file.getAbsolutePath());
        try {
            mRecorder.prepare();
            mRecorder.start();
            mRecordingIndicatorHandler = new Handler();
            mRecordingIndicatorHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getSupportActionBar() == null) return;
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.RED));
                }
            }, RECORDING_INDICATOR_DELAY);
        } catch (IOException e) {
            Log.e("LOG", "io problems while preparing [" +
                    file.getAbsolutePath() + "]: " + e.getMessage());
        }
    }

    private void stopRecording() {
        if (mRecorder != null) {
            if (mRecordingIndicatorHandler != null) {
                mRecordingIndicatorHandler.removeCallbacksAndMessages(null);
            }
            try {
                mRecorder.stop();
            } catch (RuntimeException stopException) {
                //handle cleanup here
                deleteTempFile();
            }
            mRecorder.release();
            mRecorder = null;
        }
        if (getSupportActionBar() == null) return;
        getSupportActionBar().setBackgroundDrawable(getThemePrimaryColor());
        setPlayButtonVisibility();
    }

    private ColorDrawable getThemePrimaryColor() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        int color = typedValue.data;
        return new ColorDrawable(color);
    }

    private String getAudioPathName(long listId, String fileName) {
        File externalDir = getExternalFilesDir(null);
        if (externalDir == null) return null;
        return externalDir.getAbsolutePath() + File.separator + listId + File.separator + fileName;
    }


    private void deleteAudioFile(Vocab vocab) {
        if (TextUtils.isEmpty(vocab.getAudioFilename()))
            return;
        String filePathName = getAudioPathName(vocab.getListId(), vocab.getAudioFilename());
        if (filePathName == null) return;
        try {
            File file = new File(filePathName);
            file.delete();
        } catch (SecurityException e) {
            Log.e("deleteAudioFile: ", e.getMessage());
        }
        return;
    }

    private void deleteTempFile() {

        try {
            mTempAudioFilePathName.delete();
        } catch (SecurityException e) {
            Log.e("tag", e.getMessage());
        }
    }

    private class AddVocab extends AsyncTask<Vocab, Void, Void> {

        @Override
        protected Void doInBackground(Vocab... params) {

            Vocab item = params[0];


            long vocabId = -1;
            try {
                DatabaseManager dbAdapter = new DatabaseManager(getApplicationContext());
                vocabId = dbAdapter.addVocabItem(item);
            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            if (vocabId >= 0) {
                item.setId(vocabId);
                copyTempFileToStorage(item);
            } else
                Log.e(TAG, "Vocab item not added");


            return null;
        }

        @Override
        protected void onPostExecute(Void results) {
            Toast.makeText(AddEditWordActivity.this, "new word added", Toast.LENGTH_SHORT).show();
            mongolEditText.setText("");
            etDefinition.setText("");
            etPronunciation.setText("");
            mongolEditText.requestFocus();
            wordsWereAdded = true;
            deleteTempFile();
        }

    }

    private void copyTempFileToStorage(Vocab item) {
        if (!mTempAudioFilePathName.exists()) return;
        if (TextUtils.isEmpty(item.getAudioFilename())) return;

        String filePath = getAudioPathName(item.getListId(), item.getAudioFilename());
        if (filePath == null) return;

        File destFile = new File(filePath);
        try {
            FileUtils.copyFile(mTempAudioFilePathName, destFile);
        } catch (IOException e) {
            Log.i("testing", "doInBackground: IOException");
            e.printStackTrace();
        }
    }

    private class UpdateVocab extends AsyncTask<Vocab, Void, Void> {

        @Override
        protected Void doInBackground(Vocab... params) {

            Vocab item = params[0];
            long vocabId = -1;
            try {

                DatabaseManager dbAdapter = new DatabaseManager(getApplicationContext());
                vocabId = dbAdapter.updateVocabItem(item);
            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            if (vocabId >= 0) {
                item.setId(vocabId);
                copyTempFileToStorage(item);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void results) {
            Toast.makeText(AddEditWordActivity.this, "updated", Toast.LENGTH_SHORT).show();
            mongolEditText.setText("");
            etDefinition.setText("");
            etPronunciation.setText("");
            deleteTempFile();
            Intent intent = new Intent();
            intent.putExtra(EDIT_MODE_KEY, true);
            setResult(RESULT_OK, intent);
            finish();
        }

    }

    private class LoadEditingWord extends AsyncTask<Long, Void, Vocab> {

        @Override
        protected Vocab doInBackground(Long... params) {

            long vocabId = params[0];

            Vocab vocabItem = null;

            try {

                DatabaseManager dbAdapter = new DatabaseManager(getApplicationContext());
                vocabItem = dbAdapter.getVocabItem(vocabId, mStudyMode);
            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            return vocabItem;

        }

        @Override
        protected void onPostExecute(Vocab vocabItem) {
            mongolEditText.setText(vocabItem.getMongol());
            etDefinition.setText(vocabItem.getDefinition());
            etPronunciation.setText(vocabItem.getPronunciation());
            mEditingWord = vocabItem;
            setPlayButtonVisibility();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.add_edit_word_activity_edit_title);
            }
        }

    }


}
