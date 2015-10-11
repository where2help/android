package app.iamin.iamin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
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
            intent.putExtra("selfLink", req.getSelfLink());
            intent.putExtra("stillOpen", req.getStillOpen());
            intent.putExtra("longitude", req.getAddress().getLongitude());
            intent.putExtra("latitude", req.getAddress().getLatitude());

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
        public FrameLayout cardView;
        public ImageView iconView;
        public TextView titleView;
        public TextView dateView;
        public TextView locationView;
        public TextView peopleNeededView;

        ItemClickListener holderClickListener;

        public ViewHolder(FrameLayout v) {
            super(v);
            cardView = v;
            cardView.setClickable(true);
            cardView.setOnClickListener(this);

            holderClickListener = clickListener;

            iconView = (ImageView) v.findViewById(R.id.item_icon);
            titleView = (TextView) v.findViewById(R.id.item_title);
            dateView = (TextView) v.findViewById(R.id.item_date);
            locationView = (TextView) v.findViewById(R.id.item_location);
            peopleNeededView = (TextView) v.findViewById(R.id.item_count);
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

        try {
            new PullNeedsActiveTask(context, new URL("http://where2help.herokuapp.com/api/v1/needs.json"), this).execute();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // TODO
        }
    }

    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       FrameLayout v = (FrameLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ListAdapter.ViewHolder holder, int position) {

        if (position % 2 == 0) {
            holder.cardView.setBackgroundColor(firstColor);
        } else {
            holder.cardView.setBackgroundColor(secondColor);
        }
        HelpRequest req = mHelpRequests[position];

        holder.iconView.setImageResource(R.mipmap.ic_medical);
        holder.locationView.setText(req.getAddress().getAddressLine(0));
        holder.titleView.setText(req.getType());
        DateFormat dtfStart = new SimpleDateFormat("H:m");
        DateFormat dtfEnd = new SimpleDateFormat("H:m");
        Date today = new Date();
        today.setHours(23);
        today.setMinutes(59);
        String dayStr = "Morgen";
        if (req.getStart().compareTo(today) < 0) {
            dayStr = "Heute";
        }
        String dString = dayStr + " " + dtfStart.format(req.getStart()) + " - " + dtfEnd.format(req.getEnd()) + " Uhr";
        holder.dateView.setText(dString);
        int numHours = (int) Math.floor((req.getEnd().getTime() - req.getStart().getTime()) / (1000 * 60 * 60));
        holder.peopleNeededView.setText(req.getStillOpen() + "");
    }

    @Override
    public int getItemCount() {
        return mHelpRequests == null ? 0 : mHelpRequests.length;
    }

    public void setData(HelpRequest[] needs) {
        mHelpRequests = needs;
        this.notifyDataSetChanged();
    }
}
