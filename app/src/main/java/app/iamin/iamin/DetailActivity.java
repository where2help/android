package app.iamin.iamin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v7.app.AlertDialog;
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

import java.util.Calendar;

/**
 * Created by Markus on 10.10.15.
 */
public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener{

    private LinearLayout mButtonBar;
    private Button mSubmitButton;
    private Button mCancelButton;
    private Button mAddButton;
    private TextView mSubmitInfo;

    private TextView mTitle;
    private TextView mAdress;

    private CountView mCountView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getName() + " - " + getAddress());
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setText(getName());

        mAdress = (TextView) findViewById(R.id.address);
        mAdress.setText(getAddress());

        mSubmitInfo = (TextView) findViewById(R.id.info);
        mButtonBar = (LinearLayout) findViewById(R.id.buttonBar);

        mAddButton = (Button) findViewById(R.id.add);
        mAddButton.setOnClickListener(this);

        mCancelButton = (Button) findViewById(R.id.cancel);
        mCancelButton.setOnClickListener(this);

        mSubmitButton = (Button) findViewById(R.id.submit);
        mSubmitButton.setOnClickListener(this);

        mCountView = (CountView) findViewById(R.id.count);
        mCountView.setCount(2);
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Wir brauchen dich! Willst du wirklich absagen?");
        builder.setPositiveButton("Natürlich nicht!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // FIRE ZE MISSILES!
            }
        });
        builder.setNegativeButton("Ja...", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addToCalendar(){
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2012, 0, 19, 7, 30);
        Calendar endTime = Calendar.getInstance();
        endTime.set(2012, 0, 19, 8, 30);
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setType("vnd.android.cursor.item/event")
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, "Where2Help - " + getName())
                .putExtra(CalendarContract.Events.DESCRIPTION, "Group class")
                .putExtra(CalendarContract.Events.EVENT_LOCATION, "The gym");
        startActivity(intent);
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

    private String getName() {
        return getIntent().getExtras().getString("name");
    }

    private String getAddress() {
        return getIntent().getExtras().getString("address");
    }

    private String getCount() {
        return getIntent().getExtras().getString("count");
    }

    private String getTime() {
        return getIntent().getExtras().getString("time");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default: break;
            case R.id.submit:
                if (mSubmitInfo.getVisibility() != View.VISIBLE) {
                    mSubmitInfo.setVisibility(View.VISIBLE);
                    mButtonBar.setVisibility(View.VISIBLE);
                    mSubmitButton.setText("Weitersagen!");
                } else {
                    mSubmitButton.setText("I'm in!");
                    mSubmitInfo.setVisibility(View.GONE);
                    mButtonBar.setVisibility(View.GONE);
                }
                break;
            case R.id.cancel:
                showDialog();
                break;
            case R.id.add:
                addToCalendar();
                break;
        }
    }
}
