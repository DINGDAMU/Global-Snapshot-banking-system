import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface AddServer extends Remote {
    int getAmount() throws RemoteException;
    void updateAmount(int amount, int sign)throws RemoteException;
    String getAddress()throws RemoteException;
    void transfer(int id,int seqNum,int money)throws RemoteException;
    void setToken(int id, Map<Integer,AddServer> server, int snapshot_number)throws  RemoteException;
    void update(int id,int[] vals,int server_amount, int snapshot_number)throws RemoteException;
    String date()throws RemoteException;
    boolean isRecording()throws RemoteException;
    void setServers(Map<Integer,AddServer> servers)throws RemoteException;
}