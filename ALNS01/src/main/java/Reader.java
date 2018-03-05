import java.io.*;
import java.util.*;

public class Reader {
	//visit data
	private ArrayList<Integer> rvsID;
	private ArrayList<Double> rvsLocX;
	private ArrayList<Double> rvsLocY;
	private ArrayList<Double> rvsDmd; 		//unused
	private ArrayList<Double> rvsTWin1;
	private ArrayList<Double> rvsTWin2;
	private ArrayList<Double> rvsSr; 		//unused
	private ArrayList<Integer> rvsPrior; 
	private ArrayList<Integer> rvsSkillReq;
	private ArrayList<Integer> rvsNumDayReq;
	private ArrayList<ArrayList<Integer>> rvsDayReq;
	
	//ctaker data
	private ArrayList<Integer> rctID;
	private ArrayList<Integer> rctSkill;
	private ArrayList<Double> rctWage;
	private ArrayList<Double> rctDBreak;
	private ArrayList<ArrayList<Double>> rctDService;
	
	public Reader(){
		//visit data
		rvsID = new ArrayList<Integer>();
		rvsLocX = new ArrayList<Double>();
		rvsLocY = new ArrayList<Double>();
		rvsDmd = new ArrayList<Double>();		//unused
		rvsTWin1 = new ArrayList<Double>();
		rvsTWin2 = new ArrayList<Double>();
		rvsSr = new ArrayList<Double>(); 		//unused
		rvsPrior = new ArrayList<Integer>();
		rvsSkillReq = new ArrayList<Integer>();
		rvsNumDayReq = new ArrayList<Integer>();
		rvsDayReq = new ArrayList<ArrayList<Integer>>();
	
		//ctaker data
		rctID = new ArrayList<Integer>();
		rctSkill = new ArrayList<Integer>();
		rctWage = new ArrayList<Double>();
		rctDBreak = new ArrayList<Double>();
		rctDService = new ArrayList<ArrayList<Double>>();
	}
	
	/*
	For visit data:
	>> use data from solomon 
	>> assume that priority level is based on the order of visit (depot = 0)
	>> assume that skill range is between 1-4 (depot = 0)
	>> assume that each required skill is evenly distributed (25% possibility of occurence)
	*/
	
	//starts to read visit data from line 7
	public void readVisit(String flnamev) throws Exception{
		FileReader flv= new FileReader(flnamev);
		BufferedReader inv = new BufferedReader(flv);
		
		ArrayList<String> fllinev = new ArrayList<String>();
		
		int i =0;
		while(inv.ready()){
			String linev =inv.readLine();
			fllinev.add(linev);
			i+=1;
		}
		inv.close();
		flv.close();
		
		for(int j=8;j<fllinev.size();j++){
			
			if(!fllinev.get(j).isEmpty()){
				StringTokenizer stv = new StringTokenizer(fllinev.get(j),",");
				
				while(stv.hasMoreTokens()){
					rvsID.add(Integer.parseInt(stv.nextToken()));
					rvsLocX.add(Double.parseDouble(stv.nextToken()));
					rvsLocY.add(Double.parseDouble(stv.nextToken()));
					rvsDmd.add(Double.parseDouble(stv.nextToken()));
					rvsTWin1.add(Double.parseDouble(stv.nextToken()));
					rvsTWin2.add(Double.parseDouble(stv.nextToken()));
					rvsSr.add(Double.parseDouble(stv.nextToken()));
					rvsPrior.add(Integer.parseInt(stv.nextToken()));
					rvsSkillReq.add(Integer.parseInt(stv.nextToken()));
					rvsNumDayReq.add(Integer.parseInt(stv.nextToken()));
					
					int k=0;
					ArrayList<Integer> tempdrq = new ArrayList<Integer>();
					while(stv.hasMoreTokens()){
						tempdrq.add(Integer.parseInt(stv.nextToken()));
						k+=1;
					}
					rvsDayReq.add(tempdrq);
				}
			}
			
		}
		
		/*check point
		System.out.println();
		System.out.println("Read visit1");
		System.out.println("rvsID "+rvsID);
		System.out.println("rvsLocX "+rvsLocX);
		System.out.println("rvsLocY "+rvsLocY);
		System.out.println("rvsDmd "+rvsDmd);
		System.out.println("rvsTWin1 "+rvsTWin1);
		System.out.println("rvsTWin2 "+rvsTWin2);
		System.out.println("rvsSr "+rvsSr);
		System.out.println("rvsPrior "+rvsPrior);
		System.out.println("rvsSkillReq "+rvsSkillReq);
		System.out.println("rvsNumDayReq "+rvsNumDayReq);
		System.out.println("rvsDayReq "+rvsDayReq);
		*/
	}
	
