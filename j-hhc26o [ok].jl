#n+2 nodes

using JuMP, Gurobi
using MathProgBase
m = Model(solver=GurobiSolver())


siteName = ["DEP_START", "A", "B", "C", "D", "DEP_END","P"]
ctakerName = ["U1","U2","U3"]
dayName =["Mon","Tue","Wed","Thu","Fri"]

#input node data
dist = [0 1 sqrt(2) 1 sqrt(5) 0 0;    #s(ij) = distance between nodes
        1 0 1 sqrt(2) 2 1 0;
        sqrt(2) 1 0 1 1 sqrt(2) 0;
        1 sqrt(2) 1 0 sqrt(2) 1 0;
        sqrt(5) 2 1 sqrt(2) 0 sqrt(5) 0;
        0 1 sqrt(2) 1 sqrt(5) 0 0;
        0 0 0 0 0 0 0]

tTravel =[0 0.25 0.35 0.25 0.25 0 0;   #t(ij)=
           0.25 0 0.25 0.35 0.25 0.25 0;
           0.35 0.25 0 0.25 0.25 0.35 0;
           0.25 0.35 0.25 0 0.25 0.25 0;
           0.25 0.25 0.25 0.25 0 0.25 0;
           0 0.25 0.35 0.25 0.25 0 0;
           0 0 0 0 0 0 0];

#input clients data
tWin = [0 4; # a(0) and b(0) = time window for each node
        1 3; # a(i) and b(i) = time window for each node
        1 2;
        2 3;
        3 3;
        0 4;
        0 4]

dRequest = [0 0 0 0 0; # h(i,d) = if patient i requests visit on day d
            1 1 1 1 1; # H(i) = sum w(i,d) over the week
            0 1 0 1 0;
            0 1 0 1 0;
            0 0 1 1 0;
            0 0 0 0 0;
            0 0 0 0 0]

dRequestSum = [0, 5, 2, 2, 2, 0, 0]

reqSkill = [0, 1, 3, 2, 2, 0, 0]  # r(i) = qualification level requirement for patient i
reqPrior = [0, 1, 3, 2, 4, 0, 0] # delta(i) = priority of patient i

#input caretaker data
hWork = tWin[1,2]-tWin[1,1]   # T = standard working duration
hWorkBreak = 1.5 # B = minimum working time before break
numCtaker = 100   # N = maximum number of different caretakers assigned to a patient

tService = [0 0 0;
            0.1 0.05 0.13;
            0.125 0.125 1000;
            0.8 0.75 1.25;
            0.8 0.75 1.25;
            0 0 0;
            0 0 0]


ctakerSkill = [4, 3, 2]  # Q(k) = qualification for caretaker k
ctakerWage = [15, 10, 5] # c(k) = wage per hour for caretaker k
ctakerBreak = [0.25, 0.25, 0.25]

capacity = 100 
n = length(siteName)
v = length(ctakerName)
d = length(dayName)

sites = collect(1:(n-1))
clients = collect(2:(n-2))
ctakers = collect(1:v)
days = collect(1:d)
sitesbr = collect(1:n)

@variable(m, x[sitesbr, sitesbr, ctakers, days], Bin) # binary, hourly assignment
@variable(m, rho[clients, ctakers, days], Bin) # binary, daily assignment
@variable(m, tau[ctakers, days], Bin) # binary, daily assignment
@variable(m, z[clients, ctakers], Bin) # binary, weekly assignment
@variable(m, y[clients, days], Bin) # binary, caretaker  assignment
@variable(m, br[ctakers, days], Bin) # binary, break node assignment
@variable(m, tv[clients,ctakers,days] >= 0) #variable, arrival time for clients
@variable(m, tb[ctakers,days] >= 0) #variable, arrival time for breaknode
@variable(m, ts[ctakers,days] >= 0) #variable, start time for each caretaker
@variable(m, te[ctakers,days] >= 0) #variable, end time for each caretaker
@variable(m, gb[ctakers,days] >= 0) #variable, gap break time for each caretaker

wObj=[100/12,100/55, 100/16, 10000/6, 100/215 ,10/9]

#eq1, eq2, eq3, eq4, eq5: objective function sets [ok]
@objective(m, Min, wObj[1]*sum{tTravel[i,j]*x[i,j,k,l], i in sites, j in sites, k in ctakers, l in days} 
+ wObj[2]*sum{dist[i,j]*x[i,j,k,l], i in sites, j in sites, k in ctakers, l in days}
+ wObj[3]*sum{z[i,k], i in clients, k in ctakers} 
+ wObj[4]*sum{y[i,l]*reqPrior[i], i in clients, l in days}
+ wObj[5]*sum{ctakerWage[k]*(te[k,l]-ts[k,l]), k in ctakers, l in days}
+ wObj[6]*sum{((hWorkBreak*br[k,l])-gb[k,l]), k in ctakers, l in days})


