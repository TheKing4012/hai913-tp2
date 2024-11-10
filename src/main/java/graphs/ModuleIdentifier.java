package graphs;
import java.util.*;

public class ModuleIdentifier {

    private Map<String, Map<String, Double>> couplingGraph;
    private double CP = 0.1;
    private int maxModules;

    public ModuleIdentifier(Map<String, Map<String, Double>> couplingGraph) {
        this.couplingGraph = couplingGraph;
        this.maxModules = couplingGraph.size() / 2;
    }

    public void identifyModules(List<Set<String>> clusters) {
        List<Set<String>> modules = new ArrayList<>();

        for (Set<String> cluster : clusters) {
            double totalCoupling = 0.0;
            int pairCount = 0;

            for (String classA : cluster) {
                for (String classB : cluster) {
                    if (!classA.equals(classB)) {
                        totalCoupling += couplingGraph.getOrDefault(classA, Collections.emptyMap())
                                .getOrDefault(classB, 0.0);
                        pairCount++;
                    }
                }
            }

            double averageCoupling = pairCount > 0 ? totalCoupling / pairCount : 0.0;

            if (averageCoupling > CP && modules.size() < maxModules) {
                modules.add(cluster);
                System.out.println("Module identifié : " + cluster + ", Couplage moyen : " + averageCoupling);
            } else {
                System.out.println("Module rejeté : " + cluster + ", Couplage moyen : " + averageCoupling);
            }
        }

        System.out.println("\nModules identifiés :");
        for (Set<String> module : modules) {
            System.out.println(module);
        }
    }

}
