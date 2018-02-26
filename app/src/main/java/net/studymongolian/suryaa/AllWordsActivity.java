package net.studymongolian.suryaa;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import net.studymongolian.suryaa.database.DatabaseManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


public class AllWordsActivity extends AppCompatActivity implements AllWordsRvAdapter.ItemClickListener {


    private List<Vocab> mWordList;
    private AllWordsRvAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_words);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        long listId = getIntent().getLongExtra(MainActivity.LIST_ID_KEY, 0);
        new GetAllVocabInList().execute(listId);
    }

    @Override
    public void onItemClick(View view, int position) {

        Date date = new Date(mWordList.get(position).getDate());
        java.text.DateFormat df = SimpleDateFormat.getDateTimeInstance(
                SimpleDateFormat.SHORT, SimpleDateFormat.SHORT, Locale.getDefault());
        String formattedDate = df.format(date);

        Toast.makeText(this, "Due date: " + formattedDate, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
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


}
