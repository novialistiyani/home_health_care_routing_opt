import java.awt.Point;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.awt.geom.Point2D;

public class Main {
	public static void main(String[] args) throws Exception{
		
		//require that there is at least one ctaker that fulfills skill requirement
		//else the associated visit will be dismissed automatically -- assumed to be a part of error in input data 
		Reader rr = new Reader();				
		Visits vs = new Visits();
		Ctakers ct = new Ctakers();
		
		int nd = 5;
		int rs = 4;
		double hStartWork = 0.0;
		double hEndWork = 4.0;//540.0; 	
		double hWorkBreak = 1.5;//240.0; 	
		int numCTaker = 100;		//max number of ctakers allowed to handle one patient
		int limCtaker = 4;			//number of ctakers employed (change if number of visit change)
		double limTime = 3600000;	//change
		String flname1 = "(25vs) C201_2.csv";
		String flname2 = "(30ct) CT1.csv";
		
		
		rr.readVisit(flname1);
		ArrayList<Integer> rvsID = rr.getrvsID();
		ArrayList<Double>rvsLocX = rr.getrvsLocX();
		ArrayList<Double> rvsLocY =rr.getrvsLocY();
		ArrayList<Double> rvsTWin1 = rr.getrvsTWin1();
		ArrayList<Double> rvsTWin2 = rr.getrvsTWin2();	
		ArrayList<Integer> rvsPrior = rr.getrvsPrior();
		ArrayList<Integer> rvsSkillReq = rr.getrvsSkillReq();
		ArrayList<ArrayList<Integer>> rvsDayReq = rr.getrvsDayReq();
		
		System.out.println(rvsID);
		
		for(int ra=0;ra<rvsID.size();ra++){
			vs.addVisitData(rvsID.get(ra),rvsLocX.get(ra),rvsLocY.get(ra),rvsPrior.get(ra),rvsSkillReq.get(ra),rvsDayReq.get(ra),rvsTWin1.get(ra),rvsTWin2.get(ra),nd);	
		}
		
		for(int rb=0;rb<rvsID.size();rb++){
			for(int rc=0;rc<rvsID.size();rc++){
				
				double px1 = rvsLocX.get(rb);
				double py1 = rvsLocY.get(rb);
				double px2 = rvsLocX.get(rc);
				double py2 = rvsLocY.get(rc);
				
				if(!rvsID.get(rb).equals(rvsID.get(rc))){
					vs.addPath(rvsID.get(rb),rvsID.get(rc),(Point2D.distance(px1,py1,px2,py2))/2);	
				}
				
			}
		}
		
		
		rr.readCtaker(flname2,limCtaker);
		ArrayList<Integer> rctID = rr.getrctID();
		ArrayList<Integer> rctSkill = rr.getrctSkill();
		ArrayList<Double> rctWage =rr.getrctWage();
		ArrayList<Double> rctDBreak = rr.getrctDBreak();
		ArrayList<ArrayList<Double>> rctDService = rr.getrctDService();
		
		
		for(int rd=0;rd<rctID.size();rd++){
			ct.addCtakerData(rctID.get(rd), rctSkill.get(rd), rctWage.get(rd), rctDBreak.get(rd), rctDService.get(rd), rs);
		}
		
		
		
		//small example data starts
		ArrayList<Integer> tempd1 = new ArrayList<Integer>();
		ArrayList<Integer> tempd2 = new ArrayList<Integer>();
		ArrayList<Integer> tempd3 = new ArrayList<Integer>();
		ArrayList<Integer> tempd4 = new ArrayList<Integer>();
		ArrayList<Integer> tempd5 = new ArrayList<Integer>();
		
		Collections.addAll(tempd1,0,0,0,0,0);
		Collections.addAll(tempd2,1,1,1,1,1);
		Collections.addAll(tempd3,0,1,0,1,0);
		Collections.addAll(tempd4,0,1,0,1,0);
		Collections.addAll(tempd5,0,0,1,1,0);
		/*
		//input small example visit data
		//custID,coord-x, coord-y, priority level, skill requirement, day requirement, time window1, time window2, number of days 
		vs.addVisitData(10,0,0,0,0,tempd1,hStartWork,hEndWork,nd);
		vs.addVisitData(20,0,1,1,1,tempd2,1,1,nd); 
		vs.addVisitData(30,1,1,3,3,tempd3,1,1,nd);
		vs.addVisitData(40,1,0,2,2,tempd4,3,3,nd);
		vs.addVisitData(50,2,1,4,2,tempd5,3,3,nd);
		
		//add small example possible paths data
		//assuming non-symmetric - though for now eucledian distance is used
		vs.addPath(10,20,0.25);
		vs.addPath(10,30,0.35);
		vs.addPath(10,40,0.25);
		vs.addPath(10,50,0.25);
		vs.addPath(20,10,0.25);
		vs.addPath(30,10,0.35);
		vs.addPath(40,10,0.25);
		vs.addPath(50,10,0.25);
		vs.addPath(20,10,0.25);
		vs.addPath(20,30,0.25);
		vs.addPath(20,40,0.35);
		vs.addPath(20,50,0.25);
		vs.addPath(10,20,0.25);
		vs.addPath(30,20,0.25);
		vs.addPath(40,20,0.35);
		vs.addPath(50,20,0.25);
		vs.addPath(30,10,0.35);
		vs.addPath(30,20,0.25);
		vs.addPath(30,40,0.25);
		vs.addPath(30,50,0.25);
		vs.addPath(10,30,0.35);
		vs.addPath(20,30,0.25);
		vs.addPath(40,30,0.25);
		vs.addPath(50,30,0.25);
		vs.addPath(40,10,0.25);
		vs.addPath(40,20,0.35);
		vs.addPath(40,30,0.25);
		vs.addPath(40,50,0.25);
		vs.addPath(10,40,0.25);
		vs.addPath(20,40,0.35);
		vs.addPath(30,40,0.25);
		vs.addPath(50,40,0.25);
		vs.addPath(50,10,0.25);
		vs.addPath(50,20,0.25);
		vs.addPath(50,30,0.25);
		vs.addPath(50,40,0.25);
		vs.addPath(10,50,0.25);
		vs.addPath(20,50,0.25);
		vs.addPath(30,50,0.25);
		vs.addPath(40,50,0.25);
		
		//input small example caretakers data
		ArrayList<Double> temps1 = new ArrayList<Double>();
		ArrayList<Double> temps2 = new ArrayList<Double>();
		ArrayList<Double> temps3 = new ArrayList<Double>();
		
		Collections.addAll(temps1,0.0,0.1,0.8,0.125,0.5);
		Collections.addAll(temps2, 0.0,0.05,0.75,0.125,100000.0);
		Collections.addAll(temps3, 0.0,0.13,1.25,100000.0,100000.0);
		
		//ctakerID, skill, wage, break duration, skill-based service duration, skill range (should be the same)
		ct.addCtakerData(11, 4, 15*60, 0.25, temps1,rs); //biar cocok sm julia
		ct.addCtakerData(21, 3, 10*60, 0.25, temps2,rs);	
		ct.addCtakerData(31, 2, 5*60, 0.25,  temps3,rs);		
		//small example data ends
		*/

		//set up weight for objective function: distance, ttravel, continuity of care, uncovered visit, ctaker wage, break time compared to minimum working hours required before break (so that it's not too early)
		ArrayList<Double> wObj = new ArrayList<Double>();

		/*
		int sumReq = 0;
		for(int re=0;re<vs.getvsDayReq().size();re++){
			for(int rf=0;rf<vs.getvsDayReq().get(re).size();rf++){
				sumReq+=(vs.getvsDayReq()).get(re).get(rf);	
			}
			
		}
		
		wObj.add(1000.0/((sumReq+limCtaker)*Collections.max(vs.getvsDistance())));
		wObj.add(1000.0/((sumReq+limCtaker)*Collections.max(vs.getvsTTravel())));
		wObj.add(1000.0/((double)sumReq*limCtaker));
		wObj.add(1000000.0/((double)sumReq*Collections.max(vs.getvsPrior())));
		wObj.add(1000.0/((nd*limCtaker*Collections.max(ct.getctWage())*(hEndWork-hStartWork))/60.0));
		wObj.add(10.0/(nd*limCtaker*hWorkBreak));
		*/
		
		//weight for small example
		Collections.addAll(wObj, (100/(55.0)),(100/(12.0)),(100/(16.0)),(10000/(6.0)),(100/(215.0)),(10/(9.0)));
		//Collections.addAll(wObj, 1.0,1.0,1.0,100.0,1.0,0.1);
		System.out.println("wObj "+wObj);
		
		//generate time service per visit, by considering skill requirement and caretakers skill
		vs.genDService(ct.getctID(),ct.getctSkill(),ct.getctDService());
		
		//generate precedence between visits, by considering time window, time service, and travel time
		vs.genPrecedence(ct.getctID());
		vs.genFeasibility(ct.getctID(),ct.getctSkill());
		vs.genSortPrecedence();
		
			
		//check point
		System.out.println();
		System.out.println("Data1");
		System.out.println("ctID "+ct.getctID());
		System.out.println("ctSkill "+ct.getctSkill());
		System.out.println("ctWage "+ct.getctWage());
		System.out.println("ctDService "+ct.getctDService());
		System.out.println();
		System.out.println("vsID "+vs.getvsID());
		System.out.println("vsDayReq "+vs.getvsDayReq());
		System.out.println("vsSkillReq "+vs.getvsSkillReq());
		
		
		//check point
		System.out.println();
		System.out.println("Data2");
		System.out.println("vsPath "+vs.getvsPath());
		System.out.println("vsDist "+vs.getvsDistance());
		System.out.println("vsTTravel "+vs.getvsTTravel());
		System.out.println("vsTWin1 "+vs.getvsTWin1());
		System.out.println("vsTWin2 "+vs.getvsTWin2());
		
		
		//check point
		System.out.println();
		System.out.println("Data3");
		System.out.println("vsDayVisit "+vs.getvsDayVisit());
		System.out.println("vsCtakerReq "+vs.getvsCtakerReq());
		System.out.println("vsDService "+vs.getvsDService());
		System.out.println("TEnd1 "+vs.getvsTEnd1());
		System.out.println("TEnd2 "+vs.getvsTEnd2());
		
		
		//check point
		System.out.println();
		System.out.println("Data4");
		System.out.println("vsSeqPath "+vs.getvsSeqPath());
		System.out.println("vsSeqDistance "+vs.getvsSeqDistance());
		System.out.println("vsSeqCtaker "+vs.getvsSeqCtaker());
		System.out.println("vsSeqTTravel "+vs.getvsSeqTTravel());
		System.out.println("vsSortSeqPath "+vs.getvsSortSeqPath());
		System.out.println("vsSortSeqDistance "+vs.getvsSortSeqDistance());
		
		
		Supports s = new Supports();
		double perc = 25.0; //change: 25,50
		
		double r=1;			
		double deg=250;//250;		
		double alpha=0.5;	
		double lim=0.01;//0.01;	
		int t=500;//1000; 		//total iteration is defined here
		int seg=20;//50; 		//segment is defined here
		
	
		//initial setup for the weight
		double wsOp1=1.0;	//reward if operator can lead to global best solution
		double wsOp2=0.75;	//reward if operator can lead to a better solution (but not global best solution)
		double wsOp3=0.5;	//reward if operator can lead to worse solution, but still accepted by SA probability
		double wsOp4=0.1;	//reward if operator can lead to worse solution and rejected by SA probability
		
		//initial weight for destroy random operator [0], destroy distance operator [1], for destroy continuity operator [2], destroy worst case operator [3]  
		ArrayList<Double> wDestroy = new ArrayList<Double>();		//weight for destroy operators
		ArrayList<Double> wCumDestroy = new ArrayList<Double>();	//cumulative weight for destroy operators
		ArrayList<Double> wiDestroy = new ArrayList<Double>();		//initial weight for destroy operators
		ArrayList<Double> wsDestroy = new ArrayList<Double>();		//score for destroy operators
		ArrayList<Integer> countDestroy = new ArrayList<Integer>();	//count how many times each destroy operators being called
		ArrayList<Double> dDestroy = new ArrayList<Double>();		//measure length of time spent for each destroy operator
		ArrayList<Double> dRepair = new ArrayList<Double>();		//measure length of time spent for each destroy operator
		
		double sumInitDestroy = 0.0;
		
		Collections.addAll(wiDestroy,1.0,1.0,1.0,1.0);
		
		for(int e=0;e<wiDestroy.size();e++){
			sumInitDestroy += wiDestroy.get(e);
			wDestroy.add(wiDestroy.get(e));
			wsDestroy.add(0.0);
			countDestroy.add(0);
			dDestroy.add(0.0);
		}
		
		//compute cumulative weight for roulette wheel 
		for(int g=0;g<wiDestroy.size();g++){
			if(g==0){
				wCumDestroy.add(wDestroy.get(g)/sumInitDestroy);
			}
			else{
				wCumDestroy.add((wCumDestroy.get(g-1))+(wDestroy.get(g)/sumInitDestroy));
			}
		}
		
		//initial weight for repair random operator [0], repair greedy operator [1], repair regret operator [2]  
		ArrayList<Double> wRepair = new ArrayList<Double>();		//weight for repair operators
		ArrayList<Double> wCumRepair = new ArrayList<Double>();		//cumulative weight for repair operators
		ArrayList<Double> wiRepair = new ArrayList<Double>();		//initial weight for repair operators
		ArrayList<Double> wsRepair = new ArrayList<Double>();		//score for repair operators
		ArrayList<Integer> countRepair = new ArrayList<Integer>();	//count how many times each repair operators being called
		double sumInitRepair = 0.0;
		
		Collections.addAll(wiRepair,1.0,1.0,1.0);
		
		for(int f=0;f<wiRepair.size();f++){
			sumInitRepair += wiRepair.get(f);
			wRepair.add(wiRepair.get(f));
			wsRepair.add(0.0);
			countRepair.add(0);
			dRepair.add(0.0);
		}
		
		//compute cumulative weight for roulette wheel
		for(int h=0;h<wiRepair.size();h++){
			if(h==0){
				wCumRepair.add(wRepair.get(h)/sumInitRepair);
			}
			else{
				wCumRepair.add((wCumRepair.get(h-1))+(wDestroy.get(h)/sumInitRepair));
			}
		}

		
		//generate initial solution
		s.genInitSol(ct.getctID(),ct.getctWage(),vs.getvsID(),vs.getvsPrior(),vs.getvsDService(),vs.getvsDayVisit(),vs.getvsDayReq(),vs.getvsSkillReq(),vs.getvsCtakerReq(),vs.getvsSortSeqPath(),vs.getvsSeqPath(),vs.getvsSeqDistance(),vs.getvsSeqTTravel(),vs.getvsTWin1(),vs.getvsTWin2(),ct.getctDBreak(),vs.getvsFeaVisit(),vs.getvsFeaVisitDay(),vs.getvsFeaVisitCtakerDay(),wObj,hEndWork,hWorkBreak,nd,numCTaker,r);
		
		//initial empty arrays
		//to keep global best solution
		ArrayList<Integer> xBest=new ArrayList<Integer>();
		ArrayList<Integer> xDayBest=new ArrayList<Integer>();
		ArrayList<Integer> xCtakerBest=new ArrayList<Integer>();
			
		//to keep currently used solution
		ArrayList<Integer> xNow=new ArrayList<Integer>();
		ArrayList<Integer> xDayNow=new ArrayList<Integer>();
		ArrayList<Integer> xCtakerNow=new ArrayList<Integer>();
	
		//to keep result from destroy-repair operators
		ArrayList<Integer> yBest=new ArrayList<Integer>();
		ArrayList<Integer> yNow=new ArrayList<Integer>();

		//initial solution is used to start iteration
		xBest=s.getsolVec();
		xDayBest=s.getsolDay();
		xCtakerBest=s.getsolCtaker();
		
		xNow=s.getsolVec();
		xDayNow=s.getsolDay();
		xCtakerNow=s.getsolCtaker();
		
		yBest=s.getsolBank();
		yNow=s.getsolBank();
		
		
		s.gensolTimeAll(xBest,xCtakerBest,xDayBest,yBest,ct.getctID(),vs.getvsID(),vs.getvsSeqTTravel(),vs.getvsSeqPath(),vs.getvsDService(),nd);	
		ArrayList<Double> soldwbest = s.getsolDWork();
		s.gensolTimeSign(xBest,xCtakerBest,xDayBest,yBest,ct.getctID(),vs.getvsID(),soldwbest,vs.getvsTWin1(),vs.getvsTWin2(),ct.getctDBreak(),hWorkBreak,nd);	
		ArrayList<Double> tsbest = s.getsolTStart();
		ArrayList<Double> tebest = s.getsolTEnd();
		ArrayList<Double> tbrbest = s.getsolTBreak();
		ArrayList<Double> tvbest = s.getsolTVisit();
		
		/*check point
		System.out.println();
		System.out.println("Data5");
		System.out.println("soldwbest "+soldwbest.size()+soldwbest);
		System.out.println("tsbest "+tsbest.size()+tsbest);
		System.out.println("tebest "+tebest.size()+tebest);
		System.out.println("tbrbest "+tbrbest.size()+tbrbest);
		System.out.println("tvbest "+tvbest.size()+tvbest);
		*/
		
		double evdistbest = s.evalsolDist(xBest,xCtakerBest,xDayBest,yBest,vs.getvsSeqPath(), vs.getvsSeqDistance());
		//System.out.println("evdistbest "+evdistbest);
		//System.out.println("solDist "+s.getsolDist());
		double evttravelbest = s.evalsolTTravel(xBest,xCtakerBest,xDayBest,yBest,vs.getvsSeqPath(),vs.getvsSeqTTravel());
		//System.out.println("evttravelbest "+evttravelbest);
		//System.out.println("solTTravel "+s.getsolTTravel());
		double evcontbest = (double) s.evalsolCont(xBest,xCtakerBest,xDayBest,yBest,numCTaker);
		//System.out.println("evcontbest "+evcontbest);
		//System.out.println("solVisit "+s.getsolVisit());
		//System.out.println("solVisitCtaker "+s.getsolVisitCtaker());
		double evuncovbest = s.evalsolUncov(vs.getvsID(),xBest,xCtakerBest,xDayBest,yBest,vs.getvsPrior());
		//System.out.println("yBest "+yBest);
		//System.out.println("evuncovbest "+ evuncovbest);
		double evpaybest = s.evalsolPay(ct.getctID(),ct.getctWage(),xBest,xCtakerBest,xDayBest,yBest,tsbest,tebest,hEndWork,nd);
		//System.out.println("evpaybest "+evpaybest);
		//System.out.println("solPay "+s.getsolPay());
		double evbreakbest = s.evalsolBreak(xBest,xCtakerBest,xDayBest,yBest,tsbest,tbrbest,hWorkBreak);
		//System.out.println("evbreakbest "+evbreakbest);
		//System.out.println("solBreakGap "+s.getsolBreakGap());

		double fBest = wObj.get(0)*evdistbest+wObj.get(1)*evttravelbest+wObj.get(2)*evcontbest+wObj.get(3)*evuncovbest+wObj.get(4)*evpaybest+wObj.get(5)*evbreakbest;
		double fNow = fBest;
		double fIter = 0.0;
		System.out.println("fBest "+fBest);
		
		
		
		String csv = "C:\\Users\\NoviaLW\\workspace\\ALNS01\\src\\main\\java\\output.csv";
		PrintWriter pw = new PrintWriter(csv);
		StringBuilder sb = new StringBuilder();
		
		sb.append("deg,limdeg,alpha,r,t,seg");
		sb.append('\n');
		sb.append(deg);
		sb.append(',');
		sb.append(lim);
		sb.append(',');
		sb.append(alpha);
		sb.append(',');
		sb.append(r);
		sb.append(',');
		sb.append(t);
		sb.append(',');
		sb.append(seg);
		sb.append(',');
		sb.append('\n');
		sb.append('\n');
		sb.append("filename1,filename2,limCtaker,perc,wObj1,wObj2,wObj3,Obj4,wObj5,wOb6");
		sb.append('\n');
		sb.append(flname1);
		sb.append(',');
		sb.append(flname2);
		sb.append(',');
		sb.append(limCtaker);
		sb.append(',');
		sb.append(perc);
		sb.append(',');
		sb.append(wObj.get(0));
		sb.append(',');
		sb.append(wObj.get(1));
		sb.append(',');
		sb.append(wObj.get(2));
		sb.append(',');
		sb.append(wObj.get(3));
		sb.append(',');
		sb.append(wObj.get(4));
		sb.append(',');
		sb.append(wObj.get(5));
		sb.append('\n');
		sb.append('\n');
		
		sb.append("tStart,tEnd,tRes,countAll,deg,i,idxDestroy,tempdDestroy,idxRepair,tempdRepair,wDestroy0,wDestroy1,wDestroy2,wDestroy3,wRepair0,wRepair1,wRepair2,fIter,evdistiter,evttraveliter,evcontiter,evuncoviter,evpayiter,evbreakiter,fNow,fBest");
		sb.append('\n');
		
		double tRes = 0.0;
		int countAll =0;
		
		while(deg>lim){
			long tStart;
			long tEnd;
			System.out.println("check time "+tRes+" "+limTime);
			
			for(int i=0;i<t;i++){
				tStart = System.nanoTime();
				System.out.println("tStart "+tStart);
				sb.append(tStart/(Math.pow(10, 6)));
				sb.append(',');
				
				Random rand=new Random();
				double p1=rand.nextDouble();
				double p2=rand.nextDouble();
				
				//check point
				System.out.println();
				System.out.println("Main1");
				System.out.println("wsDestroy: "+wsDestroy);	//score destroy
				System.out.println("dDestroy: "+dDestroy);		//total duration taken
				System.out.println("countDestroy: "+countDestroy);//total called
				System.out.println("wDestroy: "+wDestroy);		//weight destroy
				System.out.println("wCumDestroy: "+wCumDestroy);//cumulative weight destroy
				System.out.println("wsRepair: "+wsRepair);		//score repair
				System.out.println("dRepair: "+dRepair);		//total duration taken
				System.out.println("countRepair: "+countRepair);//total called
				System.out.println("wRepair: "+wRepair);		//weight repair
				System.out.println("wCumRepair: "+wCumRepair);	//cumulative weight repair
				
				
				System.out.println();
				System.out.println("deg "+deg);
				System.out.println("i: "+i);
				System.out.println("p1: "+p1);					//to choose a destroy operator
				System.out.println("p2: "+p2);					//to choose a repair operator
				
				
				//roulette wheel mechanism 
				int idxDestroy=0;
				int idxRepair=0;
				
				
				for(int k=0;k<wCumDestroy.size();k++){
					if(p1>=wCumDestroy.get(k)){
						idxDestroy+=1;
					}
				}
				
				
				for(int l=0;l<wCumRepair.size();l++){
					if(p2>=wCumRepair.get(l)){
						idxRepair+=1;	
					}
				}
				
				
				//prepare number of visits to remove -- for destroy operators
				int q=rand.nextInt(xNow.size()*((int)perc)/100);
				
				/*check point
				System.out.println();
				System.out.println("Main2");
				System.out.println("p1 "+p1);
				System.out.println("p2 "+p2);
				System.out.println("idxDestroy "+idxDestroy);
				System.out.println("idxRepair "+idxRepair);
				*/
				System.out.println("q "+q);
				
				
				//copy xNow as xIter
				//to keep solution that will be perturbed 
				ArrayList<Integer> xIter=new ArrayList<Integer>();
				ArrayList<Integer> xDayIter=new ArrayList<Integer>();
				ArrayList<Integer> xCtakerIter=new ArrayList<Integer>();
				ArrayList<Integer> yIter=new ArrayList<Integer>();
				
				double tempdDestroy=0.0;
				double tempdRepair=0.0;
							
				for(int aa=0;aa<xNow.size();aa++){
					xIter.add(xNow.get(aa));
					xCtakerIter.add(xCtakerNow.get(aa));
					xDayIter.add(xDayNow.get(aa));
				}
				
				for(int ab=0;ab<yNow.size();ab++){
					yIter.add(yNow.get(ab));
				}
				fIter=fNow;
				
				//choose operators based on roulette wheel
				//implement destroy operator
				if(idxDestroy==0){
					
					long tsDestroyRandom = System.nanoTime();
					
					s.destroyRandom(xIter, xCtakerIter, xDayIter, yIter,q );
					
					xIter=s.getsolVec();
					xCtakerIter=s.getsolCtaker();
					xDayIter=s.getsolDay();
					yIter=s.getsolBank();
					
					long teDestroyRandom = System.nanoTime();
					tempdDestroy = (teDestroyRandom - tsDestroyRandom)/1.0E06;
					
					/*check point
					System.out.println();
					System.out.println("destroy random");
					System.out.println(">>xBest "+xBest);
					System.out.println(">>yBest "+yBest);
					
					System.out.println(">>xNow "+xNow);
					System.out.println(">>yNow "+yNow);
					
					System.out.println(">>xIter "+xIter);
					System.out.println(">>yIter "+yIter);
					*/
					System.out.println(">>tempdDestroy "+tempdDestroy);
					
				}

				else if(idxDestroy==1){
					
					long tsDestroyDistance = System.nanoTime();
					
					s.destroyDistance(xIter, xCtakerIter, xDayIter, yIter,vs.getvsID(), vs.getvsLoc(), vs.getvsPath(), q, r);
					
					xIter=s.getsolVec();
					xCtakerIter=s.getsolCtaker();
					xDayIter=s.getsolDay();
					yIter=s.getsolBank();
					
					long teDestroyDistance = System.nanoTime();
					tempdDestroy = (teDestroyDistance - tsDestroyDistance)/1.0E06;
					
					/*check point
					System.out.println();
					System.out.println("destroy distance");
					System.out.println(">>xBest "+xBest);
					System.out.println(">>yBest "+yBest);
					
					System.out.println(">>xNow "+xNow);
					System.out.println(">>yNow "+yNow);
					
					System.out.println(">>xIter "+xIter);
					System.out.println(">>yIter "+yIter);	
					*/
					System.out.println(">>tempdDestroy "+tempdDestroy);
					
				}
				
				else if(idxDestroy==2){
					
					long tsDestroyCont = System.nanoTime();
					
					s.destroyCont(xIter, xCtakerIter, xDayIter, yIter, q);
					
					xIter=s.getsolVec();
					xCtakerIter=s.getsolCtaker();
					xDayIter=s.getsolDay();
					yIter=s.getsolBank();
					
					long teDestroyCont = System.nanoTime();
					tempdDestroy = (teDestroyCont - tsDestroyCont)/1.0E06;
					
					/*check point
					System.out.println();
					System.out.println("destroy continuity");
					System.out.println(">>xBest "+xBest);
					System.out.println(">>yBest "+yBest);
					
					System.out.println(">>xNow "+xNow);
					System.out.println(">>yNow "+yNow);
					
					System.out.println(">>xIter "+xIter);
					System.out.println(">>yIter "+yIter);
					*/
					System.out.println(">>tempdDestroy "+tempdDestroy);
					
				}
				
				else if(idxDestroy==3){
					
					long tsDestroyWorst = System.nanoTime();
					
					s.destroyWorst(ct.getctID(),ct.getctWage(),vs.getvsID(),vs.getvsPrior(), xIter, xCtakerIter, xDayIter, yIter,vs.getvsDService(),vs.getvsSeqPath(),vs.getvsSeqDistance(),vs.getvsSeqTTravel(),vs.getvsTWin1(),vs.getvsTWin2(),ct.getctDBreak(),wObj,hEndWork,hWorkBreak,nd,numCTaker,q,r);
					
					xIter=s.getsolVec();
					xCtakerIter=s.getsolCtaker();
					xDayIter=s.getsolDay();
					yIter=s.getsolBank();
					
					long teDestroyWorst = System.nanoTime();
					tempdDestroy = (teDestroyWorst - tsDestroyWorst)/1.0E06;
					
					/*check point
					System.out.println();
					System.out.println("destroy worst");
					System.out.println(">>xBest "+xBest);
					System.out.println(">>yBest "+yBest);
					
					System.out.println(">>xNow "+xNow);
					System.out.println(">>yNow "+yNow);
					
					System.out.println(">>xIter "+xIter);
					System.out.println(">>yIter "+yIter);
					System.out.println(">>fIter "+fIter);
					*/
					
					System.out.println(">>tempdDestroy "+tempdDestroy);
					
				}
				
				/*check point
				System.out.println();
				System.out.println("Main3");
				System.out.println("AFTER DESTROY:");
				System.out.println(">>xBest "+xBest.size()+" "+xBest);
				System.out.println(">>yBest "+yBest.size()+" "+yBest);
				System.out.println(">>xNow "+xNow.size()+" "+xNow);
				System.out.println(">>yNow "+yNow.size()+" "+yNow);
				System.out.println(">>xIter "+xIter.size()+" "+xIter);
				System.out.println(">>yIter "+yIter.size()+" "+yIter);
				*/
				
				//implement repair operator
				if(idxRepair==0){
					
					long tsRepairRandom = System.nanoTime();
					
					s.repairRandom(ct.getctID(),ct.getctWage(),vs.getvsID(),vs.getvsPrior(),xIter, xCtakerIter, xDayIter, yIter,vs.getvsDService(),vs.getvsSeqPath(),vs.getvsSeqDistance(),vs.getvsSeqTTravel(),vs.getvsTWin1(),vs.getvsTWin2(),ct.getctDBreak(),vs.getvsFeaVisit(),vs.getvsFeaVisitDay(),vs.getvsFeaVisitCtakerDay(),wObj,hEndWork, hWorkBreak, nd, numCTaker);
				
					
					
					xIter=s.getsolVec();
					xCtakerIter=s.getsolCtaker();
					xDayIter=s.getsolDay();
					yIter=s.getsolBank();
					
					long teRepairRandom = System.nanoTime();
					tempdRepair = (teRepairRandom - tsRepairRandom)/1.0E06;
					
					/*check point
					System.out.println();
					System.out.println("repair random");
					System.out.println(">>xBest "+xBest);
					System.out.println(">>yBest "+yBest);
					System.out.println(">>fBest "+fBest);
					
					System.out.println(">>xNow "+xNow);
					System.out.println(">>yNow "+yNow);
					System.out.println(">>fNow "+fNow);
					
					System.out.println(">>xIter "+xIter);
					System.out.println(">>yIter "+yIter);
					System.out.println(">>fIter "+fIter);
					*/
					
					System.out.println(">>tempdRepair "+tempdRepair);
					
				}
				
				else if(idxRepair==1){
					
					long tsRepairGreedy = System.nanoTime();
					
					s.repairGreedy(ct.getctID(),ct.getctWage(),vs.getvsID(),vs.getvsPrior(),xIter, xCtakerIter, xDayIter, yIter,vs.getvsDService(),vs.getvsSeqPath(),vs.getvsSeqDistance(),vs.getvsSeqTTravel(),vs.getvsTWin1(),vs.getvsTWin2(),ct.getctDBreak(),vs.getvsFeaVisit(),vs.getvsFeaVisitDay(),vs.getvsFeaVisitCtakerDay(),wObj,hEndWork, hWorkBreak, nd, numCTaker);

					System.out.println(">>yIter "+yIter);
					
					xIter=s.getsolVec();
					xCtakerIter=s.getsolCtaker();
					xDayIter=s.getsolDay();
					yIter=s.getsolBank();
					
					long teRepairGreedy = System.nanoTime();
					tempdRepair = (teRepairGreedy - tsRepairGreedy)/1.0E06;
					
					/*check point
					System.out.println();
					System.out.println("repair greedy");
					System.out.println(">>xBest "+xBest);
					System.out.println(">>yBest "+yBest);
					System.out.println(">>fBest "+fBest);
					
					System.out.println(">>xNow "+xNow);
					System.out.println(">>yNow "+yNow);
					System.out.println(">>fNow "+fNow);
					
					System.out.println(">>xIter "+xIter);
					System.out.println(">>yIter "+yIter);
					System.out.println(">>fIter "+fIter);
					*/
					
					System.out.println(">>tempdRepair "+tempdRepair);
					
				}
				
				else if(idxRepair==2){
					
					long tsRepairRegret = System.nanoTime();
					
					s.repairRegret(ct.getctID(),ct.getctWage(),vs.getvsID(),vs.getvsPrior(),xIter, xCtakerIter, xDayIter, yIter,vs.getvsDService(),vs.getvsSeqPath(),vs.getvsSeqDistance(),vs.getvsSeqTTravel(),vs.getvsTWin1(),vs.getvsTWin2(),ct.getctDBreak(),vs.getvsFeaVisit(),vs.getvsFeaVisitDay(),vs.getvsFeaVisitCtakerDay(),wObj,hEndWork, hWorkBreak, nd, numCTaker);
					
					System.out.println(">>yIter "+yIter);
					
					xIter=s.getsolVec();
					xCtakerIter=s.getsolCtaker();
					xDayIter=s.getsolDay();
					yIter=s.getsolBank();
					
					long teRepairRegret = System.nanoTime();
					tempdRepair = (teRepairRegret - tsRepairRegret)/1.0E06;
					
					/*check point
					System.out.println();
					System.out.println("repair regret");
					System.out.println(">>xBest "+xBest);
					System.out.println(">>yBest "+yBest);
					System.out.println(">>fBest "+fBest);
					
					System.out.println(">>xNow "+xNow);
					System.out.println(">>yNow "+yNow);
					System.out.println(">>fNow "+fNow);
					
					System.out.println(">>xIter "+xIter);
					System.out.println(">>yIter "+yIter);
					System.out.println(">>fIter "+fIter);
					*/
					
					System.out.println(">>tempdRepair "+tempdRepair);
					
				}
				
				
				/*check point
				System.out.println();
				System.out.println("Main4");
				System.out.println("AFTER REPAIR:");
				System.out.println("xIter: "+xIter.size()+" "+xIter);
				System.out.println("xCtakerIter: "+xCtakerIter);
				System.out.println("xDayIter: "+xDayIter);
				System.out.println("yIter: "+yIter.size()+" "+yIter);
				*/
				
				s.gensolTimeAll(xIter,xCtakerIter,xDayIter,yIter,ct.getctID(),vs.getvsID(),vs.getvsSeqTTravel(),vs.getvsSeqPath(),vs.getvsDService(),nd);	
				ArrayList<Double> soldwiter = s.getsolDWork();
				s.gensolTimeSign(xIter,xCtakerIter,xDayIter,yIter,ct.getctID(),vs.getvsID(),soldwiter,vs.getvsTWin1(),vs.getvsTWin2(),ct.getctDBreak(),hWorkBreak,nd);	
				ArrayList<Double> tsiter = s.getsolTStart();
				ArrayList<Double> teiter = s.getsolTEnd();
				ArrayList<Double> tbriter = s.getsolTBreak();
				
				double evdistiter = s.evalsolDist(xIter,xCtakerIter,xDayIter,yIter,vs.getvsSeqPath(), vs.getvsSeqDistance());
				double evttraveliter = s.evalsolTTravel(xIter,xCtakerIter,xDayIter,yIter,vs.getvsSeqPath(),vs.getvsSeqTTravel());
				double evcontiter = (double) s.evalsolCont(xIter,xCtakerIter,xDayIter,yIter,numCTaker);
				double evuncoviter = s.evalsolUncov(vs.getvsID(),xIter,xCtakerIter,xDayIter,yIter,vs.getvsPrior());
				double evpayiter = s.evalsolPay(ct.getctID(),ct.getctWage(),xIter,xCtakerIter,xDayIter,yIter,tsiter,teiter,hEndWork,nd);
				double evbreakiter = s.evalsolBreak(xIter,xCtakerIter,xDayIter,yIter,tsiter,tbriter,hWorkBreak);
				
				fIter=wObj.get(0)*evdistiter+wObj.get(1)*evttraveliter+wObj.get(2)*evcontiter+wObj.get(3)*evuncoviter+wObj.get(4)*evpayiter+wObj.get(5)*evbreakiter;
				
				/*check point
				System.out.println();
				System.out.println("Main5");
				System.out.println("UPDATE COST");
				System.out.println("fBest: "+fBest);
				System.out.println("fNow: "+fNow);
				System.out.println("fIter: "+fIter);
				*/
				
				/*
				System.out.println("evdistiter "+evdistiter);
				System.out.println("evttraveliter "+evttraveliter);
				System.out.println("evcontiter "+evcontiter);
				System.out.println("evuncoviter "+evuncoviter);
				System.out.println("evpayiter "+evpayiter);
				System.out.println("evbreakiter "+evbreakiter);
				*/
				
				if(fIter<fNow){
					
					//if(perc>10) perc=perc/1.10;
					
					//if solution from destroy-repair operator is better than current solution, take them as current solution
					xNow.clear();
					xCtakerNow.clear();
					xDayNow.clear();
					yNow.clear();
					
					for(int ac=0;ac<xIter.size();ac++){
						xNow.add(xIter.get(ac));
						xCtakerNow.add(xCtakerIter.get(ac));
						xDayNow.add(xDayIter.get(ac));
					}
					
					for(int ad=0;ad<yIter.size();ad++){
						yNow.add(yIter.get(ad));
					}
					
					fNow=fIter;
					
					
					//if solution from destroy-repair operator is also better than global best solution
					if(fIter<fBest){
						System.out.println("Check1: "+idxDestroy+" "+wsDestroy.get(idxDestroy)+" "+wsOp1);					
						
						//add score and increment counting for destroy operator
						wsDestroy.set(idxDestroy, wsDestroy.get(idxDestroy)+wsOp1);		
						countDestroy.set(idxDestroy, countDestroy.get(idxDestroy)+1);	
						dDestroy.set(idxDestroy, dDestroy.get(idxDestroy)+tempdDestroy);
						
						//add score and increment counting for repair operator
						wsRepair.set(idxRepair, wsRepair.get(idxRepair)+wsOp1);			
						countRepair.set(idxRepair, countRepair.get(idxRepair)+1);
						dRepair.set(idxRepair, dRepair.get(idxRepair)+tempdRepair);
						
						//take solution as global best solution
						xBest.clear();
						xCtakerBest.clear();
						xDayBest.clear();
						yBest.clear();
						
						for(int ae=0;ae<xIter.size();ae++){
							xBest.add(xIter.get(ae));
							xCtakerBest.add(xCtakerIter.get(ae));
							xDayBest.add(xDayIter.get(ae));
						}
						
						for(int af=0;af<yIter.size();af++){
							yBest.add(yIter.get(af));
						}

						fBest=fIter;
					}
					
					else{
						System.out.println("Check2: "+idxDestroy+" "+wsDestroy.get(idxDestroy)+" "+wsOp2);
						
						//add score and increment counting for destroy operator
						wsDestroy.set(idxDestroy, wsDestroy.get(idxDestroy)+wsOp2);
						countDestroy.set(idxDestroy, countDestroy.get(idxDestroy)+1);
						dDestroy.set(idxDestroy, dDestroy.get(idxDestroy)+tempdDestroy);
						
						//add score and increment counting for repair operator
						wsRepair.set(idxRepair, wsRepair.get(idxRepair)+wsOp2);
						countRepair.set(idxRepair, countRepair.get(idxRepair)+1);
						dRepair.set(idxRepair, dRepair.get(idxRepair)+tempdRepair);
					}
				}
				
				else if(fIter>fNow){
					//if solution from destroy-repair operator is worse than current solution, but accepted by SA function
					double p3=rand.nextDouble();
					//if(perc<90) perc=perc*1.10;
					
					if(p3<Math.exp((-1*(fIter-fNow))/deg)){
						System.out.println("Check3: "+p3+" "+Math.exp((-1*(fIter-fNow))/deg));
						System.out.println("Check3: "+idxDestroy+" "+wsDestroy.get(idxDestroy)+" "+wsOp3);
						
						//add score and increment counting for destroy operator
						wsDestroy.set(idxDestroy, wsDestroy.get(idxDestroy)+wsOp3);
						countDestroy.set(idxDestroy, countDestroy.get(idxDestroy)+1);
						dDestroy.set(idxDestroy, dDestroy.get(idxDestroy)+tempdDestroy);
						
						//add score and increment counting for repair operator
						wsRepair.set(idxRepair, wsRepair.get(idxRepair)+wsOp3);
						countRepair.set(idxRepair, countRepair.get(idxRepair)+1);
						dRepair.set(idxRepair, dRepair.get(idxRepair)+tempdRepair);
						
						
						//and take them as current solution
						xNow.clear();
						xCtakerNow.clear();
						xDayNow.clear();
						yNow.clear();
						for(int ag=0;ag<xIter.size();ag++){
							xNow.add(xIter.get(ag));
							xCtakerNow.add(xCtakerIter.get(ag));
							xDayNow.add(xDayIter.get(ag));
						}
						
						for(int ah=0;ah<yIter.size();ah++){
							yNow.add(yIter.get(ah));
						}
						fNow=fIter;
					}
					
					//if solution from destroy-repair operator is worse than current solution, but rejected by SA function
					else{
						System.out.println("Check4: "+idxDestroy+" "+wsDestroy.get(idxDestroy)+" "+wsOp4);
						
						//add score and increment counting for destroy operator
						wsDestroy.set(idxDestroy, wsDestroy.get(idxDestroy)+wsOp4);
						countDestroy.set(idxDestroy, countDestroy.get(idxDestroy)+1);
						dDestroy.set(idxDestroy, dDestroy.get(idxDestroy)+tempdDestroy);
						
						//add score and increment counting for repair operator
						wsRepair.set(idxRepair, wsRepair.get(idxRepair)+wsOp4);
						countRepair.set(idxRepair, countRepair.get(idxRepair)+1);
						dRepair.set(idxRepair, dRepair.get(idxRepair)+tempdRepair);	
					}
				}
				
				/*check point
				System.out.println();
				System.out.println("Main6");
				System.out.println("UPDATE SOLUTION");
				System.out.println("xBest "+xBest);
				System.out.println("xCtakerBest "+xCtakerBest);
				System.out.println("xDayBest "+xDayBest);
				System.out.println("yBest "+yBest);
				System.out.println("fBest: "+fBest);
				
				System.out.println();
				System.out.println("xNow "+xNow);
				System.out.println("xCtakerNow "+xCtakerNow);
				System.out.println("xDayNow "+xDayNow);
				System.out.println("yNow "+yNow);
				System.out.println("fNow: "+fNow);
				
				System.out.println();
				System.out.println("xIter "+xIter);
				System.out.println("xCtakerIter "+xCtakerIter);
				System.out.println("xDayIter "+xDayIter);
				System.out.println("yIter "+yIter);
				System.out.println("fIter: "+fIter);
				*/
				
				/*
				System.out.println("check");
				System.out.println("evdistiter "+evdistiter);
				System.out.println("evttraveliter "+evttraveliter);
				System.out.println("evcontiter "+evcontiter);
				System.out.println("evuncoviter "+evuncoviter);
				System.out.println("evpayiter "+evpayiter);
				System.out.println("evbreakiter "+evbreakiter);
				*/
					
				/*check point
				System.out.println();
				System.out.println("Main7");
				System.out.println("UPDATE SCORE");
				System.out.println("wsDestroy: "+wsDestroy);
				System.out.println("dDestroy: "+dDestroy);
				System.out.println("countDestroy: "+countDestroy);
				System.out.println("wDestroy: "+wDestroy);
				System.out.println("wsRepair: "+wsRepair);
				System.out.println("dRepair: "+dRepair);
				System.out.println("countRepair: "+countRepair);
				System.out.println("wRepair: "+wRepair);
				*/
				
				tEnd = System.nanoTime();
				System.out.println("tEnd "+tEnd);
				tRes += (tEnd - tStart)/Math.pow(10,6);
				countAll+=1;
				
				sb.append(tEnd/Math.pow(10,6));
				sb.append(',');
				sb.append(tRes);
				sb.append(',');
				sb.append(countAll);
				sb.append(',');
				
				System.out.println("tRes(in ms) "+tRes);
				System.out.println("tCycle(in ms) "+tRes/countAll);
				
				
				//after a segment passes, need to update weight
				double sumNowDestroy = 0.0;
				double sumNowRepair = 0.0;	
				ArrayList<Double> dSumDestroy = new ArrayList<Double>();	//will contain the normalized log-transformed duration
				ArrayList<Double> dSumRepair = new ArrayList<Double>();		//will contain the normalized log-transformed duration
				
				if(i%seg==0&&i>0){
					double gamma=0.5;
					double beta= 0.25;
					
					//normalization of duration
					for(int da=0;da<wiDestroy.size();da++){
						
						if(dDestroy.get(da)/countDestroy.get(da)<=10&&countDestroy.get(da)!=0.0){	//if takes less than 10 ms, will obtain full weight of 1
							dSumDestroy.add(1.0);
						}
						else if(dDestroy.get(da)/countDestroy.get(da)>10&&countDestroy.get(da)!=0.0){//if more than 10 ms, will obtain weight of 1/log(time)
							dSumDestroy.add(1/(Math.log10(dDestroy.get(da)/countDestroy.get(da))));
						}
						else if(countDestroy.get(da)==0.0){
							dSumDestroy.add(0.0);
						}
	
					}
					
					for(int db=0;db<wiRepair.size();db++){
						if(dRepair.get(db)/countRepair.get(db)<=10&&countRepair.get(db)!=0.0){
							dSumRepair.add(1.0);
						}
						else if(dRepair.get(db)/countRepair.get(db)>10&&countRepair.get(db)!=0){
							dSumRepair.add(1/(Math.log10(dRepair.get(db)/countRepair.get(db))));
						}
						else if(countRepair.get(db)==0.0){
							dSumRepair.add(0.0);
						}
					}
					
					for(int c=0;c<wiDestroy.size();c++){
						double a=wDestroy.get(c);
						double b=wsDestroy.get(c);
						int count=countDestroy.get(c);
						double dur=dSumDestroy.get(c);
						if(count>0){
							wDestroy.set(c,(gamma*a)+(beta*(b/count))+((1-gamma-beta)*dur));	
						}
						
						else if(count==0){
							wDestroy.set(c,(gamma*a)+(beta*(b))+((1-gamma-beta)*dur));
						}
					}
					
					for(int d=0;d<wiRepair.size();d++){
						double a=wRepair.get(d);
						double b=wsRepair.get(d);
						int count=countRepair.get(d);
						double dur=dSumRepair.get(d);
						if(count>0){
							wRepair.set(d,(gamma*a)+(beta*(b/count))+((1-gamma-beta)*dur));	
						}
						else if(count==0){
							wRepair.set(d,(gamma*a)+(beta*(b))+((1-gamma-beta)*dur));
						}
					}
					
					//normalization of weight
					for(int e=0;e<wiDestroy.size();e++){
						sumNowDestroy+=wDestroy.get(e);
					}
					
					for(int f=0;f<wiRepair.size();f++){
						sumNowRepair+=wRepair.get(f);
					}
					
					//cumulative probability for roulette wheel mechanism
					for(int g=0;g<wDestroy.size();g++){
						if(g==0){
							wCumDestroy.set(g,wDestroy.get(g)/sumNowDestroy);
						}
						else{
							wCumDestroy.set(g,((wCumDestroy.get(g-1))+(wDestroy.get(g)/sumNowDestroy)));
						}
					}
					
					for(int h=0;h<wRepair.size();h++){
						if(h==0){
							wCumRepair.set(h,wRepair.get(h)/sumNowRepair);
						}
						else{
							wCumRepair.set(h,((wCumRepair.get(h-1))+(wRepair.get(h)/sumNowRepair)));
						}
					}
					
					//reset scores
					wsDestroy.clear();
					wsRepair.clear();
					countDestroy.clear();
					countRepair.clear();
					dDestroy.clear();
					dRepair.clear();
					
					//System.out.println(wiDestroy);
					
					for(int y=0;y<wiDestroy.size();y++){
						wsDestroy.add(0.0);	
						countDestroy.add(0);
						dDestroy.add(0.0);
					}
					
					for(int z=0;z<wiDestroy.size();z++){
						wsRepair.add(0.0);	
						countRepair.add(0);
						dRepair.add(0.0);
					}
				}
				
				if(i>=0){
					String xIterArr = xIter.toString();
					String xCtakerIterArr = xCtakerIter.toString();
					String xDayIterArr = xDayIter.toString();
					String xBestArr = xBest.toString();
					String xCtakerBestArr = xCtakerBest.toString();
					String xDayBestArr = xDayBest.toString();
					String xNowArr = xNow.toString();
					String xCtakerNowArr = xCtakerNow.toString();
					String xDayNowArr = xDayNow.toString();
					
					sb.append(deg);
					sb.append(',');
					sb.append(i);
					sb.append(',');
					sb.append(idxDestroy);
					sb.append(',');
					sb.append(tempdDestroy);
					sb.append(',');
					sb.append(idxRepair);
					sb.append(',');
					sb.append(tempdRepair);
					sb.append(',');
					sb.append(wDestroy.get(0));
					sb.append(',');
					sb.append(wDestroy.get(1));
					sb.append(',');
					sb.append(wDestroy.get(2));
					sb.append(',');
					sb.append(wDestroy.get(3));
					sb.append(',');
					sb.append(wRepair.get(0));
					sb.append(',');
					sb.append(wRepair.get(1));
					sb.append(',');
					sb.append(wRepair.get(2));
					sb.append(',');
					sb.append(fIter);
					sb.append(',');
					sb.append(evdistiter);
					sb.append(',');
					sb.append(evttraveliter);
					sb.append(',');
					sb.append(evcontiter);
					sb.append(',');
					sb.append(evuncoviter);
					sb.append(',');
					sb.append(evpayiter);
					sb.append(',');
					sb.append(evbreakiter);
					sb.append(',');
					sb.append(fNow);
					sb.append(',');
					sb.append(fBest);
					sb.append(',');
					sb.append("--");
					sb.append(',');
					sb.append(soldwiter);
					sb.append(',');
					sb.append("--");
					sb.append(',');
					sb.append(tsiter);
					sb.append(',');
					sb.append("--");
					sb.append(',');
					sb.append(teiter);
					sb.append(',');
					sb.append("--");
					sb.append(',');
					sb.append(tbriter);
					sb.append(',');
					sb.append("--");
					sb.append(',');
					sb.append(xIterArr.substring(1,xIterArr.length()-1));
					sb.append(',');
					sb.append(xCtakerIterArr.substring(1,xCtakerIterArr.length()-1));
					sb.append(',');
					sb.append(xDayIterArr.substring(1,xDayIterArr.length()-1));
					sb.append(',');
					
					sb.append(xNowArr.substring(1,xNowArr.length()-1));
					sb.append(',');
					sb.append(xCtakerNowArr.substring(1,xCtakerNowArr.length()-1));
					sb.append(',');
					sb.append(xDayNowArr.substring(1,xDayNowArr.length()-1));
					sb.append(',');
					
					sb.append(xBestArr.substring(1,xBestArr.length()-1));
					sb.append(',');
					sb.append(xCtakerBestArr.substring(1,xCtakerBestArr.length()-1));
					sb.append(',');
					sb.append(xDayBestArr.substring(1,xDayBestArr.length()-1));					
					sb.append('\n');
					
				}
				
			}
			
			deg=deg*alpha;
			if(tRes>limTime) break;
		}
		
		sb.append("tRes,");
		sb.append(tRes);
		sb.append('\n');
		sb.append("countAll,");
		sb.append(countAll);
		sb.append('\n');
		pw.write(sb.toString());
		pw.close();
		
	}
}
