package awooo.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class NIOClient {

    public static void main(String[] args) throws IOException {
        SocketChannel channel = SocketChannel.open();

        channel.configureBlocking(false);

        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 6666);

        if (!channel.connect(address)) {
            while (!channel.finishConnect()) {
                System.out.println("connecting, do other things");
            }
        }

        ByteBuffer buffer = ByteBuffer.wrap("hello".getBytes(StandardCharsets.UTF_8));
        channel.write(buffer);

        System.out.println();
    }

}
