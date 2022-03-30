package com.company.service;

import com.company.infrastructure.FilesModel;
import com.company.infrastructure.NetworkCommands;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class FilesHandler implements Runnable
{
    private List<FilesModel> sendingFiles;
    private List<FilesModel> recivingFiles;
    private BufferedReader in;
    private PrintWriter out;
    private Socket serverSocket;
    private String directoryPath;

    public FilesHandler(Socket serverSocket, List<FilesModel> sendingFiles, List<FilesModel> recivingFiles, BufferedReader in, PrintWriter out, String directoryPath)
    {
        this.sendingFiles = sendingFiles;
        this.recivingFiles = recivingFiles;
        this.in = in;
        this.out = out;
        this.serverSocket = serverSocket;
        this.directoryPath = directoryPath;
    }

    @Override
    public void run()
    {
        /////////////////////////////////ODBIERANIE Z SERWERA/////////////////////////////////////////////////////

        try
        {
            out.println(NetworkCommands.SEND_FILES_TO_CLIENT.name());

            for(FilesModel file_m : recivingFiles) {

                String clientPath = directoryPath + "\\" + file_m.relativePath;

                File file = new File(clientPath);
                if (file.exists()) {

                    file.delete();
                    file.getParentFile().mkdirs(); // creates whole directory
                    // structure
                    file.createNewFile(); // creates the file

                } else {

                    file.getParentFile().mkdirs();
                    file.createNewFile();

                }

                out.println(NetworkCommands.SEND_FILES_TO_CLIENT.name());

                in.readLine(); // SEND_FILE_PATH
                out.println(file_m.relativePath);

                in.readLine(); // SENDING_FILESIZE_FROM_SERVER
                long fileSize = Long.parseLong(in.readLine()); // filesize

                in.readLine(); // SEND_FILE
                out.println("OK");

                int numberOfBytes = 0;
                byte[] buffer = new byte[4096];


                float wholeFileSize = fileSize;
                float receivedFileSize = numberOfBytes;
                System.out.println(" << File transfer started >> ");

                DataInputStream dataIn = new DataInputStream(serverSocket.getInputStream());
                FileOutputStream dataOut = new FileOutputStream(file);

                while (fileSize > 0) {
                    try {
                        this.serverSocket.setSoTimeout(0);
                        if ((numberOfBytes = dataIn.read(buffer, 0, (int)Math.min(buffer.length, fileSize))) > 0) {

                            System.out.println("Writing " + numberOfBytes + " bytes...");
                            dataOut.write(buffer, 0, numberOfBytes);
                            fileSize -= numberOfBytes;
                            receivedFileSize += numberOfBytes;
                            System.out.println("Done... " + (wholeFileSize - receivedFileSize) + " left");

                            System.out.println(String.format("Client restored: %.2f", 100 * (receivedFileSize/wholeFileSize)) + "%");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                this.serverSocket.setSoTimeout(0);

                System.out.println(" << File transfer finished >> ");
                dataOut.close();

            }

            out.println(NetworkCommands.RECEIVING_FINISHED.name());

            System.out.println("RECEIVING COMPLITED");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        /////////////////////////////////WYSYLANIE NA SERWER/////////////////////////////////////////////////////

        out.println(NetworkCommands.RECEIVE_FILES_FROM_CLIENT.name());

        for (FilesModel file_m : sendingFiles) {
            try {
                File tempFile = new File(file_m.absolutePath);

                out.println(NetworkCommands.RECEIVE_FILES_FROM_CLIENT.name());

                in.readLine(); // SEND_ME_FILEPATH
                String filepath = file_m.relativePath;
                out.println(filepath); // PATH

                in.readLine(); // SEND_ME_FILESIZE
                long fileSize = tempFile.length();
                out.println(fileSize); // SIZE

                in.readLine(); // SEND_ME_FILE

                int numberOfBytes = 0;
                byte[] buffer = new byte[4096];
                DataOutputStream dataOut = new DataOutputStream(serverSocket.getOutputStream());
                FileInputStream fis = new FileInputStream(tempFile);

                while (fileSize > 0 && (numberOfBytes = fis.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {

                    dataOut.write(buffer, 0, numberOfBytes);
                    fileSize -= numberOfBytes;

                }

                System.out.println(in.readLine());

                fis.close();

                Long hash = file_m.crcHash;
                out.println(hash);

                System.out.println(in.readLine());
                out.println(file_m.absolutePath);

                fis.close();

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }

        out.println(NetworkCommands.SENDING_FINISHED.name());

        System.out.println("SENDING COMPLITED");

    }
}