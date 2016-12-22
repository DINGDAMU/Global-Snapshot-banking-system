/*
The AddServerImpl Class contains the details of the server implementation.
It manages the local and the global snapshot algorithm, the transfer and the
update money functions and all the variables to make the network consistent.
*/
package Rmi;



import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AddServerImpl extends UnicastRemoteObject implements AddServer,Serializable{
    private String address;
    private int tot = 5000000;
    private int amount;
    private int arr[]=new int[5];
    private boolean[] tok =new boolean[5];
    private int[] val=new int[5];
    private boolean record=false;
    private int identifier;
    private int snapshot_amount;
    private int expected=1;
    private int arrived=0;
    private int check_tot=0;
    Logger logger;  
    FileHandler fh;
    Map<Integer,AddServer> server;
    
    private static final ReadWriteLock tokenLock = new ReentrantReadWriteLock();
    private static final ReadWriteLock updateLock = new ReentrantReadWriteLock();

    private int[] snapshot_amounts = new int[5];
    private int[][] snapshot_matrix = new int[5][5];

    public AddServerImpl (String address, int amount,int id) throws RemoteException, IOException, NotBoundException {
        super();
        System.setProperty("sun.rmi.transport.tcp.responseTimeout","100000");
        for(int i=0; i<tok.length; i++){
            tok[i]=false;
            val[i]=0;
            arr[i]=-1;
        }
        this.address=address;
        this.amount=amount;
        this.identifier=id;
        logger = Logger.getLogger("Log_"+id+".log");  
        fh = new FileHandler("Log_Server_"+id+".log");  
        logger.addHandler(fh);
        logger.setUseParentHandlers(false);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter); 
    }

    /*
    The setToken() function manges the local snapshot algorithm.
    Each time a server receive a token, if the token is the one expected, in 
    according with the snapshot number, it will send a token to each other
    servers in the network. When tokens from all other servers has been
    collected, it will stop recording its local snapshot and send results to 
    the other servers.
    */
    @Override
    public void setToken(int id, Map<Integer,AddServer> server, int snapshot_number) throws RemoteException {
        updateLock.writeLock().lock();
        //tok[identifier]=true;
        //tok[id]=true;
        //System.out.println(date()+"Token received from "+server.get(id).getAddress()+" with snapshot_number = "+snapshot_number);
        //logger.info(date()+"Token received from "+server.get(id).getAddress()+" with snapshot_number = "+snapshot_number+"\n");
        if(snapshot_number==expected){
            this.expected++;
            //System.out.println(date()+"Snapshot "+snapshot_number+" started");
            logger.info(date()+"Snapshot "+snapshot_number+" started\n");
            tokenLock.writeLock().lock();
            record=true;
            snapshot_amount=amount;    
            //tok[id]=true;
            //System.out.println(date()+"Amount: "+snapshot_amount+"€");
            updateLock.writeLock().unlock();
            tokenLock.writeLock().unlock();
            for(int i=0; i<tok.length; i++){
                if(i!=identifier){
                    //System.out.println(date()+"Sending token to server "+server.get(i).getAddress());
                    //logger.info(date()+"Sending token to server "+server.get(i).getAddress()+"\n");
                    server.get(i).setToken(identifier,server, snapshot_number);
                }
            }  
        }
        else{
            updateLock.writeLock().unlock();
        }
        //tokenLock.writeLock().lock();
        tok[identifier]=true;
        tok[id]=true;
        //tokenLock.writeLock().unlock();


        boolean end = true; 
       
        for(int i=0; i<tok.length; i++){
            if(!tok[i]){
                end=false;
            }
        }

        if(end){
            record=false;
            //System.out.println(date()+"\nSnapshot "+snapshot_number+" ended\n");
            for(int i=0; i<tok.length; i++){
                if(i!=identifier){
                    //System.out.println(date()+"Sending local snapshot "+snapshot_number+" to server"+server.get(i).getAddress());
                    server.get(i).update(identifier,val,snapshot_amount, snapshot_number);
                }
            }
            server.get(identifier).update(identifier,val,snapshot_amount, snapshot_number);
            logger.info(date()+"Snapshot "+snapshot_number+" ended\n");
        }
    }

    /*
    The update() function will receive the local snapshot form other servers.
    When all the local snapshots has been received, the function write in the
    log file of the current server the global snapshot of the system
    */
    @Override
    public void update(int id, int[] vals, int server_amount, int snapshot_number) throws RemoteException {        
        arrived++;
        //System.out.println(date()+"Received local snapshot "+snapshot_number+" from server "+server.get(id).getAddress());
        //System.out.println(date()+"Received local value from server"+server.get(id).getAddress()+": "+server_amount);
        snapshot_amounts[id]=server_amount;
        for (int i=0;i<vals.length;i++){
            snapshot_matrix[i][id]=vals[i];
        }
        if(arrived==5){
            arrived=0;
            for (int i=0;i<vals.length;i++){           
                //System.out.println(date()+server.get(i).getAddress()+" -> "+snapshot_amounts[i]);
                logger.info(date()+server.get(i).getAddress()+" -> "+snapshot_amounts[i]+"\n");
                check_tot+=snapshot_amounts[i];
            }
            for(int i=0;i<5;i++){
                for(int j=0;j<5;j++){
                    //System.out.println(date()+"Trans from "+server.get(i).getAddress()+" to "+server.get(j).getAddress()+" -> "+snapshot_matrix[i][j]);
                    logger.info(date()+"Trans from "+server.get(i).getAddress()+" to "+server.get(j).getAddress()+" -> "+snapshot_matrix[i][j]+"\n");
                    check_tot+=snapshot_matrix[i][j];
                }
            }
            logger.info(date()+"Total money in the snapshot: "+check_tot+"€\n\n\n\n");
            //System.out.println(date()+"Total money in the snapshot: "+check_tot+"€");
            if(check_tot!=5000000 && identifier == 0){
                System.out.println("error at snapshot "+snapshot_number);
            }
            check_tot=0;

            for(int i=0; i<tok.length; i++){
                tok[i]=false;
                val[i]=0;
                snapshot_amounts[i]=0;
            }         
        }
    }

    /*
    The updateAmount() function takes as input the amount of money that the
    server sent to another one and proceeds to update its new money amount.
    */
    @Override
    public void updateAmount(int money, int sign)throws RemoteException {
        //updateLock.writeLock().lock();
        tokenLock.writeLock().lock();
        if(sign==0){
            this.amount -= money;
        }
        else{
            this.amount += money;
        }
        //updateLock.writeLock().unlock();
        tokenLock.writeLock().unlock();
    }

    /*
    The getAmount() function returns the actual money on the server.
    */
    @Override
    public int getAmount() throws RemoteException{
        return amount;
    }

    /*
    The getAddress() function returns the url address of the server.
    */
    @Override
    public String getAddress()throws RemoteException{
        return address;
    }

    /*
    The transfer() function takes as inpute the identifier of the server that
    sends the money, the sequence number of the packet request and the amount
    of sent money. It will manage the tansfer mony request updating server
    money amounts and snashot arrays.
    */
    @Override
    public void transfer(int id,int seqNum,int money)throws RemoteException{
        updateLock.writeLock().lock();
        //tokenLock.writeLock().lock();
        if(arr[id]!=seqNum){
            //tokenLock.writeLock().lock();
            if(!server.get(id).isRecording()){
                tokenLock.writeLock().lock();
                if(record&&!tok[id]){
                    //System.out.println(date()+"Arrived "+money+"€ from "+id);
                    val[id]+= money;
                }
                amount += money;
                arr[id]=seqNum;
                tokenLock.writeLock().unlock();
            }
            else{
                server.get(id).updateAmount(money,1);
            }
        }
        server.get(id).updateAmount(money,0);
        updateLock.writeLock().unlock();
    }
    
    /*
    The date() function is used to print the current time for the log file.
    */
    @Override
    public String date(){
        long milliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss:SS");    
        Date resultdate = new Date(milliseconds);
        return(sdf.format(resultdate)+" -> ");
    }
    
    /*
    The isRecording() function will return a boolean value that represent the
    local snapshot current state. It is used from the clint to check if the 
    snapshot is running on the server.
    */
    @Override
    public boolean isRecording(){
        return record;
    }      
    
    /*
    The setServers() function is used to set up server initially.
    */
    public void setServers(Map<Integer,AddServer> servers){
        server=servers;
    }
    
}