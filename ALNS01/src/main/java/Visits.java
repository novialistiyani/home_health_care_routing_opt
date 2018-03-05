import java.util.*;
import java.awt.geom.Point2D;
import java.math.*;

public class Visits {
	//index by vsID
	private ArrayList<Integer> vsID;				//visit ID
	private ArrayList<Point2D> vsLoc;				//visit location
	private ArrayList<Integer> vsPrior;				//visit priority
	private ArrayList<Integer> vsSkillReq;			//visit skill requirement
	private ArrayList<ArrayList<Integer>> vsDayReq;	//visit day requirement
	private ArrayList<Double> vsTWin1;				//visit start time window
	private ArrayList<Double> vsTWin2;				//visit end time window
	private ArrayList<ArrayList<Integer>> vsCtakerReq;//list of possible ctakers per visit by considering their skill
	
	private ArrayList<ArrayList<Integer>> vsDayVisit;//list of visits per day in a week

	//index by vsPath
	private ArrayList<ArrayList<Integer>> vsPath;	//list of paths
	private ArrayList<Double> vsTTravel;			//travel time for paths
	private ArrayList<Double> vsDistance;			//distance for paths
	
	private ArrayList<Integer> vsIdxPath;			//list if visits in vsPath 
	
	//index by vsID+ctID (generating precedence list by considering time window, service time, travel time, and range skill
	private ArrayList<ArrayList<Double>> vsDService;//transform ctaker service time duration - whose input is based on their skill - to be based on different visit skill requirement
	private ArrayList<ArrayList<Double>> vsTEnd1;	//earliest end time based on time window1, service time, and travel time
	private ArrayList<ArrayList<Double>> vsTEnd2;	//earliest end time based on time window2, service time, and travel time

	//index by vsSeqPath
	private ArrayList<ArrayList<Integer>> vsSeqPath; //list of possible paths, two visits are only possible if start time doesn't overlap (consider time window1, time window2, service time, travel time)
	private ArrayList<ArrayList<Integer>> vsSeqPathCtaker; //bisa dihapus? 
	private ArrayList<Double> vsSeqTTravel; 		//travel time for the possible paths
	private ArrayList<Double> vsSeqDistance;		//distance for the possible paths
	private ArrayList<Integer> vsSeqCtaker;			//ctakers for the possible paths
	
	private ArrayList<ArrayList<Integer>> vsSortSeqPath; //list of possible paths, sorted by the restrictions of ctaker and its distance 
	private ArrayList<Double> vsSortSeqDistance; 		 //distance for vsSortSeqPath	
	
	//index by vsFeaVisitCtakerDay
	private ArrayList<Integer> vsFeaVisit;				 //feasible visit 
	private ArrayList<Integer> vsFeaDay;				 //day for the feasible visit
	private ArrayList<Integer> vsFeaCtaker;				 //ctaker for the feasible visit
	private ArrayList<ArrayList<Integer>> vsFeaVisitCtakerDay;//feasible (visit, ctaker, day)
	
	private ArrayList<ArrayList<Integer>> vsFeaVisitDay; //feasible (visit, day)
	
	
	public Visits(){
		vsID = new ArrayList<Integer>();
		vsLoc = new ArrayList<Point2D>();
		vsPrior = new ArrayList<Integer>();
		vsSkillReq = new ArrayList<Integer>();
		vsDayReq = new ArrayList<ArrayList<Integer>>();
		vsTWin1 = new ArrayList<Double>();
		vsTWin2 = new ArrayList<Double>();
		vsPath = new ArrayList<ArrayList<Integer>>();
		vsIdxPath = new ArrayList<Integer>();
		vsTTravel = new ArrayList<Double>();
		vsDistance = new ArrayList<Double>();
		vsDService = new ArrayList<ArrayList<Double>>();
		vsTEnd1 = new ArrayList<ArrayList<Double>>();
		vsTEnd2 = new ArrayList<ArrayList<Double>>();
		vsSeqPath = new ArrayList<ArrayList<Integer>>();
		vsSeqPathCtaker = new ArrayList<ArrayList<Integer>>();
		vsSeqTTravel = new ArrayList<Double>();
		vsSeqDistance = new ArrayList<Double>();
		vsSeqCtaker =  new ArrayList<Integer>();
		vsFeaVisit = new ArrayList<Integer>();
		vsFeaDay = new ArrayList<Integer>();
		vsFeaCtaker = new ArrayList<Integer>();
		vsFeaVisitDay = new ArrayList<ArrayList<Integer>>();
		vsFeaVisitCtakerDay = new ArrayList<ArrayList<Integer>>();
		vsSortSeqPath = new ArrayList<ArrayList<Integer>>();
		vsSortSeqDistance = new ArrayList<Double>();
		vsCtakerReq = new ArrayList<ArrayList<Integer>>();
		vsDayVisit = new ArrayList<ArrayList<Integer>>();
	}
	
