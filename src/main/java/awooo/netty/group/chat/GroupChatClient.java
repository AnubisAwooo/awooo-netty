package awooo.netty.group.chat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

public class GroupChatClient {
    
    public static void main(String[] args) {
        GroupChatClient chatClient = new GroupChatClient("127.0.0.1", 6669);
        new Thread(chatClient::run).start();
        while (true) {
            Scanner scanner = new Scanner(System.in);
            String message = scanner.nextLine();
            chatClient.send(message);
        }
    }
    
    private String host;
    private int port;
    private Channel channel;
    
    public GroupChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    private void run() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                            .addLast("decoder", new StringDecoder())
                            .addLast("encoder", new StringEncoder())
                            .addLast("mine", new SimpleChannelInboundHandler<String>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                    System.out.println(msg);
                                }
    
                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    cause.printStackTrace();
                                    ctx.close();
                                }
                            });
                    }
                });
            ChannelFuture future = bootstrap.connect(host, port).sync();
            System.out.println("client is ready.");
            this.channel = future.channel();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
    
    private void send(String message) {
        if (null == channel) { return; }
        if (message.endsWith("\n")) { message = message.substring(0, message.length() - 1); }
        if (message.endsWith("\r")) { message = message.substring(0, message.length() - 1); }
        channel.writeAndFlush(message);
    }
    
}
