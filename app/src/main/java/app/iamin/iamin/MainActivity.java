package app.iamin.iamin;

import android.graphics.Rect;
import android.location.Address;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.maps.SupportMapFragment;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private HelpRequest[] needs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Freiwillige vor!");

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // TODO: remove
        //initializeData();

        // specify an adapter (see also next example)
        mAdapter = new ListAdapter(this, needs);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new MainActivity.SpacesItemDecoration(getResources().getDimensionPixelSize(R.dimen.grid_spacing)));

    }



    private class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space/2;
            outRect.top = space/2;
        }
    }

    // TODO: very ugly, please make sure to implement proper data handling
    public void updateNeeds(HelpRequest[] needs) {
        this.needs = needs;
        //
    }

    // TODO: remove me
    /*private void initializeData(){
        helpRequests = new ArrayList<HelpRequest>();
        for (int i = 0; i < 12; i++) {
            HelpRequest req1 = new HelpRequest(HelpRequest.TYPE.DOCTOR);
            req1.setStillOpen(3);
            Address addr = new Address(Locale.GERMAN);
            addr.setFeatureName("Wien, Westbahnhof");
            req1.setAddress(addr);
            req1.setStart(new Date());
            req1.setEnd(new Date());
            helpRequests.add(req1);
        }
    }*/

}
