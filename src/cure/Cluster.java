package cure;
import java.util.ArrayList;

/**
 * Class Cluster represents a collection of points and its set of representative points.
 * It also stores the distance from its closest neighboring cluster.
 * 
 * @version 1.0
 * @author jmishra
 */
public class Cluster {

	public ArrayList rep = new ArrayList();
	public ArrayList pointsInCluster = new ArrayList();
	public double distanceFromClosest = 0;
	public Cluster closestCluster;
	public ArrayList closestClusterRep = new ArrayList();
	
	public double computeDistanceFromCluster(Cluster cluster) {
		double minDistance = 1000000;
		for(int i = 0; i<rep.size(); i++) {
			for(int j = 0; j<cluster.rep.size() ; j++) {
				Point p1 = (Point)rep.get(i);
				Point p2 = (Point)cluster.rep.get(j);
				double distance = p1.calcDistanceFromPoint(p2);
				if(minDistance > distance) minDistance = distance;
			}
		}
		return minDistance;
	}
	
	public int getClusterSize() {
		return pointsInCluster.size();
	}
	
	public ArrayList getPointsInCluster() {
		return pointsInCluster;
	}
}