package app.iamin.iamin;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Paul on 10-10-2015.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private Context mContext;
    private HelpRequest[] mHelpRequests;

    private int firstColor;
    private int secondColor;

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    private final ItemClickListener clickListener = new ItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            HelpRequest req = mHelpRequests[position];

            Intent intent = new Intent();
            intent.setClass(mContext, DetailActivity.class);
            intent.putExtra("address", req.getAddress().getAddressLine(0));
            intent.putExtra("type", req.getType());
            intent.putExtra("typeSingular", req.getTypeSingular());
            intent.putExtra("typeIcon", req.getTypeIcon());
            intent.putExtra("selfLink", req.getSelfLink());
            intent.putExtra("stillOpen", req.getStillOpen());
            intent.putExtra("longitude", req.getAddress().getLongitude());
            intent.putExtra("latitude", req.getAddress().getLatitude());
            intent.putExtra("id", req.getId());

            DateFormat dtfStart = new SimpleDateFormat("d. M H:m");
            DateFormat dtfEnd = new SimpleDateFormat("H:m");
            String date = dtfStart.format(req.getStart()) + " - " + dtfEnd.format(req.getEnd()) + " Uhr";
            intent.putExtra("date", date);
            intent.putExtra("dateStart", req.getStart().getTime());
            intent.putExtra("dateStartForm", dtfEnd.format(req.getStart()));
            intent.putExtra("dateEnd", req.getEnd().getTime());

            mContext.startActivity(intent);
        }
    };

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public final class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
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

    public ListAdapter(Context context, HelpRequest[] helpRequests) {
        mContext = context;
        firstColor = context.getResources().getColor(R.color.windowBackground);
        secondColor = context.getResources().getColor(R.color.windowBackgroundLight);
        mHelpRequests = helpRequests;
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
        HelpRequest req = mHelpRequests[position];

        holder.parent.setBackgroundColor((position % 2) == 0 ? firstColor : secondColor);
        holder.iconImageView.setImageResource(req.getTypeIcon());
        holder.addressTextView.setText(req.getAddress().getAddressLine(0));
        holder.typeTextView.setText(req.getStillOpen() == 1 ? req.getTypeSingular() : req.getType());

        DateFormat dtfStart = new SimpleDateFormat("H:m");
        DateFormat dtfEnd = new SimpleDateFormat("H:m");
        Date today = new Date();
        today.setHours(23);
        today.setMinutes(59);
        String dayStr = (req.getStart().compareTo(today) < 0) ? "Heute" : "Morgen";
        String dString = dayStr + " " + dtfStart.format(req.getStart()) + " - " + dtfEnd.format(req.getEnd()) + " Uhr";
        holder.dateTextView.setText(dString);
        // int numHours = (int) Math.floor((req.getEnd().getTime() - req.getStart().getTime()) / (1000 * 60 * 60));
        holder.countTextView.setText(req.getStillOpen() + "");
    }

    @Override
    public int getItemCount() {
        return mHelpRequests == null ? 0 : mHelpRequests.length;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(HelpRequest[] needs) {
        mHelpRequests = needs;
        this.notifyDataSetChanged();
    }
}
