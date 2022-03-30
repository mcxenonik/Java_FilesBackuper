package com.inowak.GUI;

import com.inowak.infrastructure.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigurationWindow {

    JFrame frame = new JFrame("Awesome Backup Server Configuration");
    private JPanel configurationPanel;
    private JButton getSomeHelpManButton;
    private JButton applyButton;
    private JButton closeButton;
    private JButton directorySelectButton;
    private JTextField portTextField;
    private JTextField directoryTextField;
    private JTextField passwordTextField;


    public ConfigurationWindow() {

        openWindow();

        portTextField.setText("5555");
        directoryTextField.setText("D:\\B_N\\server");
        passwordTextField.setText("securePassword");

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.exit(0);
            }
        });

        directorySelectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser f = new JFileChooser();
                f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                f.showSaveDialog(null);
                directoryTextField.setText(String.valueOf(f.getSelectedFile()));
            }
        });

        getSomeHelpManButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JOptionPane.showMessageDialog(createMessageDialog(), "Put server hostname or IP in \"Server Address\" field.\n" +
                        "\"Server Port\" can be any unused port ranging from 0 to 65535.\n" +
                        "Due to some networks limitations we suggest using ports over 1024.\n" +
                        "Remember to add firewall rules into your system.");
            }
        });

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    Config.portNumber = Integer.valueOf(portTextField.getText());
                    Config.password = passwordTextField.getText();
                    Config.workingDirectory = Paths.get(directoryTextField.getText());

                    if (!Files.exists(Config.workingDirectory)) {
                        File f = new File(String.valueOf(Config.workingDirectory));
                        f.mkdir();
                    }

                    frame.dispose();
                    new MainWindow();

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void openWindow() {
        frame.setContentPane(configurationPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640,480);
        frame.setVisible(true);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    private Frame createMessageDialog() {
        JFrame messageDialog = new JFrame("MessageFrame");
        messageDialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return messageDialog;
    }
}
