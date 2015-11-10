package app.iamin.iamin.ui;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.iamin.iamin.R;

/**
 * Created by Markus on 04.11.15.
 */
public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {

    private Context mContext;
    private String[] mFilterList;
    private int mFilterState = 0;
    private FilterChangedListener mFilterChangedListener;

    public FilterAdapter(Context context, int filterState, FilterChangedListener listener) {
        mContext = context;
        mFilterChangedListener = listener;
        mFilterState = filterState;
        mFilterList = context.getResources().getStringArray(R.array.filters);
        setHasStableIds(true);
    }

    @Override
    public FilterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        return new ViewHolder(inflater.inflate(R.layout.filter_item, parent, false));
    }

    @Override
    public void onBindViewHolder(FilterAdapter.ViewHolder holder, int position) {
        String filter = mFilterList[position];
        holder.filterItem.setText(filter);
        holder.filterItem.setTextColor(mFilterState == position ?
                ContextCompat.getColor(mContext, R.color.text_light_sec) :
                ContextCompat.getColor(mContext, R.color.text_light_hint));
    }

    @Override
    public int getItemCount() {
        return mFilterList == null ? 0 : mFilterList.length;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public final class ViewHolder extends RecyclerView.ViewHolder {

        TextView filterItem;

        public ViewHolder(View view) {
            super(view);
            filterItem = (TextView) view;
            filterItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mFilterState = getAdapterPosition();
                    mFilterChangedListener.onFilterChanged(view, getAdapterPosition());
                }
            });
        }
    }

    public interface FilterChangedListener {
        void onFilterChanged(View view, int position);
    }
}