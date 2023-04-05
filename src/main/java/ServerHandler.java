
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

public class ServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        //读取客户端发送的消息，并打印出来
        System.out.println("***** Client " + ctx.channel().remoteAddress() + " : sent a message: *****");
        System.out.println(msg);
        String[] lines = msg.split(System.lineSeparator());
        for (String line : lines) {
            System.out.println(line);
        }

        //ctx.writeAndFlush("Server successfully received the message.\r\n");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //发送消息给客户端
        ctx.writeAndFlush("Server successfully received the message.\r\n");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("***** Client: " + incoming.remoteAddress() + " is connected. *****");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
    }
}