#eq6: each patient is either visited or not
for i in clients
    for l in days
        if dRequest[i,l] > 0
            @constraint(m, sum{x[i,j,k,l], j in sites, k in ctakers; i!=j} + y[i,l] >= 1 ) 
        end
    end
end

#eq7: number of visit to be fulfilled 
for i in clients
    @constraint(m, sum{rho[i,k,l], k in ctakers, l in days} <= dRequestSum[i] )
end

#eq8: caretaker daily assignment refers to day-specific request
for i in clients
    for k in ctakers
        for l in days
            @constraint(m, rho[i,k,l] <= dRequest[i,l])
        end
    end
end

#eq9: caretaker assignment refer to the qualification level
for i in clients
    for k in ctakers
        @constraint(m, reqSkill[i]*z[i,k] <= ctakerSkill[k])
    end
end
    
#eq10, eq11, eq12: caretaker daily assignment also refers to eq9
for i in clients
    for k in ctakers
        for l in days
            @constraint(m, rho[i,k,l] <= z[i,k])
            @constraint(m, rho[i,k,l] <= tau[k,l])
        end
    end
end

#eq13: caretaker hourly assignment refers to daily assignment
for i in clients
    for k in ctakers
        for l in days
            @constraint(m, sum{x[i,j,k,l], j in sites;i!=j} <= rho[i,k,l])
        end
    end
end

#eq14, eq15, eq16: arc assignment for special cases: impossible arcs and arcs for non-visiting caretaker
for i in clients
    for k in ctakers
        for l in days
            @constraint(m, x[i,1,k,l] == 0)
            @constraint(m,x[(n-1),1,k,l] == 0)
            @constraint(m, x[1,(n-1),k,l] <= 1-tau[k,l])
        end
    end
end

#eq17: each caretaker should start at depot
for k in ctakers
    for l in days
        @constraint(m, sum{x[1,j,k,l], j in sites;j!=1} == 1 )         
    end
end
  
#eq18: each caretaker should end at depot 
for k in ctakers
    for l in days 
        @constraint(m, sum{x[i,(n-1),k,l], i in sites;i!=(n-1)} == 1 ) 
    end
end

#eq19: flow conservation constraint
for h in clients
    for k in ctakers
        for l in days
            @constraint(m, sum{x[i,h,k,l], i in sites;i!=h} - sum{x[h,j,k,l], j in sites;j!=h} == 0)
        end
    end
end

#eq20: subtour elimination constraint
for i in sites
    for j in sites
        for k in ctakers
            for l in days
                if i!=j
                    @constraint(m, x[i,j,k,l] + x[j,i,k,l] <= 1)
                end
            end
        end
    end
end

#eq21: tighten bound for z[i,k]
for i in clients
    for k in ctakers
        @constraint(m, sum{x[i,j,k,l], j in sites, l in days;i!=j} >= z[i,k])
    end
end

#eq22: get duration from start time to break time, to be exploited as soft constraint in adjusting break time (see 6th component in the objective function)
for k in ctakers
    for l in days
        bigM10=tWin[1,2]
        @constraint(m, tb[k,l]-ts[k,l]>=gb[k,l]-bigM10*(1-br[k,l]))
        @constraint(m, gb[k,l]<=bigM10*br[k,l])
    end
end


#eq23: ensure that break time occurs after caregiver reaches his first visit
for j in clients
    for k in ctakers
        for l in days
        bigM11=tWin[1,2]-tWin[1,1]
        @constraint(m, tb[k,l] - ts[k,l] >= tTravel[1,j]*x[1,j,k,l] - bigM11*(1-br[k,l]))
        end
    end
end


#eq24: assignment of arc entering break node refers to break node assignment in eq35
for k in ctakers
    for l in days
        @constraint(m, sum{x[i,n,k,l], i in clients} == br[k,l]) 
    end
end

#eq25: caretaker that enters break node should leave break node as well
for i in clients
    for k in ctakers
        for l in days
            @constraint(m, x[n,i,k,l] - x[i,n,k,l] == 0)
        end
    end 
end

#eq26: break node assignment should be based on assignment to associated clients nodes
for i in clients
    for k in ctakers
        for l in days
            @constraint(m, x[n,i,k,l] - sum{x[j,i,k,l], j in sites; i!=j} <= 0)
        end
    end
