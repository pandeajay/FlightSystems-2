package graphs.impl;

import graphs.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.RestGraphDatabase;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.server.rest.web.RestfulGraphDatabase;





public class NeoGraph implements Graph{
	
	GraphDatabaseService graphDb = null;
	static Map<String, String > nodeIAndNeoId = new HashMap<String, String>();
	static List<String> edgeList = new ArrayList<String>();
	
	
	/**
	 * Constructor
	 */
	public NeoGraph(){
		try{
			if(graphDb == null){
				graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( "E:\\EmbeddedDB" );
			}
			if(graphDb == null){
				throw new Exception("can not initialize Neo graph ");
			}
			
		}catch(Exception e){
			System.out.println("Exception "+ e ) ;
		}
	}

	public static void main(String[] args){
		NeoGraph neoGraph = new NeoGraph();
		
		List<Map<String,String>> toListWithWeight = new ArrayList<Map<String,String>> ();
		Map<String, String> map = new HashMap<String, String>();
		map.put("2", "2");
		map.put("3","3");
		map.put("4", "100");
		toListWithWeight.add(map);			
		business.NodeImpl node1 = new business.NodeImpl("1", toListWithWeight);
		map.clear();
		toListWithWeight.clear();
		
		long id = neoGraph.createNode(node1);
		Node newNode = neoGraph.getNode(id);
		System.out.println("NeoGraph newNode " + newNode);
		
	}


	/**
	 * Returns Node from id
	 * @param id
	 * @return Node
	 */
	public Node getNode(long id) {
		Node node = null;
		try (Transaction tx = graphDb.beginTx()) {
			node = graphDb.getNodeById(id);
			tx.success();
		} catch (Exception e) {
			System.out.println("Exception in NeoGraph createNode " + e);
		}
		return node;

	}

	/**
	 * Creates Neo Node from Node
	 * input: node
	 * output: new node id
	 */

	@Override
	public long createNode(business.NodeImpl node) {
		if(!validateNode(node)){
			return 0;
		}
		
		Node myNode = null;
		try ( Transaction tx = graphDb.beginTx() ){
			 myNode = graphDb.createNode();
			 myNode.setProperty( "id", node.id);
			 myNode.setProperty( "to", node.to.toString());	
			 NeoGraph.nodeIAndNeoId.put(node.id, ""+myNode.getId());
			 tx.success();
		}catch(Exception e){
			System.out.println("Exception in NeoGraph createNode " + e);
			return 0;
		}
		return myNode.getId();
		
	}

	/**
	 * Creates Edge for passed node
	 * input : node
	 * output : edgeId 
	 * 
	 */
	@Override
	public long createEdge(business.NodeImpl node) {
		if(!validateNode(node)){
			return 0;
		}
		try ( Transaction tx = graphDb.beginTx() ){
			long fromId = 0 ;
			try{
				fromId = Long.parseLong(NeoGraph.nodeIAndNeoId.get(node.id));
			}catch(Exception ex){
				System.out.println("Exception in NeoGraph createEdges. From node does not exist" );
				return 0;
			}
			Node fromNode = graphDb.getNodeById(fromId);			
			Iterator<Entry<String, String>> it = node.to.entrySet().iterator();
			while(it.hasNext()){
				Entry<String, String> entry = it.next();
				long toId = 0 ;
				try{
					toId = Long.parseLong(NeoGraph.nodeIAndNeoId.get(entry.getKey()));
				}catch(Exception ex){
					System.out.println("Exception in NeoGraph createEdges. To node does not exist" );
					return 0;
				}
				Node toNode = graphDb.getNodeById(toId);
				Relationship edge = fromNode.createRelationshipTo(toNode, RelTypes.TravellingTo);
				edge.setProperty("weight", entry.getValue());
				edgeList.add(""+edge.getId());						
			}	
			 tx.success();
			 
			 return 1;
		}catch(Exception e){
			System.out.println("Exception in NeoGraph createEdges " + e);
		}		
		
		return 0;
		
	}


	/**
	 * For a from and to pair return shortest path weight
	 * input : from and to id
	 * output : weight of shortest path between from and to
	 */

