package AST;

public class AST_TYPE extends AST_Node{
    public String type;
    public String idValue;
    public AST_TYPE(String type, String idValue){
        this.type = type;
        this.idValue = idValue;
        SerialNumber = AST_Node_Serial_Number.getFresh();
        System.out.format("====================== type -> ID(%s)\n", idValue);
    }

    public AST_TYPE(String type){
        this.type = type;
        SerialNumber = AST_Node_Serial_Number.getFresh();
        System.out.format("====================== dec -> %s\n", type);
    }
}
