/*
The client Class create a client with a specific id, associated to the
corresponding server. The client continously send money to a random server,
with a delay from 2 to 10 milliseconds. Every 500 money transactions the 
client with id = 0, communicates to its server to start a gloabal snapshot 
algorithm.
*/
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class RmiClient{

    public static void main(String[] args) throws IOException, NotBoundException {
        
        System.setProperty("sun.rmi.transport.tcp.responseTimeout","100000");

        int count=1;
        int snapshot_number=1;
        
        int id=0;                                       
        Map<Integer,AddServer> server=new TreeMap<>();

        int seqNum=0;
        int num_server=5;
        
        AddServer add0 = (AddServer) Naming.lookup("rmi://127.0.0.1:1090/Hello0");
        AddServer add1 = (AddServer) Naming.lookup("rmi://127.0.0.1:1091/Hello1");
        AddServer add2 = (AddServer) Naming.lookup("rmi://127.0.0.1:1092/Hello2");
        AddServer add3 = (AddServer) Naming.lookup("rmi://127.0.0.1:1093/Hello3");
        AddServer add4 = (AddServer) Naming.lookup("rmi://127.0.0.1:1094/Hello4");
        
        server.put(0,add0);
        server.put(1,add1);
        server.put(2,add2);
        server.put(3,add3);
        server.put(4,add4);
        
        server.get(0).setServers(server);
        server.get(1).setServers(server);
        server.get(2).setServers(server);
        server.get(3).setServers(server);
        server.get(4).setServers(server);
        Random random = new Random();

        int money=getRandomAmount(server.get(id).getAmount());
        
        try {
            while (server.get(id).getAmount()>=money) {
                double x = System.currentTimeMillis();
                double y = System.currentTimeMillis();
                while(y-x<random.nextInt(5)){
                    y = System.currentTimeMillis();
                }
                count++;
                int destination = getRandomBank(num_server);

                String bank = server.get(destination).getAddress();

                if(id!=destination) {
                    System.out.println(date()+"Sending "+money+" to bank"+destination+" "+bank);
                    try{
                        if(!server.get(id).isRecording()){
                            server.get(destination).transfer(id,seqNum, money);
                            //server.get(id).updateAmount(money,0);
                        }   
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
                money = getRandomAmount(server.get(id).getAmount());//Choose the random money to send
                seqNum++;
                if(count%1000==0){
                    System.out.println("here1");                    
                    server.get(id).setToken(id,server, snapshot_number);
                    snapshot_number++;
                }
            }
        } catch (Exception e) {
            System.out.println("Client failed "+e);
        }finally {
            System.out.println("The client's work is finishing!");
        }
    }
    
    /*
    The getRandomBank() function is used to retrieve a random bank to send
    money.
    */
    public static int getRandomBank(int nodes){
        return (int)(Math.random()*nodes);
    }
    
    /*
    The getRandomAmount() function returns a value between 0 and 100, according 
    to the actual amount of money in the server. If the server has less than 100
    money, so the function will return a value between 0 and server_amount.
    */
    public static int getRandomAmount(int max){
        return (int)(Math.random()*Math.min(max,100));
    }
    
    /*
    The date() function is used to print the current time for the log file.
    */
    public static String date(){
        long milliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss:SS");    
        Date resultdate = new Date(milliseconds);
        return(sdf.format(resultdate)+" -> ");
    }
}
