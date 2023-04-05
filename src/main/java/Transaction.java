package src.main.java;
import java.sql.*;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;

public class Transaction {
    
    private DBHandler db;
    private int transactionID;
    private int accountID;

    public Transaction(DBHandler rhsdb, int rhsaccountID){
        this.db = rhsdb;
        this.accountID = rhsaccountID;
    }

    // 用的时候一定要注意上级method需要有个try catch
    public void createTransactionDB() throws SQLException{
        String sql = "INSERT INTO TRANSACTION" +
        "(ACCOUNT_ID) VALUES (" +
        this.accountID + ") RETURNING TRANS_ID;";
        ResultSet result = this.db.commitAndReturn(sql);
        if(result.next()){
            this.transactionID = result.getInt("TRANS_ID");
        }else{
            throw new SQLException("cannot add a new transaction tuple");
        }
    }

    public int getID(){
        return this.transactionID;
    }
}
