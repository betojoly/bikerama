package br.com.imcom.bikerama;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by BETO on 21/12/2015.
 */
public class IdadeAdapter extends ArrayAdapter<Idade> {

    private Activity context;
    ArrayList<Idade> data = null;

    public IdadeAdapter(Activity context, int resource,
                         ArrayList<Idade> data) {
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

        Idade item = data.get(position);

        if (item != null) { // Parse the data from each object and set it.

            TextView IdadeId = (TextView) row.findViewById(R.id.item_id);
            TextView IdadeName = (TextView) row.findViewById(R.id.item_value);
            if (IdadeId != null) {
                IdadeId.setText(item.getId());
            }
            if (IdadeName != null) {
                IdadeName.setText(item.getName());
            }

        }

        return row;
    }
}
