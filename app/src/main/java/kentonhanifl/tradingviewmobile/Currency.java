package kentonhanifl.tradingviewmobile;

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

