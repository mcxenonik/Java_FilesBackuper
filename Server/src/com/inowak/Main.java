package com.inowak;

import com.inowak.GUI.ConfigurationWindow;

import javax.swing.*;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        //Sprawdzanie czy jest zapisana konfiguracja
        //Jesli jest to próba połączenia
        //Jeśli się uda to MainWindow
        //Jeżeli nie to ConfigurationWindow

        new ConfigurationWindow();
    }
}
