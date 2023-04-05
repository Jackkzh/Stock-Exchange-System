import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import java.util.concurrent.CountDownLatch;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientHandler extends SimpleChannelInboundHandler<String> {

//    private final CountDownLatch latch;
//
//    public ClientHandler(CountDownLatch latch) {
//        this.latch = latch;
//    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //发送消息到服务端
        ctx.writeAndFlush("Successfully connected to server\r\n");
    }



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        System.out.println("Received message from server: " + msg);
//        if ("Server successfully received the message.".equals(msg)) {
//            latch.countDown();
//        }
    }

}


