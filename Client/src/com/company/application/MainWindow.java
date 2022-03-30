package com.company.application;

import com.company.infrastructure.Config;
import com.company.infrastructure.FilesModel;
import com.company.service.ServerHandler;

import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.zip.CRC32;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.DirectoryChooser;

public class MainWindow extends JFrame implements ActionListener {

    private JPanel mainPanel;
    private JButton addDirectoryButton;
    private JComboBox localFilesComboBox;

    private DefaultListModel serverFilesListModel = new DefaultListModel();
    private DefaultListModel serverFilesCategoryListModel = new DefaultListModel();
    private DefaultListModel serverFilesActionListModel = new DefaultListModel();
    private DefaultListModel localFilesListModel = new DefaultListModel();
    private DefaultListModel localFilesCategoryListModel = new DefaultListModel();
    private DefaultListModel localFilesActionListModel = new DefaultListModel();

    private JList serverFilesList;
    private JButton compareButton;
    private JButton synchronizeButton;
    private JButton showAllServerFilesButton;
    private JRadioButton twoWayRadioButton;
    private JRadioButton updateServerRadioButton;
    private JList localFilesList;
    private JList localFilesCategory;
    private JList serverFilesAction;
    private JList localFilesAction;
    private JList serverFilesCategory;
    private JLabel localFilesCategoryLabel;
    private JLabel serverFilesCategoryLabel;
    private JLabel localFilesActionLabel;
    private JLabel serverFilesActionLabel;
    private JLabel localFilesLabel;
    private JRadioButton updateLocalRadioButton;
    private JButton deleteFileOnServerButton;
    private JComboBox serverFilesComboBox;

    private List<FilesModel> filesToSend;
    private List<FilesModel> filesToDownload;

