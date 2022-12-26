package AST;
import TYPES.*;
import SYMBOL_TABLE.*;

public class AST_TYPE extends AST_Node{
    public String type;
    public String idValue = "";

    public AST_TYPE(String type, String idValue){
        SerialNumber = AST_Node_Serial_Number.getFresh();
        System.out.format("====================== type -> ID(%s)\n", idValue);
        this.type = type;
        this.idValue = idValue;
    }

    public AST_TYPE(String type){
        SerialNumber = AST_Node_Serial_Number.getFresh();
        System.out.format("====================== type -> %s\n", type);
        this.type = type;
    }

    public void PrintMe() {
        System.out.format("AST_TYPE: %s\n", type);
        /*********************************/
        /* Print to AST GRAPHIZ DOT file */
        /*********************************/
        AST_GRAPHVIZ.getInstance().logNode(SerialNumber,
                String.format("type %s\n", type));
    }

    public TYPE SemantMe() throws SemanticException
    {
        /* Check if the *type* is valid- in the scope and it's name is class name/ array name/ int/string */
        if (type.equals("TYPE_INT"))
        {
            return TYPE_INT.getInstance();
        }
        if (type.equals("TYPE_STRING"))
        {
            return TYPE_STRING.getInstance();
        }
        if (type.equals("TYPE_VOID"))
        {
            return TYPE_VOID.getInstance();
        }
        /* Check if it's a name of existing array or a class */
        /* Assumption- If we are in a class then the class is already entered to the symbol table*/
        if (!SYMBOL_TABLE.getInstance().typeCanBeInstanced(this.idValue))
            throw new SemanticException("Invalid type");
        /* typeCanBeInstanced(idValue) == true then this the type with this name is exist */
        return SYMBOL_TABLE.getInstance().find(this.idValue);
    }
}
