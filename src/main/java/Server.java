import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import javax.validation.constraints.NotNull;
// import for ChannelPipeline
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class Server {
    public static void main(String[] args) throws Exception {
        // creates two multi-thread groups
        // BossGroup is responsible for listening and accepting new incoming connections from clients
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // WorkerGroup is responsible for handling the actual taks once the connection is established
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        // Configure Netty server side Initialization
        try {
            //创建服务端的启动对象，设置参数
            ServerBootstrap bootstrap = new ServerBootstrap();
            //设置两个线程组boosGroup和workerGroup
            bootstrap.group(bossGroup, workerGroup)
                    //设置服务端通道实现类型,这里使用 NIO 的实现类 NioServerSocketChannel
                    .channel(NioServerSocketChannel.class)
                    //设置服务端的 TCP 参数, 设置线程队列得到连接个数,SO_BACKLOG 表示服务端接受客户端连接的队列的最大长度
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //设置保持活动连接状态, SO_KEEPALIVE 表示是否启用 TCP 的 KeepAlive 机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //使用匿名内部类的形式初始化通道对象,为每个客户端连接创建一个新的 ChannelHandler 处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(@NotNull SocketChannel socketChannel) throws Exception {
                            //将 ServerHandler 处理器添加到管道中，用来处理客户端连接的读写事件
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                            pipeline.addLast("decoder", new StringDecoder());
                            pipeline.addLast("encoder", new StringEncoder());
                            pipeline.addLast(new ServerHandler());
                        }
                    });//给workerGroup的EventLoop对应的管道设置处理器
            System.out.println("Stock Exchange Server is online!");

            //ChannelFuture 是 Netty 中的一个异步 IO 操作结果的句柄，来监听异步操作结果
            //一个操作的结果通常是一个 ChannelFuture 对象，如：绑定、连接、写入、读取等

            //绑定端口号，启动服务端
            ChannelFuture channelFuture = bootstrap.bind(7788).sync();
            //对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        } finally {
            //关闭服务器的两个线程池，释放线程池占用的资源。
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}


