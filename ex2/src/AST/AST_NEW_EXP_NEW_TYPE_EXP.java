package AST;

public class AST_NEW_EXP_NEW_TYPE_EXP extends AST_NEW_EXP {
    public AST_TYPE type;
    public AST_EXP exp;

    public AST_NEW_EXP_NEW_TYPE_EXP(AST_TYPE type, AST_EXP exp) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        SerialNumber = AST_Node_Serial_Number.getFresh();

        // TODO: Add print derivation rule

        /*******************************/
        /* COPY INPUT DATA NENBERS ... */
        /*******************************/
        this.type = type;
        this.exp = exp;
    }
}
