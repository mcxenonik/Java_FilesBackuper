package com.inowak.service;

import com.inowak.infrastructure.FilesModel;
import com.inowak.infrastructure.NetworkCommands;
import com.inowak.infrastructure.Config;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

public class ClientHandler implements Runnable{

    private BufferedReader in;
    private PrintWriter out;
    private Socket clientSocket;

    public ClientHandler(Socket socket) throws IOException {

        this.clientSocket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

    }

    @Override
    public void run()
    {

        NetworkCommands clientCommand = NetworkCommands.READY;

        while (!clientCommand.equals(NetworkCommands.EXIT))
        {
            clientCommand = receiveCommand();

            switch (clientCommand)
            {
                case SEND_FILE_LIST:

                    sendLocalFileList();
                    break;

                case RECEIVE_FILES_FROM_CLIENT:

                    receiveFilesFromClient();
                    break;

                case SEND_FILES_TO_CLIENT:

                    sendFilesToClient();
                    break;

                case DELETE_FILE_FROM_SERVER:

                    try {
                        deleteFileFromServer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                default:

                    break;
            }
        }
    }

    private void sendCommand(NetworkCommands command) {
        out.println(command.name());
        System.out.println(command.name());
    }

    private NetworkCommands receiveCommand() {
        String received = null;
        try {
            received = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        NetworkCommands result = NetworkCommands.NOT_RECOGNISED;
        for (NetworkCommands i : NetworkCommands.values()) {

            if (i.name().equals(received))
            {
                return i;
            }
        }
        return result;
    }

    public boolean authenticate() throws IOException {
        sendCommand(NetworkCommands.SEND_PASSWORD);
        if (in.readLine().equals(Config.password)) {
            sendCommand(NetworkCommands.ACCESS_GRANTED);
            return true;
        } else {
            sendCommand(NetworkCommands.ACCESS_DENIED);
            return false;
        }
    }

    private void sendLocalFileList() {

        List<FilesModel> localFiles = new ArrayList<>();
        List<File> temp = new ArrayList<>();

        File dir = new File(String.valueOf(Config.workingDirectory));

        String relativePath;
        long hash;

            for (File i : getFileList(dir, temp)) {

                relativePath = new File(String.valueOf(Config.workingDirectory)).toURI().relativize(new File(i.getAbsolutePath()).toURI()).getPath();

                hash = crc32hash(String.valueOf(i.getAbsolutePath()));

                localFiles.add(new FilesModel(i.getAbsolutePath(), relativePath, hash));
            }

            for (FilesModel fm : localFiles)
            {
            sendCommand(NetworkCommands.SENDING_FILE_LIST);

            sendCommand(NetworkCommands.SENDING_FILE_PATH);
            out.println(fm.relativePath);

            sendCommand(NetworkCommands.SENDING_FILE_HASH);
            out.println(fm.crcHash);

            sendCommand(NetworkCommands.SENDING_FILE_ABSOLUTE_PATH);
            out.println(fm.absolutePath);
            }

            sendCommand(NetworkCommands.READY);
    }

    /*
    private void sendLocalFileList() {

        for (FilesModel fm : Config.serverFiles)
        {
            sendCommand(NetworkCommands.SENDING_FILE_LIST);

            sendCommand(NetworkCommands.SENDING_FILE_PATH);
            out.println(fm.relativePath);

            sendCommand(NetworkCommands.SENDING_FILE_HASH);
            out.println(fm.crcHash);

            sendCommand(NetworkCommands.SENDING_FILE_ABSOLUTE_PATH);
            out.println(fm.absolutePath);
        }

        sendCommand(NetworkCommands.READY);
    }
    */

    private List<File> getFileList(File currentDirectory, List<File> files) {

        File[] listOfFiles = currentDirectory.listFiles();

        for(File i : listOfFiles) {

            if (i.isFile()) {

                files.add(i);

            } else if (i.isDirectory()) {

                getFileList(i, files);

            }
        }
        return files;
    }

    public static long crc32hash(String filepath) {

        CRC32 crc = new CRC32();
        try {
            InputStream inputStream = new BufferedInputStream(new FileInputStream(filepath));
            int cnt;

            while ((cnt = inputStream.read()) != -1) {

                crc.update(cnt);

            }
            inputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return crc.getValue();
    }

    public void receiveFilesFromClient()
    {
        String clientPath = "";
        String serverPath = "";
        String absolutePath = "";
        Long hashOnServer;
        Long hashOnClient;

        try
        {
            while (!receiveCommand().equals(NetworkCommands.SENDING_FINISHED))
            {
                sendCommand(NetworkCommands.SEND_FILE_PATH);
                clientPath = in.readLine();

                serverPath = Config.workingDirectory + "\\" + clientPath;

                File file = new File(serverPath);
                if (file.exists()) {

                    file.delete();
                    file.getParentFile().mkdirs(); // creates whole directory
                    // structure
                    file.createNewFile(); // creates the file

                } else {

                    file.getParentFile().mkdirs();
                    file.createNewFile();

                }

                sendCommand(NetworkCommands.SEND_FILE_SIZE);
                long fileSize = Long.parseLong(in.readLine());

                sendCommand(NetworkCommands.SEND_FILE);
                int numberOfBytes = 0;
                byte[] buffer = new byte[4096];

                // http://stackoverflow.com/questions/10367698/java-multiple-file-transfer-over-socket
                DataInputStream dataIn = new DataInputStream(this.clientSocket.getInputStream());
                FileOutputStream dataOut = new FileOutputStream(file);

                long wholeFileSize = fileSize;
                long receivedFileSize = numberOfBytes;
                System.out.println(" << File transfer started >> ");

                while (fileSize > 0
                        && (numberOfBytes = dataIn.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) > 0) {

                    dataOut.write(buffer, 0, numberOfBytes);
                    fileSize -= numberOfBytes;

                    receivedFileSize += numberOfBytes;
                    System.out.println(
                            (String.format("Main backuped: %.2f", 100 * ((float) receivedFileSize / (float) wholeFileSize))) + "%");

                }

                System.out.println(" << File transfer finished >> ");
                dataOut.close();

                sendCommand(NetworkCommands.SEND_FILE_HASH);
                hashOnClient = Long.parseLong(in.readLine());
                hashOnServer = crc32hash(String.valueOf(file.getAbsolutePath()));

                sendCommand(NetworkCommands.SEND_FILE_PATH);
                absolutePath = in.readLine();

                //Config.serverFiles.add(new FilesModel(absolutePath, clientPath, hashOnClient));

                //System.out.println("serverHash > " + hashOnServer);
                //System.out.println("serverHash > " + hashOnClient);

                if (hashOnServer.equals(hashOnClient)) {

                    System.out.println(" << File sent succesfully >> ");

                } else {

                    System.out.println(" << File was corrupted during transfer >> ");

                }
            }

            System.out.println("RECEIVING COMPLITED");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void sendFilesToClient()
    {
        try {

            while (!receiveCommand().equals(NetworkCommands.RECEIVING_FINISHED)) {

                sendCommand(NetworkCommands.SEND_FILE_PATH);

                String clientPath = in.readLine();
                String serverPath = Config.workingDirectory + "\\" + clientPath;
                System.out.println(serverPath);
                File file = new File(serverPath);

                long fileSize = file.length();
                sendCommand(NetworkCommands.SENDING_FILE_SIZE);
                out.println(fileSize);

                sendCommand(NetworkCommands.SEND_FILE);
                in.readLine(); // OK

                int numberOfBytes = 0;
                byte[] buffer = new byte[4096];
                FileInputStream fis = new FileInputStream(file);
                DataOutputStream dataOut = new DataOutputStream(this.clientSocket.getOutputStream());

                double wholeFileSize = fileSize;
                double alreadySentSize = 0;
                while (fileSize > 0
                        && (numberOfBytes = fis.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {

                    System.out.println("Writing " + numberOfBytes + " bytes...");
                    dataOut.write(buffer, 0, numberOfBytes);
                    System.out.println("Done...");
                    fileSize -= numberOfBytes;

                    alreadySentSize += numberOfBytes;
                    System.out.println(
                            (String.format("Main restored: %.2f", 100 * ((float) alreadySentSize / (float) wholeFileSize))) + "%");


                }

                fis.close();

                System.out.println("Main restored finished");

            }

            System.out.println("SENDING COMPLITED");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void deleteFileFromServer() throws IOException
    {
        File file = new File(Config.workingDirectory + "\\" + in.readLine());
        file.delete();

        System.out.println("File deleted");
    }

}
