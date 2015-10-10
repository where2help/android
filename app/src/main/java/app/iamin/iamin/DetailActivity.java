package app.iamin.iamin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Markus on 10.10.15.
 */
public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private LinearLayout mButtonBar;
    private Button mSubmitButton;
    private Button mCancelButton;
    private Button mAddButton;
    private TextView mSubmitInfo;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Title");
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSubmitInfo = (TextView) findViewById(R.id.info);

        mSubmitButton = (Button) findViewById(R.id.submit);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSubmitInfo.getVisibility() != View.VISIBLE) {
                    mSubmitInfo.setVisibility(View.VISIBLE);
                    mButtonBar.setVisibility(View.VISIBLE);
                    mSubmitButton.setText("Weitersagen!");
                } else {
                    mSubmitButton.setText("I'm in!");
                    mSubmitInfo.setVisibility(View.GONE);
                    mButtonBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng sydney = new LatLng(-33.867, 151.206);

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));

        map.addMarker(new MarkerOptions()
                .title("Sydney")
                .snippet("The most populous city in Australia.")
                .position(sydney));
    }
}
