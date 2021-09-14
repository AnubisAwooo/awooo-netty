package awooo.nio;

import java.nio.IntBuffer;

public class BasicBuffer {
    public static void main(String[] args) {
        IntBuffer intBuffer = IntBuffer.allocate(5);

        for (int i = 0; i < intBuffer.capacity(); i++) {
            System.out.println("put -> " + i);
            intBuffer.put(i);
        }

        System.out.println("--------------------");

        // buffer 读写切换
        intBuffer.flip();

        while (intBuffer.hasRemaining()) {
            System.out.println("get -> " + intBuffer.get());
        }

    }
}
