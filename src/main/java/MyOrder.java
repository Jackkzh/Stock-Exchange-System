package src.main.java;
import java.sql.*;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;

public class MyOrder {

    private DBHandler db;
    private int orderID;
    private double amountPurchase;
    private double limitPrice;
    private String status;
    private Timestamp createdTime;
    private String symbolName;
    private int accountID;
    private int transID;

    public MyOrder(DBHandler rhsdb, double rhsamountPurchase, double rhslimitPrice, String rhsstatus, Timestamp rhscreatedTime, String rhssymbolName, int rhsaccountID, int rhstransID) throws IllegalArgumentException {

        if(rhsamountPurchase == 0){
            throw new IllegalArgumentException("amount purchase cannot be 0");
        }

        if(rhslimitPrice <= 0){
            throw new IllegalArgumentException("limit price cannot be 0 or negative");
        }
        this.db = rhsdb;
        this.amountPurchase = rhsamountPurchase;
        this.limitPrice = rhslimitPrice;
        this.status = rhsstatus;
        this.createdTime = rhscreatedTime;
        this.symbolName = rhssymbolName;
        this.accountID = rhsaccountID;
        this.transID = rhstransID;
    }

    // 用的时候一定要注意上级method需要有个try catch
    // order tag
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
        ResultSet result = this.db.commitAndReturn(sql);
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

    // query tag
    // cancel tag
    
}
