package yinzhe.test.countalgorithm;

import rice.environment.Environment;
import rice.p2p.commonapi.*;
import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.CancellableTask;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.RouteMessage;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.MessageDeserializer;
import rice.p2p.scribe.Scribe;
import rice.p2p.scribe.ScribeClient;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.ScribeImpl;
import rice.p2p.scribe.ScribeImpl.TopicManager;
import rice.p2p.scribe.Topic;
import yinzhe.test.msg.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.messaging.JavaSerializedDeserializer;
/**
 * 
 * @author zhangsuke
 * This is the Class for Scribe Application/Client. Contains functions related to update
 * 
 */
import rice.tutorial.scribe.MyScribeClient.PublishContent;
public class WordCountApplication implements ScribeClient, Application{
	 
	 public int windowsize;
	 public CancellableTask publishTaskroot;
	 public CancellableTask publishTaskleaf;
	 public Endpoint endpoint;
	 public Scribe scribe;
	 public Topic tp;
	 Environment env;
	 public LinkedHashMap<String,Integer> selfDict = new LinkedHashMap<String,Integer>();
	 public LinkedHashMap<Id,Integer> childlist = new LinkedHashMap<Id,Integer>();
	 public int whichcsvread;
	 public int linecnt;
	 public int roundcnt;
	 //public double interval;
	 public int readrate;
	 //public double rate;
	 public int readnum;
	 public long endtimeforeachround;
	 public int startplace;
	 public long starttimeforeachwindow;
	 public String[] keyarr;
	 public int leafnodenum;
	 public int whichwindow;
	 public int rootcnter=0;
	 public int readrndcnt;
	 public int readleft;
	 public int good;
	 //public ScheduledExecutorService executor; 
	 //public int cntfiles =0;
	 //Constructor
	 public WordCountApplication(Node node) {
		  this.endpoint = node.buildEndpoint(this, "wordcountinstance");
		  this.scribe = new ScribeImpl(node,"Wordcount");
		  ((JavaSerializedDeserializer)this.endpoint.getDeserializer()).setAlwaysUseJavaSerialization(true);
		  this.endpoint.register();
		  this.startplace =0;
		  this.whichwindow = 0;
	 }
	  
	  public void scribe(Topic tp) {
		  scribe.subscribe(tp, this); 
	  }
	  
	  public void scribeforothernode() {
		  this.tp=new Topic(new PastryIdFactory(env), "SimpleAggr");
		  scribe.subscribe(this.tp, this);
	  }
	  
	  
	  public void setreadnum(int readin) {
		  this.readnum = readin;
	  }
	  
	  public void check() {
		  checkmsg msg = new checkmsg(0);
		  scribe.publish(tp, msg);
	  }
	  
	  public void getchildlist() {
		  //This function is used for a node to get it potential childlist. Will be used further for determining if this node get all data from its child.
		  if (this.getChildrenParam(this.tp).size() !=0) {
		  Iterator iter = this.getChildrenParam(this.tp).iterator();
		  NodeHandle thisnh = (NodeHandle) iter.next();
		  childlist.put(thisnh.getId(), 0);
		  //childlist.add(thisnh.getId());
		  while(iter.hasNext()) {
			 thisnh = (NodeHandle) iter.next();
			// childlist.add(thisnh.getId());
			 childlist.put(thisnh.getId(), 0);
		  }
		  }
	  }
	  
	  public void readdata() {
			//Used to read data
			 //int passline=0;
			 this.roundcnt=0;
			 
			 //String csvFile1 = "/home/johnny/WCDataset2/TAXI_sample_data_senml_time"+readnum+".csv";
			  String csvFile1 = "/home/johnny/WCDatasetbig/TestRead"+readnum+".csv";
			 String line = "";
			  String csvspliter = ",";

			  try (BufferedReader br = new BufferedReader(new FileReader(csvFile1))){
			  while (roundcnt<readrate*windowsize) {
					  // long stime =  System.nanoTime();
				     //while (passline<linecnt)
				     //{br.readLine();
				     //passline+=1;}
				     if ((line = br.readLine()) != null) {
						  //long stime =  System.nanoTime();
				    	  //line = br.readLine();
						  String[] dataread = line.split(csvspliter);
						 // System.out.println(dataread[0]+" "+dataread[1]);
						  if (selfDict.get(dataread[0])!=null) {
						  selfDict.put(dataread[0], selfDict.get(dataread[0])+ Integer.parseInt(dataread[1])); }
						  else
						  {
							  selfDict.put(dataread[0], Integer.parseInt(dataread[1])); 
						  }
						  //selfDict.entrySet().forEach(entry-> {System.out.println(entry.getKey()+" "+ entry.getValue());});
						  roundcnt+=1;
					 }
			  }
			  //linecnt+= roundcnt;
			  this.selfDict.put("marker", 1);
			  this.keyarr= this.selfDict.keySet().toArray(new String[this.selfDict.keySet().size()]);
			  this.readrndcnt = (readrate*windowsize)/(keyarr.length);
			  this.readleft = (readrate*windowsize)-(keyarr.length)*readrndcnt;
			  } catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	  }   
	  
