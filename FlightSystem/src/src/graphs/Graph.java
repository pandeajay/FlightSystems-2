package src.graphs;

import java.util.List;



import src.graphs.node.Node;

public interface Graph {
	long createNode(Node node);
	long deleteNode(String nodeId);
	void createNodes(List<Node> nodes);
	void createEdges(List<Node> nodes);
	long createEdge(Node node);
	void deleteNodes(List<Node> node);
	double getShortestPathWeight(String from , String to);	
	List<?> getShortestPathVetices(String from , String to);
	void close();
	List<Object> getAllEdges();
	int deleteAllNodes();
	int deleteAllEdges();

}
