package net.studymongolian.suryaa;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

class ListsRvAdapter extends RecyclerView.Adapter<ListsRvAdapter.ViewHolder> {

    private List<VocabList> mLists = Collections.emptyList();
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    ListsRvAdapter(Context context, List<VocabList> lists) {
        this.mInflater = LayoutInflater.from(context);
        this.mLists = lists;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_item_lists, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String name = mLists.get(position).getName();
        holder.tvListName.setText(name);
    }

    @Override
    public int getItemCount() {
        return mLists.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        TextView tvListName;

        ViewHolder(View itemView) {
            super(itemView);
            tvListName = itemView.findViewById(R.id.tv_list_name);
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

    VocabList getItem(int id) {
        return mLists.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    interface ItemClickListener {
        void onItemClick(View view, int position);
        boolean onItemLongClick(View view, int position);
    }
}