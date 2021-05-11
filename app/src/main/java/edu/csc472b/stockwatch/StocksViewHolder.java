package edu.csc472b.stockwatch;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StocksViewHolder extends RecyclerView.ViewHolder {

    TextView stockSymbol;
    TextView lastPrice;
    TextView companyName;
    TextView priceChange;
    TextView percentChange;
    TextView arrowView;

    public StocksViewHolder(@NonNull View itemView) {
        super(itemView);
        stockSymbol = itemView.findViewById(R.id.stockSymbol);
        lastPrice = itemView.findViewById(R.id.lastPrice);
        companyName = itemView.findViewById(R.id.companyName);
        priceChange = itemView.findViewById(R.id.priceChange);
        percentChange = itemView.findViewById(R.id.percentChange);
        arrowView = itemView.findViewById(R.id.arrowView);
    }
}
