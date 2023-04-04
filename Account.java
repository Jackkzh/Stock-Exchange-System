import java.sql.*;
import java.sql.Statement;
import java.time.Instant;
import java.sql.Connection;
import java.sql.DriverManager;

public class Account {

    private DBHandler db;
    private int accountID;
    private double current_balance;

    // 用的时候一定要注意上级method需要有个try catch
    public void createNewAccountDB() throws SQLException{
        String sql = "INSERT INTO ACCOUNT" +
        "(ACCOUNT_ID, CURRENT_BALANCE) VALUES (" +
        this.accountID + ", " + this.current_balance + ");";
        this.db.commit(sql);
    }

    // can check if we already have this account
    // update data in case there are some other thread change it
    public void checkExistedAccountandRefreshDB(DBHandler rhsdb, int rhsaccountID) throws SQLException {

        String sql = "SELECT * FROM ACCOUNT WHERE ACCOUNT_ID = "+
        rhsaccountID + ";";
        ResultSet result = rhsdb.commitAndReturn(sql);
        if(!result.next()){
            throw new SQLException("cannot find this account in db");
        }

        // keep it the newest
        this.db = rhsdb;
        this.accountID = result.getInt("ACCOUNT_ID");
        this.current_balance = result.getDouble("CURRENT_BALANCE");

    }
    
    // inside create tag
    // add a new account so do not have to check if there already has one
    // if already has one, error
    public Account(DBHandler rhsdb, int rhsaccountID, double rhscurrent_balance){
        this.db = rhsdb;
        this.accountID = rhsaccountID;
        this.current_balance = rhscurrent_balance; 
    }

    // inside transaction tag
    // have to check if there already has one
    // if not exist, error
    public Account(DBHandler rhsdb, int rhsaccountID) throws SQLException {
        checkExistedAccountandRefreshDB(rhsdb, rhsaccountID);
    }

    public void updateBalanceDB(double money) throws SQLException {

        String sql = "UPDATE ACCOUNT SET CURRENT_BALANCE = " + money + " WHERE " +
        "ACCOUNT_ID = " + this.accountID + ";";
        this.db.commit(sql);
    }

    public double findAmountofSymbolDB(String rhssymbolName) throws SQLException {

        String sql = "SELECT AMOUNT FROM POSITION WHERE ACCOUNT_ID = "+
        this.accountID + " AND SYMBOL_NAME = \'" + rhssymbolName + "\';";
        ResultSet result = this.db.commitAndReturn(sql);
        if(!result.next()){
            throw new SQLException("cannot find the related position");
        }
        return result.getDouble("AMOUNT");

    }

    // check order is ok
    // create Transaction transaction = new transaction(account.db, account.accountID)
    // create open order and pass it to following steps
    // step1 create open order
    // step2 match open order
    // step3 execute order
    // order sym="SYM" amount="AMT" limit="LMT" 
    // amount > 0 buy or < 0 sell
    // MyOrder(DBHandler rhsdb, double rhsamountPurchase, double rhslimitPrice, String rhsstatus,
    // Timestamp rhscreatedTime, String rhssymbolName, int rhsaccountID, int rhstransID)
    public int createOrderStep1(String rhssymbolName, double rhsamountPurchase, double rhslimitPrice) throws SQLException {

        int originalorderid = 0;
        boolean tf = false;

        if(rhsamountPurchase==0){
            throw new SQLException("error amount purchase");
        }
        if(rhslimitPrice<=0){
            throw new SQLException("error limit price");
        }
        // a buyer order
        if(rhsamountPurchase>0){
            double moneyhavetopay = rhsamountPurchase * rhslimitPrice;

            if(moneyhavetopay <= this.current_balance){
                this.current_balance = this.current_balance - moneyhavetopay;
                // update balance from account first
                updateBalanceDB(this.current_balance);
                // crate new transaction
                Transaction transaction = new Transaction(this.db, this.accountID);
                // update new transaction to db
                transaction.createTransactionDB();
                // create new order
                MyOrder myorder = new MyOrder(this.db, rhsamountPurchase, rhslimitPrice, "OPEN", Timestamp.from(Instant.now()), rhssymbolName, this.accountID, transaction.getID());
                // update new order to db
                myorder.createMyOrderDB();
                
                originalorderid = myorder.getID();
                tf = true;

                // 看这里！！！！！！！match还没加！！！！！

            }else{
                throw new SQLException("do have enough money to buy a position");
            }

        }else{
            // a seller order
            double moneyhavetoincrease = -1 * rhsamountPurchase * rhslimitPrice;
            double amountofSymbol = findAmountofSymbolDB(rhssymbolName);
            if(rhsamountPurchase <= amountofSymbol){
                this.current_balance = this.current_balance + moneyhavetoincrease;

                // update balance from account first
                updateBalanceDB(this.current_balance);
                // delete or update position related
                Position.findOldPositionDirectlyDB(this.db, this.accountID, rhssymbolName);
                Position position = new Position(this.db, rhsamountPurchase, rhssymbolName, this.accountID);
                // update new position to db
                position.createNewPositionDB();

                // crate new transaction
                Transaction transaction = new Transaction(this.db, this.accountID);
                // update new transaction to db
                transaction.createTransactionDB();
                // create new order
                MyOrder myorder = new MyOrder(this.db, rhsamountPurchase, rhslimitPrice, "OPEN", Timestamp.from(Instant.now()), rhssymbolName, this.accountID, transaction.getID());
                // update new order to db
                myorder.createMyOrderDB();
                
                originalorderid = myorder.getID();
                tf = true;

                // 看这里！！！！！！！match还没加！！！！！

            }else{
                throw new SQLException("do have enough money to buy a position");
            }
        }

        if(!tf){
            throw new SQLException("cannot create original buyer or seller order");
        }
        return originalorderid;
    }


    // 只用于测试db 过会删掉
    public static void main(String[] args){

        try{
            DBHandler db = new DBHandler();
            db.createDBHandler();

            Account account = new Account(db,12345,20000);
            account.createNewAccountDB();

            Position position = new Position(db, 80, "CCC", 12345);
            position.createNewPositionDB();

            int orderID = account.createOrderStep1("SYM", 100, 145.67);
            int orderID2 = account.createOrderStep1("CCC", -50, 88.6);
            System.out.println("the original order id for buyer is: " + orderID);
            System.out.println("the original order id for seller is: " + orderID2);

        }catch(Exception e){
            e.printStackTrace();
            System.out.println("something goes wrong with the account");
        }
    }
    
}
