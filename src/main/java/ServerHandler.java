
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import jakarta.persistence.criteria.CriteriaBuilder.In;
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
import java.sql.SQLException;
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
    //public DBHandler db;

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
        // change lines[0] to integer
        Integer length = Integer.parseInt(lines[0]);
        
        System.out.println("Received message from client of length: " + length); 

        // save lines from 1 - end
        String request = "";
        for (int i = 1; i < lines.length; i++) {
            request += lines[i];
            // 处理每一行
            // ...
        }
        //Integer lengthWithoutSpaces = request.length();
        //System.out.println("Received message from client of length: " + lengthWithoutSpaces);
        //System.out.println(request);


//        int length = Integer.parseInt(lines[0]);
//        System.out.println("fdsfdfasf");
//        String request = "";
//        for (int i = 1; i < lines.length; i++) {
//            request += lines[i];
//            // 处理每一行
//            // ...
//        }



        //System.out.println("Received message from client: " + request.length());

        //System.out.println(request);

        //System.out.println("Received message from client: " + request);
        // 改
        try{

            xmlParser.parseXML(request);

            String response = xmlParser.getResponseMessage();
            System.out.println(response);
            ctx.writeAndFlush(response);

            //db.getC().close();
        } catch (IllegalArgumentException e) {
            System.out.println("in illegal argument exception");
            System.out.println(e.getMessage());
            return;
        }
        catch(SAXException e){
            System.out.println("in SAX exception");
            System.out.println(e.getMessage());
            return;
        } catch (IOException e) {
            System.out.println("in IO exception");
            System.out.println(e.getMessage());
            return;
        } catch (ParserConfigurationException e) {
            System.out.println("in parser configuration exception");
            System.out.println(e.getMessage());
            return;
        } catch (TransformerException e) {
            System.out.println("in transformer exception");
            System.out.println(e.getMessage());
            return;
        } catch (SQLException e) {
            System.out.println("in SQL exception");
            System.out.println(e.getMessage());
            return;
        } catch (Exception e) {
            System.out.println("fdsfdfjdskfdjjldsajfkjfkdsajlkjfakljlasdjlkf");
            System.out.println(e.getMessage());
            return;
        }
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
        //DBHandler db = DBHandler.getInstance();
        DBHandler.getInstance().createDBHandler();
        xmlParser = new XMLParser();
//        db = new DBHandler();
//        db.createDBHandler();
        //System.out.println("***** Client: " + incoming.remoteAddress() + " is connected. *****");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        //DBHandler db = DBHandler.getInstance();
        DBHandler.getInstance().getC().close();
//        db.getC().close();
    }
}
