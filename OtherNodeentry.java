package yinzhe.test.OtherNodeentry;

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

public class OtherNodeentry {



	/**
	 * 
	 * @author zhangsuke
	 * This is the Test Class for this miniproject. It contains 3 functions, including main() as the entry; bootnode for node generation & boot and the makescribe for scribe
	 * 
	 */

	

		  //Constructor. Do nothing here.
		  public OtherNodeentry(int bindport, InetSocketAddress bootaddress,
		      int numNodes, Environment env) throws Exception {  
		  }
		  
		  //The function for generate and boot nodes.  
		    public void Bootnodes(int bindport, InetSocketAddress bootaddress,NodeIdFactory nidFactory,PastryNodeFactory factory,Vector<PastryNode> nodevec) throws IOException, InterruptedException {
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
		      nodevec.add(node);
		      System.out.println("Finished creating OtherNode: " + node);
		      //return node;
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
        
		public void loggerhealth(String str) {
			 FileWriter fw = null;
			 try {
			  //if (this.isRoot()!=true) 
			 // {
				//  File f = new File("/home/johnny/Testlog/YinzheWordCountTestPar" +this.endpoint.getId()+ ".txt");
			 // }
			  //fw = new FileWriter(f,true);
			 // }else 
			 // {
				 File f = new File("/home/johnny/Testlog/YinzheWordCountTesthealth.txt");
			     fw = new FileWriter(f,true);
			 // }
			 }catch(IOException e) {
				 e.printStackTrace();
			 }
			 PrintWriter pw = new PrintWriter(fw);
			 pw.print(str+"\n");
			 pw.flush();
			 try {
				 fw.flush();
				 pw.close();
				 fw.close();
			 }catch(IOException e)
			 {
				 e.printStackTrace();
			 }
			 
			 
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
		      int sleepdelay =900000;
		      // the port to use locally
		      int numNodes = Integer.parseInt(args[3]);
		      int windowsize = Integer.parseInt(args[4]);
		      int rate=Integer.parseInt(args[5]);
		      int expsize= Integer.parseInt(args[6]);
		      // Instance of test
		      OtherNodeentry test = new OtherNodeentry(bindport, bootaddress, numNodes,
		          env);
		      boolean checklive = true;
		      //Generate basic parts for node generation
		      NodeIdFactory nidFactory = new RandomNodeIdFactory(env);
		      PastryNodeFactory factory = new SocketPastryNodeFactory(nidFactory, bindport, env);
		      
		      //Store Vector initialized here
		      Vector nodevec = new Vector();

//Step one: Create Node join ring and Create applications. Should sleep after finish

		      //First loop, used for generate nodes and scribe client
		      for (int i=0;i<numNodes;i++) {
		    	test.Bootnodes(bindport, bootaddress, nidFactory, factory,nodevec);
		        env.getTimeSource().sleep(1000);
		    	  //env.getTimeSource().sleep(3000);
		      }
		      Vector cltvec = new Vector();
		      for (int i=0;i<numNodes;i++) {
		    	  WordCountApplication clt = test.Createapplications((PastryNode) nodevec.elementAt(i), env);
		    	  env.getTimeSource().sleep(500);
		    	  cltvec.add(clt);
		      }
		      
		      env.getTimeSource().sleep(sleepdelay);
		      
		      //Check liveness
		      for (int i=0;i<numNodes;i++) {
		    	  //WordCountApplication clt = test.Createapplications((PastryNode) nodevec.elementAt(i), env);
		    	  //env.getTimeSource().sleep(500);
		    	checklive = checklive & ((PastryNode)(nodevec.elementAt(i))).isAlive(((PastryNode)(nodevec.elementAt(i))).getLocalHandle());
		    	
		    	 // cltvec.add(clt);
		    }
		      test.loggerhealth("The health check result for this VM's Ring is "+checklive);
		      checklive = true;
		      System.out.println("Finished node and application construction, Ready for step 2, Now you have 60 sec to type input at bootnode");
		      env.getTimeSource().sleep(60000);//Will have 10 sec for you to type input.
		      
//Step two: Do scribe for everynode and read data.  Should sleep after finish
		      
		      //Scribe to Topic
		      for (int i=0;i<numNodes;i++) {
		    	   ((WordCountApplication) cltvec.elementAt(i)).scribeforothernode();
		    	   //((WordCountApplication) cltvec.elementAt(i)).settopic();
		    	   env.getTimeSource().sleep(1000);
		    	   System.out.println("Finished scribe one scribe client");
		      }
		      
		      env.getTimeSource().sleep(sleepdelay);

		     
		      //env.getTimeSource().sleep(10000); 
		      
		      Set<Integer> nums = test.getsetforread(numNodes);
		    
		      for (int i=0;i<numNodes;i++) {
		    	 //Set parameters for wordcount and read data, get childlist for checking
		    	  ((WordCountApplication) cltvec.elementAt(i)).windowsize = windowsize;
		    	  ((WordCountApplication) cltvec.elementAt(i)).readrate = rate;
		    	  ((WordCountApplication) cltvec.elementAt(i)).leafnodenum = expsize;
		    	  ((WordCountApplication) cltvec.elementAt(i)).setreadnum(nums.toArray(new Integer[nums.size()])[0]);
		    	  ((WordCountApplication) cltvec.elementAt(i)).readdata(); 
		    	  ((WordCountApplication) cltvec.elementAt(i)).getchildlist();
		    	  
		      //check liveness
		      for (int i=0;i<numNodes;i++) {
		    	checklive = checklive & ((PastryNode)(nodevec.elementAt(i))).isAlive(((PastryNode)(nodevec.elementAt(i))).getLocalHandle());
		    }
		      test.loggerhealth("The health check result for this VM's after Scribe is "+checklive);
		      checklive = true;
		      
		      
		      System.out.println("Finished Read Data, Now you have 60 sec to type input at bootnode");
		      env.getTimeSource().sleep(60000);
		      
		      System.out.println("Construction Finished, Now Processing Data");
		      for (int i=0;i<numNodes;i++) {
		    	  if (((WordCountApplication) cltvec.elementAt(i)).isRoot()==true) {
		    		  ((WordCountApplication) cltvec.elementAt(i)).startPublishTask();
		    	 }
		      }
		      
		        //check liveness
		      for (int i=0;i<numNodes;i++) {
		    	  //WordCountApplication clt = test.Createapplications((PastryNode) nodevec.elementAt(i), env);
		    	  //env.getTimeSource().sleep(500);
		    	checklive = checklive & ((PastryNode)(nodevec.elementAt(i))).isAlive(((PastryNode)(nodevec.elementAt(i))).getLocalHandle());
		    	
		    	 // cltvec.add(clt);
		    }
		      test.loggerhealth("The health check result for this VM's after Start computation is "+checklive);
		      checklive = true;
		      env.getTimeSource().sleep(10000);
		      
		        //check liveness
		      for (int i=0;i<numNodes;i++) {
		    	  //WordCountApplication clt = test.Createapplications((PastryNode) nodevec.elementAt(i), env);
		    	  //env.getTimeSource().sleep(500);
		    	checklive = checklive & ((PastryNode)(nodevec.elementAt(i))).isAlive(((PastryNode)(nodevec.elementAt(i))).getLocalHandle());
		    	
		    	 // cltvec.add(clt);
		    }
		      test.loggerhealth("The health check result for this VM's after Start computation is "+checklive);
		      
		      env.getTimeSource().sleep(10000);
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
