import java.io.*;
import java.net.Socket;
import java.nio.file.Path;

public class ServerHandlerThread extends Thread {
    private Socket socket;
    private PrintWriter writer;
    private boolean running;

    public ServerHandlerThread(String address, int port) {
        try {
            socket = new Socket(address, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        try {
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            writer = new PrintWriter(output, true);
            String message;
            while ((message = reader.readLine()) != null){
                    System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void send(String message){
        writer.println(message);
    }

    public void login(String name) {
        writer.println("LO"+name);
    }

    public void broadcast(String message) {
        writer.println("BR"+message);
    }

    public void whisper(String message) {
        writer.println("WH"+message);
    }

    public void online() {
        writer.println("ON");
    }


}
