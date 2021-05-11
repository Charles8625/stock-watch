package edu.csc472b.stockwatch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener {

    public final static List<Stock> stockList = new ArrayList<>();
    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private StocksAdapter stocksAdapter;
    private int pos;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String stockSymbol;
    private List<Stock> tempStockList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        stocksAdapter = new StocksAdapter(stockList, this);
        recyclerView.setAdapter(stocksAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });


        // Connected to network?
        if (checkNetworkConnection()) {

            // Show Error Dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Content Cannot Be Added Without A Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();

            stockList.clear();

            // Put all stocks in display with zeros
            readJSONData();

            for (Stock s : stockList) {
                s.setLastPrice("0.00");
                s.setPriceChange("0.00");
                s.setPercentChange("(0.00%)");
            }

            // Sort Stock List
            Collections.sort(stockList);
            Log.d(TAG, "Lists: stockList: " + stockList);

            // Notify adapter
            stocksAdapter.notifyDataSetChanged();

        } else {

            // Execute NameDownloader
            NameDownloader nameDownloader = new NameDownloader();
            new Thread(nameDownloader).start();

            stockList.clear();
            loadFile();

            // For each stock in the tempStockList execute StockDownloader
            for (Stock s : tempStockList) {
                StockDownloader stockDownloader = new StockDownloader(this, s.getStockSymbol());
                new Thread(stockDownloader).start();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        writeJSONData();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_stock_watch, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.addStock) {
            makeStockDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadFile() {

        tempStockList.clear();

        try {
            InputStream is = getApplicationContext().
                    openFileInput(getString(R.string.file_name));

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // Create JSON Array from string file content
            JSONArray noteArr = new JSONArray(sb.toString());
            for (int i = 0; i < noteArr.length(); i++) {
                JSONObject nObj = noteArr.getJSONObject(i);

                String companyName = nObj.getString("companyName");
                String stockSymbol = nObj.getString("stockSymbol");
                String lastPrice = nObj.getString("lastPrice");
                String priceChange = nObj.getString("priceChange");
                String percentChange = nObj.getString("percentChange");

                Stock s = new Stock(stockSymbol, lastPrice, companyName,
                        priceChange, percentChange);

                tempStockList.add(s);
            }
            Log.d(TAG, "Lists: tempStockList: " + tempStockList.toString());

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onCreate: " + e);
        }

    }

    private void makeStockDialog() {
        if (checkNetworkConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Content Cannot Be Added Without A Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();

        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            final EditText sSymbolET = new EditText(this);
            sSymbolET.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            sSymbolET.setGravity(Gravity.CENTER_HORIZONTAL);
            builder.setView(sSymbolET);

            NameDownloader nameDownloader = new NameDownloader();
            new Thread(nameDownloader).start();

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {


                    stockSymbol = sSymbolET.getText().toString().trim();
                    final ArrayList<String> results = NameDownloader.findMatches(stockSymbol);

                    if (results.size() == 0) {
                        doNoAnswer(stockSymbol);
                    } else if (results.size() == 1) {
                        String symbol = results.get(0);
                        String[] onlySymbolOne = symbol.split(" - ");
                        doSelection(onlySymbolOne[1]);

                    } else {

                        String[] array = results.toArray(new String[0]);

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Make a selection");
                        builder.setItems(array, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {


                                String symbol = results.get(which);
                                Log.d(TAG, "onClick: symbol: " + symbol);

                                String[] onlySymbol = symbol.split(" - ");
                                Log.d(TAG, "onClick: " + onlySymbol[onlySymbol.length - 1]);
                                doSelection(onlySymbol[onlySymbol.length - 1]);


                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog2 = builder.create();
                        dialog2.show();
                    }

                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            builder.setTitle("Stock Selection");
            builder.setMessage("Please enter a stock symbol:");

            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    private boolean checkNetworkConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo == null || !netInfo.isConnectedOrConnecting();
    }

    private void doRefresh() {

        if (checkNetworkConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Content Cannot Be Added Without A Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        } else {

            stockList.clear();
            loadFile();

            for (Stock s : tempStockList) {
                StockDownloader stockDownloader = new StockDownloader(this, s.getStockSymbol());
                new Thread(stockDownloader).start();
            }
        }
        stocksAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void doNoAnswer(String symbol) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("No data for specified symbol/name");
        builder.setTitle("No Data Found: " + symbol);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void doSelection(String stockSymbol) {

        StockDownloader stockDownloader = new StockDownloader(this, stockSymbol);
        new Thread(stockDownloader).start();
    }

    @Override
    public void onClick(View view) {

        int pos = recyclerView.getChildLayoutPosition(view);
        Stock s = stockList.get(pos);

        String symbol = s.getStockSymbol();

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.marketwatch.com/investing/stock/" + symbol));
        startActivity(intent);

    }

    @Override
    public boolean onLongClick(View view) {
        pos = recyclerView.getChildLayoutPosition(view);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (!stockList.isEmpty()) {
                    stockList.remove(pos);
                    writeJSONData();
                    stocksAdapter.notifyDataSetChanged();
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.setTitle("Delete this Stock?");
        AlertDialog dialog = builder.create();
        dialog.show();
        return false;
    }

    public void addStock(Stock stock) {

        if (stock == null) {
            badDataAlert(stockSymbol);
            return;
        }

        for (Stock s : stockList) {
            if (s.equals(stock)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setMessage(stock.getCompanyName() + " is already displayed");
                builder.setTitle("Duplicate Stock");
                builder.setIcon(R.drawable.baseline_error_black_36);

                AlertDialog dialog = builder.create();
                dialog.show();
                return;
            }
        }

        stockList.add(stock);
        Collections.sort(stockList);
        writeJSONData();
        stocksAdapter.notifyDataSetChanged();
    }

    private void badDataAlert(String sym) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("No data for selection");
        builder.setTitle("Symbol Not Found: " + sym);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void writeJSONData() {
        try {
            FileOutputStream fos = getApplicationContext().
                    openFileOutput(getString(R.string.file_name), Context.MODE_PRIVATE);

            JsonWriter writer = new JsonWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
            writer.setIndent("  ");
            writer.beginArray();
            for (Stock s : stockList) {
                writer.beginObject();
                writer.name("companyName").value(s.getCompanyName());
                writer.name("stockSymbol").value(s.getStockSymbol());
                writer.name("lastPrice").value(s.getLastPrice());
                writer.name("priceChange").value(s.getPriceChange());
                writer.name("percentChange").value(s.getPercentChange());

                writer.endObject();
            }
            writer.endArray();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "writeJSONData: " + e.getMessage());
        }
    }

    private void readJSONData() {
        try {
            FileInputStream fis = getApplicationContext().
                    openFileInput(getString(R.string.file_name));

            byte[] data = new byte[fis.available()];
            fis.close();
            String json = new String(data);

            JSONArray noteArr = new JSONArray(json);
            for (int i = 0; i < noteArr.length(); i++) {
                JSONObject nObj = noteArr.getJSONObject(i);

                String companyName = nObj.getString("companyName");
                String stockSymbol = nObj.getString("stockSymbol");
                String lastPrice = nObj.getString("lastPrice");
                String priceChange = nObj.getString("priceChange");
                String percentChange = nObj.getString("percentChange");

                Stock s = new Stock(stockSymbol, lastPrice, companyName,
                        priceChange, percentChange);
                stockList.add(s);
            }
            stocksAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "readJSONData: " + e.getMessage());
        }
    }
}
