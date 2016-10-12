package org.droidplanner.android.activities;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.droidplanner.android.Sinistro;
import org.droidplanner.android.fragments.SearchToolFragment;
import org.droidplanner.android.network.ComunicazioneConServerThread;
import org.droidplanner.android.Polizza;
import org.droidplanner.android.R;
import org.droidplanner.android.network.request.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PolizzeActivity extends DrawerNavigationUI implements SearchToolFragment.SearchToolListener{

    TextView polizzeTotTextView;
    PolizzeAdapter polizzeAdapter;
    ProgressBar progressBar;


    private FragmentManager fragmentManager;

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_container;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        fragmentManager = getSupportFragmentManager();

        super.onCreate(savedInstanceState);

        polizzeAdapter = new PolizzeAdapter();

        setContentView(R.layout.activity_polizze);
        polizzeTotTextView = new TextView(this);
        polizzeTotTextView.setPadding(0, (int)getResources().getDimension(R.dimen.vertical_margin), 0, 0);
        ListView polizzeListView = (ListView)findViewById(R.id.polizze_list_view);
        polizzeListView.addHeaderView(polizzeTotTextView);
        polizzeListView.setAdapter(polizzeAdapter);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
    }

    @Override
    protected int getNavigationDrawerMenuItemId() {
        return R.id.navigation_editor;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void addToolbarFragment(){
        final int toolbarId = getToolbarId();
        SearchToolFragment searchToolFragment = (SearchToolFragment) fragmentManager.findFragmentById(toolbarId);
        if (searchToolFragment == null) {
            searchToolFragment = new SearchToolFragment();
            fragmentManager.beginTransaction().add(toolbarId, searchToolFragment).commit();
        }
    }

    @Override
    public void onSearch(String search) {
        if(TextUtils.isEmpty(search))
            return;

        Request cercaPolizzeReq = ComunicazioneConServerThread.selectPolizzeByCodiceClienteRequest(search);
        new ComunicazioneConServerThread(
                cercaPolizzeReq,
                new ComunicazioneConServerThread.RequestListener() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            Log.d("POLIZZE", response);
                            JSONObject responseJSON = new JSONObject(response);
                            JSONArray polizzeJSON = responseJSON.getJSONArray("polizze");
                            for(int i=0; i<polizzeJSON.length(); i++){
                                Polizza polizza = new Polizza(polizzeJSON.optJSONObject(i));
                                polizzeAdapter.polizze.add(polizza);
                            }
                            polizzeAdapter.notifyDataSetChanged();
                            polizzeTotTextView.setText(getResources().getQuantityString(R.plurals.polizze_tot_string, polizzeJSON.length(), polizzeJSON.length()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(int responseCode, String response) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
        ).start();
        progressBar.setVisibility(View.VISIBLE);
    }


    private class PolizzeAdapter extends BaseAdapter{

        ArrayList<Polizza> polizze;

        public PolizzeAdapter() {
            this.polizze = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return polizze.size();
        }

        @Override
        public Object getItem(int position) {
            return polizze.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.polizza_layout, parent, false);
            }

            TextView indirizzoPrimaLineaTextView = (TextView)convertView.findViewById(R.id.indirizzo_prima_linea_text_view);
            TextView indirizzoSecondaLineaTextView = (TextView)convertView.findViewById(R.id.indirizzo_seconda_linea_text_view);
            TextView dataTextView = (TextView)convertView.findViewById(R.id.data_text_view);

            Polizza polizza = (Polizza)getItem(position);

            indirizzoPrimaLineaTextView.setText(polizza.getVia() + " " + polizza.getNumeroCivico());
            indirizzoSecondaLineaTextView.setText(polizza.getCap() + " " + polizza.getCitta() + " (" + polizza.getProvincia() + ")");

            SimpleDateFormat inputDateFormat = new SimpleDateFormat("ddMMyyyy");
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            try {
                Date dataPolizza = inputDateFormat.parse(polizza.getDataContratto());
                dataTextView.setText(outputDateFormat.format(dataPolizza));
            } catch (ParseException e) {
                e.printStackTrace();
                dataTextView.setText(outputDateFormat.format("-"));
            }
            convertView.setOnClickListener(new PolizzaClickListener(convertView, polizza));

            return convertView;
        }

    }


    private class PolizzaClickListener implements View.OnClickListener{

        private Polizza polizza;
        private View clickedView;

        public PolizzaClickListener(View clickedView, Polizza polizza) {
            this.polizza = polizza;
            this.clickedView = clickedView;
        }

        @Override
        public void onClick(View v) {
            Request cercaPercorsiReq = ComunicazioneConServerThread.selectPercorsiByCodicePolizzaRequest(polizza.getCodice());
            new ComunicazioneConServerThread(
                    cercaPercorsiReq,
                    new ComunicazioneConServerThread.RequestListener() {
                        @Override
                        public void onSuccess(String response) {
                            try {
                                Log.d("PERCORSI", response);
                                JSONObject responseJSON = new JSONObject(response);
                                JSONArray percorsiJSON = responseJSON.optJSONArray("percorsi");
                                if(percorsiJSON.length() == 0) {
                                    startActivity(new Intent(PolizzeActivity.this, EditorActivity.class));
                                    progressBar.setVisibility(View.GONE);
                                } else {
                                    cercaSinistri(polizza.getCodice());
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(int responseCode, String response) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
            ).start();
            progressBar.setVisibility(View.VISIBLE);
        }


        private void cercaSinistri(String codicePolizza) {
            Request cercaSinistriReq = ComunicazioneConServerThread.selectSinistriByCodicePolizzaRequest(codicePolizza);
            new ComunicazioneConServerThread(
                    cercaSinistriReq,
                    new ComunicazioneConServerThread.RequestListener() {
                        @Override
                        public void onSuccess(String response) {
                            try {
                                Log.d("SINISTRI", response);
                                JSONObject responseJSON = new JSONObject(response);
                                JSONArray sinistriJSON = responseJSON.getJSONArray("sinistri");
                                for(int i=0; i<sinistriJSON.length(); i++){
                                    Sinistro sinistro = new Sinistro(sinistriJSON.optJSONObject(i));
                                    addViewSinistro((LinearLayout)clickedView.findViewById(R.id.contenitore_sinistri), sinistro);
                                }
                                //todo scaricare waypoint
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(int responseCode, String response) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
            ).start();
            progressBar.setVisibility(View.VISIBLE);
        }

    }

    private void addViewSinistro(LinearLayout parent, Sinistro sinistro){
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        View sinistroView = layoutInflater.inflate(R.layout.sinistro_layout, parent, false);

        SimpleDateFormat inputDateFormat = new SimpleDateFormat("ddMMyyyy");
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date dataPolizza = inputDateFormat.parse(sinistro.getDataSinistro());
            ((TextView)sinistroView.findViewById(R.id.data_sinistro)).setText(outputDateFormat.format(dataPolizza));
        } catch (ParseException e) {
            e.printStackTrace();
            ((TextView)sinistroView.findViewById(R.id.data_sinistro)).setText(outputDateFormat.format("-"));
        }
        ((TextView)sinistroView.findViewById(R.id.codice_sinistro)).setText(sinistro.getCodice());
        parent.addView(sinistroView, -1, params);
    }

}
