package awooo.netty.group.chat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GroupChatServer {
    
    public static void main(String[] args) throws InterruptedException {
        new GroupChatServer(6669).run();
    }
    
    private int port;
    private ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    
    public GroupChatServer(int port) {
        this.port = port;
    }
    
    private void run() throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
        
            serverBootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("decoder", new StringDecoder());
                        pipeline.addLast("encoder", new StringEncoder());
                        pipeline.addLast("mine", new SimpleChannelInboundHandler<String>() {
                            @Override
                            public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                                // 连接建立
                                Channel channel = ctx.channel();
                                channelGroup.writeAndFlush(current() + " System: client " + channel.remoteAddress() + " join group. Welcome.");
                                channelGroup.add(channel);
                            }
    
                            @Override
                            public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
                                Channel channel = ctx.channel();
                                channelGroup.remove(channel);
                                channelGroup.writeAndFlush(current() + " System: client " + channel.remoteAddress() + " leave group. Bye Bye.");
                            }
                            
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                String message = current() + " " + ctx.channel().remoteAddress() + ": " + msg;
                                channelGroup.stream().filter(channel -> ctx.channel() != channel).forEach(channel -> {
                                    channel.writeAndFlush(message);
                                });
                            }
    
                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                cause.printStackTrace();
                                ctx.close();
                            }
                        });
                    }
                });
        
            ChannelFuture future = serverBootstrap.bind(port).sync();
            System.out.println("server is ready.");
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
    
    public static String current() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
    
}
