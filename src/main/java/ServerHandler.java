import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import javax.validation.constraints.NotNull;


public class ServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 客户端向服务器端发送数据时，服务器端会自动调用该方法，以处理客户端发送的消息
     * @param ctx 上下文对象，含有通道channel，管道pipeline
     * @param msg 客户端发送的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //获取客户端发送过来的消息
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println("Receive message: " + ctx.channel().remoteAddress() + " from client. " + byteBuf.toString(CharsetUtil.UTF_8));
    }

    /**
     * 通道读取完毕时，会自动调用该方法。在这个方法中，服务器端会向客户端发送一条消息。
     * @param ctx 上下文对象，含有通道channel，管道pipeline
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //发送消息给客户端
        ctx.writeAndFlush(Unpooled.copiedBuffer("Server side sent a message ", CharsetUtil.UTF_8));
    }

    /**
     * 发生异常时，会自动调用该方法。关闭通道，释放相关的资源。
     * @param ctx 上下文对象，含有通道channel，管道pipeline
     * @param cause 发生的异常
     * @throws IllegalStateException if the system is in an illegal state to perform the operation
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //发生异常，关闭通道
        ctx.close();
    }
}