	  public void run50() {
		  //This function is used to send a set of data in one round
		  LinkedHashMap<String,Integer> datasend = new LinkedHashMap<String,Integer>();
			if (this.startplace!=readrate*windowsize-readrate) {
			for (int i=startplace;i<this.startplace+this.readrate;i++) {
				
				datasend.put(keyarr[i],this.selfDict.get(keyarr[i]));
			}
			
			this.startplace+= this.readrate;
		}
			else {
				//Readched last line the marker
				//LinkedHashMap<String,Integer> datasend = new LinkedHashMap<String,Integer>();
				for (int i=startplace;i<this.startplace+this.readrate+1;i++) {
					
					datasend.put(keyarr[i],this.selfDict.get(keyarr[i]) );
				}
				this.publishTaskleaf.cancel();
				this.startplace=0;
			}

		  NodeHandle dst = this.getParentParam(this.tp);
  		  BottomNodesMsg msgsend = new BottomNodesMsg(datasend,this.endpoint.getId());
  		  endpoint.route(dst.getId(), msgsend, dst);
	  }
	  
	  
	 public boolean check(LinkedHashMap<Id,Integer> childlistinput) {
		 //Check if now the parent node get all data from its child
		 boolean isUnique = true;
		// boolean isUnique = true;
		 for (Map.Entry<Id, Integer> en : childlistinput.entrySet()) {
		     //if (en.getValue()!=windowsize && en.getValue()!=1 ) {
			 if (en.getValue()!=windowsize && en.getValue()!=1 ) {
		    	 //if () {
		    		 //Not leaf nor intermediate
		    		 isUnique = false;
		    		 break;
		    	 //}
		     }
		     }         
		// }
		 return isUnique;
		 //return (new HashSet(childlistinput.values()).size()==1);
	 }
	 
 
	 public  boolean checkmarker(LinkedHashMap<String,Integer> selfDictinput) {
		 int nummar = selfDictinput.get("marker");
			 if (nummar>=(int)(this.leafnodenum*0.85))
			 {
				 return true;
			 }
			 else
			 {
				 return false;
			 }
	 }
	 
	 public void startPublishTask() {
		    publishTaskroot = endpoint.scheduleMessage(new Startwindow(), 1000, windowsize*1000+90000);//Give 5 more sec every window.should be more?
		    //System.out.println("Start Publishtask");
     }
	 
	 public void startPublishTaskforleaf() {
		    publishTaskleaf = endpoint.scheduleMessage(new selfschedule50msg(), 0, 1000);
		    
		    //System.out.println("Start Publishtask");
     }
	 

