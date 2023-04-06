//package src.main.java;
import java.sql.*;
import java.util.*;

public class MyOrder {

//    private DBHandler db;
    private int orderID;
    private double amountPurchase;
    private double limitPrice;
    private String status;
    private Timestamp createdTime;
    private String symbolName;
    private int accountID;
    private int transID;

    public MyOrder(int rhsorderID, double rhsamountPurchase, double rhslimitPrice, String rhsstatus, Timestamp rhscreatedTime, String rhssymbolName, int rhsaccountID, int rhstransID){

        //this.db = rhsdb;
        this.orderID = rhsorderID;
        this.amountPurchase = rhsamountPurchase;
        this.limitPrice = rhslimitPrice;
        this.status = rhsstatus;
        this.createdTime = rhscreatedTime;
        this.symbolName = rhssymbolName;
        this.accountID = rhsaccountID;
        this.transID = rhstransID;
    }

    public MyOrder(double rhsamountPurchase, double rhslimitPrice, String rhsstatus, Timestamp rhscreatedTime, String rhssymbolName, int rhsaccountID, int rhstransID) throws IllegalArgumentException {

        if(rhssymbolName == "" || rhssymbolName == null){
            throw new IllegalArgumentException("error symbol name");
        }
        if(rhsamountPurchase == 0){
            throw new IllegalArgumentException("amount purchase cannot be 0");
        }

        if(rhslimitPrice <= 0){
            throw new IllegalArgumentException("limit price cannot be 0 or negative");
        }
//        this.db = rhsdb;
        this.amountPurchase = rhsamountPurchase;
        this.limitPrice = rhslimitPrice;
        this.status = rhsstatus;
        this.createdTime = rhscreatedTime;
        this.symbolName = rhssymbolName;
        this.accountID = rhsaccountID;
        this.transID = rhstransID;
    }

    public Timestamp getCreatedTime(){
        return this.createdTime;
    }
    // 用的时候一定要注意上级method需要有个try catch
    // order tag

    /**
     * openOrder for Transactions
     * @throws SQLException
     */
    public void createMyOrderDB() throws SQLException{
        String sql = "INSERT INTO MYORDER" +
        "(AMOUNT_PURCHASE, LIMIT_PRICE, STATUS, CREATED_TIME, " +
        "SYMBOL_NAME, ACCOUNT_ID, TRANS_ID) VALUES (" +
        this.amountPurchase + ", " +
        this.limitPrice + ", \'" +
        this.status + "\', \'" +
        this.createdTime + "\', \'" +
        this.symbolName + "\', " +
        this.accountID + ", " +
        this.transID + ") RETURNING ORDER_ID;";
        ResultSet result = DBHandler.getInstance().commitAndReturn(sql);
        if(result.next()){
            this.orderID = result.getInt("ORDER_ID");
        }else{
            // 改 暂时用SQL
            throw new SQLException("cannot add a new order tuple");
        }
    }

    public int getID(){
        return this.orderID;
    }

    public double getLimitprice(){
        return this.limitPrice;
    }

    public String getSymbolname(){
        return this.symbolName;
    }

    public DBHandler getDB(){
        return DBHandler.getInstance();
    }

    public int getAccountID(){
        return this.accountID;
    }

    public double getamountPurchase(){
        return this.amountPurchase;
    }

    public int gettransID(){
        return this.transID;
    }

    public String getstatus(){
        return this.status;
    }

    public void symbolUpdateStatusDB(String rhsstatus) throws SQLException {
        String sql = "UPDATE MYORDER SET STATUS = \'" + rhsstatus + "\' WHERE " +
        "ORDER_ID = " + this.orderID + ";";
        DBHandler.getInstance().commit(sql);
    }

    public void amountUpdateStatusDB(double amount) throws SQLException {
        String sql = "UPDATE MYORDER SET AMOUNT_PURCHASE = " + amount + " WHERE " +
        "ORDER_ID = " + this.orderID + ";";
        DBHandler.getInstance().commit(sql);
    }

    public void findnewestOrder(int findorderID) throws SQLException {

        String sql = "SELECT * FROM MYORDER WHERE ORDER_ID = " + 
        findorderID + ";";
        ResultSet result = DBHandler.getInstance().commitAndReturn(sql);

        if(result.next()){

            this.amountPurchase = result.getDouble("AMOUNT_PURCHASE");
            this.limitPrice = result.getDouble("LIMIT_PRICE");
            this.status = result.getString("STATUS");
            this.createdTime = result.getTimestamp("CREATED_TIME");
            this.symbolName = result.getString("SYMBOL_NAME");
            this.accountID = result.getInt("ACCOUNT_ID");
            this.transID = result.getInt("TRANS_ID");

        }else{
            throw new SQLException("Cannot find this order");
        }

    }

    public void deleteOrder(int findorderID) throws SQLException {

        String sql = "DELETE FROM MYORDER WHERE ORDER_ID = " + findorderID + ";";
        DBHandler.getInstance().commit(sql);

    }
    
}
