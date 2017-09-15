package kentonhanifl.tradingviewmobile;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Main extends AppCompatActivity {
int a = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AsyncTask<URL, Integer, StringBuffer> Markets = new GetFeed().execute();
        //TextView text = (TextView) findViewById(R.id.textView2);

        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(a==0) {
                    AsyncTask<URL, Integer, StringBuffer> Markets = new GetFeed().execute();

                }
                setText(Integer.toString(a));
                a++;
            }
        });
    }

    public void setText(String s) {
        TextView text = (TextView) findViewById(R.id.textView2);
        text.setText(s);
    }




    class GetFeed extends AsyncTask<URL, Integer, StringBuffer> {
        @Override
        protected StringBuffer doInBackground(URL... urls) {
            HttpURLConnection connection = null;
            StringBuffer response = null;
            try {

                URL url = new URL("https://bittrex.com/api/v1.1/public/getmarketsummaries");
                connection = (HttpURLConnection) url.openConnection();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));
                response = new StringBuffer();
                response.append(in.readLine());
                Log.d("H", response.toString());
                return response;

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

        protected void onPostExecute(StringBuffer r) {
            //TextView text = (TextView) findViewById(R.id.textView2);
            String s = r.substring(0, 30);
            //text.setText(s);
            setText(s);
            a=0;
        }
    }
}


