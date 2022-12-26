package TYPES;

public class TYPE_ARRAY extends TYPE{

    /* The type can be a class/array/primitive type*/
    public TYPE arrayType;
    public int arraySize;

    /****************/
    /* CTROR(S) ... */
    /****************/
    public TYPE_ARRAY(TYPE arrayType,String name)
    {
        this.name = name;
        this.arrayType = arrayType;
    }

    @Override
    public boolean isArray(){ return true;}
}
