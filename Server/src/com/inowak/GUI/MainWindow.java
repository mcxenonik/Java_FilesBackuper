package com.inowak.GUI;

import com.inowak.infrastructure.Config;
import com.inowak.service.Server;

import javax.swing.*;

public class MainWindow {

    JFrame frame = new JFrame("Awesome Backup Server Status");
    JPanel mainPanel;
    public JTextArea logsTextArea;



    public MainWindow() {

        openWindow();
        Thread serverThread = new Thread(new Server());
        serverThread.start();
        logsTextArea.append("Server is listening on port: " + Config.portNumber + "\n");

    }

    private void openWindow() {
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(320,240);
        frame.setVisible(true);
        //frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }
}
