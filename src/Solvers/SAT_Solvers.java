package Solvers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

public class SAT_Solvers
{
	public ArrayList<int []> readClausesFromFile(String inputFilePath)
	{
		ArrayList<int []> clauses = new ArrayList<int []>();
		try
		{
			File myFile = new File(inputFilePath);
			Scanner myReader = new Scanner(myFile);
			
			String firstLine = myReader.nextLine();
			String[] splitFirstLine = firstLine.split(" ", 4);
			int numClauses = Integer.valueOf(splitFirstLine[2]);
			int numVars = Integer.valueOf(splitFirstLine[3]);
			
			String data = "";
			while (myReader.hasNextLine())
				data += myReader.nextLine();
			myReader.close();
			
			String [] clausesData = data.split("0");
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
	
	// returns -1 if the clause is false,
	// 0 if it is unknown, and 1 if true
	private int clauseTF(int [] clause, HashMap<Integer, Boolean> model)
	{
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
	
	public boolean DPLL(ArrayList<int []> clauses, ArrayList<Integer> symbols, HashMap<Integer, Boolean> model)
	{
		// return true if all clauses are true and return false if any clause is false
		int isModSat = modelTF(clauses, model);
		if(isModSat == 1)
			return true;
		if(isModSat == -1)
			return false;
		
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
		HashMap<Integer, Boolean> copy = (HashMap<Integer, Boolean>) model.clone();
		model.put(Integer.valueOf(first), Boolean.valueOf(true));
		copy.put(Integer.valueOf(first), Boolean.valueOf(false));
		return DPLL(clauses, symbols, model) || DPLL(clauses, symbols, copy);
	}
	
	private int findPureSymbol(ArrayList<int []> clauses, ArrayList<Integer> symbols, HashMap<Integer, Boolean> model)
	{
		// first, create a map of every symbol and whether it is positive or negative
		// if ever you see a symbol that's already in the map and not consistent, give it a value of 0
		HashMap<Integer, Integer> posNeg = new HashMap<Integer, Integer>();
		for(int [] clause : clauses)
		{
			for(int sym : clause)
			{
				if(!posNeg.containsKey(sym))
				{
					if(sym > 0)
						posNeg.put(Integer.valueOf(Math.abs(sym)), Integer.valueOf(1));
					else
						posNeg.put(Integer.valueOf(Math.abs(sym)), Integer.valueOf(-1));
				}
				else if((posNeg.get(Math.abs(sym)).equals(Integer.valueOf(1)) && sym < 0) 
					|| (posNeg.get(Math.abs(sym)).equals(Integer.valueOf(-1)) && sym > 0))
						posNeg.replace(Math.abs(sym), Integer.valueOf(0));
			}
		}
		
		// return any symbol that doesn't have a value of 0
		for(Entry<Integer, Integer> entry : posNeg.entrySet())
			if(! entry.getValue().equals(Integer.valueOf(0)))
				return entry.getKey();
		
		// if there are none, return failure
		return 0;
	}
	
	private int findUnitClause(ArrayList<int []> clauses, ArrayList<Integer> symbols, HashMap<Integer, Boolean> model)
	{
		// a unit clause exists if every symbol in a clause is the same, including positivity and negativity
		// if one is found, returns the symbol in that clause
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
		int rand = (int) Math.random() * clauses.size();
		if(clauseTF(clauses.get(rand), model) == -1)
			return rand;
		return findRandFalseClauseIndex(clauses, model);
	}
	
	
	public HashMap<Integer, Boolean> walkSAT(ArrayList<int []> clauses, long p, int maxFlips)
	{
		// randomize every symbol in model
		HashMap<Integer, Boolean> model = new HashMap<Integer, Boolean>();
		for(int [] clause : clauses)
		{
			for(int symbol : clause)
			{
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
		
		for(int i = 0; i < maxFlips; i++)
		{
			// if model is good then stop
			if(modelTF(clauses, model) == 1)
				return model;
			
			int [] clause = clauses.get(findRandFalseClauseIndex(clauses, model));
			
			if(Math.random() < p)
			{
				// flip a random symbol in the false clause
				int key = clause[(int) Math.random() * clause.length];
				Boolean val = model.get(key);
				if(val.booleanValue())
					model.replace(Integer.valueOf(key), Boolean.valueOf(false));
				else
					model.replace(Integer.valueOf(key), Boolean.valueOf(true));
			}
			else
			{
				// flip the symbol that maximizes satisfied clauses in model after that flip
			}
		}
		
		return null;
	}
}
