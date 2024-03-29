package AST;
import TYPES.*;
import SYMBOL_TABLE.*;

public class AST_CLASS_DEC extends AST_Node{
    public String className;
    public String extendsName;
    public AST_LIST<AST_CFIELD> cFieldList;

    public AST_CLASS_DEC(String className, String extendsName, AST_LIST<AST_CFIELD> cFieldList, int line){
        SerialNumber = AST_Node_Serial_Number.getFresh();
        System.out.format("====================== classDec -> CLASS ID(%s) extends ID(%s) {cFieldList}\n", className, extendsName);
        this.className = className;
        this.extendsName = extendsName;
        this.cFieldList = cFieldList;
        this.line = line;
    }

    public AST_CLASS_DEC(String className, AST_LIST<AST_CFIELD> cFieldList, int line){
        SerialNumber = AST_Node_Serial_Number.getFresh();
        System.out.format("====================== classDec -> CLASS ID(%s) {cFieldList}\n", className);
        this.className = className;
        this.cFieldList = cFieldList;
        this.line = line;
    }

    public void PrintMe() {
        System.out.print("classDec\n");

        if (extendsName == null)
            System.out.format("classDec: %s\n", className);
        else
            System.out.format("classDec: %s extends: %s\n", className, extendsName);
        if(cFieldList != null)
            cFieldList.PrintMe();

        String nodeName = extendsName == null ?
                String.format("classDec: %s\n", className) : String.format("classDec: %s extends: %s\n", className, extendsName);
        AST_GRAPHVIZ.getInstance().logNode(SerialNumber, nodeName);
        if (cFieldList != null)
            AST_GRAPHVIZ.getInstance().logEdge(SerialNumber, cFieldList.SerialNumber);
    }

    public TYPE SemantMe() throws SemanticException
    {
        if(SYMBOL_TABLE.getInstance().getCurrentScopeType() != ScopeTypeEnum.GLOBAL){
            throw new SemanticException(this);
        }

        if(SYMBOL_TABLE.getInstance().find(this.className) != null)
            throw new SemanticException(this);

        /*************************/
        /* [1] Begin Class Scope */
        /*************************/


        TYPE_CLASS type_class = new TYPE_CLASS(null, this.className, null);

        /***************************/
        /* [2] Semant Data Members */
        /***************************/

        if(this.extendsName != null){
            TYPE father = SYMBOL_TABLE.getInstance().find(this.extendsName);
            if(father == null){
                System.out.println(this.line);
                throw new SemanticException(this);
            }
            if(!father.isClass()) {
                throw new SemanticException(this);
            }

            type_class.father = (TYPE_CLASS) father;
        }

        SYMBOL_TABLE.getInstance().enter(className, type_class, true);
        SYMBOL_TABLE.getInstance().beginScope(ScopeTypeEnum.CLASS, type_class);

        //SemantMe will check if illegal inheritance or duplicated names in the same scope
        cFieldList.SemantMe();

        /*****************/
        /* [3] End Scope */
        /*****************/
        SYMBOL_TABLE.getInstance().endScope();

        return type_class;
    }
}
