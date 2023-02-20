package yinzhe.test.msg;

import rice.p2p.scribe.ScribeContent;

/**
 * 
 * @author zhangsuke
 * This is the Class for Constructing a RequestUpdateWordCountData Message. Data could be any type.
 * 
 */

public class MsgOfRequestUpdate implements ScribeContent {
	  int data;
	  public MsgOfRequestUpdate(int data) {
		  this.data = data;
	  }
}
