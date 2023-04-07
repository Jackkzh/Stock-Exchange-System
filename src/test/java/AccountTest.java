import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import java.lang.IllegalArgumentException;

public class AccountTest {

    // scalability test
    public void helper_responseComparator(String request, String expectedResponse) throws ClassNotFoundException,
            SQLException, ParserConfigurationException, SAXException, IOException, TransformerException {
//        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
//
//        Shared.cleanAllTables(jdbc);
        DBHandler db = DBHandler.getInstance();
        db.createDBHandler();
        XMLParser parser = new XMLParser();
        String response = parser.responseMessage;
        //System.out.println(response);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void test_normalCreateAccount() throws ClassNotFoundException, SQLException,
            ParserConfigurationException, SAXException, IOException, TransformerException {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<create>" +
                        "<account id=\"123456\" balance=\"1000\"/>" +
                        "<account id=\"738\" balance=\"2000\"/>" +
                    "</create>";

        String expected =
                "<results>"+
                    "<created id=\"123456\"/>" +
                    "<created id=\"738\"/>" +
                "</results>";
        this.helper_responseComparator(request, expected);
    }

    @Test
    public void test_parseAndProcessRequest_illegalRoot() throws ClassNotFoundException, SQLException,
            ParserConfigurationException, SAXException, IOException, TransformerException{

        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<create>" +
                        "<account id=\"123456\" balance=\"1000\"/>" +
                        "<account id=\"123456\" balance=\"2000\"/>" +
                    "</create>";

        String expected =
                "<results>"+
        "<created id=\"123456\"/>" +
        "<created id=\"738\"/>" +
                "</results>";

        this.helper_responseComparator(request, expected);
    }

    // functionality test
    @Test
    public void test_normalCreate() throws ClassNotFoundException, SQLException,
            ParserConfigurationException, SAXException, IOException, TransformerException, IllegalArgumentException{

        DBHandler.getInstance().createDBHandler();
        Account account = new Account(12345,20000);
        account.createNewAccountDB();
        assertEquals(12345, account.getAccountID());
        assertEquals(20000, account.getcurrentBalance());
    }

    @Test
    public void test_errorCreate() throws ClassNotFoundException, SQLException,
            ParserConfigurationException, SAXException, IOException, TransformerException, IllegalArgumentException{

        DBHandler.getInstance().createDBHandler();
        Account account = new Account(12345,20000);
        account.createNewAccountDB();
        Account account2 = new Account(12345,20000);
        assertThrows(SQLException.class, ()->account2.createNewAccountDB());
    }

    @Test
    public void test_errorBalance() throws ClassNotFoundException, SQLException,
            ParserConfigurationException, SAXException, IOException, TransformerException, IllegalArgumentException{

        DBHandler.getInstance().createDBHandler();
        Account account = new Account(12345,-20000);
        assertThrows(IllegalArgumentException.class, ()->account.createNewAccountDB());
    }

}
