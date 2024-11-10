package graphs;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.LongStream;

import org.eclipse.jdt.core.dom.*;

import processors.ASTProcessor;
import utility.Utility;
import visitors.ClassDeclarationsCollector;
import visitors.MethodDeclarationsCollector;
import visitors.MethodInvocationsCollector;

public class CallGraph extends ASTProcessor {
	/* ATTRIBUTES */
	private Set<String> methods = new HashSet<>();
	private Map<String, Map<String, Integer>> invocations = new HashMap<>();
	private Map<String, String> variableTypeMap = new HashMap<>();

	/* CONSTRUCTOR */
	public CallGraph(String projectPath) {
		super(projectPath);
	}

	public Set<String> getMethods() {
		return methods;
	}

	public long getNbMethods() {
		return methods.size();
	}

	public long getNbInvocations() {
		return invocations.keySet()
				.stream()
				.map(source -> invocations.get(source))
				.map(destination -> destination.values())
				.flatMap(Collection::stream)
				.flatMapToLong(value -> LongStream.of((long) value))
				.sum();
	}

	public Map<String, Map<String, Integer>> getInvocations() {
		return invocations;
	}

	public void trackObjectInstantiation(String variableName, String className) {
		if (variableTypeMap.containsKey(variableName)) {
			String existingType = variableTypeMap.get(variableName);
			if (!existingType.equals(className)) {
				variableTypeMap.put(variableName, className);
				System.out.println("Updated variable type for " + variableName + " to " + className);
			}
		} else {
			variableTypeMap.put(variableName, className);
			System.out.println("Mapped variable " + variableName + " to class " + className);
		}
	}


	public boolean addMethod(String method) {
		return methods.add(method);
	}

	public boolean addMethods(Set<String> methods) {
		return methods.addAll(methods);
	}

	public void addInvocation(String source, String destination) {
		String sourceClass = getClassNameFromMethod(source);
		String destClass = getClassNameFromMethod(destination);

		// Résoudre dynamiquement le type de source si c'est une variable
		if (variableTypeMap.containsKey(source)) {
			sourceClass = variableTypeMap.get(source);
		}

		// Résoudre dynamiquement le type de destination si c'est une variable
		if (variableTypeMap.containsKey(destination)) {
			destClass = variableTypeMap.get(destination);
		}

		if (invocations.containsKey(sourceClass)) {
			if (invocations.get(sourceClass).containsKey(destClass)) {
				int currentInvocations = invocations.get(sourceClass).get(destClass);
				invocations.get(sourceClass).put(destClass, currentInvocations + 1);
			} else {
				invocations.get(sourceClass).put(destClass, 1);
			}
		} else {
			invocations.put(sourceClass, new HashMap<>());
			invocations.get(sourceClass).put(destClass, 1);
		}
	}


	public void addInvocation(String source, String destination, int occurrences) {
		methods.add(source);
		methods.add(destination);

		if (!invocations.containsKey(source))
			invocations.put(source, new HashMap<String, Integer>());

		invocations.get(source).put(destination, occurrences);
	}

	public void addInvocations(Map<String, Map<String, Integer>> map) {
		for (String source : map.keySet())
			for (String destination : map.get(source).keySet())
				this.addInvocation(source, destination, map.get(source).get(destination));
	}


	private String getClassNameFromMethod(String methodName) {
		// Extraire le nom de la classe à partir du nom complet de la méthode (format: className::methodName)
		int index = methodName.indexOf("::");
		if (index != -1) {
			return methodName.substring(0, index);
		}
		return methodName;
	}

	// Collecte des méthodes et invocations à partir d'un projet
	public static CallGraph createCallGraph(String projectPath) throws IOException {
		CallGraph graph = new CallGraph(projectPath);

		for (CompilationUnit cUnit : graph.parser.parseProject()) {
			graph.collectMethodsAndInvocations(cUnit);
		}

		return graph;
	}

	// Collecte des méthodes et des invocations d'une unité de compilation
	private void collectMethodsAndInvocations(CompilationUnit cUnit) {
		ClassDeclarationsCollector classCollector = new ClassDeclarationsCollector();
		cUnit.accept(classCollector);

		for (TypeDeclaration cls : classCollector.getClasses()) {
			MethodDeclarationsCollector methodCollector = new MethodDeclarationsCollector();
			cls.accept(methodCollector);

			for (MethodDeclaration method : methodCollector.getMethods()) {
				String methodName = Utility.getMethodFullyQualifiedName(cls, method);
				this.addMethod(methodName);

				// Collecte des invocations de méthode
				MethodInvocationsCollector invocationCollector = new MethodInvocationsCollector();
				this.addInvocations(cls, method, methodName, invocationCollector);
				this.addSuperInvocations(methodName, invocationCollector);
			}
		}
	}

