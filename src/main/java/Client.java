import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

//import for decoder and encoder
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import java.util.concurrent.CountDownLatch;
//import for  'Base64'
import java.util.Base64;

public class Client {

    public static void main(String[] args) throws Exception {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        try {
            //创建bootstrap对象，配置参数
//            CountDownLatch latch = new CountDownLatch(1);
            Bootstrap bootstrap = new Bootstrap();
            //设置线程组
            bootstrap.group(eventExecutors)
                    //设置客户端的通道实现类型
                    .channel(NioSocketChannel.class)
                    //使用匿名内部类初始化通道
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //添加客户端通道的处理器
                            //ch.pipeline().addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                            ch.pipeline().addLast("decoder", new StringDecoder());
                            ch.pipeline().addLast("encoder", new StringEncoder());
                            ch.pipeline().addLast(new ClientHandler());
                        }
                    });
            System.out.println("This is Client side!");
            //连接服务端
            ChannelFuture channelFuture = bootstrap.connect("vcm-32395.vm.duke.edu", 12345).sync();
            //vcm-32395.vm.duke.edu
//            String input =
//                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
//                    "<create>\n" +
//                            "    <account id=\"2\" balance=\"100.00\"/>\n" +
//                            "    <account id=\"1\" balance=\"100.00\"/>\n" +
//                            "    <symbol sym=\"AAPL\">\n" +
//                            "        <account id=\"2\">1234</account>\n" +
//                            "        <account id=\"3\">12324</account>\n" +
//                            "    </symbol>\n" +
//                            "    <symbol sym=\"MSFT\">\n" +
//                            "        <account id=\"5\">12</account>\n" +
//                            "        <account id=\"3\">324</account>\n" +
//                            "    </symbol>\n" +
//                            "</create>\n";
            //channelFuture.channel().writeAndFlush(input);
            while (true) {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                StringBuilder sb = new StringBuilder(); // 用 StringBuilder 来保存所有的输入行
                sb.setLength(0);
                String firstLine = in.readLine();

                if (firstLine.equals("exit")) {
                    break;
                }
                sb.append(firstLine).append(System.lineSeparator());
                while (true) {
                    String line = in.readLine();
                    if (line.isEmpty()) {
                        break;
                    }
                    // System.lineSeparator() 返回当前系统的行分隔符；可以避免因不同操作系统行分隔符不同而产生的问题。
                    sb.append(line).append(System.lineSeparator());
                }
                //对通道关闭进行监听
                String input = sb.toString(); // 将 StringBuilder 转成字符串
                channelFuture.channel().writeAndFlush(input);
                //System.out.println("Sent message to server: " + input);
            }

            //channelFuture.channel().closeFuture().sync();
        } finally {
            //关闭线程组
            eventExecutors.shutdownGracefully();
        }
    }
}
