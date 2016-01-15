package br.com.imcom.bikerama;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

/**
 * Created by BETO on 08/01/2016.
 */
public class TipoAtividade implements OnItemSelectedListener{

    public void onItemSelected(AdapterView<?> parent, View view, int pos,
                               long id) {

        /*Toast.makeText(parent.getContext(),
                "On Item Select : \n" + parent.getItemAtPosition(pos).toString(),
                Toast.LENGTH_LONG).show();*/
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }
}
