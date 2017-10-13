package kentonhanifl.CryptoGraph;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetFeed extends AsyncTask<URL, Integer, StringBuffer>{

    public AsyncResponse response = null;

    public GetFeed(AsyncResponse asyncResponse)
    {
        response = asyncResponse;
    }

    @Override
    protected StringBuffer doInBackground(URL ... urls) {
        //Get JSON objects
        //We just put the entire HTTP response into a StringBuffer
        HttpURLConnection connection = null;
        StringBuffer response = null;

        try {
            //We only want the first URL. No use for the other ones, but the override MAKES us have a list for the parameter on doInBackground()
            URL url= urls[0];
            connection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                        connection.getInputStream()));
            response = new StringBuffer();
            response.append(in.readLine());
            return response;
            //------------Exceptions are not fully understood. Will get back to it...
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response;
    }

    protected void onPostExecute(StringBuffer jsonStringBuffer)
    {
        response.processFinish(jsonStringBuffer);
    }

}

