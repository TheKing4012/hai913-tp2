package graphs;

import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class CouplingGraphVisualizer {

    private Map<String, Map<String, Double>> couplingGraph;

    public CouplingGraphVisualizer(Map<String, Map<String, Double>> couplingGraph) {
        this.couplingGraph = couplingGraph;
    }

    public void displayGraph() {
        Graph gsGraph = new SingleGraph("Coupling Graph");

        // Ajouter tous les nœuds pour chaque clé dans le graphe
        for (String className : couplingGraph.keySet()) {
            Node node = gsGraph.addNode(className);
            node.setAttribute("ui.label", className);
        }

        // Ajouter les arêtes pondérées
        for (String classSource : couplingGraph.keySet()) {
            for (String classDestination : couplingGraph.get(classSource).keySet()) {
                double weight = couplingGraph.get(classSource).get(classDestination);

                // Nombre d'arcs basé sur le poids (arrondi)
                int edgeCount = (int) Math.round(weight * 10);
                for (int i = 0; i < edgeCount; i++) {
                    String edgeId = classSource + "-" + classDestination + "-" + i;
                    try {
                        Edge edge = gsGraph.addEdge(edgeId, classSource, classDestination, false);
                        edge.setAttribute("ui.label", String.format("%.2f", weight));
                    } catch (org.graphstream.graph.EdgeRejectedException e) {
                        System.out.println("Arête rejetée: " + edgeId);
                    }
                }
            }
        }

        // Style du graphe
        String styleSheet = "node { text-size: 16; fill-color: lightgray; } " +
                "edge { text-size: 14; }";

        gsGraph.setAttribute("ui.stylesheet", styleSheet);

        // Afficher le graphe
        gsGraph.display();
    }
}
