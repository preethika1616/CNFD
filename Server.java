package group.chatting.application;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server implements Runnable {
    
    private Socket socket;
    
    // Use a thread-safe collection for clients
    private static final List<BufferedWriter> clients = new CopyOnWriteArrayList<>();

    public Server(Socket socket) {
        this.socket = socket;
    }
    
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            
            clients.add(writer);
            
            String data;
            while ((data = reader.readLine()) != null) {
                System.out.println("Received: " + data.trim());
                
                for (BufferedWriter bw : clients) {
                    try {
                        bw.write(data);
                        
                        bw.write("\r\n");
                        bw.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Remove the client writer from the list
            clients.removeIf(bw -> {
                try {
                    bw.close();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            });
        }
    }

    public static void main(String[] args) {
        int port = 11005;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            
            while (true) {
                Socket socket = serverSocket.accept();
                Server server = new Server(socket);
                Thread thread = new Thread(server);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }}}