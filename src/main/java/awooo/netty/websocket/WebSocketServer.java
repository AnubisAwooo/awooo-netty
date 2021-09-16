package awooo.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.time.LocalDateTime;

public class WebSocketServer {
    
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        
        try {
            // 创建服务端启动对象
            ServerBootstrap bootstrap = new ServerBootstrap();
            
            bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                            .addLast(new HttpServerCodec())
                            .addLast(new ChunkedWriteHandler())
                            .addLast(new HttpObjectAggregator(8192))
                            .addLast(new WebSocketServerProtocolHandler("/hello"))
                            .addLast(new SimpleChannelInboundHandler<TextWebSocketFrame>() {
                                @Override
                                public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("client connected: " + ctx.channel().id().asLongText());
                                }
    
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
                                    System.out.println("server receive: " + msg.text());
                                    
                                    ctx.channel().writeAndFlush(new TextWebSocketFrame("server time: " + LocalDateTime.now() + " " + msg.text()));
                                }
    
                                @Override
                                public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("client disconnected: " + ctx.channel().id().asLongText());
                                }
    
                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    cause.printStackTrace();
                                    ctx.close();
                                }
                            });
                    }
                });
            
            System.out.println("server is ready.");
            
            ChannelFuture future = bootstrap.bind(8668).sync();
            
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
    
}
