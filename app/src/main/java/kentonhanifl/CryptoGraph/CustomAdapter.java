package kentonhanifl.CryptoGraph;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;



public class CustomAdapter extends ArrayAdapter<Currency>
{
    ArrayList<Currency> OrigCurrencies;
    ArrayList<Currency> ShownItems;
    Context context;
    SharedPreferences sharedPreferencesData;


    /*
    --------------------------------------------------------------------------------
    CONSTRUCTOR
    --------------------------------------------------------------------------------
    */

    public CustomAdapter(ArrayList<Currency> data, Context context, SharedPreferences sharedPreferencesData) {
        super(context, R.layout.tablerow, data);
        OrigCurrencies = new ArrayList<Currency>();
        OrigCurrencies.addAll(data);
        ShownItems = new ArrayList<Currency>();
        ShownItems.addAll(data);
        this.context=context;
        this.sharedPreferencesData = sharedPreferencesData;
    }

    /*
    --------------------------------------------------------------------------------
    FILTER
    Filters the ListView when called by the SearchView queries
    --------------------------------------------------------------------------------
    */

    public Filter getFilter()
    {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                Log.d(Main.tag+"before ", String.valueOf((Main.Currencies.size())));
                FilterResults resultsReturned = new FilterResults();
                ArrayList<Currency> results = new ArrayList<Currency>();
                if(constraint!= null && OrigCurrencies.size()>0 && OrigCurrencies!=null)
                {
                    for (Currency currency : OrigCurrencies)
                    {
                        if (currency.getName().toLowerCase()
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
                    resultsReturned.values = OrigCurrencies;
                    resultsReturned.count = OrigCurrencies.size();
                }
                return resultsReturned;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                ShownItems = (ArrayList<Currency>) filterResults.values;
                clear();
                Log.d(Main.tag+"after ", String.valueOf((Main.Currencies.size())));
                for(int i = 0; i<ShownItems.size(); i++)
                {
                    add(ShownItems.get(i));
                }
                notifyDataSetChanged();
            }
        };
    }

    /*
    --------------------------------------------------------------------------------
    GET VIEW
    Sets all of the text in the ListView
    --------------------------------------------------------------------------------
    */

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

        if (getItem(position).favorite)
        {
            view.setBackgroundColor(Color.parseColor("#fff69b"));
            int i = 0;
        }


        /*
        ---------------------------
        Column One
        ---------------------------
        */

        TextView text1 = (TextView) view.findViewById(R.id.tableCell1);
        final String name = getItem(position).getName();
        text1.setText(name);
        text1.setTextColor(Color.parseColor("#3179ed"));
        final int pos = position;

        text1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences.Editor editor = sharedPreferencesData.edit();
                editor.putString("ChartMarketName", getItem(pos).MarketName);
                editor.commit();

                Intent about = new Intent(context, ChartActivity.class);
                context.startActivity(about);

            }
        });

        /*
        ---------------------------
        Column Two
        ---------------------------
        */

        TextView text2 = (TextView) view.findViewById(R.id.tableCell2);
        final String last = String.format("%.8f",getItem(position).Last);
        text2.setText(last); //-------Raises a weird warning...


        text2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Toast toast = Toast.makeText(getContext(), "Favorited", Toast.LENGTH_SHORT);
                toast.show();
                */
                if (getItem(pos).favorite)
                {
                    unfavorite(pos);
                }
                else
                {
                    setFavorite(pos);
                }
                notifyDataSetChanged();
            }
            });


        /*
        ---------------------------
        Column Three
        ---------------------------
        */
        TextView text3 = (TextView) view.findViewById(R.id.tableCell3);
        final String change = String.format("%.1f",getItem(position).getChange());
        text3.setText(change+"%");

        text3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*oast toast = Toast.makeText(getContext(), "Favorited", Toast.LENGTH_SHORT);
                toast.show();*/
                if (getItem(pos).favorite)
                {
                    unfavorite(pos);
                }
                else
                {
                    setFavorite(pos);
                }
                notifyDataSetChanged();
            }
        });

        return view;
    }

    /*
    --------------------------------------------------------------------------------
    General functions not related to setting the view or filtering data
    --------------------------------------------------------------------------------
    */

    public void setFavorite(int position)
    {
        Currency itm = new Currency();
        itm = getItem(position);
        int itmindex = Main.Currencies.indexOf(itm);
        Main.Currencies.get(itmindex).favorite=true;
        getItem(position).favorite = true;
        Main.saveCurrencies();
    }

    public void unfavorite(int position)
    {
        Currency itm = new Currency();
        itm = getItem(position);
        int itmindex = Main.Currencies.indexOf(itm);
        Main.Currencies.get(itmindex).favorite=false;
        getItem(position).favorite = false;
        Main.saveCurrencies();
    }
}
