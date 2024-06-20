import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandlerThread extends Thread {
    public Socket getSocket() {
        return socket;
    }

    private Socket socket;
    private PrintWriter writer;
    private Server server;
    private String clientName = null;

    public ClientHandlerThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void run(){
        try {
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            writer = new PrintWriter(output, true);
            String message;

            // Ожидаем логин и пароль
            while ((message = reader.readLine()) != null) {
                if (message.startsWith("LO")) {
                    String[] credentials = message.substring(2).split(" ");
                    if (credentials.length == 2) {
                        String login = credentials[0];
                        String password = credentials[1];
                        if (authenticate(login, password)) {
                            login(login);
                            break;
                        } else {
                            send("ERR: Incorrect username or password");
                        }
                    }
                }
            }



            while ((message = reader.readLine()) != null){
                String prefix = message.substring(0,2);
                String postfix = message.substring(2);
                switch(prefix) {
                    case "LO" -> login(postfix);
                    case "BR" -> server.broadcast(this,postfix);
                    case "WH" -> server.whisper(this,postfix);
                    case "ON" -> server.online(this);
                    case "FI" -> server.sendFile(this,postfix);
                }

                System.out.println(message);
            }
            System.out.println("closed");
            server.removeClient(this);
        }
        catch(SocketException e){
            server.broadcastLogout(this);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean authenticate(String login, String password) {
        User user = server.getUser(login);
        return user != null && user.getPassword().equals(password);
    }


    public void send(String message){
        writer.println(message);
    }

    public String getClientName() {
        return clientName;
    }

    public void login(String name) {
        clientName = name;
        server.online(this);
        server.broadcastLogin(this);
    }


}

