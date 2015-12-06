package app.iamin.iamin.ui;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.iamin.iamin.R;
import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.ui.widget.NeedViewNew;
import app.iamin.iamin.util.UiUtils;
import io.realm.RealmResults;

/**
 * Created by Paul on 10-10-2015.
 */
public class NeedFeedAdapter extends RecyclerView.Adapter<NeedFeedAdapter.ViewHolder> {

    private Activity mContext;
    private RealmResults<Need> mNeeds;

    public NeedFeedAdapter(Activity context) {
        mContext = context;
        setHasStableIds(true);
    }

    @Override
    public NeedFeedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        NeedViewNew view = (NeedViewNew) inflater.inflate(R.layout.need_item_new, parent, false);
        return new ViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(NeedFeedAdapter.ViewHolder holder, int position) {
        Need need = mNeeds.get(position);
        holder.mNeedView.setNeed(need);
    }

    @Override
    public int getItemCount() {
        return mNeeds == null ? 0 : mNeeds.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(RealmResults<Need> needs) {
        mNeeds = needs;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        NeedViewNew mNeedView;
        ItemClickListener mClickListener;

        public ViewHolder(NeedViewNew needView, ItemClickListener clickListener) {
            super(needView);
            mNeedView = needView;
            mNeedView.setClickable(true);
            mNeedView.setOnClickListener(this);
            mClickListener = clickListener;
        }

        @Override
        public void onClick(View v) {
            mClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public Need getItem(int position) {
        return mNeeds.get(position);
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    private final ItemClickListener clickListener = new ItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            UiUtils.fireDetailIntent(mContext, getItem(position));
        }
    };
}