	//add visit data: visit ID, x coord, y coord, priority level, skill requirement, day requirement, time window1, time window2, number of days
	public void addVisitData(int vid, double x, double y, int pr, int srq, ArrayList<Integer> drq,double tw1,double tw2, int nd){
		
		if(!vsID.contains(vid)&&drq.size()==nd){
			//update list
			vsID.add(vid);
			vsLoc.add(new Point2D.Double(x,y));
			vsPrior.add(pr);
			vsSkillReq.add(srq);
			vsDayReq.add(drq);
			vsTWin1.add(tw1);
			vsTWin2.add(tw2);
				
			/*check point
			System.out.println("Add visit: "+ vsID);
			System.out.println("Add location: "+vsLoc);
			System.out.println("Add priority: "+vsPrior);
			System.out.println("Add skill requirement: "+vsSkillReq);
			
			System.out.println("Add day requirement: "+vsDayReq);
			System.out.println("Add time window1: "+vsTWin1);
			System.out.println("Add time window2: "+vsTWin2);
			*/
		}
		
		else if(drq.size()!=nd){
			throw new IllegalArgumentException("Unmatch input");
		}
	}
	
	//add path data: visit ID1, visit ID2, time travel
	public void addPath(int vid1, int vid2, double tt){
		
		ArrayList<Integer> temp = new ArrayList<Integer>();
		temp.add(vid1);
		temp.add(vid2);
		
		//input path
		if(vsID.contains(vid1)&&vsID.contains(vid2)&&!vsPath.contains(temp)){
			int idx1 = vsID.indexOf(vid1);
			int idx2 = vsID.indexOf(vid2); 
			
			double px1 = vsLoc.get(idx1).getX();
			double py1 = vsLoc.get(idx1).getY();
			double px2 = vsLoc.get(idx2).getX();
			double py2 = vsLoc.get(idx2).getY();
			
			double dist = Point2D.distance(px1, py1, px2, py2);
			
			vsPath.add(temp);
			vsTTravel.add(tt);
			vsDistance.add(dist);
			
			//generate indexing
			if(!vsIdxPath.contains(vid1)){
				vsIdxPath.add(vid1);
			}
			
			if(!vsIdxPath.contains(vid2)){
				vsIdxPath.add(vid2);
			}
		}
		
		/*check point
		System.out.println("vsPath "+vsPath);
		System.out.println("vsDistance "+vsDistance);		
		System.out.println("vsTTravel "+vsTTravel);
		*/
	}
	
	
	//generate time service based on skill requirement and caretaker skill in each level: caretakerID, caretaker skill, caretaker time service
	public void genDService(ArrayList<Integer> cid, ArrayList<Integer> sk, ArrayList<ArrayList<Double>> dsr){
		
		int m = vsID.size();
		int n = cid.size();
		
		//row ==> visit, col ==> ctaker
		//array inside ==> rangeSkill
		for(int i=0;i<m;i++){
			ArrayList<Double> temp = new ArrayList<Double>();
			ArrayList<Double> temp1 = new ArrayList<Double>();
			ArrayList<Double> temp2 = new ArrayList<Double>();
			ArrayList<Integer> temp3 = new ArrayList<Integer>();
			
			//compute service time, end time1, end time2
			for(int j=0;j<n;j++){
				if(vsSkillReq.get(i)<=sk.get(j)){
					temp.add(dsr.get(j).get(vsSkillReq.get(i)));
					temp1.add(dsr.get(j).get(vsSkillReq.get(i)) + vsTWin1.get(i));
					temp2.add(dsr.get(j).get(vsSkillReq.get(i)) + vsTWin2.get(i));
					temp3.add(cid.get(j));
				}
				else{
					temp.add(100000.0);
					temp1.add(100000.0);
					temp2.add(100000.0);
				}		
			}
			vsDService.add(temp);
			vsTEnd1.add(temp1);
			vsTEnd2.add(temp2);
			vsCtakerReq.add(temp3);
			
		}
		
		/*check point
		System.out.println("vsDService "+vsDService);
		System.out.println("TEnd1 "+vsTEnd1);
		System.out.println("TEnd2 "+vsTEnd2);
		System.out.println("vsCtakerReq "+vsCtakerReq);
		*/
	}
	
