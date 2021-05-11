package edu.csc472b.stockwatch;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class Stock implements Serializable, Comparable<Stock> {

    private String stockSymbol;
    private String lastPrice;
    private String companyName;
    private String priceChange;
    private String percentChange;


    Stock(String stockSymbol, String lastPrice, String companyName,
          String priceChange, String percentChange) {

        this.stockSymbol = stockSymbol;
        this.lastPrice = lastPrice;
        this.companyName = companyName;
        this.priceChange = priceChange;
        this.percentChange = percentChange;


    }

    public void setLastPrice(String lastPrice) {
        this.lastPrice = lastPrice;

    }

    public void setPriceChange(String priceChange) {
        this.priceChange = priceChange;

    }

    public void setPercentChange(String percentChange) {
        this.percentChange = percentChange;

    }

    public String getStockSymbol() {
        return stockSymbol;

    }

    public String getLastPrice() {
        return lastPrice;

    }

    public String getCompanyName() {
        return companyName;

    }

    public String getPriceChange() {
        return priceChange;

    }

    public String getPercentChange() {
        return percentChange;

    }

    @Override
    public int hashCode() {
        return Objects.hash(companyName, stockSymbol);
    }


    @Override
    public int compareTo(Stock stock) {

        return stockSymbol.compareTo(stock.getStockSymbol());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return companyName.equals(stock.companyName) &&
                stockSymbol.equals(stock.stockSymbol);
    }

    @NonNull
    @Override
    public String toString() {
        return companyName + " (" + stockSymbol + "), " + lastPrice + ", " + priceChange;
    }
}
