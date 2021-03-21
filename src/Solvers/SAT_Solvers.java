package Solvers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class SAT_Solvers
{
	private ArrayList<int []> clauses;
	
	public SAT_Solvers(String inputFilePath)
	{
		clauses = new ArrayList<int []>();
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
	}
	
	public boolean DPLL(ArrayList<int []> clauses, ArrayList<Integer> symbols, HashMap<Integer, Boolean> model)
	{
		// return true if all clauses are true and return false if any clause is false
		boolean allTrue = true;
		for(int [] clause : clauses)
		{
			if(clauseTF(clause, model) != 1)
			{
				allTrue = false;
			}
			if(clauseTF(clause, model) == -1)
			{
				return false;
			}
		}
		if(allTrue)
			return true;
		
		// otherwise find pure symbols and unit clauses
		int pureSymbol = findPureSymbol(clauses, symbols, model);
		if(pureSymbol != 0)
		{
			if(pureSymbol > 0)
			{
				pureSymbol = Math.abs(pureSymbol);
				model.put(pureSymbol, Boolean.valueOf(true));
				symbols.remove(symbols.indexOf(pureSymbol));
				return DPLL(clauses, symbols, model);
			}
			else
			{
				model.replace(pureSymbol, Integer.valueOf(0));
				// more here
				return DPLL();
			}
		}
		
		// if you're out of those then actually do the dfs
		
		
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
			else if((b.booleanValue() && var < 0) || !b.booleanValue() && var > 0)
				return -1;
		}
		if(unknown)
			return 0;
		return 1;
	}
	
	private int findPureSymbol(ArrayList<int []> clauses, ArrayList<Integer> symbols, HashMap<Integer, Boolean> model)
	{
		
	}
	
	private int findUnitClauses(ArrayList<int []> clauses, ArrayList<Integer> symbols, HashMap<Integer, Boolean> model)
	{
		
	}
}
