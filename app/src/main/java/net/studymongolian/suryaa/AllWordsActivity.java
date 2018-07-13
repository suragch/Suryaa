package net.studymongolian.suryaa;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import net.studymongolian.suryaa.database.DatabaseManager;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class AllWordsActivity extends AppCompatActivity implements AllWordsRvAdapter.ItemClickListener {


    private static final String TAG = "AllWordsActivity";
    private static final int EDIT_WORD_ACTIVITY_REQUEST_CODE = 1;
    public static final String CHANGES_MADE_KEY = "changesMade";
    private List<Vocab> mWordList;
    private AllWordsRvAdapter adapter;
    private MediaPlayer mPlayer = null;
    ActionMode mActionMode;
    private int mLongClickedItemIndex = -1;
    private boolean changesWereMade = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_words);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        long listId = getIntent().getLongExtra(MainActivity.LIST_ID_KEY, 0);
        new GetAllVocabInList().execute(listId);
    }

    @Override
    public void onItemClick(View view, int position) {
        Vocab item = adapter.getItem(position);
        playAudio(item);
    }

    @Override
    public boolean onItemLongClick(View view, int position) {
        // show context menu
        if (mActionMode != null) return false;
        mLongClickedItemIndex = position;
        mActionMode = startSupportActionMode(mActionModeCallback);
        if (mActionMode == null) return false;
        mActionMode.setTag(position);
        view.setSelected(true);
        return true;
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.words_context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            int position = Integer.parseInt(mode.getTag().toString());
            Vocab vocabItem = adapter.getItem(position);

            switch (item.getItemId()) {
                case R.id.menu_edit:
                    editWord(vocabItem);
                    mode.finish();
                    return true;
                case R.id.menu_move:
                    moveWord(vocabItem);
                    mode.finish();
                    return true;
                case R.id.menu_delete:
                    deleteWord(vocabItem);
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };

    private void editWord(Vocab vocabItem) {
        Intent intent = new Intent(this, AddEditWordActivity.class);
        intent.putExtra(MainActivity.LIST_ID_KEY, vocabItem.getListId());
        intent.putExtra(AddEditWordActivity.WORD_ID_KEY, vocabItem.getId());
        startActivityForResult(intent, EDIT_WORD_ACTIVITY_REQUEST_CODE);
    }

    private void moveWord(Vocab vocabItem) {
        new ShowLists().execute(vocabItem);
    }

    private void deleteWord(final Vocab vocabItem) {

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this word?");

        // add the buttons
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DeleteVocab().execute(vocabItem);
            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {

            case EDIT_WORD_ACTIVITY_REQUEST_CODE:

                boolean wordEdited = data.getBooleanExtra(AddEditWordActivity.EDIT_MODE_KEY, false);
                if (wordEdited) {
                    Vocab word = mWordList.get(mLongClickedItemIndex);
                    if (word == null) break;
                    new RefreshVocabItem().execute(word.getId());
                    changesWereMade = true;
                }
                break;
        }
    }

    private void playAudio(Vocab item) {
        String pathName = getPathForCurrentAudioFile(item);
        if (pathName == null) return;
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
            Log.e(TAG, "prepare() failed");
        }
    }

    private String getPathForCurrentAudioFile(Vocab item) {
        String filename = item.getAudioFilename();
        if (TextUtils.isEmpty(filename)) return null;

        File externalDir = getExternalFilesDir(null);
        if (externalDir == null) return null;

        String dirPath = externalDir.getAbsolutePath();
        return dirPath + File.separator +
                item.getListId() + File.separator +
                filename;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                intent.putExtra(CHANGES_MADE_KEY, changesWereMade);
                setResult(RESULT_OK, intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class GetAllVocabInList extends AsyncTask<Long, Void, List<Vocab>> {

        @Override
        protected List<Vocab> doInBackground(Long... params) {

            long listId = params[0];

            List<Vocab> results = new LinkedList<>();

            try {
                DatabaseManager dbAdapter = new DatabaseManager(getApplicationContext());
                results = dbAdapter.getAllVocabInList(listId);
            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            return results;

        }

        @Override
        protected void onPostExecute(List<Vocab> results) {
            RecyclerView recyclerView = findViewById(R.id.rv_all_words);
            LinearLayoutManager horizontalLayoutManagaer
                    = new LinearLayoutManager(AllWordsActivity.this, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(horizontalLayoutManagaer);
            adapter = new AllWordsRvAdapter(getApplicationContext(), results);
            adapter.setClickListener(AllWordsActivity.this);
            recyclerView.setAdapter(adapter);
            mWordList = results;
        }

    }

    private class RefreshVocabItem extends AsyncTask<Long, Void, Vocab> {

        @Override
        protected Vocab doInBackground(Long... params) {

            long vocabId = params[0];

            Vocab vocabItem = null;

            try {

                DatabaseManager dbAdapter = new DatabaseManager(getApplicationContext());
                vocabItem = dbAdapter.getVocabItem(vocabId, StudyMode.MONGOL);
            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            return vocabItem;
        }

        @Override
        protected void onPostExecute(Vocab vocabItem) {
            mWordList.set(mLongClickedItemIndex, vocabItem);
            adapter.notifyItemChanged(mLongClickedItemIndex);
            mLongClickedItemIndex = -1;
        }

    }

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
            showListDialog(currentItem, otherLists);
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

    private void showListDialog(final Vocab currentItem, final List<VocabList> otherLists) {
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
                new MoveVocabItemToNewList(AllWordsActivity.this, currentItem, newListId).execute();
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

    private static class MoveVocabItemToNewList extends AsyncTask<Void, Void, Void> {

        private WeakReference<AllWordsActivity> activityReference;
        String audioFileName;
        long vocabId;
        long oldListId;
        long newListId;

        MoveVocabItemToNewList(AllWordsActivity context,
                               Vocab currentItem,
                               long newListId) {
            activityReference = new WeakReference<>(context);
            this.audioFileName = currentItem.getAudioFilename();
            this.vocabId = currentItem.getId();
            this.oldListId = currentItem.getListId();
            this.newListId = newListId;
        }

        @Override
        protected Void doInBackground(Void... params) {

            AllWordsActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return null;

            try {

                DatabaseManager dbAdapter = new DatabaseManager(activity);
                dbAdapter.updateVocabItemList(vocabId, newListId);
                FileUtils.moveAudioFile(activity, audioFileName, oldListId, newListId);
            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void results) {
            AllWordsActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            activity.removeItemFromAdapter();
        }

    }

    private void removeItemFromAdapter() {
        mWordList.remove(mLongClickedItemIndex);
        adapter.notifyItemRemoved(mLongClickedItemIndex);
        mLongClickedItemIndex = -1;
        changesWereMade = true;
    }


    private class DeleteVocab extends AsyncTask<Vocab, Void, Vocab> {

        @Override
        protected Vocab doInBackground(Vocab... params) {

            Vocab item = params[0];

            try {
                DatabaseManager dbAdapter = new DatabaseManager(getApplicationContext());
                dbAdapter.deleteVocabItem(item.getId());
                deleteAudioFile(item);

            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            return item;
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
        protected void onPostExecute(Vocab item) {
            removeItemFromAdapter();
        }

    }
}
