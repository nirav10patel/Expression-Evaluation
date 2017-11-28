package apps;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression 
{

	/**
	 * Expression to be evaluated
	 */
	String expr;                
    
	/**
	 * Scalar symbols in the expression 
	 */
	ArrayList<ScalarSymbol> scalars;   
	
	/**
	 * Array symbols in the expression
	 */
	ArrayList<ArraySymbol> arrays;
    
    /**
     * String containing all delimiters (characters other than variables and constants), 
     * to be used with StringTokenizer
     */
    public static final String delims = " \t*+-/()[]";
    
    /**
     * Initializes this Expression object with an input expression. Sets all other
     * fields to null.
     * 
     * @param expr Expression
     */
    public Expression(String expr)
    {
        this.expr = expr;
    }

    /**
     * Populates the scalars and arrays lists with symbols for scalar and array
     * variables in the expression. For every variable, a SINGLE symbol is created and stored,
     * even if it appears more than once in the expression.
     * At this time, values for all variables are set to
     * zero - they will be loaded from a file in the loadSymbolValues method.
     */
    public void buildSymbols() 
    {
    	
    	scalars = new ArrayList<ScalarSymbol>();
		arrays = new ArrayList<ArraySymbol>();
		String var = "";
		String expression = "";
		
		if (this.expr.charAt(this.expr.length() - 1) != ')');
    	{
    		this.expr = '(' + this.expr + ')';
    	}
    	
    	for (int inc = 0; inc < this.expr.length(); inc++)
    	{
    		if (this.expr.charAt(inc) != ' ')
    		{
    			expression = expression + this.expr.charAt(inc);
    		}
    	}
		
    	this.expr = expression;
    	//System.out.println("Built: " + expression);
    	
		for (int i = 0; i < expression.length(); i++)
		{
			if (Character.isLetter(expression.charAt(i)))
			{
				while (Character.isLetter(expression.charAt(i)) && i < expression.length())
				{
					var = var + expression.charAt(i);
					i++;
				}
				//System.out.println("var: " + var);
				
				if (expression.charAt(i) == '[' )
				{
					 ArraySymbol ArrayVal = new ArraySymbol (var);
					 arrays.add(ArrayVal);
				}
				else
				{
					ScalarSymbol ScalarVal = new ScalarSymbol (var);
					scalars.add(ScalarVal);
				}
			}
			var = "";
		}
    }
    
    /**
     * Loads values for symbols in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     */
    public void loadSymbolValues(Scanner sc) 
    throws IOException 
    {
        while (sc.hasNextLine()) 
        {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String sym = st.nextToken();
            ScalarSymbol ssymbol = new ScalarSymbol(sym);
            ArraySymbol asymbol = new ArraySymbol(sym);
            int ssi = scalars.indexOf(ssymbol);
            int asi = arrays.indexOf(asymbol);
            if (ssi == -1 && asi == -1) 
            {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) 
            { // scalar symbol
                scalars.get(ssi).value = num;
            } 
            else 
            { // array symbol
            	asymbol = arrays.get(asi);
            	asymbol.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) 
                {
                    String tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    asymbol.values[index] = val;              
                }
            }
        }
    }
    
    
    /**
     * Evaluates the expression, using RECURSION to evaluate subexpressions and to evaluate array 
     * subscript expressions.
     * 
     * @return Result of evaluation
     */
    public float evaluate() 
    {
    	//System.out.println("Start evaluate: " + this.expr);
    	String expression = this.expr;
    	String piece = "";
    	String temp = "";
    	int counter = 0;
    	int marker = 0;
    	int start = 0;
    	int end = 0;
    	String val;
    	boolean isArray = false;
    	for (int i = 0; i < expression.length(); i++)
    	{
    		if (expression.charAt(i) == '(')
    		{
    			counter++;
    		}
    		if (expression.charAt(i) == '[')
    		{
    			counter++;
    		}
    		if (expression.charAt(i) == ')' || expression.charAt(i) == ']')
    		{
    			if (expression.charAt(i) == ')')
    			{
    				marker = i;
        			break;
    			}
    			
    			else
    			{
    				isArray = true;
    				marker = i;
        			break;
    			}
    		}
    	}
    	
    	for (int j = 0; j < expression.length(); j++)
    	{
    		if (isArray)
    		{
    			if (expression.charAt(j) == '(' || expression.charAt(j) == '[')
    			{
    				counter--;
    			}
    			if (counter == 0)
    			{
    				piece = expression.substring(j, marker + 1);
    				//System.out.println("piece: " + piece);
    				//System.out.println();
    				start = j;
    				end = marker + 1;
    				break;
    			}
    		}
    		
    		else
    		{
    			if (expression.charAt(j) == '(' || expression.charAt(j) == '[')
    			{
    				counter--;
    			}
    			if (counter == 0)
    			{
    				piece = expression.substring(j, marker + 1);
    				//System.out.println("piece: " + piece);
    				//System.out.println();
    				start = j;
    				end = marker + 1;
    				break;
    			}
    		}
    	}
    	boolean ready = false;
    	boolean multiply = true;
    	Stack<String> ops = new Stack<String>();
    	Stack<String> holder = new Stack<String>();
    	val = eval(piece, ready, multiply, ops, holder);
		
    	//System.out.println(expression);
    	
    	String arr;
    	String arr1;
    	String arr2;
    	int [] box;
    	int tempnum;
    	
    	if (arrays.size() != 0)
    	{
    		for (int m = 0; m < arrays.size(); m++)
    		{
    			arr1 = arrays.get(m).name;
    			for (int n = 0; n < arrays.size(); n++)
    			{
    				arr2 = arrays.get(n).name;
    				if(arr1.equals(arr2))
    				{
    					arrays.set(n, arrays.get(m));
    				}
    			}
    		}
    	}
    	if (isArray)
    	{
    		for (int k = 0; k < start; k++)
    		{
    			if (!Character.isLetter(expression.charAt(k)))
    			{
    				temp = "";
    			}
    			temp = temp + expression.charAt(k);
    		}
    		
    		if (!Character.isLetter(temp.charAt(0)))
    		{
    			temp = temp.substring(1, temp.length());
    		}
    		
    		for (int l = 0; l < arrays.size(); l++)
    		{
    			arr = arrays.get(l).name;
    			if (temp.equals(arr))
    			{
    				box = arrays.get(l).values;
    				float x = Float.parseFloat(val);
    				tempnum =  (int) x;
    				tempnum = box[tempnum];
    				val = Integer.toString(tempnum);
    				break;
    			}
    		}
    		
    		
    		this.expr = expression.substring(0,start-temp.length()) + val + expression.substring(end, expression.length());
        	expression = this.expr;
    		
    	}
    	
    	else
    	{
    		this.expr = expression.substring(0,start) + val + expression.substring(end, expression.length());
            expression = this.expr;
    	}
    	
    	//System.out.println("End Expression = " + expression);
    	if (expression.charAt(0) == '(')
    	{
    		return evaluate();
    	}
    	else
    	{
    		return Float.parseFloat(expression);
    	}
    	
    }

    private String eval(String expr, boolean ready, boolean multiply, Stack<String> ops, Stack<String> holder)
    {
    	//System.out.println("Recursive start: " + expr);
    	
    	
    	if (expr.charAt(0) == '(' || expr.charAt(0) == '[')
    	{
    		expr = expr.substring(1,expr.length()-1);
    	}
    	if (expr.charAt(expr.length()-1) == ')' || expr.charAt(expr.length()-1) == ']')
    	{
    		expr = expr.substring(0,expr.length()-2);
    	}
    	
    	
    	//System.out.println("EXPRESSION NOW: " + expr);
    	
    	
    	if (!ready)
    	{
    		String tempstring;
    		boolean isNeg = false;
    		
    		StringTokenizer xp = new StringTokenizer(expr, delims);
    		while (xp.hasMoreTokens())
    		{
        		if (expr.charAt(0) == '-')
        		{
        			isNeg = true;
        		}
    			if (isNeg)
        		{
        			tempstring = '-' + xp.nextToken();
        			
        			//System.out.println("Token: " + tempstring);
        			
        			holder.push(tempstring);
        			expr = expr.substring(tempstring.length(), expr.length());
        			if (expr.length() > 0)
        			{
        				tempstring = Character.toString(expr.charAt(0));
        				
        				//System.out.println("Token: " + tempstring);
        				
            			holder.push(tempstring);
            			expr = expr.substring(1, expr.length());
        			}
        			isNeg = false;
        		}
        		else
        		{
        			String otherstring = "";
        			tempstring = xp.nextToken();
        			if (Character.isLetter(tempstring.charAt(0)))
        			{
        				for (int i = 0; i < scalars.size(); i++)
        				{
        					String tempScalarcheck = "";
        					String tempScalar = scalars.get(i).name;
        					for (int j = 0; j < tempScalar.length(); j++)
        					{
        						if (tempScalar.charAt(j) == '=')
        						{
        							break;
        						}
        						tempScalarcheck = tempScalarcheck + tempScalar.charAt(j);
        					}
        					tempScalar = tempScalarcheck;
        					otherstring = tempScalar;
        					
        					if (tempScalar.equals(tempstring))
        					{
        						int tempint = scalars.get(i).value;
        						tempstring = Integer.toString(tempint);
        						break;
        					}
        				}
        			}
        			
        			//System.out.println("Token: " + tempstring);
        			
        			holder.push(tempstring);
        			if (otherstring.length() != 0)
        			{
        				tempstring = otherstring;
        				otherstring = "";
        			}
        			expr = expr.substring(tempstring.length(), expr.length());
        			if (expr.length() > 0)
        			{
        				tempstring = Character.toString(expr.charAt(0));
        				
        				//System.out.println("Token: " + tempstring);
        				
            			holder.push(tempstring);
            			expr = expr.substring(1, expr.length());
        			}
        		}
    		}
    		while (!holder.isEmpty())
        	{
        		ops.push(holder.pop());
        	}
        	
        	ready = true;
    	}
    	
    	
    	
    	if (ops.size() == 1)
    	{
    		return ops.pop();
    	}
    	
    	float num1 = 0;
    	float num2 = 0;
    	if (multiply)
    	{
    		while (!ops.isEmpty())
        	{
        		String value = ops.pop();
        		if (value.equals("*") || value.equals("/"))
        		{
        			if (value.equals("*"))
        			{
        				if (ops.peek() == "-")
        				{
        					ops.pop();
        					num2 = Float.parseFloat(ops.pop()) * -1;
        				}
        				else
        				{
        					num2 = Float.parseFloat(ops.pop());
        				}
        				num1 = Float.parseFloat(holder.pop());
        				String result = Float.toString(num1*num2);
        				ops.push(result);
        				
        				while(!holder.isEmpty())
        				{
        					ops.push(holder.pop());
        				}
        				break;
        			}
        			
        			if (value.equals("/"))
        			{
        				if (ops.peek() == "-")
        				{
        					ops.pop();
        					num2 = Float.parseFloat(ops.pop()) * -1;
        				}
        				else
        				{
        					num2 = Float.parseFloat(ops.pop());
        				}
        				num1 = Float.parseFloat(holder.pop());
        				String result = Float.toString(num1/num2);
        				ops.push(result);
        				
        				while(!holder.isEmpty())
        				{
        					ops.push(holder.pop());
        				}
        				break;
        			}
        		
        		}
        		holder.push(value);
        	}
        	
        	if (!holder.isEmpty())
        	{
        		multiply = false;
        		while (!holder.isEmpty())
        		{
        			ops.push(holder.pop());
        		}
        	}
    	}
    	
    	if (!multiply)
    	{
    		while (!ops.isEmpty())
        	{
        		String value = ops.pop();
        		if (value.equals("+") || value.equals("-"))
        		{
        			if (value.equals("+"))
        			{
        				if (ops.peek() == "-")
        				{
        					ops.pop();
        					num2 = Float.parseFloat(ops.pop()) * -1;
        				}
        				else
        				{
        					num2 = Float.parseFloat(ops.pop());
        				}
        				num1 = Float.parseFloat(holder.pop());
        				String result = Float.toString(num1+num2);
        				ops.push(result);
        				while(!holder.isEmpty())
        				{
        					ops.push(holder.pop());
        				}
        				break;
        			}
        			
        			if (value.equals("-"))
        			{
        				if (ops.peek() == "-")
        				{
        					ops.pop();
        					num2 = Float.parseFloat(ops.pop()) * -1;
        				}
        				else
        				{
        					num2 = Float.parseFloat(ops.pop());
        				}
        				num1 = Float.parseFloat(holder.pop());
        				String result = Float.toString(num1-num2);
        				ops.push(result);
        				
        				while(!holder.isEmpty())
        				{
        					ops.push(holder.pop());
        				}
        				break;
        			}
        		}
        		holder.push(value);
        	}
    	}
    	
    	
    	
    	if (ops.size() == 1)
    	{
    		return ops.pop();
    	}
    	
    	else
    	{
    		String finishop = "done";
    		return eval(finishop,ready,multiply,ops,holder);
    	}
    	
    }
    /**
     * Utility method, prints the symbols in the scalars list
     */
    public void printScalars() 
    {
        for (ScalarSymbol ss: scalars) 
        {
            System.out.println(ss);
        }
    }
    
    /**
     * Utility method, prints the symbols in the arrays list
     */
    public void printArrays() 
    {
    		for (ArraySymbol as: arrays) 
    		{
    			System.out.println(as);
    		}
    }

}