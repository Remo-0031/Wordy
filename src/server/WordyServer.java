package server;

import common.wordyApp.Player;
import common.wordyApp.Wordy;
import common.wordyApp.WordyHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;


import java.io.*;
import java.sql.*;
import java.util.ArrayList;

public class WordyServer {
    static ArrayList<String> words;
    static ArrayList<Player> credentials;
    private static Connection con;


    public static void main(String[] args) throws SQLException, IOException {
        try{
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant and register it with the ORB
            WordyImpl imp = new WordyImpl();

            // get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(imp);
            Wordy href = WordyHelper.narrow(ref);

            // get the root naming context
            // NameService invokes the name service
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            // Use NamingContextExt which is part of the Interoperable
            // Naming Service (INS) specification.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // bind the Object Reference in Naming
            String name = "Word";
            NameComponent[] path = ncRef.to_name( name );
            ncRef.rebind(path, href);

            System.out.println("WordTester ready and waiting ...");

            // wait for invocations from clients
            orb.run();
        }

        catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }


        System.out.println("HelloServer Exiting ...");


    }


}

