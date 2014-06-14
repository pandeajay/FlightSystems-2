package graphs;

import java.util.List;
import business.NodeImpl;

public interface Graph {
	long createNode(NodeImpl node);
	long deleteNode(String nodeId);
	void createNodes(List<NodeImpl> nodes);
	void createEdges(List<NodeImpl> nodes);
	long createEdge(NodeImpl node);
	void deleteNodes(List<NodeImpl> node);
	double getShortestPathWeight(String from , String to);	
	List<?> getShortestPathVetices(String from , String to);
	void close();
	List<Object> getAllEdges();
	int deleteAllNodes();
	int deleteAllEdges();

}
