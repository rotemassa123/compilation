package TYPES;

import jdk.nashorn.internal.ir.Symbol;

public class TYPE_VAR extends TYPE{
    public TYPE type;

    public TYPE_VAR(String name, TYPE type){
        this.name = name;
        this.type = type;
    }

    @Override
    public boolean isVar(){ return true; }
}
