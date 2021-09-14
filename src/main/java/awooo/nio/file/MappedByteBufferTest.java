package awooo.nio.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MappedByteBufferTest {

    public static void main(String[] args) throws IOException {
        File file = new File("./test.txt");
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

        FileChannel channel = randomAccessFile.getChannel();

        MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, 5);

        map.put(0, (byte) '5');

        channel.close();

        randomAccessFile.close();

        file.setLastModified(System.currentTimeMillis());
    }

}
