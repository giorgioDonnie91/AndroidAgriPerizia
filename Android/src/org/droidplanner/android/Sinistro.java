package org.droidplanner.android;


import org.droidplanner.android.utils.Utils;
import org.json.JSONObject;

public class Sinistro {

    private String codice;
    private String codiceRischio;
    private String codicePolizza;
    private String dataSinistro;
    private String dataPerizia;
    private boolean chiuso;

    public Sinistro(JSONObject polizzaJSON) {
        if(polizzaJSON == null)
            return;
        codice = polizzaJSON.optString("CodS");
        codiceRischio = polizzaJSON.optString("CodR");
        codicePolizza = polizzaJSON.optString("CodPZ");
        dataSinistro = polizzaJSON.optString("DataSinistro");
        dataPerizia = polizzaJSON.optString("DataPerizia");
        chiuso = !"0".equals(polizzaJSON.optString("Chiuso"));
    }

    public String getCodice() {
        return Utils.secureGet(codice);
    }

    public String getCodiceRischio() {
        return Utils.secureGet(codiceRischio);
    }

    public String getCodicePolizza() {
        return Utils.secureGet(codicePolizza);
    }

    public String getDataSinistro() {
        return Utils.secureGet(dataSinistro);
    }

    public String getDataPerizia() {
        return Utils.secureGet(dataPerizia);
    }

    public boolean isChiuso() {
        return chiuso;
    }
}
