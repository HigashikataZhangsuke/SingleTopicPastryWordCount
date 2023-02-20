package yinzhe.test.BootNodeentry;

import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import rice.p2p.scribe.ScribeImpl;
import rice.environment.Environment;
import rice.environment.logging.Logger;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.scribe.Topic;
import rice.pastry.*;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;
import testcsv.testofread;
import rice.pastry.dist.DistPastryNodeFactory;
import yinzhe.test.countalgorithm.*;
import java.io.*;

/**
 * 
 * @author zhangsuke
 * This is the Test Class for this miniproject. It contains 3 functions, including main() as the entry; bootnode for node generation & boot and the makescribe for scribe
 * 
 */

public class BootNodeentry {

	  //Constructor. Do nothing here.
	  public BootNodeentry(int bindport, InetSocketAddress bootaddress,
	      int numNodes, Environment env) throws Exception {  
	  }
	  
	  //The function for generate and boot nodes.  
	    public PastryNode Bootnodes(int bindport, InetSocketAddress bootaddress,NodeIdFactory nidFactory,PastryNodeFactory factory) throws IOException, InterruptedException {
	      PastryNode node = factory.newNode();
		  node.boot(bootaddress);
	      // the node may require sending several messages to fully boot into the ring
	     synchronized(node) {
	        while(!node.isReady() && !node.joinFailed()) {
	          // delay so we don't busy-wait
	         node.wait(1000);
	          // abort if can't join
	          if (node.joinFailed()) {
	            throw new IOException("Could not join the FreePastry ring.  Reason:"+node.joinFailedReason()); 
	          }
	        }       
	     }
	      //use this Vec to store nodes for further scribe clients generation
	      //nodevec.add(node);
	      System.out.println("Finished creating BootNode: " + node);
	      return node;
	    }
	    
	    //The function for client generation
	    public WordCountApplication Createapplications(PastryNode node,Environment env) throws InterruptedException {
	    	WordCountApplication collectclient = new WordCountApplication(node);
	    	//env.getTimeSource().sleep(3000);
	    	System.out.println("Finished generate one scribe client for " + node.getId().toString());
	    	return collectclient;
	    }
	    
	public int getnum(int i) {
		return i;
	}
	
	public HashSet<Integer> getsetforread(int max){
		Random rand = new Random();
		HashSet<Integer> hashset = new HashSet<Integer>();
		while (hashset.size()<max) {
			hashset.add(rand.nextInt(max));
		}
		return hashset;
	}
	

	public static void main(String[] args) throws Exception {
		
	    // Loads pastry configurations & Set a logger for whole system.
		//PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream("/home/johnny/YinzheWordCountTest.txt")),true);
		//System.setOut(ps);
	    Environment env = new Environment();
	    
	    // disable the UPnP setting (in case you are testing this on a NATted LAN or single computer)
	    env.getParameters().setString("nat_search_policy", "never");
	    
	    try {
	    	
	      // the port to use locally
	      int bindport = Integer.parseInt(args[0]);

	      // build the bootaddress from the command line args
	      InetAddress bootaddr = InetAddress.getByName(args[1]);
	      int bootport = Integer.parseInt(args[2]);
	      InetSocketAddress bootaddress = new InetSocketAddress(bootaddr, bootport);
	      int sleepdelay =60000;
	      // the port to use locally
	      int numNodes = Integer.parseInt(args[3]);
	      int windowsize = Integer.parseInt(args[4]);
	      int rate=Integer.parseInt(args[5]);
	      int expsize= Integer.parseInt(args[6]);
	      // Instance of test
	      BootNodeentry test = new BootNodeentry(bindport, bootaddress, numNodes,
	          env);
	      
	      //Generate basic parts for node generation
	      NodeIdFactory nidFactory = new RandomNodeIdFactory(env);
	      PastryNodeFactory factory = new SocketPastryNodeFactory(nidFactory, bindport, env);
	      
//Step one: Create Node join ring and Create applications. Should wait input after finish
	      
	    	PastryNode bootnode = test.Bootnodes(bindport, bootaddress, nidFactory, factory);
	          env.getTimeSource().sleep(sleepdelay);
	    	  WordCountApplication clt = test.Createapplications(bootnode, env);
	    	  System.out.println("Finished Bootnode construction, Ready for step 2");
	    	  Scanner input = new Scanner(System.in);
	      	  String val = "";
	      	  while (val.equals("Step2")!=true) {
	      		  System.out.println("The nodes are join the rings, Plz give instructions after they finished");
	      		  val = input.nextLine();
	      		  
	      	  }
	      	  System.out.println("Start Step2 Now");
	      	  
//Step two: Do scribe for bootnode and read data.  Should wait
	      	Topic tp = new Topic(new PastryIdFactory(bootnode.getEnvironment()), "SimpleAggr");
	    	   clt.scribe(tp);
	    	   clt.settopic(tp);
	    	  //env.getTimeSource().sleep(1000);
	    	   System.out.println("Finished scribe one scribe client");
	    	  env.getTimeSource().sleep(5000);
	    
	      
	      Set<Integer> nums = test.getsetforread(numNodes);
	    	 
	    	  clt.windowsize = windowsize;
		    	 //((WordCountApplication) cltvec.elementAt(i)).interval = interval;
	    	  clt.readrate = rate;
		    		 clt.leafnodenum = expsize;
		    		 clt.getchildlist();
		    	 //if (((WordCountApplication) cltvec.elementAt(i)).childlist.isEmpty()) {
		    	    clt.setreadnum(nums.toArray(new Integer[nums.size()])[0]);
		    		clt.readdata(); 
	      System.out.println("Finished Read Data");

      	  
//Step3: Do computation. Just start this after Datareading
	      Scanner input2 = new Scanner(System.in);
      	  String val2 = "";
      	  while (val2.equals("Step3")!=true) {
      		  System.out.println("Just wait for all node finish scribe. Then give instruction for get childlist and calculation! ");
      		 // System.out.println("Root for this tp currently is "+ clt.getRoot(tp));
      		  val2 = input.nextLine();
      		  }
      	  System.out.println("Start Step3 Now");
	      System.out.println("Construction Finished, Now Processing Data");
	    //  System.out.println("Root for this tp currently is "+ clt.getRoot(tp));
	     // for (int i=0;i<numNodes;i++) {
	    	  if (clt.isRoot()==true) {
	    		  clt.startPublishTask();
	    	 // }
	      }
	      
	    } catch (Exception e) {
	      // remind user how to use
	      System.out.println("Usage:");
	      System.out
	          .println("java [-cp FreePastry-<version>.jar] rice.tutorial.scribe.ScribeTutorial localbindport bootIP bootPort numNodes");
	      System.out
	          .println("example java rice.tutorial.scribe.ScribeTutorial 9001 pokey.cs.almamater.edu 9001 10");
	      throw e;
	    }
	  }
	
}