end

#eq27: arrival time at node j is after caretaker finishes his service at node i and travels to node j 
#for eq24, eq25, eq26 --> node i <= node p <= node j
for i in clients
    for j in clients
        for k in ctakers
            for l in days
                if i!=j       
                    bigM1=tWin[1,2]-tWin[1,1]+ tTravel[i,j] + tService[i,k]    
                    @constraint(m, tv[i,k,l] + tTravel[i,j] + tService[i,k] - bigM1*(1 - x[i,j,k,l]) <= tv[j,k,l])
                end
            end
        end
    end
end

#eq28: arrival time at node j also considers break node arrival time from node j (break first) 
for j in clients
    for k in ctakers
        for l in days
            bigM2=tWin[1,2]-tWin[1,1]+ ctakerBreak[k]
            @constraint(m, tb[k,l] + ctakerBreak[k] - bigM2*(1 - x[j,n,k,l]) <= tv[j,k,l])
        end
    end
end

#eq29: arrival time of node j is less than arrival time of break node
for i in clients
    for j in clients
        for k in ctakers
            for l in days
                if i!=j
                    bigM3=tWin[1,2]-tWin[1,1]+ tTravel[i,j] + tService[i,k] 
                    @constraint(m, tv[i,k,l] + tTravel[i,j] + tService[i,k] - tb[k,l] <= bigM3*(2 - x[i,j,k,l] - x[j,n,k,l]))
                end
            end
        end
    end
end

#eq30: starting time for caretaker k 
for i in clients
    for k in ctakers
        for l in days
            if i!=1
                bigM4=tWin[1,2]-tWin[1,1]+ tTravel[1,i]
                @constraint(m, ts[k,l] + tTravel[1,i] - bigM4*(1-x[1,i,k,l]) <= tv[i,k,l])
            end
        end
    end
end

#eq31: ending time for caretaker k 
for i in clients
    for k in ctakers
        for l in days
            if i!=(n-1)
                bigM5=tWin[1,2]-tWin[1,1]+ tTravel[i,(n-1)] + tService[i,k]
                @constraint(m, tv[i,k,l] + tTravel[i,(n-1)] + tService[i,k] - bigM5*(1 - x[i,(n-1),k,l]) <= te[k,l])
            end
        end
    end
end

#eq32: eliminating idle time
for k in ctakers
    for l in days
        @constraint(m, sum{tTravel[i,j]*x[i,j,k,l], i in sites, j in sites} 
        + br[k,l]*ctakerBreak[k]
        + sum{tService[i,k]*rho[i,k,l], i in clients} 
        <= te[k,l] - ts[k,l]) 
    end
end

#eq33, eq34: working duration for each caretaker
for k in ctakers
    for l in days
        @constraint(m, ts[k,l] >= tau[k,l]*tWin[1,1])
        @constraint(m, te[k,l] <= tau[k,l]*tWin[1,2])
    end
end

#eq35: set up time window for clients 
for i in clients
    for k in ctakers
        for l in days
            @constraint(m, tWin[i,1]*sum{x[j,i,k,l], j in sites; i!=j} <= tv[i,k,l])
            @constraint(m, tWin[i,2]*sum{x[j,i,k,l], j in sites; i!=j} >= tv[i,k,l])
        end
    end
end

#eq36, eq37: set up arrival time for break node assignment 
for k in ctakers
    for l in days
        bigM6=tWin[1,2]
        @constraint(m, tb[k,l] <= hWorkBreak + ts[k,l]) 
        @constraint(m, tb[k,l] <= bigM6*br[k,l])
    end
end

#eq38: break node assignment refers to minimum working hours accumulated
for k in ctakers
    for l in days
        bigM7=max(0,tWin[1,2]-tWin[1,1] - hWorkBreak)
        @constraint(m, sum{tTravel[i,j]*x[i,j,k,l], i in sites, j in sites} 
        + sum{tService[i,k]*rho[i,k,l], i in clients} - hWorkBreak <= bigM7*br[k,l]) 
        @constraint(m, sum{tTravel[i,j]*x[i,j,k,l], i in sites, j in sites} 
        + sum{tService[i,k]*rho[i,k,l], i in clients} - hWorkBreak <= bigM7*sum{x[i,n,k,l], i in clients}) 
    end
end

