package net.studymongolian.suryaa;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import net.studymongolian.mongollibrary.ImeContainer;
import net.studymongolian.mongollibrary.MongolEditText;
import net.studymongolian.mongollibrary.MongolFont;
import net.studymongolian.mongollibrary.MongolInputMethodManager;

import java.io.File;
import java.io.IOException;


public class AddEditWordActivity extends AppCompatActivity {

    static final String WORD_ID_KEY = "word_id";
    static final String EDIT_MODE_KEY = "edit_mode";
    static final String WORDS_ADDED_KEY = "words_added";
    private boolean wordsWereAdded = false;

    private static final String LOG_TAG = "AddEditWordActivity";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
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

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Intent intent = getIntent();
        long editingWordId = intent.getLongExtra(WORD_ID_KEY, -1); // -1 means adding new word, not editing
        mCurrentListId = intent.getLongExtra(MainActivity.LIST_ID_KEY, 1);

        ImageView mRecordButton = findViewById(R.id.ivRecordButton);
        mRecordButton.setOnTouchListener(mRecordListener);
        ImageView mPlayButton = findViewById(R.id.ivPlayButton);
        mPlayButton.setOnClickListener(mPlayButtonClickListener);
        mTempAudioFilePathName = new File(
                getExternalCacheDir().getAbsolutePath() + "/"
                        + FileUtils.TEMP_AUDIO_FILE_NAME);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        mongolEditText = findViewById(R.id.metMongolWord);
        etDefinition = findViewById(R.id.etDefinition);
        etPronunciation = findViewById(R.id.etPronunciation);
        ImeContainer imeContainer = findViewById(R.id.keyboard_container);

        MongolInputMethodManager mimm = new MongolInputMethodManager();
        mimm.addEditor(etDefinition);
        mimm.addEditor(etPronunciation);
        mimm.addEditor(mongolEditText);
        mimm.setIme(imeContainer);
        mimm.setAllowSystemSoftInput(MongolInputMethodManager.SYSTEM_EDITOR_ONLY);
        mimm.startInput();

        deleteTempFile();

        // set the MongolFont
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String fontStyle = sharedPref.getString(SettingsActivity.KEY_PREF_FONT, SettingsActivity.KEY_PREF_FONT_DEFAULT);
        if (!fontStyle.equals(SettingsActivity.KEY_PREF_FONT_DEFAULT)) {
            mongolEditText.setTypeface(MongolFont.get(SettingsActivity.QIMED, getApplicationContext()));
        }

        // edit mode
        if (editingWordId >= 0) {
            getSupportActionBar().setTitle("Edit word");
            new LoadEditingWord().execute(editingWordId);
        }
    }

    private View.OnTouchListener mRecordListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startRecording(mTempAudioFilePathName);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    stopRecording();
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
            try {
                String dataSource = mTempAudioFilePathName.getAbsolutePath();
                if (!mTempAudioFilePathName.exists() && mEditingWord != null) {
                    dataSource = getAudioPathName(mCurrentListId, mEditingWord.getAudioFileName());
                }
                mPlayer.setDataSource(dataSource);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }
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
            //if (mRecordButton != null) mRecordButton.setVisibility(View.INVISIBLE);
            //if (mPlayButton != null) mPlayButton.setVisibility(View.INVISIBLE);
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
                if (TextUtils.isEmpty(mongolEditText.getText().toString())) {
                    // TODO add an alert
                    return true;
                }
                if (mEditingWord != null) { // Edit word
                    Vocab updateVocab = mEditingWord;
                    updateVocab.setDate(System.currentTimeMillis());
                    updateVocab.setMongol(mongolEditText.getText().toString());
                    updateVocab.setDefinition(etDefinition.getText().toString());
                    updateVocab.setPronunciation(etPronunciation.getText().toString());
                    if (mTempAudioFilePathName.exists()) {
                        if (!TextUtils.isEmpty(updateVocab.getAudioFileName())) {
                            deleteAudioFile(updateVocab.getList(), updateVocab.getAudioFileName());
                        }
                        updateVocab.setAudioFileName(String.valueOf(updateVocab.getDate()) + FileUtils.AUDIO_FILE_EXTENSION);
                    }
                    new UpdateVocab().execute(updateVocab);
                } else { // add word
                    Vocab newVocab = new Vocab();
                    newVocab.setDate(System.currentTimeMillis());
                    newVocab.setList(mCurrentListId);
                    newVocab.setMongol(mongolEditText.getText().toString());
                    newVocab.setDefinition(etDefinition.getText().toString());
                    newVocab.setPronunciation(etPronunciation.getText().toString());
                    if (mTempAudioFilePathName.exists()) {
                        newVocab.setAudioFileName(String.valueOf(newVocab.getDate()) + FileUtils.AUDIO_FILE_EXTENSION);
                    }
                    new AddVocab().execute(newVocab);
                }

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

    // TODO replace the temporary ipa buttons with an ipa keyboard
    public void onIpaButtonClick(View view) {
        Button button = (Button) view;
        CharSequence ipa = button.getText();
        // insert at cursor position
        int start = Math.max(etPronunciation.getSelectionStart(), 0);
        int end = Math.max(etPronunciation.getSelectionEnd(), 0);
        etPronunciation.getText().replace(Math.min(start, end), Math.max(start, end),
                ipa, 0, ipa.length());
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
        getSupportActionBar().setBackgroundDrawable(getThemePrimaryColor());
    }

    private ColorDrawable getThemePrimaryColor() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        int color = typedValue.data;
        return new ColorDrawable(color);
    }

    private String getAudioPathName(long listId, String fileName) {
        return getExternalFilesDir(null).getAbsolutePath() + "/" + listId + "/" + fileName;
    }



    private boolean deleteAudioFile(long listId, String fileName) {
        String filePathName = getAudioPathName(listId, fileName);
        try {
            File file = new File(filePathName);
            return file.delete();
        } catch (SecurityException e) {
            Log.e("tag", e.getMessage());
        }
        return false;
    }

    private void deleteTempFile() {

        try {
            boolean fileDeleted = mTempAudioFilePathName.delete();
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
                if (item.getList() <= 0) {
                    // make a new list
                    String listName = "list";
                    long listId = dbAdapter.createNewList(listName);
                    item.setList(listId);
                }
                vocabId = dbAdapter.addVocabItem(item);
            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            //File tempFile = new File(mTempAudioFilePathName);
            if (mTempAudioFilePathName.exists() && !TextUtils.isEmpty(item.getAudioFileName())) {
                File destFile = new File(getExternalFilesDir(null).getAbsolutePath() + "/"
                        + item.getList() + "/" + item.getAudioFileName());
                FileUtils.copyFile(mTempAudioFilePathName, destFile);
            }

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

            //File tempFile = new File(mTempAudioFilePathName);
            if (mTempAudioFilePathName.exists() && !TextUtils.isEmpty(item.getAudioFileName())) {
                File destFile = new File(getExternalFilesDir(null).getAbsolutePath() + "/"
                        + item.getList() + "/" + item.getAudioFileName());
                FileUtils.copyFile(mTempAudioFilePathName, destFile);
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
                vocabItem = dbAdapter.getVocabItem(vocabId);
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
        }

    }


}
