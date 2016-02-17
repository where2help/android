package app.iamin.iamin.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import app.iamin.iamin.R;
import butterknife.Bind;
import butterknife.ButterKnife;

public class TermsActivity extends AppCompatActivity {

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.content) TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        ButterKnife.bind(this);

        toolbar.setTitle("Nutzungsbedingungen");
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        content.setText(" Da du, o Herr, dich einmal wieder nahst\n" +
                "    Und fragst, wie alles sich bei uns befinde,\n" +
                "    Und du mich sonst gewöhnlich gerne sahst,\n" +
                "    So siehst du mich auch unter dem Gesinde.\n" +
                "    Verzeih, ich kann nicht hohe Worte machen,\n" +
                "    Und wenn mich auch der ganze Kreis verhöhnt;\n" +
                "    Mein Pathos brächte dich gewiß zum Lachen,\n" +
                "    Hättst du dir nicht das Lachen abgewöhnt.\n" +
                "    Von Sonn' und Welten weiß ich nichts zu sagen,\n" +
                "    Ich sehe nur, wie sich die Menschen plagen.\n" +
                "    Der kleine Gott der Welt bleibt stets von gleichem Schlag,\n" +
                "    Und ist so wunderlich als wie am ersten Tag.\n" +
                "    Ein wenig besser würd er leben,\n" +
                "    Hättst du ihm nicht den Schein des Himmelslichts gegeben;\n" +
                "    Er nennt's Vernunft und braucht's allein,\n" +
                "    Nur tierischer als jedes Tier zu sein.\n" +
                "    Er scheint mir, mit Verlaub von euer Gnaden,\n" +
                "    Wie eine der langbeinigen Zikaden,\n" +
                "    Die immer fliegt und fliegend springt\n" +
                "    Und gleich im Gras ihr altes Liedchen singt;\n" +
                "    Und läg er nur noch immer in dem Grase!\n" +
                "    In jeden Quark begräbt er seine Nase.");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