#eq39, eq40: lower bound for tb[k,l]
for i in clients
    for k in ctakers
        for l in days
            bigM8=tWin[1,2]-tWin[1,1]+ctakerBreak[k]
            bigM9=max(0,tWin[1,2]-tWin[1,1]-hWorkBreak)
            @constraint(m, tb[k,l] >= tv[i,k,l] - ctakerBreak[k]*x[i,n,k,l]  - bigM8*(1 - x[i,n,k,l]))
        end
    end
end



#eq41: imposing standard working duration, excluding time spent for break
for k in ctakers
    for l in days
        @constraint(m, te[k,l] - ts[k,l]  <= hWork)
    end
end

#eq42: maximum number of different caretaker allowed per clients
for i in clients
    @constraint(m, sum{z[i,k], k in ctakers} <= numCtaker)
end

status = solve(m)
println("solution :",getobjectivevalue(m))
#print(m) 
println("solve time:",MathProgBase.getsolvetime(m))


#initiate empty arrays to collect result
xArr = Array((Int64),n,n,v,d)
rhoArr = Array((Int64),(n-3),v,d)
tauArr = Array((Int64),v,d)
zArr = Array((Int64),(n-3),v)
yArr = Array((Int64),(n-3),d)

brArr = Array((Int64),v,d)
tvArr = Array((Float64),(n-3),v,d)
tbArr = Array((Float64),v,d)
tsArr = Array((Float64),v,d)
teArr = Array((Float64),v,d)
gbArr = Array((Float64),v,d)


#println("x :",getvalue(x))

#need to write result to a csv file
#write result by variable
fname = "C:\\Users\\NoviaLW\\Documents\\Dissertation\\2-software\\Julia\\code [ilp]\\result.csv"
fout = open(fname,"w")
write(fout,"RESULT FOR ILP HOME CARE ROUTING PROBLEM \n")
write(fout,"solution :,")
writecsv(fout,getobjectivevalue(m))
write(fout,"\n")
write(fout,"solve time :,")
writecsv(fout,getsolvetime(m))



#write(fout,"\n")
write(fout,"xArray \n")
write(fout,"sitesbr,sitesbr,ctakers,days,xArr,xArr.dist,xArr.tTravel \n")

for i in sitesbr
    for j in sitesbr
        for k in ctakers
            for l in days
                xArr[i,j,k,l] = getvalue(x[i,j,k,l])
                write(fout,join((i,j,k,l,xArr[i,j,k,l],xArr[i,j,k,l]*dist[i,j],xArr[i,j,k,l]*tTravel[i,j]),","),"\n")
            end
        end
    end
end


#println("rho :",getvalue(rho))

write(fout,"\n")
write(fout,"rhoArray \n")
write(fout,"clients,ctakers,days,rhoArr \n")

for i in clients
    for k in ctakers
        for l in days
            rhoArr[i-1,k,l] = getvalue(rho[i,k,l])
            write(fout,join((i,k,l,rhoArr[i-1,k,l]),","),"\n")
        end
    end
end


#println("tau :",getvalue(tau))

write(fout,"\n")
write(fout,"tauArray \n")
write(fout,"ctakers,days,tauArr \n")

for k in ctakers
    for l in days
        tauArr[k,l] = getvalue(tau[k,l])
        write(fout,join((k,l,tauArr[k,l]),","),"\n")
    end
end


println("z :",getvalue(z))

write(fout,"\n")
write(fout,"zArray \n")
write(fout,"clients,ctakers,zArr \n")

for i in clients
    for k in ctakers
        zArr[i-1,k]=getvalue(z[i,k])
        write(fout,join((i,k,zArr[i-1,k]),","),"\n")
    end
end


println("y :",getvalue(y))

write(fout,"\n")
write(fout,"yArray \n")
write(fout,"clients,days,yArr,yArr.reqPrior \n")

for i in clients
    for l in days
        yArr[i-1,l]=getvalue(y[i,l])
        write(fout,join((i,l,yArr[i-1,l],yArr[i-1,l]*reqPrior[i]),","),"\n")
    end
end

#println("br :",getvalue(br))

write(fout,"\n")
write(fout,"brArray \n")
write(fout,"ctakers,days,brArr,gbArr,hWorkBreak - (tb-ts),hWorkBreak-gb , \n")


for k in ctakers
    for l in days
        brArr[k,l] = getvalue(br[k,l])
        tbArr[k,l] = getvalue(tb[k,l])
        tsArr[k,l] = getvalue(ts[k,l])
        gbArr[k,l] = getvalue(gb[k,l])
        write(fout,join((k,l,brArr[k,l],gbArr[k,l],(brArr[k,l]*hWorkBreak)-(tbArr[k,l]-tsArr[k,l]),((hWorkBreak*brArr[k,l])-gbArr[k,l])),","),"\n")
    end
