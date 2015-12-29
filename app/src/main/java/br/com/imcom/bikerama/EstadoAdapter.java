package br.com.imcom.bikerama;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by BETO on 16/12/2015.
 */
public class EstadoAdapter extends ArrayAdapter<Estado> {

    private Activity context;
    ArrayList<Estado> data = null;

    public EstadoAdapter(Activity context, int resource,
                         ArrayList<Estado> data) {
        super(context, resource, data);
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            row = inflater.inflate(R.layout.simple_spinner_item, parent, false);
        }

        Estado item = data.get(position);

        if (item != null) { // Parse the data from each object and set it.

            TextView EstadoId = (TextView) row.findViewById(R.id.item_id);
            TextView EstadoName = (TextView) row.findViewById(R.id.item_value);
            if (EstadoId != null) {
                EstadoId.setText(item.getId());
            }
            if (EstadoName != null) {
                EstadoName.setText(item.getName());
            }

        }

        return row;
    }
}
