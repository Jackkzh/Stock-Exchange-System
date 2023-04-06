//package src.main.java;
import java.sql.*;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;

public class Position {

    //private DBHandler db;
    private int positionID;
    private double amount;
    private String symbol_name;
    private int accountID;

    // for transaction
    public Position(double rhsamount, String rhssymbol_name, int rhsaccountID, boolean tf){
        //this.db = rhsdb;
        this.amount = rhsamount;
        this.symbol_name = rhssymbol_name;
        this.accountID = rhsaccountID;
    }

    public Position(double rhsamount, String rhssymbol_name, int rhsaccountID) throws IllegalArgumentException {

        if(rhsamount <= 0){
            throw new IllegalArgumentException("amount cannot be 0 or negative");
        }
        //this.db = rhsdb;
        this.amount = rhsamount;
        this.symbol_name = rhssymbol_name;
        this.accountID = rhsaccountID;
    }

    public int getID(){
        return this.positionID;
    }

    public double getAmount(){
        return this.amount;
    }

    static void findOldPositionDirectlyDB(int rhsaccountID, String rhssymbol_name) throws SQLException {
        String sql = "SELECT * FROM POSITION WHERE ACCOUNT_ID = " + rhsaccountID + 
        " AND SYMBOL_NAME = \'" + rhssymbol_name + "\';";
        ResultSet result = DBHandler.getInstance().commitAndReturn(sql);
        if(!result.next()){
            throw new SQLException("cannot find the existed position tuple");
        }
    }

    public ResultSet findOldPositionDB() throws SQLException {
        String sql = "SELECT * FROM POSITION WHERE ACCOUNT_ID = " + this.accountID + 
        " AND SYMBOL_NAME = \'" + this.symbol_name + "\';";
        ResultSet result = DBHandler.getInstance().commitAndReturn(sql);
        return result;
    }

    public void deletePositionDB() throws SQLException {
        String sql = "DELETE FROM POSITION WHERE ACCOUNT_ID = " + this.accountID +
        " AND SYMBOL_NAME = \'" + this.symbol_name + "\';";
        DBHandler.getInstance().commit(sql);
    }

    public void updateAmountofPositionDB(double newamount) throws SQLException {
        String sql = "UPDATE POSITION SET AMOUNT = " + newamount + " WHERE " +
        "ACCOUNT_ID = " + this.accountID +
        " AND SYMBOL_NAME = \'" + this.symbol_name + "\';";
        DBHandler.getInstance().commit(sql);
    }

    // 用的时候一定要注意上级method需要有个try catch
    public void createNewPositionDB() throws SQLException, IllegalArgumentException{

        ResultSet oldtuple = findOldPositionDB();
        if(!oldtuple.next()){
            // purely new
            String sql = "INSERT INTO POSITION" +
            "(AMOUNT, SYMBOL_NAME, ACCOUNT_ID) VALUES (" +
            this.amount + ", \'" +
            this.symbol_name + "\', " + this.accountID + ") RETURNING POSITION_ID;";
            ResultSet result = DBHandler.getInstance().commitAndReturn(sql);
            if(result.next()){
                this.positionID = result.getInt("POSITION_ID");
            }else{
                throw new SQLException("cannot add a new position tuple");
            }
        }else{
            // negative total amount or
            // <account id="123456" balance="1000"/>
            // <account id="123456" balance="1000"/>
            // it does twice leads to only one position
            // balance = 2000
            int oldpositionID = oldtuple.getInt("POSITION_ID");
            double oldamount = oldtuple.getDouble("AMOUNT");
            String oldsymbolNmae = oldtuple.getString("SYMBOL_NAME");
            int oldaccountID = oldtuple.getInt("ACCOUNT_ID");
            double newamount = this.amount + oldamount;
            
            if(newamount<0){
                throw new IllegalArgumentException("total amount cannot be negative");
            }else if(newamount==0){
                deletePositionDB();
            }else{
                updateAmountofPositionDB(newamount);
            }
            this.amount = this.amount + oldamount;

        }
        
    }
    
}
