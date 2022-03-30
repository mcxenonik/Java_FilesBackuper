package com.inowak.service;

import com.inowak.infrastructure.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(Config.portNumber);
            while(true) {

                System.out.println("Server is listening on port: " + serverSocket.getLocalPort() + "\n");
                Socket socket = serverSocket.accept();
                System.out.println("Connection request from: " + socket.getInetAddress() + "\n");

                if (socket.isConnected()) {

                    System.out.println("Connection accepted\n");

                    ClientHandler client = new ClientHandler(socket);


                    if (client.authenticate()) {

                        Thread thread = new Thread(client);
                        thread.start();

                        System.out.println("Access granted\n");

                    } else {

                        socket.close();

                        System.out.println("Access denied\n");

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
