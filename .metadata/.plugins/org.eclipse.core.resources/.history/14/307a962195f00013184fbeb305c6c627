package src.graphs;

import java.util.List;


import src.graphs.node.Node;

public interface Graph {
	long createNode(Node node);
	long deleteNode(Node node);
	void createNodes(List<Node> nodes);
	void createEdges(List<Node> nodes);
	void createEdge(Node node);
	void deleteNodes(List<Node> node);
	double getShortestPathWeight(String from , String to);

	// List getShortestPathVetices(List<DefaultWeightedEdge> edgeList);
	List getShortestPathVetices(String from , String to);
	void close();

}
