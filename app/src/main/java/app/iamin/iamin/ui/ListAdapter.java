package app.iamin.iamin.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.iamin.iamin.model.Need;
import app.iamin.iamin.R;
import app.iamin.iamin.util.UiUtils;

/**
 * Created by Paul on 10-10-2015.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private Context mContext;
    private Need[] needs;

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    private final ItemClickListener clickListener = new ItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            Intent intent = UiUtils.getDetailIntent(mContext, needs[position]);
            mContext.startActivity(intent);
        }
    };

    public final class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public NeedView needView;
        ItemClickListener holderClickListener;

        public ViewHolder(NeedView view) {
            super(view);
            needView = view;
            needView.setClickable(true);
            needView.setOnClickListener(this);
            holderClickListener = clickListener;
        }

        @Override
        public void onClick(View v) {
            holderClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public ListAdapter(Context context) {
        mContext = context;
        setHasStableIds(true);
    }

    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       NeedView view = (NeedView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_new, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListAdapter.ViewHolder holder, int position) {
        Need need = needs[position];
        holder.needView.setNeed(need);
        //holder.needView.setBackgroundResource((position % 2) == 0 ? R.color.windowBackground : R.color.windowBackgroundLight);
    }

    @Override
    public int getItemCount() {
        return needs == null ? 0 : needs.length;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(Need[] needs) {
        this.needs = needs;
        this.notifyDataSetChanged();
    }
}
