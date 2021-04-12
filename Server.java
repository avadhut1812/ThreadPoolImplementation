package ThreadPoolImpl;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private ServerSocket serverSocket;
    File directory;
    private CustomThreadPool threadPool =
            new CustomThreadPool(10);

    public static void main(String[] args) {
        try {
            if(args.length == 2) {
                int port= Integer.parseInt(args[0]);
                Server server = new Server(port,args[1]);
                server.start();
            }
            else{
                System.err.println("Please provide all command parameters");
                System.exit(0);
            }

        } catch (IOException e) {
            System.err.println("Error occured:" + e.getMessage());
            System.exit(0);
        }
    }

    public Server(int port,String dir) throws IOException {
        serverSocket = new ServerSocket(port);
        File tempDirectory = new File(System.getProperty("user.dir")+File.separator+dir);
        if(!tempDirectory.isDirectory()){
           tempDirectory.mkdir();
        }
        this.directory=tempDirectory;
    }

    /**
     * @throws IOException
     */
    private void start() throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();
            HttpHandler connection = new HttpHandler(socket,directory);
            this.threadPool.execute(
                    connection);
        }
    }



}