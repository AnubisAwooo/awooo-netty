package awooo.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class ScatteringAndGatheringTest {

    public static void main(String[] args) throws Exception {
        ServerSocketChannel channel = ServerSocketChannel.open();

        InetSocketAddress inetSocketAddress = new InetSocketAddress(7000);

        channel.socket().bind(inetSocketAddress);

        ByteBuffer[] buffers = new ByteBuffer[2];

        buffers[0] = ByteBuffer.allocate(5);
        buffers[1] = ByteBuffer.allocate(3);

        SocketChannel socketChannel = channel.accept();

        while (true) {
            int byteRead = 0;
            while (byteRead < 8) {
                byteRead += socketChannel.read(buffers);
                Arrays.stream(buffers)
                        .map(b -> "p=" + b.position() + " l=" + b.limit())
                        .forEach(System.out::println);
            }
            Arrays.stream(buffers).forEach(ByteBuffer::flip);

            int out = 0;
            while (out < 8) {
                out += socketChannel.write(buffers);
            }

            Arrays.stream(buffers).forEach(ByteBuffer::clear);
        }

    }

}
