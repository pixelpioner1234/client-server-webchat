public class Main {
    public static void main(String[] args) {
        Server server;
        server = new Server(8084);
        server.listen();
    }
}