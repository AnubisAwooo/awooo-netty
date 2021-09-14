package awooo.nio.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileCopy {

    public static void main(String[] args) {
        File file1 = new File("./test.txt");
        File file2 = new File("./test2.txt");

        try (FileInputStream in = new FileInputStream(file1);
             FileOutputStream out = new FileOutputStream(file2, false);
             FileChannel inChannel = in.getChannel();
             FileChannel outChannel = out.getChannel()){

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (inChannel.read(buffer) != -1) {
                buffer.flip();
                outChannel.write(buffer);
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
