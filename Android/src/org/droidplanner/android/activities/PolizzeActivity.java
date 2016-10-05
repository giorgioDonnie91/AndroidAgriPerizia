package org.droidplanner.android.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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

public class PolizzeActivity extends AppCompatActivity {

    EditText cercaIDClienteEditText;
    TextView polizzeTotTextView;
    PolizzeAdapter polizzeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        polizzeAdapter = new PolizzeAdapter();

        setContentView(R.layout.activity_polizze);
        cercaIDClienteEditText = (EditText)findViewById(R.id.id_cliente_edit_text);
        polizzeTotTextView = (TextView)findViewById(R.id.polizze_tot_text_view);
        ListView polizzeListView = (ListView)findViewById(R.id.polizze_list_view);
        polizzeListView.setAdapter(polizzeAdapter);
    }

    public void cercaPolizze(View view){
        String idCliente = cercaIDClienteEditText.getText().toString();
        if(TextUtils.isEmpty(idCliente))
            return;

        Request cercaPolizzeReq = ComunicazioneConServerThread.selectPolizzeByCodiceClienteRequest(idCliente);
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
                    }

                    @Override
                    public void onError(int responseCode, String response) {

                    }
                }
        ).start();
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
            convertView.setOnClickListener(new PolizzaClickListener(polizza));

            return convertView;
        }

    }


    private class PolizzaClickListener implements View.OnClickListener{

        private Polizza polizza;

        public PolizzaClickListener(Polizza polizza) {
            this.polizza = polizza;
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
                                    startActivity(new Intent(PolizzeActivity.this, FlightActivity.class));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(int responseCode, String response) {

                        }
                    }
            ).start();
        }
    }


}
