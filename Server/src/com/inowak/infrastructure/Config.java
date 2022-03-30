package com.inowak.infrastructure;

import java.io.*;
import java.nio.file.Path;

public class Config implements Serializable {

    private static final long serialVersionUID = 3691305301775499500L;
    public static int portNumber;
    public static Path workingDirectory;
    public static String password;
    //public static List<FilesModel> serverFiles = new ArrayList<>();

    /*
    public static void readConfig() throws Exception
    {
        File file = new File(Config.workingDirectory + "Config.dat");

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        Config config = (Config)ois.readObject();
        ois.close();
    }

    public static void saveConfig(Config config) throws IOException
    {
        File file = new File(Config.workingDirectory + "Config.dat");

        if(file.exists())
        {
            file.delete();
        }

        file.createNewFile();

        try {

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(config);
            oos.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    */
}
