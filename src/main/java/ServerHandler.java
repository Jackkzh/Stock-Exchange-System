
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

//import for channel
import io.netty.channel.Channel;

import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
//import for list
import java.util.List;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import org.xml.sax.InputSource;
import java.util.Base64;

public class ServerHandler extends SimpleChannelInboundHandler<String> {

    public XMLParser xmlParser;

    public ServerHandler() {

    }

    public List<Integer> findDifferentIndexes(String str1, String str2) {
        List<Integer> indexes = new ArrayList<>();
        int minLength = Math.min(str1.length(), str2.length());
        for (int i = 0; i < minLength; i++) {
            if (str1.charAt(i) != str2.charAt(i)) {
                indexes.add(i);
            }
        }
        if (str1.length() != str2.length()) {
            int maxLen = Math.max(str1.length(), str2.length());
            indexes.add(maxLen - 1);
        }
        return indexes;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg)  throws TransformerException,
            ParserConfigurationException, IOException, SAXException, IllegalArgumentException {

        // clear msg from before


        String[] lines = msg.split(System.lineSeparator());

        String request = "";
        for (String line : lines) {
                request += line.trim();
        }
        //System.out.println("Received message from client: " + request);

        //check if msg is a valid xml, if not print error message
//        try {
//            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder builder = factory.newDocumentBuilder();
//            builder.parse(new InputSource(new StringReader(msg)));
//        } catch (SAXException e) {
//            System.out.println("Invalid XML");
//            return;
//        }
//
//
//        xmlParser = new XMLParser();
//        try {
//            xmlParser.parseXML(msg);
//        } catch (IllegalArgumentException e)
//        {
//            System.out.println("Invalid XML syntax");
//            return;
//        }
        xmlParser = new XMLParser();
        xmlParser.parseXML(request);

        String response = xmlParser.getResponseMessage();
        ctx.writeAndFlush(response);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //发送消息给客户端
        //ctx.writeAndFlush("Server successfully received the message.\r\n");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        //System.out.println("***** Client: " + incoming.remoteAddress() + " is connected. *****");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
    }
}
