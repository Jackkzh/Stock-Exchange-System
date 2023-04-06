//package src.main.java;

import java.sql.*;
import java.time.Instant;
import java.util.*;

public class Account {

//    private DBHandler db;
    private int accountID;
    private double current_balance;

    // 用的时候一定要注意上级method需要有个try catch
    public void createNewAccountDB() throws SQLException {

        String sql = "INSERT INTO ACCOUNT" +
                "(ACCOUNT_ID, CURRENT_BALANCE) VALUES (" +
                this.accountID + ", " + this.current_balance + ");";
        DBHandler.getInstance().commit(sql);

    }

    // can check if we already have this account
    // update data in case there are some other thread change it
    public void checkExistedAccountandRefreshDB(int rhsaccountID) throws SQLException {

        String sql = "SELECT * FROM ACCOUNT WHERE ACCOUNT_ID = "+
        rhsaccountID + ";";
        ResultSet result = DBHandler.getInstance().commitAndReturn(sql);
        if(!result.next()){
            throw new SQLException("cannot find this account in db");
        }

        // keep it the newest
        //this.db = rhsdb;
        this.accountID = result.getInt("ACCOUNT_ID");
        this.current_balance = result.getDouble("CURRENT_BALANCE");

    }
    
    // inside create tag
    // add a new account so do not have to check if there already has one
    // if already has one, error
    public Account(int rhsaccountID, double rhscurrent_balance) throws IllegalArgumentException {
        if(rhscurrent_balance<0){
            throw new IllegalArgumentException("current balance cannot be negative");
        }
        //DBHandler.getInstance() = rhsdb;
        this.accountID = rhsaccountID;
        this.current_balance = rhscurrent_balance; 
    }

    // inside transaction tag
    // have to check if there already has one
    // if not exist, error
    public Account(int rhsaccountID) throws SQLException {
        checkExistedAccountandRefreshDB(rhsaccountID);
    }

    public void updateBalanceDB(int accountID, double money) throws SQLException {

        String sql = "UPDATE ACCOUNT SET CURRENT_BALANCE = " + money + " WHERE " +
        "ACCOUNT_ID = " + accountID + ";";
        DBHandler.getInstance().commit(sql);
    }

    public double findAmountofSymbolDB(String rhssymbolName) throws SQLException {

        String sql = "SELECT AMOUNT FROM POSITION WHERE ACCOUNT_ID = "+
        this.accountID + " AND SYMBOL_NAME = \'" + rhssymbolName + "\';";
        ResultSet result = DBHandler.getInstance().commitAndReturn(sql);
        if(!result.next()){
            throw new SQLException("cannot find the related position");
        }
        return result.getDouble("AMOUNT");

    }

    public double findBalanceByOrder(int rhsaccountID) throws SQLException {
        String sql = "SELECT CURRENT_BALANCE FROM ACCOUNT WHERE ACCOUNT_ID = " + rhsaccountID + ";";
        ResultSet result = DBHandler.getInstance().commitAndReturn(sql);
        if(!result.next()){
            throw new SQLException("cannot find the related current balance");
        }
        return result.getDouble("CURRENT_BALANCE");
    }

    // find matched sellers
    public ArrayList<MyOrder> priorityListofmatchedSeller(MyOrder buyerorder) throws SQLException {
        ArrayList<MyOrder> matchedSellers = new ArrayList<>();

        String sql = "SELECT * FROM MYORDER WHERE AMOUNT_PURCHASE < 0 AND LIMIT_PRICE <= " + 
        buyerorder.getLimitprice() + " AND STATUS = \'OPEN\' AND SYMBOL_NAME = \'" + buyerorder.getSymbolname() + "\' " +
        "AND ACCOUNT_ID <> " + buyerorder.getAccountID() +
        " ORDER BY LIMIT_PRICE ASC, CREATED_TIME ASC;";
        ResultSet result = DBHandler.getInstance().commitAndReturn(sql);

        while(result.next()){

            int temporderID = result.getInt("ORDER_ID");
            double temoamountPurchase = result.getDouble("AMOUNT_PURCHASE");
            double templimitPrice = result.getDouble("LIMIT_PRICE");
            String tempstatus = result.getString("STATUS");
            Timestamp tempcreatedTime = result.getTimestamp("CREATED_TIME");
            String tempsymbolName = result.getString("SYMBOL_NAME");
            int tempaccountID = result.getInt("ACCOUNT_ID");
            int temptransID = result.getInt("TRANS_ID");
            MyOrder temporder = new MyOrder(temporderID, temoamountPurchase, templimitPrice, tempstatus, tempcreatedTime, tempsymbolName, tempaccountID, temptransID);
            matchedSellers.add(temporder);

        }

        //System.out.println("the size is: "+matchedSellers.size());
        return matchedSellers;

    }

