package com.adrian.sofergt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ComenziAdapter extends ArrayAdapter<Comanda> {
    ArrayList<Comanda> comenzi;
    Comanda comanda;
    Context context;

    public ComenziAdapter(Context _context, ArrayList<Comanda> _comenzi) {
        super(_context, 0, _comenzi);
        context = _context;
        comenzi = _comenzi;
    }

    Comanda _comanda;
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Get the data item for this position
        _comanda = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view


        if (convertView==null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.comanda, parent, false);
        }


        TextView titlu = convertView.findViewById(R.id.comanda_titlu);
        TextView tel=convertView.findViewById(R.id.comanda_tel);
        TextView timp_Stabilit=convertView.findViewById(R.id.comanda_timpstabilit);
        final TextView timp_ramas = convertView.findViewById(R.id.comanda_timpramas);
        TextView ora_la_care_a_fost_stabilita_comanda = convertView.findViewById(R.id.comanda_ora);


        titlu.setText(_comanda.getTitlu());
        tel.setText("Client tel: "+_comanda.nr_Tel_Client);
        timp_Stabilit.setText("Timp stabilit: "+_comanda.getTimp_Stabilit()+" minute");
        timp_ramas.setText(_comanda.getOraDestinatie());
        ora_la_care_a_fost_stabilita_comanda.setText("Ora stabilire comanda:" + _comanda.getOra_la_care_a_fost_stabilita_comanda());

        return convertView;
    }

    public ArrayList getAL() {
        return comenzi;
    }

    @Nullable
    @Override
    public Comanda getItem(int position) {
        return super.getItem(position);
    }

    public void removeItem(int index) {
        try {
            comenzi.remove(index);
            this.notifyDataSetChanged();
        } catch (Exception e) {
        }
    }


}