	//generate possible path by sequencing them based on time window, time service, and time travel: caretaker ID 
	public void genPrecedence(ArrayList<Integer> cid){
		
		int m = vsID.size();
		int n = cid.size();
		
		//for every pair of visits and available caretaker
		for(int i=0;i<m-1;i++){
			for(int j=i+1;j<m;j++){
				for(int k=0;k<n;k++){
					ArrayList<Integer> temp1 = new ArrayList<Integer>();
					temp1.add(vsID.get(i));
					temp1.add(vsID.get(j));
					
					int x1 = vsPath.indexOf(temp1);
					
					//check combination i+j or j+i ==> only choose those non-overlapping sequence (consider time service and time travel)
					if(x1!=-1){
						if((vsTWin1.get(i)<vsTWin1.get(j)&&vsTEnd1.get(i).get(k)<vsTEnd1.get(j).get(k)&&vsTEnd1.get(i).get(k)+vsTTravel.get(x1)<=vsTWin1.get(j))	//earliest vs earliest
								||(vsTWin1.get(i)<vsTWin2.get(j)&&vsTEnd1.get(i).get(k)<vsTEnd2.get(j).get(k)&&vsTEnd1.get(i).get(k)+vsTTravel.get(x1)<=vsTWin2.get(j))		//earliest vs latest
								||(vsTWin2.get(i)<vsTWin1.get(j)&&vsTEnd2.get(i).get(k)<vsTEnd1.get(j).get(k)&&vsTEnd2.get(i).get(k)+vsTTravel.get(x1)<=vsTWin1.get(j))		//latest vs earliest
								||(vsTWin2.get(i)<vsTWin2.get(j)&&vsTEnd2.get(i).get(k)<vsTEnd2.get(j).get(k)&&vsTEnd2.get(i).get(k)+vsTTravel.get(x1)<=vsTWin2.get(j))){	//latest vs latest
									
							vsSeqPath.add(temp1);
							vsSeqCtaker.add(cid.get(k));
							vsSeqDistance.add(vsDistance.get(vsPath.indexOf(temp1)));
							vsSeqTTravel.add(vsTTravel.get(vsPath.indexOf(temp1)));
							temp1.add(0);
							temp1.set(0,cid.get(k));
							temp1.set(1,vsID.get(i));
							temp1.set(2,vsID.get(j));
							vsSeqPathCtaker.add(temp1);//buat apa? bisa lgs pakai vsSeqPath
			
						}	
					}
				}
			}
		}
		
		for(int i=0;i<m-1;i++){
			for(int j=i+1;j<m;j++){
				for(int k=0;k<n;k++){
					ArrayList<Integer> temp2 = new ArrayList<Integer>();
					temp2.add(vsID.get(j));
					temp2.add(vsID.get(i));
					
					int x2 = vsPath.indexOf(temp2);					
					
					if(x2!=-1){
						if((vsTWin1.get(i)>vsTWin1.get(j)&&vsTEnd1.get(i).get(k)>vsTEnd1.get(j).get(k)&&vsTEnd1.get(j).get(k)+vsTTravel.get(x2)<=vsTWin1.get(i))	//earliest vs earliest
								||(vsTWin1.get(i)>vsTWin2.get(j)&&vsTEnd1.get(i).get(k)>vsTEnd2.get(j).get(k)&&vsTEnd2.get(j).get(k)+vsTTravel.get(x2)<=vsTWin1.get(i))		//earliest vs latest
								||(vsTWin2.get(i)>vsTWin1.get(j)&&vsTEnd2.get(i).get(k)>vsTEnd1.get(j).get(k)&&vsTEnd1.get(j).get(k)+vsTTravel.get(x2)<=vsTWin2.get(i))		//latest vs earliest
								||(vsTWin2.get(i)>vsTWin2.get(j)&&vsTEnd2.get(i).get(k)>vsTEnd2.get(j).get(k)&&vsTEnd2.get(j).get(k)+vsTTravel.get(x2)<=vsTWin2.get(i))){	//latest vs latest
									
							vsSeqPath.add(temp2);
							vsSeqCtaker.add(cid.get(k));
							vsSeqDistance.add(vsDistance.get(vsPath.indexOf(temp2)));
							vsSeqTTravel.add(vsTTravel.get(vsPath.indexOf(temp2)));
							temp2.add(0);
							temp2.set(0,cid.get(k));
							temp2.set(1,vsID.get(j));
							temp2.set(2,vsID.get(i));
							vsSeqPathCtaker.add(temp2);
						}
						
					}
									
				}
			}
		}
		
		/*check point
		System.out.println("vsSeqPath "+vsSeqPath);
		System.out.println("vsSeqCtaker "+vsSeqCtaker);
		System.out.println("vsSeqDistance "+vsSeqCtaker);
		System.out.println("vsSeqCtaker "+vsSeqCtaker);
		*/
	}
	