    // find matched buyers
    public ArrayList<MyOrder> priorityListofmatchedBuyer(MyOrder sellerorder) throws SQLException {

        ArrayList<MyOrder> matchedBuyers = new ArrayList<>();

        String sql = "SELECT * FROM MYORDER WHERE AMOUNT_PURCHASE > 0 AND LIMIT_PRICE >= " + 
        sellerorder.getLimitprice() + " AND STATUS = \'OPEN\' AND SYMBOL_NAME = \'" + sellerorder.getSymbolname() + "\' " +
        "AND ACCOUNT_ID <> " + sellerorder.getAccountID() +
        " ORDER BY LIMIT_PRICE DESC, CREATED_TIME ASC;";
        ResultSet result = DBHandler.getInstance().commitAndReturn(sql);

        while(result.next()){

            int temporderID = result.getInt("ORDER_ID");
            double temoamountPurchase = result.getDouble("AMOUNT_PURCHASE");
            double templimitPrice = result.getDouble("LIMIT_PRICE");
            String tempstatus = result.getString("STATUS");
            Timestamp tempcreatedTime = result.getTimestamp("CREATED_TIME");
            String tempsymbolName = result.getString("SYMBOL_NAME");
            int tempaccountID = result.getInt("ACCOUNT_ID");
            int temptransID = result.getInt("TRANS_ID");
            MyOrder temporder = new MyOrder(temporderID, temoamountPurchase, templimitPrice, tempstatus, tempcreatedTime, tempsymbolName, tempaccountID, temptransID);
            matchedBuyers.add(temporder);
        }


        return matchedBuyers;

    }

