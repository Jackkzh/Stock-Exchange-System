public class 草稿 {

    // db是什么东西？server class里面的一个field 也就是DBHandler db
    public void zwx的server(){
        // DBHandler db = new DBHandler();
        // ......一些zwx那边的步骤
        // response被发送之后记得connection close 也就是下面这行
        // this.db.getC().close();
    }

    public void zwx的parse(){

        // msg......开始拆东西
        // 如果拆出来是create tag
            // 如果拆出来 account tag
                // lock
                // try-catch 结构
                // this.db.getC().setAutoCommit(false);
                // Account account = new account(db, accountID, current_balance)
                // account.createNewAccountDB()
                // this.db.getC().commit();
                // unlock
                // 如果catch了除了sqlException之外的Exception 格式错误 压根不会与db交互 输出相关error tag
                // 如果catch了sqlException 说明db内部出现错误 新建account不成功
                // 比如账户已存在 输出相关error tag 并且需要rollback！！！
            // 如果拆出来 symbol tag
                // lock
                // try-catch 结构
                // this.db.getC().setAutoCommit(false);
                // Position position = new position(拆出来的值1，值2，值3...)
                // position.createNewPositionDB()
                // this.db.getC().commit();
                // unlock
                // 如果catch了除了sqlException之外的Exception 格式错误 压根不会与db交互 输出相关error tag
                // 如果catch了sqlException 说明db内部出现错误 新建position不成功
                // 输出相关error tag 并且需要rollback！！！
        // 如果拆出来是transaction tag
            // 如果拆出来 order tag
                // lock
                // Account account = new account(db, accountID)
                // int orderID = account.createOrderStep1(amountPurchase, limitPrice, symbolName)
                // unlock
            // 如果拆出来 query tag
                // lock
                // Account account = new account(db, accountID)
                // unlock
            // 如果拆出来 cancel tag
                // lock
                // Account account = new account(db, accountID)
                // unlock

    }
    
}
