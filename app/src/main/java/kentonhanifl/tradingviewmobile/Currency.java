package kentonhanifl.tradingviewmobile;

import android.support.annotation.NonNull;

import java.util.Comparator;

public class Currency
{
    public String MarketName;
    public float Last;
    boolean favorite;

    Currency()
    {
        favorite = false;
        MarketName="";
        Last = 0;
    }

    @Override
    public String toString()
    {
        return this.getName();
    }


    //Just want to hide the BTC- from the name since default prices are always shown in BTC.
    //Otherwise, we can display the full market name
    public String getName()
    {
        StringBuffer nameBuffer = new StringBuffer(MarketName);
        if(MarketName.startsWith("BTC-"))
        {
            return nameBuffer.substring(4, nameBuffer.length());
        }
        else return MarketName;
    }


    //Overridden for checking if currencies are apart of the Currencies ArrayList in main
    //I define equivalency as "The market name is the same"
    @Override
    public boolean equals(Object o) {
        Currency currency = (Currency) o;
        if (this == o) return true; //Are they the same object?
        if (o == null || getClass() != o.getClass()) return false; //Is o null or not even of this class?

        if(currency.MarketName.equals(MarketName))
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    //Overriden because people on StackOverflow said equals() and hashCode() are commonly overridden together.
    //This is a pruned down version of the automatically generated override of hashCode()
    @Override
    public int hashCode() {
        int result = MarketName != null ? MarketName.hashCode() : 0;
        return result;
    }


}

/*
----------------------------
COMPARATORS
----------------------------
*/

class CurrencyNameCompare implements Comparator<Currency>
{
    @Override
    public int compare(Currency lhs, Currency rhs) {

            //Normal sorting
            if (    !lhs.getName().startsWith("USDT-")
                    &&
                    !rhs.getName().startsWith("USDT-"))
            {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }

            //Compare BTC to USDT market (BTC comes first)
            else if (   !lhs.getName().startsWith("USDT-")
                        &&
                        rhs.getName().startsWith("USDT-"))
            {
                return -1;
            }

            //Compare USDT to BTC market (BTC comes first)
            else if (   lhs.getName().startsWith("USDT-")
                        &&
                        !rhs.getName().startsWith("USDT-"))
            {
                return 1;
            }

            //Compare USDT to USDT (normal sorting)
            else if (   lhs.getName().startsWith("USDT-")
                        &&
                        rhs.getName().startsWith("USDT-"))
            {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        return 0;
    }
}

class BackwardsCurrencyNameCompare implements Comparator<Currency>
{
    @Override
    public int compare(Currency lhs, Currency rhs) {

        //Normal sorting
        if (    !lhs.getName().startsWith("USDT-")
                &&
                !rhs.getName().startsWith("USDT-"))
        {
            return rhs.getName().compareToIgnoreCase(lhs.getName());
        }

        //Compare BTC to USDT market (BTC comes first)
        else if (   !lhs.getName().startsWith("USDT-")
                &&
                rhs.getName().startsWith("USDT-"))
        {
            return -1;
        }

        //Compare USDT to BTC market (BTC comes first)
        else if (   lhs.getName().startsWith("USDT-")
                &&
                !rhs.getName().startsWith("USDT-"))
        {
            return 1;
        }

        //Compare USDT to USDT (normal sorting)
        else if (   lhs.getName().startsWith("USDT-")
                &&
                rhs.getName().startsWith("USDT-"))
        {
            return rhs.getName().compareToIgnoreCase(lhs.getName());
        }
        return 0;
    }
}

//----------

class CurrencyPriceCompare implements Comparator<Currency>
{
    @Override
    public int compare(Currency lhs, Currency rhs) {

        //Normal sorting
        if (    !lhs.getName().startsWith("USDT-")
                &&
                !rhs.getName().startsWith("USDT-"))
        {
            return Float.compare(rhs.Last,lhs.Last);
        }

        //Compare BTC to USDT market (BTC comes first)
        else if (   !lhs.getName().startsWith("USDT-")
                    &&
                    rhs.getName().startsWith("USDT-"))
        {
            return -1;
        }

        //Compare USDT to BTC market (BTC comes first)
        else if (   lhs.getName().startsWith("USDT-")
                    &&
                    !rhs.getName().startsWith("USDT-"))
        {
            return 1;
        }

        //Compare USDT to USDT (normal sorting)
        else if (   lhs.getName().startsWith("USDT-")
                    &&
                    rhs.getName().startsWith("USDT-"))
        {
            return Float.compare(rhs.Last,lhs.Last);
        }


        return 0;
    }
}

class BackwardsCurrencyPriceCompare implements Comparator<Currency>
{
    @Override
    public int compare(Currency lhs, Currency rhs) {

        //Normal sorting
        if (    !lhs.getName().startsWith("USDT-")
                &&
                !rhs.getName().startsWith("USDT-"))
        {
            return Float.compare(lhs.Last,rhs.Last);
        }

        //Compare BTC to USDT market (BTC comes first)
        else if (   !lhs.getName().startsWith("USDT-")
                    &&
                    rhs.getName().startsWith("USDT-"))
        {
            return -1;
        }

        //Compare USDT to BTC market (BTC comes first)
        else if (   lhs.getName().startsWith("USDT-")
                    &&
                    !rhs.getName().startsWith("USDT-"))
        {
            return 1;
        }

        //Compare USDT to USDT (normal sorting)
        else if (   lhs.getName().startsWith("USDT-")
                    &&
                    rhs.getName().startsWith("USDT-"))
        {
            return Float.compare(lhs.Last,rhs.Last);
        }
        return 0;
    }
}

//----------

class CurrencyFavoriteCompare implements Comparator<Currency>
{
    @Override
    public int compare(Currency lhs, Currency rhs) {

        if (lhs.favorite && rhs.favorite)
        {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }

        else if (lhs.favorite && !rhs.favorite)
        {
            return -1;
        }

        else if (!lhs.favorite && rhs.favorite)
        {
            return 1;
        }

        else if (!lhs.favorite && !rhs.favorite)
        {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }

        return 0;
    }
}