end


#println("tv :",getvalue(tv))

write(fout,"\n")
write(fout,"tvArray \n")
write(fout,"clients,ctakers,days,tvArr \n")

for i in clients
    for k in ctakers
        for l in days
            tvArr[i-1,k,l] = getvalue(tv[i,k,l])
           
            write(fout,join((i,k,l,tvArr[i-1,k,l]),","),"\n")
        end
    end
end


#println("tb :",getvalue(tb))

write(fout,"\n")
write(fout,"tbArray \n")
write(fout,"ctakers,days,tbArr \n")


for k in ctakers
    for l in days
        tbArr[k,l] = getvalue(tb[k,l])
        write(fout,join((k,l,tbArr[k,l]),","),"\n")
    end
end


#println("ts :",getvalue(ts))

write(fout,"\n")
write(fout,"tsArray \n")
write(fout,"ctakers,days,tsArr \n")


for k in ctakers
    for l in days
        tsArr[k,l] = getvalue(ts[k,l])
        write(fout,join((k,l,tsArr[k,l]),","),"\n")
    end
end


#println("te :",getvalue(te))

write(fout,"\n")
write(fout,"teArray \n")
write(fout,"ctakers,days,teArr,ctakerWage.(ts-te) \n")

for k in ctakers
    for l in days
        tsArr[k,l] = getvalue(ts[k,l])
        teArr[k,l] = getvalue(te[k,l])
        write(fout,join((k,l,teArr[k,l],ctakerWage[k]*(teArr[k,l]-tsArr[k,l])),","),"\n")
    end
end

f2=0
f1=0
for i in sites
    for j in sites
        for k in ctakers
            for l in days
                f2+=tTravel[i,j]*getvalue(x[i,j,k,l])
                f1+=dist[i,j]*getvalue(x[i,j,k,l])
            end
        end
    end
end

f3=0
for i in clients
    for k in ctakers
        f3+=getvalue(z[i,k])
    end
end

f4=0
for i in clients
    for l in days
        f4+=getvalue(y[i,l])*reqPrior[i]
    end
end

f5=0
f6=0
for k in ctakers
    for l in days
        f5+=ctakerWage[k]*(getvalue(te[k,l])-getvalue(ts[k,l]))
        f6+=(hWorkBreak*getvalue(br[k,l])) - getvalue(gb[k,l])
    end
end

print("function1 ",f1," ",f1*wObj[1],"\n")
print("function2 ",f2," ",f2*wObj[2],"\n")
print("function3 ",f3," ",f3*wObj[3],"\n")
print("function4 ",f4," ",f4*wObj[4],"\n")
print("function5 ",f5," ",f5*wObj[5],"\n")
print("function6 ",f6," ",f6*wObj[6],"\n")

#write result by routes and assignment 
routes=Any[]

for k in ctakers
    for l in days
        r=Int[]
        t=10000
        r=push!(r,1)     
        idx=1
        for i in sitesbr
        
            for j in sitesbr
                if i!=j  
                    if(i==r[idx])&&(xArr[i,j,k,l]!=0)&&(j!=7)
                        r=push!(r,j)
                        idx=idx+1 
                    elseif(xArr[i,j,k,l]!=0)&&(j==7)
                        t=i
                    end
                end
            end
            if(r[idx]>=6)
                routes=push!(routes,[[k;l];r;t])
                break
            end 
        end
        #println("routes ",routes)
    end
end

write(fout,"\n")
idx=0

for k in ctakers
    for l in days
        idx=idx+1
        if (length(routes[idx])-1>4)
            write(fout,"Schedule for caretaker ",join(k,",")," on day ",join(l,","),"\n")
            for i in collect(3:(length(routes[idx]))-1)
                n=routes[idx][i]
                if (n == 1)
                    write(fout,"Start : ",",",join(tsArr[k,l],","),"\n")
                elseif(n == 6)
                    write(fout,"End : ",",",join(teArr[k,l],","),"\n")
                else
                    write(fout,"Client ",join(n,",")," : ",",",join(tvArr[n-1,k,l],","),"\n")
                end
            end

            if tbArr[k,l] > 0
                write(fout,"With break","\n")
                write(fout,"Client ",join(routes[idx][length(routes[idx])],",")," : ",",",join(tbArr[k,l],","),"\n")
            end
            write(fout,"\n")
        end
        
    end
    
end

close(fout)