import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

// The Organism class represents a "genetic" individual in the population
class Organism {
    // A string containing all possible characters that can be part of the organism's genetic code
    private static final String GENES = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNO" +
            "PQRSTUVWXYZ1234567890, .-+*;:_!\"#%&/()=?@${[]}'";

    // The goal string that we are trying to match
    private String goalString;
    // The organism's current genetic code (string)
    private String value;
    // A Random object for generating random numbers
    private Random random = new Random();

    // Constructor to create an organism with a specified genetic code, or random if not provided
    public Organism(String goalString, String value) {
        this.goalString = goalString;
        this.value = value != null ? value : generateRandomValue(); // Generate random if value is null
    }

    // Overloaded constructor to create an organism with random genetic code
    public Organism(String goalString) {
        this(goalString, null);
    }

    // Method to generate a random genetic code string of the same length as the goal string
    private String generateRandomValue() {
        StringBuilder valueBuilder = new StringBuilder();
        for (int i = 0; i < goalString.length(); i++) {
            valueBuilder.append(GENES.charAt(random.nextInt(GENES.length()))); // Randomly pick a gene
        }
        return valueBuilder.toString(); // Return the randomly generated genetic code
    }

    // Method to calculate the fitness of the organism (how close it is to the goal string)
    public int fitness() {
        int fitness = 0;
        for (int i = 0; i < goalString.length(); i++) {
            if (i < value.length() && value.charAt(i) == goalString.charAt(i)) {
                fitness++; // Increment fitness for each matching character
            }
        }
        return fitness; // Return the total fitness score
    }

    // Method to create two offspring (children) by mating with another organism
    public List<Organism> mate(Organism other) {
        int crossoverPoint = random.nextInt(value.length()); // Randomly choose a crossover point
        String child1Value = value.substring(0, crossoverPoint) +
                other.value.substring(crossoverPoint); // Child 1 gets part of each parent
        String child2Value = other.value.substring(0, crossoverPoint) +
                value.substring(crossoverPoint); // Child 2 gets the reverse
        List<Organism> children = new ArrayList<>();
        children.add(new Organism(goalString, child1Value)); // Add the first child
        children.add(new Organism(goalString, child2Value)); // Add the second child
        return children;
    }

    // Method to mutate the organism's genetic code with a certain probability
    public void mutate(double mutateProb) {
        StringBuilder newValue = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            if (random.nextDouble() < mutateProb) { // With a given probability, mutate the gene
                newValue.append(GENES.charAt(random.nextInt(GENES.length())));
            } else {
                newValue.append(value.charAt(i)); // Otherwise, keep the original gene
            }
        }
        value = newValue.toString(); // Update the organism's genetic code
    }

    // Overridden toString method to provide a nice string representation of the organism
    @Override
    public String toString() {
        return "Value: " + value + ", Goal: " + goalString + ", Fitness: " + fitness();
    }
}

// The Population class represents a group of organisms in a population
class Population {
    private String goalString;
    private int popSize; // Population size
    private int numGen; // Number of generations to evolve
    private double mutateProb; // Probability of mutation
    private List<Organism> thisGeneration; // The list of organisms in the current generation
    private int stagnationCount = 0; // To track stagnation (when the population stops improving)
    private int bestFitnessLastGen = 0; // Fitness of the best organism from the last generation
    private Random random = new Random(); // Random object for generating random numbers

    // Constructor to initialize the population with random organisms
    public Population(String goalString, int popSize, int numGen, double mutateProb) {
        this.goalString = goalString;
        this.popSize = popSize;
        this.numGen = numGen;
        this.mutateProb = mutateProb;
        this.thisGeneration = new ArrayList<>();
        for (int i = 0; i < popSize; i++) {
            thisGeneration.add(new Organism(goalString)); // Create random organisms
        }
    }

    // Elitism: Returns the best organism from the current generation
    private Organism elitism() {
        List<Organism> sample = new ArrayList<>(thisGeneration);
        Collections.shuffle(sample); // Randomly shuffle the population to avoid bias
        return Collections.max(sample, Comparator.comparingInt(Organism::fitness)); // Return the organism with the highest fitness
    }

    // Main method that evolves the population over multiple generations
    public void iterate() {
        for (int g = 0; g < numGen; g++) {
            // Sort organisms by fitness in descending order (best first)
            thisGeneration.sort(Comparator.comparingInt(Organism::fitness).reversed());
            Organism bestOrganism = thisGeneration.get(0);
            System.out.println("Generation " + g + ": Best Guess - " + bestOrganism);

            // Check if the goal string has been matched
            if (bestOrganism.fitness() == goalString.length()) {
                System.out.println("Target string has been successfully guessed!");
                break;
            }

            int currentBestFitness = bestOrganism.fitness();
            if (currentBestFitness == bestFitnessLastGen) {
                stagnationCount++; // Increase stagnation count if the best fitness has not improved
            } else {
                stagnationCount = 0; // Reset stagnation count if there's progress
            }

            bestFitnessLastGen = currentBestFitness;

            // If stagnation is detected, introduce diversity into the population
            if (stagnationCount > 50) {
                System.out.println("Stagnation detected at generation " +
                        g + ". Introducing diversity.");
                stagnation();
            }

            List<Organism> nextGeneration = new ArrayList<>();
            while (nextGeneration.size() < popSize) {
                // Mate two elite organisms to create the next generation
                Organism parent1 = elitism();
                Organism parent2 = elitism();
                List<Organism> children = parent1.mate(parent2);
                for (Organism child : children) {
                    child.mutate(mutateProb); // Mutate the children
                    nextGeneration.add(child);
                    if (nextGeneration.size() >= popSize) break; // Stop when the population is full
                }
            }
            thisGeneration = nextGeneration; // Update the population to the new generation
        }
    }

    // Introduce diversity to avoid stagnation by replacing random organisms with new random ones
    private void stagnation() {
        int numToAdd = popSize / 10;
        for (int i = 0; i < numToAdd; i++) {
            thisGeneration.set(random.nextInt(popSize), new Organism(goalString)); // Replace a random organism
        }
    }
}

// Main class that runs the genetic algorithm
public class GATest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Scanner object to read user input

        // Get user input for goal string, population size, generations, and mutation probability
        System.out.print("Enter a string to be the goal string of the program: ");
        String goalString = scanner.nextLine();

        System.out.print("Enter a population size: ");
        int populationSize = scanner.nextInt();

        System.out.print("How many generations would you like to iterate through? ");
        int generations = scanner.nextInt();

        System.out.print("Enter a mutation probability (0 to 1): ");
        double mutationProbability = scanner.nextDouble();

        // Create a new population with the provided parameters
        Population population = new Population(goalString, populationSize,
                generations, mutationProbability);
        // Start evolving the population
        population.iterate();

        scanner.close(); // Close the scanner
    }
}