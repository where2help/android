package app.iamin.iamin;

import android.content.Intent;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<HelpRequest> helpRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // TODO: remove
        initializeData();

        // specify an adapter (see also next example)
        mAdapter = new ListAdapter(helpRequests);
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

    // TODO: remove me
    private void initializeData(){
        helpRequests = new ArrayList<HelpRequest>();
        HelpRequest req1 = new HelpRequest(HelpRequest.TYPE.DOCTOR);
        req1.setStillOpen(3);
        req1.setLocation(new Location("Vienna"));
        req1.setStart(new Date());
        req1.setEnd(new Date());
        helpRequests.add(req1);
    }

}