    public void buyerMatch(MyOrder myorder) throws SQLException, IllegalArgumentException {

        ArrayList<MyOrder> matchedSellers = priorityListofmatchedSeller(myorder);
        if(!matchedSellers.isEmpty()){
            for(int i = 0;i<matchedSellers.size();i++){
                // if buyer == seller
                if(myorder.getamountPurchase() == (-1 * matchedSellers.get(i).getamountPurchase())){
                    double matchedmoney = myorder.getamountPurchase() * matchedSellers.get(i).getLimitprice();

                    // update seller
                    double sellerbalance = findBalanceByOrder(matchedSellers.get(i).getAccountID()) + matchedmoney;
                    // update balance from account first
                    updateBalanceDB(matchedSellers.get(i).getAccountID(), sellerbalance);

                    matchedSellers.get(i).findnewestOrder(matchedSellers.get(i).getID());

                    // update buyer
                    double extramoney = this.current_balance + myorder.getamountPurchase() * (myorder.getLimitprice() - matchedSellers.get(i).getLimitprice());
                    // update balance from account first
                    updateBalanceDB(this.accountID, extramoney);

                    this.current_balance = extramoney;
                    myorder.findnewestOrder(myorder.getID());

                    // delete or update position related
                    Position position = new Position(myorder.getamountPurchase(), myorder.getSymbolname(), this.accountID);
                    // update new position to db
                    position.createNewPositionDB();
                    
                    // change from open to executed
                    myorder.symbolUpdateStatusDB("EXECUTED");
                    matchedSellers.get(i).symbolUpdateStatusDB("EXECUTED");

                    matchedSellers.get(i).findnewestOrder(matchedSellers.get(i).getID());
                    myorder.findnewestOrder(myorder.getID());

                    break;

                }else if(myorder.getamountPurchase() > (-1 * matchedSellers.get(i).getamountPurchase())){
                    // if buyer > seller
                    double matchedmoney = (-1 * matchedSellers.get(i).getamountPurchase()) * matchedSellers.get(i).getLimitprice();

                    // update seller
                    double sellerbalance = findBalanceByOrder(matchedSellers.get(i).getAccountID()) + matchedmoney;
                    // update balance from account first
                    updateBalanceDB(matchedSellers.get(i).getAccountID(), sellerbalance);

                    matchedSellers.get(i).findnewestOrder(matchedSellers.get(i).getID());

                    // update buyer
                    double extramoney = this.current_balance + (-1 * matchedSellers.get(i).getamountPurchase()) * (myorder.getLimitprice() - matchedSellers.get(i).getLimitprice());
                    // update balance from account first
                    updateBalanceDB(this.accountID, extramoney);
                    
                    this.current_balance = extramoney;
                    myorder.findnewestOrder(myorder.getID());

                    // delete or update position related
                    Position position = new Position((-1 * matchedSellers.get(i).getamountPurchase()), myorder.getSymbolname(), this.accountID);
                    // update new position to db
                    position.createNewPositionDB();
                    
                    // oepn 400 to
                    // open 100 and executed 300
                    MyOrder executedorder = new MyOrder((-1 * matchedSellers.get(i).getamountPurchase()), matchedSellers.get(i).getLimitprice(), "EXECUTED", Timestamp.from(Instant.now()), myorder.getSymbolname(), this.accountID, myorder.gettransID());
                    executedorder.createMyOrderDB();
                    // change from open to executed
                    double remainingamount = myorder.getamountPurchase() - (-1 * matchedSellers.get(i).getamountPurchase());
                    if(remainingamount<=0){
                        throw new  IllegalArgumentException("new amount of buyer open order cannot be negative or 0");
                    }
                    myorder.amountUpdateStatusDB(remainingamount);
                    matchedSellers.get(i).symbolUpdateStatusDB("EXECUTED");

                    matchedSellers.get(i).findnewestOrder(matchedSellers.get(i).getID());
                    myorder.findnewestOrder(myorder.getID());


                }else if(myorder.getamountPurchase() < (-1 * matchedSellers.get(i).getamountPurchase())){
                    // if buyer < seller
                    double matchedmoney = myorder.getamountPurchase() * matchedSellers.get(i).getLimitprice();

                    // update seller
                    double sellerbalance = findBalanceByOrder(matchedSellers.get(i).getAccountID()) + matchedmoney;
                    // update balance from account first
                    updateBalanceDB(matchedSellers.get(i).getAccountID(), sellerbalance);

                    matchedSellers.get(i).findnewestOrder(matchedSellers.get(i).getID());

                    // update buyer
                    double extramoney = this.current_balance + myorder.getamountPurchase() * (myorder.getLimitprice() - matchedSellers.get(i).getLimitprice());
                    // update balance from account first
                    updateBalanceDB(this.accountID, extramoney);
                    this.current_balance = extramoney;

                    // delete or update position related
                    Position position = new Position(myorder.getamountPurchase(), myorder.getSymbolname(), this.accountID);
                    // update new position to db
                    position.createNewPositionDB();
                    
                    // oepn buy 400 and sell 500 to
                    // executed 400 and sell 100
                    myorder.symbolUpdateStatusDB("EXECUTED");
                    // change from open to executed
                    MyOrder executedorder = new MyOrder((-1 * myorder.getamountPurchase()), matchedSellers.get(i).getLimitprice(), "EXECUTED", Timestamp.from(Instant.now()), myorder.getSymbolname(), matchedSellers.get(i).getAccountID(), matchedSellers.get(i).gettransID());
                    executedorder.createMyOrderDB();
                    double remainingamount = matchedSellers.get(i).getamountPurchase() + myorder.getamountPurchase();
                    if(remainingamount>=0){
                        throw new  IllegalArgumentException("new amount of existed sell open order cannot be positive or 0");
                    }
                    matchedSellers.get(i).amountUpdateStatusDB(remainingamount);

                    matchedSellers.get(i).findnewestOrder(matchedSellers.get(i).getID());
                    myorder.findnewestOrder(myorder.getID());

                    break;

                }

            }
        }

    }

