package com.company;


import com.company.application.ConfigurationWindow;

import javax.swing.*;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {

        ConfigurationWindow configurationWindow = new ConfigurationWindow();
        configurationWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        configurationWindow.setVisible(true);

    }
}
