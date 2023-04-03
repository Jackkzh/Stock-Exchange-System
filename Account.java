import java.sql.*;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;

public class Account {

    private DBHandler db;
    private int accountID;
    private double current_balance;
    public Account(DBHandler rhsdb, int rhsaccountID, double rhscurrent_balance){
        this.db = rhsdb;
        this.accountID = rhsaccountID;
        this.current_balance = rhscurrent_balance; 
    }

    // 用的时候一定要注意上级method需要有个try catch
    public void createNewAccountDB() throws SQLException{
        String sql = "INSERT INTO ACCOUNT" +
        "(ACCOUNT_ID, CURRENT_BALANCE) VALUES (" +
        this.accountID + ", " + this.current_balance + ");";
        this.db.commit(sql);
    }

    // 只用于测试db 过会删掉
    public static void main(String[] args){

        try{
            DBHandler db = new DBHandler();
            db.createDBHandler();
            Account account = new Account(db,12345,1000);
            account.createNewAccountDB();

            Position position = new Position(db, 10, "SYM", 12345);
            position.createNewPositionDB();
            Position position2 = new Position(db, 3000, "SYM", 12345);
            position2.createNewPositionDB();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("something goes wrong with the account");
        }
    }
    
}
