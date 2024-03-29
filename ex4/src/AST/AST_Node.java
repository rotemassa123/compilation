package AST;
import SYMBOL_TABLE.SYMBOL_TABLE;
import TEMP.*;
import TYPES.*;
import IR.IR;
import IR.*;

import java.util.ArrayList;
import java.util.List;

public abstract class AST_Node
{
	/*******************************************/
	/* The serial number is for debug purposes */
	/* In particular, it can help in creating  */
	/* a graphviz dot format of the AST ...    */
	/*******************************************/
	public int SerialNumber;

	/***********************************************/
	/* The default message for an unknown AST node */
	/***********************************************/

	public int line;

	public void PrintMe()
	{
		System.out.print("AST NODE UNKNOWN\n");
	}
	
	public abstract TYPE SemantMe() throws SemanticException;

	public abstract TEMP IRme();

	public boolean checkAssign(TYPE_VAR assignVar, TYPE expType, AST_Node exp)
	{
		/*  Case 1: var = new exp (Only for class or array) or var = exp (need to be from same type)
		 *  Case 2: var.id = new exp (Only for class or array) or var = exp (need to be from same type)
		 *  Case 3: var[exp] = new exp (Only for class or array) or var = exp (need to be from same type)
		 *  SemantMe on var will return TYPE_VAR */

		/* new _: valid only on Arrays and Classes */
		if (exp instanceof AST_NEW_EXP_NEW_TYPE_EXP)
		{
			if (assignVar.type instanceof TYPE_ARRAY)
			{
				/* A = new type[exp] -> check A.arrayType = type*/
				if (((AST_NEW_EXP_NEW_TYPE_EXP) exp).exp != null)
				{
					TYPE arrayType = ((TYPE_ARRAY) assignVar.type).arrayType;
					// Allow inheritance
					if (arrayType instanceof TYPE_CLASS)
					{
						if (expType instanceof TYPE_CLASS && ((TYPE_CLASS) expType).inheritsFrom((TYPE_CLASS) arrayType))
						{
							return true;
						}
					}
					else if (arrayType == expType)
					{
						return true;
					}
				}
				// else- A = new type (Not allows because A is an array)
			}

			else if (assignVar.type instanceof TYPE_CLASS)
			{
				/* A = new B -> check A.type = B OR B inherits from A.type*/
				if (((AST_NEW_EXP_NEW_TYPE_EXP) exp).exp == null)
				{
					if (expType instanceof TYPE_CLASS && ((TYPE_CLASS) expType).inheritsFrom((TYPE_CLASS) assignVar.type))
						return true;
				}
			}
			// In every other assignments (string or int)- can't be a new
		}
		else
		{// exp instanceof AST_EXP (no new)
			// Only for class or array
			if (expType instanceof TYPE_NIL)
			{
				if (assignVar.type instanceof TYPE_ARRAY || assignVar.type instanceof TYPE_CLASS)
					return true;
			}
			else
			{ // expType != null
				/* In this case: assignVar = something with type expType*/
				if (assignVar.type instanceof TYPE_CLASS)
				{/* className a = b -> b is from TYPE_CLASS and equal or inheritance from the className*/
					if (expType instanceof TYPE_CLASS && ((TYPE_CLASS) expType).inheritsFrom((TYPE_CLASS) assignVar.type))
						return true;
				}
				/* arrayName a = b -> b is from type array and equal to arrayName*/
				else if (assignVar.type instanceof TYPE_ARRAY)
				{
					if (expType instanceof TYPE_ARRAY)
					{
						if (assignVar.type == expType)
							return true;
					}
				}
				/* string/int a*/
				else
				{
					if (assignVar.type == expType)
						return true;
				}
			}
		}
		return false;
	}

	public TYPE_FUNCTION CheckCallToFunc(String id, AST_VAR var, AST_LIST<AST_EXP> l) throws SemanticException {
		TYPE typeFound;

		/* Find type */
		if (var == null) {
			typeFound = SYMBOL_TABLE.getInstance().find(id);
		}
		else {
			TYPE typeOfVar = ((TYPE_VAR)var.SemantMe()).type;
			if (!typeOfVar.isClass()) {
				throw new SemanticException(this);
			}
			TYPE_CLASS typeClassOfVar = (TYPE_CLASS)typeOfVar;
			typeFound = SYMBOL_TABLE.getInstance().findInInheritance(id, typeClassOfVar);
		}

		/* Check if type was found */
		if (typeFound == null) {
			throw new SemanticException(this);
		}

		/* Check if it's a method */
		if (!(typeFound instanceof TYPE_FUNCTION)) {
			// There is no way a function with this name exists,
			// because this type wouldn't have been inserted to this symbol table in the first place
			throw new SemanticException(this);
		}

		/* Check call */
		CallToFuncMatchesFunc((TYPE_FUNCTION)typeFound, l); // We've already made sure typeFound is of TYPE_FUNC

		return (TYPE_FUNCTION) typeFound;
	}

	private void CallToFuncMatchesFunc(TYPE_FUNCTION func, AST_LIST<AST_EXP> l) throws SemanticException{
		//if our func has no params and the called func has params - throw error
		if((l == null && func.params != null) || (func.params == null && l != null)){
			throw new SemanticException(this);
		}

		//if they are both null all is good darling
		if(l != null && func.params != null){
			/* Check if parameters match expected parameters */
			TYPE_LIST params = (TYPE_LIST) l.SemantMe(); // l.SemantMe() supposed to return TYPE_LIST
			if (!func.params.equalsForValidatingGivenParams(params)) {
				throw new SemanticException(this);
			}
		}
	}

	public List<TEMP> build_param_list(AST_LIST ast_param_list)
	{
		AST_LIST pointer = ast_param_list;
		TEMP param_temp;
		List<TEMP> temp_list = new ArrayList<>();

		while (pointer != null)
		{
			param_temp = pointer.head.IRme();
			temp_list.add(param_temp);
			pointer = pointer.tail;
		}

		return temp_list;
	}

}
