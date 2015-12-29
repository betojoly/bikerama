package br.com.imcom.bikerama;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by BETO on 22/12/2015.
 */
public class TipoAdapter extends ArrayAdapter<Tipo> {

    private Activity context;
    ArrayList<Tipo> data = null;

    public TipoAdapter(Activity context, int resource,
                       ArrayList<Tipo> data) {
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

        Tipo item = data.get(position);

        if (item != null) { // Parse the data from each object and set it.

            TextView TipoId = (TextView) row.findViewById(R.id.item_id);
            TextView TipoName = (TextView) row.findViewById(R.id.item_value);
            if (TipoId != null) {
                TipoId.setText(item.getId());
            }
            if (TipoName != null) {
                TipoName.setText(item.getName());
            }

        }

        return row;
    }
}