    public void sellerMatch(MyOrder myorder) throws SQLException , IllegalArgumentException {

        ArrayList<MyOrder> matchedBuyers = priorityListofmatchedBuyer(myorder);
        if(!matchedBuyers.isEmpty()){
            for(int i = 0;i<matchedBuyers.size();i++){

                // if seller == buyer
                if((-1 * myorder.getamountPurchase()) == matchedBuyers.get(i).getamountPurchase()){
                    double matchedmoney = matchedBuyers.get(i).getamountPurchase() * matchedBuyers.get(i).getLimitprice();

                    // update seller
                    double sellerbalance = findBalanceByOrder(myorder.getAccountID()) + matchedmoney;
                    // update balance from account first
                    updateBalanceDB(myorder.getAccountID(), sellerbalance);

                    this.current_balance = sellerbalance;
                    myorder.findnewestOrder(myorder.getID());

                    // update buyer
                    // delete or update position related
                    Position position = new Position(matchedBuyers.get(i).getamountPurchase(), matchedBuyers.get(i).getSymbolname(), matchedBuyers.get(i).getAccountID());
                    // update new position to db
                    position.createNewPositionDB();
                    
                    // change from open to executed
                    myorder.symbolUpdateStatusDB("EXECUTED");
                    matchedBuyers.get(i).symbolUpdateStatusDB("EXECUTED");

                    myorder.findnewestOrder(myorder.getID());
                    matchedBuyers.get(i).findnewestOrder(matchedBuyers.get(i).getID());

                    break;

                }else if((-1 * myorder.getamountPurchase()) < matchedBuyers.get(i).getamountPurchase()){
                    // if seller < buyer
                    // buyer > seller
                    double matchedmoney = (-1 * myorder.getamountPurchase()) * matchedBuyers.get(i).getLimitprice();

                    // update seller
                    double sellerbalance = findBalanceByOrder(myorder.getAccountID()) + matchedmoney;
                    // update balance from account first
                    updateBalanceDB(myorder.getAccountID(), sellerbalance);

                    this.current_balance = sellerbalance;
                    myorder.findnewestOrder(myorder.getID());

                    // update buyer
                    // delete or update position related
                    Position position = new Position((-1 * myorder.getamountPurchase()), matchedBuyers.get(i).getSymbolname(), matchedBuyers.get(i).getAccountID());
                    // update new position to db
                    position.createNewPositionDB();
                    
                    // oepn 400 to
                    // open 100 and executed 300
                    MyOrder executedorder = new MyOrder((-1 * myorder.getamountPurchase()), matchedBuyers.get(i).getLimitprice(), "EXECUTED", Timestamp.from(Instant.now()), matchedBuyers.get(i).getSymbolname(), matchedBuyers.get(i).getAccountID(), matchedBuyers.get(i).gettransID());
                    executedorder.createMyOrderDB();
                    // change from open to executed
                    double remainingamount = matchedBuyers.get(i).getamountPurchase() - (-1 * myorder.getamountPurchase());
                    if(remainingamount<=0){
                        throw new  IllegalArgumentException("new amount of buyer open order cannot be negative or 0");
                    }
                    matchedBuyers.get(i).amountUpdateStatusDB(remainingamount);
                    myorder.symbolUpdateStatusDB("EXECUTED");

                    matchedBuyers.get(i).findnewestOrder(matchedBuyers.get(i).getID());
                    myorder.findnewestOrder(myorder.getID());

                    break;

                }else if((-1 * myorder.getamountPurchase()) > matchedBuyers.get(i).getamountPurchase()){
                    // if seller > buyer
                    // buyer < seller
                    double matchedmoney = matchedBuyers.get(i).getamountPurchase() * matchedBuyers.get(i).getLimitprice();

                    // update seller
                    double sellerbalance = findBalanceByOrder(myorder.getAccountID()) + matchedmoney;
                    // update balance from account first
                    updateBalanceDB(myorder.getAccountID(), sellerbalance);

                    this.current_balance = sellerbalance;
                    myorder.findnewestOrder(myorder.getID());

                    // update buyer
                    // delete or update position related
                    Position position = new Position( matchedBuyers.get(i).getamountPurchase(), matchedBuyers.get(i).getSymbolname(), matchedBuyers.get(i).getAccountID());
                    // update new position to db
                    position.createNewPositionDB();
                    
                    // oepn buy 400 and sell 500 to
                    // executed 400 and sell 100
                    matchedBuyers.get(i).symbolUpdateStatusDB("EXECUTED");
                    // change from open to executed
                    MyOrder executedorder = new MyOrder((-1 * matchedBuyers.get(i).getamountPurchase()), matchedBuyers.get(i).getLimitprice(), "EXECUTED", Timestamp.from(Instant.now()), myorder.getSymbolname(), myorder.getAccountID(), myorder.gettransID());
                    executedorder.createMyOrderDB();
                    double remainingamount = myorder.getamountPurchase() + matchedBuyers.get(i).getamountPurchase();
                    if(remainingamount>=0){
                        throw new  IllegalArgumentException("new amount of existed sell open order cannot be positive or 0");
                    }
                    myorder.amountUpdateStatusDB(remainingamount);

                    matchedBuyers.get(i).findnewestOrder(matchedBuyers.get(i).getID());
                    myorder.findnewestOrder(myorder.getID());


                }
                
            }
        }

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
    public synchronized int createOrderStep1(String rhssymbolName, double rhsamountPurchase, double rhslimitPrice) throws SQLException, IllegalArgumentException {

        int originaltransactionid = 0;
        boolean tf = false;

        if(rhssymbolName == "" || rhssymbolName == null){
            throw new IllegalArgumentException("error symbol name");
        }
        if(rhsamountPurchase==0){
            throw new IllegalArgumentException("error amount purchase");
        }
        if(rhslimitPrice<=0){
            throw new IllegalArgumentException("error limit price");
        }

        // a buyer order
        // balance--
        if(rhsamountPurchase>0){
            double moneyhavetopay = rhsamountPurchase * rhslimitPrice;

            if(moneyhavetopay <= this.current_balance){
                this.current_balance = this.current_balance - moneyhavetopay;
                // update balance from account first
                updateBalanceDB(this.accountID, this.current_balance);
                // crate new transaction
                Transaction transaction = new Transaction(this.accountID);
                // update new transaction to db
                transaction.createTransactionDB();
                // create new order
                MyOrder myorder = new MyOrder(rhsamountPurchase, rhslimitPrice, "OPEN", Timestamp.from(Instant.now()), rhssymbolName, this.accountID, transaction.getID());
                // update new order to db
                myorder.createMyOrderDB();
                
                originaltransactionid = transaction.getID();
                tf = true;

                buyerMatch(myorder);

            }else{
                throw new SQLException("do have enough money to buy a position");
            }

        }else{
            // a seller order
            // position amount--
            double amountofSymbol = findAmountofSymbolDB(rhssymbolName);
            if(((-1) * rhsamountPurchase) <= amountofSymbol){
                
                // delete or update position related
                Position.findOldPositionDirectlyDB(this.accountID, rhssymbolName);
                Position position = new Position(rhsamountPurchase, rhssymbolName, this.accountID, true);
                // update new position to db
                position.createNewPositionDB();

                // crate new transaction
                Transaction transaction = new Transaction(this.accountID);
                // update new transaction to db
                transaction.createTransactionDB();
                // create new order
                MyOrder myorder = new MyOrder(rhsamountPurchase, rhslimitPrice, "OPEN", Timestamp.from(Instant.now()), rhssymbolName, this.accountID, transaction.getID());
                // update new order to db
                myorder.createMyOrderDB();
                
                originaltransactionid = transaction.getID();
                tf = true;

                sellerMatch(myorder);

            }else{
                throw new SQLException("related position does have enough amount to sell");
            }
        }

        if(!tf){
            throw new SQLException("cannot create original buyer or seller order");
        }
        return originaltransactionid;
    }

    // query tag
    public ArrayList<MyOrder> getQuery(int rhstransID, String rhsstatus) throws SQLException {

        ArrayList<MyOrder> orderarr = new ArrayList<>();

        String sql0 = "SELECT * FROM TRANSACTION WHERE TRANS_ID = " + rhstransID + ";";
        ResultSet result0 = DBHandler.getInstance().commitAndReturn(sql0);
        if(!result0.next()){
            throw new SQLException("cannot find this transaction");
        }

        String sql = "SELECT * FROM MYORDER WHERE ACCOUNT_ID = " + this.accountID +
        " AND TRANS_ID = " + rhstransID + " AND STATUS = \'" + rhsstatus + "\';";
        ResultSet result = DBHandler.getInstance().commitAndReturn(sql);

        while(result.next()){

            int temporderID = result.getInt("ORDER_ID");
            double temoamountPurchase = result.getDouble("AMOUNT_PURCHASE");
            double templimitPrice = result.getDouble("LIMIT_PRICE");
            String tempstatus = result.getString("STATUS");
            Timestamp tempcreatedTime = result.getTimestamp("CREATED_TIME");
            String tempsymbolName = result.getString("SYMBOL_NAME");
            int tempaccountID = result.getInt("ACCOUNT_ID");
            int temptransID = result.getInt("TRANS_ID");
            MyOrder temporder = new MyOrder(temporderID, temoamountPurchase, templimitPrice, tempstatus, tempcreatedTime, tempsymbolName, tempaccountID, temptransID);
            orderarr.add(temporder);

        }

        return orderarr;

    }
    // cancel tag
    public void getCancel(int rhstransID) throws SQLException {

        String sql = "SELECT * FROM MYORDER WHERE ACCOUNT_ID = " + this.accountID +
        " AND TRANS_ID = " + rhstransID + " AND STATUS = \'OPEN\';";
        ResultSet result = DBHandler.getInstance().commitAndReturn(sql);
        if(result.next()){

            int temporderID = result.getInt("ORDER_ID");
            double temoamountPurchase = result.getDouble("AMOUNT_PURCHASE");
            double templimitPrice = result.getDouble("LIMIT_PRICE");
            String tempstatus = result.getString("STATUS");
            Timestamp tempcreatedTime = result.getTimestamp("CREATED_TIME");
            String tempsymbolName = result.getString("SYMBOL_NAME");
            int tempaccountID = result.getInt("ACCOUNT_ID");
            int temptransID = result.getInt("TRANS_ID");
            MyOrder temporder = new MyOrder(temporderID, temoamountPurchase, templimitPrice, tempstatus, tempcreatedTime, tempsymbolName, tempaccountID, temptransID);

            String sql2 = "UPDATE MYORDER SET STATUS = \'CANCELED\', CREATED_TIME = \'" + Timestamp.from(Instant.now()) +
            "\' WHERE ACCOUNT_ID = " + this.accountID +
            " AND TRANS_ID = " + rhstransID + " AND ORDER_ID = " + temporder.getID() + ";";
            DBHandler.getInstance().commit(sql2);

            // buyer
            if(temporder.getamountPurchase()>0){

                double backmoney = this.current_balance + temporder.getamountPurchase() * temporder.getLimitprice();
                updateBalanceDB(this.accountID, backmoney);
                this.current_balance = backmoney;

            }else if(temporder.getamountPurchase()<0){
                // seller
                // delete or update position related
                Position position = new Position((-1 * temporder.getamountPurchase()), temporder.getSymbolname(), this.accountID);
                // update new position to db
                position.createNewPositionDB();

            }

            temporder.findnewestOrder(temporder.getID());

        }else{
            throw new SQLException("This transaction does not have open order");
        }

    }

    // 只用于测试db 过会删掉
    /*
    public static void main(String[] args){

        try{
            DBHandler db = new DBHandler();
            db.createDBHandler();

            Account account = new Account(db,12345,20000);
            account.createNewAccountDB();

            Account account2 = new Account(db,55555,20000);
            account2.createNewAccountDB();

            Account account3 = new Account(db,33333,20000);
            account3.createNewAccountDB();

            Account account4 = new Account(db,54321,20000);
            account4.createNewAccountDB();

            Position position = new Position(db, 600, "CCC", 55555);
            position.createNewPositionDB();
            Position position2 = new Position(db, 700, "CCC", 33333);
            position2.createNewPositionDB();

            //int orderID = account.createOrderStep1("SYM", 100, 145.67);
            //System.out.println("account amount now: " + account.current_balance);

            int orderID = account.createOrderStep1("CCC", 500, 3);
            int orderID1 = account4.createOrderStep1("CCC", 200, 2);
            int orderID2 = account2.createOrderStep1("CCC", -600, 1);

            account4.getCancel(2);
            
            ArrayList<MyOrder> orderarr = new ArrayList<>();
            ArrayList<MyOrder> orderarr2 = new ArrayList<>();
            ArrayList<MyOrder> orderarr3 = new ArrayList<>();
            orderarr = account4.getQuery(2, "OPEN");
            for(int i = 0;i<orderarr.size();i++){
                System.out.println("the order id is "+orderarr.get(i).getID()+" and status is "+orderarr.get(i).getstatus());
            }
            orderarr2 = account4.getQuery(2, "EXECUTED");
            for(int i = 0;i<orderarr2.size();i++){
                System.out.println("the order id is "+orderarr2.get(i).getID()+" and status is "+orderarr2.get(i).getstatus());
            }
            orderarr3 = account4.getQuery(2, "CANCELED");
            for(int i = 0;i<orderarr3.size();i++){
                System.out.println("the order id is "+orderarr3.get(i).getID()+" and status is "+orderarr3.get(i).getstatus());
            }
            //int orderID3 = account3.createOrderStep1("CCC", -200, 2);
            
            //System.out.println("account amount now: " + account.current_balance);

            //position = new Position(db, 0, "CCC", 12345, true);
            //position.createNewPositionDB();
            //System.out.println("position amount now: " + position.getAmount());
            //System.out.println("the original order id for buyer is: " + orderID);
            //System.out.println("the original order id for seller is: " + orderID2);

        }catch(Exception e){
            e.printStackTrace();
            System.out.println("something goes wrong with the test");
        }
    }
    */
    
}
