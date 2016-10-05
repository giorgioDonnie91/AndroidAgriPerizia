package org.droidplanner.android;


import org.droidplanner.android.utils.Utils;
import org.json.JSONObject;

public class Polizza {

    private String codice;
    private String codiceCliente;
    private String regione;
    private String provincia;
    private String citta;
    private String via;
    private String numeroCivico;
    private String cap;
    private String dataContratto;

    public Polizza(JSONObject polizzaJSON) {
        if(polizzaJSON == null)
            return;
        codice = polizzaJSON.optString("codPZ");
        codiceCliente = polizzaJSON.optString("codC");
        regione = polizzaJSON.optString("regione");
        provincia = polizzaJSON.optString("provincia");
        citta = polizzaJSON.optString("citta");
        via = polizzaJSON.optString("via");
        numeroCivico = polizzaJSON.optString("nCivico");
        cap = polizzaJSON.optString("cap");
        dataContratto = polizzaJSON.optString("dataContratto");
    }

    public String getCodice() {
        return Utils.secureGet(codice);
    }

    public String getCodiceCliente() {
        return Utils.secureGet(codiceCliente);
    }

    public String getRegione() {
        return Utils.secureGet(regione);
    }

    public String getProvincia() {
        return Utils.secureGet(provincia);
    }

    public String getCitta() {
        return Utils.secureGet(citta);
    }

    public String getVia() {
        return Utils.secureGet(via);
    }

    public String getNumeroCivico() {
        return Utils.secureGet(numeroCivico);
    }

    public String getCap() {
        return Utils.secureGet(cap);
    }

    public String getDataContratto() {
        return Utils.secureGet(dataContratto);
    }
}
