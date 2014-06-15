package graphs.impl;

import graphs.Graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import business.NodeImpl;





public class Jgraph implements Graph {
	
	private final SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> jGraph = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
	
		
	/**
	 * Creates Neo Node from Node
	 * input: node
	 * output: new node id
	 */
	public long createNode(NodeImpl node){
		try{
			jGraph.addVertex(node.id);
		}catch(Exception e){
			System.out.println("Exception in Graph2 add " + e);
		}
		return 1;
	}
	
	
	/**
	 * For a from and to pair return shortest path weight
	 * input : from and to id
	 * output : weight of shortest path between from and to
	 */
	public double getShortestPathWeight(String from , String to){
		DijkstraShortestPath<String, DefaultWeightedEdge> shortpath = new DijkstraShortestPath<String, DefaultWeightedEdge>(this.jGraph, from, to);
		return shortpath.getPathLength();
	}
	
	/**
	 * For a from and to pair return shortest path Edges
	 * input : from and to id
	 * output : Edges of shortest path between from and to
	 */
	public List<DefaultWeightedEdge> getShortestPathEdgeList(String from , String to){
		DijkstraShortestPath<String, DefaultWeightedEdge> shortpath = new DijkstraShortestPath<String, DefaultWeightedEdge>(this.jGraph, from, to);
		return shortpath.getPathEdgeList();
	}
	

	public List getShortestPathVetices(List<DefaultWeightedEdge> edgeList){
		List<String> list = new ArrayList<String>();
		Iterator<DefaultWeightedEdge> it = edgeList.iterator();
		while(it.hasNext()){
			DefaultWeightedEdge edge =  it.next();
			if(!list.contains(jGraph.getEdgeSource(edge) )){
				list.add(jGraph.getEdgeSource(edge));
			}
			if(!list.contains(jGraph.getEdgeTarget(edge) )){
				list.add(jGraph.getEdgeTarget(edge));	
			}
		}
		return list;
		
	}

	/**
	 * For a from and to pair return shortest path vertices
	 * input : from and to id
	 * output : Vertices id of shortest path between from and to
	 */
	@Override
	public List getShortestPathVetices(String from, String to) {
		return getShortestPathVetices(getShortestPathEdgeList(from, to));
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Creates JGraph nodes from Json nodes
	 */
	@Override
	public void createNodes(List<NodeImpl> nodes) {
		for(NodeImpl node : nodes){
			createNode(node);
		}
		
	}

	/**
	 * Creates edges from passed JSon nodes
	 */
	@Override
	public void createEdges(List<NodeImpl> nodes) {
		for(NodeImpl node : nodes){
			createEdge(node);
		}
		
	}

	/**
	 * Creates edges of a specified Node
	 */
	@Override
	public long createEdge(NodeImpl node) {
		Iterator<Entry<String, String>> it = node.to.entrySet().iterator();
		while(it.hasNext()){			
			Entry<String, String> entry = it.next();
			//boolean ff = graph.containsVertex(node.id);
			DefaultWeightedEdge  edge = jGraph.addEdge(node.id, entry.getKey() );
			if(edge!= null && entry.getValue() != null && entry.getValue().length() > 0){
				jGraph.setEdgeWeight(edge, Double.parseDouble(entry.getValue()));
			}
		}
		
		return 1;
		
	}


	/**
	 * Deletes specified node
	 */
	@Override
	public long deleteNode(String nodeId) {
		if(jGraph.removeVertex(nodeId)){
			return 1;
		}else{
			return 0;		
		}
		
	}

	/**
	 * Deletes passed nodes
	 */
	@Override
	public void deleteNodes(List<NodeImpl> nodes) {
		for(NodeImpl node : nodes){
			deleteNode(node.id);
		}		
	}




	@Override
	public List<Object> getAllEdges() {
		List<Object> list = new ArrayList <Object>();
		list.addAll(jGraph.edgeSet());
		return list;
	}

	/**
	 * Deletes all nodes
	 */

	@Override
	public int deleteAllNodes() {
		try{
			Set<String> vertexSet = jGraph.vertexSet();
			List<String> vertexList = new ArrayList<String>();
			for( String vertex : vertexSet){
				vertexList.add(vertex);
			}
			jGraph.removeAllVertices(vertexList);
			return 1;
		}catch(Exception e){
			System.out.print("Exception in deleteAllNodes " + e);
		}
		return 0;
	}


	/**
	 * Deletes all edges
	 */
	@Override
	public int deleteAllEdges() {
		try{
			Set<DefaultWeightedEdge> edgeSet = jGraph.edgeSet();
			List<DefaultWeightedEdge> edgeList = new ArrayList<DefaultWeightedEdge>();
			for( DefaultWeightedEdge edge : edgeSet){
				edgeList.add(edge);
			}
			jGraph.removeAllEdges(edgeList);
			return 1;
		}catch(Exception e){		
			System.out.print("Exception in deleteAllEdges " + e);
		}
		return 0;
		
	}
	
	public SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> getJGraph(){
		return jGraph;		
	}
}
