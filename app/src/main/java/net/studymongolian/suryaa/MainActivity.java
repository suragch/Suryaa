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
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.studymongolian.mongollibrary.MongolFont;
import net.studymongolian.mongollibrary.MongolLabel;
import net.studymongolian.suryaa.database.DatabaseManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    static final String LIST_ID_KEY = "listKey";
    private static final int LIST_ACTIVITY_REQUEST_CODE = 0;
    private static final int ADD_WORD_ACTIVITY_REQUEST_CODE = 1;
    private static final int SETTINGS_ACTIVITY_REQUEST_CODE = 2;
    private static final int ALL_WORDS_ACTIVITY_REQUEST_CODE = 3;
    private static final String TAG = "MainActivity";
    static final String IPA_FONT = "fonts/FreeSans.ttf";

    private TextView mNumberOfWordsView;
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
    private StudyMode mStudyMode;
    private static final int MIN_QUALITY_FOR_CORRECT = 3;
    private static final int MILLISECONDS_IN_A_DAY = 86400000;
    private static final int DEFAULT_INTERVAL = 6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsActivity.setNightMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        mNumberOfWordsView = findViewById(R.id.tv_number_of_words);
        mAnswerButton = findViewById(R.id.answer_button);
        mButtonPanel = findViewById(R.id.button_panel);
        mAnswerButtonLayout = findViewById(R.id.answer_button_layout);
        mMongolView = findViewById(R.id.ml_mongol_vocab);
        mDefinitionView = findViewById(R.id.tv_definition);
        mPronunciationView = findViewById(R.id.tv_pronunciation);
        mPronunciationView.setTypeface(MongolFont.get(IPA_FONT, this));
        mPlayButton = findViewById(R.id.ib_play_audio);
        mPlayButton.setOnClickListener(mPlayButtonClickListener);

        // set the MongolFont
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String fontStyle = sharedPref.getString(SettingsActivity.KEY_PREF_FONT, SettingsActivity.KEY_PREF_FONT_DEFAULT);
        if (!fontStyle.equals(SettingsActivity.KEY_PREF_FONT_DEFAULT)) {
            mMongolView.setTypeface(MongolFont.get(SettingsActivity.QIMED, getApplicationContext()));
        }

        // get study mode
        String studyModeCode = sharedPref.getString(SettingsActivity.KEY_PREF_STUDY_MODE,
                SettingsActivity.KEY_PREF_STUDY_MODE_CODE_DEFAULT);
        mStudyMode = StudyMode.lookupByCode(studyModeCode);

        // get current list
        long currentListId = sharedPref.getLong(SettingsActivity.KEY_PREF_CURRENT_LIST, 1);
        // 1 is the default id (used on first run)
        new GetTodaysVocab().execute(currentListId);

    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem itemEdit = menu.findItem(R.id.mi_edit);
        MenuItem itemMove = menu.findItem(R.id.mi_move);
        MenuItem itemDelete = menu.findItem(R.id.mi_delete);
        if (mCurrentVocabItem == null) {
            itemEdit.setVisible(false);
            itemMove.setVisible(false);
            itemDelete.setVisible(false);
        } else {
            itemEdit.setVisible(true);
            itemMove.setVisible(true);
            itemDelete.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.mi_list:
                intent = new Intent(this, ListsActivity.class);
                intent.putExtra(ListsActivity.LIST_ID_KEY, mCurrentList.getListId());
                startActivityForResult(intent, LIST_ACTIVITY_REQUEST_CODE);
                return true;
            case R.id.mi_add:
                intent = new Intent(this, AddEditWordActivity.class);
                intent.putExtra(LIST_ID_KEY, mCurrentList.getListId());
                startActivityForResult(intent, ADD_WORD_ACTIVITY_REQUEST_CODE);
                return true;
            case R.id.mi_edit:
                intent = new Intent(this, AddEditWordActivity.class);
                intent.putExtra(LIST_ID_KEY, mCurrentList.getListId());
                intent.putExtra(AddEditWordActivity.WORD_ID_KEY, mCurrentVocabItem.getId());
                startActivityForResult(intent, ADD_WORD_ACTIVITY_REQUEST_CODE);
                return true;
            case R.id.mi_move:
                moveWord();
                return true;
            case R.id.mi_delete:
                deleteWord();
                return true;
            case R.id.mi_all_words:
                intent = new Intent(this, AllWordsActivity.class);
                intent.putExtra(LIST_ID_KEY, mCurrentList.getListId());
                startActivityForResult(intent, ALL_WORDS_ACTIVITY_REQUEST_CODE);
                return true;
            case R.id.mi_settings:
                intent = new Intent(this, SettingsActivity.class);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                boolean currentNightMode = sharedPref.getBoolean(
                        SettingsActivity.KEY_PREF_NIGHT_MODE, false);
                String currentFont = sharedPref.getString(
                        SettingsActivity.KEY_PREF_FONT, SettingsActivity.KEY_PREF_FONT_DEFAULT);
                String currentStudyModeCode = sharedPref.getString(
                        SettingsActivity.KEY_PREF_STUDY_MODE, SettingsActivity.KEY_PREF_STUDY_MODE_CODE_DEFAULT);
                intent.putExtra(SettingsActivity.KEY_PREF_NIGHT_MODE, currentNightMode);
                intent.putExtra(SettingsActivity.KEY_PREF_FONT, currentFont);
                intent.putExtra(SettingsActivity.KEY_PREF_STUDY_MODE, currentStudyModeCode);
                startActivityForResult(intent, SETTINGS_ACTIVITY_REQUEST_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void moveWord() {
        new ShowLists().execute(mCurrentVocabItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case LIST_ACTIVITY_REQUEST_CODE:
                if (data != null) {
                    long listId = data.getLongExtra(ListsActivity.LIST_ID_KEY, -1);
                    if (listId >= 0) {
                        new GetTodaysVocab().execute(listId);
                    } else {
                        handleQuestionsFinished();
                    }
                }
                break;
            case ADD_WORD_ACTIVITY_REQUEST_CODE:
                boolean wordsAdded = data.getBooleanExtra(AddEditWordActivity.WORDS_ADDED_KEY, false);
                if (wordsAdded) {
                    new GetTodaysVocab().execute(mCurrentList.getListId());
                    return;
                }
                boolean wordEdited = data.getBooleanExtra(AddEditWordActivity.EDIT_MODE_KEY, false);
                if (wordEdited) {
                    new RefreshVocabItem().execute(mCurrentVocabItem.getId());
                }
                break;
            case SETTINGS_ACTIVITY_REQUEST_CODE:
                recreate();
                break;
            case ALL_WORDS_ACTIVITY_REQUEST_CODE:
                boolean changesMade = data.getBooleanExtra(AllWordsActivity.CHANGES_MADE_KEY, false);
                if (changesMade) {
                    new GetTodaysVocab().execute(mCurrentList.getListId());
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
            if (pathName == null) {
                v.setVisibility(View.INVISIBLE);
                return;
            }
            if (mPlayer != null) {
                mPlayer.release();
                mPlayer = null;
            }
            mPlayer = new MediaPlayer();
            setPlayingImage();
            changeImageBackWhenFinishedPlaying();
            try {
                mPlayer.setDataSource(pathName);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                Log.e(TAG, "prepare() failed");
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

    private String getPathForCurrentAudioFile() {
        String filename = mCurrentVocabItem.getAudioFilename();
        if (TextUtils.isEmpty(filename)) return null;

        File externalDir = getExternalFilesDir(null);
        if (externalDir == null) return null;

        String dirPath = externalDir.getAbsolutePath();
        return dirPath + File.separator +
                mCurrentVocabItem.getListId() + File.separator +
                filename;
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
        setAnswerVisibility();
        mAnswerButton.setVisibility(View.GONE);
        mAnswerButtonLayout.setVisibility(View.VISIBLE);
    }

    private void setAnswerVisibility() {
        mMongolView.setVisibility(View.VISIBLE);
        mDefinitionView.setVisibility(View.VISIBLE);
        mPronunciationView.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(mCurrentVocabItem.getAudioFilename())) {
            mPlayButton.setVisibility(View.VISIBLE);
        }
    }

    public void onResponseButtonClick(View view) {
        if (mLocked) return;

        int response = getResponseValue(view);

        if (mCurrentVocabItem.isFirstViewToday()) {
            calculateSuperMemo2Algorithm(response);
            new UpdateVocabPracticeData().execute(mCurrentVocabItem);
            mCurrentVocabItem.setFirstViewToday(false);
            showNextDueDate();
        }

        // keep practicing wrong answers until they get them right
        if (response < MIN_QUALITY_FOR_CORRECT) {
            mTodaysQuestions.add(mCurrentVocabItem);
        }

        prepareNextQuestion();
    }

    private void showNextDueDate() {
        long nextDueDate = mCurrentVocabItem.getNextDueDate();
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(nextDueDate)).toString();
        Toast toast = Toast.makeText(this, dateString, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void calculateSuperMemo2Algorithm(int quality) {
        // algorithm based on:
        // https://www.supermemo.com/english/ol/sm2.htm
        // http://www.blueraja.com/blog/477/a-better-spaced-repetition-learning-algorithm-sm2
        // TODO improve algorithm
        // https://www.supermemo.com/help/smalg.htm

        float easiness = mCurrentVocabItem.getEasinessFactor();
        int consecutiveCorrect = mCurrentVocabItem.getConsecutiveCorrect();
        //int interval = mCurrentVocabItem.getInterval();

        easiness = (float) Math.max(1.3, easiness - 0.8 + 0.28 * quality - 0.02 * quality * quality);

        // consecutive correct
        boolean answerIsCorrect = quality >= MIN_QUALITY_FOR_CORRECT;
        if (answerIsCorrect)
            consecutiveCorrect++;
        else
            consecutiveCorrect = 0;

        long nextDueDate;
        long now = System.currentTimeMillis();
        if (answerIsCorrect)
            nextDueDate = (long) (now
                    + MILLISECONDS_IN_A_DAY * DEFAULT_INTERVAL
                    * Math.pow(easiness, consecutiveCorrect - 1));
        else
            nextDueDate = now + MILLISECONDS_IN_A_DAY;

        mCurrentVocabItem.setEasinessFactor(easiness);
        mCurrentVocabItem.setConsecutiveCorrect(consecutiveCorrect);
        mCurrentVocabItem.setNextDueDate(nextDueDate);

    }

    private int getResponseValue(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.response_button_0:
                return 0;
            case R.id.response_button_1:
                return 1;
            case R.id.response_button_2:
                return 2;
            case R.id.response_button_3:
                return 3;
            case R.id.response_button_4:
                return 4;
            case R.id.response_button_5:
                return 5;
            default:
                return 0;
        }
    }

    private void prepareNextQuestion() {
        mCurrentVocabItem = getNextQuestion();
        if (mCurrentVocabItem == null) {
            handleQuestionsFinished();
            return;
        }
        setQuestionVisibility(mCurrentVocabItem);
        setQuestionText(mCurrentVocabItem);
    }

    private Vocab getNextQuestion() {
        Vocab nextQuestion = mTodaysQuestions.poll();
        if (nextQuestion == null) return null;
        switch (mStudyMode) {
            case MONGOL:
                if (TextUtils.isEmpty(nextQuestion.getMongol()))
                    return getNextQuestion();
                break;
            case DEFINITION:
                if (TextUtils.isEmpty(nextQuestion.getDefinition()))
                    return getNextQuestion();
                break;
            case PRONUNCIATION:
                if (TextUtils.isEmpty(nextQuestion.getPronunciation())
                        && TextUtils.isEmpty(nextQuestion.getAudioFilename()))
                    return getNextQuestion();
                break;
        }
        return nextQuestion;
    }

    private void handleQuestionsFinished() {
        makeEverythingInvisible();
        mNumberOfWordsView.setText(String.valueOf(0));
        invalidateOptionsMenu();
    }

    private void makeEverythingInvisible() {
        mMongolView.setVisibility(View.INVISIBLE);
        mDefinitionView.setVisibility(View.INVISIBLE);
        mPronunciationView.setVisibility(View.INVISIBLE);
        mPlayButton.setVisibility(View.INVISIBLE);
        mButtonPanel.setVisibility(View.INVISIBLE);
    }

    private void setQuestionVisibility(Vocab item) {
        mAnswerButton.setVisibility(View.VISIBLE);
        mAnswerButtonLayout.setVisibility(View.GONE);
        mButtonPanel.setVisibility(View.VISIBLE);
        switch (mStudyMode) {
            case MONGOL:
                mMongolView.setVisibility(View.VISIBLE);
                mDefinitionView.setVisibility(View.INVISIBLE);
                mPronunciationView.setVisibility(View.INVISIBLE);
                mPlayButton.setVisibility(View.INVISIBLE);
                break;
            case DEFINITION:
                mMongolView.setVisibility(View.INVISIBLE);
                mDefinitionView.setVisibility(View.VISIBLE);
                mPronunciationView.setVisibility(View.INVISIBLE);
                mPlayButton.setVisibility(View.INVISIBLE);
                break;
            case PRONUNCIATION:
                mMongolView.setVisibility(View.INVISIBLE);
                mDefinitionView.setVisibility(View.INVISIBLE);
                mPronunciationView.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(item.getAudioFilename())) {
                    mPlayButton.setVisibility(View.INVISIBLE);
                } else {
                    mPlayButton.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void setQuestionText(Vocab item) {
        setListNameWithQuestionsLeft();
        mMongolView.setText(item.getMongol());
        mDefinitionView.setText(item.getDefinition());
        mPronunciationView.setText(item.getPronunciation());
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
            prepareNextQuestion();
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
            final long nextDueDate = item.getNextDueDate();
            final int consecutiveCorrect = item.getConsecutiveCorrect();
            final float eFactor = item.getEasinessFactor();

            try {

                DatabaseManager dbAdapter = new DatabaseManager(getApplicationContext());
                dbAdapter.updateVocabItemPracticeData(
                        mStudyMode, id, nextDueDate, consecutiveCorrect, eFactor);
                // TODO if audio file was updated then change
            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void results) {
            mLocked = false;
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

    // TODO this is not dry, repeated in AllWordsActivity
    private class ShowLists extends AsyncTask<Vocab, Void, List<VocabList>> {

        Vocab currentItem;

        @Override
        protected List<VocabList> doInBackground(Vocab... params) {

            currentItem = params[0];
            List<VocabList> results = null;

            try {
                DatabaseManager dbAdapter = new DatabaseManager(getApplicationContext());
                results = dbAdapter.getAllLists();
            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            return results;
        }

        @Override
        protected void onPostExecute(List<VocabList> results) {
            List<VocabList> otherLists = filterOutCurrentList(currentItem.getListId(), results);
            showListDialog(currentItem.getId(), otherLists);
        }
    }

    private List<VocabList> filterOutCurrentList(long currentListId, List<VocabList> lists) {
        List<VocabList> filtered = new ArrayList<>();
        for (VocabList list : lists) {
            if (list.getListId() != currentListId)
                filtered.add(list);
        }
        return filtered;
    }

    private void showListDialog(final long currentWordId, final List<VocabList> otherLists) {
        if (otherLists == null || otherLists.size() ==0) {
            Toast.makeText(this, "There are no other lists", Toast.LENGTH_SHORT).show();
            // TODO remove the MOVE item from the menu or allow a new list to be created here.
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Move to another list");

        String[] items = getListNames(otherLists);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                long newListId = otherLists.get(which).getListId();
                new MoveVocabItemToNewList().execute(currentWordId, newListId);
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String[] getListNames(List<VocabList> otherLists) {
        String[] names = new String[otherLists.size()];
        for (int i = 0; i < otherLists.size(); i++) {
            names[i] = otherLists.get(i).getName();
        }
        return names;
    }

    private class MoveVocabItemToNewList extends AsyncTask<Long, Void, Void> {

        @Override
        protected Void doInBackground(Long... params) {

            long vocabId = params[0];
            long listId = params[1];

            try {

                DatabaseManager dbAdapter = new DatabaseManager(getApplicationContext());
                dbAdapter.updateVocabItemList(vocabId, listId);
            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void results) {
            prepareNextQuestion();
        }

    }
}
