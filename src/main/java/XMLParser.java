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
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class XMLParser {

    public void parseXML(String xml) throws TransformerException, ParserConfigurationException, IOException, SAXException, IllegalArgumentException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(xml);
        ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        Document doc = db.parse(input);
        doc.getDocumentElement().normalize();

        // creates a Response XML object
        Document responseXML = db.newDocument();
        Element responseRoot = responseXML.createElement("results");
        responseXML.appendChild(responseRoot);

        // get the root of Request XML
        Element root = doc.getDocumentElement();
        String rootName = root.getNodeName();
        NodeList nodeList = root.getChildNodes();

        // top root is either create or transactions
        if (rootName.equals("create")) {
            processCreateXML(nodeList, responseXML);
        } else if (rootName.equals("transactions")) {
            System.out.println("transactions....");
            processTransactionsXML(nodeList, root, responseXML);
        } else {
            throw new IllegalArgumentException("XML only accepts create or transactions.");
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(responseXML), new StreamResult(writer));
        String result = writer.getBuffer().toString().replaceAll("\n|\r", "");
        System.out.println(result);
//        TransformerFactory transformerFactory = TransformerFactory.newInstance();
//        Transformer transformer = transformerFactory.newTransformer();
//
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
    public void processCreateXML(NodeList nodeList, Document responseXML) {
        // process each node in nodeList, check
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String nodeName = element.getNodeName();
                if (nodeName.equals("account")) {
                    // print in processCreateXML
                    //System.out.println("processCreateXML....");
                    processAccountXML(element, responseXML);
                } else if (nodeName.equals("symbol")) {
                    // get symbol sym name
                    String symName = element.getAttribute("sym");
                    processSymbol(element, responseXML);
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
    public void processAccountXML(Element element, Document responseXML) throws InvalidParameterException {
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
            return;
        }

        try {
            /**
             * this is the part to creating an account
             */
            //createAccount(.....); // error when account is already created


            // write a message in syntax "<created id="ACCOUNT_ID"/>" ,and add to ResponseXML
            Element created = responseXML.createElement("created");
            int id = Integer.parseInt(element.getAttribute("id"));
            created.setAttribute("id", Integer.toString(id));
            // get root of responseXML
            Element responseRoot = responseXML.getDocumentElement();
            responseRoot.appendChild(created);
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            Element created = responseXML.createElement("error");
            int id = Integer.parseInt(element.getAttribute("id"));
            created.setAttribute("id", Integer.toString(id));
            created.appendChild(responseXML.createTextNode(errorMsg));
            Element responseRoot = responseXML.getDocumentElement();
            responseRoot.appendChild(created);
        }
    }


    /**
     * This funciton is used to parse symbol XML
     * @param element the element to be processed
     * @param responseXML the response XML
     */
    public void processSymbol(Element element, Document responseXML) throws InvalidParameterException {

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


                        // write a message to ResponseXML
                        Element created = responseXML.createElement("created");
                        created.setAttribute("sym", symName);
                        created.setAttribute("id", Integer.toString(id));
                        Element responseRoot = responseXML.getDocumentElement();
                        responseRoot.appendChild(created);
                    }
                }
            }
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
            String errorMsg = e.getMessage();
            Element created = responseXML.createElement("error");
            created.setAttribute("sym", symName);
            Element firstChild = (Element) element.getFirstChild();
            created.setAttribute("id", firstChild.getAttribute("id"));
            created.appendChild(responseXML.createTextNode(errorMsg));
            Element responseRoot = responseXML.getDocumentElement();
            responseRoot.appendChild(created);
        }

    }




    /**
     * This function is used to parse and process 'transactions' operation
     * @param nodeList the list of nodes to be processed
     * @param root the root element (which is 'transactions')
     * @ param responseXML the response XML
     */
    public void processTransactionsXML(NodeList nodeList, Element root, Document responseXML) throws InvalidParameterException {
        try {
            // check if Account ID is valid
            int accountID = Integer.parseInt(root.getAttribute("id"));

            // process each node in nodeList, check
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String nodeName = element.getNodeName();
                    if (nodeName.equals("order")) {
                        String sym = element.getAttribute("sym");
                        double amount = Double.parseDouble(element.getAttribute("amount"));
                        double limits = Double.parseDouble(element.getAttribute("limit"));
                        // opened order API


                    } else if (nodeName.equals("cancel")) {
                        int TransID = Integer.parseInt(element.getAttribute("id"));
                        // cancel order API
                    } else if (nodeName.equals("query")) {
                        String TransID = element.getAttribute("sym");
                        // query order API
                    }
                }
            }
        } catch (NumberFormatException e) {
            throw new InvalidParameterException("Invalid attributes format");
            //responseXML.appendChild(responseRoot);
        } catch (Exception e) {
            throw new InvalidParameterException("transaction logic error");
        }
    }


    // write a main function to test the XMLParser
    public static void main(String[] args) throws TransformerException,ParserConfigurationException, IOException, SAXException, SQLException {
        XMLParser xmlParser = new XMLParser();
        String xml =
                "<create>\n" +
                "    <account id=\"2\" balance=\"100.00\"/>\n" +
                "    <account id=\"1\" balance=\"100.00\"/>\n" +
                "    <symbol sym=\"AAPL\">\n" +
                "        <account id=\"2\">1234</account>\n" +
                "        <account id=\"3\">12324</account>\n" +
                "    </symbol>\n" +
                "    <symbol sym=\"MSFT\">\n" +
                "        <account id=\"5\">12</account>\n" +
                "        <account id=\"3\">324</account>\n" +
                "    </symbol>\n" +
                "</create>";
        xmlParser.parseXML(xml);
    }
}
