package net.studymongolian.suryaa;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import net.studymongolian.mongollibrary.MongolFont;
import net.studymongolian.mongollibrary.MongolLabel;
import net.studymongolian.suryaa.database.DatabaseManager;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    static final String LIST_ID_KEY = "listKey";
    private static final int LIST_ACTIVITY_REQUEST_CODE = 0;
    private static final int ADD_WORD_ACTIVITY_REQUEST_CODE = 1;
    private static final int SETTINGS_ACTIVITY_REQUEST_CODE = 2;
    private static final String LOG_TAG = "MainActivity";
    private static final String TAG = "MainActivity";

    private TextView mNumberOfWordsView;
    private ScrollView mAnswerPanel;
    private TextView mAnswerButton;
    private FrameLayout mButtonPanel;
    private LinearLayout mAnswerButtonLayout;
    private VocabList mCurrentList;
    private Queue<Vocab> mTodaysQuestions;
    private Vocab mCurrentVocabItem;
    private MongolLabel mMongolView;
    private TextView mDefinitionView;
    private TextView mPronunciationView;
    private ImageView mPlayButton;
    private MediaPlayer mPlayer = null;
    private boolean mLocked = false; // don't allow operations during db background tasks
    private StudyMode mStudyMode = StudyMode.MONGOL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsActivity.setNightMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        mNumberOfWordsView = findViewById(R.id.tv_number_of_words);
        mAnswerPanel = findViewById(R.id.answer_panel);
        mAnswerButton = findViewById(R.id.answer_button);
        mButtonPanel = findViewById(R.id.button_panel);
        mAnswerButtonLayout = findViewById(R.id.answer_button_layout);
        mMongolView = findViewById(R.id.ml_mongol_vocab);
        mDefinitionView = findViewById(R.id.tv_definition);
        mPronunciationView = findViewById(R.id.tv_pronunciation);
        mPlayButton = findViewById(R.id.ib_play_audio);
        mPlayButton.setOnClickListener(mPlayButtonClickListener);

        // set the MongolFont
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String fontStyle = sharedPref.getString(SettingsActivity.KEY_PREF_FONT, SettingsActivity.KEY_PREF_FONT_DEFAULT);
        if (!fontStyle.equals(SettingsActivity.KEY_PREF_FONT_DEFAULT)) {
            mMongolView.setTypeface(MongolFont.get(SettingsActivity.QIMED, getApplicationContext()));
        }

        // get current list
        long currentListId = sharedPref.getLong(SettingsActivity.KEY_PREF_CURRENT_LIST, 1);
        // 1 is the default id (used on first run)
        // load current vocab item
        new GetTodaysVocab().execute(currentListId);

    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem itemDelete = menu.findItem(R.id.mi_delete);
        MenuItem itemEdit = menu.findItem(R.id.mi_edit);
        if (mCurrentVocabItem == null) {
            itemDelete.setVisible(false);
            itemEdit.setVisible(false);
        } else {
            itemDelete.setVisible(true);
            itemEdit.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.mi_list:
                intent = new Intent(this, ListsActivity.class);
                startActivityForResult(intent, LIST_ACTIVITY_REQUEST_CODE);
                return true;
            case R.id.mi_add:
                intent = new Intent(this, AddEditWordActivity.class);
                intent.putExtra(LIST_ID_KEY, mCurrentList.getListId());
                startActivityForResult(intent, ADD_WORD_ACTIVITY_REQUEST_CODE);
                return true;
            case R.id.mi_delete:
                deleteWord();
                return true;
            case R.id.mi_edit:
                intent = new Intent(this, AddEditWordActivity.class);
                intent.putExtra(LIST_ID_KEY, mCurrentList.getListId());
                intent.putExtra(AddEditWordActivity.WORD_ID_KEY, mCurrentVocabItem.getId());
                startActivityForResult(intent, ADD_WORD_ACTIVITY_REQUEST_CODE);
                return true;
            case R.id.mi_all_words:
                intent = new Intent(this, AllWordsActivity.class);
                intent.putExtra(LIST_ID_KEY, mCurrentList.getListId());
                startActivity(intent);
                return true;
            case R.id.mi_settings:
                intent = new Intent(this, SettingsActivity.class);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                boolean currentNightMode = sharedPref.getBoolean(
                        SettingsActivity.KEY_PREF_NIGHT_MODE, false);
                String currentFont = sharedPref.getString(
                        SettingsActivity.KEY_PREF_FONT, SettingsActivity.KEY_PREF_FONT_DEFAULT);
                intent.putExtra(SettingsActivity.KEY_PREF_NIGHT_MODE, currentNightMode);
                intent.putExtra(SettingsActivity.KEY_PREF_FONT, currentFont);
                startActivityForResult(intent, SETTINGS_ACTIVITY_REQUEST_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case LIST_ACTIVITY_REQUEST_CODE:

                // get String data from Intent
                long listId = -1;
                if (data != null) {
                    listId = data.getLongExtra(ListsActivity.LIST_ID_KEY, -1);
                }
                new GetTodaysVocab().execute(listId);

                break;
            case ADD_WORD_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {

                    boolean wordsAdded = data.getBooleanExtra(AddEditWordActivity.WORDS_ADDED_KEY, false);
                    if (wordsAdded) {
                        new GetTodaysVocab().execute(mCurrentList.getListId());
                        return;
                    }

                    boolean wordEdited = data.getBooleanExtra(AddEditWordActivity.EDIT_MODE_KEY, false);
                    if (wordEdited) {
                        new RefreshVocabItem().execute(mCurrentVocabItem.getId());
                    }
                }
                break;
            case SETTINGS_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    recreate();
                }
                break;
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }


    private View.OnClickListener mPlayButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String pathName = getPathForCurrentAudioFile();
            if (pathName == null) return; // TODO should hide the button
            if (mPlayer != null) {
                mPlayer.release();
                mPlayer = null;
            }
            mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(pathName);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }
        }
    };

    private String getPathForCurrentAudioFile() {
        String filename = mCurrentVocabItem.getAudioFilename();
        if (TextUtils.isEmpty(filename)) return null;

        File externalDir = getExternalFilesDir(null);
        if (externalDir == null) return null;

        String dirPath = externalDir.getAbsolutePath();
        String pathName =
                dirPath + File.separator +
                mCurrentVocabItem.getListId() + File.separator +
                filename;
        return pathName;
    }

    private void deleteWord() {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this word?");

        // add the buttons
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DeleteVocab().execute(mCurrentVocabItem);
            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onAnswerButtonClick(View view) {
        mAnswerPanel.setVisibility(View.VISIBLE);
        mAnswerButton.setVisibility(View.GONE);
        mAnswerButtonLayout.setVisibility(View.VISIBLE);
    }

    public void onResponseButtonClick(View view) {
        if (mLocked) return;

        switch (view.getId()) {
            case R.id.response_button_1:
                mTodaysQuestions.add(mCurrentVocabItem);
                prepareNextQuestion();
                break;
            case R.id.response_button_2:
                Calendar tomorrow = Calendar.getInstance();
                tomorrow.add(Calendar.DATE, 1);
                mCurrentVocabItem.setNextPracticeDate(tomorrow.getTimeInMillis());
                new UpdateVocabPracticeData().execute(mCurrentVocabItem);
                break;
            case R.id.response_button_3:
                Calendar nextWeek = Calendar.getInstance();
                nextWeek.add(Calendar.DATE, 7);
                mCurrentVocabItem.setNextPracticeDate(nextWeek.getTimeInMillis());
                new UpdateVocabPracticeData().execute(mCurrentVocabItem);
                break;
            case R.id.response_button_4:
                Calendar nextMonth = Calendar.getInstance();
                nextMonth.add(Calendar.DATE, 30);
                mCurrentVocabItem.setNextPracticeDate(nextMonth.getTimeInMillis());
                new UpdateVocabPracticeData().execute(mCurrentVocabItem);
                break;
        }

    }

    private void prepareNextQuestion() {
        mAnswerPanel.setVisibility(View.INVISIBLE);
        mAnswerButton.setVisibility(View.VISIBLE);
        mAnswerButtonLayout.setVisibility(View.GONE);
        mCurrentVocabItem = mTodaysQuestions.poll();
        setQuestionText(mCurrentVocabItem);
    }

    private void setQuestionText(Vocab item) {
        if (item == null) {
            mMongolView.setVisibility(View.INVISIBLE);
            mButtonPanel.setVisibility(View.INVISIBLE);
            mNumberOfWordsView.setText(String.valueOf(0));
            invalidateOptionsMenu();
            return;
        }
        mMongolView.setVisibility(View.VISIBLE);
        mButtonPanel.setVisibility(View.VISIBLE);
        setListNameWithQuestionsLeft();
        mMongolView.setText(item.getMongol());
        mDefinitionView.setText(item.getDefinition());
        mPronunciationView.setText(item.getPronunciation());
        if (TextUtils.isEmpty(item.getAudioFilename())) {
            mPlayButton.setVisibility(View.INVISIBLE);
        } else {
            mPlayButton.setVisibility(View.VISIBLE);
        }
    }

    private void setListNameWithQuestionsLeft() {
        if (mCurrentList == null) return;
        int today = 0;
        if (mTodaysQuestions != null) {
            today = mTodaysQuestions.size();
            if (mCurrentVocabItem != null) today++;
        }
        mNumberOfWordsView.setText(String.valueOf(today));
    }

    private class GetTodaysVocab extends AsyncTask<Long, Void, Queue<Vocab>> {

        private long list;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLocked = true;

        }

        @Override
        protected Queue<Vocab> doInBackground(Long... params) {

            list = params[0];

            Queue<Vocab> results = new LinkedList<>();

            try {

                DatabaseManager dbAdapter = new DatabaseManager(getApplicationContext());
                results = dbAdapter.getTodaysVocab(list, mStudyMode);
            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            return results;

        }

        @Override
        protected void onPostExecute(Queue<Vocab> results) {
            mTodaysQuestions = results;
            mCurrentVocabItem = mTodaysQuestions.poll(); // TODO careful of concurrency issues here
            setQuestionText(mCurrentVocabItem);
            mLocked = false;
            new GetList().execute(list);
        }

    }

    private class GetList extends AsyncTask<Long, Void, VocabList> {

        @Override
        protected VocabList doInBackground(Long... params) {

            long list = params[0];

            VocabList result = null;

            try {

                DatabaseManager dbAdapter = new DatabaseManager(getApplicationContext());
                result = dbAdapter.getList(list);
            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            return result;

        }

        @Override
        protected void onPostExecute(VocabList result) {
            mCurrentList = result;
            setListNameWithQuestionsLeft();
            invalidateOptionsMenu();
        }

    }

    private class UpdateVocabPracticeData extends AsyncTask<Vocab, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLocked = true;

        }

        @Override
        protected Void doInBackground(Vocab... params) {

            Vocab item = params[0];
            final long id = item.getId();
            final long nextPracticeDate = item.getNextPracticeDate();
            final int nthTry = item.getNthTry();
            final int interval = item.getInterval();
            final float eFactor = item.getEasinessFactor();

            try {

                DatabaseManager dbAdapter = new DatabaseManager(getApplicationContext());
                dbAdapter.updateVocabItemPracticeData(
                        mStudyMode, id, nextPracticeDate, nthTry, interval, eFactor);
                // TODO if audio file was updated then change
            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void results) {
            mLocked = false;
            prepareNextQuestion();
        }

    }

    private class RefreshVocabItem extends AsyncTask<Long, Void, Vocab> {

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
            mCurrentVocabItem = vocabItem;
            setQuestionText(vocabItem);
        }

    }

    private class DeleteVocab extends AsyncTask<Vocab, Void, Void> {

        @Override
        protected Void doInBackground(Vocab... params) {

            Vocab item = params[0];

            try {
                DatabaseManager dbAdapter = new DatabaseManager(getApplicationContext());
                dbAdapter.deleteVocabItem(item.getId());
                deleteAudioFile(item);

            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            return null;
        }

        private void deleteAudioFile(Vocab item) {
            if (TextUtils.isEmpty(item.getAudioFilename()))
                return;
            File audioFile = new File(
                    getExternalFilesDir(null),
                    String.valueOf(item.getListId()) + File.separator
                            + item.getAudioFilename());
            if (audioFile.exists() && audioFile.delete())
                Log.i(TAG, "File deleted: " + item.getAudioFilename());
        }

        @Override
        protected void onPostExecute(Void results) {
            prepareNextQuestion();
        }

    }
}
