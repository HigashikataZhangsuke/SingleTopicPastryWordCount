package yinzhe.test.msg;

import java.io.IOException;
import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.Hashtable;

import rice.p2p.commonapi.*;
import rice.p2p.commonapi.rawserialization.*;
import rice.p2p.util.rawserialization.JavaSerializer;

/**
 * 
 * @author zhangsuke
 * This is the Class for constructing a Msg that contains WordCount Data. It will be sent to some node's parent, from any bottom node.
 * 
 */

public class BottomNodesMsg implements RawMessage {
  
  public static final short TYPE = 1;
  
  Id from;
  public LinkedHashMap<String,Integer> Dict = new LinkedHashMap<String,Integer>();
  
  public BottomNodesMsg(LinkedHashMap<String,Integer> Dictinput,Id frominput) {
    this.Dict = Dictinput;
    this.from = frominput;
  }
  
  public String toString() {
    return "Dict";
  }

  public int getPriority() {
    return Message.LOW_PRIORITY;
  }

  public short getType() {
    return TYPE;
  }

  public LinkedHashMap<String,Integer> getdictdata() {
	  return this.Dict;
  }
  
  public Id getIddata() {
	  return this.from;
  }
  
  public void serialize(OutputBuffer buf) throws IOException {
	//Use Java default Serializer
	JavaSerializer.serialize(this, buf);
  }
}