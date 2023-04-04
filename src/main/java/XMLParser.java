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
import java.util.ArrayList;
import java.util.List;

public class XMLParser {

    public void parseXML(String xml) throws ParserConfigurationException, IOException, SAXException, IllegalArgumentException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(xml);
        ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        Document doc = db.parse(input);
        doc.getDocumentElement().normalize();

        Element root = doc.getDocumentElement();
        String rootName = root.getNodeName();
        NodeList nodeList = root.getChildNodes();

        // top root is either create or transactions
        if (rootName.equals("create")) {
            processCreateXML(nodeList);
        } else if (rootName.equals("transactions")) {
//

            processTransactionsXML(nodeList, root);
        } else {
            throw new IllegalArgumentException("XML only accepts create or transactions.");
        }

    }

    /**
     * This function is used to parse and process 'create' operation
     * @param nodeList
     */
    public void processCreateXML(NodeList nodeList) {
        // process each node in nodeList, check

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String nodeName = element.getNodeName();
                if (nodeName.equals("account")) {
                    processAccountXML(element, true);
                } else if (nodeName.equals("symbol")) {
                    // get symbol sym name
                    String symName = element.getAttribute("sym");

                    // recursively process the symbol node
                    NodeList symbolList = element.getChildNodes();
                    for (int j = 0; j < symbolList.getLength(); j++) {
                        Node symbolNode = symbolList.item(j);
                        if (symbolNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element symbolElement = (Element) symbolNode;
                            processAccountXML(symbolElement, false);
                        }
                    }
                }

            }
        }

    }


    /**
     * This is function is used to create an account; if the account already exists, it will throw an exception
     * @param element the element to be processed
     * @param isCreate true if creating a new account, false if creating/updating a symbol
     * @throws InvalidParameterException
     */
    public void processAccountXML(Element element, boolean isCreate) throws InvalidParameterException {
//        if (!element.hasAttribute("id") || !element.hasAttribute("balance")) {
//            throw new InvalidParameterException("Invalid account, attributes missing");
//        }
        try {
            if (isCreate) {
                //turn if into int

                //turn balance into double
                double balance = Double.parseDouble(element.getAttribute("balance"));

                /**
                 * this is the part to creating an account
                 */
                // createAccount(.....); // error when account is already created
            } else {
                int id = Integer.parseInt(element.getAttribute("id"));
                double num = Double.parseDouble(element.getTextContent().trim());
                /**
                 * this is the part to create a symbol(it is legal if the symbol already exists)
                 */
                // createSymbol(.....); // error when symbol is already created

            }

        }
        // this is to catch exception if the format of the id and balance is not int and double
        catch (NumberFormatException e) {
            throw new InvalidParameterException("Invalid attributes format");
        }
        // this is to catch exception if createAccount() throws an exception
        catch (Exception e) {
            throw new InvalidParameterException("transaction logic error");
        }
    }


    /**
     * This function is used to parse and process 'transactions' operation
     * @param nodeList the list of nodes to be processed
     * @param root the root element (which is 'transactions')
     */
    public void processTransactionsXML(NodeList nodeList, Element root) throws InvalidParameterException {
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
        } catch (Exception e) {
            throw new InvalidParameterException("transaction logic error");
        }
    }




}
