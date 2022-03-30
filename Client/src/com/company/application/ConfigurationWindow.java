package com.company.application;

import com.company.infrastructure.Config;
import com.company.service.ServerHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;

public class ConfigurationWindow extends JFrame implements ActionListener {

    private JPanel configurationPanel;

    private JButton applyButton;
    private JButton exitButton;
    private JButton getHelpButton;

    private JTextField passwordTextField;
    private JTextField serverPortTextField;
    private JTextField serverAddressTextField;

    private ServerHandler serverHandler;

    public ConfigurationWindow()
    {
        setTitle("Awesome Backup Configuration");
        setSize(300,250);
        setLayout(null);

        this.setContentPane(configurationPanel);

        applyButton.addActionListener(this);
        exitButton.addActionListener(this);
        getHelpButton.addActionListener(this);

    }

    private Frame createMessageDialog()
    {
        JFrame messageDialog = new JFrame("MessageFrame");
        messageDialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        return messageDialog;
    }

    void creatMainWindow()
    {
        MainWindow mainWindow = new MainWindow(this.serverHandler);
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();

        if(source == applyButton)
        {
            Config.password = passwordTextField.getText();
            Config.portNumber = Integer.parseInt(serverPortTextField.getText());
            Config.serverAddress = serverAddressTextField.getText();

            try
            {
                Socket serverSocket = new Socket(Config.serverAddress, Config.portNumber);
                serverHandler = new ServerHandler(serverSocket);

                if (serverHandler.authenticate()) {
                    this.dispose();
                    this.creatMainWindow();
                } else {
                    JOptionPane.showMessageDialog(createMessageDialog(), "Invalid password.");
                }
            }
            catch(Exception exception)
            {
                exception.printStackTrace();
                JOptionPane.showMessageDialog(createMessageDialog(), "Could not connect to server.");
            }
        }

        else if(source == exitButton)
        {
            System.exit(0);
        }

        else if(source == getHelpButton)
        {
            JOptionPane.showMessageDialog(createMessageDialog(), "Put server hostname or IP in \"Server Address\" field.\n" +
                    "\"Server Port\" can be any unused port ranging from 0 to 65535.\n" +
                    "Due to some networks limitations we suggest using ports over 1024.\n" +
                    "Remember to add firewall rules into your system.");
        }
    }
}
