import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Simone
 */
public class RunServers {
    public static void main(String[] args) throws NotBoundException, MalformedURLException, RemoteException {
        RmiServer s0 = new RmiServer(0,1090);
        RmiServer s1 = new RmiServer(1,1091);
        RmiServer s2 = new RmiServer(2,1092);
        RmiServer s3 = new RmiServer(3,1093);
        RmiServer s4 = new RmiServer(4,1094);   
    }   
}
