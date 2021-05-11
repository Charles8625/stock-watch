package edu.csc472b.stockwatch;

import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class StockDownloader implements Runnable {

    private static final String TAG = "StockDownloader";

    private final String stockRunnable;
    private MainActivity mainActivity;

    StockDownloader(MainActivity mainActivity, String stockSymbol) {

        this.mainActivity = mainActivity;
        stockRunnable = "https://cloud.iexapis.com/stable/stock/" + stockSymbol
                + "/quote?token=sk_6b2b1c50bf9f41b7a83bfa6ed7e0479f";
    }

    @Override
    public void run() {

        Uri dataUri = Uri.parse(stockRunnable);
        String urlToUse = dataUri.toString();

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
                return;
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }


        } catch (Exception e) {
            return;
        }

        Log.d(TAG, "run: " + sb.toString());
        process(sb.toString());

    }

    private void process(String s) {
        try {

            JSONObject jStock = new JSONObject(s);
            Log.d(TAG, "process: jStock " + jStock.toString());

            String companyName = jStock.getString("companyName");
            String stockSymbol = jStock.getString("symbol");

            String lastPrice = jStock.getString("latestPrice");
            if (lastPrice.equals("null")) lastPrice = "0.00";
            float fLastPrice = Float.parseFloat(lastPrice);
            lastPrice = String.format(Locale.getDefault(), "%,.2f", fLastPrice);

            String priceChange = jStock.getString("change");
            if (priceChange.equals("null")) priceChange = "0.00";
            float fPriceChange = Float.parseFloat(priceChange);
            priceChange = String.format(Locale.getDefault(), "%,.2f", fPriceChange);

            String percentChange = jStock.getString("changePercent");
            if (percentChange.equals("null")) percentChange = "0.00";
            float fPercentChange = 100 * Float.parseFloat(percentChange);
            percentChange = String.format(Locale.getDefault(), "%,.2f", fPercentChange);
            percentChange = "(" + percentChange + "%)";

            final Stock stock = new Stock(stockSymbol, lastPrice, companyName, priceChange, percentChange);

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.addStock(stock);
                    Log.d(TAG, "run: adding Stock from swipeRefresh");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "process: Exception " + e);
        }

    }

}
