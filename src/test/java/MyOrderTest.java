import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import java.lang.IllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.function.Executable;

public class MyOrderTest {

    // functionality test
    @Test
    public void test_normalOrderCreate() throws ClassNotFoundException, SQLException,
            ParserConfigurationException, SAXException, IOException, TransformerException, IllegalArgumentException{

        DBHandler.getInstance().createDBHandler();
        Account account = new Account(12345,20000);
        account.createNewAccountDB();
        Transaction transaction = new Transaction(12345);
        transaction.createTransactionDB();
        MyOrder myorder = new MyOrder(100, 1, "OPEN", Timestamp.from(Instant.now()), "SYM", 12345, transaction.getID());
        Executable executable = () -> myorder.createMyOrderDB();
        assertDoesNotThrow(executable);
        //assertDoesNotThrow(myorder.createMyOrderDB());
    }

    @Test
    public void test_errorOrderCreate() throws ClassNotFoundException, SQLException,
            ParserConfigurationException, SAXException, IOException, TransformerException, IllegalArgumentException{

        DBHandler.getInstance().createDBHandler();
        Account account = new Account(12345,20000);
        account.createNewAccountDB();
        Transaction transaction = new Transaction(12345);
        transaction.createTransactionDB();
        MyOrder myorder = new MyOrder(100, -1, "OPEN", Timestamp.from(Instant.now()), "SYM", 12345, transaction.getID());
        assertThrows(IllegalArgumentException.class, ()->myorder.createMyOrderDB());
    }
    
}
