//package src.main.java;
import java.sql.*;
import java.sql.Statement;
import java.time.Instant;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBHandler {

    private Connection c;
    public void createDBHandler(){
        this.c = null;
        try{
            Class.forName("org.postgresql.Driver");
            // do it remote?
            this.c = DriverManager
                .getConnection("jdbc:postgresql://localhost:5432/stockexchange","postgres","passw0rd");

            System.out.println("Open db successfully");

            // 只用于测试
            dropAllTable();
            commitAllTable();
            System.out.println("Create tables successfully");
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Something goes wrong while trying to connect to the db");
            return;
        }

    }

    public synchronized void commitAllTable() throws SQLException{

        String sqlAccount = "CREATE TABLE IF NOT EXISTS ACCOUNT" +
        "(ACCOUNT_ID INT PRIMARY KEY NOT NULL UNIQUE CHECK (ACCOUNT_ID >= 0),CURRENT_BALANCE FLOAT CHECK (CURRENT_BALANCE >= 0));";

        String sqlPosition = "CREATE TABLE IF NOT EXISTS POSITION" +
        "(POSITION_ID SERIAL PRIMARY KEY NOT NULL,AMOUNT FLOAT NOT NULL CHECK (AMOUNT > 0)," +
        "SYMBOL_NAME VARCHAR(256),ACCOUNT_ID INT," +
        "FOREIGN KEY (ACCOUNT_ID) REFERENCES ACCOUNT(ACCOUNT_ID) ON DELETE SET NULL ON UPDATE CASCADE);";

        String sqlMyTransaction = "CREATE TABLE IF NOT EXISTS TRANSACTION" +
        "(TRANS_ID SERIAL PRIMARY KEY NOT NULL,ACCOUNT_ID INT," +
        "FOREIGN KEY (ACCOUNT_ID) REFERENCES ACCOUNT(ACCOUNT_ID) ON DELETE SET NULL ON UPDATE CASCADE);";

        String sqlMyOrder = "CREATE TYPE STATUSTYPE AS ENUM('OPEN','EXECUTED','CANCELED');" +
        "CREATE TABLE IF NOT EXISTS MYORDER" +
        "(ORDER_ID SERIAL PRIMARY KEY NOT NULL,AMOUNT_PURCHASE FLOAT CHECK (AMOUNT_PURCHASE <> 0),LIMIT_PRICE FLOAT CHECK (LIMIT_PRICE > 0)," +
        "STATUS STATUSTYPE NOT NULL," +
        "CREATED_TIME TIMESTAMP NOT NULL," +
        "SYMBOL_NAME VARCHAR(256)," +
        "ACCOUNT_ID INT," + 
        "TRANS_ID INT," +
        "FOREIGN KEY (ACCOUNT_ID) REFERENCES ACCOUNT(ACCOUNT_ID) ON DELETE SET NULL ON UPDATE CASCADE," +
        "FOREIGN KEY (TRANS_ID) REFERENCES TRANSACTION(TRANS_ID) ON DELETE SET NULL ON UPDATE CASCADE);";

        commit(sqlAccount);
        commit(sqlPosition);
        commit(sqlMyTransaction);
        commit(sqlMyOrder);

    }

    public synchronized void dropAllTable() throws SQLException{
        String sqlDrop = "DROP TABLE IF EXISTS MYORDER CASCADE;" +
        "DROP TABLE IF EXISTS TRANSACTION CASCADE;"+
        "DROP TABLE IF EXISTS POSITION CASCADE;" +
        "DROP TABLE IF EXISTS ACCOUNT CASCADE;" +
        "DROP TYPE IF EXISTS STATUSTYPE;";
        commit(sqlDrop);
        System.out.println("Drop tables successfully");
    }
    
    public synchronized void commit(String sql) throws SQLException{

        Statement stmt = null;
        stmt = this.c.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();

    }
  
    public synchronized ResultSet commitAndReturn(String sql) throws SQLException{

        Statement stmt = null;
        stmt = this.c.createStatement();
        ResultSet result = stmt.executeQuery(sql);
        return result;

    }

    public Connection getC(){
        return this.c;
    }

    // 只用于测试db 过会删掉
    /* 
    public static void main(String[] args){
        DBHandler db = new DBHandler();
        db.createDBHandler();
    }
    */

}