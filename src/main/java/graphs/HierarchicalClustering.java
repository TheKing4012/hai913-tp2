package graphs;

import java.util.*;

import static org.fusesource.jansi.Ansi.ansi;

public class HierarchicalClustering {

    private Map<String, Map<String, Double>> couplingData;
    private Node dendrogram;

    public HierarchicalClustering(Map<String, Map<String, Double>> couplingData) {
        this.couplingData = couplingData;
        this.dendrogram = performClustering();
        printFullDendrogram();
    }

    private Node performClustering() {
        List<Cluster> clusters = initializeClusters();

        while (clusters.size() > 1) {
            ClusterPair closestPair = findClosestClusters(clusters);
            Cluster mergedCluster = mergeClusters(closestPair);
            clusters.remove(closestPair.c1);
            clusters.remove(closestPair.c2);
            clusters.add(mergedCluster);
        }

        return clusters.get(0).root;
    }

    private List<Cluster> initializeClusters() {
        List<Cluster> clusters = new ArrayList<>();
        for (String className : couplingData.keySet()) {
            clusters.add(new Cluster(new Node(className)));
        }
        return clusters;
    }

    private ClusterPair findClosestClusters(List<Cluster> clusters) {
        ClusterPair closestPair = null;
        double maxCoupling = -1;

        for (int i = 0; i < clusters.size(); i++) {
            for (int j = i + 1; j < clusters.size(); j++) {
                double coupling = getCoupling(clusters.get(i), clusters.get(j));
                if (coupling > maxCoupling) {
                    maxCoupling = coupling;
                    closestPair = new ClusterPair(clusters.get(i), clusters.get(j));
                }
            }
        }

        return closestPair;
    }

    private Cluster mergeClusters(ClusterPair pair) {
        Node mergedNode = new Node(pair.c1.root, pair.c2.root);
        return new Cluster(mergedNode);
    }

    private double getCoupling(Cluster c1, Cluster c2) {
        double coupling = 0;
        for (String class1 : c1.getClassNames()) {
            for (String class2 : c2.getClassNames()) {
                coupling += couplingData.getOrDefault(class1, new HashMap<>()).getOrDefault(class2, 0.0);
            }
        }
        return coupling / (c1.size() * c2.size());
    }

    private static class Cluster {
        Node root;

        public Cluster(Node root) {
            this.root = root;
        }

        public int size() {
            return root.getClassNames().size();
        }

        public Set<String> getClassNames() {
            return root.getClassNames();
        }
    }

    private static class Node {
        String className;
        Node left;
        Node right;

        public Node(String className) {
            this.className = className;
        }

        public Node(Node left, Node right) {
            this.left = left;
            this.right = right;
        }

        public Set<String> getClassNames() {
            Set<String> classNames = new HashSet<>();
            if (className != null) classNames.add(className);
            if (left != null) classNames.addAll(left.getClassNames());
            if (right != null) classNames.addAll(right.getClassNames());
            return classNames;
        }
    }

    private static class ClusterPair {
        Cluster c1;
        Cluster c2;

        public ClusterPair(Cluster c1, Cluster c2) {
            this.c1 = c1;
            this.c2 = c2;
        }
    }

    private void printDendrogram(Node node, String indent, boolean isLeft) {
        if (node == null) return;

        if (node.className != null) {
            System.out.println(indent + (isLeft ? "|-- " : "\\-- ") + node.className);
        } else {
            System.out.println(indent + (isLeft ? "|-- " : "\\-- ") + "Cluster:");
            printDendrogram(node.left, indent + (isLeft ? "|   " : "    "), true);
            printDendrogram(node.right, indent + (isLeft ? "|   " : "    "), false);
        }
    }

    private void printFullDendrogram() {
        System.out.println();
        System.out.println(ansi().fgGreen().a("=== Dendrogramme : ===").reset());
        printDendrogram(dendrogram, "", true);
        System.out.println(ansi().fgGreen().a("=====================================").reset());
        System.out.println();
    }
}
