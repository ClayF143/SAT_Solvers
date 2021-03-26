package Solvers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class SAT_Solvers
{
	private int topC, nodesExpanded; // c is the fitness of a model, nodes is for DPLL
	
	public SAT_Solvers()
	{
		topC = 0; 
		nodesExpanded = 0;
		// these just help keep track of data so that I don't have to have functions return their values
	}
	
	public ArrayList<int []> readClausesFromFile(String inputFilePath)
	{
		// returns a list of arrays, where each array represents a clause.
		// input is the filepath
		
		ArrayList<int []> clauses = new ArrayList<int []>();
		try
		{
			File myFile = new File(inputFilePath);
			Scanner myReader = new Scanner(myFile);
			
			String firstLine = myReader.nextLine();
			firstLine = myReader.nextLine();
			String[] splitFirstLine = firstLine.split(" ", 4);
			int numClauses = Integer.valueOf(splitFirstLine[3]);
			int numVars = Integer.valueOf(splitFirstLine[2]);
			
			
			String data = "";
			while (myReader.hasNextLine())
				data += myReader.nextLine();
			myReader.close();
			
			String [] clausesData = data.split(" 0");
			for(String clause : clausesData)
			{
				String [] strClause = clause.split(" ");
				int [] intClause = new int [strClause.length];
				for(int i = 0; i < strClause.length; i++)
				{
					intClause[i] = Integer.valueOf(strClause[i]);
				}
				clauses.add(intClause);
			}
			
			if(clauses.size() != numClauses)
			{
				System.out.println("Error with clauses, size is not the same as the size specified at the start of the file");
			}
		}
		catch (FileNotFoundException e)
		{
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		return clauses;
	}
	
	private int clauseTF(int [] clause, HashMap<Integer, Boolean> model)
	{
		// given a clause and a model, will return an int representing whether the clause is satisfied
		// 1 for sat, -1 for unsat, 0 for unknown
		
		boolean unknown = false;
		for(int var : clause)
		{
			Boolean b = model.get(Math.abs(var));
			if(b == null)
				unknown = true;
			else if((b.booleanValue() && var > 0) || !b.booleanValue() && var < 0)
				return 1;
		}
		if(unknown)
			return 0;
		return -1;
	}
	
	private int modelTF(ArrayList<int []> clauses, HashMap<Integer, Boolean> model)
	{
		// determines if the model satisfies the clauses using clauseTF
		// again, 1 for sat, -1 for unsat, and 0 for unknown
		boolean unknown = false;
		for(int [] clause : clauses)
		{
			if(clauseTF(clause, model) == -1)
			{
				return -1;
			}
			if(clauseTF(clause, model) == 0)
			{
				unknown = true;
			}
		}
		if(unknown)
			return 0;
		return 1;
	}
	
	private int evaluateModel(ArrayList<int []> clauses, HashMap<Integer, Boolean> model)
	{
		// returns how many clauses are satisfied with the given model
		
		int c = 0;
		for(int [] clause : clauses)
			if(clauseTF(clause, model) == 1)
				c++;
		return c;
	}
	
	public HashMap<Integer, Boolean> DPLL(ArrayList<int []> clauses, ArrayList<Integer> symbols, HashMap<Integer, Boolean> model)
	{
		// the first search I have, determines if a model exists that satisfies the clauses with a dfs and some pruning
		// recursive, takes the clauses, a list of the variables in the clauses that are unassigned, and the model.
		
		nodesExpanded++;
		
		// first, return true if all clauses are true and return false if any clause is false
		int isModSat = modelTF(clauses, model);
		if(isModSat == 1)
		{
			return model;
		}
		if(isModSat == -1)
		{
			return null;
		}
		if(symbols.isEmpty())
		{
			System.out.println("Error: Symbol set is empty but model returns unknown");
			for(Integer key : model.keySet())
			{
				System.out.println(String.valueOf(key) + " " + String.valueOf(model.get(key)) + ", ");
			}
		}
		
		// if unknown, find pure symbols and unit clauses
		int pureSymbol = findPureSymbol(clauses, symbols, model);
		if(pureSymbol != 0)
		{
			if(pureSymbol > 0)
				model.put(Math.abs(pureSymbol), Boolean.valueOf(true));
			else
				model.put(Math.abs(pureSymbol), Boolean.valueOf(false));
			
			pureSymbol = Math.abs(pureSymbol);
			symbols.remove(symbols.indexOf(pureSymbol));
			return DPLL(clauses, symbols, model);
		}
		
		int unitClauseSymbol = findUnitClause(clauses, symbols, model);
		if(unitClauseSymbol != 0)
		{
			if(unitClauseSymbol > 0)
				model.put(Math.abs(unitClauseSymbol), Boolean.valueOf(true));
			else
				model.put(Math.abs(unitClauseSymbol), Boolean.valueOf(false));
			
			unitClauseSymbol = Math.abs(unitClauseSymbol);
			symbols.remove(symbols.indexOf(unitClauseSymbol));
			return DPLL(clauses, symbols, model);
		}
		
		// if you're out of those then actually do the dfs
		int first = symbols.get(0);
		symbols.remove(0);
		ArrayList<Integer> mySyms = (ArrayList<Integer>) symbols.clone();
		HashMap<Integer, Boolean> copy = (HashMap<Integer, Boolean>) model.clone();
		model.put(Integer.valueOf(first), Boolean.valueOf(true));
		copy.put(Integer.valueOf(first), Boolean.valueOf(false));
		HashMap<Integer, Boolean> temp = DPLL(clauses, symbols, model);
		if(temp != null)
			return temp;
		return DPLL(clauses, mySyms, copy);
	}
	
	private int findPureSymbol(ArrayList<int []> clauses, ArrayList<Integer> symbols, HashMap<Integer, Boolean> model)
	{
		// finds any symbol in the clauses that appears consistently, ie always x1 rather than -x1
		// returns the positive of that symbol if it always appears positive and negative if it always appears negative
		int [] posNeg = new int [symbols.size()];
		for(int [] clause : clauses)
		{
			for(int c : clause)
			{
				int i = symbols.indexOf(Integer.valueOf(Math.abs(c)));
				if(i != -1)
				{
					if(c > 0 && posNeg[i] >= 0)
						posNeg[i] = 1;
					else if(c < 0 && (posNeg[i] == 0 || posNeg[i] == -1))
						posNeg[i] = -1;
					else
						posNeg[i] = -2;
				}
			}
		}
		for(int i = 0; i < symbols.size(); i++)
			if(posNeg[i] != -2)
				return symbols.get(i).intValue() * posNeg[i];
		return 0;
		
	}
	
	private int findUnitClause(ArrayList<int []> clauses, ArrayList<Integer> symbols, HashMap<Integer, Boolean> model)
	{
		// a unit clause exists if every symbol in a clause is the same, including positivity and negativity
		// if one is found, returns the symbol in that clause, including sign
		for(int [] clause : clauses)
		{
			boolean b = true;
			int sym = clause[0];
			for(int s : clause)
			{
				if(s != sym)
					b = false;
			}
			if(b)
				return sym;
		}
		return 0;
	}
	
	private int findRandFalseClauseIndex(ArrayList<int []> clauses, HashMap<Integer, Boolean> model)
	{
		// returns the index of a random false clause
		// loops infinitely if there are no false clauses
		int rand = (int) (Math.random() * clauses.size());
		if(clauseTF(clauses.get(rand), model) == -1)
			return rand;
		return findRandFalseClauseIndex(clauses, model);
	}
	
	
	public HashMap<Integer, Boolean> walkSAT(ArrayList<int []> clauses, double p, int maxFlips)
	{
		/*
		 I noticed with this method that often it will come very close to an answer but fail to find a solution
		 seemingly it gets stuck in a local minimum, so something that can help is to start over from a different 
		 starting point, rerandomize it and try again.  Hence this try loop.
		
		 This method starts with a randomized model, then based on a paramatarized probability, decides to either
		 pick a random variable in a random false clause to flip, or to flip the variable that results in the 
		 highest number of satisfied clauses.  It continues to flip until it times out with a paramatarized maxflip counter
		 */
		int tries = 5;
		while(tries != 0)
		{
			tries--;
			
			// randomize every symbol in model
			HashMap<Integer, Boolean> model = new HashMap<Integer, Boolean>();
			for(int [] clause : clauses)
			{
				for(int symbol : clause)
				{
					symbol = Math.abs(symbol);
					if(! model.containsKey(symbol))
					{
						double rand = Math.random();
						Boolean value = Boolean.valueOf(false);
						if(rand > .5)
							value = Boolean.valueOf(true);
						model.put(symbol, value);
					}
				}
			}
			
			// start flipping variables
			for(int i = 0; i < maxFlips; i++)
			{	
				int myC = evaluateModel(clauses, model);
				if(myC > topC)
					topC = myC;
				
				// if model is good then stop
				if(modelTF(clauses, model) == 1)
				{
					return model;
				}
				
				int [] clause = clauses.get(findRandFalseClauseIndex(clauses, model));
				if(clauseTF(clause, model) != -1)
				{
					System.out.println("Error: finding random false clauses doesn't always find clauses that are false");
					return null;
				}
				if(Math.random() < p)
				{
					// flip a random symbol in the false clause
					int key = Math.abs(clause[(int) Math.random() * clause.length]);
					flipVal(model, Integer.valueOf(key));
				}
				else
				{
					// flip the symbol that maximizes satisfied clauses in model after that flip
					Integer bestKey = Integer.valueOf(0);
					int maxC = Integer.valueOf(0);
					for(Integer key : model.keySet())
					{
						flipVal(model, key);
						int currC = evaluateModel(clauses, model);
						flipVal(model, key);
						if(currC > maxC)
						{
							bestKey = key;
							maxC = currC;
						}
					}
					
					flipVal(model, bestKey);
				}
			}
		}
		return null;
	}
	
	private void flipVal(HashMap<Integer, Boolean> model, Integer key)
	{
		// flips the value of the key from false to true or from true to false.
		if(model.get(key).booleanValue())
			model.replace(key, Boolean.valueOf(false));
		else
			model.replace(key, Boolean.valueOf(true));
	}
	
	private ArrayList<Integer> initialSymbols(ArrayList<int []> clauses)
	{
		// an expensive way of getting a list of integers from 1 to the number of variables
		ArrayList<Integer> syms = new ArrayList<Integer>();
		for(int [] clause : clauses)
			for(int c : clause)
				if(!syms.contains(Integer.valueOf(Math.abs(c))))
					syms.add(Integer.valueOf(Math.abs(c)));
		return syms;
	}
	
	public static void main(String [] args)
	{
		String current = "";
		try
		{
			current = new java.io.File( "." ).getCanonicalPath();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		String tests = current + "\\src\\A3_tests";
		System.out.println(current);
		System.out.println(tests);
		
		
		File directoryPath = new File(tests);
		File [] filesList = directoryPath.listFiles();
		
		SAT_Solvers s = new SAT_Solvers();
		int [] dpllNodeData = new int [filesList.length];
		long [] dpllTimeData = new long [filesList.length];
		int [][] walkSatCData = new int [filesList.length][10];
		long [][] walkSatTimeData = new long [filesList.length][10];
		for(int i = 0; i < filesList.length; i++)
		{
			
			s.nodesExpanded = 0;
			ArrayList<int []> clauses = s.readClausesFromFile(filesList[i].getAbsolutePath());
			ArrayList<Integer> symbols = s.initialSymbols(clauses);
			HashMap<Integer, Boolean> result = new HashMap<Integer, Boolean>();
			
			// do dpll
			long start = System.currentTimeMillis();
			result = s.DPLL(clauses, symbols, result);
			long stop = System.currentTimeMillis();
			
			// record dpll data
			dpllNodeData[i] = s.nodesExpanded;
			dpllTimeData[i] = stop - start;
			
			// print solution
			if(result != null)
			{
				System.out.print("DPLL: " + filesList[i].getName() + " SAT ");
				for(Integer key: result.keySet())
				{
					if(!result.get(key))
						System.out.print("-");
					System.out.print(String.valueOf(Integer.valueOf(key)) + " ");
				}
				System.out.println();
			}
			else
				System.out.println("DPLL: " + filesList[i].getName() + " UNSAT ");
			
			for(int j = 0; j < 10; j++)
			{
				s.topC = 0;
				
				// do walkSat
				start = System.currentTimeMillis();
				result = s.walkSAT(clauses, .5, 1000);
				stop = System.currentTimeMillis();
				
				// record walkSat data
				walkSatTimeData[i][j] = stop - start;
				walkSatCData[i][j] = s.topC;
				
				if(result != null)
				{
					System.out.print("WalkSAT: " + filesList[i].getName() + " SAT ");
					for(Integer key: result.keySet())
					{
						if(!result.get(key))
							System.out.print("-");
						System.out.print(String.valueOf(Integer.valueOf(key)) + " ");
					}
					System.out.println();
				}
				else
					System.out.println("WalkSAT: " + filesList[i].getName() + " UNSAT " + s.topC);
			}
			
			
		}
		
		// print dpll data
		System.out.println("dpll time");
		for(long l : dpllTimeData)
			System.out.print(String.valueOf(l) + " ");
		System.out.println("\ndpll nodes");
		for(int nodeD : dpllNodeData)
			System.out.print(String.valueOf(nodeD) + " ");
		
		// print walkSAT data
		long [] averageTime = new long [walkSatTimeData.length];
		double [] averageC = new double [walkSatCData.length];
		
		System.out.println("\n\nwalkSat average time");
		for(int i = 0; i < averageTime.length; i++)
		{
			long sum = 0;
			for(long c : walkSatTimeData[i])
				sum += c;
			averageTime[i] = sum / 10;;
			System.out.print(sum / 10);
			System.out.print(" ");
		}
		
		System.out.println("\n\nwalkSat all times");
		for(long [] trial : walkSatTimeData)
		{
			for(long t : trial)
			{
				System.out.print(String.valueOf(t) + " ");
			}
			System.out.println();
		}
		
		System.out.println("\n\nwalkSat average best C");
		for(int i = 0; i < averageC.length; i++)
		{
			double sum = 0;
			for(int c : walkSatCData[i])
				sum += c;
			averageC[i] = sum/10;
			System.out.print(sum/10);
			System.out.print(" ");
		}
		
		System.out.println("\n\nwalkSat all best c");
		for(int [] trial : walkSatCData)
		{
			for(int t : trial)
			{
				System.out.print(String.valueOf(t) + " ");
			}
			System.out.println();
		}
		
		
	}
}
