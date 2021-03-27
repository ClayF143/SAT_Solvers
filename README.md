# SAT_Solvers

A3 Report
Clay Fike
CSC 372 Artificial Intelligence
3/26/21

DPLL
DPLL is a complete, backtracking search used to find some solution to a cnf statement, proving that statement to be satisfiable.  A model in a DPLL search is a possible solution, a set of values to each of the variables in the cnf statement.  DPLL is, at its core, a depth first search where for each node, one of these variables is assigned a value.  What makes DPLL different is the use of two patterns that it makes use of in cnf statements.  If a statement has any variables in a pure form, i.e. a variable that is always represented as a positive literal or always as a negative literal, then that variable can be assigned true if positive and false if negative.  If any clause has only 1 variable, i.e. a clause in the form of aj v aj v aj … then that variable can be assigned a value of true if positive and false if negative.  These two patterns can significantly decrease the amount of time required for the search.  In my code, the recursive method has the cnf statement, the unassigned variables, and a model as parameters.  It first determines if the model can satisfy the statement in its current state, then finds any pure symbols, then finds any unit clauses, then continues the dfs.


WalkSat
WalkSat is an incomplete local search algorithm used to find some solution to a cnf statement, proving that statement to be satisfiable.  The first thing walkSat does is randomly assign values to each of the variables in the statement.  Once that is done, it will enter a loop in which, based on some probability passed as a parameter, it will decide to either find a random false clause, given the current model, pick a random variable in that clause, and flip its value from true to false or vice versa, or it will flip the variable that results in the model that has the most number of true clauses after that flip.  It repeats this decision until it either finds a solution or hits a ceiling on the number of flips it can make, which is given as a parameter.  After seeing this search run, I realized that often the algorithm will get stuck on a solution that solves every clause but one or two.  Seemingly, this is because the algorithm got stuck in a local minimum.  A tactic that makes this less common is starting over with a newly randomized model after a set amount of flips, and I implemented it.


Data
Time for DPLL in 20 variable statements:
23 19 8 36 13 15 2 25 9 26 13 42 14 13 0 23
Time for DPLL in 40 variable statements:
2563 8251 196 20711 6884 11234 1340 32428 233 9805 8327 3759 25082 13087 955 3414
Nodes explored in 20 variable statements:
927 2671 1356 6327 2268 1656 266 4459 1557 3539 2325 9223 2687 2447 36 3713
Nodes explored in 40 variable statements:
198671 682685 17720 1727131 561772 1003843 111675 2666719 19961 829533 687798 299629 2437098 1077715 79662 281799

Average WalkSat time per statement in 20 variable statements:
64 97 4 97 15 94 56 96 1 86 24 89 5 95 1 92 96
Average WalkSat time per statement in 40 variable statements:
301 299 302 178 304 177 299 172 297 167 300 250 296 251 300 
Average Best Model in 20 variable statements:
83.8 83.0 84.0 83.0 84.0 83.0 83.5 83.0 84.0 82.9 83.7 83.0 84.0 83.0 84.0 83.0
Average Best Model in 40 variable statements:
167.9 166.9 167.1 166.9 167.6 167.0 167.6 166.9 167.7 167.0 167.6 167.0 167.2 166.6 167.2 166.7 

From the data, we can see that as the variables double, walkSat’s time increases about 2 - 5 times as much as it was.  Compareabley, DPLL goes from 2 digits to 4 or 5.  While both of these algorithms seem to have exponential time or worse, based on this increase, DPLL fares significantly worse than walkSat as the complexity of the problem increases.  We can also see that WalkSat consistently finds a model that almost satisfies the statement, regardless as to whether or not that statement is actually satisfiable, which, as I’ve said, implies that there are local minima in these large cnf statements that it can get stuck in.

Also, for walkSat, I used a probability of .5 and a maxTurns of 1000, and the algorithm randomizes the model 5 times before it actually gives up.


What I learned
One thing I learned is the appeal in incomplete algorithms.  WalkSat doesn’t always find the correct solution, but it usually will, especially if you run it 10 times on the same statement.  DPLL, on the other hand, always gives you a solution if one exists, but it takes so much longer than WalkSat, and I imagine this is only more true as the number of clauses and variables increase, to the point that DPLL might not even be conceivable, let alone whether or not it’s preferable.
