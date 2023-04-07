import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import java.lang.IllegalArgumentException;

public class DBHandlerTest {

    // functionality test
    @Test
    public void test_normalPositionCreate() throws ClassNotFoundException, SQLException,
            ParserConfigurationException, SAXException, IOException, TransformerException, IllegalArgumentException{

        DBHandler.getInstance().createDBHandler();
        String sql = "SELECT * FROM MYORDER;";
        assertDoesNotThrow(DBHandler.getInstance().commit(sql));
    }

    @Test
    public void test_errorPositionCreatenotexist() throws ClassNotFoundException, SQLException,
            ParserConfigurationException, SAXException, IOException, TransformerException, IllegalArgumentException{

        DBHandler.getInstance().createDBHandler();
        String sql = "SELECT * FROM MYORDER";

        assertThrows(SQLException.class, ()->DBHandler.getInstance().commit(sql));
    }
    
}
