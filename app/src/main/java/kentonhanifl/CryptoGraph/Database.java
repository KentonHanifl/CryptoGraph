package kentonhanifl.CryptoGraph;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

import static kentonhanifl.CryptoGraph.Main.tag;

public class Database{

    private SharedPreferences data;
    private int dataSize;

    Database(SharedPreferences data)
    {
        this.data = data;
        dataSize = data.getInt("SIZE", 0);
    }

    public int getDataSize()
    {
        return dataSize;
    }

    public void loadDatabase(ArrayList<Currency> Currencies, ArrayList<Currency> BannerCurrencies, BannerCondition<Currency> condition)
    {

        Currency temp;
        for(int i = 0; i < dataSize; i++) {
            temp = new Currency();
            temp.MarketName = data.getString("MarketName_" + i, "ERR1");
            temp.Last = data.getFloat("Last_" + i, 0);
            temp.favorite = data.getBoolean("Favorite_" + i, false);
            temp.PrevDay = data.getFloat("PrevDay_" + i, 0);
            if (Currencies.indexOf(temp) == -1) {
                Currencies.add(temp);
                if (condition.test(temp)) {
                    BannerCurrencies.add(temp);
                }
            }
            else
            {
                if (!temp.MarketName.startsWith("ETH-")) { //Take out the ETH markets
                    Currencies.get(Currencies.indexOf(temp)).Last = temp.Last;
                    Currencies.get(Currencies.indexOf(temp)).PrevDay = temp.PrevDay;
                    Currencies.get(Currencies.indexOf(temp)).favorite = temp.favorite;
                }
            }
            if (BannerCurrencies.indexOf(temp)== -1)
            {
                if (condition.test(temp)){
                    BannerCurrencies.add(temp);
                }
            }
            else
            {
                if (temp.MarketName.startsWith("USDT-")) {
                    BannerCurrencies.get(BannerCurrencies.indexOf(temp)).Last = temp.Last;
                }
            }
        }
    }

    public void loadOnlyCurrencies(ArrayList<Currency> Currencies) {
        Currency temp;
        for (int i = 0; i < dataSize; i++) {
            temp = new Currency();
            temp.MarketName = data.getString("MarketName_" + i, "ERR1");
            temp.Last = data.getFloat("Last_" + i, 0);
            temp.favorite = data.getBoolean("Favorite_" + i, false);
            temp.PrevDay = data.getFloat("PrevDay_" + i, 0);
            if (Currencies.indexOf(temp) == -1) {
                Currencies.add(temp);
            } else {
                if (!temp.MarketName.startsWith("ETH-")) { //Take out the ETH markets
                    Currencies.get(Currencies.indexOf(temp)).Last = temp.Last;
                    Currencies.get(Currencies.indexOf(temp)).PrevDay = temp.PrevDay;
                    Currencies.get(Currencies.indexOf(temp)).favorite = temp.favorite;
                }
            }
        }
    }


    public void save(ArrayList<Currency> Currencies)
    {
        /*
        Cheap way to get/put an array with shared preferences. Found the implementation at https://stackoverflow.com/questions/7057845/save-arraylist-to-sharedpreferences (The second answer as of 9/23/17)
        Each iteration grabs the data members for each coin and adds it to Currencies
        ORDER:
        MarketName
        Last
        favorite
        PrevDay
         */
        SharedPreferences.Editor editor = data.edit();
        editor.putInt("SIZE", Currencies.size());
        int i = 0;
        for(Currency c : Currencies)
        {
            editor.remove("MarketName_"+i);
            editor.putString("MarketName_"+i, Currencies.get(i).MarketName);
            editor.remove("Last_"+i);
            editor.putFloat("Last_"+i, Currencies.get(i).Last);
            editor.remove("Favorite_"+i);
            editor.putBoolean("Favorite_"+i, Currencies.get(i).favorite);
            editor.remove("PrevDay_"+i);
            editor.putFloat("PrevDay_"+i, Currencies.get(i).PrevDay);
            i++;
        }
        editor.apply();
    }


    public void setupChart(String MarketName)
    {
        SharedPreferences.Editor editor = data.edit();
        editor.putString("ChartMarketName", MarketName);
        editor.commit();
    }

    public String getMarketName()
    {
        return data.getString("ChartMarketName","USDT-BTC");
    }
}

//Just made so that eventually the banner can do other things besides the USDT if desired.
abstract class BannerCondition<Currency> {
    abstract boolean test(Currency c);
}
