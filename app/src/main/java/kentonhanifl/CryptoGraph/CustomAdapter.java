package kentonhanifl.CryptoGraph;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;



public class CustomAdapter extends ArrayAdapter<Currency>
{
    private ArrayList<Currency> OrigCurrencies;
    private ArrayList<Currency> ShownItems;
    private Context context;
    private Database database;


    /*
    --------------------------------------------------------------------------------
    CONSTRUCTOR
    --------------------------------------------------------------------------------
    */

    public CustomAdapter(ArrayList<Currency> data, Context context) {
        super(context, R.layout.tablerow, data);
        OrigCurrencies = new ArrayList<Currency>();
        OrigCurrencies.addAll(data);
        ShownItems = new ArrayList<Currency>();
        ShownItems.addAll(data);
        this.context=context;
        database = Main.database;
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


        //Set row colors alternating between grey and white. If the coin is favorited, instead draw the row as yellow
        int rowBackgroundDark = ContextCompat.getColor(context, R.color.rowBackgroundDark);
        int rowBackgroundLight = ContextCompat.getColor(context, R.color.rowBackgroundLight);
        int rowBackgroundFavorite = ContextCompat.getColor(context, R.color.rowBackgroundFavorite);
        if(position%2==0)
        {
            view.setBackgroundColor(rowBackgroundDark);
        }
        else
        {
            view.setBackgroundColor(rowBackgroundLight);
        }

        if (getItem(position).favorite)
        {
            view.setBackgroundColor(rowBackgroundFavorite);
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
        text1.setPaintFlags(text1.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        final int pos = position;

        text1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                database.setupChart(getItem(pos).MarketName);
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
    Consider moving somewhere else
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
