package benchmarker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MinCluster {
	long clusterId;
	Set<String> files;
	List<MinCluster> children;
	int level;
	MinCluster(){
		files = new HashSet<String>();
		children = new ArrayList<MinCluster>();	
		level = -1;
	}
	public void addFile(String child){
		files.add(child);
	}
	public void addFile(Set child){
		files.addAll(child);
	}
	public void addChild(MinCluster child){
		children.add(child);
	}
}
