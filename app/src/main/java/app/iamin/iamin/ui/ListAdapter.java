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
    private Need[] mHelpRequests;

    private int firstColor;
    private int secondColor;

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    private final ItemClickListener clickListener = new ItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            Intent intent = UiUtils.getDetailIntent(mContext, mHelpRequests[position]);
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
        firstColor = context.getResources().getColor(R.color.windowBackground);
        secondColor = context.getResources().getColor(R.color.windowBackgroundLight);
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
        Need req = mHelpRequests[position];

        holder.parent.setBackgroundColor((position % 2) == 0 ? firstColor : secondColor);
        holder.iconImageView.setImageResource(req.getCategoryIcon());
        holder.addressTextView.setText(req.getAddress().getAddressLine(0));
        holder.typeTextView.setText(req.getCount() == 1 ? req.getCategorySingular() : req.getCategoryPlural());

        String dayStr = TimeUtils.formatHumanFriendlyShortDate(mContext, req.getStart());
        String dString = dayStr + " " + TimeUtils.formatTimeOfDay(req.getStart()) + " - " +
                TimeUtils.formatTimeOfDay(req.getEnd()) + " Uhr";

        holder.dateTextView.setText(dString);
        holder.countTextView.setText(req.getCount() + "");
    }

    @Override
    public int getItemCount() {
        return mHelpRequests == null ? 0 : mHelpRequests.length;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(Need[] needs) {
        mHelpRequests = needs;
        this.notifyDataSetChanged();
    }
}
