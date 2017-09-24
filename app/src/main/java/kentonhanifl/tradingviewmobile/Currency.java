package kentonhanifl.tradingviewmobile;




public class Currency
{
    Currency()
    {
        favorite = false;
        MarketName="";
        Last = 0;
    }
    public String MarketName;
    public float Last;
    boolean favorite;

    //Just want to hide the BTC- from the name since default prices are always shown in BTC.
    public String getName()
    {
        StringBuffer nameBuffer = new StringBuffer(MarketName);
        if(MarketName.startsWith("BTC-"))
        {
            return nameBuffer.substring(4, nameBuffer.length());
        }
        else return MarketName;
    }

    @Override
    public boolean equals(Object o) {
        Currency currency = (Currency) o;
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if(currency.MarketName.equals(MarketName))
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    @Override
    public int hashCode() {
        int result = MarketName != null ? MarketName.hashCode() : 0;
        return result;
    }

}

