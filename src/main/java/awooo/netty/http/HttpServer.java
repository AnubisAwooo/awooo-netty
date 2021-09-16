package awooo.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

public class HttpServer {
    
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            
            bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        
                        // 先过滤一层 http 解码编码
                        pipeline.addLast("MyHttpServerCodec", new HttpServerCodec());
                        
                        pipeline.addLast("MyHttpServerHandler", new SimpleChannelInboundHandler<HttpObject>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
                                if (msg instanceof HttpRequest) {
                                    ByteBuf content = Unpooled.copiedBuffer("hello, I'm server.", StandardCharsets.UTF_8);
    
                                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
                                    
                                    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
                                    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
                                    
                                    ctx.writeAndFlush(response);
                                }
                            }
                        });
                        
                    }
                });
    
            ChannelFuture future = bootstrap.bind(6668).sync();
            
            future.channel().closeFuture().sync();
    
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
    
}
