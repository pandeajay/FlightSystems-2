package src.graphs;


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
	
	
	
	public NeoGraph(){
		try{
			if(graphDb == null){
				graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( "C:\\Users\\apande\\Documents\\GitHub\\FlightSystems\\EmbeddedDB" );
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
		src.graphs.node.Node node1 = new src.graphs.node.Node("1", toListWithWeight);
		map.clear();
		toListWithWeight.clear();
		
		long id = neoGraph.createNode(node1);
		Node newNode = neoGraph.getNode(id);
		System.out.println("NeoGraph newNode " + newNode);
		
	}


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


	@Override
	public long createNode(src.graphs.node.Node node) {
		if(node == null){
			System.out.println("NeoGraph createNode : node is null ");
			return 0;
		}
		if(node.id == null || node.id.length() == 0){
			System.out.println("NeoGraph createNode : node id is null ");
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

	@Override
	public long createEdge(src.graphs.node.Node node) {
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



	@Override
	public double getShortestPathWeight(String from, String to) {
		
		double minWeight = 0.0 ;
		
		try (Transaction tx = graphDb.beginTx()) {
			long fromId = Long.parseLong(NeoGraph.nodeIAndNeoId.get(from));
			long toId = Long.parseLong(NeoGraph.nodeIAndNeoId.get(to));
			
			Node fromNode = graphDb.getNodeById(fromId);			
			Node toNode = graphDb.getNodeById(toId);			
			


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
		}
		return minWeight;
	}


	
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

	@Override
	public List getShortestPathVetices(String from, String to) {
		List<String> list = new ArrayList<String>();
		
		try (Transaction tx = graphDb.beginTx()) {
			long fromId = Long.parseLong(NeoGraph.nodeIAndNeoId.get(from));
			long toId = Long.parseLong(NeoGraph.nodeIAndNeoId.get(to));
			
			Node fromNode = graphDb.getNodeById(fromId);			
			Node toNode = graphDb.getNodeById(toId);
			
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

	@Override
	public void createNodes(List<src.graphs.node.Node> nodes) {
		for(src.graphs.node.Node node : nodes){
			createNode(node);
		}	
	}

	@Override
	public void createEdges(List<src.graphs.node.Node> nodes) {
		for(src.graphs.node.Node node : nodes){
			createEdge(node);
		}	
		
	}

	@Override
	public long deleteNode(String nodeId) {
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

	@Override
	public void deleteNodes(List<src.graphs.node.Node> nodes) {
		try (Transaction tx = graphDb.beginTx()) {
			for (src.graphs.node.Node node : nodes) {
				deleteNode(node.id);
			}
			tx.success();
		} catch (Exception e) {
			System.out.println("Exception in NeoGraph getShortestPathVetices "
					+ e);
		}
	}
	

	public List<Object> getAllNodes() {
		List<Object> list = new ArrayList<Object>();
		Iterator<Node> it = graphDb.getAllNodes().iterator();
		while(it.hasNext()){
			list.add(it.next());
		}
		return list;
		
	}

	@Override
	public List<Object> getAllEdges() {
		graphDb.getAllNodes();		
		return null;
	}
	
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
}
