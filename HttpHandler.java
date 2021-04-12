package ThreadPoolImpl;
import java.io.*;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


public class HttpHandler implements Runnable {
    private Socket socket;
    private File directory;


    public HttpHandler(Socket socket, File directory) {
        this.socket = socket;
        this.directory=directory;
    }

    public void run() {
        try {
            handleRequest();
        } catch (Exception e) {
            System.err.println("Error Occured: " + e.getMessage());
            try {
                socket.close();
                System.exit(0);
            } catch (IOException e1) {
                System.err.println("Error Closing socket Connection.");
                System.exit(0);
            }
            System.err.println("Server is Terminating!");
        }
    }


    private void handleRequest() throws Exception {
        InputStream input;
        OutputStream output;
        if (directory.isDirectory()) {
            input = socket.getInputStream();
            output = socket.getOutputStream();
            serverRequest(input, output, directory.toString());
            output.close();
            input.close();
        } else {
            throw new Exception("src directory not present!");
        }
        socket.close();
    }

    private void serverRequest(InputStream input, OutputStream output, String root) throws Exception {
        String line;
        File resource = new File(directory+ "/Test.txt");
        BufferedReader bf = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

        while ((line = bf.readLine()) != null) {
            if (line.length() <= 0) {
                break;
            }
            boolean isPost = line.startsWith("POST");
            int contentLength = 0;
            while (!(line = bf.readLine()).equals("")) {
                 if (isPost) {
                    final String contentHeader = "Content-Length: ";
                    if (line.startsWith(contentHeader)) {
                        contentLength = Integer.parseInt(line.substring(contentHeader.length()));
                    }
                }
            }
            StringBuilder body = new StringBuilder();
            if (isPost) {
                int c;
                for (int i = 0; i < contentLength; i++) {
                    c = bf.read();
                    body.append((char) c);

                }
            }
            Files.writeString(resource.toPath(), body.toString(), StandardCharsets.UTF_8);
            populateResponse(resource,output);
            output.flush();
            break;
        }
    }


    private void populateResponse(File resource, OutputStream output) throws IOException {
        SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        String REQ_FOUND = "HTTP/1.0 200 OK\n";
        String SERVER = "Server: HTTP server/0.1\n";
        String DATE = "Date: " + format.format(new java.util.Date()) + "\n";
        String CONTENT_TYPE = "Content-type: " + URLConnection.guessContentTypeFromName(resource.getName());
        String LENGTH = "Content-Length: " + (resource.length()) + "\n\n";

        String header = REQ_FOUND + SERVER + DATE + CONTENT_TYPE + LENGTH;
        output.write(header.getBytes());

        Files.copy(Paths.get(resource.toString()), output);
        output.flush();
    }
}