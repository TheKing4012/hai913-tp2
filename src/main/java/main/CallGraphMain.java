package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import graphs.*;
import processors.ASTProcessor;

public class CallGraphMain extends AbstractMain {

	@Override
	protected void menu() {
		StringBuilder builder = new StringBuilder();
		builder.append("1. Static call graph.");
		builder.append("\n2. Dynamic call graph.");
		builder.append("\n3. Help menu.");
		builder.append("\n"+QUIT+". To quit.");
		
		System.out.println(builder);
	}

	public static void main(String[] args) {	
		CallGraphMain main = new CallGraphMain();
		BufferedReader inputReader;
		CallGraph callGraph = null;
		try {
			inputReader = new BufferedReader(new InputStreamReader(System.in));
			if (args.length < 1)
				setTestProjectPath(inputReader);
			else
				verifyTestProjectPath(inputReader, args[0]);
			String userInput = "";
			
			do {	
				main.menu();			
				userInput = inputReader.readLine();
				main.processUserInput(userInput, callGraph);
				Thread.sleep(3000);
				
			} while(!userInput.equals(QUIT));
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	protected void processUserInput(String userInput, ASTProcessor processor) {
		CallGraph callGraph = (CallGraph) processor;
		try {
			switch(userInput) {
				case "1":
					callGraph = CallGraph.createCallGraph(TEST_PROJECT_PATH);

					Map<String, Map<String, Double>> couplugins = callGraph.calculateCouplingBetweenAllClasses();
					CouplingGraphVisualizer visualizer = new CouplingGraphVisualizer(couplugins);
					visualizer.displayGraph();

					HierarchicalClustering hc = new HierarchicalClustering(couplugins);
					//List<Set<String>> clusters = hc.agglomerativeClustering();

					System.out.println(couplugins);

					System.out.println("Graphe de Couplage entre Classes :");
					for (String classA : couplugins.keySet()) {
						for (Map.Entry<String, Double> entry : couplugins.get(classA).entrySet()) {
							System.out.printf("%s -> %s : %.2f%n", classA, entry.getKey(), entry.getValue());
						}
					}


					// Afficher les clusters générés
					/*
					System.out.println("Clusters après le clustering hiérarchique :");
					for (Set<String> cluster : clusters) {
						System.out.println(cluster);
					}

					/*
					DendrogramVisualizer visualizer = new DendrogramVisualizer(callGraph.generateClassCouplingGraph());
					visualizer.displayDendrogram();

					 */

					// Créer une instance de ModuleIdentification pour identifier les modules
					/*
					ModuleIdentifier moduleIdentification = new ModuleIdentifier(couplugins);

					moduleIdentification.identifyModules(clusters);

					 */

					//callGraph.log();

					break;

				case "2":
					System.err.println("Not implemented yet");
					break;

				case "3":
					return;

				case QUIT:
					System.out.println("Bye...");
					return;

				default:
					System.err.println("Sorry, wrong input. Please try again.");
					return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