	//sorting precedence path list based on the number of ctakers and its distance
	public void genSortPrecedence(){
		
		ArrayList<ArrayList<Integer>> tempidxpath= new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> temppath= new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> tempfreqpath= new ArrayList<Integer>();
		
		//generate indexing for each path
		for(int i=0;i<vsSeqPath.size();i++){
			ArrayList<Integer> temp = new ArrayList<Integer>();
			temp.add(vsSeqPath.get(i).get(1));
			temp.add(vsSeqPath.get(i).get(2));
			temppath.add(temp);
			
			if(!tempidxpath.contains(temp)){
				tempidxpath.add(temp);
			}
		}
		
		//count occurrences for each combination -- the number represents possible ctaker to get through  particular visiting path 
		for(int j=0;j<tempidxpath.size();j++){
			tempfreqpath.add(Collections.frequency(temppath,tempidxpath.get(j)));
		}
		
		//sort day and ctaker combination based on the occurrences
		for(int k=0;k<tempidxpath.size();k++){
			for(int l=k+1;l<tempidxpath.size();l++){
				if(tempfreqpath.get(l)<tempfreqpath.get(k)){
					Collections.swap(tempidxpath,k,l);
					Collections.swap(tempfreqpath,k,l);
				}
			}
		}
		
		//sorting sequenced path based on its occurrences number and distance
		for(int s=Collections.min(tempfreqpath);s<=Collections.max(tempfreqpath);s++){
			ArrayList<ArrayList<Integer>> temp = new ArrayList<ArrayList<Integer>>();
			ArrayList<ArrayList<Integer>> tempfin = new ArrayList<ArrayList<Integer>>();
			ArrayList<Double> tempfind = new ArrayList<Double>();
			
			//start from batch with smallest number
			for(int t=0;t<tempidxpath.size();t++){
				if(tempfreqpath.get(t).equals(s)){
					temp.add(tempidxpath.get(t)); //select those with particular number of occurrences
				}
			}
			
			//get the distance and complete path info
			for(int u=0;u<temp.size();u++){
				for(int v=0;v<temppath.size();v++){
					if(temppath.get(v).equals(temp.get(u))){
						tempfin.add(vsSeqPath.get(v));
						tempfind.add(vsSeqDistance.get(v));
					}
				}
			}
			
			//sort these grouped batch based on its distances
			for(int w=0;w<tempfin.size();w++){
				for(int x=w+1;x<tempfin.size();x++){
					if(tempfind.get(x)<tempfind.get(w)){
						Collections.swap(tempfin,w,x);
						Collections.swap(tempfind,w,x);
					}
				}
			}
			
			vsSortSeqDistance.addAll(tempfind);
			vsSortSeqPath.addAll(tempfin);
		}
		
		/*check point
		System.out.println("temppath "+temppath);
		System.out.println("tempidxpath "+tempidxpath);
		System.out.println("tempfreqpath "+tempfreqpath);
		
		System.out.println("vsSortSeqPath "+vsSortSeqPath);
		System.out.println("vsSortSeqDistance "+vsSortSeqDistance);
		*/
	}
	
