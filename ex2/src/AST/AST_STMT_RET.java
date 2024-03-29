package AST;

public class AST_STMT_RET extends AST_STMT {
    public AST_EXP exp;

    public AST_STMT_RET(AST_EXP exp) {
        SerialNumber = AST_Node_Serial_Number.getFresh();
        String addedExp = "";
        if (exp != null) {
            addedExp = " exp";
        }
        String deriveRule = String.format("====================== stmt -> RETURN%s SEMICOLON\n", addedExp);
        System.out.print(deriveRule);
        this.exp = exp;
    }

    public void PrintMe() {
        System.out.print("AST_STMT_RET\n");

        /*****************************/
        /* RECURSIVELY PRINT var ... */
        /*****************************/
        exp.PrintMe();

        /*********************************/
        /* Print to AST GRAPHIZ DOT file */
        /*********************************/
        AST_GRAPHVIZ.getInstance().logNode(SerialNumber, "return stmt\n");

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        AST_GRAPHVIZ.getInstance().logEdge(SerialNumber, exp.SerialNumber);
    }
}
