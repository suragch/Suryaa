package net.studymongolian.suryaa;


import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import net.studymongolian.suryaa.database.DatabaseManager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ListsActivity extends AppCompatActivity implements ListsRvAdapter.ItemClickListener {

    private static final int ACTIVITY_CHOOSE_FILE = 3;
    static final String LIST_ID_KEY = "list_id";
    ActionMode mActionMode;
    public static final int REQUEST_WRITE_STORAGE = 112;
    private final String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private long mCurrentList;
    private boolean mCurrentListWasDeleted = false;

    private ListsRvAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mCurrentList = getIntent().getLongExtra(LIST_ID_KEY, -1);

        new GetAllLists(this).execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lists, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.miNewList:
                createNewList();
                return true;
            case R.id.miImport:
                importFile();
                return true;
            case android.R.id.home:
                onUserFinishing();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        onUserFinishing();
    }

    private void onUserFinishing() {

        if (mCurrentListWasDeleted && adapter.getItemCount() > 0) {
            chooseNewList();
            return;
        }

        Intent intent = new Intent();

        if (mCurrentListWasDeleted && adapter.getItemCount() == 0) {
            intent.putExtra(LIST_ID_KEY, -1);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    private void chooseNewList() {
        long listId = adapter.getItem(0).getListId();
        new SelectList(this).execute(listId);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(this, "The app was allowed to write to your storage!", Toast.LENGTH_LONG).show();
                    // Reload the activity with permission granted or use the features that required the permission
                } else {
                    Toast.makeText(this,
                            "Cannot export because the app was not allowed to write to your storage.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        long listId = adapter.getItem(position).getListId();
        new SelectList(this).execute(listId);
    }

    @Override
    public boolean onItemLongClick(View view, int position) {
        // show context menu
        if (mActionMode != null) return false;
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
            inflater.inflate(R.menu.lists_context_menu, menu);
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
            VocabList vocabList = adapter.getItem(position);

            switch (item.getItemId()) {
                case R.id.menu_rename:
                    renameList(vocabList);
                    mode.finish();
                    return true;
                case R.id.menu_export:
                    exportList(vocabList);
                    mode.finish();
                    return true;
                case R.id.menu_delete:
                    deleteList(vocabList);
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

    private void importFile() {


        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("files/*");
        intent = Intent.createChooser(chooseFile, "Choose a CSV text file");
        startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);

    }

    private void exportList(VocabList list) {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_WRITE_STORAGE);
        new ExportList(getApplication()).execute(list);
    }

    private void createNewList() {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("New list name");
        final View view = getLayoutInflater().inflate(R.layout.new_list_name_alert, null);
        alert.setView(view);
        alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                EditText edittext = view.findViewById(R.id.etNewListName);
                String listName = edittext.getText().toString();
                new CreateNewList(ListsActivity.this).execute(listName);
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }

    private void renameList(final VocabList list) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Rename list from " + list.getName() + " to...");
        final View view = getLayoutInflater().inflate(R.layout.new_list_name_alert, null);
        alert.setView(view);
        alert.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                EditText edittext = view.findViewById(R.id.etNewListName);
                String listName = edittext.getText().toString();
                if (TextUtils.isEmpty(listName)) return;
                list.setName(listName);
                new RenameList(ListsActivity.this).execute(list);
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }

    private void deleteList(final VocabList list) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Delete list");
        alert.setMessage("Are you sure that you want to delete \"" + list.getName() + "\"?");
        alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                new DeleteList(ListsActivity.this).execute(list);
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        if (requestCode == ACTIVITY_CHOOSE_FILE) {
            Uri uri = data.getData();
            String filePath = getPathFromURI(uri);
            new ImportList(this).execute(filePath);
        }
    }

    public String getPathFromURI(Uri uri) {
        String path = uri.getPath();
        if (path != null)
            return path;

        String[] projection = {MediaStore.Files.FileColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        cursor.moveToFirst();
        int column_index = cursor.getColumnIndexOrThrow(projection[0]);
        path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    private static class GetAllLists extends AsyncTask<Void, Void, List<VocabList>> {

        private WeakReference<ListsActivity> activityReference;

        GetAllLists(ListsActivity activity) {
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected List<VocabList> doInBackground(Void... params) {

            List<VocabList> results = new ArrayList<>();

            try {
                DatabaseManager dbAdapter = new DatabaseManager(activityReference.get());
                results = dbAdapter.getAllLists();
            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            return results;

        }

        @Override
        protected void onPostExecute(List<VocabList> results) {
            ListsActivity activity = activityReference.get();
            if (activity == null) return;

            RecyclerView recyclerView = activity.findViewById(R.id.rv_list_names);
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            activity.adapter = new ListsRvAdapter(activity, results);
            activity.adapter.setClickListener(activity);
            recyclerView.setAdapter(activity.adapter);
        }

    }

    private static class CreateNewList extends AsyncTask<String, Void, Long> {

        private WeakReference<ListsActivity> activityReference;

        CreateNewList(ListsActivity activity) {
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected Long doInBackground(String... params) {

            String listName = params[0];
            long result = -1;

            try {
                DatabaseManager dbAdapter = new DatabaseManager(activityReference.get());
                result = dbAdapter.createNewList(listName);
            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            return result;

        }

        @Override
        protected void onPostExecute(Long insertedRowNumber) {
            ListsActivity activity = activityReference.get();
            if (activity == null) return;
            new GetAllLists(activity).execute();
        }

    }

    private static class RenameList extends AsyncTask<VocabList, Void, Long> {

        private WeakReference<ListsActivity> activityReference;

        RenameList(ListsActivity activity) {
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected Long doInBackground(VocabList... params) {

            VocabList list = params[0];
            long listId = -1;

            try {
                DatabaseManager dbAdapter = new DatabaseManager(activityReference.get());
                listId = dbAdapter.updateList(list);
            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            return listId;

        }

        @Override
        protected void onPostExecute(Long insertedRowNumber) {
            ListsActivity activity = activityReference.get();
            if (activity == null) return;
            if (insertedRowNumber < 0)
                Toast.makeText(activity, "Couldn't rename list", Toast.LENGTH_SHORT).show();
            new GetAllLists(activity).execute();
        }

    }

    private static class DeleteList extends AsyncTask<VocabList, Void, Long> {

        private WeakReference<ListsActivity> activityReference;

        DeleteList(ListsActivity activity) {
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected Long doInBackground(VocabList... params) {

            VocabList list = params[0];
            long listId = list.getListId();

            try {
                DatabaseManager dbAdapter = new DatabaseManager(activityReference.get());

                // delete the audio files before deleting the list
                File listFolder = new File(activityReference.get().getExternalFilesDir(null),
                        String.valueOf(list.getListId()));
                FileUtils.deleteRecursive(listFolder);
                dbAdapter.deleteList(list.getListId());
            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            return listId;

        }

        @Override
        protected void onPostExecute(Long deletedListId) {
            ListsActivity activity = activityReference.get();
            if (activity == null) return;
            if (activity.mCurrentList == deletedListId) {
                activity.mCurrentListWasDeleted = true;
            }
            new GetAllLists(activity).execute();
        }

    }

    private static class SelectList extends AsyncTask<Long, Void, Long> {

        private WeakReference<ListsActivity> activityReference;

        SelectList(ListsActivity activity) {
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected Long doInBackground(Long... params) {

            long listId = params[0];
            ListsActivity activity = activityReference.get();
            if (activity == null) return listId;


            try {
                DatabaseManager dbAdapter = new DatabaseManager(activity);
                dbAdapter.updateListAccessDate(listId);
            } catch (Exception e) {
                Log.i("app", e.toString());
            }

            return listId;

        }

        @Override
        protected void onPostExecute(Long listId) {
            ListsActivity activity = activityReference.get();
            if (activity == null) return;

            // save this as the default in preferences
            PreferenceManager.getDefaultSharedPreferences(activity)
                    .edit()
                    .putLong(SettingsActivity.KEY_PREF_CURRENT_LIST, listId)
                    .apply();

            Intent intent = new Intent();
            intent.putExtra(LIST_ID_KEY, listId);
            activity.setResult(RESULT_OK, intent);
            activity.finish();
        }

    }

    private static class ExportList extends AsyncTask<VocabList, Void, Boolean> {

        private WeakReference<Application> appContextReference;

        ExportList(Application appContext) {
            appContextReference = new WeakReference<>(appContext);
        }

        @Override
        protected Boolean doInBackground(VocabList... params) {

            boolean result;
            Context appContext = appContextReference.get();
            if (appContext == null) return false;

            VocabList list = params[0];

            List<Vocab> allVocabInList;


            try {

                DatabaseManager dbAdapter = new DatabaseManager(appContext);
                allVocabInList = dbAdapter.getAllVocabInList(list.getListId());

                // export db and audio
                File externalDir = appContext.getExternalFilesDir(null);
                if (externalDir == null)
                    return false;
                String pathName  = externalDir.getAbsolutePath()
                        + "/" + list.getListId();
                File sourceFolder = new File(pathName);
                result = FileUtils.exportList(appContext, allVocabInList, list.getName(), sourceFolder);

            } catch (Exception e) {
                Log.i("testing", "doInBackground: FileUtils.exportList threw an exception");
                Log.i("app", e.toString());
                result = false;
            }

            return result;

        }

        @Override
        protected void onPostExecute(Boolean exportSuccessful) {

            Application app = appContextReference.get();
            if (app == null) return;

            if (exportSuccessful) {
                Toast.makeText(app, "List exported", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(app, "List couldn't be exported", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private static class ImportList extends AsyncTask<String, Void, Boolean> {

        private WeakReference<ListsActivity> activityReference;

        ImportList(ListsActivity activityContext) {
            activityReference = new WeakReference<>(activityContext);
        }

        @Override
        protected Boolean doInBackground(String... params) {


            boolean result;
            Context activityContext = activityReference.get();
            if (activityContext == null) return false;

            String csvFilePathName = params[0];
            String listname = new File(csvFilePathName).getName();
            if (listname.endsWith(FileUtils.EXPORT_IMPORT_FILE_EXTENSION)) {
                listname = listname.replace(FileUtils.EXPORT_IMPORT_FILE_EXTENSION, "");
                listname = listname.replace("_", " ");
            }

            List<Vocab> allVocabInList;

            long newListId = -1;

            try {
                DatabaseManager dbAdapter = new DatabaseManager(activityContext);
                newListId = dbAdapter.createNewList(listname);
                allVocabInList = FileUtils.importFile(csvFilePathName, newListId);
                // copy audio files

                copyAudioFilesRemovingNullReferences(activityContext, csvFilePathName, allVocabInList);
                dbAdapter.bulkInsert(allVocabInList);
                result = true;

            } catch (Exception e) {
                Log.e("app", e.toString());
                try {
                    DatabaseManager dbAdapter = new DatabaseManager(activityContext);
                    dbAdapter.deleteList(newListId);
                } catch (Exception ex) {
                    Log.e("app", ex.toString());
                }
                result = false;
            }

            return result;

        }

        private void copyAudioFilesRemovingNullReferences
                (Context context, String csvFilePathName, List<Vocab> allVocabInList)
                throws Exception {

            for (Vocab item : allVocabInList) {
                String fileName = item.getAudioFilename();
                if (TextUtils.isEmpty(fileName)) {
                    continue;
                }
                String parentDir = new File(csvFilePathName).getParent();
                File source = new File(parentDir, item.getAudioFilename());
                File externalDir = context.getExternalFilesDir(null);
                if (source.exists() && externalDir != null) {
                    String pathName  = externalDir.getAbsolutePath() + File.separator +
                            item.getListId();
                    File dest = new File(pathName, item.getAudioFilename());
                    FileUtils.copyFile(source, dest);
                } else {
                    item.setAudioFilename("");
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean importSuccessful) {

            ListsActivity activity = activityReference.get();
            if (activity == null) return;
            if (importSuccessful) {
                Toast.makeText(activity, "List imported", Toast.LENGTH_SHORT).show();
                new GetAllLists(activity).execute();
            } else {
                Toast.makeText(activity, "List couldn't be imported", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
