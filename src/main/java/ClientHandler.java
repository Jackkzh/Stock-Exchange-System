import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.CountDownLatch;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class ClientHandler extends SimpleChannelInboundHandler<String> {

//    private final CountDownLatch latch;
//
//    public ClientHandler(CountDownLatch latch) {
//        this.latch = latch;
//    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //发送消息到服务端
        //ctx.writeAndFlush("Successfully connected to server\r\n");
    }



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws  Exception {
        String response = formatXml(msg);
        System.out.println(response);
    }

    public static String formatXml(String unformattedXml) throws Exception {
        // Parse the given XML string into a DOM document
        unformattedXml = unformattedXml.trim();
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(unformattedXml)));

        // Create a transformer for formatting the XML
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty("indent", "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        // Format the XML and write it to a string
        StringWriter stringWriter = new StringWriter();
        StreamResult streamResult = new StreamResult(stringWriter);
        DOMSource domSource = new DOMSource(document);
        transformer.transform(domSource, streamResult);
        return stringWriter.toString();
    }

}