	//generate possible visits based on skill requirement and day requirement
	public void genFeasibility(ArrayList<Integer> cid,ArrayList<Integer> sk){
		
		int m = vsID.size();
		int n = cid.size();
		
		for(int i=0;i<m;i++){
			for(int j=0;j<vsDayReq.get(i).size();j++){
				for(int k=0;k<n;k++){
					//only add to the list on the requested day -- if fulfils skill requirement
					if((vsID.get(i)!=10&&vsDayReq.get(i).get(j)>=1&&sk.get(k)>=vsSkillReq.get(i))||vsID.get(i)==10){
						vsFeaVisit.add(vsID.get(i));
						vsFeaDay.add(j+1);
						vsFeaCtaker.add(cid.get(k));
						
						ArrayList<Integer> temp1 = new ArrayList<Integer>();
						temp1.add(vsID.get(i));
						temp1.add(j+1);
						
						if(!vsFeaVisitDay.contains(temp1)){
							vsFeaVisitDay.add(temp1);
						}
						
						ArrayList<Integer> temp2 = new ArrayList<Integer>();
						temp2.add(vsID.get(i));
						temp2.add(cid.get(k));
						temp2.add(j+1);
						
						vsFeaVisitCtakerDay.add(temp2);
					}
				}
			}
		}
		
		/*check point
		System.out.println("vsFeaVisit "+vsFeaVisit);
		System.out.println("vsFeaCtaker "+vsFeaCtaker);
		System.out.println("vsFeaDay "+vsFeaDay);
		System.out.println("vsFeaVisitDay "+vsFeaVisitDay);
		System.out.println("vsFeaVisitCtakerDay "+vsFeaVisitCtakerDay);
		*/
		
	}
	
	//lookup information of a particular visit
	public Point2D lookupvsLoc(int vid){
		int i = vsID.indexOf(vid);
		
		if(i==-1){
			throw new IllegalArgumentException("Unmatch input");
		}
		
		Point2D l = vsLoc.get(i);
		return l;
	}
	
	public int lookupvsPrior(int vid){
		int i = vsID.indexOf(vid);
		
		if(i==-1){
			throw new IllegalArgumentException("Unmatch input");
		}
		
		int pr = vsPrior.get(i);
		return pr;
	}
	
	public int lookupvsSkillReq(int vid){
		int i = vsID.indexOf(vid);
		
		if(i==-1){
			throw new IllegalArgumentException("Unmatch input");
		}
		
		int srq = vsSkillReq.get(i);
		return srq;
	}
	
	public ArrayList<Integer> lookupvsCtakerReq(int vid){
		int i = vsID.indexOf(vid);
		
		if(i==-1){
			throw new IllegalArgumentException("Unmatch input");
		}
		
		ArrayList<Integer> crq = vsCtakerReq.get(i);
		return crq;
	}
	
	public ArrayList<Integer> lookupvsDayReq(int vid){
		int i = vsID.indexOf(vid);
		
		if(i==-1){
			throw new IllegalArgumentException("Unmatch input");
		}
		
		ArrayList<Integer> drq = vsDayReq.get(i);
		return drq;
	}
	
	public double lookupvsTWin1(int vid){
		int i = vsID.indexOf(vid);
		
		if(i==-1){
			throw new IllegalArgumentException("Unmatch input");
		}
		
		double tw1 = vsTWin1.get(i);
		return tw1;
	}
	
	public double lookupvsTWin2(int vid){
		int i = vsID.indexOf(vid);
		
		if(i==-1){
			throw new IllegalArgumentException("Unmatch input");
		}
		
		double tw2 = vsTWin2.get(i);
		return tw2;
	}
	
	public double lookupvsTTravel(int vid1, int vid2){
		ArrayList<Integer> temp = new ArrayList<Integer>();
		temp.add(vid1);
		temp.add(vid2);
		
		int i = vsPath.indexOf(temp);
		
		if(i==-1){
			throw new IllegalArgumentException("Unmatch input");
		}
		
		double tt = vsTTravel.get(i);
		return tt;
	}
	
	public double lookupvsDistance(int vid1, int vid2){
		ArrayList<Integer> temp = new ArrayList<Integer>();
		temp.add(vid1);
		temp.add(vid2);
		
		int i = vsPath.indexOf(temp);
		
		if(i==-1){
			throw new IllegalArgumentException("Unmatch input");
		}
		
		double dist = vsDistance.get(i);
		return dist;
	}
	
	public ArrayList<Integer> lookupvsFeaDay(int vid){
		ArrayList<Integer> temp = new ArrayList<Integer>();

		if(vsFeaVisit.indexOf(vid)==-1){
			throw new IllegalArgumentException("Unmatch input");
		}
		else{
			for(int i=0;i<vsFeaVisit.size();i++){

				if(vsFeaVisit.get(i)==vid&&!temp.contains(vsFeaDay.get(i))){
					temp.add(vsFeaDay.get(i));
				}
			}	
		}
		
		return temp;
	}

