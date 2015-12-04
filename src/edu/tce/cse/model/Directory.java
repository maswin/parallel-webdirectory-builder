package edu.tce.cse.model;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.tce.cse.document.DocNode;
public class Directory {
	public Map<Integer, List<Long>> directoryMap;
	public Directory(){
		directoryMap = new HashMap();
	}
}
