package cli;

import graphs.CallGraph;
import graphs.CouplingGraphVisualizer;
import graphs.HierarchicalClustering;
import graphs.ModuleIdentifier;
import main.AbstractMain;
import utils.ColorHelper;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.fusesource.jansi.Ansi.ansi;

public class CouplingCLI extends AbstractMain {
    private static String PROJECT_PATH;

    public static IntegerInputProcessor inputProcessor;
    private static final String QUIT = "0";

    private static CallGraph callGraph;
    private static Map<String, Map<String, Double>> couplgins;
    private static List<Set<String>> clusters;

    public boolean isProjectSelected() {
        return PROJECT_PATH != null;
    }

    public boolean areClusterDefined() {
        return clusters != null;
    }

    public void run(){
        BufferedReader inputReader;
        String userInput = "";
        try {
            inputReader = new BufferedReader(new InputStreamReader(System.in));
            do {
                menu();
                userInput = inputReader.readLine();
                processUserInput(inputReader, userInput);
            } while (!userInput.equals(QUIT));
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void menu() {
        System.out.println(ansi().fgGreen().a("=== Menu principal de l'application ===").reset());
        System.out.println(ansi().fgYellow().a("0. Quitter").reset());

        if (isProjectSelected()) {
            System.out.println(ColorHelper.info("1. Changer de projet"));
            System.out.println(ColorHelper.info("2. Générer couplage"));
            System.out.println(ColorHelper.info("3. Afficher clusters hierarchiques"));
            System.out.println(ColorHelper.info("4. Afficher les modules"));
        } else {
            System.out.println(ColorHelper.info("1. Sélectionner un projet"));
        }
        System.out.println(ansi().fgGreen().a("=====================================").reset());
    }


    private void processUserInput(BufferedReader reader, String userInput) {
        inputProcessor = new IntegerInputProcessor(reader);
        try {
            switch (userInput) {
                case "1":
                    selectProject(reader);
                    callGraph = CallGraph.createCallGraph(PROJECT_PATH);
                    couplgins = callGraph.calculateCouplingBetweenAllClasses();

                    //{a={b=0.5, system.out=0.5}, b={a=0.25, system.out=0.75}, system.out={a=0.0, b=0.0}}
                    break;

                case "2":
                    if (isProjectSelected()) {
                        CouplingGraphVisualizer visualizer = new CouplingGraphVisualizer(couplgins);
                        visualizer.displayGraph();
                    } else {
                        System.err.println(ansi().fgCyan().a("Erreur: Aucun projet sélectionné."));
                    }
                    break;

                case "3":
                    if (isProjectSelected()) {
                        new HierarchicalClustering(couplgins);
                    } else {
                        System.err.println(ColorHelper.error("Erreur: Aucun projet sélectionné."));
                    }
                    break;

                case "4":
                    if (isProjectSelected() && areClusterDefined()) {
                        ModuleIdentifier moduleIdentification = new ModuleIdentifier(couplgins);

                        moduleIdentification.identifyModules(clusters);
                    } else {
                        System.err.println(ColorHelper.error("Erreur: Aucun projet sélectionné."));
                    }
                    break;

                case QUIT:
                    System.out.println(ansi().fgGreen().a("Revenez bientôt !").reset());
                    return;

                default:
                    System.err.println(ColorHelper.error("Erreur: Veuillez réessayer."));
                    return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Méthode pour sélectionner le projet
    private void selectProject(BufferedReader reader) throws IOException {
        System.out.println("");
        System.out.println(ansi().fgGreen().a("=== Menu choix de saisie ===").reset());
        System.out.println(ColorHelper.info("1. Ouvrir explorateur de fichiers"));
        System.out.println(ColorHelper.info("2. Entrer manuellement le chemin d'un projet Java"));
        System.out.println(ansi().fgGreen().a("=====================================").reset());
        String choice = reader.readLine();

        String projectPath = null;
        if ("1".equals(choice)) {
            projectPath = openFileExplorer();
        } else if ("2".equals(choice)) {
            projectPath = getJavaProjectPathFromUser(reader);
        } else {
            System.err.println(ColorHelper.error("Erreur: Choix non valide."));
        }

        if (projectPath != null) {
            PROJECT_PATH = projectPath;
            System.out.println(ColorHelper.warning("Chemin du projet Java validé : " + PROJECT_PATH));
        }
    }

    // Ouvrir l'explorateur de fichiers pour sélectionner un répertoire
    private String openFileExplorer() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Limiter la sélection aux répertoires
        int result = fileChooser.showOpenDialog(null); // Ouvre l'explorateur de fichiers

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            if (verifyJavaProject(selectedDirectory.getAbsolutePath())) {
                return selectedDirectory.getAbsolutePath() + "/src";
            } else {
                System.err.println(ColorHelper.error("Erreur: Ce n'est pas un projet Java valide (dossier 'src' non trouvé)."));
                return null;
            }
        } else {
            System.out.println(ColorHelper.error("Aucun répertoire sélectionné."));
            return null;
        }
    }

    // Vérifier si le chemin contient un dossier 'src'
    private boolean verifyJavaProject(String path) {
        File srcFolder = new File(path, "src");
        return srcFolder.exists() && srcFolder.isDirectory();
    }

    // Récupérer le chemin du projet Java saisi par l'utilisateur
    private String getJavaProjectPathFromUser(BufferedReader reader) throws IOException {
        System.out.println("Veuillez entrer le chemin du projet Java : ");
        String path = reader.readLine();
        if (verifyJavaProject(path)) {
            return path + "/src";
        } else {
            System.err.println(ColorHelper.error("Erreur: Ce n'est pas un projet Java valide (dossier 'src' non trouvé)."));
            return null;
        }
    }
}
