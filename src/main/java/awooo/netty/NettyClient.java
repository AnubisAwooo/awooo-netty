package awooo.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;

public class NettyClient {
    
    public static void main(String[] args) throws InterruptedException {
    
        EventLoopGroup eventExecutors = new NioEventLoopGroup();
    
        try {
            Bootstrap bootstrap = new Bootstrap();
        
            bootstrap.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                System.out.println("client: " + ctx);
                                ctx.writeAndFlush(Unpooled.copiedBuffer("hello, server", CharsetUtil.UTF_8));
                            }
    
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                System.out.println("message from server: " + buf.toString(StandardCharsets.UTF_8));
                                System.out.println("address from server: " + ctx.channel().remoteAddress());
                            }
    
                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                cause.printStackTrace();
                                ctx.close();
                            }
                        });
                    }
                });
            System.out.println("client is ready.");
        
            ChannelFuture future = bootstrap.connect("127.0.0.1", 6668).sync();
        
            future.channel().closeFuture().sync();
        } finally {
            eventExecutors.shutdownGracefully();
        }
    }
    
}
