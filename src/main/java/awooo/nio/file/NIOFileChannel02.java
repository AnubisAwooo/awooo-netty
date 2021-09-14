package awooo.nio.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NIOFileChannel02 {


    public static void main(String[] args)  {
        File file = new File("./test.txt");
        try (FileInputStream in = new FileInputStream(file);
             FileChannel channel = in.getChannel()){

            ByteBuffer buffer = ByteBuffer.allocate((int) file.length());

            channel.read(buffer);

            // 写模式下 position 当前位置序号就是容量
            // 写完成 切换成读模式 从头开始读 position = 0 一直可以读到原先写入的位置 limit
            buffer.flip();

            while (buffer.hasRemaining()) {
                System.out.print(new String(new byte[]{buffer.get()}));
            }

            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
