import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Supports {
	
	private ArrayList<Integer> solBank;		//request bank
	
	//index by solVec, solDay, solCtaker
	private ArrayList<Integer> solVec;		//solution representation (10 ==> depot)
	private ArrayList<Integer> solDay;		//day index for solution representation
	private ArrayList<Integer> solCtaker;	//ctaker index for solution representation
	
	public ArrayList<Integer> solSBreak;	//binary for break in a route (1 ==>  the visit will be the break node for that route)
	public ArrayList<Double> solDBreak;		//break duration for the break node
	public ArrayList<Double> solDWorkBreak;	//total work duration plus break duration
	public ArrayList<Double> solDWait;		//total waiting duration (if there is a gap between end time from the previous node to start time of the next 
	
	//index by solIdxDay+solIdxCtaker
	private ArrayList<Integer> solIdxDay;
	private ArrayList<Integer> solIdxCtaker;
	private ArrayList<Double> solTStart;   	//start time for each ctaker per day in a week	
	private ArrayList<Double> solTEnd;	   	//end time for each ctaker per day	in a week
	private ArrayList<Double> solTBreak;   	//break time for each ctaker per day in a week
	private ArrayList<Double> solBreakGap;
	
	//index by solDay+solCtaker
	private ArrayList<Double> solTVisit; 	//visit for the solution representation
	private ArrayList<Double> solDist;		//distance for the solution representation
	private ArrayList<Double> solTTravel;	//travel time for the solution representation
	private ArrayList<Double> solPay;		//ctaker wage for the solution representation
	private ArrayList<Double> solDWork;		//work duration for the solution representation

	//for continuity of care, which caretaker for which visit (visit, ctaker) 
	private ArrayList<Integer> solVisit;	//list of different visits in the solution
	private ArrayList<Integer> solVisitCtaker;
	private ArrayList<Integer> solSumVisit;
	private ArrayList<Integer> solSumVisitCtaker; //to handle number of ctaker limitation (index by solSumVisit)		 
	
	public Supports(){
		solVec = new ArrayList<Integer>(); 
		solDay = new ArrayList<Integer>(); 
		solCtaker = new ArrayList<Integer>(); 
		solBank = new ArrayList<Integer>();

		solVisit = new ArrayList<Integer>();
		solVisitCtaker = new ArrayList<Integer>();
		solIdxDay = new ArrayList<Integer>();
		solIdxCtaker  = new ArrayList<Integer>();
		solTStart = new ArrayList<Double>();
		solTEnd = new ArrayList<Double>();
		solTBreak = new ArrayList<Double>();
		solTVisit = new ArrayList<Double>();
		solDist = new ArrayList<Double>();
		solTTravel = new ArrayList<Double>();
		solPay = new ArrayList<Double>();
		solBreakGap =new ArrayList<Double>();
		solDWork = new ArrayList<Double>();
		
		solSBreak = new ArrayList<Integer>();
		solDBreak = new ArrayList<Double>();
		solDWorkBreak = new ArrayList<Double>();
		solDWait = new ArrayList<Double>();
		solSumVisit= new ArrayList<Integer>();
		solSumVisitCtaker= new ArrayList<Integer>();
	}
	
	//initial solution construction ctaker id, visit id, day visit, day requirement, skill requirement,   feasible visit, feasible visit day, feasible visit ctaker day, number of days 
	public void genInitSol(ArrayList<Integer> cid,ArrayList<Double> wg,ArrayList<Integer> vid,ArrayList<Integer> pr,ArrayList<ArrayList<Double>> vdsrl,ArrayList<ArrayList<Integer>> dvs,ArrayList<ArrayList<Integer>> drq,ArrayList<Integer> srq,ArrayList<ArrayList<Integer>> crq,ArrayList<ArrayList<Integer>> sortptl,ArrayList<ArrayList<Integer>> seqptl,ArrayList<Double> seqdistl,ArrayList<Double> seqttl,ArrayList<Double> twl1,ArrayList<Double> twl2,ArrayList<Double> dbl,ArrayList<Integer> feav,ArrayList<ArrayList<Integer>> feavd,ArrayList<ArrayList<Integer>> feavcd,ArrayList<Double> wo, double he, double hbr,int nd,int nc,double r){
	
		clearsolCont();
		clearsolRoute();
		clearsolTime();

		ArrayList<Integer> sol = new ArrayList<Integer>();
		ArrayList<Integer> solc = new ArrayList<Integer>();
		ArrayList<Integer> sold = new ArrayList<Integer>();
		ArrayList<Integer> solb = new ArrayList<Integer>();
		ArrayList<Integer> temprank= new ArrayList<Integer>();
		
		
		//prepare list of visits
		ArrayList<ArrayList<Integer>> tempvl = new ArrayList<ArrayList<Integer>>();  
		for(int i=0;i<feavd.size();i++){
			if(feavd.get(i).get(0)!=10){
				tempvl.add(feavd.get(i));
				if(!temprank.contains(feavd.get(i).get(0))){
					temprank.add(feavd.get(i).get(0));
				}
			}
		}
		
		//generate initial depots
		for(int j=0;j<cid.size();j++){
			for(int k=0;k<nd;k++){
				sol.add(10);
				solc.add(cid.get(j));
				sold.add(k+1);
			}
		}
		
		//check skill requirement for each client and the number of requested visit
		ArrayList<Integer> tempsumd = new ArrayList<Integer>();
		ArrayList<Integer> tempsrq = new ArrayList<Integer>();
		
		for(int l=0;l<vid.size();l++){
			if(vid.get(l)!=10){
				int d=0;
				for(int m=0;m<nd;m++){
					d+=drq.get(l).get(m);
				}
				tempsrq.add(srq.get(l));
				tempsumd.add(d);
			}
			else if(vid.get(l)==10) 
				continue;
		}
		
		//sort clients based on the skill requirement and numbers of visits required
		for(int n=0;n<temprank.size();n++){
			for(int o=n+1;o<temprank.size();o++){
				if(tempsrq.get(o)>tempsrq.get(n)){
					Collections.swap(temprank,n,o);
					Collections.swap(tempsumd,n,o);
					Collections.swap(tempsrq,n,o);
				}
				else if(tempsrq.get(o)==tempsrq.get(n)){
					if(tempsumd.get(o)>tempsumd.get(n)){
						Collections.swap(temprank,n,o);	
						Collections.swap(tempsumd,n,o);
						Collections.swap(tempsrq,n,o);
					}
				}
			}
		}
		
		//sort visits required based on client-based rank
		ArrayList<ArrayList<Integer>> tempmain = new ArrayList<ArrayList<Integer>>();
		for(int p=0;p<temprank.size();p++){
			for(int q=0;q<tempvl.size();q++){
				if(tempvl.get(q).get(0)==temprank.get(p)){
					tempmain.add(tempvl.get(q));
				}
			}
		}
		
		//also get information of visits required in a particular day
		ArrayList<ArrayList<Integer>> tempvisit=new ArrayList<ArrayList<Integer>>();
		tempvisit.addAll(dvs);
		
		
		/*check point
		System.out.println();
		System.out.println("Initial random1");
		System.out.println("tempvl "+tempvl);
		System.out.println("temprank "+temprank);
		System.out.println("tempsumd "+tempsumd);
		System.out.println("tempsrq "+tempsrq);
		System.out.println("tempmain "+tempmain); 	//visit,day
		System.out.println("tempvisit "+tempvisit); //list of visits for each day in a week (will lead to route)
		System.out.println("crq "+crq);				//which ctaker is allowed for which visit
		*/
		
		while(tempmain.size()>0){
			Random rand=new Random();
			
			//can modify by using similar mechanism in destroy distance
			double x1 = rand.nextDouble();
			int x2 = (int)Math.round(Math.pow(x1,r)*(tempmain.size()-1));	//pointer for tempmain
			int v = tempmain.get(x2).get(0); //visit
			int d = tempmain.get(x2).get(1); //day
			
			/*check point
			System.out.println();
			System.out.println("Initial random2");
			System.out.println("v "+v);
			System.out.println("d "+d);
			System.out.println("x2 "+x2);
			*/
			
			ArrayList<Integer> templist = dvs.get(d-1); 		//list of all visit in particular day
			ArrayList<Integer> tempc = crq.get(vid.indexOf(v));	//possible ctaker to visit v
			
			//choose ctaker for prioritized visit --> choose to start from path with least number of ctaker capable doing it 
			ArrayList<Integer> tempcfreqidx= new ArrayList<Integer>();
			ArrayList<Integer> tempcfreq= new ArrayList<Integer>();
			ArrayList<Integer> tempcfin= new ArrayList<Integer>();
			for(int ad=0;ad<tempc.size();ad++){
				for(int ae=0;ae<sortptl.size();ae++){
					int p1=sortptl.get(ae).get(0);
					int p2=sortptl.get(ae).get(1);
					int p3=sortptl.get(ae).get(2);
					
					if((p1==tempc.get(ad)&&templist.contains(p2)&&templist.contains(p3))||
					(p1==tempc.get(ad)&&p2==10&&templist.contains(p3))||
					(p1==tempc.get(ad)&&templist.contains(p2)&&p3==10)){
						tempcfreqidx.add(tempc.get(ad));
						tempcfreq.add(ae);
					}
				}
			}	
			
			//get ctaker ID for which the distance path (that contains one of the future visited nodes) is the shortest
			for(int af=0;af<tempcfreqidx.size();af++){
				if(tempcfreq.get(af)==Collections.min(tempcfreq)){
					tempcfin.add(tempcfreqidx.get(af));
				}
			}
			
			/*check point
			System.out.println();
			System.out.println("Initial random3");
			System.out.println("crq "+crq);
			System.out.println("templist "+templist);
			System.out.println("tempc "+tempc); 
			System.out.println("tempcfreqidx "+tempcfreqidx);
			System.out.println("tempcfreq "+tempcfreq);
			System.out.println("tempcfin "+tempcfin);
			*/
			
			int c= 0;	//define which ctaker to visit
			
			if(tempcfin.size()>1){
				double x3 = rand.nextDouble();
				int x4 = (int)Math.round(Math.pow(x3,r)*(tempcfin.size()-1));
				c = tempcfin.get(x4);		
			}
			else if(tempcfin.size()==1){
				c = tempcfin.get(0);
			}
			
			//fix visit list, if a visit does not have similar ctaker possibility
			int z=0;
			int size3=templist.size();
			for(int s=0;s<size3;s++){				
				ArrayList<Integer> temp1 =crq.get(vid.indexOf(templist.get(z)));
				
				if(!temp1.contains(c)){
					
					//if does not match, remove from templist
					templist.remove(templist.indexOf(templist.get(z)));
				}
				else if(temp1.contains(c)||temp1.equals(c)){
					
					//if matches, remove from tempmain and tempvisit (so we won't visit it again)
					//for tempvisit: only remove those that still exist  in templist (remember that we have removed visit that doesn't have any similar ctaker with chosen visit c)
					ArrayList<Integer> temp2= new ArrayList<Integer>();
					temp2.add(templist.get(z));
					temp2.add(d);
					tempmain.remove(tempmain.indexOf(temp2));
					
					ArrayList<Integer> temp3=new ArrayList<Integer>();
					for(int w=0;w<tempvisit.get(d-1).size();w++){
						temp3.add(tempvisit.get(d-1).get(w));
					}
					temp3.remove(temp3.indexOf(templist.get(z)));
					tempvisit.set(d-1, temp3);
					z=z+1;
				}
			}
			
			/*check point (after removing irrelevant visits in respective list)
			System.out.println();
			System.out.println("Initial random4");
			System.out.println("c "+c); 
			System.out.println("tempmain "+tempmain); 
			System.out.println("tempvisit "+tempvisit); 
			System.out.println("templist "+templist);
			*/
			
			//shuffle list of visit to allocate
			Collections.shuffle(templist);
			
			ArrayList<ArrayList<Integer>> templistv = new ArrayList<ArrayList<Integer>>();	//prepare array as input for next repair random-based mechanism
			for(int aa=0;aa<templist.size();aa++){
				ArrayList<Integer> temp4 = new ArrayList<Integer>();
				temp4.add(templist.get(aa));
				temp4.add(c);
				temp4.add(d);
				templistv.add(temp4);
			}
			
			//System.out.println("templistv "+templistv);
			
			int size5=templistv.size();
			for(int ab=0;ab<size5;ab++){
				ArrayList<Integer> idxv = new ArrayList<Integer>();		//list of possible index to insert
				ArrayList<Integer> temp5 =locateVisits(cid,sol,solc,sold,templistv.get(0),nd);
				
				//if there is only one possible index to insert (a,a) 
				if(temp5.get(0)==temp5.get(1)){
					idxv.add(temp5.get(0));
				}
				
				//if there are more possibilities to insert (a,b)
				else{
					for(int ac=temp5.get(0);ac<=temp5.get(1);ac++){
						idxv.add(ac);
					}
				}
				
				//check path feasibility
				ArrayList<Integer> tempstat = new ArrayList<Integer>();		//status of feasibility for every possible index
				for(int ad=0;ad<idxv.size();ad++){
					//prepare for indexing for possible visit
					ArrayList<Integer> temp6 = new ArrayList<Integer>();
					temp6.add(c);
					temp6.add(d);
					
					//and for next visit that will be affected if it happens (check the solution)
					ArrayList<Integer> temp7 = new ArrayList<Integer>();
					if(idxv.get(ad)<solc.size()){
						temp7.add(solc.get(idxv.get(ad)));		
						temp7.add(sold.get(idxv.get(ad)));	
					}
					
					//get path to possible previous visit,to be matched with list of sequenced path
					ArrayList<Integer> temp8 = new ArrayList<Integer>();
					temp8.add(c);
					temp8.add(sol.get((idxv.get(ad))-1));
					temp8.add(templistv.get(0).get(0));
					
					//get path to possible previous visit -- see temp6&temp7, there is possibility that this visit is the last position in the route
					ArrayList<Integer> temp9 = new ArrayList<Integer>();
					temp9.add(c);
					temp9.add(templistv.get(0).get(0));
					
					if(!temp6.equals(temp7)||idxv.get(ad)>=solc.size()){
						temp9.add(10);	//the visit is indeed the last position in the route, should return to depot
					}
					else if(temp6.equals(temp7)){
						temp9.add(sol.get(idxv.get(ad)));
					}
					
					if(seqptl.contains(temp8)&&seqptl.contains(temp9)){
						tempstat.add(1);
					}
					else tempstat.add(0);
					
					/*check point
					System.out.println();
					System.out.println("Initial random5");
					System.out.println("temp6 "+temp6);
					System.out.println("temp7 "+temp7);
					System.out.println("temp8 "+temp8);
					System.out.println("temp9 "+temp9);
					*/
				}
				
				//insert in its best position
				/*check point
				System.out.println();
				System.out.println("Initial random6");
				System.out.println("idxv "+idxv);
				System.out.println("tempstat "+tempstat);
				*/
				
				//evaluate if the path is feasible, get the minimum value
				ArrayList<Double> tempev = new ArrayList<Double>();
				
				int sumstat=0;
				for(int aa=0;aa<tempstat.size();aa++){
					sumstat+=tempstat.get(aa);
				}
				
				if(sumstat>0){
					for(int ae=0;ae<tempstat.size();ae++){
						if(tempstat.get(ae)==1){
							ArrayList<Integer> tempsol = new ArrayList<Integer>();
							ArrayList<Integer> tempsolc = new ArrayList<Integer>();
							ArrayList<Integer> tempsold = new ArrayList<Integer>();
							ArrayList<Integer> tempsolb = new ArrayList<Integer>();
							for(int af=0;af<sol.size();af++){
								tempsol.add(sol.get(af));
								tempsolc.add(solc.get(af));
								tempsold.add(sold.get(af));
							}
							
							for(int ag=0;ag<solb.size();ag++){
								tempsolb.add(solb.get(ag));	
							}
							
							tempsol.add(idxv.get(ae),templistv.get(0).get(0));
							tempsolc.add(idxv.get(ae),c);
							tempsold.add(idxv.get(ae),d);
							if(tempsolb.size()>0&&tempsolb.indexOf(templistv.get(0).get(0))!=-1) tempsolb.remove(tempsolb.indexOf(templistv.get(0).get(0))); 
						
							/*check point
							System.out.println();
							System.out.println("Initial random7");
							System.out.println("tempsolsize "+tempsol.size());
							System.out.println("tempsol "+tempsol);
							System.out.println("tempsolc "+tempsolc);
							System.out.println("tempsold "+tempsold);
							System.out.println("tempsolb "+tempsolb);
							System.out.println("seqptl"+seqptl);
							System.out.println("seqttl"+seqttl);
							System.out.println("vdsrl"+vdsrl);
							*/
													
							gensolTimeAll(tempsol,tempsolc,tempsold,tempsolb,cid,vid,seqttl,seqptl,vdsrl, nd);
							ArrayList<Double> soldw = getsolDWork();
							gensolTimeSign(tempsol,tempsolc,tempsold,tempsolb,cid,vid,soldw,twl1,twl2, dbl,hbr, nd);
							ArrayList<Double> ts = getsolTStart();
							ArrayList<Double> te = getsolTEnd();
							ArrayList<Double> tbr = getsolTBreak();
							
							//adding clients that will cause overlap time visit or overtime will trigger penalty of 500000.0
							/*check point
							System.out.println("soldw "+soldw);
							System.out.println("ts "+ts);
							System.out.println("te "+te);
							System.out.println("tbr "+tbr);
							*/
							
							double evdist = evalsolDist(tempsol,tempsolc,tempsold,tempsolb,seqptl,seqdistl);
							double evttravel = evalsolTTravel(tempsol,tempsolc,tempsold,tempsolb,seqptl,seqttl);
							double evcont = (double) evalsolCont(tempsol,tempsolc,tempsold,tempsolb,nc);
							double evuncov = evalsolUncov(vid,tempsol,tempsolc,tempsold,tempsolb,pr);
							double evpay = evalsolPay(cid,wg,tempsol,tempsolc,tempsold,tempsolb,ts,te,he,nd);
							ArrayList<Double> pay = getsolPay();
							double evbreak = evalsolBreak(tempsol,tempsolc,tempsold,tempsolb,ts,tbr,hbr);
							double evall = wo.get(0)*evdist+wo.get(1)*evttravel+wo.get(2)*evcont+wo.get(3)*evuncov+wo.get(4)*evpay+wo.get(5)*evbreak;
							
							//check time feasibility, after taking break time into account
							//also check whether number of ctakers limitation are fulfilled
							if(!ts.contains(500000.0)&&!te.contains(500000.0)&&evcont!=500000.0&&!pay.contains(100000.0)){
								tempev.add(evall);
								
							}
							
							else if(ts.contains(500000.0)||te.contains(500000.0)||evcont==500000.0||pay.contains(100000.0)){
								tempev.add(100000.0);
							}
								 
							tempsol.clear();
							tempsolc.clear();
							tempsold.clear();
							tempsolb.clear();
							
						}
						else tempev.add(100000.0); 
					}
					//System.out.println("tempev "+tempev);
					
					ArrayList<Integer> tempidxv= new ArrayList<Integer>();
					for(int ah=0;ah<tempev.size();ah++){
						if(tempev.get(ah)<100000.0){
							tempidxv.add(idxv.get(ah));
						}
					}
					
					int x5=100000; 
					if(tempidxv.size()>0){
						if(tempidxv.size()>1){
							x5 = tempidxv.get(rand.nextInt(tempidxv.size()));
						}
						else if(tempidxv.size()==1){
							x5 = tempidxv.get(0);
						}
						
						/*check point
						System.out.println();
						System.out.println("Initial random8");
						System.out.println("x5 "+x5);
						System.out.println("tempidxv "+tempidxv);
						System.out.println("templistv "+templistv.get(0));
						*/
						
						//insert the chosen index to solution and remove it from request bank
						sol.add(x5,templistv.get(0).get(0)); 
						solc.add(x5,c);
						sold.add(x5,d);
						templistv.remove(0);
					}
					
					else if(tempidxv.size()==0){
						solb.add(templistv.get(0).get(0));
						templistv.remove(0);
					}
					
				}
				
				else if(sumstat==0){
					//if there is no feasible index, still 'remove' it from request bank to temporary array
					//they will be removed back to request bank once repair randoom done
					solb.add(templistv.get(0).get(0));
					templistv.remove(0);
				}
				
			}
			
			/*check point
			System.out.println();
			System.out.println("Initial random9");
			System.out.println("tempmain "+tempmain);
			System.out.println("tempvisit "+tempvisit);
			System.out.println("templist "+templist);
			System.out.println("sol "+sol);
			System.out.println("solc "+solc);
			System.out.println("sold "+sold);
			System.out.println("solb "+solb);
			*/
		}
		
		clearsolVec();
		clearsolBank();
		
		for(int y=0;y<sol.size();y++){
			solVec.add(sol.get(y));
			solCtaker.add(solc.get(y));
			solDay.add(sold.get(y));
		}
		
		for(int zb=0;zb<solb.size();zb++){
			solBank.add(solb.get(zb));
		}
		
		
		/*optimal solution from Julia (wage ctaker2 = 10, obj = 112/156)
		Collections.addAll(solVec,10,10,10,10,10,10,20,10,20,30,40,10,20,50,10,20,30,40,50,10,20,10,10,10,10,10);
		Collections.addAll(solCtaker,11,11,11,11,11,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,31,31,31,31,31);
		Collections.addAll(solDay,1,2,3,4,5,1,1,2,2,2,2,3,3,3,4,4,4,4,4,5,5,1,2,3,4,5);
		*/
		
		/*
		//optimal solution from Julia (wage for ctaker 2 = 7)
		Collections.addAll(solVec,10,10,10,10,10,10,20,10,20,30,40,10,20,50,10,20,30,40,50,10,20,10,10,10,10,10);
		Collections.addAll(solCtaker,11,11,11,11,11,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,31,31,31,31,31);
		Collections.addAll(solDay,1,2,3,4,5,1,1,2,2,2,2,3,3,3,4,4,4,4,4,5,5,1,2,3,4,5);
		*/
		
		/*compare with Java (wage ctaker2 = 10, obj = 103/...)  
		Collections.addAll(solVec,10,20,10,	10,	10,	10,	10,	10,	30,	20,	10,	50,	10,	20,	30,	40,	50,	10,	10,	10,	40,	10,	20,	10,	10);
		Collections.addAll(solCtaker,11,11,	11,	11,	11,	11,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	31,	31,	31,	31,	31,	31,	31);
		Collections.addAll(solDay,1,1,2,3,	4,	5,	1,	2,	2,	2,	3,	3,	4,	4,	4,	4,	4,	5,	1,	2,	2,	3,	3,	4,	5);
		*/
		
		/*compare with Java (wage ctaker2 = 10, obj = 103/151)
		Collections.addAll(solVec,10,10,10,10,10,10,10,20,30,40,10,50,10,20,30,40,50,10,10,20,10,10,20,10,10,20);
		Collections.addAll(solCtaker,11,11,11,11,11,21,21,21,21,21,21,21,21,21,21,21,21,21,31,31,31,31,31,31,31,31);
		Collections.addAll(solDay,1,2,3,4,5,1,2,2,2,2,3,3,4,4,4,4,4,5,1,1,2,3,3,4,5,5);
		*/
		
		/*
		Collections.addAll(solVec,10,	10,	10,	10,	10,	10,	10,	10,	10,	10,	10,	240,	190,	160,	200,	140,	220,	10,	60,	110,	240,	180,	190,	200,	170,	220,	10,	250,	160,	180,	200,	130,	10,	250,	160,	200,	190,	220,	10,	240,	190,	180,	160,	200,	170,	220,	10,	210,	230,	60,	50,	40,	20,	80,	130,	100,	260,	10,	20,	80,	150,	100,	120,	10,	60,	110,	90,	40,	50,	20,	220,	100,	260,	70,	10,	60,	110,	40,	50,	20,	80,	10,	250,	110,	60,	40,	30,	80,	120);
		Collections.addAll(solCtaker,11,	11,	11,	11,	11,	21,	21,	21,	21,	21,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41);
		Collections.addAll(solDay,1,	2,	3,	4,	5,	1,	2,	3,	4,	5,	1,	1,	1,	1,	1,	1,	1,	2,	2,	2,	2,	2,	2,	2,	2,	2,	3,	3,	3,	3,	3,	3,	4,	4,	4,	4,	4,	4,	5,	5,	5,	5,	5,	5,	5,	5,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	2,	2,	2,	2,	2,	2,	3,	3,	3,	3,	3,	3,	3,	3,	3,	3,	3,	4,	4,	4,	4,	4,	4,	4,	5,	5,	5,	5,	5,	5,	5,	5);
		*/
		
		/*check 45vs_lim3
		Collections.addAll(solVec,10,	340,	220,	140,	10,	420,	430,	390,	10,	400,	340,	220,	10,	10,	420,	430,	10,	130,	440,	10,	410,	440,	170,	10,	250,	270,	130,	100,	10,	420,	400,	190,	220,	310,	390,	430,	10,	250,	340,	400,	190,	170,	220,	440,	10,	420,	240,	60,	50,	200,	320,	390,	290,	10,	240,	340,	400,	200,	320,	220,	20,	100,	10,	60,	110,	160,	90,	200,	290,	460,	440,	10,	330,	110,	50,	320,	360,	290,	460,	10,	60,	160,	110,	280,	200,	290,	390,	460,	10,	210,	230,	160,	190,	400,	350,	370,	260,	100,	20,	80,	10,	60,	110,	190,	180,	280,	370,	350,	150,	120,	10,	40,	180,	370,	280,	350,	450,	430,	70,	10,	60,	160,	250,	340,	370,	350,	280,	380,	450,	80,	10,	40,	240,	180,	370,	310,	350,	360,	300);
		Collections.addAll(solCtaker,11,	11,	11,	11,	11,	11,	11,	11,	11,	11,	11,	11,	11,	11,	11,	11,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	21,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	31,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41,	41);
		Collections.addAll(solDay,1,	1,	1,	1,	2,	2,	2,	2,	3,	3,	3,	3,	4,	5,	5,	5,	1,	1,	1,	2,	2,	2,	2,	3,	3,	3,	3,	3,	4,	4,	4,	4,	4,	4,	4,	4,	5,	5,	5,	5,	5,	5,	5,	5,	1,	1,	1,	1,	1,	1,	1,	1,	1,	2,	2,	2,	2,	2,	2,	2,	2,	2,	3,	3,	3,	3,	3,	3,	3,	3,	3,	4,	4,	4,	4,	4,	4,	4,	4,	5,	5,	5,	5,	5,	5,	5,	5,	5,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	2,	2,	2,	2,	2,	2,	2,	2,	2,	2,	3,	3,	3,	3,	3,	3,	3,	3,	3,	4,	4,	4,	4,	4,	4,	4,	4,	4,	4,	4,	5,	5,	5,	5,	5,	5,	5,	5,	5);
		*/
		
		//check point
		System.out.println();
		System.out.println("Initial random10");
		System.out.println("Initial solution");
		System.out.println("solVec "+solVec);
		System.out.println("solCtaker "+solCtaker);
		System.out.println("solDay "+solDay);
		System.out.println("solBank "+solBank);
		
	}
	
	//destroy operators
	//random destroy operator: sol, solc, sold, solb, number of visits to remove
	public void destroyRandom(ArrayList<Integer> sol,ArrayList<Integer> solc,ArrayList<Integer> sold,ArrayList<Integer> solb,int q){
		clearsolCont();
		clearsolRoute();
		clearsolTime();

		
		for(int i=0;i<q;i++){
			Random rand = new Random(); 
			int x = rand.nextInt(sol.size());		
			
			//remove the randomly chosen visit
			solb.add(sol.get(x));
			sol.remove(x);
			sold.remove(x);
			solc.remove(x);
			
			/*check point			
			System.out.println();
			System.out.println("Destroy random1");
			System.out.println("x "+x);
			System.out.println("sol "+sol);
			System.out.println("solc "+solc);
			System.out.println("sold "+sold);
			System.out.println("solb "+solb);
			*/
		}
		
		clearsolVec();
		clearsolBank();
		
		for(int y=0;y<sol.size();y++){
			solVec.add(sol.get(y));
			solCtaker.add(solc.get(y));
			solDay.add(sold.get(y));
		}
		
		for(int z=0;z<solb.size();z++){
			solBank.add(solb.get(z));
		}
		
		/*check point
		System.out.println();
		System.out.println("Destroy random2");
		System.out.println("RESULT");
		System.out.println("sol "+solVec);
		System.out.println("solCtaker "+solCtaker);
		System.out.println("solDay "+solDay);
		System.out.println("solBank "+solBank);
		System.out.println("solb "+solb);
		*/
	}
	
	//distance-related destroy operator: visit ID list, visit location list, path list, number of visits to remove, random parameter
	public void destroyDistance(ArrayList<Integer> sol,ArrayList<Integer> solc,ArrayList<Integer> sold,ArrayList<Integer> solb,ArrayList<Integer> vid,ArrayList<Point2D> locl,ArrayList<ArrayList<Integer>> ptl,int q,double r){
		clearsolCont();
		clearsolRoute();
		clearsolTime();
		
		int zl=100000;
		
		Random rand = new Random(); 
		double pzx=100000.0;
		double pzy=100000.0;
		
		//choose seed visit, either from current solution...
		if(solb.size()==0){
			int x1 = rand.nextInt(sol.size());
			zl=sol.get(x1);
			
			//get coordinate for the chosen node
			pzx=locl.get(vid.indexOf(zl)).getX(); 
			pzy=locl.get(vid.indexOf(zl)).getY();
			
			/*check point
			System.out.println();
			System.out.println("Destroy distance1");
			System.out.println("x1 "+x1);
			System.out.println("zl "+zl);
			System.out.println();
			*/
			
			solb.add(zl);
			sol.remove(x1);
			sold.remove(x1);
			solc.remove(x1);
		}
		
		//...or removed solution
		else if(solb.size()>0){
			int x2 = rand.nextInt(solb.size());
			zl=solb.get(x2);
			
			//get coordinate for the chosen node
			pzx=locl.get(vid.indexOf(zl)).getX();
			pzy=locl.get(vid.indexOf(zl)).getY();
			
			/*check point
			System.out.println();
			System.out.println("Destroy distance2");
			System.out.println("x2 "+x2);
			System.out.println("zl "+zl);
			System.out.println();
			*/
		}
		
		//remove another q randomly-chosen visit, with respect of its distance to seed visit
		//we compute distance of each visit in the solution representation toward seed visit
		for(int i=0;i<q;i++){
			ArrayList<Integer> tempsol = new ArrayList<Integer>();		//list of visits to compute
			ArrayList<Integer> tempsolidx = new ArrayList<Integer>();	//indexing for solution
			ArrayList<Double> tempsoldist = new ArrayList<Double>();	//distance between each visit toward seed visit
			
			//compute distance for all visits
			for(int j=0;j<sol.size();j++){
				double plx;
				double ply;
				double dist;
				
				tempsolidx.add(j);		//indexing for sorting, so we can retrieve the appropriate visit after sorting process
				
				plx = locl.get(vid.indexOf(sol.get(j))).getX();
				ply = locl.get(vid.indexOf(sol.get(j))).getY();
				tempsol.add(sol.get(j));
				
				ArrayList<Integer> temp1 = new ArrayList<Integer>();
				temp1.add(zl);
				temp1.add(sol.get(j));
				
				ArrayList<Integer> temp2 = new ArrayList<Integer>();
				temp2.add(sol.get(j));
				temp2.add(zl);
				
				if(ptl.contains(temp1)||ptl.contains(temp2)){
					dist=Point2D.distance(pzx, pzy, plx, ply);
					tempsoldist.add(dist);
				}
				else if(!ptl.contains(temp1)&&!ptl.contains(temp2)){
					tempsoldist.add(100000.0);
				}
			}
			
			/*check point
			System.out.println();
			System.out.println("Destroy distance3");
			System.out.println("BEFORE SORT");
			System.out.println("tempsol "+tempsol);
			System.out.println("tempsoldist "+tempsoldist);
			*/
			
			//sort computed visit in ascending order
			for(int k=0;k<tempsol.size();k++){
				for(int l=k+1;l<tempsol.size();l++){
					if(tempsoldist.get(l)<tempsoldist.get(k)){
						Collections.swap(tempsoldist,k,l);
						Collections.swap(tempsol,k,l);
						Collections.swap(tempsolidx,k,l);
					}	
				}
			}
			
			/*check point
			System.out.println();
			System.out.println("Destroy distance4");
			System.out.println("AFTER SORT");
			System.out.println("tempsol "+tempsol);
			System.out.println("tempsoldist "+tempsoldist);
			*/
			
			//choose a visit to remove -- randomly, but still taking the rank into account
			double x3=rand.nextDouble();
			int idxSort = (int)Math.round(Math.pow(x3,r)*(tempsol.size()-1));
			//int idxSol = tempsol.get(idxSort);
			int idx = tempsolidx.get(idxSort);
			
			//System.out.println(idx);
			solb.add(sol.get(idx));
			
			sol.remove(idx);
			solc.remove(idx);
			sold.remove(idx);
			
			/*check point
			System.out.println();
			System.out.println("Destroy distance5");
			System.out.println("idxSort "+idxSort+" idx "+idx);
			System.out.println("sol "+sol);
			System.out.println("solc "+solc);
			System.out.println("sold "+sold);
			System.out.println("solb "+solb);
			*/
		}
		
		clearsolVec();
		clearsolBank();
		
		for(int y=0;y<sol.size();y++){
			solVec.add(sol.get(y));
			solCtaker.add(solc.get(y));
			solDay.add(sold.get(y));
		}
		
		for(int z=0;z<solb.size();z++){
			solBank.add(solb.get(z));
		}
		
		/*check point
		System.out.println();
		System.out.println("Destroy distance6");
		System.out.println("RESULT");
		System.out.println("sol "+sol);
		System.out.println("solCtaker "+solCtaker);
		System.out.println("solDay "+solDay);
		
		System.out.println("solBank "+solBank);
		System.out.println("solb "+solb);
		*/
	}
	
	//continuity-of-care destroy operator: sol, solc, sold, solb, number of visits to remove (no need random parameter as there's no sorting process)
	public void destroyCont(ArrayList<Integer> sol,ArrayList<Integer> solc,ArrayList<Integer> sold,ArrayList<Integer> solb, int q){
		clearsolCont();
		clearsolRoute();
		clearsolTime();
		
		Random rand = new Random(); 
		
		//choose seed visit from current solution
		//not choosing from request bank -- as we do not know which ctaker allocated if the visit is in request bank
		int x1 = rand.nextInt(sol.size());
		int zl = sol.get(x1);
		int zc = solc.get(x1);
		solb.add(zl);
		sol.remove(x1);
		sold.remove(x1);
		solc.remove(x1);
		
		//prepare for indexing
		ArrayList<ArrayList<Integer>> tempidx=new ArrayList<ArrayList<Integer>>();
		for(int h=0;h<sol.size();h++){
			ArrayList<Integer> temp1= new ArrayList<Integer>();
			temp1.add(sol.get(h));
			temp1.add(solc.get(h));
			temp1.add(sold.get(h));
			tempidx.add(temp1);
		}
		
		//check solution with respect of its caretaker similarity
		ArrayList<Integer> tempcont = new ArrayList<Integer>();
		ArrayList<Integer> tempcontc = new ArrayList<Integer>();
		ArrayList<Integer> tempcontd = new ArrayList<Integer>();
		for(int i=0;i<sol.size();i++){
			if(solc.get(i)==zc){
				tempcont.add(sol.get(i));
				tempcontc.add(solc.get(i));
				tempcontd.add(sold.get(i));
			}
		}
		
		/*check point
		System.out.println();
		System.out.println("Destroy conntinuity1");
		System.out.println("x1 "+x1); 
		System.out.println("tempcont "+tempcont);
		System.out.println("tempcontc "+tempcontc);
		System.out.println("tempcontd "+tempcontd);
		*/
		
		//only remove those with similar ctaker
		//there is possibility that, for certain ctaker, number of visits handled are less than number of visits to remove -- so we need to take the minimum 
		int out = Math.min(tempcont.size(),q);
		//System.out.println("out "+out);
		//System.out.println("solb "+solb);
		//System.out.println("tempcont "+tempcont);
		
		for(int j=0;j<out;j++){
			int x2 = rand.nextInt(tempcont.size());	
			ArrayList<Integer>  temp2 = new ArrayList<Integer>();
			temp2.add(tempcont.get(x2));
			temp2.add(tempcontc.get(x2));
			temp2.add(tempcontd.get(x2));
			
			for(int k=0;k<sol.size();k++){
				if(tempidx.get(k).equals(temp2)){
					solb.add(sol.get(k));
					sol.remove(k);
					sold.remove(k);
					solc.remove(k);
				}
			}

			tempcont.remove(x2);
			tempcontc.remove(x2);
			tempcontd.remove(x2);
			
			/*check point
			System.out.println("Destroy continuity2");
			System.out.println("x2 "+x2);
			System.out.println("tempcont "+tempcont);
			System.out.println("tempcontc "+tempcontc);
			System.out.println("tempcontd "+tempcontd);
			System.out.println("sol "+sol);
			System.out.println("solc "+solc);
			System.out.println("sold "+sold);
			System.out.println("solb "+solb);
			*/
		}
		
		clearsolVec();
		clearsolBank();
		
		for(int y=0;y<sol.size();y++){
			solVec.add(sol.get(y));
			solCtaker.add(solc.get(y));
			solDay.add(sold.get(y));
		}
		
		for(int z=0;z<solb.size();z++){
			solBank.add(solb.get(z));
		}
		
		/*check point
		System.out.println();
		System.out.println("Destroy continuity3");
		System.out.println("RESULT");
		System.out.println("sol "+sol);
		System.out.println("solCtaker "+solCtaker);
		System.out.println("solDay "+solDay);
		
		System.out.println("solBank "+solBank);
		System.out.println("solb "+solb);
		*/
	}
	
	//worst-case destroy operator: ctaker ID, ctaker wage, visit ID, sol, solc, sold, solb, service time by ctaker skill and clients request, sequenced path, sequenced ctaker for the path, sequenced distance, sequenced travel time, time window1, time window2, break time duration, end of working time, minimum working hours before break, number of days, number of visit to remove, random parameter 
	public void destroyWorst(ArrayList<Integer> cid, ArrayList<Double> wg, ArrayList<Integer> vid,ArrayList<Integer> pr,ArrayList<Integer> sol,ArrayList<Integer> solc,ArrayList<Integer> sold,ArrayList<Integer> solb,ArrayList<ArrayList<Double>> vdsrl, ArrayList<ArrayList<Integer>> seqptl,ArrayList<Double> seqdistl, ArrayList<Double> seqttl, ArrayList<Double> twl1, ArrayList<Double> twl2, ArrayList<Double> dbl, ArrayList<Double> wo, double he,double hbr, int nd,int nc, int q,double r){
		clearsolCont();
		clearsolRoute();
		clearsolTime();
		
		for(int h=0;h<q;h++){
			Random rand = new Random();
			ArrayList<Double> tempevrm = new ArrayList<Double>();
			
			//compute cost for original solution
			gensolTimeAll(sol,solc,sold,solb,cid,vid,seqttl,seqptl,vdsrl, nd);
			ArrayList<Double> soldw = getsolDWork();
			gensolTimeSign(sol,solc,sold,solb,cid,vid,soldw,twl1,twl2, dbl,hbr, nd);
			ArrayList<Double> ts = getsolTStart();
			ArrayList<Double> te = getsolTEnd();
			ArrayList<Double> tbr = getsolTBreak();
			
			double evdist = evalsolDist(sol,solc,sold,solb,seqptl,seqdistl);
			double evttravel = evalsolTTravel(sol,solc,sold,solb,seqptl,seqttl);
			double evcont = (double) evalsolCont(sol,solc,sold,solb,nc);
			double evuncov = evalsolUncov(vid,sol,solc,sold,solb,pr);
			double evpay = evalsolPay(cid,wg,sol,solc,sold,solb,ts,te,he,nd);
			double evbreak = evalsolBreak(sol,solc,sold,solb,ts,tbr,hbr);
			
			double evall = wo.get(0)*evdist+wo.get(1)*evttravel+wo.get(2)*evcont+wo.get(3)*evuncov+wo.get(4)*evpay+wo.get(5)*evbreak;
			//System.out.println("check original "+evdist+" "+evttravel+" "+evcont+" "+evuncov+" "+evpay+" "+evbreak);
			
			//compute cost for each visit in solution -- by removing them from original solution
			for(int i=0;i<sol.size();i++){
				
				//remember which visit is removed				
				int temp1=sol.get(i);
				int temp2=solc.get(i);
				int temp3=sold.get(i);
				
				//remove the visit from solution
				sol.remove(i);
				solc.remove(i);
				sold.remove(i);
				solb.add(temp1);
				
				/*check point
				System.out.println();
				System.out.println("Destroy worst1");
				System.out.println("temp1 "+temp1+" temp2 "+temp2+" temp3 "+temp3);
				System.out.println("sol "+sol);
				System.out.println("solc "+solc);
				System.out.println("sold "+sold);
				System.out.println("solb "+solb);
				*/
				
				//compute cost of the solution without that visit
				gensolTimeAll(sol,solc,sold,solb,cid,vid,seqttl,seqptl,vdsrl, nd);
				soldw = getsolDWork();
				gensolTimeSign(sol,solc,sold,solb,cid,vid,soldw,twl1,twl2, dbl,hbr, nd);
				ts = getsolTStart();
				te = getsolTEnd();
				
				double evrmdist = evalsolDist(sol,solc,sold,solb,seqptl,seqdistl);
				double evrmttravel = evalsolTTravel(sol,solc,sold,solb,seqptl,seqttl);
				int evrmcont = evalsolCont(sol,solc,sold,solb,nc);
				double evrmuncov = evalsolUncov(vid,sol,solc,sold,solb,pr);
				double evrmpay = evalsolPay(cid,wg,sol,solc,sold,solb,ts,te,he,nd);
				double evrmbreak = evalsolBreak(sol,solc,sold,solb,ts,tbr,hbr);
				
				//compute the cost of the visit by computing the difference between cost of original solution to the one without a particular visit...
				double evrmall = evall - (wo.get(0)*evrmdist+wo.get(1)*evrmttravel+wo.get(2)*evrmcont+wo.get(3)*evrmuncov+wo.get(4)*evrmpay+wo.get(5)*evrmbreak);
				
				//...and keep them in an array
				tempevrm.add(evrmall);
				
				//transform the solution  to its original version (for next loop)
				sol.add(i,temp1);
				solc.add(i,temp2);
				sold.add(i,temp3);
				solb.remove(solb.get(solb.size()-1));
			}
			
			ArrayList<Integer> tempevrmidx= new ArrayList<Integer>();
			ArrayList<ArrayList<Integer>> tempsol = new ArrayList<ArrayList<Integer>>();

			//prepare for indexing
			for(int j=0;j<tempevrm.size();j++){
				ArrayList<Integer> temp1 = new ArrayList<Integer>();
				tempevrmidx.add(j);		//indexing for sorting, so we can retrieve the appropriate visit after sorting process
				temp1.add(sol.get(j));
				temp1.add(solc.get(j));
				temp1.add(sold.get(j));
				
				tempsol.add(temp1);
			}
			
			/*check point
			System.out.println();
			System.out.println("Destroy worst2");
			System.out.println("BEFORE SORT");
			System.out.println("tempevrm "+tempevrm);
			System.out.println("tempsol "+tempsol);
			System.out.println("tempevrmidx "+tempevrmidx);
			*/
		
			//sort solution based on its cost in descending order
			for(int k=0;k<tempevrm.size();k++){
				for(int l=k+1;l<tempevrm.size();l++){
					if(tempevrm.get(l)>tempevrm.get(k)){
						Collections.swap(tempsol,k,l);
						Collections.swap(tempevrm,k,l);
						Collections.swap(tempevrmidx,k,l);
					}
				}
			}
			
			/*check point
			System.out.println();
			System.out.println("Destroy worst3");
			System.out.println("AFTER SORT");
			System.out.println("tempevrm "+tempevrm);
			System.out.println("tempsol "+tempsol);
			System.out.println("tempevrmidx "+tempevrmidx);
			*/
			
			double x = rand.nextDouble();
			int idxSort = (int)Math.round(Math.pow(x,r)*(tempevrm.size()-1));
			int idx = tempevrmidx.get(idxSort);	
			
			//System.out.println("idxSort "+idxSort+" idx "+idx);
			
			solb.add(sol.get(idx));
			
			sol.remove(idx);
			solc.remove(idx);
			sold.remove(idx);
		
		}
		
		clearsolVec();
		clearsolBank();
		
		for(int y=0;y<sol.size();y++){
			solVec.add(sol.get(y));
			solCtaker.add(solc.get(y));
			solDay.add(sold.get(y));
		}
		
		for(int z=0;z<solb.size();z++){
			solBank.add(solb.get(z));
		}
		
		/*check point
		System.out.println();
		System.out.println("Destroy worst4");
		System.out.println("RESULT");
		System.out.println("sol "+solVec);
		System.out.println("solCtaker "+solCtaker);
		System.out.println("solDay "+solDay);
		
		System.out.println("solBank "+solBank);
		System.out.println("solb "+solb);
		*/	
	}
	
	//repair operator
	//random repair operator: ctaker id, sol, solc, sold, solb, feasible visit, feasibile visit day, feasible visit caretaker day, number of days
	//randomly insert visits to its feasible position, not checking its time feasibility
	public void repairRandom(ArrayList<Integer> cid, ArrayList<Double> wg, ArrayList<Integer> vid, ArrayList<Integer> pr,ArrayList<Integer> sol,ArrayList<Integer> solc,ArrayList<Integer> sold,ArrayList<Integer> solb,ArrayList<ArrayList<Double>> vdsrl,ArrayList<ArrayList<Integer>> seqptl, ArrayList<Double> seqdistl,ArrayList<Double> seqttl,ArrayList<Double> twl1,ArrayList<Double> twl2,ArrayList<Double> dbl, ArrayList<Integer> feav,ArrayList<ArrayList<Integer>> feavd,ArrayList<ArrayList<Integer>> feavcd, ArrayList<Double> wo, double he, double hbr, int nd, int nc){
		clearsolCont();
		clearsolRoute();
		clearsolTime();
		
		//System.out.println(feavcd);
		ArrayList<Integer> solbrm = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> tempv = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> tempnov = new ArrayList<ArrayList<Integer>>();
		
		
		//prepare indexing
		for(int i=0;i<sol.size();i++){
			ArrayList<Integer> temp1 = new ArrayList<Integer>();
			if(sol.get(i)!=10){
				temp1.add(sol.get(i));
				temp1.add(sold.get(i));
				if(!tempv.contains(temp1)){
					tempv.add(temp1);
				}
			}
			
			else if(sol.get(i)==10){
				temp1.add(sol.get(i));
				temp1.add(solc.get(i));
				temp1.add(sold.get(i));
				if(!tempv.contains(temp1)){
					tempv.add(temp1);
				}
			}		
		}
		
		//get visit combination to check on whether a visit has been assigned, by referring to indexing earlier
		for(int j=0;j<solb.size();j++){
			if(solb.get(j)!=10){
				for(int k=0;k<feavd.size();k++){
					if(solb.get(j).equals(feavd.get(k).get(0))&&tempv.indexOf(feavd.get(k))==-1&&!tempnov.contains(feavd.get(k))){
						tempnov.add(feavd.get(k));
					}
				}
				
			}
			else if(solb.get(j)==10){
				for(int l=0;l<feavcd.size();l++){
					if(solb.get(j).equals(feavcd.get(l).get(0))&&tempv.indexOf(feavcd.get(l))==-1&&!tempnov.contains(feavcd.get(l))){
						tempnov.add(feavcd.get(l));
					}
				}
			}
		}
		
		//arrange request bank to have same order with tempnov
		solb.clear();
		for(int l=0;l<tempnov.size();l++){
			solb.add(tempnov.get(l).get(0));
		}
		
		/*check point
		System.out.println();
		System.out.println("Repair random1");
		System.out.println("BEFORE");
		System.out.println(sol);
		System.out.println(solc);
		System.out.println(sold);
		System.out.println("tempnov "+tempnov);
		System.out.println("tempv "+tempv);
		System.out.println("CHECK:"+tempnov.get(0).get(0)+" "+tempnov.size()); 
		*/
		
		//get index for depot
		int o=0;
		int size1 = tempnov.size();
		for(int n=0;n<size1;n++){
			if(tempnov.get(o).get(0)==10){

				int idxd = locateDepot(sol,solc,sold,tempnov.get(o)); 
				sol.add(idxd,tempnov.get(o).get(0));
				solc.add(idxd,tempnov.get(o).get(1));
				sold.add(idxd,tempnov.get(o).get(2));	
				
				tempnov.remove(o);
				solb.remove(o);
				
				/*check point
				System.out.println();
				System.out.println("Repair random2");
				System.out.println("AFTER");
				System.out.println("idx "+idxd);
				System.out.println("tempnov "+tempnov);
				System.out.println("sol "+sol);
				System.out.println("solc "+solc);
				System.out.println("sold "+sold);
				System.out.println("solb "+solb);
				*/
				
			}
			else if(tempnov.get(o).get(0)!=10){o+=1;}
		}
		
		//difference from random heuristic starts from this line
		//get index for other visits 
		//check possible ctakers
		int size2=tempnov.size();
		for(int t=0;t<size2;t++){
			ArrayList<ArrayList<Integer>> tempx = new ArrayList<ArrayList<Integer>>();
			
			for(int s=0;s<cid.size();s++){
				ArrayList temp3 = new ArrayList<Integer>();
				temp3.add(tempnov.get(0).get(0));
				temp3.add(cid.get(s));
				temp3.add(tempnov.get(0).get(1));
				
				if(feavcd.contains(temp3)){
					tempx.add(temp3);		//containing visits with possible ctaker
				}
			}
			
			//check possible index to insert
			ArrayList<Integer> idxv = new ArrayList<Integer>();		//list of possible index to insert
			ArrayList<Integer> tempc = new ArrayList<Integer>();  	//list of possible ctaker to insert
			ArrayList<Integer> tempd = new ArrayList<Integer>(); 	//list of possible day to insert
			
			for(int u=0;u<tempx.size();u++){
				ArrayList<Integer> temp4 =locateVisits(cid,sol,solc,sold,tempx.get(u),nd); 
				
				//if there is only one possible index to insert (a,a) 
				if(temp4.get(0)==temp4.get(1)){
					idxv.add(temp4.get(0));			//possible index
					tempc.add(tempx.get(u).get(1));	//possible ctaker
					tempd.add(tempx.get(u).get(2));	//possible day
				}
				//if there are more possibilities to insert (a,b)
				else{
					for(int v=temp4.get(0);v<=temp4.get(1);v++){
						idxv.add(v);
						tempc.add(tempx.get(u).get(1));
						tempd.add(tempx.get(u).get(2));
					}
				}
			}
			
			//System.out.println(tempc);
			
			//check path feasibility
			ArrayList<Integer> tempstat = new ArrayList<Integer>();		//status of feasibility for every possible index
			for(int w=0;w<idxv.size();w++){
				
				//prepare for indexing for possible visit
				ArrayList<Integer> temp5 = new ArrayList<Integer>();
				temp5.add(tempc.get(w));
				temp5.add(tempd.get(w));
				
				//and for next visit that will be affected if it happens (check the solution)
				ArrayList<Integer> temp6 = new ArrayList<Integer>();
				if(idxv.get(w)<solc.size()){
					temp6.add(solc.get(idxv.get(w)));		
					temp6.add(sold.get(idxv.get(w)));	
				}
				
				//get path to possible previous visit,to be matched with list of sequenced path
				ArrayList<Integer> temp7 = new ArrayList<Integer>();
				temp7.add(tempc.get(w));
				temp7.add(sol.get((idxv.get(w))-1));
				temp7.add(tempnov.get(0).get(0));
			
				//get path to possible previous visit -- see temp5&temp6, there is possibility that this visit is the last position in the route
				ArrayList<Integer> temp8 = new ArrayList<Integer>();
				temp8.add(tempc.get(w));
				temp8.add(tempnov.get(0).get(0));
				
				if(!temp5.equals(temp6)||idxv.get(w)>=solc.size()){
					temp8.add(10);	//the visit is indeed the last position in the route, should return to depot
				}
				else if(temp5.equals(temp6)){
					temp8.add(sol.get(idxv.get(w)));
				}
				
				if(seqptl.contains(temp7)&&seqptl.contains(temp8)){
					tempstat.add(1);
				}
				else tempstat.add(0);
				
				/*check point
				System.out.println();
				System.out.println("Repair random3");
				System.out.println("temp5 "+temp5);
				System.out.println("temp6 "+temp6);
				System.out.println("temp7 "+temp7);
				System.out.println("temp8 "+temp8);
				*/
			}
			
			//insert in its best position
			/*check point
			System.out.println();
			System.out.println("Repair random4");
			System.out.println("idxv "+idxv);
			System.out.println("tempc "+tempc);
			System.out.println("tempd "+tempd);
			System.out.println("tempx "+tempx);
			System.out.println("tempstat "+tempstat);
			*/
			
			//evaluate if the path is feasible, get the minimum value
			ArrayList<Double> tempev = new ArrayList<Double>();
			
			int sumstat=0;
			for(int aa=0;aa<tempstat.size();aa++){
				sumstat+=tempstat.get(aa);
			}
			
			if(sumstat>0){
				for(int ab=0;ab<tempstat.size();ab++){
					if(tempstat.get(ab)==1){
						ArrayList<Integer> tempsol = new ArrayList<Integer>();
						ArrayList<Integer> tempsolc = new ArrayList<Integer>();
						ArrayList<Integer> tempsold = new ArrayList<Integer>();
						ArrayList<Integer> tempsolb = new ArrayList<Integer>();
						for(int ac=0;ac<sol.size();ac++){
							tempsol.add(sol.get(ac));
							tempsolc.add(solc.get(ac));
							tempsold.add(sold.get(ac));
						}
						
						for(int ad=0;ad<solb.size();ad++){
							tempsolb.add(solb.get(ad));	
						}
						
						tempsol.add(idxv.get(ab),tempnov.get(0).get(0));
						tempsolc.add(idxv.get(ab),tempc.get(ab));
						tempsold.add(idxv.get(ab),tempd.get(ab));
						if(tempsolb.size()>0&&tempsolb.indexOf(tempnov.get(0).get(0))!=-1) tempsolb.remove(tempsolb.indexOf(tempnov.get(0).get(0))); 
					
						/*check point
						System.out.println();
						System.out.println("Repair random5");
						System.out.println("tempsolsize "+tempsol.size());
						System.out.println("tempsol "+tempsol);
						System.out.println("tempsolc "+tempsolc);
						System.out.println("tempsold "+tempsold);
						System.out.println("tempsolb "+tempsolb);
						*/
												
						gensolTimeAll(tempsol,tempsolc,tempsold,tempsolb,cid,vid,seqttl,seqptl,vdsrl, nd);
						ArrayList<Double> soldw = getsolDWork();
						gensolTimeSign(tempsol,tempsolc,tempsold,tempsolb,cid,vid,soldw,twl1,twl2, dbl,hbr, nd);
						ArrayList<Double> ts = getsolTStart();
						ArrayList<Double> te = getsolTEnd();
						ArrayList<Double> tbr = getsolTBreak();
						
						//adding clients that will cause overlap time visit or overtime will trigger penalty of 500000.0
						/*check point
						System.out.println("soldw "+soldw);
						System.out.println("ts "+ts);
						System.out.println("te "+te);
						System.out.println("tbr "+tbr);
						*/
						
						double evdist = evalsolDist(tempsol,tempsolc,tempsold,tempsolb,seqptl,seqdistl);
						double evttravel = evalsolTTravel(tempsol,tempsolc,tempsold,tempsolb,seqptl,seqttl);
						double evcont = (double) evalsolCont(tempsol,tempsolc,tempsold,tempsolb,nc);
						double evuncov = evalsolUncov(vid,tempsol,tempsolc,tempsold,tempsolb,pr);
						double evpay = evalsolPay(cid,wg,tempsol,tempsolc,tempsold,tempsolb,ts,te,he,nd);
						ArrayList<Double> pay = getsolPay();
						double evbreak = evalsolBreak(tempsol,tempsolc,tempsold,tempsolb,ts,tbr,hbr);
						double evall = wo.get(0)*evdist+wo.get(1)*evttravel+wo.get(2)*evcont+wo.get(3)*evuncov+wo.get(4)*evpay+wo.get(5)*evbreak;
						
						//check time feasibility, after taking break time into account
						//also check whether number of ctakers limitation are fulfilled
						if(!ts.contains(500000.0)&&!te.contains(500000.0)&&evcont!=500000.0&&!pay.contains(100000.0)){
							tempev.add(evall);
							
						}
						
						else if(ts.contains(500000.0)||te.contains(500000.0)||evcont==500000.0||pay.contains(100000.0)){
							tempev.add(100000.0);
						}
							 
						tempsol.clear();
						tempsolc.clear();
						tempsold.clear();
						tempsolb.clear();
						
					}
					else tempev.add(100000.0); 
				}
				//System.out.println("tempev "+tempev);
				
				ArrayList<Integer> tempidxv= new ArrayList<Integer>();
				for(int ae=0;ae<tempev.size();ae++){
					if(tempev.get(ae)<100000.0){
						tempidxv.add(idxv.get(ae));
					}
				}
				
				int x=100000; 
				Random rand=new Random();
				if(tempidxv.size()>0){
					if(tempidxv.size()>1){
						x = tempidxv.get(rand.nextInt(tempidxv.size()));
					}
					else if(tempidxv.size()==1){
						x = tempidxv.get(0);
					}
					
					/*check point
					System.out.println();
					System.out.println("Repair random6");
					System.out.println("x "+x);
					System.out.println("tempidxv "+tempidxv);
					System.out.println("tempnov "+tempnov.get(0));
					*/
					
					//insert the chosen index to solution and remove it from request bank
					sol.add(x,tempnov.get(0).get(0)); 
					solc.add(x,tempc.get(idxv.indexOf(x)));
					sold.add(x,tempd.get(idxv.indexOf(x)));
					solb.remove(0);
					tempnov.remove(0);
				}
				
				else if(tempidxv.size()==0){
					solbrm.add(tempnov.get(0).get(0));
					solb.remove(0);
					tempnov.remove(0);
				}
				
			}
			
			else if(sumstat==0){
				//if there is no feasible index, still 'remove' it from request bank to temporary array
				//they will be removed back to request bank once repair randoom done
				solbrm.add(tempnov.get(0).get(0));
				solb.remove(0);
				tempnov.remove(0);
			}
		}
			
		clearsolVec();
		clearsolBank();
		
		for(int y=0;y<sol.size();y++){
			solVec.add(sol.get(y));
			solCtaker.add(solc.get(y));
			solDay.add(sold.get(y));
		}
		
		for(int z=0;z<solbrm.size();z++){
			solBank.add(solbrm.get(z));
		}
		
		/*check point
		System.out.println("Repair random7");
		System.out.println("solsize "+solBank.size());
		System.out.println("sol "+solVec);
		System.out.println("solCtaker "+solCtaker);
		System.out.println("solDay "+solDay);
		
		System.out.println("solBank "+solBank);
		System.out.println("solb "+solb);
		System.out.println("solbrm "+solbrm);
		*/
	}
	
	//greedy repair operator: ctaker id, ctaker wage, visit ID, sol, solc, sold, solb, service duration, sequenced path, sequenced distance, sequenced travel time, time window1, time window2, feasible visit, feasibile visit day, feasible visit caretaker day, end working time, minimum working hours before break, number of days
	public void repairGreedy(ArrayList<Integer> cid, ArrayList<Double> wg, ArrayList<Integer> vid, ArrayList<Integer> pr,ArrayList<Integer> sol,ArrayList<Integer> solc,ArrayList<Integer> sold,ArrayList<Integer> solb,ArrayList<ArrayList<Double>> vdsrl,ArrayList<ArrayList<Integer>> seqptl, ArrayList<Double> seqdistl,ArrayList<Double> seqttl,ArrayList<Double> twl1,ArrayList<Double> twl2,ArrayList<Double> dbl, ArrayList<Integer> feav,ArrayList<ArrayList<Integer>> feavd,ArrayList<ArrayList<Integer>> feavcd, ArrayList<Double> wo,double he, double hbr, int nd, int nc){
		clearsolCont();
		clearsolRoute();
		clearsolTime();
		
		//System.out.println(feavcd);
		ArrayList<Integer> solbrm = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> tempv = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> tempnov = new ArrayList<ArrayList<Integer>>();
		
		//prepare indexing
		for(int i=0;i<sol.size();i++){
			ArrayList<Integer> temp1 = new ArrayList<Integer>();
			if(sol.get(i)!=10){
				temp1.add(sol.get(i));
				temp1.add(sold.get(i));
				if(!tempv.contains(temp1)){
					tempv.add(temp1);
				}
			}
			
			else if(sol.get(i)==10){
				temp1.add(sol.get(i));
				temp1.add(solc.get(i));
				temp1.add(sold.get(i));
				if(!tempv.contains(temp1)){
					tempv.add(temp1);
				}
			}		
		}
		
		//get visit combination to check whether a visit has been assigned, by referring to indexing earlier
		for(int j=0;j<solb.size();j++){
			if(solb.get(j)!=10){
				for(int k=0;k<feavd.size();k++){
					if(solb.get(j).equals(feavd.get(k).get(0))&&tempv.indexOf(feavd.get(k))==-1&&!tempnov.contains(feavd.get(k))){
						tempnov.add(feavd.get(k));
					}
				}
				
			}
			else if(solb.get(j)==10){
				for(int l=0;l<feavcd.size();l++){
					if(solb.get(j).equals(feavcd.get(l).get(0))&&tempv.indexOf(feavcd.get(l))==-1&&!tempnov.contains(feavcd.get(l))){
						tempnov.add(feavcd.get(l));
					}
				}
			}
		}
				
		//arrange request bank to have same order with tempnov
		solb.clear();
		for(int l=0;l<tempnov.size();l++){
			solb.add(tempnov.get(l).get(0));
		}
		
		/*check point
		System.out.println();
		System.out.println("Repair greedy1");
		System.out.println("BEFORE");
		System.out.println("sol "+sol);
		System.out.println("solc "+solc);
		System.out.println("sold "+sold);
		System.out.println("solb "+solb);
		System.out.println("tempnov "+tempnov);
		System.out.println("tempv "+tempv);
		System.out.println("CHECK:"+tempnov.get(0).get(0)+" "+tempnov.size()); 
		*/
		
		//get index for depot
		int o=0;
		int size1 = tempnov.size();
		for(int n=0;n<size1;n++){
			if(tempnov.get(o).get(0)==10){

				int idxd = locateDepot(sol,solc,sold,tempnov.get(o)); 
				sol.add(idxd,tempnov.get(o).get(0));
				solc.add(idxd,tempnov.get(o).get(1));
				sold.add(idxd,tempnov.get(o).get(2));	
				
				tempnov.remove(o);
				solb.remove(o);
				
				/*check point
				System.out.println();
				System.out.println("Repair greedy2");
				System.out.println("AFTER");
				System.out.println("idx "+idxd);
				System.out.println("tempnov "+tempnov);
				System.out.println("sol "+sol);
				System.out.println("solc "+solc);
				System.out.println("sold "+sold);
				System.out.println("solb "+solb);
				*/
				
			}
			else if(tempnov.get(o).get(0)!=10){o+=1;}
		}
		
		//difference from random heuristic starts from this line
		//get index for other visits 
		//check possible ctakers
		int size2=tempnov.size();
		for(int t=0;t<size2;t++){
			ArrayList<ArrayList<Integer>> tempx = new ArrayList<ArrayList<Integer>>();
			
			for(int s=0;s<cid.size();s++){
				ArrayList temp3 = new ArrayList<Integer>();
				temp3.add(tempnov.get(0).get(0));
				temp3.add(cid.get(s));
				temp3.add(tempnov.get(0).get(1));
				
				if(feavcd.contains(temp3)){
					tempx.add(temp3);		//containing visits with possible ctaker
				}
			}
			
			//check possible index to insert
			ArrayList<Integer> idxv = new ArrayList<Integer>();		//list of possible index to insert
			ArrayList<Integer> tempc = new ArrayList<Integer>();  	//list of possible ctaker to insert
			ArrayList<Integer> tempd = new ArrayList<Integer>(); 	//list of possible day to insert
			
			for(int u=0;u<tempx.size();u++){
				ArrayList<Integer> temp4 =locateVisits(cid,sol,solc,sold,tempx.get(u),nd); 
				
				//if there is only one possible index to insert (a,a) 
				if(temp4.get(0)==temp4.get(1)){
					idxv.add(temp4.get(0));			//possible index
					tempc.add(tempx.get(u).get(1));	//possible ctaker
					tempd.add(tempx.get(u).get(2));	//possible day
				}
				//if there are more possibilities to insert (a,b)
				else{
					for(int v=temp4.get(0);v<=temp4.get(1);v++){
						idxv.add(v);
						tempc.add(tempx.get(u).get(1));
						tempd.add(tempx.get(u).get(2));
					}
				}
			}
			
			//System.out.println(tempc);
			
			//check path feasibility
			ArrayList<Integer> tempstat = new ArrayList<Integer>();		//status of feasibility for every possible index
			for(int w=0;w<idxv.size();w++){
				
				//prepare for indexing for possible visit
				ArrayList<Integer> temp5 = new ArrayList<Integer>();
				temp5.add(tempc.get(w));
				temp5.add(tempd.get(w));
				
				//and for next visit that will be affected if it happens (check the solution)
				ArrayList<Integer> temp6 = new ArrayList<Integer>();
				if(idxv.get(w)<solc.size()){
					temp6.add(solc.get(idxv.get(w)));		
					temp6.add(sold.get(idxv.get(w)));	
				}
				
				//get path to possible previous visit,to be matched with list of sequenced path
				ArrayList<Integer> temp7 = new ArrayList<Integer>();
				temp7.add(tempc.get(w));
				temp7.add(sol.get((idxv.get(w))-1));
				temp7.add(tempnov.get(0).get(0));
			
				//get path to possible previous visit -- see temp5&temp6, there is possibility that this visit is the last position in the route
				ArrayList<Integer> temp8 = new ArrayList<Integer>();
				temp8.add(tempc.get(w));
				temp8.add(tempnov.get(0).get(0));
				
				if(!temp5.equals(temp6)||idxv.get(w)>=solc.size()){
					temp8.add(10);	//the visit is indeed the last position in the route, should return to depot
				}
				else if(temp5.equals(temp6)){
					temp8.add(sol.get(idxv.get(w)));
				}
				
				if(seqptl.contains(temp7)&&seqptl.contains(temp8)){
					tempstat.add(1);
				}
				else tempstat.add(0);
				
				/*check point
				System.out.println();
				System.out.println("Repair greedy3");
				System.out.println("temp5 "+temp5);
				System.out.println("temp6 "+temp6);
				System.out.println("temp7 "+temp7);
				System.out.println("temp8 "+temp8);
				*/
			}
			
			//insert in its best position
			/*check point
			System.out.println();
			System.out.println("Repair greedy4");
			System.out.println("idxv "+idxv);
			System.out.println("tempc "+tempc);
			System.out.println("tempd "+tempd);
			System.out.println("tempx "+tempx);
			System.out.println("tempstat "+tempstat);
			*/
			
			//evaluate if the path is feasible, get the minimum value
			ArrayList<Double> tempev = new ArrayList<Double>();
			
			int sumstat=0;
			for(int aa=0;aa<tempstat.size();aa++){
				sumstat+=tempstat.get(aa);
			}
			
			if(sumstat>0){
				for(int ab=0;ab<tempstat.size();ab++){
					if(tempstat.get(ab)==1){
						ArrayList<Integer> tempsol = new ArrayList<Integer>();
						ArrayList<Integer> tempsolc = new ArrayList<Integer>();
						ArrayList<Integer> tempsold = new ArrayList<Integer>();
						ArrayList<Integer> tempsolb = new ArrayList<Integer>();
						for(int ac=0;ac<sol.size();ac++){
							tempsol.add(sol.get(ac));
							tempsolc.add(solc.get(ac));
							tempsold.add(sold.get(ac));
						}
						
						for(int ad=0;ad<solb.size();ad++){
							tempsolb.add(solb.get(ad));	
						}
						
						tempsol.add(idxv.get(ab),tempnov.get(0).get(0));
						tempsolc.add(idxv.get(ab),tempc.get(ab));
						tempsold.add(idxv.get(ab),tempd.get(ab));
						if(tempsolb.size()>0&&tempsolb.indexOf(tempnov.get(0).get(0))!=-1) tempsolb.remove(tempsolb.indexOf(tempnov.get(0).get(0))); 
					
						/*check point
						System.out.println();
						System.out.println("Repair greedy5");
						System.out.println("tempsolsize "+tempsol.size());
						System.out.println("tempsol "+tempsol);
						System.out.println("tempsolc "+tempsolc);
						System.out.println("tempsold "+tempsold);
						System.out.println("tempsolb "+tempsolb);
						System.out.println("solb "+solb);
						System.out.println("solbrm "+solbrm);
						*/
						
												
						gensolTimeAll(tempsol,tempsolc,tempsold,tempsolb,cid,vid,seqttl,seqptl,vdsrl, nd);
						ArrayList<Double> soldw = getsolDWork();
						gensolTimeSign(tempsol,tempsolc,tempsold,tempsolb,cid,vid,soldw,twl1,twl2, dbl,hbr, nd);
						ArrayList<Double> ts = getsolTStart();
						ArrayList<Double> te = getsolTEnd();
						ArrayList<Double> tbr = getsolTBreak();
						
						/*adding clients that will cause overlap time visit or overtime will trigger penalty of 500000.0
						System.out.println("soldw "+soldw);
						System.out.println("ts "+ts);
						System.out.println("te "+te);
						System.out.println("tbr "+tbr);
						*/
						
						double evdist = evalsolDist(tempsol,tempsolc,tempsold,tempsolb,seqptl,seqdistl);
						double evttravel = evalsolTTravel(tempsol,tempsolc,tempsold,tempsolb,seqptl,seqttl);
						double evcont = (double) evalsolCont(tempsol,tempsolc,tempsold,tempsolb,nc);
						double evuncov = evalsolUncov(vid,tempsol,tempsolc,tempsold,tempsolb,pr);
						double evpay = evalsolPay(cid,wg,tempsol,tempsolc,tempsold,tempsolb,ts,te,he,nd);
						ArrayList<Double> pay = getsolPay();
						double evbreak = evalsolBreak(tempsol,tempsolc,tempsold,tempsolb,ts,tbr,hbr);
						double evall = wo.get(0)*evdist+wo.get(1)*evttravel+wo.get(2)*evcont+wo.get(3)*evuncov+wo.get(4)*evpay+wo.get(5)*evbreak;
						
						//check time feasibility, after taking break time into account
						//also check whether number of ctakers limitation are fulfilled
						if(!ts.contains(500000.0)&&!te.contains(500000.0)&&evcont!=500000.0&&!pay.contains(100000.0)){
							tempev.add(evall);
							
						}
						
						else if(ts.contains(500000.0)||te.contains(500000.0)||evcont==500000.0||pay.contains(100000.0)){
							tempev.add(100000.0);
						}
							 
						tempsol.clear();
						tempsolc.clear();
						tempsold.clear();
						tempsolb.clear();
						
					}
					else tempev.add(100000.0); 
				}
				//System.out.println("tempev "+tempev);
				
				//collect only feasible solution and get the minimum cost
				double minev = 100000.0;
				ArrayList<Integer> tempidxv= new ArrayList<Integer>();
				for(int ae=0;ae<tempev.size();ae++){
					
					if(tempev.get(ae)<minev){
						minev=tempev.get(ae);
						tempidxv.clear();
						tempidxv.add(idxv.get(ae));
						
					}
					
					//but there is possibility of having equal cost despite of different index
					else if(tempev.get(ae)==minev&&tempev.get(ae)!=100000.0){
						tempidxv.add(idxv.get(ae));
						
					}
				}
					
				//randomly chosen index if there are more than 1 minimum cost
				int x=100000; 
				Random rand=new Random();
				if(tempidxv.size()>0){
					if(tempidxv.size()>1){
						x = tempidxv.get(rand.nextInt(tempidxv.size()));
					}
					else if(tempidxv.size()==1){
						x = tempidxv.get(0);
					}
					
					/*check point
					System.out.println();
					System.out.println("Repair greedy6");
					System.out.println("minev "+minev);
					System.out.println("x "+x);
					System.out.println("t "+t);
					System.out.println("tempnov "+tempnov.get(0));
					System.out.println("solb "+solb);
					*/
					
					//insert the chosen index to solution and remove it from request bank
					sol.add(x,tempnov.get(0).get(0)); 
					solc.add(x,tempc.get(idxv.indexOf(x)));
					sold.add(x,tempd.get(idxv.indexOf(x)));
					solb.remove(0);
					tempnov.remove(0);
					
				}
				
				else if(tempidxv.size()==0){
					solbrm.add(tempnov.get(0).get(0));
					solb.remove(0);
					tempnov.remove(0);
				}
			}
			
			else if(sumstat==0){
				//if there is no feasible index, still 'remove' it from request bank to temporary array
				//they will be removed back to request bank once detris done
				solbrm.add(tempnov.get(0).get(0));
				solb.remove(0);
				tempnov.remove(0);
			}
		}
		
		//System.out.println("solb "+solb);
		//System.out.println("solbrm "+solbrm);
		
		clearsolVec();
		clearsolBank();
		
		for(int y=0;y<sol.size();y++){
			solVec.add(sol.get(y));
			solCtaker.add(solc.get(y));
			solDay.add(sold.get(y));
		}
		
		for(int z=0;z<solbrm.size();z++){
			solBank.add(solbrm.get(z));
		}
		
		/*check point
		System.out.println();
		System.out.println("Repair greedy7");
		System.out.println("RESULT");
		System.out.println("sol "+solVec);
		System.out.println("solCtaker "+solCtaker);
		System.out.println("solDay "+solDay);
		
		System.out.println("solBank "+solBank);
		System.out.println("solb "+solb);
		System.out.println("solbrm "+solbrm);
		*/
	}
	
	//regret repair operator: ctaker id, ctaker wage, visit ID, sol, solc, sold, solb, service duration, sequenced path, sequenced distance, sequenced travel time, time window1, time window2, feasible visit, feasibile visit day, feasible visit caretaker day, end working time, minimum working hours before break, number of days
	public void repairRegret(ArrayList<Integer> cid, ArrayList<Double> wg, ArrayList<Integer> vid,ArrayList<Integer> pr, ArrayList<Integer> sol,ArrayList<Integer> solc,ArrayList<Integer> sold,ArrayList<Integer> solb,ArrayList<ArrayList<Double>> vdsrl,ArrayList<ArrayList<Integer>> seqptl, ArrayList<Double> seqdistl,ArrayList<Double> seqttl,ArrayList<Double> twl1,ArrayList<Double> twl2,ArrayList<Double> dbl, ArrayList<Integer> feav,ArrayList<ArrayList<Integer>> feavd,ArrayList<ArrayList<Integer>> feavcd, ArrayList<Double> wo,double he, double hbr, int nd, int nc){
		clearsolCont();
		clearsolRoute();
		clearsolTime();

		//System.out.println(feavcd);
		ArrayList<Integer> solbrm = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> tempv = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> tempnov = new ArrayList<ArrayList<Integer>>();
		
		//arraylist inside solb arraylist (sorted request bank -- to be exploited in regret heuristic)
		ArrayList<Integer> solbsort = new ArrayList<Integer>();								
		ArrayList<ArrayList<Integer>> tempnovsort = new ArrayList<ArrayList<Integer>>();	
		ArrayList<Double> tempregret = new ArrayList<Double>();
		ArrayList<Double> templowregret = new ArrayList<Double>();
		ArrayList<ArrayList<Integer>> tempidxvsort = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> tempcsort = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> tempdsort = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Double>> tempevsort = new ArrayList<ArrayList<Double>>();
		
		
		//prepare indexing
		for(int i=0;i<sol.size();i++){
			ArrayList<Integer> temp1 = new ArrayList<Integer>();
			if(sol.get(i)!=10){
				temp1.add(sol.get(i));
				temp1.add(sold.get(i));
				if(!tempv.contains(temp1)){
					tempv.add(temp1);
				}
			}
			
			else if(sol.get(i)==10){
				temp1.add(sol.get(i));
				temp1.add(solc.get(i));
				temp1.add(sold.get(i));
				if(!tempv.contains(temp1)){
					tempv.add(temp1);
				}
			}		
		}
		
		//get visit combination to check whether a visit has been assigned, by referring to indexing earlier
		for(int j=0;j<solb.size();j++){
			if(solb.get(j)!=10){
				for(int k=0;k<feavd.size();k++){
					if(solb.get(j).equals(feavd.get(k).get(0))&&tempv.indexOf(feavd.get(k))==-1&&!tempnov.contains(feavd.get(k))){
						tempnov.add(feavd.get(k));
					}
				}
				
			}
			else if(solb.get(j)==10){
				for(int l=0;l<feavcd.size();l++){
					if(solb.get(j).equals(feavcd.get(l).get(0))&&tempv.indexOf(feavcd.get(l))==-1&&!tempnov.contains(feavcd.get(l))){
						tempnov.add(feavcd.get(l));
					}
				}
			}
		}
		
		//arrange request bank to have same order with tempnov
		solb.clear();
		for(int l=0;l<tempnov.size();l++){
			solb.add(tempnov.get(l).get(0));
		}
		
		/*check point
		System.out.println();
		System.out.println("Repair regret1");
		System.out.println("BEFORE");
		System.out.println("sol "+sol);
		System.out.println("solc "+solc);
		System.out.println("sold "+sold);
		System.out.println("solb "+solb);
		System.out.println("tempnov "+tempnov);
		System.out.println("tempv "+tempv);
		System.out.println("CHECK:"+tempnov.get(0).get(0)+" "+tempnov.size()); 
		*/
		
		//get index for depot
		int o=0;
		int size1 = tempnov.size();
		ArrayList<Integer> solbdep = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> tempnovdep = new ArrayList<ArrayList<Integer>>();
		
		for(int n=0;n<size1;n++){
			if(tempnov.get(0).get(0)==10){

				int idxd = locateDepot(sol,solc,sold,tempnov.get(o)); 
				sol.add(idxd,tempnov.get(0).get(0));
				solc.add(idxd,tempnov.get(0).get(1));
				sold.add(idxd,tempnov.get(0).get(2));	
				
				tempnov.remove(0);
				solb.remove(0);
				
			}
			else if(tempnov.get(0).get(0)!=10){
				solbdep.add(solb.get(0));
				tempnovdep.add(tempnov.get(0));
				tempnov.remove(o);
				solb.remove(o);
				
			}
		}
		//moving back request bank to the original array after finish with locating depot
		for(int p=0;p<solbdep.size();p++){
			tempnov.add(tempnovdep.get(p));
			solb.add(solbdep.get(p));
		}
		solbdep.clear();
		tempnovdep.clear();
		
		/*check point
		System.out.println();
		System.out.println("Repair regret2");
		System.out.println("tempnov "+tempnov);
		System.out.println("sol "+sol);
		System.out.println("solc "+solc);
		System.out.println("sold "+sold);
		System.out.println("solb "+solb);
		*/
		
		//difference from random heuristic starts from this line
		//get index for other visits 
		//check possible ctakers
		int size2=tempnov.size();
		for(int u=0;u<size2;u++){
			ArrayList<ArrayList<Integer>> tempx = new ArrayList<ArrayList<Integer>>();
			
			for(int v=0;v<cid.size();v++){
				ArrayList temp3 = new ArrayList<Integer>();
				temp3.add(tempnov.get(u).get(0));
				temp3.add(cid.get(v));
				temp3.add(tempnov.get(u).get(1));
				
				if(feavcd.contains(temp3)){
					tempx.add(temp3);		//containing visits with possible ctaker
				}
			}
			
			//check possible index to insert
			ArrayList<Integer> idxv = new ArrayList<Integer>();		//list of possible index to insert
			ArrayList<Integer> tempc = new ArrayList<Integer>();  	//list of possible ctaker to insert
			ArrayList<Integer> tempd = new ArrayList<Integer>(); 	//list of possible day to insert
			ArrayList<Double> tempev1 = new ArrayList<Double>();
			
			for(int w=0;w<tempx.size();w++){
				ArrayList<Integer> temp4 =locateVisits(cid,sol,solc,sold,tempx.get(w),nd); 
				
				//if there is only one possible index to insert (a,a) 
				if(temp4.get(0)==temp4.get(1)){
					idxv.add(temp4.get(0));			//possible index
					tempc.add(tempx.get(w).get(1));	//possible ctaker
					tempd.add(tempx.get(w).get(2));	//possible day
				}
				//if there are more possibilities to insert (a,b)
				else{
					for(int aa=temp4.get(0);aa<=temp4.get(1);aa++){
						idxv.add(aa);
						tempc.add(tempx.get(w).get(1));
						tempd.add(tempx.get(w).get(2));
					}
				}
			}
			
			for(int ab=0;ab<idxv.size();ab++){
				//initiate empty array to calculate cost of inserting a visit in different index
				ArrayList<Integer> tempsolsort = new ArrayList<Integer>();
				ArrayList<Integer> tempsolcsort = new ArrayList<Integer>();
				ArrayList<Integer> tempsoldsort = new ArrayList<Integer>();
				ArrayList<Integer> tempsolbsort = new ArrayList<Integer>();
				
				
				for(int ac=0;ac<sol.size();ac++){
					tempsolsort.add(sol.get(ac));
					tempsolcsort.add(solc.get(ac));
					tempsoldsort.add(sold.get(ac));
				}
				
				for(int ad=0;ad<solb.size();ad++){
					tempsolbsort.add(solb.get(ad));	
				}
				
				tempsolsort.add(idxv.get(ab),tempnov.get(u).get(0));
				tempsolcsort.add(idxv.get(ab),tempc.get(ab));
				tempsoldsort.add(idxv.get(ab),tempd.get(ab));
				if(tempsolbsort.size()>0&&tempsolbsort.indexOf(tempnov.get(u).get(0))!=-1) tempsolbsort.remove(tempsolbsort.indexOf(tempnov.get(u).get(0))); 			
				
				gensolTimeAll(tempsolsort,tempsolcsort,tempsoldsort,tempsolbsort,cid,vid,seqttl,seqptl,vdsrl, nd);
				ArrayList<Double> soldwsort = getsolDWork();
				gensolTimeSign(tempsolsort,tempsolcsort,tempsoldsort,tempsolbsort,cid,vid,soldwsort,twl1,twl2, dbl,hbr, nd);
				ArrayList<Double> tssort = getsolTStart();
				ArrayList<Double> tesort = getsolTEnd();
				ArrayList<Double> tbrsort = getsolTBreak();
				
				//adding clients that will cause overlap time visit or overtime will trigger penalty of 500000.0
				/*check point
				System.out.println();
				System.out.println("Repair regret3");
				System.out.println("soldwsort "+soldwsort);
				System.out.println("tssort "+tssort);
				System.out.println("tesort "+tesort);
				System.out.println("tbrsort "+tbrsort);
				*/
				
				double evdistsort = evalsolDist(tempsolsort,tempsolcsort,tempsoldsort,tempsolbsort,seqptl,seqdistl);
				//System.out.println("evdistsort "+getsolDist());
				double evttravelsort = evalsolTTravel(tempsolsort,tempsolcsort,tempsoldsort,tempsolbsort,seqptl,seqttl);
				//System.out.println("evttravelsort "+getsolTTravel());
				double evcontsort = (double) evalsolCont(tempsolsort,tempsolcsort,tempsoldsort,tempsolbsort,nc);
				//System.out.println("evcontsort "+evcontsort);
				double evuncovsort = evalsolUncov(vid,tempsolsort,tempsolcsort,tempsoldsort,tempsolbsort,pr);
				//System.out.println("evuncovsort "+evuncovsort);
				double evpaysort = evalsolPay(cid,wg,tempsolsort,tempsolcsort,tempsoldsort,tempsolbsort,tssort,tesort,he,nd);
				//System.out.println("evpaysort "+getsolPay());
				double evbreaksort = evalsolBreak(tempsolsort,tempsolcsort,tempsoldsort,tempsolbsort,tssort,tbrsort,hbr);
				//System.out.println("evbreaksort "+evbreaksort);
				double evallsort = wo.get(0)*evdistsort+wo.get(1)*evttravelsort+wo.get(2)*evcontsort+wo.get(3)*evuncovsort+wo.get(4)*evpaysort+wo.get(5)*evbreaksort;
				
				tempev1.add(evallsort);
			}
			
			tempidxvsort.add(idxv);
			tempcsort.add(tempc);
			tempdsort.add(tempd);
			tempevsort.add(tempev1);
			double fbest1 = Collections.min(tempev1);
			
			ArrayList<Double> tempev2 = new ArrayList<Double>();
			for(int ae=0;ae<tempev1.size();ae++){
				if(tempev1.get(ae)!=fbest1){
					tempev2.add(tempev1.get(ae));	//just to get second best position, no need to copy other index
				}
			}
			
			if(tempev2.size()>0){
				double fbest2 = Collections.min(tempev2);	
				tempregret.add(fbest2-fbest1);
				templowregret.add(fbest1);	
			}
			else if(tempev2.size()==0){
				tempregret.add(0.0);	//all element is minimum, then the regret value is zero
				templowregret.add(fbest1);	
			}
			
		}
		
		/*check point
		System.out.println();
		System.out.println("Repair regret4");
		System.out.println("solb "+solb);
		System.out.println("tempnov "+tempnov);
		System.out.println("tempidxvsort "+tempidxvsort);
		System.out.println("tempcsort "+tempcsort);
		System.out.println("tempdsort "+tempdsort);
		System.out.println("tempevsort "+tempevsort);
		System.out.println("tempregret "+tempregret);
		System.out.println("templowregret "+templowregret);
		*/
		
		//sorting request bank based on its regret value in descending order
		for(int ag=0;ag<solb.size();ag++){
			solbsort.add(solb.get(ag));
			tempnovsort.add(tempnov.get(ag));
		}
		
		for(int ah=0;ah<solbsort.size();ah++){
			for(int ai=ah+1;ai<solbsort.size();ai++){
				if(tempregret.get(ai)>tempregret.get(ah)){
					Collections.swap(tempregret,ah,ai);
					Collections.swap(templowregret,ah,ai);
					Collections.swap(tempidxvsort,ah,ai);
					Collections.swap(tempcsort,ah,ai);
					Collections.swap(tempdsort,ah,ai);
					Collections.swap(tempevsort,ah,ai);
					Collections.swap(solbsort, ah, ai);
					Collections.swap(tempnovsort, ah, ai);
					
				}
				else if(tempregret.get(ai)==tempregret.get(ah)){
					if(templowregret.get(ai)<templowregret.get(ah)){
						Collections.swap(tempregret,ah,ai);
						Collections.swap(templowregret,ah,ai);
						Collections.swap(tempidxvsort,ah,ai);
						Collections.swap(tempcsort,ah,ai);
						Collections.swap(tempdsort,ah,ai);
						Collections.swap(tempevsort,ah,ai);
						Collections.swap(solbsort, ah, ai);
						Collections.swap(tempnovsort, ah, ai);
					}
					
				}
			}
		}
		
		/*check point
		System.out.println();
		System.out.println("Repair regret5");
		System.out.println("solbsort "+solbsort);
		System.out.println("tempnovsort "+tempnovsort);
		System.out.println("tempidxvsort "+tempidxvsort);
		System.out.println("tempcsort "+tempcsort);
		System.out.println("tempdsort "+tempdsort);
		System.out.println("tempevsort "+tempevsort);
		System.out.println("tempregret "+tempregret);
		System.out.println("templowregret "+templowregret);
		*/
		
		//from this line, using similar logic in repairGreedy
		int size3=tempnovsort.size();
		for(int t=0;t<size3;t++){
			ArrayList<ArrayList<Integer>> tempx = new ArrayList<ArrayList<Integer>>();
			
			for(int s=0;s<cid.size();s++){
				ArrayList temp3 = new ArrayList<Integer>();
				temp3.add(tempnovsort.get(0).get(0));
				temp3.add(cid.get(s));
				temp3.add(tempnovsort.get(0).get(1));
				
				if(feavcd.contains(temp3)){
					tempx.add(temp3);		//containing visits with possible ctaker
				}
			}
			
			//check possible index to insert
			ArrayList<Integer> idxv = new ArrayList<Integer>();		//list of possible index to insert
			ArrayList<Integer> tempc = new ArrayList<Integer>();  	//list of possible ctaker to insert
			ArrayList<Integer> tempd = new ArrayList<Integer>(); 	//list of possible day to insert
			
			for(int u=0;u<tempx.size();u++){
				ArrayList<Integer> temp4 =locateVisits(cid,sol,solc,sold,tempx.get(u),nd); 
				
				//if there is only one possible index to insert (a,a) 
				if(temp4.get(0)==temp4.get(1)){
					idxv.add(temp4.get(0));			//possible index
					tempc.add(tempx.get(u).get(1));	//possible ctaker
					tempd.add(tempx.get(u).get(2));	//possible day
				}
				//if there are more possibilities to insert (a,b)
				else{
					for(int v=temp4.get(0);v<=temp4.get(1);v++){
						idxv.add(v);
						tempc.add(tempx.get(u).get(1));
						tempd.add(tempx.get(u).get(2));
					}
				}
			}
			
			//System.out.println(tempc);
			
			//check path feasibility
			ArrayList<Integer> tempstat = new ArrayList<Integer>();		//status of feasibility for every possible index
			for(int w=0;w<idxv.size();w++){
				
				//prepare for indexing for possible visit
				ArrayList<Integer> temp5 = new ArrayList<Integer>();
				temp5.add(tempc.get(w));
				temp5.add(tempd.get(w));
				
				//and for next visit that will be affected if it happens (check the solution)
				ArrayList<Integer> temp6 = new ArrayList<Integer>();
				if(idxv.get(w)<solc.size()){
					temp6.add(solc.get(idxv.get(w)));		
					temp6.add(sold.get(idxv.get(w)));	
				}
				
				//get path to possible previous visit,to be matched with list of sequenced path
				ArrayList<Integer> temp7 = new ArrayList<Integer>();
				temp7.add(tempc.get(w));
				temp7.add(sol.get((idxv.get(w))-1));
				temp7.add(tempnovsort.get(0).get(0));
			
				//get path to possible previous visit -- see temp5&temp6, there is possibility that this visit is the last position in the route
				ArrayList<Integer> temp8 = new ArrayList<Integer>();
				temp8.add(tempc.get(w));
				temp8.add(tempnovsort.get(0).get(0));
				
				if(!temp5.equals(temp6)||idxv.get(w)>=solc.size()){
					temp8.add(10);	//the visit is indeed the last position in the route, should return to depot
				}
				else if(temp5.equals(temp6)){
					temp8.add(sol.get(idxv.get(w)));
				}
				
				if(seqptl.contains(temp7)&&seqptl.contains(temp8)){
					tempstat.add(1);
				}
				else tempstat.add(0);
				
				/*check point
				System.out.println();
				System.out.println("Repair regret6");
				System.out.println("temp5 "+temp5);
				System.out.println("temp6 "+temp6);
				System.out.println("temp7 "+temp7);
				System.out.println("temp8 "+temp8);
				*/
			}
			
			//insert in its best position
			/*check point
			System.out.println();
			System.out.println("Repair regret7");
			System.out.println("idxv "+idxv);
			System.out.println("tempc "+tempc);
			System.out.println("tempd "+tempd);
			System.out.println("tempx "+tempx);
			System.out.println("tempstat "+tempstat);
			*/
			
			//evaluate if the path is feasible, get the minimum value
			ArrayList<Double> tempev = new ArrayList<Double>();
			
			int sumstat=0;
			for(int aa=0;aa<tempstat.size();aa++){
				sumstat+=tempstat.get(aa);
			}
			
			if(sumstat>0){
				for(int ab=0;ab<tempstat.size();ab++){
					if(tempstat.get(ab)==1){
						ArrayList<Integer> tempsol = new ArrayList<Integer>();
						ArrayList<Integer> tempsolc = new ArrayList<Integer>();
						ArrayList<Integer> tempsold = new ArrayList<Integer>();
						ArrayList<Integer> tempsolb = new ArrayList<Integer>();
						for(int ac=0;ac<sol.size();ac++){
							tempsol.add(sol.get(ac));
							tempsolc.add(solc.get(ac));
							tempsold.add(sold.get(ac));
						}
						
						for(int ad=0;ad<solbsort.size();ad++){
							tempsolb.add(solbsort.get(ad));	
						}
						
						tempsol.add(idxv.get(ab),tempnovsort.get(0).get(0));
						tempsolc.add(idxv.get(ab),tempc.get(ab));
						tempsold.add(idxv.get(ab),tempd.get(ab));
						if(tempsolb.size()>0&&tempsolb.indexOf(tempnovsort.get(0).get(0))!=-1) tempsolb.remove(tempsolb.indexOf(tempnovsort.get(0).get(0))); 
					
						/*check point
						System.out.println();
						System.out.println("Repair regret8");
						System.out.println("tempsolsize "+tempsol.size());
						System.out.println("tempsol "+tempsol);
						System.out.println("tempsolc "+tempsolc);
						System.out.println("tempsold "+tempsold);
						System.out.println("tempsolb "+tempsolb);
						*/
												
						gensolTimeAll(tempsol,tempsolc,tempsold,tempsolb,cid,vid,seqttl,seqptl,vdsrl, nd);
						ArrayList<Double> soldw = getsolDWork();
						gensolTimeSign(tempsol,tempsolc,tempsold,tempsolb,cid,vid,soldw,twl1,twl2, dbl,hbr, nd);
						ArrayList<Double> ts = getsolTStart();
						ArrayList<Double> te = getsolTEnd();
						ArrayList<Double> tbr = getsolTBreak();
						
						/*adding clients that will cause overlap time visit or overtime will trigger penalty of 500000.0
						System.out.println("soldw "+soldw);
						System.out.println("ts "+ts);
						System.out.println("te "+te);
						System.out.println("tbr "+tbr);
						*/
						
						double evdist = evalsolDist(tempsol,tempsolc,tempsold,tempsolb,seqptl,seqdistl);
						double evttravel = evalsolTTravel(tempsol,tempsolc,tempsold,tempsolb,seqptl,seqttl);
						double evcont = (double) evalsolCont(tempsol,tempsolc,tempsold,tempsolb,nc);
						double evuncov = evalsolUncov(vid,tempsol,tempsolc,tempsold,tempsolb,pr);
						double evpay = evalsolPay(cid,wg,tempsol,tempsolc,tempsold,tempsolb,ts,te,he,nd);
						ArrayList<Double> pay = getsolPay();
						double evbreak = evalsolBreak(tempsol,tempsolc,tempsold,tempsolb,ts,tbr,hbr);
						double evall = wo.get(0)*evdist+wo.get(1)*evttravel+wo.get(2)*evcont+wo.get(3)*evuncov+wo.get(4)*evpay+wo.get(5)*evbreak;
						
						//check time feasibility, after taking break time into account
						//also check whether number of ctakers limitation are fulfilled
						if(!ts.contains(500000.0)&&!te.contains(500000.0)&&evcont!=500000.0&&!pay.contains(100000.0)){

							tempev.add(evall);
							
						}
						
						else if(ts.contains(500000.0)||te.contains(500000.0)||evcont==500000.0||pay.contains(100000.0)){
							tempev.add(100000.0);
						}
							 
						tempsol.clear();
						tempsolc.clear();
						tempsold.clear();
						tempsolb.clear();
						
					}
					else tempev.add(100000.0); 
				}
				//System.out.println("tempev "+tempev);
				
				//collect only feasible solution and get the minimum cost
				double minev = 100000.0;
				ArrayList<Integer> tempidxv= new ArrayList<Integer>();
				for(int ae=0;ae<tempev.size();ae++){
					
					if(tempev.get(ae)<minev){
						minev=tempev.get(ae);
						tempidxv.clear();
						tempidxv.add(idxv.get(ae));
						
					}
					
					//but there is possibility of having equal cost despite of different index
					else if(tempev.get(ae)==minev&&tempev.get(ae)!=100000.0){
						tempidxv.add(idxv.get(ae));
						
					}
				}
					
				//randomly chosen index if there are more than 1 minimum cost
				int x=100000; 
				Random rand=new Random();
				if(tempidxv.size()>0){
					if(tempidxv.size()>1){
						x = tempidxv.get(rand.nextInt(tempidxv.size()));
					}
					else if(tempidxv.size()==1){
						x = tempidxv.get(0);
					}
					
					/*check point
					System.out.println();
					System.out.println("Repair regret9");
					System.out.println("minev "+minev);
					System.out.println("x "+x);
					System.out.println("t "+t);
					System.out.println("tempnovsort "+tempnovsort.get(0));
					*/
					
					//insert the chosen index to solution and remove it from request bank
					sol.add(x,tempnovsort.get(0).get(0)); 
					solc.add(x,tempc.get(idxv.indexOf(x)));
					sold.add(x,tempd.get(idxv.indexOf(x)));
					solbsort.remove(0);
					tempnovsort.remove(0);
					tempidxvsort.remove(0);
					tempcsort.remove(0);
					tempdsort.remove(0);
					tempevsort.remove(0);
					tempregret.remove(0);
					templowregret.remove(0);
				}
				
				else if(tempidxv.size()==0){
					solbrm.add(tempnovsort.get(0).get(0));
					solbsort.remove(0);
					tempnovsort.remove(0);
					tempidxvsort.remove(0);
					tempcsort.remove(0);
					tempdsort.remove(0);
					tempevsort.remove(0);
					tempregret.remove(0);
					templowregret.remove(0);
				}
			}
			
			else if(sumstat==0){
				//if there is no feasible index, still 'remove' it from request bank to temporary array
				//they will be removed back to request bank once detris done
				solbrm.add(tempnovsort.get(0).get(0));
				solbsort.remove(0);
				tempnovsort.remove(0);
				tempidxvsort.remove(0);
				tempcsort.remove(0);
				tempdsort.remove(0);
				tempevsort.remove(0);
				tempregret.remove(0);
				templowregret.remove(0);
			}
		}
		
		solb.clear();
		tempnov.clear();
		clearsolVec();
		clearsolBank();
		
		for(int y=0;y<sol.size();y++){
			solVec.add(sol.get(y));
			solCtaker.add(solc.get(y));
			solDay.add(sold.get(y));
		}
		
		for(int z=0;z<solbrm.size();z++){
			solBank.add(solbrm.get(z));
		}
		
		/*check point
		System.out.println();
		System.out.println("Repair regret10");
		System.out.println("RESULT");
		System.out.println("sol "+solVec);
		System.out.println("solCtaker "+solCtaker);
		System.out.println("solDay "+solDay);
		
		System.out.println("solBank "+solBank);
		System.out.println("solb "+solb);
		System.out.println("solbrm "+solbrm);
		*/
	
	}
	
	//generate working duration in each visit: sol, solc, sold, solb, ctaker ID, visit ID, sequenced travel time, sequenced path, service duration, number of days 
	public void gensolTimeAll(ArrayList<Integer> sol,ArrayList<Integer> solc,ArrayList<Integer> sold,ArrayList<Integer> solb,ArrayList<Integer> cid,ArrayList<Integer> vid,ArrayList<Double> seqttl, ArrayList<ArrayList<Integer>> seqptl, ArrayList<ArrayList<Double>> vdsrl,int nd){

		clearsolCont();
		clearsolRoute();
		clearsolTime();
		
		int m = sol.size();
		int n = cid.size();
		
		//initiate time visit and work duration
		for(int e=0;e<m;e++){
			solTVisit.add(0.0);
			solDWork.add(0.0);
		}
			
		//already include mechanism to check path feasibility, by checking its precedence
		//there are 3 scenarios considered --> is a visit in starting, ending, or mid of a route?
		int i=1;
		while(i<m-1){
			Random rand = new Random(); 
			double x = rand.nextDouble(); 
			
			//get day, ctaker, index day, index ctaker
			int c1=solc.get(i-1);
			int c2=solc.get(i);
			int c3=solc.get(i+1);
			int d1=sold.get(i-1);
			int d2=sold.get(i);
			int d3=sold.get(i+1);
			int s1=sol.get(i-1);
			int s2=sol.get(i);
			int s3=sol.get(i+1);
			int cidx1=cid.indexOf(c1);
			int cidx2=cid.indexOf(c2);
			int cidx3=cid.indexOf(c3);
			int idx1=vid.indexOf(s1);
			int idx2=vid.indexOf(s2);
			int idx3=vid.indexOf(s3);
			
			//pointer for path (for travel time)
			ArrayList<Integer> tempb = new ArrayList<Integer>();
			tempb.add(c1);
			tempb.add(s1);
			tempb.add(s2);
			int pb=seqptl.indexOf(tempb);
			
			ArrayList<Integer> tempf = new ArrayList<Integer>();
			tempf.add(c2);
			tempf.add(s2);
			tempf.add(s3);
			int pf=seqptl.indexOf(tempf);
			
			ArrayList<Integer> tempd2 = new ArrayList<Integer>();
			tempd2.add(c2);
			tempd2.add(s2);
			tempd2.add(10);
			int pd2=seqptl.indexOf(tempd2);
			
			ArrayList<Integer> tempd3 = new ArrayList<Integer>();
			tempd3.add(c3);
			tempd3.add(s3);
			tempd3.add(10);
			int pd3=seqptl.indexOf(tempd3);
			
			//pointer for day ctaker index (for tstart,tend, tbreak)
			ArrayList<Integer> temp1 = new ArrayList<Integer>();
			temp1.add(c1);
			temp1.add(d1);
			
			ArrayList<Integer> temp2 = new ArrayList<Integer>();
			temp2.add(c2);
			temp2.add(d2);
			
			ArrayList<Integer> temp3 = new ArrayList<Integer>();
			temp3.add(c3);
			temp3.add(d3);
			
			/*check point
			System.out.println();
			System.out.println("Evaluation gensoltimeall1");
			//System.out.println(i);
			//System.out.println("d "+d1+" "+d2+" "+d3);
			//System.out.println("c "+c1+" "+c2+" "+c3);
			System.out.println("temppath "+tempb+" "+tempf+" "+tempd2+" "+tempd3);
			System.out.println("idxPath "+pb+" "+pf+" "+pd2+" "+pd3);
			System.out.println("temppointer "+temp1+" "+temp2+" "+temp3);
			//System.out.println("ttl ");
			if(pb!=-1){System.out.println(seqttl.get(pb));}
			if(pf!=-1){System.out.println(seqttl.get(pf));}
			if(pd2!=-1){System.out.println(seqttl.get(pd2));}
			if(pd3!=-1){System.out.println(seqttl.get(pd3));}
			System.out.println("vdsrl "+vdsrl.get(idx1).get(cidx1)+" "+vdsrl.get(idx2).get(cidx2)+" "+vdsrl.get(idx3).get(cidx3));
			System.out.println(solDWork);
			*/
			
			//if the visit starts a route
			if((!temp2.equals(temp1))&&(temp2.equals(temp3))){
				if(pf!=-1&&s2==10){
					solDWork.set(i, seqttl.get(pf));
				}
				else if(pf==-1||s2!=10) solDWork.set(i,100000.0);
				
				if(i+1==m-1&&solDWork.get(i+1)<=0.0&&pd3!=-1){
					solDWork.set(i+1,seqttl.get(pd3)+vdsrl.get(idx3).get(cidx3));
				}
				
				else if(i+1==m-1&&solDWork.get(i+1)<=0.0&&pd3==-1) solDWork.set(i+1,100000.0);
			}
			
			//if the visit is in the mid of a route
			else if((temp2.equals(temp1))&&(temp2.equals(temp3))){
				if(pf!=-1&&pb!=-1&&s2!=10){
					solDWork.set(i,seqttl.get(pf)+vdsrl.get(idx2).get(cidx2));
			
				}
				
				else if(pf==-1||pb==-1||s2==10){
					solDWork.set(i,100000.0);
				}
				
				if(i-1==0&&solDWork.get(i-1)<=0.0&&pb!=-1&&s1==10){
					solDWork.set((i-1),seqttl.get(pb));
				}
				
				else if((i-1==0&&solDWork.get(i-1)<=0.0&&pb==-1)||(i-1==0&&solDWork.get(i-1)<=0.0&&s1!=10)){
					solDWork.set((i-1),100000.0);
				}	
				
				else if(i+1==m-1&&solDWork.get(i+1)<=0.0&&pd3!=-1){
					solDWork.set(i+1,seqttl.get(pd3)+vdsrl.get(idx3).get(cidx3));
				}
				
				else if(i+1==m-1&&solDWork.get(i+1)<=0.0&&pd3==-1){
					solDWork.set(i+1,100000.0);
				}
			}
			
			//if the visit ends a route
			else if((temp2.equals(temp1))&&(!temp2.equals(temp3))){
				
				//there's link from previous client and to back to depot
				if(pb!=-1&&pd2!=-1&&s2!=10){
					solDWork.set(i, seqttl.get(pd2)+vdsrl.get(idx2).get(cidx2));
				}
				
				else if(pb==-1||pd2==-1||s2==10) solDWork.set(i, 100000.0);
				
				if(i-1==0&&solDWork.get(i-1)<=0.0&&pb!=-1&&s1==10){
					solDWork.set((i-1),seqttl.get(pb));
				}
				
				else if((i-1==0&&solDWork.get(i-1)<=0.0&&pb==-1)||(i-1==0&&solDWork.get(i-1)<=0.0&&s1!=10)){
					solDWork.set((i-1),100000.0);
				}
			}
			i+=1;
		}
		//System.out.println(solDWork);
		
		clearsolVec();
		clearsolBank();
		
		for(int y=0;y<sol.size();y++){
			solVec.add(sol.get(y));
			solCtaker.add(solc.get(y));
			solDay.add(sold.get(y));
		}
		
		for(int z=0;z<solb.size();z++){
			solBank.add(solb.get(z));
		}
		
		/*check point
		System.out.println();
		System.out.println("Evaluation gensoltimeall2");
		System.out.println("RESULT");
		System.out.println("sol "+solVec);
		System.out.println("solCtaker "+solCtaker);
		System.out.println("solDay "+solDay);
		
		System.out.println("solBank "+solBank);
		*/
	}
	
	//generate time marking (start time, end time, visit time, break time) in each visit: sol, solc, sold, solb, ctaker ID, visit ID, working duration (refer to gensolTimeAll), time window1, time window2, break duration, minimum working hours before break, number of days
	public void gensolTimeSign(ArrayList<Integer> sol,ArrayList<Integer> solc,ArrayList<Integer> sold,ArrayList<Integer> solb,ArrayList<Integer> cid,ArrayList<Integer> vid,ArrayList<Double> dwl,ArrayList<Double> twl1,ArrayList<Double> twl2, ArrayList<Double> dbl, double hbr,int nd){
		clearsolCont();
		clearsolRoute();
		clearsolTime();
		
		int n = cid.size();

		//initiate time start, time end, and time break
		ArrayList<ArrayList<Integer>> tempidx = new ArrayList<ArrayList<Integer>>();
		
		//prepare ctaker+day indexing
		for(int g=0;g<n;g++){
			for(int h=0;h<nd;h++){
				ArrayList<Integer> temp1 = new ArrayList<Integer>();
				temp1.add(cid.get(g));
				temp1.add(h+1);
				
				tempidx.add(temp1);
			}
		}
		
		
		//prepare work duration indexing
		ArrayList<ArrayList<Integer>> tempdw = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<dwl.size();i++){
			ArrayList<Integer> temp2 = new ArrayList<Integer>();
			temp2.add(solc.get(i));		
			temp2.add(sold.get(i));
			tempdw.add(temp2);
		}
		
		//partition time duration according to its caretakers and day
		ArrayList<ArrayList<Double>> tempall = new ArrayList<ArrayList<Double>>();	//working duration 
		ArrayList<ArrayList<Integer>> tempv = new ArrayList<ArrayList<Integer>>();	//visit
		for(int j=0;j<tempidx.size();j++){
			ArrayList<Double> temp3=new ArrayList<Double>();
			ArrayList<Integer> temp4=new ArrayList<Integer>();
			for(int k=0;k<tempdw.size();k++){
				if(tempdw.get(k).equals(tempidx.get(j))){
					temp3.add(dwl.get(k));
					temp4.add(sol.get(k));
				}
			}
			tempall.add(temp3);
			tempv.add(temp4);
		}
		
		//collecting data based on tempidx indexing (per ctaker per day): time window, cumulative working time for break time, break time (both binary and duration)  
		ArrayList<ArrayList<Double>> temptw1 = new ArrayList<ArrayList<Double>>();	//time window1
		ArrayList<ArrayList<Double>> temptw2 = new ArrayList<ArrayList<Double>>();	//time window2
		ArrayList<ArrayList<Double>> tempcumdw = new ArrayList<ArrayList<Double>>();//cumulative working duration
		ArrayList<ArrayList<Integer>> tempsbr = new ArrayList<ArrayList<Integer>>();//binary for break node
		ArrayList<ArrayList<Double>> tempaddbr = new ArrayList<ArrayList<Double>>();//break duration for node before break node
		
		for(int l=0;l<tempidx.size();l++){
			
			ArrayList<Double> temp5=new ArrayList<Double>();	//for time window1
			ArrayList<Double> temp6=new ArrayList<Double>();	//for time window2
			ArrayList<Double> temp7=new ArrayList<Double>();	//for cumulative working duration
			ArrayList<Integer> temp8=new ArrayList<Integer>();	//for break node assignment
			ArrayList<Double> temp9=new ArrayList<Double>();	//for break duration in break node
			for(int m=0;m<tempall.get(l).size();m++){
				
				//generate associated time window
				temp5.add(twl1.get(vid.indexOf(tempv.get(l).get(m))));
				temp6.add(twl2.get(vid.indexOf(tempv.get(l).get(m))));
				
				if(!tempall.get(l).contains(100000.0)){
					temp9.add(0.0);
					
					//cumulative sum of working duration, to compute break node requirement
					if(m==0){
						temp7.add(tempall.get(l).get(m));
					}
					else if(m>0){
						temp7.add((temp7.get(m-1))+(tempall.get(l).get(m)));
					}
					
					//assign binary for break node
					if(temp7.get(m)>hbr&&!temp8.contains(1)&&m-1>=0){
						temp8.add(1);
						//and assign break duration for previous node
						//remember that break is before visit and thus, it is not allowed to have break duration in the first visit
						//as the binary assignment for the previous node implies that the ctaker will require break even before he/she starts the visit
						temp9.set(m-1, dbl.get(cid.indexOf(tempidx.get(l).get(0))));
					}
					else {temp8.add(0);}
				}
					
				else{
					temp7.add(100000.0);
					temp8.add(0);
					temp9.add(0.0);
				}
				
			}
			
			temptw1.add(temp5);
			temptw2.add(temp6);
			tempcumdw.add(temp7);
			tempsbr.add(temp8);
			tempaddbr.add(temp9);		
		}
		
		//get work duration with break
		ArrayList<ArrayList<Double>> tempallbr = new ArrayList<ArrayList<Double>>(); //work duration, including break duration
		
		for(int p=0;p<tempidx.size();p++){
			ArrayList<Double> temp10 = new ArrayList<Double>();
	
			for(int q=0;q<tempall.get(p).size();q++){
				if(!tempall.get(p).contains(100000.0)){
					double dur=tempall.get(p).get(q)+tempaddbr.get(p).get(q); 
					temp10.add(dur);	
				}
				else{
					temp10.add(100000.0);
				}
			}
			tempallbr.add(temp10);
		}
		
		//compute start and end time in each node (end node already include travel time back to depot)
		ArrayList<ArrayList<Double>> temptimesign1 = new ArrayList<ArrayList<Double>>(); 	//start time in each node
		ArrayList<ArrayList<Double>> temptimesign2 = new ArrayList<ArrayList<Double>>(); 	//end time in each node
		ArrayList<ArrayList<Double>> tempwait = new ArrayList<ArrayList<Double>>(); 		//wait time if there is difference between start time and end time at previous node
		
		for(int s=0;s<tempidx.size();s++){
			ArrayList<Double> temp11 = new ArrayList<Double>(); //for time start visit
			ArrayList<Double> temp12 = new ArrayList<Double>(); //for time end visit
			ArrayList<Double> temp13 = new ArrayList<Double>(); //for waiting time
			
			
			for(int t=0;t<tempall.get(s).size();t++){
				if(!tempall.get(s).contains(100000.0)){
					if(t==0&&tempall.get(s).size()>1){
						temp11.add(temptw1.get(s).get(t+1)-tempallbr.get(s).get(t));
						temp12.add(temptw1.get(s).get(t+1));
						temp13.add(0.0);
						
					}
					else if(t==0&&tempall.get(s).size()==1){
						temp11.add(0.0);
						temp12.add(0.0);
						temp13.add(0.0);
					}
					else{
						//if fulfill precedence (even after including break duration)...
						if(temp12.get(t-1)<=temptw2.get(s).get(t)&&temp11.get(t-1)>=0){		//...remember that vsSeqPath already considers service time			
							temp11.add(Math.max(temptw1.get(s).get(t),temp12.get(t-1)));	//...get start time = max(time window1, end time in previous node)
							temp12.add(temp11.get(t)+tempallbr.get(s).get(t));				//...get end time (also including break duration in next node)
							temp13.add(temp11.get(t)-temp12.get(t-1));						//...get wait time if there's difference
						}
						else{
							temp11.add(500000.0);		//if cannot fulfill precedence, impose penalty
							temp12.add(500000.0);
							temp13.add(0.0);		//but wait time is zero
						}
					}
				}
				else{
					temp11.add(100000.0);
					temp12.add(100000.0);
					temp13.add(0.0);
				}
			}
			temptimesign1.add(temp11);
			temptimesign2.add(temp12);
			tempwait.add(temp13);
		}
		
		ArrayList<ArrayList<Double>> temptimeshift1 = new ArrayList<ArrayList<Double>>(); 	//start time in each node after shifted
		ArrayList<ArrayList<Double>> temptimeshift2 = new ArrayList<ArrayList<Double>>(); 	//end time in each node after shifted
		ArrayList<ArrayList<Double>> tempshift = new ArrayList<ArrayList<Double>>(); 		//record shifting 
		ArrayList<Double> tempsumwait =new ArrayList<Double>();
		
		//check whether there is non-zero waiting time in a route
		for(int aa=0;aa<tempidx.size();aa++){
			double sumwait =0.0;
			for(int ab=0;ab<tempall.get(aa).size();ab++){
				sumwait+=tempwait.get(aa).get(ab);
			}
			tempsumwait.add(sumwait);
		}
		
		//prepare empty array
		for(int ac=0;ac<tempidx.size();ac++){
			ArrayList<Double> temp14 = new ArrayList<Double>();
			ArrayList<Double> temp15 = new ArrayList<Double>();
			ArrayList<Double> temp16 = new ArrayList<Double>();
			
			for(int ad=0;ad<tempall.get(ac).size();ad++){
				temp14.add(0.0);
				temp15.add(0.0);
				temp16.add(0.0);
			}
			temptimeshift1.add(temp14);
			temptimeshift2.add(temp15);
			tempshift.add(temp16);
		}
		
		//shifting time marking if there is non zero wait time in a route
		for(int ae=0;ae<tempidx.size();ae++){
			
			for(int af=tempall.get(ae).size()-1;af>=0;af--){
				
				//only interested in feasible route (path and time) that has non zero waiting time
				if(tempsumwait.get(ae)>0.0&&!tempall.get(ae).contains(100000.0)&&!temptimesign1.get(ae).contains(500000.0)){
					if(af==tempall.get(ae).size()-1){
						tempshift.get(ae).set(af,0.0);
						temptimeshift1.get(ae).set(af,temptimesign1.get(ae).get(af));
						temptimeshift2.get(ae).set(af,temptimesign2.get(ae).get(af));
					}
					else if(af!=tempall.get(ae).size()-1){
						double slack = temptw2.get(ae).get(af) - temptimesign1.get(ae).get(af);
						tempshift.get(ae).set(af,Math.min(slack,tempshift.get(ae).get(af+1)+tempwait.get(ae).get(af+1)));
						temptimeshift1.get(ae).set(af,temptimesign1.get(ae).get(af)+tempshift.get(ae).get(af));
						temptimeshift2.get(ae).set(af,temptimesign2.get(ae).get(af)+tempshift.get(ae).get(af));
					}
				}
				else if(tempsumwait.get(ae)==0.0||tempall.get(ae).contains(100000.0)||temptimesign1.get(ae).contains(500000.0)){
					
					tempshift.get(ae).set(af,0.0);
					temptimeshift1.get(ae).set(af,temptimesign1.get(ae).get(af));
					temptimeshift2.get(ae).set(af,temptimesign2.get(ae).get(af));
				}
			}
		}
		
		//store required result
		for(int u=0;u<tempidx.size();u++){
			for(int v=0;v<tempall.get(u).size();v++){
				solSBreak.add(tempsbr.get(u).get(v));
				solDBreak.add(tempaddbr.get(u).get(v));
				solDWorkBreak.add(tempallbr.get(u).get(v));
				solDWait.add(tempwait.get(u).get(v));
				
				//to input start time
				if(v==0){
				
					solTVisit.add(temptimeshift1.get(u).get(0));
					solIdxCtaker.add(tempidx.get(u).get(0));
					solIdxDay.add(tempidx.get(u).get(1));
					
					if(!temptimeshift1.contains(100000.0)){
						solTStart.add(temptimeshift1.get(u).get(0));	
					}
					else solTStart.add(100000.0);
					
				}
				
				//to input visit time
				if(v!=0){
					solTVisit.add(temptimeshift1.get(u).get(v));
				}
				
				//to input end time
				if(v==tempall.get(u).size()-1){
					if(!temptimeshift2.contains(100000.0)){
						solTEnd.add(temptimeshift2.get(u).get(tempall.get(u).size()-1));	
					}
					else solTEnd.add(100000.0);
				}
				
				//to input break time
				if(tempaddbr.get(u).get(v)>0.0){
					solTBreak.add(temptimeshift2.get(u).get(v)-dbl.get(cid.indexOf(tempidx.get(u).get(0))));
				}
				else solTBreak.add(0.0);
				
				
			}
		}
		
		/*check point
		System.out.println();
		System.out.println("Evaluation gensoltimesign1");
		System.out.println("tempdw "+tempdw.size()+tempdw);
		System.out.println("tempidx "+tempdw.size()+tempdw);
		System.out.println("tempall "+tempall.size()+tempall);
		System.out.println("tempv "+tempv.size()+tempv);
		System.out.println("tempaddbr "+tempaddbr.size()+tempaddbr);
		System.out.println("tempallbr "+tempallbr.size()+tempallbr);
		System.out.println("temptw1 "+temptw1.size()+temptw1);
		System.out.println("temptw2 "+temptw2.size()+temptw2);
		System.out.println("temptimesign1 "+temptimesign1.size()+temptimesign1);
		System.out.println("temptimesign2 "+temptimesign2.size()+temptimesign2);
		System.out.println("tempwait "+tempwait.size()+tempwait);
		System.out.println("temptimeshift1 "+temptimeshift1.size()+temptimeshift1);
		System.out.println("temptimeshift2 "+temptimeshift2.size()+temptimeshift2);
		System.out.println("tempshift "+tempshift.size()+tempshift);
		System.out.println("tempcumdw "+tempcumdw.size()+tempcumdw);
		System.out.println("tempsbr "+tempsbr.size()+tempsbr);
		*/
		
		/*check point
		System.out.println();
		System.out.println("Evaluation gensoltimesign2");
		System.out.println("solTStart "+solTStart.size()+solTStart);
		System.out.println("solTEnd "+solTEnd.size()+solTEnd);
		System.out.println("solTBreak "+solTBreak.size()+solTBreak);
		System.out.println("solTVisit "+solTVisit.size()+solTVisit);
		System.out.println("solDWorkBreak "+solDWorkBreak.size()+solDWorkBreak);
		System.out.println("solSBreak "+solSBreak.size()+solSBreak);
		System.out.println("solDBreak "+solDBreak.size()+solDBreak);
		System.out.println("solDWait "+solDWait.size()+solDWait);
		*/
		
		clearsolVec();
		clearsolBank();
		
		for(int y=0;y<sol.size();y++){
			solVec.add(sol.get(y));
			solCtaker.add(solc.get(y));
			solDay.add(sold.get(y));
		}
		
		for(int z=0;z<solb.size();z++){
			solBank.add(solb.get(z));
		}
		
		/*check point
		System.out.println();
		System.out.println("Evaluation gensoltimesign3");
		System.out.println("RESULT");
		System.out.println("temptimesign1 "+temptimesign1);
		System.out.println("temptimesign2 "+temptimesign2);
		System.out.println("sol "+solVec);
		System.out.println("solCtaker "+solCtaker);
		System.out.println("solDay "+solDay);
		
		System.out.println("solBank "+solBank);
		*/
	}
	
	//evaluate total distance in a solution: sol, solc, sold, solb, sequenced path, sequenced distance
	public double evalsolDist(ArrayList<Integer> sol,ArrayList<Integer> solc,ArrayList<Integer> sold,ArrayList<Integer> solb,ArrayList<ArrayList<Integer>> seqptl, ArrayList<Double> seqdistl){
		clearsolCont();
		clearsolRoute();
		clearsolTime();
		
		int m = sol.size();
		
		for(int e=0;e<m;e++){
			solDist.add(0.0);
		}
		
		int i=1;
		while(i<m-1){		
			//get day, ctaker, index day, index ctaker
			int c1=solc.get(i-1);
			int c2=solc.get(i);
			int c3=solc.get(i+1);
			int d1=sold.get(i-1);
			int d2=sold.get(i);
			int d3=sold.get(i+1);
			int s1=sol.get(i-1);
			int s2=sol.get(i);
			int s3=sol.get(i+1);
			
			//pointer for path (for distance)
			ArrayList<Integer> tempb = new ArrayList<Integer>();
			tempb.add(c1);
			tempb.add(s1);
			tempb.add(s2);
			int pb=seqptl.indexOf(tempb);
			
			ArrayList<Integer> tempf = new ArrayList<Integer>();
			tempf.add(c2);
			tempf.add(s2);
			tempf.add(s3);
			int pf=seqptl.indexOf(tempf);
			
			ArrayList<Integer> tempd2 = new ArrayList<Integer>();
			tempd2.add(c2);
			tempd2.add(s2);
			tempd2.add(10);
			int pd2=seqptl.indexOf(tempd2);
			
			ArrayList<Integer> tempd3 = new ArrayList<Integer>();
			tempd3.add(c3);
			tempd3.add(s3);
			tempd3.add(10);
			int pd3=seqptl.indexOf(tempd3);
			
			//pointer for day ctaker index 
			ArrayList<Integer> temp1 = new ArrayList<Integer>();
			temp1.add(c1);
			temp1.add(d1);
			
			ArrayList<Integer> temp2 = new ArrayList<Integer>();
			temp2.add(c2);
			temp2.add(d2);
			
			ArrayList<Integer> temp3 = new ArrayList<Integer>();
			temp3.add(c3);
			temp3.add(d3);
			
			/*check point
			System.out.println();
			System.out.println("Evaluation evalsoldist1");
			System.out.println(i);
			System.out.println("d "+d1+" "+d2+" "+d3);
			System.out.println("c "+c1+" "+c2+" "+c3);
			System.out.println("temppath "+tempb+" "+tempf+" "+tempd2+" "+tempd3);
			System.out.println("idxPath "+pb+" "+pf+" "+pd2+" "+pd3);
			System.out.println("temppointer "+temp1+" "+temp2+" "+temp3);
			System.out.println("ttl ");
			if(pb!=-1){System.out.println(seqdistl.get(pb));}
			if(pf!=-1){System.out.println(seqdistl.get(pf));}
			if(pd2!=-1){System.out.println(seqdistl.get(pd2));}
			if(pd3!=-1){System.out.println(seqdistl.get(pd3));}
			System.out.println(solDist);
			*/
			
			//if the visit starts a route
			if((!temp2.equals(temp1))&&(temp2.equals(temp3))){
				if(pf!=-1&&s2==10){
					solDist.set(i, seqdistl.get(pf));
				}
				else if(pf==-1||s2!=10) solDist.set(i,100000.0);
				
				if(i+1==m-1&&solDist.get(i+1)<=0.0&&pd3!=-1){
					solDist.set(i+1,seqdistl.get(pd3));
				}
				
				else if(i+1==m-1&&solDist.get(i+1)<=0.0&&pd3==-1) solDist.set(i+1,100000.0);
			}
			
			//if the visit is in the mid of a route
			else if((temp2.equals(temp1))&&(temp2.equals(temp3))){
				if(pf!=-1&&pb!=-1&&s2!=10){
					solDist.set(i,seqdistl.get(pf));
				}
				
				else if(pf==-1||pb==-1||s2==10){
					solDist.set(i,100000.0);
				}
				
				if(i-1==0&&solDist.get(i-1)<=0.0&&pb!=-1&&s1==10){
					solDist.set((i-1),seqdistl.get(pb));
				}
				
				else if((i-1==0&&solDist.get(i-1)<=0.0&&pb==-1)||(i-1==0&&solDist.get(i-1)<=0.0&&s1!=10)){
					solDist.set((i-1),100000.0);
				}	
				
				else if(i+1==m-1&&solDist.get(i+1)<=0.0&&pd3!=-1){
					solDist.set(i+1,seqdistl.get(pd3));
				}
				
				else if(i+1==m-1&&solDist.get(i+1)<=0.0&&pd3==-1){
					solDist.set(i+1,100000.0);
				}
			}
			
			//if the visit ends a route
			else if((temp2.equals(temp1))&&(!temp2.equals(temp3))){
				if(pb!=-1&&pd2!=-1&&s2!=10){
					solDist.set(i, seqdistl.get(pd2));
				}
				
				else if(pb==-1||pd2==-1||s2==10) solDist.set(i, 100000.0);
				
				if(i-1==0&&solDist.get(i-1)<=0.0&&pb!=-1&&s1==10){
					solDist.set((i-1),seqdistl.get(pb));
				}
				
				else if((i-1==0&&solDist.get(i-1)<=0.0&&pb==-1)||(i-1==0&&solDist.get(i-1)<=0.0&&s1!=10)){
					solDist.set((i-1),100000.0);
				}
			}
			i+=1;
		}
		
		double solSumDist=0.0;
		for(int j=0;j<m;j++){
			solSumDist+=solDist.get(j);
		}
		
		clearsolVec();
		clearsolBank();
		
		for(int y=0;y<sol.size();y++){
			solVec.add(sol.get(y));
			solCtaker.add(solc.get(y));
			solDay.add(sold.get(y));
		}
		
		for(int z=0;z<solb.size();z++){
			solBank.add(solb.get(z));

		}
		
		/*check point
		System.out.println();
		System.out.println("Evaluation evalsoldist2");
		System.out.println("RESULT");
		System.out.println("solDist "+solDist);
		System.out.println("sol "+solVec);
		System.out.println("solCtaker "+solCtaker);
		System.out.println("solDay "+solDay);
		
		System.out.println("solBank "+solBank);
		*/
		
		return solSumDist;
	}
	
	//evaluate total time travelled by caretakers in a solution: sol, solc, sold, solb, sequenced path, sequenced time travel
	public double evalsolTTravel(ArrayList<Integer> sol,ArrayList<Integer> solc,ArrayList<Integer> sold,ArrayList<Integer> solb,ArrayList<ArrayList<Integer>> seqptl, ArrayList<Double> seqttl){
		clearsolCont();
		clearsolRoute();
		clearsolTime();
		int m = sol.size();
		
		for(int e=0;e<m;e++){
			solTTravel.add(0.0);
		}
		
		int i=1;
		while(i<m-1){			
			//get day, ctaker, index day, index ctaker
			int c1=solc.get(i-1);
			int c2=solc.get(i);
			int c3=solc.get(i+1);
			int d1=sold.get(i-1);
			int d2=sold.get(i);
			int d3=sold.get(i+1);
			int s1=sol.get(i-1);
			int s2=sol.get(i);
			int s3=sol.get(i+1);
			
			//pointer for path (for travel time)
			ArrayList<Integer> tempb = new ArrayList<Integer>();
			tempb.add(c1);
			tempb.add(s1);
			tempb.add(s2);
			int pb=seqptl.indexOf(tempb);
			
			ArrayList<Integer> tempf = new ArrayList<Integer>();
			tempf.add(c2);
			tempf.add(s2);
			tempf.add(s3);
			int pf=seqptl.indexOf(tempf);
			
			ArrayList<Integer> tempd2 = new ArrayList<Integer>();
			tempd2.add(c2);
			tempd2.add(s2);
			tempd2.add(10);
			int pd2=seqptl.indexOf(tempd2);
			
			ArrayList<Integer> tempd3 = new ArrayList<Integer>();
			tempd3.add(c3);
			tempd3.add(s3);
			tempd3.add(10);
			int pd3=seqptl.indexOf(tempd3);
			
			//pointer for day ctaker index 
			ArrayList<Integer> temp1 = new ArrayList<Integer>();
			temp1.add(c1);
			temp1.add(d1);
			
			ArrayList<Integer> temp2 = new ArrayList<Integer>();
			temp2.add(c2);
			temp2.add(d2);
			
			ArrayList<Integer> temp3 = new ArrayList<Integer>();
			temp3.add(c3);
			temp3.add(d3);
			
			/*check point
			System.out.println();
			System.out.println("Evaluation evalsolttravel1");
			System.out.println(i);
			System.out.println("d "+d1+" "+d2+" "+d3);
			System.out.println("c "+c1+" "+c2+" "+c3);
			System.out.println("temppath "+tempb+" "+tempf+" "+tempd2+" "+tempd3);
			System.out.println("idxPath "+pb+" "+pf+" "+pd2+" "+pd3);
			System.out.println("temppointer "+temp1+" "+temp2+" "+temp3);
			System.out.println("ttl ");
			if(pb!=-1){System.out.println(seqttl.get(pb));}
			if(pf!=-1){System.out.println(seqttl.get(pf));}
			if(pd2!=-1){System.out.println(seqttl.get(pd2));}
			if(pd3!=-1){System.out.println(seqttl.get(pd3));}			
			*/
			
			//if the visit starts a route
			if((!temp2.equals(temp1))&&(temp2.equals(temp3))){
				if(pf!=-1&&s2==10){
					solTTravel.set(i, seqttl.get(pf));
				}
				else if(pf==-1||s2!=10) solTTravel.set(i,100000.0);
				
				if(i+1==m-1&&solTTravel.get(i+1)<=0.0&&pd3!=-1){
					solTTravel.set(i+1,seqttl.get(pd3));
				}
				
				else if(i+1==m-1&&solTTravel.get(i+1)<=0.0&&pd3==-1) solTTravel.set(i+1,100000.0);
			}
			
			//if the visit is in the mid of a route
			else if((temp2.equals(temp1))&&(temp2.equals(temp3))){
				if(pf!=-1&&pb!=-1&&s2!=10){
					solTTravel.set(i,seqttl.get(pf));
				}
				
				else if(pf==-1||pb==-1||s2==10){
					solTTravel.set(i,100000.0);
				}
				
				if(i-1==0&&solTTravel.get(i-1)<=0.0&&pb!=-1&&s1==10){
					solTTravel.set((i-1),seqttl.get(pb));
				}
				
				else if((i-1==0&&solTTravel.get(i-1)<=0.0&&pb==-1)||(i-1==0&&solTTravel.get(i-1)<=0.0&&s1!=10)){
					solTTravel.set((i-1),100000.0);
				}	
				
				else if(i+1==m-1&&solTTravel.get(i+1)<=0.0&&pd3!=-1){
					solTTravel.set(i+1,seqttl.get(pd3));
				}
				
				else if(i+1==m-1&&solTTravel.get(i+1)<=0.0&&pd3==-1){
					solTTravel.set(i+1,100000.0);
				}
			}
			
			//if the visit ends a route
			else if((temp2.equals(temp1))&&(!temp2.equals(temp3))){
				if(pb!=-1&&pd2!=-1&&s2!=10){
					solTTravel.set(i, seqttl.get(pd2));
				}
				
				else if(pb==-1||pd2==-1||s2==10) solTTravel.set(i, 100000.0);
				
				if(i-1==0&&solTTravel.get(i-1)<=0.0&&pb!=-1&&s1==10){
					solTTravel.set((i-1),seqttl.get(pb));
				}
				
				else if((i-1==0&&solTTravel.get(i-1)<=0.0&&pb==-1)||(i-1==0&&solTTravel.get(i-1)<=0.0&&s1!=10)){
					solTTravel.set((i-1),100000.0);
				}
			}
			i+=1;
		}
		
		
		double solSumTTravel=0.0;
		for(int j=0;j<m;j++){
			solSumTTravel+=solTTravel.get(j);
		}
		
		clearsolVec();
		clearsolBank();
		
		for(int y=0;y<sol.size();y++){
			solVec.add(sol.get(y));
			solCtaker.add(solc.get(y));
			solDay.add(sold.get(y));
		}
		
		for(int z=0;z<solb.size();z++){
			solBank.add(solb.get(z));

		}	
		
		/*check point
		System.out.println();
		System.out.println();
		System.out.println("Evaluation evalsolttravel2");
		System.out.println("RESULT");
		System.out.println("solTTravel "+solTTravel);
		System.out.println("sol "+solVec);
		System.out.println("solCtaker "+solCtaker);
		System.out.println("solDay "+solDay);
		
		System.out.println("solBank "+solBank);
		*/
		
		return solSumTTravel;
	}
	
	//evaluate total different caretakers assigned with respect to the visits: sol, solc, sold, solb
	public int evalsolCont(ArrayList<Integer> sol,ArrayList<Integer> solc,ArrayList<Integer> sold,ArrayList<Integer> solb, int nc){
		clearsolCont();
		clearsolRoute();
		clearsolTime();
		
		int solSumCont=0;
	
		ArrayList<ArrayList<Integer>> tempvc = new ArrayList<ArrayList<Integer>>();
		
		//prepare index to get (visit,ctaker) combination
		for(int i=0;i<sol.size();i++){		
			ArrayList<Integer> temp1 = new ArrayList<Integer>();
			temp1.add(sol.get(i));
			temp1.add(solc.get(i));
			tempvc.add(temp1);
		}
		
		ArrayList<ArrayList<Integer>> tempv = new ArrayList<ArrayList<Integer>>();
		for(int j=0;j<tempvc.size();j++){
			if(!tempv.contains(tempvc.get(j))&&tempvc.get(j).get(0)!=10){
				tempv.add(tempvc.get(j));
			}
		}
			
		for(int k=0;k<tempv.size();k++){
			solVisit.add(tempv.get(k).get(0));
			solVisitCtaker.add(tempv.get(k).get(1));
		}
		
		//to handle number of ctaker limitation
		for(int l=0;l<solVisit.size();l++){
			if(!solSumVisit.contains(solVisit.get(l))){
				solSumVisit.add(solVisit.get(l));
				solSumVisitCtaker.add(Collections.frequency(solVisit,solVisit.get(l)));
			}
		}
		
		int temp2=0;
		for(int p=0;p<solSumVisit.size();p++){
			if(solSumVisitCtaker.get(p)>nc){
				temp2+=1;
			}
		}
		
		if(temp2==0){
			solSumCont=solVisitCtaker.size();	
		}
		else if(temp2>0){
			solSumCont=500000;
		}
		
		
		clearsolVec();
		clearsolBank();
		
		for(int y=0;y<sol.size();y++){
			solVec.add(sol.get(y));
			solCtaker.add(solc.get(y));
			solDay.add(sold.get(y));
		}
		
		for(int z=0;z<solb.size();z++){
			solBank.add(solb.get(z));
		}
		
		
		/*check point
		System.out.println();
		System.out.println("Evaluation evalsolcontinuity1");
		System.out.println("RESULT");
		System.out.println("solVisit "+solVisit);
		System.out.println("solVisitCtaker "+solVisitCtaker);
		System.out.println("solSumVisit "+solSumVisit);
		System.out.println("solSumVisitCtaker "+solSumVisitCtaker);
		System.out.println("sol "+solVec);
		System.out.println("solCtaker "+solCtaker);
		System.out.println("solDay "+solDay);
		
		System.out.println("solBank "+solBank);
		*/
		
		return solSumCont;
	}
	
	//evaluate number of uncovered visits: sol, solc, sold, solb
	public int evalsolUncov(ArrayList<Integer> vid, ArrayList<Integer> sol,ArrayList<Integer> solc,ArrayList<Integer> sold,ArrayList<Integer> solb, ArrayList<Integer> pr){
		clearsolCont();
		clearsolRoute();
		clearsolTime();
		
		int solSumUncov=0;
		
		for(int i=0;i<solb.size();i++){
			
			solSumUncov+=pr.get(vid.indexOf(solb.get(i)));	
			
			
		}
		
		
		clearsolVec();
		clearsolBank();
		
		for(int y=0;y<sol.size();y++){
			solVec.add(sol.get(y));
			solCtaker.add(solc.get(y));
			solDay.add(sold.get(y));
		}
		
		for(int z=0;z<solb.size();z++){
			solBank.add(solb.get(z));
		}
		
		/*check point
		System.out.println();
		System.out.println("Evaluation evalsoluncovered1");
		System.out.println("RESULT");
		System.out.println("sol "+solVec);
		System.out.println("solCtaker "+solCtaker);
		System.out.println("solDay "+solDay);
		
		System.out.println("solBank "+solBank);
		*/
		
		return solSumUncov;
	}
		
	//evaluate wage to pay, depending on the working time difference: ctaker ID, ctaker wage, sol, solc, sold, solb, time start (refer to gensolTimeSign), time end (refer to gensolTimeSign), end working time, number of days
	public double evalsolPay(ArrayList<Integer> cid, ArrayList<Double> wg, ArrayList<Integer> sol,ArrayList<Integer> solc,ArrayList<Integer> sold,ArrayList<Integer> solb,ArrayList<Double> ts, ArrayList<Double> te, double he, int nd){
		clearsolCont();
		clearsolRoute();
		clearsolTime();
			
		//prepare for indexing
		ArrayList<ArrayList<Integer>> tempsol = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<sol.size();i++){
			ArrayList<Integer> temp = new ArrayList<Integer>();
			temp.add(solc.get(i));
			temp.add(sold.get(i));
			tempsol.add(temp);
		}
		
		int k=0;
		for(int i=0;i<tempsol.size();i++){
			
			//start of solution can assign ctaker wage directly from working
			if(i==0){
				
				//compute ctaker wage based on the working duration
				if(ts.get(k)!=100000.0&&te.get(k)!=100000.0&&te.get(k)<=he&&ts.get(k)!=500000.0&&te.get(k)!=500000.0){
					solPay.add(((te.get(k)-ts.get(k))/60.0)*wg.get(cid.indexOf(solc.get(i))));	
				}
				
				//if any of the node is infeasible, impose penalty
				else if(ts.get(k)==100000.0||te.get(k)==100000.0||te.get(k)>he||te.get(k)==500000.0||te.get(k)==500000.0){
					solPay.add(100000.0);
				}
				
				/*check point
				System.out.println();
				System.out.println("Evaluation evalsolpay1");
				System.out.println("k "+k+" i "+(i));
				System.out.println(tempsol.get(i));
				System.out.println(ts.get(k));
				System.out.println(te.get(k));
				*/
			}
			
			else if(i>0&&!tempsol.get(i).equals(tempsol.get(i-1))){
					//if(k+1<ts.size()-1) 
					k+=1;
					
					if(ts.get(k)!=100000.0&&te.get(k)!=100000.0&&te.get(k)<=he&&ts.get(k)!=500000.0&&te.get(k)!=500000.0){   
						solPay.add(((te.get(k)-ts.get(k))/60.0)*wg.get(cid.indexOf(solc.get(i))));	
					}
					else if(ts.get(k)==100000.0||te.get(k)==100000.0||te.get(k)>he||te.get(k)==500000.0||te.get(k)==500000.0){
						solPay.add(100000.0);
					}
				
				/*check point
				System.out.println();
				System.out.println("Evaluation evalsolpay2");
				System.out.println("k "+k+" i "+(i));
				System.out.println(tempsol.get(i-1));
				System.out.println(tempsol.get(i));
				System.out.println(tempsol.get(i).equals(tempsol.get(i-1)));
				System.out.println(ts.get(k));
				System.out.println(te.get(k));
				*/
			}
		}
		
		double solSumPay=0.0;
		for(int l=0;l<solPay.size();l++){
			solSumPay+=solPay.get(l);
		}
		
		clearsolVec();
		clearsolBank();
		
		for(int y=0;y<sol.size();y++){
			solVec.add(sol.get(y));
			solCtaker.add(solc.get(y));
			solDay.add(sold.get(y));
		}
		
		for(int z=0;z<solb.size();z++){
			solBank.add(solb.get(z));
		}
		
		/*check point
		System.out.println();
		System.out.println("Evaluation evalsolpay3");
		System.out.println("RESULT");
		System.out.println("sol "+solVec);
		System.out.println("solCtaker "+solCtaker);
		System.out.println("solDay "+solDay);
		
		System.out.println("solBank "+solBank);
		*/
		
		return solSumPay;
	}
	
	//evaluate how close break time is with respect to minimum number of work hours before break: sol, solc, sold, solb, time break, minimum of work hours before break 
	public double evalsolBreak(ArrayList<Integer> sol,ArrayList<Integer> solc,ArrayList<Integer> sold,ArrayList<Integer> solb, ArrayList<Double> ts,ArrayList<Double> tbr, double hbr){
		clearsolCont();
		clearsolRoute();
		clearsolTime();
		
		//prepare indexing ctaker+day
		ArrayList<ArrayList<Integer>> tempidx = new ArrayList<ArrayList<Integer>>();
		for(int h=0;h<sol.size();h++){
			ArrayList<Integer> temp = new ArrayList<Integer>();
			temp.add(solc.get(h));
			temp.add(sold.get(h));
			tempidx.add(temp);
		}
		
		//if index changes, it will allow pointer j to move forward with respect to time start
		//remember that we only have break if we are not in depot
		int k=0;
		for(int i=0;i<sol.size();i++){
			if(i==0){
				k=0;
			}
			else if(i>0&&!tempidx.get(i).equals(tempidx.get(i-1))){	
				//if(k+1<ts.size()-1) 
				k+=1;
			}
			
			
			
			if(tbr.get(i)>0.0){
				solBreakGap.add(hbr-(tbr.get(i)-ts.get(k)));
				//System.out.println(i+" "+k+" "+" "+tbr.get(i)+" "+ts.get(k));
			}	
			else if(tbr.get(i)==0.0){
				solBreakGap.add(0.0);
			}
		}
		
		double solSumBreak=0.0;
		for(int l=0;l<solBreakGap.size();l++){
			solSumBreak+=solBreakGap.get(l);
		}
		
		clearsolVec();
		clearsolBank();
		
		for(int y=0;y<sol.size();y++){
			solVec.add(sol.get(y));
			solCtaker.add(solc.get(y));
			solDay.add(sold.get(y));
		}
		
		for(int z=0;z<solb.size();z++){
			solBank.add(solb.get(z));
		}
			
		/*check point
		System.out.println();
		System.out.println("Evaluation evalsolbreak1");
		System.out.println("RESULT");
		System.out.println("sol "+solVec);
		System.out.println("solCtaker "+solCtaker);
		System.out.println("solDay "+solDay);
		
		System.out.println("solBank "+solBank);
		*/
		
		return solSumBreak;
	}
	
	//get variables
	public ArrayList<Integer> getsolVisit(){
		ArrayList<Integer> dupl = new ArrayList<Integer>();
		for(int i=0;i<solVisit.size();i++){
			dupl.add(solVisit.get(i));
		}
		return dupl;
	}
	
	public ArrayList<Integer> getsolVisitCtaker(){
		ArrayList<Integer> dupl = new ArrayList<Integer>();
		for(int i=0;i<solVisitCtaker.size();i++){
			dupl.add(solVisitCtaker.get(i));
		}
		return dupl;
	}
	
	public ArrayList<Integer> getsolSumVisitCtaker(){
		ArrayList<Integer> dupl = new ArrayList<Integer>();
		for(int i=0;i<solSumVisitCtaker.size();i++){
			dupl.add(solSumVisitCtaker.get(i));
		}
		return dupl;
	}

	public ArrayList<Integer> getsolSumVisit(){
		ArrayList<Integer> dupl = new ArrayList<Integer>();
		for(int i=0;i<solSumVisit.size();i++){
			dupl.add(solSumVisit.get(i));
		}
		return dupl;
	}
	
	public ArrayList<Double> getsolDWork(){
		ArrayList<Double> dupl = new ArrayList<Double>();
		for(int i=0;i<solDWork.size();i++){
			dupl.add(solDWork.get(i));
		}
		return dupl;
	}
	
	public ArrayList<Double> getsolDWorkBreak(){
		ArrayList<Double> dupl = new ArrayList<Double>();
		for(int i=0;i<solDWorkBreak.size();i++){
			dupl.add(solDWorkBreak.get(i));
		}
		return dupl;
	}
	
	public ArrayList<Double> getsolDBreak(){
		ArrayList<Double> dupl = new ArrayList<Double>();
		for(int i=0;i<solDBreak.size();i++){
			dupl.add(solDBreak.get(i));
		}
		return dupl;
	}
	
	public ArrayList<Double> getsolDWait(){
		ArrayList<Double> dupl = new ArrayList<Double>();
		for(int i=0;i<solDWait.size();i++){
			dupl.add(solDWait.get(i));
		}
		return dupl;
	}
	
	public ArrayList<Integer> getsolSBreak(){
		ArrayList<Integer> dupl = new ArrayList<Integer>();
		for(int i=0;i<solSBreak.size();i++){
			dupl.add(solSBreak.get(i));
		}
		return dupl;
	}
	
	public ArrayList<Double> getsolTVisit(){
		ArrayList<Double> dupl = new ArrayList<Double>();
		for(int i=0;i<solTVisit.size();i++){
			dupl.add(solTVisit.get(i));
		}
		return dupl;
	}

	public ArrayList<Integer> getsolIdxCtaker(){
		ArrayList<Integer> dupl = new ArrayList<Integer>();
		for(int i=0;i<solIdxCtaker.size();i++){
			dupl.add(solIdxCtaker.get(i));
		}
		return dupl;
	}
	
	public ArrayList<Integer> getsolIdxDay(){
		ArrayList<Integer> dupl = new ArrayList<Integer>();
		for(int i=0;i<solIdxDay.size();i++){
			dupl.add(solIdxDay.get(i));
		}
		return dupl;
	}
	
	public ArrayList<Double> getsolTStart(){
		ArrayList<Double> dupl = new ArrayList<Double>();
		for(int i=0;i<solTStart.size();i++){
			dupl.add(solTStart.get(i));
		}
		return dupl;
	}
	
	public ArrayList<Double> getsolTEnd(){
		ArrayList<Double> dupl = new ArrayList<Double>();
		for(int i=0;i<solTEnd.size();i++){
			dupl.add(solTEnd.get(i));
		}
		return dupl;
	}

	public ArrayList<Double> getsolTBreak(){
		ArrayList<Double> dupl = new ArrayList<Double>();
		for(int i=0;i<solTBreak.size();i++){
			dupl.add(solTBreak.get(i));
		}
		return dupl;
	}
	
	public ArrayList<Double> getsolBreakGap(){
		ArrayList<Double> dupl = new ArrayList<Double>();
		for(int i=0;i<solBreakGap.size();i++){
			dupl.add(solBreakGap.get(i));
		}
		return dupl;
	}
	
	public ArrayList<Integer> getsolVec(){
		ArrayList<Integer> dupl = new ArrayList<Integer>();
		for(int i=0;i<solVec.size();i++){
			dupl.add(solVec.get(i));
		}
		return dupl;
	}
	
	public ArrayList<Integer> getsolCtaker(){
		ArrayList<Integer> dupl = new ArrayList<Integer>();
		for(int i=0;i<solCtaker.size();i++){
			dupl.add(solCtaker.get(i));
		}
		return dupl;
	}
	
	public ArrayList<Integer> getsolDay(){
		ArrayList<Integer> dupl = new ArrayList<Integer>();
		for(int i=0;i<solDay.size();i++){
			dupl.add(solDay.get(i));
		}
		return dupl;
	}
	
	public ArrayList<Integer> getsolBank(){
		ArrayList<Integer> dupl = new ArrayList<Integer>();
		for(int i=0;i<solBank.size();i++){
			dupl.add(solBank.get(i));
		}
		return dupl;
	}
	
	public ArrayList<Double> getsolDist(){
		ArrayList<Double> dupl = new ArrayList<Double>();
		for(int i=0;i<solDist.size();i++){
			dupl.add(solDist.get(i));
		}
		return dupl;
	}	

	public ArrayList<Double> getsolPay(){
		ArrayList<Double> dupl = new ArrayList<Double>();
		for(int i=0;i<solPay.size();i++){
			dupl.add(solPay.get(i));
		}
		return dupl;
	}	
	
	public ArrayList<Double> getsolTTravel(){
		ArrayList<Double> dupl = new ArrayList<Double>();
		for(int i=0;i<solTTravel.size();i++){
			dupl.add(solTTravel.get(i));
		}
		return dupl;
	}	
	
	//clear ArrayList
	public void clearsolVec(){
		if(solVec.size()>0){
			solVec.clear();
			solDay.clear();
			solCtaker.clear();	
		}
	}
	
	public void clearsolBank(){
		if(solBank.size()>0){
			solBank.clear();
		}
	}
	
	public void clearsolTime(){
		if(solTStart.size()>0){
			solTStart.clear();
			solTEnd.clear();
			solTBreak.clear();
			solBreakGap.clear();
			solIdxDay.clear();
			solIdxCtaker.clear();
		}
	}
	
	public void clearsolRoute(){
		if(solDWork.size()>0){
			solTVisit.clear();
			solTTravel.clear();
			solDWork.clear();
			solSBreak.clear();
			solDBreak.clear();
			solDWorkBreak.clear();
			solDWait.clear();
		}
		
		if(solDist.size()>0){
			solDist.clear();
			solPay.clear();
		}
	}
	
	public void clearsolCont(){
		if(solVisit.size()>0){
			solVisit.clear();
			solVisitCtaker.clear();
			solSumVisit.clear();
			solSumVisitCtaker.clear();
		}
			
	}
	
	//additional methods
	//to help in ALNS algorithm
	public ArrayList<Integer> copyListInt(ArrayList<Integer> input){
		ArrayList<Integer> dupl = new ArrayList<Integer>();
		for(int i=0;i<input.size();i++){
			dupl.add(input.get(i));
		}
		return dupl;
	}
	
	public int locateDepot(ArrayList<Integer> sol,ArrayList<Integer> solc,ArrayList<Integer> sold, ArrayList<Integer> dep){
		int idx=100000;
		
		//only if the input is depot
		if(dep.get(0)==10){
			ArrayList<Integer> temp1 = new ArrayList <Integer>();
			int c=dep.get(1);
			int d=dep.get(2);
			
			temp1.add(c);
			temp1.add(d);
			
			ArrayList<ArrayList<Integer>> tempcd = new ArrayList<ArrayList<Integer>>();
			
			//prepare for ctaker and day indexing
			for(int i=0;i<sol.size();i++){
				ArrayList<Integer> temp2 = new ArrayList <Integer>();
				temp2.add(solc.get(i));
				temp2.add(sold.get(i));
				tempcd.add(temp2);
				
			}
			
			/*check point
			System.out.println("tempcd "+tempcd);
			System.out.println("temp1 "+temp1);
			*/
			
			//if we know the ctaker+day exists already, retrieve the earliest position -- as ctaker will always starts from 10
			if(tempcd.contains(temp1)){
				idx=tempcd.indexOf(temp1);
			}
			
			//if we don't see that such ctaker+day combination exists...
			else if(!tempcd.contains(temp1)){
				idx=0;

				//...get the ctaker (indexOf will give index for the first similar ctaker found)...
				int idxc=solc.indexOf(c);
				
				//...then...
				if(idxc!=-1){
					
					//...start to locate from that index...
					idx=idxc;
					
					//...keep adding the index if the day number is less than the one we're looking for...
					while(solc.get(idx)<=c&&sold.get(idx)<d){
						if(idx<sold.size()-1){
							idx+=1;	
						}
						
						//...near the end of the array, should stop before exceed the bound
						else{
							idx+=1;
							break;
						}
						
					}
				}
				
				//else start to locate from index zero
				else if(idxc==-1){
					idx=0;
					while(solc.get(idx)<c){
						if(idx<solc.size()-1){
							idx+=1;	
						}
						else{
							idx+=1;
							break;
						}
					}
				}
				
			}
			
		}
		
		else if(dep.get(0)!=10){
			throw new IllegalArgumentException("Unmatch input");
		}
		return idx;
	}
	
	public ArrayList<Integer> locateVisits(ArrayList<Integer> cid,ArrayList<Integer> sol,ArrayList<Integer> solc,ArrayList<Integer> sold, ArrayList<Integer> vs,int nd){
		int m=cid.size();
		ArrayList<Integer> idx=new ArrayList<Integer>();
		
		//we can only locate visits if we have complete depots in the array
		if(Collections.frequency(sol,10)==(m*nd)){
			ArrayList<Integer> temp1 = new ArrayList <Integer>();
			
			//get ctaker and day from input visit
			int c=vs.get(1);
			int d=vs.get(2);
			
			temp1.add(c);
			temp1.add(d);
			
			//prepare ctaker+day indexing
			ArrayList<ArrayList<Integer>> tempcd = new ArrayList<ArrayList<Integer>>();
			
			
			for(int i=0;i<sol.size();i++){
				ArrayList<Integer> temp2 = new ArrayList <Integer>();
				temp2.add(solc.get(i));
				temp2.add(sold.get(i));
				tempcd.add(temp2);
			}
			
			//locate the earliest and latest index found
			int a = tempcd.indexOf(temp1);
			int b = tempcd.lastIndexOf(temp1);
			
			idx.add(a+1);
			idx.add(b+1);
		}
		else if(Collections.frequency(sol,10)!=(m*nd)){
			throw new IllegalArgumentException("Unmatch input");
		}
		return idx;
	}
}

