package app.iamin.iamin;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.regex.Pattern;

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
    private TextView mAddress;
    private TextView mDate;
    private TextView mWeb;

    private CountView mCountView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getType() + " - " + getAddress());
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        GoogleMapOptions options = new GoogleMapOptions().liteMode(true);

        SupportMapFragment mMapFragment = SupportMapFragment.newInstance(options);
        mMapFragment.getMapAsync(DetailActivity.this);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.map, mMapFragment);
        ft.commit();

        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setText(getType());

        mAddress = (TextView) findViewById(R.id.address);
        mAddress.setText(getAddress());

        mDate = (TextView) findViewById(R.id.date);
        mDate.setText(getDate());

        mWeb = (TextView) findViewById(R.id.web);
        mWeb.setText("www.google.at");
        mWeb.setOnClickListener(this);

        mSubmitInfo = (TextView) findViewById(R.id.info);
        mButtonBar = (LinearLayout) findViewById(R.id.buttonBar);

        mAddButton = (Button) findViewById(R.id.add);
        mAddButton.setOnClickListener(this);

        mCancelButton = (Button) findViewById(R.id.cancel);
        mCancelButton.setOnClickListener(this);

        mSubmitButton = (Button) findViewById(R.id.submit);
        mSubmitButton.setOnClickListener(this);

        mCountView = (CountView) findViewById(R.id.count);
        mCountView.setCount(getStillOpen());
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
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setType("vnd.android.cursor.item/event")
                .putExtra("beginTime", getDateStart())
                .putExtra("endTime", getDateEnd())
                .putExtra("allDay", false)
                .putExtra("title", "Where2Help - " + getType())
                .putExtra("description", "Where2Help - " + getType() + " für " + "mind. 2h.")
                .putExtra("eventLocation", getAddress());
        startActivity(intent);
    }

    public void onActionShare() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Where2Help braucht noch " + getStillOpen() + " " +
                getType() + " am " + getDate() + " für " + "mind. 2h" + " am " + getAddress() + ".");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void openInBrowser(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.getUiSettings().setMapToolbarEnabled(false);
        LatLng sydney = new LatLng(-33.867, 151.206);

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));

        map.addMarker(new MarkerOptions()
                .title("Sydney")
                .snippet("The most populous city in Australia.")
                .position(sydney));
    }

    private String getType() {
        return getIntent().getExtras().getString("type");
    }

    private String getAddress() {
        return getIntent().getExtras().getString("address");
    }

    private int getStillOpen() {
        return getIntent().getExtras().getInt("stillOpen");
    }

    private String getDate() {
        return getIntent().getExtras().getString("date");
    }

    private long getDateStart() {
        return getIntent().getExtras().getLong("dateStart");
    }

    private long getDateEnd() {
        return getIntent().getExtras().getLong("dateEnd");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default: break;
            case R.id.submit:
                if (mSubmitInfo.getVisibility() != View.VISIBLE) {
                    mSubmitButton.setText("Registriere...");
                    mSubmitButton.setEnabled(false);
                    // call rest
                    registerForEvent();
                    mSubmitButton.setEnabled(true);
                    mSubmitInfo.setVisibility(View.VISIBLE);
                    mButtonBar.setVisibility(View.VISIBLE);
                    mSubmitButton.setText("Weitersagen!");
                } else {
                    onActionShare();
/*                    mSubmitButton.setText("I'm in!");
                    mSubmitInfo.setVisibility(View.GONE);
                    mButtonBar.setVisibility(View.GONE);*/
                }
                break;
            case R.id.cancel:
                showDialog();
                break;
            case R.id.add:
                addToCalendar();
                break;
            case R.id.web:
                openInBrowser("http://" + mWeb.getText().toString());
                break;
        }
    }

    private void registerForEvent() {
        // get phone number
        TelephonyManager tMgr = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = tMgr.getLine1Number();
        // get email
        String email = null;
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                email = account.name;
                break;
            }
        }
        System.out.println("Email: " + email + ", phone: "+ phoneNumber);
    }
}
