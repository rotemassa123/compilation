package AST;

public class AST_EXP_BINOP extends AST_EXP
{
	int OP;
	public AST_EXP left;
	public AST_EXP right;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AST_EXP_BINOP(AST_EXP left, AST_EXP right, int OP) {
		SerialNumber = AST_Node_Serial_Number.getFresh();
		String sOP="";
		switch(OP) {
			case 0:
				sOP = "+";
				break;
			case 1:
				sOP = "-";
				break;
			case 2:
				sOP = "*";
				break;
			case 3:
				sOP = "/";
				break;
			case 4:
				sOP = "<";
				break;
			case 5:
				sOP = ">";
				break;
			case 6:
				sOP = "=";
				break;
		}
		System.out.print(String.format("====================== exp -> exp %s exp\n", sOP));
		this.left = left;
		this.right = right;
		this.OP = OP;
	}
	
	/*************************************************/
	/* The printing message for a binop exp AST node */
	/*************************************************/
	public void PrintMe() {
		String sOP="";
		
		/*********************************/
		/* CONVERT OP to a printable sOP */
		/*********************************/
		switch(OP) {
			case 0:
				sOP = "+";
				break;
			case 1:
				sOP = "-";
				break;
			case 2:
				sOP = "*";
				break;
			case 3:
				sOP = "/";
				break;
			case 4:
				sOP = "<";
				break;
			case 5:
				sOP = ">";
				break;
			case 6:
				sOP = "=";
				break;
		}
		/*************************************/
		/* AST NODE TYPE = AST BINOP EXP */
		/*************************************/
		System.out.print("AST NODE EXP BINOP\n");

		/**************************************/
		/* RECURSIVELY PRINT left + right ... */
		/**************************************/
		if (left != null) left.PrintMe();
		if (right != null) right.PrintMe();
		
		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AST_GRAPHVIZ.getInstance().logNode(
			SerialNumber,
			String.format("BINOP(%s)",sOP));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (left  != null) AST_GRAPHVIZ.getInstance().logEdge(SerialNumber,left.SerialNumber);
		if (right != null) AST_GRAPHVIZ.getInstance().logEdge(SerialNumber,right.SerialNumber);
	}
}