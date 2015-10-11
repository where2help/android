package app.iamin.iamin;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Paul on 10-10-2015.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private Context mContext;
    private HelpRequest[] mHelpRequests;

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    private final ItemClickListener clickListener = new ItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            HelpRequest req = mHelpRequests[position];

            Intent intent = new Intent();
            intent.setClass(mContext, DetailActivity.class);
            intent.putExtra("address", req.getAddress().getFeatureName());
            intent.putExtra("name", req.getName());

            mContext.startActivity(intent);
        }
    };

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public final class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public CardView cardView;
        public ImageView iconView;
        public TextView titleView;
        public TextView dateView;
        public TextView locationView;
        public CountView peopleNeededView;

        ItemClickListener holderClickListener;

        public ViewHolder(CardView v) {
            super(v);
            cardView = v;
            cardView.setClickable(true);
            cardView.setOnClickListener(this);

            holderClickListener = clickListener;

            iconView = (ImageView) v.findViewById(R.id.item_icon);
            titleView = (TextView) v.findViewById(R.id.item_title);
            dateView = (TextView) v.findViewById(R.id.item_date);
            locationView = (TextView) v.findViewById(R.id.item_location);
            peopleNeededView = (CountView) v.findViewById(R.id.item_people_needed);
        }

        @Override
        public void onClick(View v) {
            holderClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public ListAdapter(Context context, HelpRequest[] helpRequests) {
        mContext = context;
        mHelpRequests = helpRequests;
        setHasStableIds(true);

        try {
            new PullNeedsActiveTask(context, new URL("http://www.mocky.io/v2/561a142d100000881568d551"), this).execute();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // TODO
        }
    }

    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ListAdapter.ViewHolder holder, int position) {
        HelpRequest req = mHelpRequests[position];

        holder.iconView.setImageResource(R.mipmap.ic_medical);
        holder.locationView.setText(req.getAddress().getFeatureName());
        holder.titleView.setText(req.getName());
        DateFormat dtfStart = new SimpleDateFormat("d. M H:m");
        DateFormat dtfEnd = new SimpleDateFormat("H:m");
        String dString = dtfStart.format(req.getStart()) + " - " + dtfEnd.format(req.getEnd()) + " Uhr";
        holder.dateView.setText(dString);
        int numHours = (int) Math.floor((req.getEnd().getTime() - req.getStart().getTime()) / (1000 * 60 * 60));
        holder.peopleNeededView.setCount(req.getStillOpen());
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
