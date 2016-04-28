package com.mb14.algorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import com.mb14.Parallel;
import com.mb14.SimulationResult;


public class MemeticAlgorithm {
	private int stopCrit;
	private ArrayList<Population> populations;
	SimulationResult noPM;
	Random rand;
	int noOfProcessors;
	int migrationGap;
	int migrationRate;
	int populationSize;
	double best;
	double oldbest;
	int convergenceCount = 0;
	public MemeticAlgorithm(int populationSize, int stopCrit, SimulationResult noPM, boolean noLS, int noOfProcessors, int migrationGap ,int migrationRate){
		
		this.populations = new ArrayList<Population>();
		for(int i=0; i < noOfProcessors; i++){
			Population pop = new Population(populationSize,noLS,populationSize);
			populations.add(pop);
		}
		this.populationSize = populationSize;
		this.migrationGap = migrationGap;
		this.stopCrit = stopCrit;
		rand = new Random();
		this.noPM = noPM;
		best = 99999999;
		oldbest = 99999999;
		this.noOfProcessors = noOfProcessors;
		this.migrationRate = migrationRate;

	}

	public void execute() throws InterruptedException, ExecutionException, NumberFormatException, IOException
	{
		
		Parallel.blockingFor(populations.size() , populations, new Parallel.Operation<Population>() {
		    public void perform(Population pop) {
		    	pop.init(populations.indexOf(pop));
				try {
					pop.evaluateFitness();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
		    };
		});
		
		for(int cnt = 1; cnt <= stopCrit; cnt ++) 
		{
			//if(hasConverged())
			//	break;
			
			Parallel.blockingFor(populations.size() ,populations, new Parallel.Operation<Population>() {
			    public void perform(Population pop) {
			    	pop.updateProperties();
					try {
						pop.enhance();
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
			    };
			});
			
			
			if(cnt%migrationGap == 0 && noOfProcessors > 1)
				migrate();
			
			findBest();
			System.out.format("Generation %d, Best: %f\n", cnt, best);
			
		}

		//System.out.println(best);	

	}

	//Ring migration
	private void migrate() {
		for(int i =0; i< populations.size();i++){
			for(int j=0;j<migrationRate;j++){
				Chromosome temp = populations.get(i).get(j);
				populations.get((i+1)%populations.size()).set(populationSize - j - 1, temp);
			}
		}
		
	}

	private boolean hasConverged() {
		if(oldbest == best){
			convergenceCount++;
			if(convergenceCount == 30)
				return true;
		}
		else{
			convergenceCount = 0;
			oldbest =  best;
		}
		return false;
	}
	

	
	private void findBest() {
		for(int i=0;i<populations.size();i++){
			if(best > populations.get(i).get(0).fitnessValue)
				best = populations.get(i).get(0).fitnessValue;
		}
	}




	
	
	


}

