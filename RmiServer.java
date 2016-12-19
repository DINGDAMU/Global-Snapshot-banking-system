import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class RmiServer{
    RmiServer(int id, int port) throws NotBoundException{
        try{
            int server_id=id;
            LocateRegistry.createRegistry(port);
            int initial_amount=1000000;
            AddServerImpl add = new AddServerImpl("127.0.0.1:"+port+"",initial_amount,server_id);
            Naming.rebind("//127.0.0.1:"+port+"/Hello"+id+"",add);
            System.out.printf("Server 127.0.0.1:"+port+" is ready!\n"); 
        }catch (RemoteException e) {
            System.out.printf("Hello server failed" + e);
        }catch (MalformedURLException e) {
            System.out.println("MalformedURLException" + e);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
