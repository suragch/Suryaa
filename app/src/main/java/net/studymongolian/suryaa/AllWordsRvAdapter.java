package net.studymongolian.suryaa;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.studymongolian.mongollibrary.MongolTextView;

import java.util.Collections;
import java.util.List;

public class AllWordsRvAdapter extends RecyclerView.Adapter<AllWordsRvAdapter.ViewHolder> {

    private List<Vocab> mVocabList = Collections.emptyList();
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    AllWordsRvAdapter(Context context, List<Vocab> words) {
        this.mInflater = LayoutInflater.from(context);
        this.mVocabList = words;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_item_all_words, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // binds the data to the view and textview in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String mongolWord = mVocabList.get(position).getMongol();
        String definition = mVocabList.get(position).getDefinition();
        String pronunciation = mVocabList.get(position).getPronunciation();
        holder.mtvMongol.setText(mongolWord);
        holder.mtvDefinition.setText(definition);
        holder.mtvPronunciation.setText(pronunciation);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mVocabList.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        MongolTextView mtvMongol;
        MongolTextView mtvDefinition;
        MongolTextView mtvPronunciation;

        ViewHolder(View itemView) {
            super(itemView);
            mtvMongol = itemView.findViewById(R.id.mtv_all_words_mongol);
            mtvDefinition = itemView.findViewById(R.id.mtv_all_words_definition);
            mtvPronunciation = itemView.findViewById(R.id.mtv_all_words_pronunciation);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if (mClickListener != null) {
                return mClickListener.onItemLongClick(view, getAdapterPosition());
            }
            return false;
        }
    }

    // convenience method for getting data at click position
    Vocab getItem(int id) {
        return mVocabList.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
        boolean onItemLongClick(View view, int position);
    }
}