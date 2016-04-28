package com.mb14.algorithm;

import java.math.BigInteger;
import java.util.Random;

import com.mb14.Component;
import com.mb14.Machine;
import com.mb14.Macros;
import com.mb14.Main;

class Chromosome implements Comparable<Chromosome>{
	static Random rand = new Random();
	double pmAvgTime;
	double fitnessValue;

	//Binary representation of the chromosome
	BigInteger combo;
	public Chromosome(BigInteger combo){
		this.combo = combo;
		this.fitnessValue = 0;
	}

	public long[] getCombolist() {
		long combos[] = new long[Main.pmOpportunity.length];
		for(int i =0;i<Main.pmOpportunity.length;i++){
			combos[i] = combo.shiftRight(Machine.compList.length*i).and(BigInteger.valueOf((long)Math.pow(2,Machine.compList.length)-1)).longValue();
		}
		return combos;
	}

	public void applyLocalSearch() 
	{
		BigInteger new_combo = combo;
		int cnt = 0;
		while(cnt++<5)
		{
			int mutationPoint = rand.nextInt(Machine.compList.length*Main.pmOpportunity.length-1);
			do
			{
				new_combo = combo.flipBit(mutationPoint);
				mutationPoint = rand.nextInt(Machine.compList.length*Main.pmOpportunity.length-1);
			}while(new_combo.equals(BigInteger.valueOf(0)));

			if(heuristic(new_combo)>heuristic(combo)){
				combo = new_combo;
			}
		}		
	}



	private long heuristic(BigInteger combo) {
		Component[] temp = new Component[Machine.compList.length];
		for(int i=0; i < Machine.compList.length; i++)
			temp[i] = new Component(Machine.compList[i]);

		int heuristicP=0;
		int heuristicN=0;
		Double fp;
		long comboList[] = getCombolist(combo);
		for(int i=0;i<comboList.length;i++){
			for(int j=0;j<temp.length;j++){
				int pos = 1<<j;
				if((comboList[i]&pos)!=0){
					fp = getFailureProbablity(temp[j], Main.pmOpportunity[i]);
					heuristicP += (fp>0.5d)?1:-1;
					temp[j].initAge = (1-temp[j].pmRF)*temp[j].initAge;
				}
				else{
					fp = getFailureProbablity(temp[j], Main.pmOpportunity[i]);
					heuristicN += (fp<0.5d)?1:-1;
				}
			}
		}
		return heuristicN+heuristicP;
	}




	private Double getFailureProbablity(Component component, int pmO) {
		int failureCount =0;
		long time = 0;
		time = Math.min(Macros.SHIFT_DURATION, Main.schedule.getSum());
		time -= pmO;
		for(int i=0;i<50;i++){
			Double cmTTF = component.getCMTTF();
			if(cmTTF < time){
				failureCount++;
			}
		}
		return failureCount/50d;
	}

	private long[] getCombolist(BigInteger combo) {
		long combos[] = new long[Main.pmOpportunity.length];
		for(int i =0;i<Main.pmOpportunity.length;i++){
			combos[i] = combo.shiftRight(Machine.compList.length*i).and( BigInteger.valueOf((long) Math.pow(2,Machine.compList.length)-1) ).longValue();
		}
		return combos;
	}

	@Override
	public int compareTo(Chromosome o) {
		return Double.compare(fitnessValue, o.fitnessValue);
	}

}