package app.iamin.iamin.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import app.iamin.iamin.model.Need;
import app.iamin.iamin.R;
import app.iamin.iamin.util.TimeUtils;
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

        public FrameLayout parent;
        public ImageView iconImageView;
        public TextView typeTextView;
        public TextView dateTextView;
        public TextView addressTextView;
        public TextView countTextView;

        ItemClickListener holderClickListener;

        public ViewHolder(FrameLayout v) {
            super(v);
            parent = v;
            parent.setClickable(true);
            parent.setOnClickListener(this);

            holderClickListener = clickListener;

            iconImageView = (ImageView) v.findViewById(R.id.item_icon);
            typeTextView = (TextView) v.findViewById(R.id.item_type);
            dateTextView = (TextView) v.findViewById(R.id.item_date);
            addressTextView = (TextView) v.findViewById(R.id.item_address);
            countTextView = (TextView) v.findViewById(R.id.item_count);
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
       FrameLayout v = (FrameLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ListAdapter.ViewHolder holder, int position) {
        Need need = needs[position];

        holder.parent.setBackgroundResource((position % 2) == 0 ?
                R.color.windowBackground : R.color.windowBackgroundLight);

        holder.iconImageView.setImageResource(need.getCategoryIcon());
        holder.addressTextView.setText(need.getAddress().getAddressLine(0));
        holder.typeTextView.setText(need.getCount() == 1 ?
                need.getCategorySingular() : need.getCategoryPlural());

        String dayStr = TimeUtils.formatHumanFriendlyShortDate(mContext, need.getStart());
        String dString = dayStr + " " + TimeUtils.formatTimeOfDay(need.getStart()) + " - " +
                TimeUtils.formatTimeOfDay(need.getEnd()) + " Uhr";

        holder.dateTextView.setText(dString);
        holder.countTextView.setText(need.getCount() + "");
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