    public MainWindow(ServerHandler serverHandler)
    {
        setTitle("Awesome Backup Client");
        setSize(1000,1000);
        setLayout(null);

        //openWindow();
        this.setContentPane(mainPanel);

        addDirectoryButton.addActionListener(this);
        showAllServerFilesButton.addActionListener(this);
        synchronizeButton.addActionListener(this);
        synchronizeButton.setEnabled(false);
        compareButton.addActionListener(this);
        compareButton.setEnabled(false);
        deleteFileOnServerButton.addActionListener(this);
        deleteFileOnServerButton.setEnabled(false);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(twoWayRadioButton);
        buttonGroup.add(updateServerRadioButton);
        buttonGroup.add(updateLocalRadioButton);

        twoWayRadioButton.setSelected(true);

        new JFXPanel(); //JavaFX init

    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();

        if(source == addDirectoryButton)
        {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            Platform.runLater(new Runnable() {
                @Override
                public void run()
                {
                    File directory = directoryChooser.showDialog(null);
                    if (directory != null)
                    {
                        localFilesComboBox.addItem(directory);
                        localFilesComboBox.setSelectedItem(directory);
                        localFilesAction.setModel(new DefaultListModel());
                        localFilesCategory.setModel(new DefaultListModel());
                        localFilesList.setModel(new DefaultListModel());
                        serverFilesList.setModel(new DefaultListModel());
                        serverFilesComboBox.removeAllItems();

                        compareButton.setEnabled(true);
                    }
                }
            });
        }

        else if(source == showAllServerFilesButton)
        {
            try
            {
                List<FilesModel> files = getServerHandler().getServerFileList();
                serverFilesListModel.removeAllElements();
                serverFilesComboBox.removeAllItems();

                for(int currentFile = 0; currentFile < files.size(); currentFile++) {

                    serverFilesListModel.addElement(files.get(currentFile).relativePath);
                    serverFilesComboBox.addItem(files.get(currentFile).relativePath);
                }

                serverFilesList.setModel(serverFilesListModel);

            }
            catch (Exception es)
            {

                es.printStackTrace();
            }

            deleteFileOnServerButton.setEnabled(true);
        }

		else if(source == compareButton)
        {
            try
            {
                File directory = new File(localFilesComboBox.getSelectedItem().toString());

                List<FilesModel> tempServerFiles = getServerHandler().getServerFileList();
                List<FilesModel> tempLocalFiles = getAllLocalFiles(directory);

                showFiles(tempServerFiles, tempLocalFiles);

                compareFiles(tempServerFiles, tempLocalFiles);
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }

            synchronizeButton.setEnabled(true);
            deleteFileOnServerButton.setEnabled(true);
        }

		else if(source == synchronizeButton)
        {
            try {
                File directory = new File(localFilesComboBox.getSelectedItem().toString());

                getServerHandler().synchronize(filesToSend, filesToDownload, directory.getAbsolutePath());

            }
            catch (Exception es)
            {
                es.printStackTrace();
            }

            synchronizeButton.setEnabled(false);
            JOptionPane.showMessageDialog(this, "Synchronization completed");
        }

		else if(source == deleteFileOnServerButton)
        {
            try
            {
                if(serverFilesComboBox.getSelectedItem() == null)
                {
                    JOptionPane.showMessageDialog(this, "First, choose file");
                }
                else
                {
                    getServerHandler().deleteFileFromServer(serverFilesComboBox.getSelectedItem().toString());
                }

                JOptionPane.showMessageDialog(this, "File deleted");
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    public long isInListCRC (List<FilesModel> list, FilesModel file)
    {
        for (int currentFile = 0; currentFile < list.size(); currentFile++)
        {
            String name_cf = Paths.get(list.get(currentFile).relativePath).getFileName().toString();
            String name_f = Paths.get(file.relativePath).getFileName().toString();

            if (name_cf.equals(name_f))
            {
                return list.get(currentFile).crcHash;
            }
        }

        return -1;
    }

    public void compareFiles(List<FilesModel> serverFiles, List<FilesModel> localFiles) throws IOException
    {
        serverFilesCategoryListModel.removeAllElements();
        serverFilesActionListModel.removeAllElements();
        localFilesCategoryListModel.removeAllElements();
        localFilesActionListModel.removeAllElements();

        filesToDownload = new ArrayList<>();
        filesToSend = new ArrayList<>();

        for(int currentFile = 0; currentFile < serverFiles.size(); currentFile++)
        {
            long crc = isInListCRC(localFiles, serverFiles.get(currentFile));
            if(crc != -1)
            {
                if(serverFiles.get(currentFile).crcHash == crc)
                {
                    serverFilesCategoryListModel.addElement("Both sides");
                    serverFilesActionListModel.addElement("Dont move");
                }
                else
                {
                    serverFilesCategoryListModel.addElement("On left but changed");

                    if(updateServerRadioButton.isSelected())
                    {
                        serverFilesActionListModel.addElement("Dont move");
                    }
                    else if(twoWayRadioButton.isSelected())
                    {
                        serverFilesActionListModel.addElement("Dont move");
                    }
                    else if(updateLocalRadioButton.isSelected())
                    {
                        serverFilesActionListModel.addElement("Move on left");
                        filesToDownload.add(serverFiles.get(currentFile));
                    }
                }
            }
            else
            {
                serverFilesCategoryListModel.addElement("Only on right");

                if(updateServerRadioButton.isSelected())
                {
                   serverFilesActionListModel.addElement("Dont move");
                }
                else if (twoWayRadioButton.isSelected())
                {
                    serverFilesActionListModel.addElement("Move on left");
                    filesToDownload.add(serverFiles.get(currentFile));
                }
                else if (updateLocalRadioButton.isSelected())
                {
                    serverFilesActionListModel.addElement("Move on left");
                    filesToDownload.add(serverFiles.get(currentFile));
                }
            }
        }

        for(int currentFile = 0; currentFile < localFiles.size(); currentFile++)
        {
            long crc = isInListCRC(serverFiles, localFiles.get(currentFile));
            if(crc != -1)
            {
                if(localFiles.get(currentFile).crcHash == crc)
                {
                    localFilesCategoryListModel.addElement("Both sides");
                    localFilesActionListModel.addElement("Dont move");
                }
                else
                {
                    localFilesCategoryListModel.addElement("On right but changed");

                    if(updateServerRadioButton.isSelected())
                    {
                        localFilesActionListModel.addElement("Move right");
                        filesToSend.add(localFiles.get(currentFile));
                    }
                    else if (twoWayRadioButton.isSelected())
                    {
                        localFilesActionListModel.addElement("Dont move");
                    }
                    else if(updateLocalRadioButton.isSelected())
                    {
                        localFilesActionListModel.addElement("Dont move");
                    }
                }
            }
            else
            {
                localFilesCategoryListModel.addElement("Only on left");

                if(updateServerRadioButton.isSelected())
                {
                    localFilesActionListModel.addElement("Move right");
                    filesToSend.add(localFiles.get(currentFile));
                }
                else if (twoWayRadioButton.isSelected())
                {
                    localFilesActionListModel.addElement("Move right");
                    filesToSend.add(localFiles.get(currentFile));
                }
                else if (updateLocalRadioButton.isSelected())
                {
                    localFilesActionListModel.addElement("Dont move");

                }
            }
        }

        serverFilesCategory.setModel(serverFilesCategoryListModel);
        serverFilesAction.setModel(serverFilesActionListModel);
        localFilesCategory.setModel(localFilesCategoryListModel);
        localFilesAction.setModel(localFilesActionListModel);
    }

    public void showFiles (List<FilesModel> serverFiles, List<FilesModel> localFiles)
    {
        serverFilesListModel.removeAllElements();
        localFilesListModel.removeAllElements();

        serverFilesComboBox.removeAllItems();

        for(int currentFile = 0; currentFile < serverFiles.size(); currentFile++) {

            serverFilesListModel.addElement(serverFiles.get(currentFile).relativePath);
            serverFilesComboBox.addItem(serverFiles.get(currentFile).relativePath);
        }

        for(int currentFile = 0; currentFile < localFiles.size(); currentFile++) {

            localFilesListModel.addElement(localFiles.get(currentFile).relativePath);
        }

        serverFilesList.setModel(serverFilesListModel);
        localFilesList.setModel(localFilesListModel);
    }

    public ServerHandler getServerHandler() throws Exception
    {
        Socket serverSocket = new Socket(Config.serverAddress, Config.portNumber);
        ServerHandler serverHandler = new ServerHandler(serverSocket);
        serverHandler.authenticate();

        return  serverHandler;
    }

    public List<FilesModel> getAllLocalFiles (File directory) throws IOException {
        List<FilesModel> localFiles = new ArrayList<>();
        List<File> temp = new ArrayList<>();

        String absolutePath;
        String relativePath;
        long hash;

        for (File i : getFileList(directory, temp)) {

            absolutePath = i.getAbsolutePath();

            relativePath = new File(String.valueOf(directory)).toURI().relativize(new File(i.getAbsolutePath()).toURI()).getPath();

            hash = crc32hash(String.valueOf(i.getAbsolutePath()));

            localFiles.add(new FilesModel(absolutePath, relativePath, hash));
        }

        return localFiles;
    }

    private List<File> getFileList(File currentDirectory, List<File> files) {

        File[] listOfFiles = currentDirectory.listFiles();
        if (listOfFiles == null) { files.add(currentDirectory); return files;}

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
}
