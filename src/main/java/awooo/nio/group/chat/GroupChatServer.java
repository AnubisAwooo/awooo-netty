package awooo.nio.group.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GroupChatServer {

    private Selector selector;

    public static void main(String[] args) {
        new GroupChatServer().run();
    }

    public GroupChatServer() {
        try {
            this.selector = Selector.open();
            ServerSocketChannel listener = ServerSocketChannel.open();
            int port = 6667;
            listener.bind(new InetSocketAddress(port));
            listener.configureBlocking(false);
            // 对于链接通道，只关心通道 是否可获取通道
            listener.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while (true) {
                _run();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void _run() throws IOException, InterruptedException {
        if (selector.select() <= 0) { return; }

        Set<SelectionKey> selectionKeys = selector.selectedKeys();

//        System.out.println("selection keys: " + selectionKeys.size());

        selectionKeys.removeIf(key -> !key.isValid());

        // 处理刚链接事件
        for (SelectionKey key : selectionKeys) {
            SocketChannel channel = handleAcceptable(key);
            if (null != channel) {
                // 并且移除本事件
                selectionKeys.remove(key);
            }
        }

        // 提取其他人发送的消息
        for (SelectionKey key : selectionKeys) {
            String message = handleReadable(key);
            if (null != message) {
                sendMessage(message, key, false);
            }
            selectionKeys.remove(key);
        }
    }

    private SocketChannel handleAcceptable(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            // 表明本通道已经准备好一个链接了
            ServerSocketChannel channel = (ServerSocketChannel) key.channel();
            SocketChannel accept = channel.accept();
            accept.configureBlocking(false);
            SelectionKey selectionKey = accept.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
            String message = new Date() + " <<< " + accept.getRemoteAddress() + " join group. welcome. <<<";
            sendMessage(message, selectionKey, true);
            return accept;
        }
        return null;
    }

    private String handleReadable(SelectionKey key) {
        if (!key.isReadable()) { return null; }
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
                return null;
            }
        } catch (Exception e) {
            close(key, channel);
            return null;
        }

        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') { sb.deleteCharAt(sb.length() - 1); }
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\r') { sb.deleteCharAt(sb.length() - 1); }

        return sb.toString();
    }

    private void sendMessage(String message, SelectionKey self, boolean system) {
        System.out.println("send message to others(" + (selector.keys().size() - 2) + "): " + message);
        ByteBuffer buffer = null;
        if (system) {
            buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)).asReadOnlyBuffer();
        } else {
            try {
                buffer = ByteBuffer.wrap(String.format("%s(%s) -> %s",
                                ((SocketChannel)self.channel()).getRemoteAddress(), new Date(), message)
                        .getBytes(StandardCharsets.UTF_8)).asReadOnlyBuffer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (SelectionKey key : selector.keys()) {
            if (key == self) { continue; }
            if (!key.isValid()) { continue; }
            if (key.channel() instanceof ServerSocketChannel) { continue; }
            SocketChannel channel = (SocketChannel) key.channel();
            try {
//                System.out.println("send message to " + channel.getRemoteAddress() + " " + message);
                channel.write(buffer);
                buffer.clear();
            } catch (IOException e) {
                e.printStackTrace();
                close(key, channel);
            }
        }
    }

    private void close(SelectionKey key, SocketChannel channel) {
        // 可能已经断开连接了
        String m = "";
        try {
            m = new Date() + " >>> " + channel.getRemoteAddress() + " leave group. bye bye. >>>";
            key.cancel();
            channel.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            sendMessage(m, key, true);
        }
    }

}
