package awooo.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BIOServer {

    public static void main(String[] args) throws IOException {
        ExecutorService service = Executors.newCachedThreadPool();


        ServerSocket serverSocket = new ServerSocket(6666);

        System.out.println("server started");

        while (true) {

            System.out.println("waiting for connect");
            final Socket socket = serverSocket.accept();

            System.out.println("connect a client");

            service.execute(() -> handle(socket));

        }

    }

    private static void handle(Socket socket) {
        System.out.println("current thread -> " + Thread.currentThread().getName());
        
        byte[] bytes = new byte[1024];
        try (InputStream in = socket.getInputStream()){
            while (true) {
                int length = in.read(bytes);

                if (length != -1) {
                    System.out.print(new String(bytes, 0, length));
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
