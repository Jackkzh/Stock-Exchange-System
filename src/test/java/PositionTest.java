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

public class PositionTest {

    // functionality test
    @Test
    public void test_normalPositionCreate() throws ClassNotFoundException, SQLException,
            ParserConfigurationException, SAXException, IOException, TransformerException, IllegalArgumentException{

        DBHandler.getInstance().createDBHandler();
        Account account = new Account(12345,20000);
        account.createNewAccountDB();
        Position position = new Position(10, "SYM", 12345);
        Executable executable = () -> position.createNewPositionDB();
        assertDoesNotThrow(executable);
        //assertDoesNotThrow(position.createNewPositionDB());
    }

    @Test
    public void test_errorPositionCreate() throws ClassNotFoundException, SQLException,
            ParserConfigurationException, SAXException, IOException, TransformerException, IllegalArgumentException{

        DBHandler.getInstance().createDBHandler();
        Account account = new Account(12345,20000);
        account.createNewAccountDB();
        Position position = new Position(-100, "SYM", 12345);

        assertThrows(IllegalArgumentException.class, ()->position.createNewPositionDB());
    }

    @Test
    public void test_errorPositionCreatenotexist() throws ClassNotFoundException, SQLException,
            ParserConfigurationException, SAXException, IOException, TransformerException, IllegalArgumentException{

        DBHandler.getInstance().createDBHandler();
        Account account = new Account(12345,20000);
        account.createNewAccountDB();
        Position position = new Position(100, "SYM", 123456);

        assertThrows(SQLException.class, ()->position.createNewPositionDB());
    }
    
}