	 public void logger(String str) {
		 FileWriter fw = null;
		 try {
			 File f = new File("/home/johnny/Testlog/YinzheWordCountTestRoot"+this.endpoint.getId()+".txt");
		     fw = new FileWriter(f,true);
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
	 public void loggererror(String str) {
		 FileWriter fw = null;
		 try {
                     File f = new File("/home/johnny/Testlog/YinzheWordCountTestError"+this.endpoint.getId()+".txt");
		     fw = new FileWriter(f,true);
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
	 
	  public void sendupdaterequest() {
	      //Just publish a random msg for asking update.
		  //System.out.println("Root sent the count and update request");
	  	  int datamu = 0;
	  	  //this.whichwindow +=1;
	  	  //this.whichwindow = windowcntinput;
	  	  //Only Root will receive this message
	  	  this.selfDict.replaceAll((key,value)->1);
	  	  starttimeforeachwindow = System.nanoTime();
	  	  this.logger("Start time of window "+(whichwindow)+" is "+starttimeforeachwindow);
	  	  MsgOfRequestUpdate updatemsg = new MsgOfRequestUpdate(datamu);
	  	  scribe.publish(this.tp, updatemsg);
	  }
	  
	  //The deliver function for topic
      public void deliver(Topic topic, ScribeContent content) {
    	  if (content instanceof MsgOfRequestUpdate){
    		  this.whichwindow +=1;
    		  if (this.whichwindow!=1) {
                 //Reset
    	         this.readrndcnt = (readrate*windowsize)/(keyarr.length);
    		 this.childlist.clear();
    		 this.getchildlist();
    		  if (this.getChildrenParam(this.tp).size() == 0) {
    			  startPublishTaskforleaf();
    			  }
    		  }
    		  else
    		  {
    			  //No child
    			  if (this.getChildrenParam(this.tp).size() == 0) {
    			  startPublishTaskforleaf();
    			  }
    		  }//But if the second time window come you need to stop the former task!!
    	  }
       }
       
      //The deliver function for App
  	  public void deliver(Id id, Message message) {
  		  if(message instanceof BottomNodesMsg) {  		    	
  		    //If you received this msg then it means you are a parent node. You should merge the data you received and then check if its time to upload data to your parent node
  		    	BottomNodesMsg msgrecv = (BottomNodesMsg)message;
  		    	LinkedHashMap<String,Integer> CountRecv = ((BottomNodesMsg) message).getdictdata();
  		    	Id from = ((BottomNodesMsg) message).getIddata();
  		    	CountRecv.forEach((key, value) -> this.selfDict.merge(key, value, (v1,v2) -> v1+v2));
  		    	//this.childlist.remove(from);
  		    	try {
  		    	this.childlist.put(from, childlist.get(from)+1);}
  		    	catch (Exception e) {
  		    		this.loggererror("Null pointer exception happened in " +this.endpoint.getId());
  		    	}
  		    	if (this.getParentParam(this.tp) != null) {
  		    	    //You are not the root node, keep update
  		    		if (check(childlist)==true &&this.selfDict.get("marker")!=1) {
  		    			//Make sure here, your marker data also received. Cuz you are only get it from the last part of bottom data
    	    		
  		    		NodeHandle dst = this.getParentParam(this.tp);
    	    		BottomNodesMsg msgsend = new BottomNodesMsg(this.selfDict,this.endpoint.getId());
    	    		endpoint.route(dst.getId(), msgsend, dst);
    	    		this.selfDict.replaceAll((key,value)->1);
    	    
    	    	}
  		    	}
  		    	else {
		    		//you are the root node,just check when it's time to record end time of this window.
  		    		if (checkmarker(selfDict)==true) {
  		    	    //Or multicast here to cancel the former task before start next window?
  		    		endtimeforeachround = System.nanoTime();
  		    		this.logger("End time of window "+(whichwindow-1)+" is "+endtimeforeachround);

  		    	
  		    	}
  		  }
  		  else if (message instanceof selfschedule50msg) {   	    	
	    		run50();
  		  }
  		  else if (message instanceof Startwindow) {
    		  sendupdaterequest();
    	  }
  	  }     
      
	  public boolean forward(RouteMessage message) {
	    return true;
	  }

	  public void update(NodeHandle handle, boolean joined) {
	    
	  }
	  
	  public boolean anycast(Topic topic, ScribeContent content) {
		    boolean returnValue = scribe.getEnvironment().getRandomSource().nextInt(3) == 0;
		    return returnValue;
	  }
	  
	  public boolean isRoot() {
	    return scribe.isRoot(tp);
	  }
	    
	  public NodeHandle getRoot(Topic tp) {
		  return scribe.getRoot(tp);
	  }
	  
	  public NodeHandle getParentParam(Topic tpinstance) {
	    return ((ScribeImpl)scribe).getParent(tpinstance); 
	  }
	  
	  public Collection<NodeHandle> getChildrenParam(Topic tpinstance) {
	    return scribe.getChildrenOfTopic(tpinstance); 
	  }
	
	  public NodeHandle getParent() {
	    return scribe.getParent(tp); 
	  }	  
	
	  public NodeHandle[] getChildren() {
	    return scribe.getChildren(tp); 
	  }

	  public void childAdded(Topic topic, NodeHandle child) {	
	  }

	  public void childRemoved(Topic topic, NodeHandle child) {
	  }

	  public void subscribeFailed(Topic topic) {
	  }
	
	  public void settopic(Topic tpinstance) {
		this.tp = tpinstance;
	  }
	  
	  public static LinkedHashMap<String, Integer> sortByValue(LinkedHashMap<String, Integer> hm)
	    {
	        // Create a list from elements of LinkedHashMap
	        List<Map.Entry<String, Integer> > list =
	                new LinkedList<Map.Entry<String, Integer> >(hm.entrySet());
	 
	        // Sort the list
	        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
	            public int compare(Map.Entry<String, Integer> o1,
	                               Map.Entry<String, Integer> o2)
	            {
	                return (o2.getValue()).compareTo(o1.getValue());
	            }
	        });
	         
	        // put data from sorted list to LinkedHashMap
	        LinkedHashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
	        for (Map.Entry<String, Integer> aa : list) {
	            temp.put(aa.getKey(), aa.getValue());
	        }
	        return temp;
	    }
	  
}
