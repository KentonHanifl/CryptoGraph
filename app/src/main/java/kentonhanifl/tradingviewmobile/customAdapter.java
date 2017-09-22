package kentonhanifl.tradingviewmobile;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;


public class customAdapter extends ArrayAdapter<Currency>
{
    ArrayList<Currency> Currencies;
    ArrayList<Currency> ShownItems;

    public customAdapter(ArrayList<Currency> data, Context context) {
        super(context, R.layout.tablerow, data);
        Currencies = new ArrayList<Currency>();
        Currencies.addAll(data);
        ShownItems = new ArrayList<Currency>();
        ShownItems.addAll(data);
    }


    public Filter getFilter()
    {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults resultsReturned = new FilterResults();
                ArrayList<Currency> results = new ArrayList<Currency>();
                if(constraint!= null && Currencies.size()>0 && Currencies!=null)
                {
                    for (Currency currency : Currencies)
                    {
                        if (currency.MarketName.toLowerCase()
                                               .contains(constraint.toString()))
                        {
                            results.add(currency);
                        }
                    }
                    resultsReturned.values = results;
                    resultsReturned.count = results.size();
                }
                else
                {
                    resultsReturned.values = Currencies;
                    resultsReturned.count = Currencies.size();
                }
                return resultsReturned;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                ShownItems = (ArrayList<Currency>) filterResults.values;
                notifyDataSetChanged();
                clear();
                for(int i = 0; i<ShownItems.size(); i++)
                {
                    add(ShownItems.get(i));
                    notifyDataSetInvalidated();
                }

            }
        };
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.tablerow, parent, false);//-------Seriously get to this warning. Need to learn how View Holder Paterns work...

        //Set row colors
        //----------Colors are in colors.xml, but I couldn't find a way to actually reference them there without it raising a warning...
        if(position%2==0)
        {
            view.setBackgroundColor(Color.parseColor("#c5c9c8"));
        }
        else
        {
            view.setBackgroundColor(Color.parseColor("#f2f7f6"));
        }

        TextView text1 = (TextView) view.findViewById(R.id.tableCell1);
        final String marketName = getItem(position).MarketName;
        text1.setText(marketName);

        text1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Eventually just use this one to
                Toast toast = Toast.makeText(getContext(), marketName, Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        TextView text2 = (TextView) view.findViewById(R.id.tableCell2);
        final String last = String.format("%.8f",getItem(position).Last);
        text2.setText(last); //-------Raises a weird warning...

        text2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast = Toast.makeText(getContext(), last, Toast.LENGTH_SHORT);
                toast.show();
            }
            });

        return view;
    }


}
