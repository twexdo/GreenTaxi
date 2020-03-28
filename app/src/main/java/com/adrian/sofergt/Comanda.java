package com.adrian.sofergt;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Comanda {
    static final long MINUT = 60000;//millisecs
    private static int counter_comenzi = 0;
    String TAG = "ComandaDebug";
    String titlu;
    String nr_Tel_Client;
    int timp_Stabilit;
    Date timp_Actual;
    String ora_la_care_ar_trebui_sa_ajunga_comanda;
    String ora_la_care_a_fost_stabilita_comanda;
    String smsID = "";
    private String comandaId_inFDB;

    public Comanda(String smsid, String nr_Tel_Client, int timp_Stabilit, String ora_la_care_a_fost_stabilita_comanda) {

        this.nr_Tel_Client = nr_Tel_Client;
        this.timp_Stabilit = timp_Stabilit;
        this.ora_la_care_a_fost_stabilita_comanda = ora_la_care_a_fost_stabilita_comanda;
        this.smsID = smsid;
    }

    public Comanda(String nr_Tel_Client, int timp_Stabilit, String orastabilita, int nr_comenzi, String thisComandaId, String smsId) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        ora_la_care_a_fost_stabilita_comanda = orastabilita;
        ora_la_care_ar_trebui_sa_ajunga_comanda = sdf.format(timp_dupa_X_minute(orastabilita, timp_Stabilit));
        counter_comenzi++;
        titlu = "Comanda " + (nr_comenzi + 1);
        this.nr_Tel_Client = nr_Tel_Client;
        this.timp_Stabilit = timp_Stabilit;
        this.comandaId_inFDB = thisComandaId;
        this.smsID = smsId;
    }

    public String getComandaId_inFDB() {
        return comandaId_inFDB;
    }

    public String getSmsID() {
        return smsID;
    }

    public String getNr_Tel_Client() {
        return nr_Tel_Client;
    }

    public int getTimp_Stabilit() {
        return this.timp_Stabilit;
    }

    public String getOra_la_care_a_fost_stabilita_comanda() {
        return ora_la_care_a_fost_stabilita_comanda;
    }

    Date timp_dupa_X_minute(String currentDateandTime, int minute) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEEEEE HH:mm:ss");

        Calendar calendar;
        try {
            Date date = null;
            try {
                date = sdf.parse(currentDateandTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MINUTE, minute);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
        return calendar.getTime();
    }

    public String getTitlu() {
        return titlu;
    }


    public String getOraDestinatie() {
        return ora_la_care_ar_trebui_sa_ajunga_comanda;
    }
}
