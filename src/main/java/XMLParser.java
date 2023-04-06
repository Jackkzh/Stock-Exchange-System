import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
//import for'PostgreJDBC'
import java.sql.*;
import java.sql.Statement;

public class XMLParser {
    // 改
    //public DBHandler db;
    public String responseMessage;

//    public XMLParser(DBHandler rhsdb) {
//        this.db = rhsdb;
//    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public boolean checkValidXML (String xml) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(xml);
            ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
            Document doc = db.parse(input);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return false;
        }
        return true;
    }

    public String makeXML(Document XMLMessage) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(XMLMessage), new StreamResult(writer));
        String result = writer.getBuffer().toString().replaceAll("\n|\r", "");
        return result;
    }

    /**
     * parseXML parses XML from client, and decide which method to call 'create' / 'transaction'
     * @param xml the xml string to be parsed (received from client side)
     * @throws TransformerException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws IllegalArgumentException
     * @throws SQLException
     */
    public void parseXML(String xml) throws TransformerException, ParserConfigurationException, IOException, SAXException, IllegalArgumentException, SQLException {
        // return a error xml to client is xml is not well-formed
        if (!checkValidXML(xml)) {
            // write a xml with root called 'error' and a msg saying 'XML is not well-formed.'
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            Document responseXML = db.newDocument();
            String errorMsg = "XML is not well-formed.";
            Element responseRoot = responseXML.createElement("error");
            responseRoot.appendChild(responseXML.createTextNode(errorMsg));
            responseXML.appendChild(responseRoot);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(responseXML), new StreamResult(writer));
            String result = writer.getBuffer().toString().replaceAll("\n|\r", "");
            responseMessage = result;
            //System.out.println("this is in chcekvalid");

            return;
        }

        // create a response XML
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(xml);
            ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
            Document doc = db.parse(input);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IllegalArgumentException("XML is not well-formed.");
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(xml);
        ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        Document doc = db.parse(input);
        doc.getDocumentElement().normalize();

        // creates a Response XML object
        Document responseXML = db.newDocument();

        // get the root of Request XML
        Element root = doc.getDocumentElement();
        //System.out.println("Root element :" + root.getNodeName());
        String rootName = root.getNodeName();
        NodeList nodeList = root.getChildNodes();

        // top root is either create or transactions
        try {
            if (rootName.equals("create")) {
                Element responseRoot = responseXML.createElement("results");
                responseXML.appendChild(responseRoot);
                processCreateXML(nodeList, responseXML);
            } else if (rootName.equals("transactions")) {
                System.out.println("transactions....");
                Element responseRoot = responseXML.createElement("results");
                responseXML.appendChild(responseRoot);
                processTransactionsXML(nodeList, root, responseXML);
            } else {
                System.out.println("error not either ");
                throw new IllegalArgumentException("XML only accepts create or transactions.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("error in parseXML");
            String errorMsg = e.getMessage();
            Element error = responseXML.createElement("error");
            error.appendChild(responseXML.createTextNode(errorMsg));
            responseXML.appendChild(error);
            responseMessage = makeXML(responseXML);
            return;
        }

        responseMessage = makeXML(responseXML);
        //System.out.println(responseMessage);
//// 将 DOMSource 转换为 StreamResult
//        DOMSource source = new DOMSource(responseXML);
//        StringWriter writer = new StringWriter();
//        StreamResult result = new StreamResult(writer);
//
//// 将 DOM 转换为字符串
//        transformer.transform(source, result);
//        String xmlString = writer.toString();
//
//// 打印 XML 字符串
//        System.out.println(xmlString);

    }

    /**
     * This function is used to parse and process 'create' operation
     * @param nodeList
     */
    public void processCreateXML(NodeList nodeList, Document responseXML) throws TransformerException, SQLException {
        // process each node in nodeList, check
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String nodeName = element.getNodeName();
                try {
                    if (nodeName.equals("account")) {
                        // print in processCreateXML
                        //System.out.println("processCreateXML....");
                        processAccountXML(element, responseXML);
                    } else if (nodeName.equals("symbol")) {
                        // get symbol sym name
                        String symName = element.getAttribute("sym");
                        processSymbol(element, responseXML);
                    } else {
                        System.out.println("error in processCreateXML");
                        throw new IllegalArgumentException("XML only accepts account or symbol.");
                    }
                } catch (IllegalArgumentException e) {
                    //System.out.println("error in processCreateXML");
                    String errorMsg = e.getMessage();
                    Element error = responseXML.createElement("error");
                    error.appendChild(responseXML.createTextNode(errorMsg));
                    Element responseRoot = responseXML.getDocumentElement();
                    responseRoot.appendChild(error);
                    responseMessage = makeXML(responseXML);
                    return;
                }
            }
        }
    }


    /**
     * This is function is used to create an account; if the account already exists, it will throw an exception
     * @param element the element to be processed
     * @param responseXML the responseXML to be added
     * @throws InvalidParameterException
     */
    public void processAccountXML(Element element, Document responseXML) throws TransformerException,InvalidParameterException, SQLException {
        try {
            int id = Integer.parseInt(element.getAttribute("id"));
            double balance = Double.parseDouble(element.getAttribute("balance"));
        }
        catch (NumberFormatException e) {
            System.out.println("Invalid account id or balance");
            String errorMsg = e.getMessage();
            Element created = responseXML.createElement("error");
            created.appendChild(responseXML.createTextNode(errorMsg));
            responseXML.appendChild(created);
            responseMessage = makeXML(responseXML);
            return;
        }

        int accountID = Integer.parseInt(element.getAttribute("id"));
        double balance = Double.parseDouble(element.getAttribute("balance"));

        try {
            /**
             * this is the part to creating an account
             */
            //this.db.getC().setAutoCommit(false);
            //createAccount(.....); // error when account is already created

            // 改
            DBHandler.getInstance().getC().setAutoCommit(false);
            Account account = new Account(accountID, balance);
            account.createNewAccountDB();
            DBHandler.getInstance().getC().commit();

            // write a message in syntax "<created id="ACCOUNT_ID"/>" ,and add to ResponseXML
            Element created = responseXML.createElement("created");
            //int id = Integer.parseInt(element.getAttribute("id"));
            created.setAttribute("id", Integer.toString(accountID));
            // get root of responseXML
            Element responseRoot = responseXML.getDocumentElement();
            responseRoot.appendChild(created);
            responseMessage = makeXML(responseXML);
        } catch (SQLException e) {
            // 改 db错？
            // throw?
//            System.out.println("dslfjhkfhdskfhjfhasdf");
//            try {
            DBHandler.getInstance().getC().rollback();
//            } catch (SQLException e1) {
//               System.out.println("wuwuwuuwuwuw");
//            }
            //System.out.println("Fdafdfsaf");
            String errorMsg = e.getMessage();
            //System.out.println(errorMsg);
            Element created = responseXML.createElement("error");
            //int id = Integer.parseInt(element.getAttribute("id"));
            created.setAttribute("id", Integer.toString(accountID));
            created.appendChild(responseXML.createTextNode(errorMsg));
            Element responseRoot = responseXML.getDocumentElement();
            responseRoot.appendChild(created);
            responseMessage = makeXML(responseXML);
            //System.out.println("error in processAccountXML");
        }
    }


    /**
     * This funciton is used to parse symbol XML
     * @param element the element to be processed
     * @param responseXML the response XML
     */
    public void processSymbol(Element element, Document responseXML) throws InvalidParameterException , SQLException,
            TransformerException {

        String symName = element.getAttribute("sym");
        NodeList symbolList = element.getChildNodes();
        try {
            if (symbolList.getLength() == 0) {
                throw new InvalidParameterException("Invalid symbol, it has no child");
            }
        } catch (NumberFormatException e) {
            String errorMsg = e.getMessage();
            Element created = responseXML.createElement("error");
            created.appendChild(responseXML.createTextNode(errorMsg));
            Element responseRoot = responseXML.getDocumentElement();
            responseRoot.appendChild(created);
            responseMessage = makeXML(responseXML);

        }
        try {
            for (int j = 0; j < symbolList.getLength(); j++) {
                Node symbolNode = symbolList.item(j);
                if (symbolNode.getNodeType() == Node.ELEMENT_NODE) {
                    // symbolElement is the account element
                    Element symbolElement = (Element) symbolNode;
                    String nodeName = symbolElement.getNodeName();
                    if (nodeName.equals("account")) {
                        int id = Integer.parseInt(symbolElement.getAttribute("id"));
                        double num = Double.parseDouble(symbolElement.getTextContent().trim());


                        // createSymbol(.....); // error when symbol is already created
                        // 改
                        DBHandler.getInstance().getC().setAutoCommit(false);
                        Position position = new Position(num,symName,id);
                        position.createNewPositionDB();
                        DBHandler.getInstance().getC().commit();

                        // write a message to ResponseXML
                        Element created = responseXML.createElement("created");
                        created.setAttribute("sym", symName);
                        created.setAttribute("id", Integer.toString(id));
                        Element responseRoot = responseXML.getDocumentElement();
                        responseRoot.appendChild(created);

                    }
                }
            }
            responseMessage = makeXML(responseXML);
        } catch (NumberFormatException e) {
            String errorMsg = e.getMessage();
            Element created = responseXML.createElement("error");
            created.setAttribute("sym", symName);
            Element firstChild = (Element) element.getFirstChild();
            created.setAttribute("id", firstChild.getAttribute("id"));
            created.appendChild(responseXML.createTextNode(errorMsg));
            Element responseRoot = responseXML.getDocumentElement();
            responseRoot.appendChild(created);


        } catch (Exception e) {
            // 改 db错？
            // throw?
            DBHandler.getInstance().getC().rollback();

            String errorMsg = e.getMessage();
            Element created = responseXML.createElement("error");
            created.setAttribute("sym", symName);
            Element firstChild = (Element) element.getFirstChild();
            created.setAttribute("id", firstChild.getAttribute("id"));
            created.appendChild(responseXML.createTextNode(errorMsg));
            Element responseRoot = responseXML.getDocumentElement();
            responseRoot.appendChild(created);
            responseMessage = makeXML(responseXML);
        }

    }



    /**
     * This function is used to parse and process 'transactions' operation
     * @param nodeList the list of nodes to be processed
     * @param root the root element (which is 'transactions')
     * @param responseXML the response XML
     * @throws InvalidParameterException
     */
    public void processTransactionsXML(NodeList nodeList, Element root, Document responseXML) throws TransformerException, InvalidParameterException {

        try {
            int AccountID = Integer.parseInt(root.getAttribute("id"));
        } catch ( NumberFormatException e) {
            String errorMsg = e.getMessage();
            Element created = responseXML.createElement("error");
            created.appendChild(responseXML.createTextNode(errorMsg));
            Element responseRoot = responseXML.getDocumentElement();
            responseRoot.appendChild(created);
            return;
        }


        // the ID of current transactions
        int accountID = Integer.parseInt(root.getAttribute("id"));

        // process each node in nodeList, check
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String nodeName = element.getNodeName();
                try {
                    if (nodeName.equals("order")) {
                        openOrder(accountID, element, responseXML);
                    } else if (nodeName.equals("cancel")) {
                        int TransID = Integer.parseInt(element.getAttribute("id"));
                        cancelOrder(accountID, element, responseXML);
                        // cancel order API
                    } else if (nodeName.equals("query")) {
                        String TransID = element.getAttribute("sym");
                        // query order API
                        queryOrder(accountID, element, responseXML);
                    } else {
                        throw new InvalidParameterException("Invalid operation in transactions");
                    }
                } catch (NumberFormatException e) {
                    String errorMsg = e.getMessage();
                    Element created = responseXML.createElement("error");
                    created.appendChild(responseXML.createTextNode(errorMsg));
                    Element responseRoot = responseXML.getDocumentElement();
                    responseRoot.appendChild(created);
                } catch (InvalidParameterException e) {
                    String errorMsg = e.getMessage();
                    Element created = responseXML.createElement("error");
                    created.appendChild(responseXML.createTextNode(errorMsg));
                    Element responseRoot = responseXML.getDocumentElement();
                    responseRoot.appendChild(created);
                } catch (Exception e) {
                    String errorMsg = e.getMessage();
                    Element created = responseXML.createElement("error");
                    created.appendChild(responseXML.createTextNode(errorMsg));
                    Element responseRoot = responseXML.getDocumentElement();
                    responseRoot.appendChild(created);
                }
            }
        }
        responseMessage = makeXML(responseXML);
    }

    /**
     * This function is used to parse and process 'query' operation
     * @param accountID the ID of current account
     * @param element the element to be processed
     * @param responseXML the response XML
     * @throws InvalidParameterException
     * @throws SQLException
     */
    public void openOrder(int accountID, Element element, Document responseXML) throws InvalidParameterException,
            SQLException, TransformerException {
        try {
            String sym = element.getAttribute("sym");
            double amount = Double.parseDouble(element.getAttribute("amount"));
            double limits = Double.parseDouble(element.getAttribute("limit"));
        } catch (NumberFormatException e) {
            String errorMsg = e.getMessage();
            Element created = responseXML.createElement("error");
            created.appendChild(responseXML.createTextNode(errorMsg));
            Element responseRoot = responseXML.getDocumentElement();
            responseRoot.appendChild(created);
        }

        String sym = element.getAttribute("sym");
        double amount = Double.parseDouble(element.getAttribute("amount"));
        double limits = Double.parseDouble(element.getAttribute("limit"));

        try {
            /**
             * open order method
             */
            // 改
            DBHandler.getInstance().getC().setAutoCommit(false);
            Account account = new Account(accountID);
            // transactionID 记得调用
            int transactionID = account.createOrderStep1(sym, amount, limits);
            DBHandler.getInstance().getC().commit();

            Element opened = responseXML.createElement("opened");
            opened.setAttribute("sym", sym);
            opened.setAttribute("amount", Double.toString(amount));
            opened.setAttribute("limit", Double.toString(limits));
            opened.setAttribute("id", Integer.toString(transactionID));
            Element responseRoot = responseXML.getDocumentElement();
            responseRoot.appendChild(opened);
            responseMessage = makeXML(responseXML);
        } catch ( Exception e) {

            // 改 db错？
            // throw?
            DBHandler.getInstance().getC().rollback();

            String errorMsg = e.getMessage();
            Element opened = responseXML.createElement("error");
            opened.setAttribute("sym", sym);
            opened.setAttribute("amount", Double.toString(amount));
            opened.setAttribute("limit", Double.toString(limits));
            opened.appendChild(responseXML.createTextNode(errorMsg));
            Element responseRoot = responseXML.getDocumentElement();
            responseRoot.appendChild(opened);
            responseMessage = makeXML(responseXML);
        }
    }

    public void queryOrder(int accountID, Element element, Document responseXML) throws SQLException, TransformerException {
        try {
            int transID = Integer.parseInt(element.getAttribute("id"));
        } catch (NumberFormatException e) {
            String errorMsg = e.getMessage();
            Element created = responseXML.createElement("error");
            created.appendChild(responseXML.createTextNode(errorMsg));
            Element responseRoot = responseXML.getDocumentElement();
            responseRoot.appendChild(created);
            responseMessage = makeXML(responseXML);
        }
        int transID = Integer.parseInt(element.getAttribute("id"));
//        String sym = element.getAttribute("sym");
//        double amount = Double.parseDouble(element.getAttribute("amount"));
//        double limits = Double.parseDouble(element.getAttribute("limit"));
        try {
            /**
             * query order method
             */
            // call query-order method
            // 改
            DBHandler.getInstance().getC().setAutoCommit(false);
            Account account = new Account(accountID);
            ArrayList<MyOrder> orderarrOpen = new ArrayList<>();
            ArrayList<MyOrder> orderarrExecuted = new ArrayList<>();
            ArrayList<MyOrder> orderarrCanceled = new ArrayList<>();
            orderarrOpen = account.getQuery(transID, "OPEN");
            orderarrExecuted = account.getQuery(transID, "EXECUTED");
            orderarrCanceled = account.getQuery(transID, "CANCELED");
            DBHandler.getInstance().getC().commit();


            Element status = responseXML.createElement("status");
            status.setAttribute("id", Integer.toString(transID));


            for (int i = 0; i < orderarrOpen.size(); i++) {
                Element open = responseXML.createElement("open");
                open.setAttribute("shares", Double.toString(orderarrOpen.get(i).getamountPurchase()));
                status.appendChild(open);
            }

            for (int i = 0; i < orderarrCanceled.size(); i++) {
                Element executed = responseXML.createElement("canceled");
                executed.setAttribute("shares", Double.toString(orderarrCanceled.get(i).getamountPurchase()));
                // convert a timestamp called A to string
                executed.setAttribute("time", orderarrCanceled.get(i).getCreatedTime().toString());
                status.appendChild(executed);
            }
            for (int i = 0; i < orderarrExecuted.size(); i++) {
                Element canceled = responseXML.createElement("executed");
                canceled.setAttribute("shares", Double.toString(orderarrExecuted.get(i).getamountPurchase()));
                canceled.setAttribute("price", Double.toString(orderarrExecuted.get(i).getLimitprice()));
                // convert a timestamp called A to string
                canceled.setAttribute("time", orderarrExecuted.get(i).getCreatedTime().toString());
                status.appendChild(canceled);
            }
            Element responseRoot = responseXML.getDocumentElement();
            responseRoot.appendChild(status);
            responseMessage = makeXML(responseXML);


        } catch ( Exception e) {
            // 改 db错？
            // throw?
            DBHandler.getInstance().getC().rollback();

            String errorMsg = e.getMessage();
            Element queried = responseXML.createElement("error");
            queried.setAttribute("id", Integer.toString(transID));
            queried.appendChild(responseXML.createTextNode(errorMsg));
            Element responseRoot = responseXML.getDocumentElement();
            responseRoot.appendChild(queried);
            responseMessage = makeXML(responseXML);
        }
    }

    public void cancelOrder(int accountID, Element element, Document responseXML) throws SQLException, TransformerException {
        try {
            int transID = Integer.parseInt(element.getAttribute("id"));
        } catch (NumberFormatException e) {
            String errorMsg = e.getMessage();
            Element created = responseXML.createElement("error");
            created.appendChild(responseXML.createTextNode(errorMsg));
            Element responseRoot = responseXML.getDocumentElement();
            responseRoot.appendChild(created);
            responseMessage = makeXML(responseXML);
        }
        int transID = Integer.parseInt(element.getAttribute("id"));
        try {
            /**
             * cancel order method
             */

            // 改
            DBHandler.getInstance().getC().setAutoCommit(false);
            Account account = new Account(accountID);
            account.getCancel(transID);
            ArrayList<MyOrder> orderarrExecuted = new ArrayList<>();
            ArrayList<MyOrder> orderarrCanceled = new ArrayList<>();

            orderarrExecuted = account.getQuery(transID, "EXECUTED");
            orderarrCanceled = account.getQuery(transID, "CANCELED");
            DBHandler.getInstance().getC().commit();

            // loop to add all canceled lines here

            Element status = responseXML.createElement("status");
            status.setAttribute("id", Integer.toString(transID));

            for (int i = 0; i < orderarrCanceled.size(); i++) {
                Element executed = responseXML.createElement("canceled");
                executed.setAttribute("shares", Double.toString(orderarrCanceled.get(i).getamountPurchase()));
                // convert a timestamp called A to string
                executed.setAttribute("time", orderarrCanceled.get(i).getCreatedTime().toString());
                status.appendChild(executed);
            }
            for (int i = 0; i < orderarrExecuted.size(); i++) {
                Element canceled = responseXML.createElement("executed");
                canceled.setAttribute("shares", Double.toString(orderarrExecuted.get(i).getamountPurchase()));
                canceled.setAttribute("price", Double.toString(orderarrExecuted.get(i).getLimitprice()));
                // convert a timestamp called A to string
                canceled.setAttribute("time", orderarrExecuted.get(i).getCreatedTime().toString());
                status.appendChild(canceled);
            }
            Element responseRoot = responseXML.getDocumentElement();
            responseRoot.appendChild(status);
            responseMessage = makeXML(responseXML);

            // call cancel-order method
        } catch (Exception e) {

            // 改 db错？
            // throw?
            DBHandler.getInstance().getC().rollback();

            String errorMsg = e.getMessage();
            Element canceled = responseXML.createElement("error");
            canceled.setAttribute("id", Integer.toString(transID));
            canceled.appendChild(responseXML.createTextNode(errorMsg));
            Element responseRoot = responseXML.getDocumentElement();
            responseRoot.appendChild(canceled);
            responseMessage = makeXML(responseXML);
        }
    }





    // write a main function to test the XMLParser
    public static void main(String[] args) throws TransformerException,ParserConfigurationException, IOException, SAXException, SQLException {

        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<create>" +
                "    <account id=\"2\" balance=\"100.00\"/>" +
                "    <account id=\"1\" balance=\"100.00\"/>" +
                "    <symbol sym=\"AAPL\">\n" +
                "        <account id=\"2\">1234</account>" +
                "        <account id=\"3\">12324</account>" +
                "    </symbol>" +
                "    <symbol sym=\"MSFT\">" +
                "        <account id=\"5\">12</account>" +
                "        <account id=\"3\">324</account>" +
                "    </symbol>" +
                "</create>";
        
        // 改
        try{
            //DBHandler db = DBHandler.getInstance();
            DBHandler.getInstance().createDBHandler();
            XMLParser xmlParser = new XMLParser();
            xmlParser.parseXML(xml);
            DBHandler.getInstance().getC().close();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("something goes wrong");
        }
        
    }
}