	@Override
	public double getShortestPathWeight(String from, String to) {		
		double minWeight = 0.0 ;
		
		if(from == null || from.length() == 0 || to == null || to.length() == 0){
			System.out.println("Exception in getShortestPathWeight. Either from or to is not specified  " );
			return 0.0;			
		}
		
		try (Transaction tx = graphDb.beginTx()) {
			long fromId = Long.parseLong(NeoGraph.nodeIAndNeoId.get(from));
			long toId = Long.parseLong(NeoGraph.nodeIAndNeoId.get(to));
			Node fromNode = null;
			Node toNode = null ;
			
			try{
				fromNode = graphDb.getNodeById(fromId);			
				toNode = graphDb.getNodeById(toId);
			}catch(Exception ex){
				System.out.println("Exception in getShortestPathWeight. From or To node does not exist " + ex);
				return 0.0;
			}

			PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(
				    PathExpanders.forTypeAndDirection( RelTypes.TravellingTo, Direction.OUTGOING ), "weight" );
						
			Iterable<WeightedPath> paths = finder.findAllPaths(fromNode, toNode);
			Iterator<WeightedPath> it = paths.iterator();
			int i = 0;
			while(it.hasNext()){
				WeightedPath  path = it.next();
				if(i == 0){
					minWeight = path.weight();
					i++;
				}
				if(minWeight > path.weight()){
					minWeight = path.weight();
				}
			}
		}catch(Exception e){
			System.out.println("Exception in getShortestPathWeight  " + e);
			return 0.0;
		}
		return minWeight;
	}


	/**
	 * Gets node form Neo node id
	 * @param neoId
	 * @return
	 */
	String getNodeIdFromNeoNodeId(long neoId){
		Iterator<Entry<String, String>> it = NeoGraph.nodeIAndNeoId.entrySet().iterator();
		
		while(it.hasNext()){
			Entry<String, String> entry = it.next();
			if(neoId == Long.parseLong(entry.getValue())){
				return entry.getKey();
			}
		}		
		
		return null;
		
	}

	/**
	 * For a from and to pair return shortest path vertices
	 * input : from and to id
	 * output : Vertices id of shortest path between from and to
	 */
	@Override
	public List getShortestPathVetices(String from, String to) {
		List<String> list = new ArrayList<String>();
		
		if(from == null || from.length() == 0 || to == null || to.length() == 0){
			System.out.println("Exception in getShortestPathVetices. Either from or to is not specified  " );
			return list ;			
		}
		
		try (Transaction tx = graphDb.beginTx()) {
			long fromId = Long.parseLong(NeoGraph.nodeIAndNeoId.get(from));
			long toId = Long.parseLong(NeoGraph.nodeIAndNeoId.get(to));
			
			Node fromNode = null;
			Node toNode = null ;
			
			try{
				fromNode = graphDb.getNodeById(fromId);			
				toNode = graphDb.getNodeById(toId);
			}catch(Exception ex){
				System.out.println("Exception in getShortestPathVetices. From or To node does not exist " + ex);
				return list;
			}
			
			PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(
				    PathExpanders.forTypeAndDirection( RelTypes.TravellingTo, Direction.OUTGOING ), "weight" );			
			
			Iterable<WeightedPath> paths = finder.findAllPaths(fromNode, toNode);
			Iterator<WeightedPath> it = paths.iterator();
			while(it.hasNext()){
				Path path = it.next();				
				Iterable<Node> nodes = path.nodes();
				Iterator<Node> it2 = nodes.iterator();
				while(it2.hasNext()){
					Node tempNode = it2.next();					
					list.add(getNodeIdFromNeoNodeId(tempNode.getId()));
				}
			}			
			tx.success();
		} catch (Exception e) {
			System.out.println("Exception in NeoGraph getShortestPathVetices " + e);
		}
		return list;

	}
	
	private static enum RelTypes implements RelationshipType
	{
	    TravellingTo
	}

	@Override
	public void close() {
			graphDb.shutdown();
	}

