package com.company.service;

import com.company.infrastructure.Config;
import com.company.infrastructure.FilesModel;
import com.company.infrastructure.NetworkCommands;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerHandler {

    Socket serverSocket;

    private BufferedReader in;
    private PrintWriter out;

    public ServerHandler(Socket socket) throws IOException
    {
        this.serverSocket = socket;
        in = new BufferedReader(new InputStreamReader(this.serverSocket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

    }

    private void sendCommand(NetworkCommands command)
    {

        out.println(command.name());
        System.out.println("Transmitted: " + command.name());

    }

    private NetworkCommands receiveCommand() throws IOException
    {

        String received = in.readLine();
        NetworkCommands result = NetworkCommands.NOT_RECOGNISED;

        for (NetworkCommands i : NetworkCommands.values()) {

            if (i.name().equals(received)) {
                System.out.println("Received: " + i);
                return i;
            }
        }

        System.out.println("Received: " + result);
        return result;
    }

    public boolean authenticate() throws IOException
    {

        if (receiveCommand() == NetworkCommands.SEND_PASSWORD) {

            out.println(Config.password);

            if (receiveCommand() == NetworkCommands.ACCESS_GRANTED) {

                System.out.println("Authentication successful\n");
                return true;

            } else {

                System.out.println("Authentication failed\n");

            }
        }

        return false;
    }

    public List<FilesModel> getServerFileList()
    {

        List<FilesModel> files = new ArrayList<>();

        try {

            sendCommand(NetworkCommands.SEND_FILE_LIST);

            while(!in.readLine().equals("READY")) { // SENDING_FILE_LIST

                in.readLine(); // SENDING_FILE_PATH
                String relativePath = in.readLine();

                in.readLine(); // SENDING_FILE_HASH
                long crcHash = Long.parseLong(in.readLine());

                in.readLine(); // SENDING_FILE_ABSOLUTE_PATH
                String absolutePath = in.readLine();

                FilesModel file = new FilesModel(absolutePath, relativePath, crcHash);

                files.add(file);

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return files;

    }

    public void deleteFileFromServer(String file)
    {
        try
        {
            sendCommand(NetworkCommands.DELETE_FILE_FROM_SERVER);

            out.println(file);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void synchronize(List<FilesModel> sendingFiles, List<FilesModel> receivingFiles, String directoryPath)
    {
        FilesHandler filesHandler = new FilesHandler(serverSocket, sendingFiles, receivingFiles, in, out, directoryPath);
        Thread filesThread = new Thread(filesHandler);
        filesThread.start();
    }

}