	private void addInvocations(TypeDeclaration cls, MethodDeclaration method,
								String methodName, MethodInvocationsCollector invocationCollector) {
		method.accept(invocationCollector);

		for (MethodInvocation invocation : invocationCollector.getMethodInvocations()) {
			String invocationName = getMethodInvocationName(cls, invocation);
			this.addMethod(invocationName);
			this.addInvocation(methodName, invocationName);
		}
	}

	private String getMethodInvocationName(TypeDeclaration cls, MethodInvocation invocation) {
		Expression expr = invocation.getExpression();
		String invocationName = "";

		if (expr != null) {
			ITypeBinding type = expr.resolveTypeBinding();

			if (type != null)
				invocationName = type.getQualifiedName() + "::" + invocation.getName().toString();
			else
				invocationName = expr + "::" + invocation.getName().toString();
		} else {
			invocationName = Utility.getClassFullyQualifiedName(cls)
					+ "::" + invocation.getName().toString();
		}

		return invocationName;
	}

	private void addSuperInvocations(String methodName, MethodInvocationsCollector invocationCollector) {
		for (SuperMethodInvocation superInvocation : invocationCollector.getSuperMethodInvocations()) {
			String superInvocationName = superInvocation.getName().getFullyQualifiedName();
			this.addMethod(superInvocationName);
			this.addInvocation(methodName, superInvocationName);
		}
	}

	// Méthode pour normaliser les noms de classes en minuscule
	private String normalizeClassName(String className) {
		return className.toLowerCase(); // On choisit de normaliser tout en minuscule
	}

	// Méthode pour fusionner les invocations de deux classes ayant des noms similaires
	private void mergeClassInvocations(Map<String, Map<String, Integer>> invocations) {
		Map<String, Map<String, Integer>> mergedInvocations = new HashMap<>();

		for (String sourceClass : invocations.keySet()) {
			String normalizedSource = normalizeClassName(sourceClass);

			// Fusionner les invocations pour les classes similaires
			for (String destinationClass : invocations.get(sourceClass).keySet()) {
				String normalizedDestination = normalizeClassName(destinationClass);

				// Si la classe source et la destination sont similaires (casse différente), on fusionne
				if (!normalizedSource.equals(normalizedDestination)) {
					// Fusionner ou ajouter l'invocation selon les cas
					mergedInvocations.computeIfAbsent(normalizedSource, k -> new HashMap<>())
							.merge(normalizedDestination, invocations.get(sourceClass).get(destinationClass), Integer::sum);
				}
			}
		}

		// Remplacer les invocations originales par les fusionnées
		invocations.clear();
		invocations.putAll(mergedInvocations);
	}

	// Méthode pour calculer et afficher le couplage entre les classes, après fusion
	public Map<String, Map<String, Double>> calculateCouplingBetweenAllClasses() {
		Map<String, Map<String, Double>> couplings = new HashMap<>();
		mergeClassInvocations(this.invocations);  // Appel de la fusion avant de calculer le couplage

		Set<String> allClasses = new HashSet<>();
		// Extraire les classes à partir des méthodes
		for (String method : methods) {
			String className = getClassNameFromMethod(method);
			allClasses.add(normalizeClassName(className)); // On normalise aussi ici
		}

		// Calculer le couplage entre chaque paire de classes
		for (String classA : allClasses) {
			Map<String, Double> associations = new HashMap<>();
			for (String classB : allClasses) {
				if (!classA.equals(classB)) { // Exclure le couplage d'une classe avec elle-même
					double coupling = calculateCoupling(classA, classB);
					//System.out.printf("Coupling between %s and %s: %.2f%n", classA, classB, coupling);
					associations.put(classB, coupling);
				}
			}
			couplings.put(classA, associations);
		}

		return couplings;
	}

	// Méthode pour calculer le couplage entre deux classes (après fusion)
	public double calculateCoupling(String classA, String classB) {
		int invocationsBetweenClasses = 0;
		int totalInvocationsA = 0;

		for (Map.Entry<String, Map<String, Integer>> entry : invocations.entrySet()) {
			String sourceClass = entry.getKey();

			if (sourceClass.startsWith(classA)) {
				for (Map.Entry<String, Integer> invocation : entry.getValue().entrySet()) {
					String destinationClass = invocation.getKey();

					if (destinationClass.startsWith(classB)) {
						invocationsBetweenClasses += invocation.getValue();
					}
				}
				totalInvocationsA += entry.getValue().values().stream().mapToInt(Integer::intValue).sum();
			}
		}

		return totalInvocationsA == 0 ? 0.0 : (double) invocationsBetweenClasses / totalInvocationsA;
	}

	// Méthode pour afficher les invocations après la fusion des classes
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Static Call Graph");
		builder.append("\nMethods: " + methods.size() + ".");
		builder.append("\nInvocations: " + getNbInvocations() + ".");
		builder.append("\n");

		for (String source : invocations.keySet()) {
			builder.append(source + ":\n");

			for (String destination : invocations.get(source).keySet())
				builder.append("\t---> " + destination +
						" (" + invocations.get(source).get(destination) + " time(s))\n");
			builder.append("\n");
		}

		return builder.toString();
	}
}