	public ArrayList<Integer> lookupvsFeaCtaker(int vid){
		ArrayList<Integer> temp = new ArrayList<Integer>();

		if(vsFeaVisit.indexOf(vid)==-1){
			throw new IllegalArgumentException("Unmatch input");
		}
		else{
			for(int i=0;i<vsFeaVisit.size();i++){

				if(vsFeaVisit.get(i)==vid&&!temp.contains(vsFeaCtaker.get(i))){
					temp.add(vsFeaCtaker.get(i));
				}
			}	
		}
		
		return temp;
	}
	
	public ArrayList<ArrayList<Integer>> lookupvsFeaVisitCtakerDay(int vid){
		ArrayList<ArrayList<Integer>> temp = new ArrayList<ArrayList<Integer>>();

		if(vsFeaVisit.indexOf(vid)==-1){
			throw new IllegalArgumentException("Unmatch input");
		}
		else{
			for(int i=0;i<vsFeaVisit.size();i++){

				if(vsFeaVisit.get(i)==vid&&!temp.contains(vsFeaVisitCtakerDay.get(i))){
					temp.add(vsFeaVisitCtakerDay.get(i));
				}
			}	
		}
		
		return temp;
	}
	
	//get variables
	public ArrayList<Integer> getvsID(){
		return vsID;
	}
	
	public ArrayList<Point2D> getvsLoc(){
		return vsLoc;
	}
	
	public ArrayList<Integer> getvsPrior(){
		return vsPrior;
	}
	
	public ArrayList<Integer> getvsSkillReq(){
		return vsSkillReq;
	}

	public ArrayList<ArrayList<Integer>> getvsCtakerReq(){
		return vsCtakerReq;
	}
	
	public ArrayList< ArrayList<Integer>> getvsDayReq(){
		return vsDayReq;
	}
	
	
	public ArrayList<ArrayList<Integer>> getvsDayVisit(){
		int nd=vsDayReq.get(0).size();
		vsDayVisit.clear();
		for(int i=0;i<nd;i++){
			ArrayList<Integer> temp=new ArrayList<Integer>();
			for(int j=0;j<vsDayReq.size();j++){
				if(vsDayReq.get(j).get(i)==1){
					temp.add(vsID.get(j));
				}
				
			}
			vsDayVisit.add(temp);	
		}
		return vsDayVisit;
	}
	
	public ArrayList<Double> getvsTWin1(){
		return vsTWin1;
	}
	
	public ArrayList<Double> getvsTWin2(){
		return vsTWin2;
	}
	
	public ArrayList<ArrayList<Integer>> getvsPath(){
		return vsPath;
	}
	
	public ArrayList<Integer> getvsIdxPath(){
		return vsIdxPath;
	}
	
	public ArrayList<Double> getvsTTravel(){
		return vsTTravel;
	}
	
	public ArrayList<Double> getvsDistance(){
		return vsDistance;
	}
	
	public ArrayList<ArrayList<Double>> getvsDService(){
		return vsDService;
	}
	
	public ArrayList<ArrayList<Double>> getvsTEnd1(){
		return vsTEnd1;
	}
	
	public ArrayList<ArrayList<Double>> getvsTEnd2(){
		return vsTEnd2;
	}
	
	public ArrayList<ArrayList<Integer>> getvsSeqPath(){
		return vsSeqPath;
	}
	
	public ArrayList<ArrayList<Integer>> getvsSortSeqPath(){
		return vsSortSeqPath;
	}
	
	public ArrayList<ArrayList<Integer>> getvsSeqPathCtaker(){
		return vsSeqPathCtaker;
	}
	
	public ArrayList<Integer> getvsSeqCtaker(){
		return vsSeqCtaker;
	}
	
	public ArrayList<Double> getvsSeqTTravel(){
		return vsSeqTTravel;
	}
	
	public ArrayList<Double> getvsSeqDistance(){
		return vsSeqDistance;
	}
	
	public ArrayList<Double> getvsSortSeqDistance(){
		return vsSortSeqDistance;
	}

	public ArrayList<Integer> getvsFeaVisit(){
		return vsFeaVisit;
	}

	public ArrayList<Integer> getvsFeaCtaker(){
		return vsFeaCtaker;
	}
	
	public ArrayList<Integer> getvsvsFeaDay(){
		return vsFeaDay;
	}
	
	public ArrayList<ArrayList<Integer>> getvsFeaVisitDay(){
		return vsFeaVisitDay;
	}
	
	public ArrayList<ArrayList<Integer>> getvsFeaVisitCtakerDay(){
		return vsFeaVisitCtakerDay;
	}
}
