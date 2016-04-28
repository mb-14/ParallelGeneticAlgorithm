package com.mb14.algorithm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import com.mb14.Machine;
import com.mb14.Main;
import com.mb14.SimulationResult;
import com.mb14.SimulationThread;

public class Population {
	

	private int totalFitness;
	EnumeratedDistribution<Chromosome> distribution;
	private ArrayList<Chromosome> population;
	private ArrayList<Chromosome> offsprings;
	private int populationSize;
	boolean noLS;
	private Random rand;
	private int noOfProcessors;
	public Population(int populationSize, boolean noLS , int noOfProcessors){
		this.populationSize = populationSize;
		this.noOfProcessors = noOfProcessors;
		population = new ArrayList<Chromosome>();
		rand = new Random();
		this.noLS = noLS;
	}

	
	public Chromosome get(int i) {
		// TODO Auto-generated method stub
		return population.get(i);
	}
	public void add(Chromosome chromosome) {
		population.add(chromosome);
		
	}
	public void addAll(ArrayList<Chromosome> offsprings) {
		population.addAll(offsprings);
		
	}
	public int size() {
		return population.size();
	}
	public void optimizeOffsprings() {
		for(Chromosome chromosome: population){
			chromosome.applyLocalSearch();
		}	
		
	}

	
	private List<Pair<Chromosome, Double>> populationDistribution() {
		ArrayList<Pair<Chromosome, Double>> dist = new ArrayList<Pair<Chromosome, Double>>();
		for(int i=0;i<populationSize;i++){
			dist.add(new Pair<Chromosome, Double>(population.get(i),population.get(i).fitnessValue/totalFitness));
		}
		return dist;
	}


	public void updateProperties() {
		totalFitness = 0;
		for(Chromosome individual: population)
			totalFitness += individual.fitnessValue;
		try
		{
			distribution = new EnumeratedDistribution<Chromosome>(populationDistribution());
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}


	public void enhance() throws InterruptedException, ExecutionException {
		offsprings = new ArrayList<Chromosome>();
		int numberOfPairs = (populationSize/4%2==0)?populationSize/4:populationSize/4+1; 
		for(int i=0;i<numberOfPairs;i++)
		{
			Chromosome[] parents = selectParents();
			if(!parents[0].combo.equals(parents[1].combo)){
				Chromosome[] offspring = crossover(parents[0],parents[1]);
				if(!offspring[0].combo.equals(BigInteger.valueOf(0)))
					offsprings.add(offspring[0]);
				if(!offspring[0].combo.equals(BigInteger.valueOf(0)))
					offsprings.add(offspring[1]);	
			}
		}
		//Do mutation here
		for(Chromosome offspring: offsprings){
			if(rand.nextDouble() < 0.4){
				int mutationPoint = rand.nextInt(Machine.compList.length*Main.pmOpportunity.length-1);

				do
				{
					offspring.combo = offspring.combo.flipBit(mutationPoint);
					mutationPoint = rand.nextInt(Machine.compList.length*Main.pmOpportunity.length-1);
				}while(offspring.combo.equals(BigInteger.valueOf(0)));
			}
		}

		if(!noLS){
			optimizeOffsprings();
		}
		
		evaluateFitness(offsprings);
		population.addAll(offsprings);
		Collections.sort(population);
		population.subList(populationSize-1, population.size()-1).clear();
		
	}
	
	//Single point crossover
		private Chromosome[] crossover(Chromosome parent1, Chromosome parent2) {
			Chromosome[] offsprings = new Chromosome[2];
			BigInteger rights[] = {parent1.combo,parent1.combo};
			BigInteger lefts[] = {parent2.combo,parent2.combo};

			int crossoverPoint = rand.nextInt(Machine.compList.length*Main.pmOpportunity.length-2)+1;

			rights[0] = rights[0].and(BigInteger.valueOf(1<<crossoverPoint-1));
			rights[1] = rights[1].and(BigInteger.valueOf(1<<crossoverPoint-1));

			lefts[0] = lefts[0].shiftRight(crossoverPoint).shiftLeft(crossoverPoint);
			lefts[1] = lefts[1].shiftRight(crossoverPoint).shiftLeft(crossoverPoint);	

			offsprings[0] = new Chromosome(lefts[0].or(rights[1]));
			offsprings[1] = new Chromosome(lefts[1].or(rights[0]));

			return offsprings;
		}


		private Chromosome[] selectParents() {
			Chromosome parents[] = new Chromosome[2];
			distribution.sample(2,parents);
			return parents;
		}
		
		void evaluateFitness() throws InterruptedException, ExecutionException{
			evaluateFitness(population);
		}
		
		private void evaluateFitness(ArrayList<Chromosome> list) throws InterruptedException, ExecutionException {
			ExecutorService threadPool = Executors.newFixedThreadPool(1);
			CompletionService<SimulationResult> pool = new ExecutorCompletionService<SimulationResult>(threadPool);
			int cnt = 0 ;

			for(Chromosome chromosome : list){
				pool.submit(new SimulationThread(Main.schedule, chromosome.getCombolist(), Main.pmOpportunity,false,cnt));
				cnt++;
			}
			for(int i=0;i<cnt;i++){
				SimulationResult result = pool.take().get();
				list.get((int)result.chromosomeID).fitnessValue = result.cost;
				list.get((int)result.chromosomeID).pmAvgTime = result.pmAvgTime;
				totalFitness += result.cost;
			}
			threadPool.shutdown();
			while(!threadPool.isTerminated());

		}


		public void sort() {
			Collections.sort(population);
			
		}


		public void init(int index) {
			int bits = Machine.compList.length*Main.pmOpportunity.length - log2(noOfProcessors);
			BigInteger base = (new BigInteger("2")).pow(Machine.compList.length*Main.pmOpportunity.length); 
			base = base.divide(new BigInteger(String.valueOf(noOfProcessors)));
			base = base.multiply(new BigInteger(String.valueOf(index)));
			BigInteger num;
			Random rnd = new Random();
			Hashtable<BigInteger, Boolean> hashTable = new Hashtable<BigInteger, Boolean>();
			for(int i=0;i<populationSize;i++)
			{

				num = new BigInteger(bits, rnd);
				num = num.add(base);
				while(hashTable.containsKey(num))
				{
					num = new BigInteger(bits, rnd);
					num = num.add(base);
				}

				population.add(new Chromosome(num));
				hashTable.put(num, new Boolean(true));
			}
			
		}

		public void set(int i,Chromosome element){
			population.set(i, element);
		}
		
		public int log2(int i){
			return (int) (Math.log(i)/Math.log(2));
		}

}