	/**
	 * Creates neo nodes from Json nodes
	 */
	@Override
	public void createNodes(List<business.NodeImpl> nodes) {
		if(nodes == null || nodes.size() == 0){
			System.out.println("NeoGraph createNodes passed empty list of nodes");
			return;
		}
		for(business.NodeImpl node : nodes){
			createNode(node);
		}	
	}

	/**
	 * Creates edges from passed JSon nodes
	 */
	@Override
	public void createEdges(List<business.NodeImpl> nodes) {
		if(nodes == null || nodes.size() == 0){
			System.out.println("NeoGraph createEdges passed empty list of nodes");
			return;
		}
		for(business.NodeImpl node : nodes){
			createEdge(node);
		}	
		
	}

	/**
	 * Deletes a nodes based on nodeId
	 */
	@Override
	public long deleteNode(String nodeId) {
		if(nodeId == null || nodeId.length() == 0 ){
			System.out.println("Error in NeoGraph deleteNode.Pass valid nodeId " );
			return 0;
		}
		try (Transaction tx = graphDb.beginTx()) {
			Node tempNode = graphDb.getNodeById(Long
					.parseLong(NeoGraph.nodeIAndNeoId.get(nodeId)));
			tempNode.delete();
			NeoGraph.nodeIAndNeoId.remove(nodeId);
			tx.success();
			return 1;
		} catch (Exception e) {
			System.out.println("Exception in NeoGraph deleteNode " + e);
		}
		return 0;
	}
	
	/**
	 * Deletes nodes from passed node ids
	 */

	@Override
	public void deleteNodes(List<business.NodeImpl> nodes) {
		if(nodes == null || nodes.size() == 0){
			System.out.println("NeoGraph deleteNodes passed empty list of nodes");
			return;
		}
		try (Transaction tx = graphDb.beginTx()) {
			for (business.NodeImpl node : nodes) {
				deleteNode(node.id);
			}
			tx.success();
		} catch (Exception e) {
			System.out.println("Exception in NeoGraph getShortestPathVetices "
					+ e);
		}
	}
	

	/**
	 * gets all nodes in a graph
	 * @return
	 */
	public List<Object> getAllNodes() {
		List<Object> list = new ArrayList<Object>();
		Iterator<Node> it = graphDb.getAllNodes().iterator();
		while(it.hasNext()){
			list.add(it.next());
		}
		return list;
		
	}

	/**
	 * Gets all edges in a graph
	 */
	@Override
	public List<Object> getAllEdges() {
		graphDb.getAllNodes();		
		return null;
	}
	
	/**
	 * deletes all nodes in a graph
	 */
	@Override
	public int deleteAllNodes() {
		try (Transaction tx = graphDb.beginTx()) {
			List<String> nodes = new ArrayList<String>();
			Iterator<Entry<String, String>> it = NeoGraph.nodeIAndNeoId.entrySet().iterator();
			while (it.hasNext()) {
				nodes.add(it.next().getKey());
			}	
			for(String nodeId : nodes){
				deleteNode(nodeId);				
			}
			tx.close();
			//tx.success();
		} catch (Exception e) {
			System.out.println("Exception in NeoGraph deleteAllNodes " 	+ e);
			return 0;
		}
		return 1;
	}
	
	/**
	 * deletes all edges from a graph
	 */
	@Override
	public int deleteAllEdges() {
		try (Transaction tx = graphDb.beginTx()) {			
			for(String edgeId : edgeList){
				Relationship edge = graphDb.getRelationshipById(Long.parseLong(edgeId));
				edge.delete();
			}			
			tx.success();
		} catch (Exception e) {
			System.out.println("Exception in NeoGraph deleteAllEdges " 	+ e);
			return 0;
		}
		return 1;
	}
	
	/**
	 * Verifies passed node so that Neo node can be created
	 * @param node
	 * @return
	 */
	boolean validateNode(business.NodeImpl node){
		if(node == null){
			System.out.println("NeoGraph createEdge : node is null ");
			return false;
		}
		if(node.id == null || node.id.length() == 0){
			System.out.println("NeoGraph createEdge : node id is null ");
			return false;
		}
		return true;
	}
}
