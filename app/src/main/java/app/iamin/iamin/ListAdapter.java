package app.iamin.iamin;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Paul on 10-10-2015.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private List<HelpRequest> mHelpRequests;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView cardView;
        public ImageView iconView;
        public TextView titleView;
        public TextView dateView;
        public TextView locationView;
        public ViewHolder(CardView v) {
            super(v);
            cardView = v;
            iconView = (ImageView) v.findViewById(R.id.item_icon);
            titleView = (TextView) v.findViewById(R.id.item_title);
            dateView = (TextView) v.findViewById(R.id.item_date);
            locationView = (TextView) v.findViewById(R.id.item_location);
        }
    }

    public ListAdapter(List<HelpRequest> helpRequests) {
        mHelpRequests = helpRequests;
    }

    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
       CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem, parent, false);
        // set the view's size, margins, paddings and layout parameters
        // ...
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ListAdapter.ViewHolder holder, int position) {
        holder.iconView.setImageResource(R.mipmap.medical);
        holder.titleView.setText(mHelpRequests.get(position).getName());
        holder.dateView.setText(mHelpRequests.get(position).getStart().toString());
        holder.locationView.setText(mHelpRequests.get(position).getLocation().toString());
    }

    @Override
    public int getItemCount() {
        return mHelpRequests.size();
    }
}
