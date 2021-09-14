package awooo.nio.file;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class NIOFileChannel01 {


    public static void main(String[] args)  {
        try (FileOutputStream os = new FileOutputStream("./test.txt", false);
             FileChannel channel = os.getChannel()){

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            buffer.put("1234567890".getBytes(StandardCharsets.UTF_8));

            // 写模式下 position 当前位置序号就是容量
            // 写完成 切换成读模式 从头开始读 position = 0 一直可以读到原先写入的位置 limit
            buffer.flip();

            channel.write(buffer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
