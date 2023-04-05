import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

public class XMLParserTest {

    public void helper_responseComparator(String request, String expectedResponse) throws ClassNotFoundException,
            SQLException, ParserConfigurationException, SAXException, IOException, TransformerException {
//        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
//
//        Shared.cleanAllTables(jdbc);

        XMLParser parser = new XMLParser();
        String response = parser.reponseMessage;
        //System.out.println(response);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void test_parseAndProcessRequest_malformed() throws ClassNotFoundException, SQLException,
            ParserConfigurationException, SAXException, IOException, TransformerException {
        String request =
                "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
                        "<create" +
                        "<account id=\"123456\" balance=\"1000\"/>" +
                        "<account id=\"738\" balance=\"2000\"/>" +
                        "</create>";

        String expected =
                "<error>" +
                        "XML only accepts create or transactions." +
                        "</error>";
        this.helper_responseComparator(request, expected);
    }

    @Test
    public void test_parseAndProcessRequest_illegalRoot() throws ClassNotFoundException, SQLException,
            ParserConfigurationException, SAXException, IOException, TransformerException{

        String request =
                "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
                        "<reate>" +
                        "<account id=\"123456\" balance=\"1000\"/>" +
                        "<account id=\"738\" balance=\"2000\"/>" +
                        "</reate>";

        String expected =
                "<error>" +
                        "XML only accepts create or transactions." +
                        "</error>";

        this.helper_responseComparator(request, expected);
    }


}