	/*
	For ctaker data:
	>> overall will provide 30 ctakers
	>> for each instance, 15-25% will be chosen
	>> assume that the ctakers have uniformly distributed skill level 
	>> assume that each skill level associates with different wage per hour and its service time range: 
		--> skill level 1 = GBP6, with basis of service duration is 120 min.
		--> skill level 2 = GBP8, with basis of service duration is 90 min.
		--> skill level 3 = GBP10, with basis of service duration is 60 min.
		--> skill level 4 = GBP12, with basis of service duration is 30 min.
	>> basis of service duration implies that the ctaker will require that amount of minutes to complete a visit that requires skill level 1.
	>> should the skill required exceeds 1, but still within ctaker capability, we will add another 15 min increment per skill requirement increase.
	   for example, if a ctaker with skill level 4 needs to finish a visit that requires skill level 3, it is assumed that he/she will need 30+15+15=60 min.
	>> however, if the ctaker does not have enough skill, service duration for that skill will be set to a big positive number.
	>> assume that each ctaker will have to work 8 hours per day (from 9 to 18), excluding break duration of 60 min 
	*/
	
	//starts to read visit data from line 4 to 8 (only 5 ctaker)
	public void readCtaker(String flnamec, int ct) throws Exception{
		FileReader flc= new FileReader(flnamec);
		BufferedReader inc = new BufferedReader(flc);
		
		ArrayList<String> fllinec = new ArrayList<String>();
		int l=0;
		while(inc.ready()){
			String linec =inc.readLine();
			fllinec.add(linec);
			l+=1;
		}
		inc.close();
		flc.close();
		
		
		
		for(int m=4;m<4+ct;m++){
			
			if(!fllinec.get(m).isEmpty()){
				StringTokenizer stc = new StringTokenizer(fllinec.get(m),",");
				
				while(stc.hasMoreTokens()){
					rctID.add(Integer.parseInt(stc.nextToken()));
					rctSkill.add(Integer.parseInt(stc.nextToken()));
					rctWage.add(Double.parseDouble(stc.nextToken()));
					rctDBreak.add(Double.parseDouble(stc.nextToken()));
				
					int n=0;
					ArrayList<Double> tempdsr = new ArrayList<Double>();
					while(stc.hasMoreTokens()){
						tempdsr.add(Double.parseDouble(stc.nextToken()));
						n+=1;
					}
					rctDService.add(tempdsr);
				}
			}
			
		}
		
		/*check point
		System.out.println();
		System.out.println("Read ctaker1");
		System.out.println("rctID "+rctID);
		System.out.println("rctSkill "+rctSkill);
		System.out.println("rctWage "+rctWage);
		System.out.println("rctDBreak "+rctDBreak);
		System.out.println("rctDService "+rctDService);	
		*/	
	}
	
	//get variables
	public ArrayList<Integer> getrvsID(){
		return rvsID;
	}
	
	public ArrayList<Double> getrvsLocX(){
		return rvsLocX;
	}
	
	public ArrayList<Double> getrvsLocY(){
		return rvsLocY;
	}
	
	public ArrayList<Double> getrvsDmd(){
		return rvsDmd;
	}
	
	public ArrayList<Double> getrvsTWin1(){
		return rvsTWin1;
	}
	
	public ArrayList<Double> getrvsTWin2(){
		return rvsTWin2;
	}
	
	public ArrayList<Double> getrvsSr(){
		return rvsSr;
	}
	
	public ArrayList<Integer> getrvsPrior(){
		return rvsPrior;
	}
	
	public ArrayList<Integer> getrvsSkillReq(){
		return rvsSkillReq;
	}
	
	public ArrayList<Integer> getrvsNumDayReq(){
		return rvsNumDayReq;
	}
	
	public ArrayList<ArrayList<Integer>> getrvsDayReq(){
		return rvsDayReq;
	}
	
	public ArrayList<Integer> getrctID(){
		return rctID;
	}
	
	public ArrayList<Integer> getrctSkill(){
		return rctSkill;
	}
	
	public ArrayList<Double> getrctWage(){
		return rctWage;
	}
	
	public ArrayList<Double> getrctDBreak(){
		return rctDBreak;
	}
	
	public ArrayList<ArrayList<Double>> getrctDService(){
		return rctDService;
	}
}
