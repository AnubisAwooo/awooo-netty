package awooo.nio.group.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Set;

public class GroupChatClient {

    private int port = 6667;

    private Selector selector;

    private SocketChannel channel;

    public static void main(String[] args) throws IOException {
        GroupChatClient client = new GroupChatClient();
        new Thread(client::run).start();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            client.channel.write(ByteBuffer.wrap(input.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public GroupChatClient() {
        try {
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            if (!channel.connect(new InetSocketAddress("127.0.0.1", port))) {
                while (!channel.finishConnect()) {
                    System.out.println("connecting, do other things");
                }
            }
            channel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while (true) {
                _run();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void _run() throws IOException {
        if (selector.select() < 0) { return; }

        Set<SelectionKey> selectionKeys = selector.selectedKeys();

        if (selectionKeys.isEmpty()) { return; }

//        System.out.println("selection keys: " + selectionKeys.size());

        for (SelectionKey key : selectionKeys) {
            handleRead(key);
//            handleWrite(key);
            selectionKeys.remove(key);
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        if (!key.isReadable()) { return; }
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        StringBuilder sb = new StringBuilder();
        try {
            buffer.clear();
            while (channel.read(buffer) > 0) {
                buffer.flip();
                sb.append(new String(buffer.array(), 0, buffer.limit()));
                buffer.clear();
            }
            if (sb.length() == 0) {
                close(key, channel);
                return ;
            }
        } catch (Exception e) {
            close(key, channel);
            return ;
        }

        System.out.println(sb);
    }

    private void close(SelectionKey key, SocketChannel channel) throws IOException {
        key.cancel();
        channel.close();
        System.out.println("group server is down.");
        System.exit(0);
    }


}
