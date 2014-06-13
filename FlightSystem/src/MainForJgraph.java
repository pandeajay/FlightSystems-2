import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jgrapht.graph.DefaultWeightedEdge;

import src.graphs.Graph;
import src.graphs.Jgraph;
import src.graphs.node.Node;
import utilities.Utils;


public class MainForJgraph {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new MainForJgraph().start();

	}

	private void start() {		
		Map<String, String> userInputMap = Utils.getDataNodesFile();		
		String nodesDataPath = userInputMap.get("DataFile");
		List<Node> newNodesList = Utils.getAllNodesFromJson(nodesDataPath);	
		
		Jgraph graph = new Jgraph();	
		graph.createNodes(newNodesList);
		graph.createEdges(newNodesList);
		
		List<String> queryList = Utils.getFromAndToList(userInputMap);
		for(String str : queryList){
			String[] strs = str.split("-");
			if(strs.length == 2){
				
				double weight = graph.getShortestPathWeight(strs[0], strs[1]);				
				List<?> edgeList = graph.getShortestPathVetices(strs[0], strs[1]);				
				Iterator<?> it = edgeList.iterator();				
				String shortestPath = "";
				while(it.hasNext()){
					Object edge = it.next();
					shortestPath += edge + "-";			
					
				}		
				if(shortestPath != null && shortestPath.length() > 0){
					shortestPath = shortestPath.substring(0,shortestPath.length() -1 );
				}
				
				System.out.println("For fromtoPair = " + strs[0]+"--" + strs[1] 
						+ "     shortestPath = " + (shortestPath.length() == 0 ? "Path does not exist"  : shortestPath) 
						+ " and its weight = " + weight + (weight > 999 ? "   practically impossible" : ""));
			}
		}
		
	
	}
}
