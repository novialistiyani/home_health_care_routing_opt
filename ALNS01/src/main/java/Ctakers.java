import java.awt.Point;
import java.util.ArrayList;

public class Ctakers {
	
	//index by ctID
	private ArrayList<Integer> ctID;					//ctaker ID
	private ArrayList<Integer> ctSkill;					//ctaker skill
	private ArrayList<Double> ctWage;					//ctaker wage
	private ArrayList<Double> ctDBreak;					//ctaker break duration
	private ArrayList<ArrayList<Double>> ctDService;	//ctaker service time (depending on the skill level)
	
	
	public Ctakers(){
		ctID = new ArrayList<Integer>();
		ctSkill = new ArrayList<Integer>();
		ctWage = new ArrayList<Double>();
		ctDBreak = new ArrayList<Double>();
		ctDService= new ArrayList<ArrayList<Double>>();
	}
	
	//add caretaker data: caretaker ID, skill, wage per hour, break duration, time service per skill level, skill range
	public void addCtakerData(int cid, int sk, double wg, double dbr, ArrayList<Double> dsr, int rsk){
		
		if(!ctID.contains(cid)&&dsr.size()==rsk+1&&dsr.get(0)==0){
			//update list
			ctID.add(cid);
			ctSkill.add(sk);
			ctWage.add(wg);
			ctDBreak.add(dbr);
			
			//check if dsr matches information from caretaker skill and skill range
			int temp=0;
			for(int g=1;g<dsr.size();g++){
				if((dsr.get(g)==100000&&g<=sk)||(g>sk&&dsr.get(g)!=100000)){
					temp+=1;
				}
			}
			
			if(temp<=0){
				ctDService.add(dsr);	
			}
			else if(temp>0){
				throw new IllegalArgumentException("Unmatch input");
			}
			
			/*checkpoint
			System.out.println("Add caretaker: "+ ctID);
			System.out.println("Add skill: "+ctSkill);
			System.out.println("Add wage: "+ctWage);
			System.out.println("Add break duration: "+ctDBreak);
			
			System.out.print("Add service duration for level 0 - "+(rsk-1)+" : [ ");
			for(int i=0;i<ctID.size();i++){
				for(int j=0;j<rsk;j++){
					System.out.print(ctDService.get(i)[j]+" ");
				}
			}
			System.out.println("]");*/
		}
		
		else if(dsr.size()!=rsk+1||dsr.get(0)!=0){
			throw new IllegalArgumentException("Unmatch input");
		}
	}
	
	//lookup information of a particular caretaker
	public int lookupctSkill(int cid){
		int i = ctID.indexOf(cid);
		
		if(i==-1){
			throw new IllegalArgumentException("Unmatch input");
		}
		
		int csk = ctSkill.get(i);
		return csk;
	}
	
	public double lookupctWage(int cid){
		int i = ctID.indexOf(cid);
		
		if(i==-1){
			throw new IllegalArgumentException("Unmatch input");
		}
		
		double wg = ctWage.get(i);
		return wg;
	}
	
	public double lookupctDBreak(int cid){
		int i = ctID.indexOf(cid);
		
		if(i==-1){
			throw new IllegalArgumentException("Unmatch input");
		}
		
		double dbr = ctDBreak.get(i);
		return dbr;
	}
	
	public ArrayList<Double> lookupctDService(int cid){
		int i = ctID.indexOf(cid);
		
		if(i==-1){
			throw new IllegalArgumentException("Unmatch input");
		}
		
		ArrayList<Double> dsr = ctDService.get(i);
		return dsr;
	}
	
	//get variables
	public ArrayList<Integer> getctID(){
		return ctID;
	}
	
	public ArrayList<Integer> getctSkill(){
		return ctSkill;
	}
	
	public ArrayList<Double> getctWage(){
		return ctWage;
	}
	
	public ArrayList<Double> getctDBreak(){
		return ctDBreak;
	}
	
	public ArrayList<ArrayList<Double>> getctDService(){
		return ctDService;
	}
}
