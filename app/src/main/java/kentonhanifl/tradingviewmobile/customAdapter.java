package kentonhanifl.tradingviewmobile;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class customAdapter extends ArrayAdapter<Currency> implements View.OnClickListener
{
    public customAdapter(ArrayList<Currency> data, Context context) {
        super(context, R.layout.tablerow, data);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.tablerow, parent, false);

        //Set row colors
        //Colors are in colors.xml, but I couldn't find a way to actually reference them there without it raising a warning...
        if(position%2==0)
        {
            view.setBackgroundColor(Color.parseColor("#c5c9c8"));
        }
        else
        {
            view.setBackgroundColor(Color.parseColor("#f2f7f6"));
        }

        TextView text1 = (TextView) view.findViewById(R.id.tableCell1);
        text1.setText(getItem(position).MarketName);

        TextView text2 = (TextView) view.findViewById(R.id.tableCell2);
        text2.setText(String.format("%.8f",getItem(position).Last));

        return view;
    }

    @Override
    public void onClick(View view) {

    }
}
