package edu.csc472b.stockwatch;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StocksAdapter extends RecyclerView.Adapter<StocksViewHolder> {

    private List<Stock> stockList;
    private MainActivity mainActivity;

    StocksAdapter(List<Stock> sList, MainActivity ma) {
        stockList = sList;
        mainActivity = ma;
    }


    @NonNull
    @Override
    public StocksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_row, parent, false);

        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);

        return new StocksViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StocksViewHolder holder, int position) {

        Stock stock = stockList.get(position);

        String negStock = holder.arrowView.getResources().getString(R.string.negative);
        String posStock = holder.arrowView.getResources().getString(R.string.positive);
        float price = Float.parseFloat(stock.getPriceChange());

        holder.stockSymbol.setText(stock.getStockSymbol());
        holder.percentChange.setText(stock.getPercentChange());
        holder.priceChange.setText(stock.getPriceChange());
        holder.companyName.setText(stock.getCompanyName());
        holder.lastPrice.setText(stock.getLastPrice());
        holder.itemView.setLongClickable(true);

        if (price < 0) {

            holder.arrowView.setText(negStock);
            holder.stockSymbol.setTextColor(Color.RED);
            holder.percentChange.setTextColor(Color.RED);
            holder.priceChange.setTextColor(Color.RED);
            holder.companyName.setTextColor(Color.RED);
            holder.lastPrice.setTextColor(Color.RED);
            holder.arrowView.setTextColor(Color.RED);

        } else {

            holder.arrowView.setText(posStock);
            holder.stockSymbol.setTextColor(Color.GREEN);
            holder.percentChange.setTextColor(Color.GREEN);
            holder.priceChange.setTextColor(Color.GREEN);
            holder.companyName.setTextColor(Color.GREEN);
            holder.lastPrice.setTextColor(Color.GREEN);
            holder.arrowView.setTextColor(Color.GREEN);
        }
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
