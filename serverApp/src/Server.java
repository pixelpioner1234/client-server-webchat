import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Server {
    private ServerSocket serverSocket;
    private List<ClientHandlerThread> clients = new ArrayList<>();
    private List<User> users = new ArrayList<>(); //

    public Server(int port) {
        try {
            this.serverSocket = new ServerSocket(port);

            users.add(new User("diana", "1111"));
            users.add(new User("john", "2222"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen(){
        System.out.println("Oczekiwanie...");
        while(true){
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
                ClientHandlerThread thread = new ClientHandlerThread(clientSocket, this);
                clients.add(thread);
                thread.start();
                System.out.println("New client joined!");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public User getUser(String login) {
        for (User user : users) {
            if (user.getLogin().equals(login)) {
                return user;
            }
        }
        return null;
    }

    public void removeClient(ClientHandlerThread client) {
        clients.remove(client);
        broadcastLogout(client);
    }

    public void broadcast(ClientHandlerThread sender, String message){
        for(var currentClient : clients)
            currentClient.send("BR"+sender.getClientName()+": "+message);

    }

    public void broadcastLogin(ClientHandlerThread client) {
        for(var currentClient : clients)
            if(currentClient != client)
                currentClient.send("LN"+client.getClientName());

    }

    public void broadcastLogout(ClientHandlerThread client) {
        for(var currentClient : clients)
            if(currentClient != client) {
                currentClient.send("LT" + client.getClientName());
                clients.remove(client);
            }
    }

    private Optional<ClientHandlerThread> getClient(String clientName) {
        return clients.stream()
                .filter(client -> clientName.equals(client.getClientName()))
                .findFirst();
    }

    public void whisper(ClientHandlerThread sender, String message) {
        String[] messageArr = message.split(" ");
        String recipientName = messageArr[0];

        Optional<ClientHandlerThread> recipient = getClient(recipientName);
        if(recipient.isPresent()) {
            recipient.get().send("WH"+sender.getClientName()+" "+messageArr[1]);
        }
        else sender.send("NU"+recipientName);
    }

    public void online(ClientHandlerThread sender) {
        String listString = clients.stream()
                .map(ClientHandlerThread::getClientName)
                .collect(Collectors.joining(" "));
        sender.send("ON"+listString);
    }

    public void sendFile(ClientHandlerThread sender, String message) throws IOException {
        String[] messageArr = message.split(" ");
        String recipientName = messageArr[0];
        long fileSize = Long.parseLong(messageArr[1]);
        String fileName = messageArr[2];

        Optional<ClientHandlerThread> recipient = getClient(recipientName);

        if(recipient.isPresent()) {
            DataInputStream fileIn = new DataInputStream(sender.getSocket().getInputStream());
            DataOutputStream fileOut = new DataOutputStream(recipient.get().getSocket().getOutputStream());

            byte[] buffer = new byte[64];
            long receivedSize = 0;
            int count;

            recipient.get().send("FI: "+sender.getClientName()+" "+fileSize+" "+fileName);
            while (receivedSize < fileSize) {
                count = fileIn.read(buffer);
                receivedSize += count;
                System.out.println(receivedSize+" "+(fileSize-receivedSize));
                fileOut.write(buffer, 0, count);
            }
        }

        else sender.send("NU: "+recipientName);

    }
}
