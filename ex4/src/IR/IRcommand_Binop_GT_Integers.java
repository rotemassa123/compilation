/***********/
/* PACKAGE */
/***********/
package IR;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/
import TEMP.*;
import MIPS.*;

public class IRcommand_Binop_GT_Integers extends IRcommand
{
    public TEMP t1;
    public TEMP t2;
    public TEMP dst;

    public IRcommand_Binop_GT_Integers(TEMP dst,TEMP t1,TEMP t2)
    {
        this.dst = dst;
        this.t1 = t1;
        this.t2 = t2;

        this.dest = dst;
        this.depends_on.add(t1);
        this.depends_on.add(t2);
    }

    /***************/
    /* MIPS me !!! */
    /***************/
    public void MIPSme()
    {
        MIPSGenerator.getInstance().GTIntegers(dst, t1, t2);
    }
}
