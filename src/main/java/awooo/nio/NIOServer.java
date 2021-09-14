package awooo.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Set;

public class NIOServer {

    public static void main(String[] args) throws Exception {
        // 建立一个 socket 通道
        ServerSocketChannel socketChannel = ServerSocketChannel.open();

        // 建立一个选择器
        Selector selector = Selector.open();

        // 让通道绑定端口
        socketChannel.socket().bind(new InetSocketAddress(6666));
        // 设定 socket 通道为非阻塞模式
        socketChannel.configureBlocking(false);

        // 将 socket 通道注册到选择器里，关注 ACCEPT 事件
        socketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            // 本次循环尝试监听 1 秒钟
            if (selector.select(1000) == 0) {
                // 这 1 秒钟内没有事件发生 继续下次循环
                System.out.println("waiting for connecting");
                continue;
            }

            // 有关注的事件发生

            // 取出事件
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            // 对每件事情进行处理
            for (SelectionKey key : selectionKeys) {
                if (key.isAcceptable()) {
                    // 该事件是客户端链接 已经有连接了 直接取出通道 传统这个 accept 会阻塞
                    SocketChannel accept = socketChannel.accept();
                    // 将这个通道设置成非阻塞的
                    accept.configureBlocking(false);
                    // 将新开的连接通道注册到选择器 关注 read 事件  顺便给一个缓冲器
                    accept.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }
                if (key.isReadable()) {
                    // 如果是 read 事件 就表明用户输入完成 可以读了
                    // 取出通道
                    SocketChannel channel = (SocketChannel) key.channel();
                    // 取出之前附带的缓冲器
                    ByteBuffer buffer = (ByteBuffer) key.attachment();

                    channel.read(buffer);

                    System.out.println("from client: " + new String(buffer.array(), 0, buffer.position()));
                }

                selectionKeys.remove(key);
            }

        }
    }

}
