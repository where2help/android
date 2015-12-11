package app.iamin.iamin.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import app.iamin.iamin.R;

import static android.R.layout.simple_dropdown_item_1line;

/**
 * Created by Markus on 04.11.15.
 */
public class FilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CITY = 0;
    private static final int TYPE_CATEGORY = 1;

    private Context mContext;
    private String[] mFilterList;
    private String[] mCityList;
    private int mFilterState = 0;
    private String mFilterCity = null;
    private FilterChangedListener mFilterChangedListener;

    public FilterAdapter(Context context, int filterState, FilterChangedListener listener) {
        mContext = context;
        mFilterChangedListener = listener;
        mFilterState = filterState;
        mFilterList = context.getResources().getStringArray(R.array.filters);
        setHasStableIds(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (viewType == TYPE_CITY) {
            return new CityViewHolder(inflater.inflate(R.layout.filter_city_item, parent, false));
        } else {
            return new CategoryViewHolder(inflater.inflate(R.layout.filter_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof CityViewHolder) {
            CityViewHolder holder = (CityViewHolder) viewHolder;
            holder.filterItem.setText(mFilterCity);
        } else {
            int categoryPosition = position - 1;
            CategoryViewHolder holder = (CategoryViewHolder) viewHolder;
            String filter = mFilterList[categoryPosition];
            holder.filterItem.setText(filter);
            holder.filterItem.setTextColor(mFilterState == categoryPosition ?
                    ContextCompat.getColor(mContext, R.color.text_light_sec) :
                    ContextCompat.getColor(mContext, R.color.text_light_hint));
        }
    }

    @Override
    public int getItemCount() {
        return mFilterList == null ? 0 : mFilterList.length + 1; // +1 for city item
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_CITY : TYPE_CATEGORY;
    }

    public void setCityList(String[] mCityList) {
        this.mCityList = mCityList;
        notifyItemChanged(0);
    }

    public final class CategoryViewHolder extends RecyclerView.ViewHolder {

        TextView filterItem;

        public CategoryViewHolder(View view) {
            super(view);
            filterItem = (TextView) view;
            filterItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mFilterState = getAdapterPosition() - 1;
                    mFilterChangedListener.onFilterCategoryChanged(view, mFilterState);
                }
            });
        }
    }

    public final class CityViewHolder extends RecyclerView.ViewHolder {

        ArrayAdapter<String> adapter;
        AutoCompleteTextView filterItem;

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public CityViewHolder(View view) {
            super(view);

            adapter = new ArrayAdapter<>(mContext, simple_dropdown_item_1line, mCityList);

            filterItem = (AutoCompleteTextView) view;

            Drawable icon = filterItem.getCompoundDrawables()[2];
            if (icon != null) icon.setAlpha(90);

            filterItem.setAdapter(adapter);
            filterItem.setImeOptions(EditorInfo.IME_ACTION_DONE);
            filterItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mFilterCity = adapter.getItem(position);
                    mFilterChangedListener.onFilterCityChanged(view, mFilterCity);
                }
            });
            filterItem.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        InputMethodManager inm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inm.hideSoftInputFromWindow(filterItem.getWindowToken(), 0);
                    }
                }
            });
            filterItem.setOnEditorActionListener(new TextView.OnEditorActionListener() {

                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        filterItem.setText(null);
                        mFilterCity = null;
                        mFilterChangedListener.onFilterCityChanged(view, mFilterCity);
                    }
                    return false;
                }
            });
        }
    }

    public interface FilterChangedListener {
        void onFilterCategoryChanged(View view, int category);

        void onFilterCityChanged(View view, String city);
    }
}