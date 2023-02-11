/***********/
/* PACKAGE */
/***********/
package MIPS;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.io.PrintWriter;
import java.util.List;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import IR.IRcommand;
import TEMP.*;

enum SegmentType{
	NONE,
	DATA,
	CODE
}

public class MIPSGenerator
{
	private int WORD_SIZE=4;
	/***********************/
	/* The file writer ... */
	/***********************/
	private PrintWriter fileWriter;
	private SegmentType current_segment = SegmentType.NONE;
	private String zero = "$zero";
	private String invalid_ptr_label = "string_invalid_ptr_dref";
	private String access_violation_label = "string_access_violation";
	private String div_by_0_label = "string_illegal_div_by_0";

	/***********************/
	/* The file writer ... */
	/***********************/
	public void finalizeFile()
	{
		fileWriter.print("\tli $v0,10\n");
		fileWriter.print("\tsyscall\n");
		fileWriter.close();
	}

	public void print_string(TEMP t)
	{
		int idx=t.getRegisterSerialNumber();
		fileWriter.format("\tmove $a0,t_%d\n",idx);
		fileWriter.format("\tli $v0,1\n");
		fileWriter.format("\tsyscall\n");
	}

	public void print_int(TEMP t)
	{
		int idx=t.getRegisterSerialNumber();
		// fileWriter.format("\taddi $a0,Temp_%d,0\n",idx);
		fileWriter.format("\tmove $a0,t_%d\n",idx);
		fileWriter.format("\tli $v0,1\n");
		fileWriter.format("\tsyscall\n");
		/* "When printing an integer, print an additional space at the end"*/
		fileWriter.format("\tli $a0,32\n");
		fileWriter.format("\tli $v0,11\n");
		fileWriter.format("\tsyscall\n");
	}

	/*==================================== Stav ====================================*/
	public void lstr(TEMP t,String str, String str_label)
	{
		/* Create string value as a global variable in data*/
		open_segment(SegmentType.DATA);
		fileWriter.format("%s: .asciiz %s\n", str_label, str);

		/* TODO: if t!= null */
		/* Point to the seted string*/
		open_segment(SegmentType.CODE);
		fileWriter.format("\tla $t%d, %s\n", t.getRegisterSerialNumber(), str_label);
	}

	public void open_segment(SegmentType segment_type)
	{
		String segment = "data";
		if (segment_type == SegmentType.CODE)
		{
			segment = "code";
		}

		if (this.current_segment != segment_type)
		{
			fileWriter.format(".%s\n", segment);
		}
		this.current_segment = segment_type;
	}

	public void add_strings(TEMP dst,TEMP str1,TEMP str2)
	{
		String a0 = "$a0";
		String len_str1 = "$s1";
		String len_str2 = "$s0";
		String return_register = "$v0";
		String syscall_num_and_return = "$v0";
		String dst_register = "$t" + dst.getRegisterSerialNumber();
		String copy_pointer = "$s1";

		/* calculate str1's length */
		len(str1);
		/* len of str1 now in len_str1*/
		move(len_str1, return_register);

		/* calculate str2's length */
		len(str2);
		/* len of str1 now in len_str1*/
		move(len_str2, return_register);

		/* Calculate str1's length + str2's length to $a0*/
		add(a0, len_str1, len_str2);
		/* Add null termination to the united length (for allocation)*/
		addu(a0, a0, 1);

		/* Allocate space for concatenated string, the pointer will be at syscall_num*/
		li(syscall_num_and_return, 9);
		fileWriter.format("\tsyscall\n");

		/* Make dst point to the beginning of the concatenated string.*/
		move(dst_register,syscall_num_and_return);

		/* Now we need to build it- copy str1 and then str2,
		* the copy_pointer will help us to copy each str's char in the right place */

		move(copy_pointer, dst_register);
		copy(str1, copy_pointer);
		copy(str2, copy_pointer);

		/* Now copy_pointer points to the end of the string- add null terminator*/
		sb(zero, copy_pointer, 0);

		/* dst = concatenated string */
		fileWriter.format("\tmove $t%d, $s1\n", dst.getRegisterSerialNumber());
	}

	/* Receives strings of dst_register and src_register
	   make the mips instruction: dst_register <- src_register
	 */
	public void move(String dst_register, String src_register)
	{
		fileWriter.format("\tmove %s, %s\n", dst_register, src_register);
	}

	public void move(TEMP dst_register, TEMP src_register)
	{
		String dst = "$t" + dst_register.getRegisterSerialNumber();
		String src = "$t" + src_register.getRegisterSerialNumber();

		move(dst, src);
	}

	/* Receives strings of dst_register, src_register and offset (for src)
	   make the mips instruction lb: dst_register <- src_register[offset]
	 */
	public void lb(String dst_register, String src_register, int offset)
	{
		/* lb: transfers one byte of data from main memory to a register */
		fileWriter.format("\tlb %s, %d(%s)\n", dst_register, offset, src_register);
	}

	/* Receives strings of dst_register, src_register and offset (for src)
	   make the mips instruction sb: dst_register[offset] <- src_register
	 */
	public void sb(String src_register, String dst_register, int offset)
	{
		/* lb: transfers one byte of data from main memory to a register */
		fileWriter.format("\tsb %s, %d(%s)\n", src_register, offset, dst_register);
	}

	/* Receives strings of reg1, reg2 and jump label
	   make the mips instruction beq: if the data: reg1 == reg2 then jump label
	 */
	public void beq(String reg1, String reg2, String label)
	{
		fileWriter.format("\tbeq %s, %s, %s\n", reg1, reg2, label);
	}

	/* Receives strings of reg1, reg2 and jump label
	   make the mips instruction beq: if the data: reg1 != reg2 then jump label
	 */
	public void bne(String reg1, String reg2, String label)
	{
		fileWriter.format("\tbne %s, %s, %s\n", reg1, reg2, label);
	}

	/* Receives strings of dst_register, reg and int inc
	   make the mips instruction addu: dst_register = (the data) reg + inc
	 */
	public void addu(String dst_register, String reg, int inc)
	{
		fileWriter.format("\taddu %s, %s, %d\n", dst_register, reg, inc);
	}

	/* Receives strings of dst_register, reg and int inc
	   make the mips instruction addu: dst_register = (the data) reg1 + reg2
	 */
	public void add(String dst_register, String reg1, String reg2)
	{
		fileWriter.format("\tadd %s, %s, %s\n", dst_register, reg1, reg2);
	}

	/* Receives Temp of str
	   Returns str's length in $v0
	   NOTE: this function is using $s0
	 */
	public void len(TEMP str)
	{
		/* Body of len_func: using $v0 to calculate the len of the argument */
		String start_label = IRcommand.getFreshLabel("start_len_loop");
		String end_label = IRcommand.getFreshLabel("end_len_loop");

		/* Set registers */
		String str_register = "$t" + str.getRegisterSerialNumber();
		String len = "$v0";
		String str_pointer = "$s0";

		move(len, zero);
		/* Add start label and check term for end loop */
		fileWriter.format("%s:\n", start_label);
		lb(str_pointer, str_register, 0);
		beq(str_pointer, zero, end_label);

		/* Loop body: len += 1, str_pointer += 1 */
		addu(len, len, 1);
		addu(str_pointer, str_pointer, 1);
		jump(start_label);

		/* Add end label */
		fileWriter.format("%s:\n", end_label);
	}

	/* Receives Temp of str and dst_pointer (it's register name)
	   Returns str's length in $v0
	   NOTE: this function is using $s0 and $s2
	   ASSUMPTION: $s1 points to the place that the chars need to be copied to
	 */
	public void copy(TEMP str, String dst_pointer)
	{
		/* Body of len_func: using $v0 to calculate the len of the argument */
		String start_label = IRcommand.getFreshLabel("start_copy_loop");
		String end_label = IRcommand.getFreshLabel("end_copy_loop");

		/* Set registers */
		String str_register = "$t" + str.getRegisterSerialNumber();
		String copied_str_char = "$s0";
		String copied_str_pointer = "$s2";

		move(copied_str_pointer, str_register);

		/* Add start label and check term for end loop */
		fileWriter.format("%s:\n", start_label);
		lb(copied_str_char, copied_str_pointer, 0);
		beq(copied_str_char, zero, end_label);

		/* Loop body: [pointer] = copied_str_char, copied_str_pointer, pointer += 1 */
		sb(copied_str_char, dst_pointer, 0);
		addu(dst_pointer, dst_pointer, 1);
		addu(copied_str_pointer, copied_str_pointer, 1);
		jump(start_label);

		/* Add end label */
		fileWriter.format("\t%s:\n", end_label);
	}


	/* Receives Temp of str and dst_pointer (it's register name)
	   Returns 0 in dst if str1 != str2 OR 1 in dst if str1 == str2*/
	public void compare_strings(TEMP dst, TEMP str1, TEMP str2)
	{
		/* Body of len_func: using $v0 to calculate the len of the argument */
		String start_label = IRcommand.getFreshLabel("start_compare_loop");
		String set_dst_0 = IRcommand.getFreshLabel("assign_zero");
		String end_label = IRcommand.getFreshLabel("end_compare_loop");

		/* Set registers */
		String dst_register = "$t" + dst.getRegisterSerialNumber();
		String str1_register = "$t" + str1.getRegisterSerialNumber();
		String str2_register = "$t" + str2.getRegisterSerialNumber();

		String pointer_str1 = "$s0";
		String char_str1 = "$s1";
		String pointer_str2 = "$s2";
		String char_str2 = "$s3";

		/* Set the value of dst to 1, if we will find out a non matching letter- will set it to 0 and exit*/
		li(dst_register, 1);

		/* Set pointers for loops */
		move(pointer_str1, str1_register);
		move(pointer_str2, str2_register);

		/* Add start label and check term for end loop */
		fileWriter.format("%s:\n", start_label);
		lb(char_str1, pointer_str1, 0);
		lb(char_str2, pointer_str2, 0);
		/* If str1[i] != str2[i] -> set dst to 0*/
		bne(char_str1, char_str2, set_dst_0);
		/* Else- if (str1[i] == str2[i]) and str1[i] == null- end */
		beq(char_str1, zero, end_label);
		/* Else- keep going */
		addu(pointer_str1, pointer_str1, 1);
		addu(pointer_str2, pointer_str2, 1);
		jump(start_label);

		/* Add set_dst_0 label and set dst to 0 */
		fileWriter.format("%s:\n", set_dst_0);
		li(dst_register, 0);

		/* Add end label */
		fileWriter.format("\t%s:\n", end_label);
	}

	public void func_prologue(String prologue_label, int local_var_num)
	{
		/* For Us- Based on practice_10 */
		open_segment(SegmentType.CODE);
		/* Create the label for the new created function*/
		label(prologue_label);

		/* Make function prologue */
		/* Store return address in $ra */
		fileWriter.format("\tsubu $sp, $sp, 4\n");
		fileWriter.format("\tsw $ra, 0($sp), 4\n");
		/* Backup $fp of called func */
		fileWriter.format("\tsubu $sp, $sp, 4\n");
		fileWriter.format("\tsw $fp, 0($sp), 4\n");
		/* Update $fp = $sp */
		fileWriter.format("\tmove $fp, $sp\n");
		/* Backup all 10 temporaries */
		for (int i = 0; i < 10; i++)
		{
			fileWriter.format("\tsubu $sp, $sp, 4\n");
			fileWriter.format("\tsw $t%d, 0($sp)\n");
		}
		/* Save space for local_var_num local variables */
		fileWriter.format("\tsubu $sp, $sp, %d\n", (local_var_num) * WORD_SIZE);
	}

	/* Receives return register (return_temp) and set $v0 <- return_temp*/
	public void set_v0(TEMP return_temp)
	{
		move("$v0", "$t\n" + return_temp.getRegisterSerialNumber());
	}

	/* Receives return register (assigned_temp) and set assigned_temp <- $v0*/
	public void get_v0(TEMP assigned_temp)
	{
		move("$t" + assigned_temp.getRegisterSerialNumber(), "$v0");
	}

	/* Function Prologue: update $sp, $fp
                         restore %t0,...,$t9
                         jump to $ra*/
	public void func_epilogue(String epilogue_label)
	{
		/* For Us- Based on practice_10 */
		open_segment(SegmentType.CODE);
		/* Create the label for the end of new created function*/
		label(epilogue_label);

		/* Make function prologue */
		/* Update $sp = $fp */
		fileWriter.format("\tmove $sp, $fp\n");
		/* Restore */
		for (int i = 0; i < 10; i++)
		{
			fileWriter.format("\tlw $t%d, %d($sp)\n", i, (-(i + 1)) * WORD_SIZE);
		}
		/* Restore precious %fp */
		fileWriter.format("\tlw $fp, 0($sp)\n");
		/* Store return address in $ra */
		fileWriter.format("\tlw $ra, 4($sp)\n");
		fileWriter.format("\taddu $sp, $sp, 8\n");
		/* jump to return address */
		fileWriter.format("\tjr $ra\n");
	}

	public void set_arguments(TEMP_LIST param_list)
	{
		open_segment(SegmentType.CODE);
		TEMP_LIST pointer = param_list;
		/* set arguments */
		for (int i = 0; i < param_list.len; i++)
		{
			TEMP current_temp = pointer.head;
			fileWriter.format("\tsubu $sp, $sp, %d\n", 4);
			fileWriter.format("\tsw $t%d, 0($sp)\n", current_temp.getRegisterSerialNumber());
			pointer = pointer.tail;
		}
	}

	public void del_arguments(int param_nums)
	{
		for (int i = 0; i < param_nums; i++)
		{
			fileWriter.format("\taddi $sp, $sp, %d\n", 4);
		}
	}

	public void global_var_dec(String global_var_label, String str_value, int int_value)
	{
		open_segment(SegmentType.DATA);
		if (str_value != null)
		{
			fileWriter.format("\t%s: .word %s\n",global_var_label, str_value);
		}
		else
		{
			fileWriter.format("\t%s: .word %d\n",global_var_label, int_value);
		}
	}

	/* Set an uninitialized global variable*/
	public void global_var_dec(String global_var_label)
	{
		open_segment(SegmentType.DATA);
		fileWriter.format("\t%s: .word %d\n",global_var_label, 0);
	}

	public void local_var_dec(int var_offset, TEMP assigned_temp)
	{
		open_segment(SegmentType.CODE);
		if (assigned_temp == null)
		{
			/* Assign zero (no assignment)*/
			li("$s0", 0);
			fileWriter.format("\tsw $s0, %d($fp)\n", var_offset);
		}
		else
		{
			fileWriter.format("\tsw $t%d, %d($fp)\n", assigned_temp.getRegisterSerialNumber(), var_offset);
		}
	}

	/* Receives variable's offset and register to set the variable data in
	*  Called on local variable or arguments (all access from stack)*/
	public void get_var_with_offset(int var_offset, TEMP var_temp)
	{
		open_segment(SegmentType.CODE);
		fileWriter.format("\tlw $t%d, %d($fp)\n", var_temp.getRegisterSerialNumber(), var_offset);
	}

	/* Receives variable's global label and register to set the global data in*/
	public void get_global_var(String global_var_label, TEMP var_temp)
	{
		open_segment(SegmentType.CODE);
		fileWriter.format("\tla $s0, %s\n", global_var_label);
		fileWriter.format("\tlw $t%d, 0($s0)\n", var_temp.getRegisterSerialNumber());
	}

	public void update_global_var(String global_var_label, TEMP temp_to_assign)
	{
		open_segment(SegmentType.CODE);
		/* Load address of global_var_label to %s0 */
		fileWriter.format("\tla $s0, %s\n", global_var_label);
		/* Store temp_to_assign in this address (update global..)*/
		fileWriter.format("\tsw $t%d, 0($s0)\n", temp_to_assign.getRegisterSerialNumber());
	}

	/* Updates local var/ arguments*/
	public void update_stack_var(int var_offset, TEMP temp_to_assign)
	{
		fileWriter.format("\tsw $t%d, %d($fp)\n", temp_to_assign.getRegisterSerialNumber(), var_offset);
	}

	/* First cell will contain array size, next cells- array cells
	 *  Set array_size_temp to point to the allocated space (the array)*/
	public void allocate_array(TEMP array_size_temp, TEMP array_temp)
	{
		open_segment(SegmentType.CODE);
		String array_size = "$t" + array_size_temp.getRegisterSerialNumber();
		String array_pointer = "$t" + array_temp.getRegisterSerialNumber();
		String a0 = "$a0";
		String v0 = "$v0";
		String four = "$s0";

		addu(array_size, array_size, 1);

		/* ===========Call malloc syscall============*/
		/* Set $a0 to the required allocated size*/
		move(a0, array_size);
		li(four, 4);
		mul(a0, a0, four);
		/* Set $v0*/
		li(v0, 9);
		fileWriter.format("\tsyscall\n");

		/* Set array_pointer ($v0 points to the allocated space)*/
		move(array_pointer, v0);

		/* Set array size in first cell */
		store(array_size, array_pointer, 0);
	}

	public void access_array(TEMP array_temp, TEMP array_index_temp, TEMP array_access_temp)
	{
		check_oob(array_temp, array_index_temp);
		String absolute_address = "$s0";
		String result_register = "$t" + array_access_temp.getRegisterSerialNumber();

		get_array_cell(array_temp, array_index_temp, absolute_address);

		/* Access - Save the value in result_register*/
		load(result_register, absolute_address, 0);
	}

	/* Receives array_temp (pointer to the start of the array), and array_index_temp (required index)
	   Make absolute_address contain the absolute address of the required cell*/
	public void get_array_cell(TEMP array_temp, TEMP array_index_temp, String absolute_address)
	{
		check_oob(array_temp, array_index_temp);check_oob(array_temp, array_index_temp);
		String array_register = "$t" + array_temp.getRegisterSerialNumber();
		String index_register = "$t" + array_index_temp.getRegisterSerialNumber();
		String four = "$s1";

		/* Array cells are located one cell next (because first cell saves array size */
		move(absolute_address, index_register);
		/* absolute_address will contain the address of the required cell:
		 		1. add 1 to the required index
		 		2. multiply by 4
		 		3. add array address*/
		addu(absolute_address, absolute_address, 1);
		li(four, 4);
		/* absolute_address*=4 */
		mul(absolute_address, absolute_address, four);
		/* absolute_address = absolute_address + array address */
		add(absolute_address, absolute_address, array_register);
	}

	public void update_array(TEMP array_temp, TEMP array_index_temp, TEMP temp_to_assign)
	{
		check_oob(array_temp, array_index_temp);
		String absolute_address = "$s0";
		String assigned_register = "$t" + temp_to_assign.getRegisterSerialNumber();

		get_array_cell(array_temp, array_index_temp, absolute_address);

		/* Access - Save the value in result_register*/
		store(assigned_register, absolute_address, 0);
	}

	public void check_oob(TEMP array_temp, TEMP array_index_temp)
	{
		open_segment(SegmentType.CODE);

		String start_check_oob = IRcommand.getFreshLabel("start_check_oob");
		String end_check_oob = IRcommand.getFreshLabel("end_check_oob");
		String error_check_oob = IRcommand.getFreshLabel("error_check_oob");

		String temp_size = "$s0";
		String array_pointer = "$t" + array_temp.getRegisterSerialNumber();
		String array_index = "$t" + array_index_temp.getRegisterSerialNumber();

		label(start_check_oob);
		/* The size of an array is saved in first cell- then check if not 0 <= array_index_temp < size -> error*/
		load(temp_size, array_pointer, 0);
		/* 0 > array_index_temp*/
		blt(array_index, zero, error_check_oob);
		/* Here: 0 <= array_index_temp, if array_index_temp < size then- end check!*/
		blt(array_index, temp_size, end_check_oob);

		/* Else- if array_index_temp >= size then error_check_oob */
		label(error_check_oob);
		/* Print "Access Violation" and then exit*/
		fileWriter.format("\tla $a0, %s\n",this.access_violation_label);
		li("$v0", 4);
		fileWriter.format("\tsyscall\n");
		li("$v0", 10);
		fileWriter.format("\tsyscall\n");

		label(end_check_oob);
	}

	public void load(String dst,String src, int offset)
	{
		open_segment(SegmentType.CODE);
		fileWriter.format("\tlw %s, %d(%s)\n",dst, offset, src);
	}

	public void store(String src, String dst, int offset)
	{
		open_segment(SegmentType.CODE);
		fileWriter.format("\tsw %s, %d(%s)\n",src, offset, dst);
	}

	public void li(TEMP t,int value)
	{
		open_segment(SegmentType.CODE);

		int idx=t.getRegisterSerialNumber();

		fileWriter.format("\tli $t%d,%d\n", idx, value);
	}

	private void li(String register, int value) {
		fileWriter.format("\tli %s, %d\n", register, value);
	}

	public void add(TEMP dst,TEMP oprnd1,TEMP oprnd2)
	{
		open_segment(SegmentType.CODE);

		int i1 = oprnd1.getRegisterSerialNumber();
		int i2 = oprnd2.getRegisterSerialNumber();
		int dstidx = dst.getRegisterSerialNumber();

		fileWriter.format("\tadd t%d, t%d, t%d\n", dstidx, i1, i2);
	}

	public void sub(TEMP dst,TEMP oprnd1,TEMP oprnd2)
	{
		open_segment(SegmentType.CODE);

		int i1 = oprnd1.getRegisterSerialNumber();
		int i2 = oprnd2.getRegisterSerialNumber();
		int dstidx = dst.getRegisterSerialNumber();

		fileWriter.format("\tsub t%d, t%d, t%d\n", dstidx, i1, i2);
	}

	public void mul(TEMP dst,TEMP oprnd1,TEMP oprnd2)
	{
		open_segment(SegmentType.CODE);

		int i1 = oprnd1.getRegisterSerialNumber();
		int i2 = oprnd2.getRegisterSerialNumber();
		int dstidx = dst.getRegisterSerialNumber();

		fileWriter.format("\tmul t%d, t%d, t%d\n", dstidx, i1, i2);
	}

	public void mul(String dst,String oprnd1,String oprnd2)
	{
		open_segment(SegmentType.CODE);
		fileWriter.format("\tmul %s, %s, %s\n", dst, oprnd1, oprnd2);
	}

	public void div(TEMP dst,TEMP oprnd1,TEMP oprnd2)
	{
		open_segment(SegmentType.CODE);

		int i1 = oprnd1.getRegisterSerialNumber();
		int i2 = oprnd2.getRegisterSerialNumber();
		int dstidx = dst.getRegisterSerialNumber();

		fileWriter.format("\tdiv t%d, t%d, t%d\n", dstidx, i1, i2);
	}

	public void label(String inlabel)
	{
		if (inlabel.equals("main"))
		{
			fileWriter.format(".text\n");
			fileWriter.format("%s:\n",inlabel);
		}
		else
		{
			fileWriter.format("%s:\n",inlabel);
		}
	}

	public void jump(String inlabel)
	{
		fileWriter.format("\tj %s\n",inlabel);
	}

	public void blt(TEMP oprnd1,TEMP oprnd2,String label)
	{
		int i1 =oprnd1.getRegisterSerialNumber();
		int i2 =oprnd2.getRegisterSerialNumber();

		fileWriter.format("\tblt t%d, t%d, %s\n",i1,i2,label);
	}

	public void blt(String register1, String register2, String label)
	{
		fileWriter.format("\tblt %s, %s, %s\n",register1, register2, label);
	}

	public void bgt(TEMP oprnd1,TEMP oprnd2,String label)
	{
		int i1 =oprnd1.getRegisterSerialNumber();
		int i2 =oprnd2.getRegisterSerialNumber();

		fileWriter.format("\tbgt t%d, t%d,%s\n",i1,i2,label);
	}

	public void bge(TEMP oprnd1,TEMP oprnd2,String label)
	{
		int i1 =oprnd1.getRegisterSerialNumber();
		int i2 =oprnd2.getRegisterSerialNumber();

		fileWriter.format("\tbge t%d,t%d,%s\n",i1,i2,label);
	}

	public void ble(TEMP oprnd1,TEMP oprnd2,String label)
	{
		int i1 =oprnd1.getRegisterSerialNumber();
		int i2 =oprnd2.getRegisterSerialNumber();

		fileWriter.format("\tble t%d,t%d,%s\n",i1,i2,label);
	}

	private void bge(String register1, String register2, String label)
	{
		fileWriter.format("\tbge %s, %s, %s\n", register1, register2, label);
	}

	public void bne(TEMP oprnd1,TEMP oprnd2,String label)
	{
		open_segment(SegmentType.CODE);

		int i1 =oprnd1.getRegisterSerialNumber();
		int i2 =oprnd2.getRegisterSerialNumber();
		
		fileWriter.format("\tbne t%d,t%d,%s\n", i1, i2, label);
	}

	public void beq(TEMP oprnd1,TEMP oprnd2,String label)
	{
		int i1 =oprnd1.getRegisterSerialNumber();
		int i2 =oprnd2.getRegisterSerialNumber();
		
		fileWriter.format("\tbeq t%d,t%d,%s\n",i1,i2,label);
	}

	public void beqz(TEMP oprnd1,String label)
	{
		int i1 =oprnd1.getRegisterSerialNumber();
				
		fileWriter.format("\tbeq t%d,$zero,%s\n",i1,label);
	}

	/* Fixes result of binop to match semantics of L language.
	   In case of overflow, will assign max value,
	   In case of underflow, will assign min value.
	 */
	public void standardBinopToLBinop(TEMP t) {
		int MAX_VALUE = 32767;
		int MIN_VALUE = -32768;

		/* Allocate labels */
		String label_check_overflow = IRcommand.getFreshLabel("check_overflow");
		String label_end = IRcommand.getFreshLabel("end");

		/* Assign registers' names to variables */
		String t_register = String.format("$t%d", t.getRegisterSerialNumber());
		String min_value_register = "$s0";
		String max_value_register = "$s1";

		/* Create code */
		li(min_value_register, MIN_VALUE); // $s0 := MIN_VALUE
		li(max_value_register, MAX_VALUE); // $s1 := MAX_VALUE

		bge(t_register, min_value_register, label_check_overflow); // check if t >= MIN_VALUE
		// here t < MIN_VALUE
		li(t_register, MIN_VALUE); // t := MIN_VALUE
		jump(label_end); // it's underflow, of course it's not and overflow

		label(label_check_overflow); // add label
		bge(max_value_register, t_register, label_end); // check if MAX_VALUE >= t
		// here t > MAX_VALUE
		li(t_register, MAX_VALUE); // t := MAX_VALUE

		label(label_end); // add label
	}

	public void GTIntegers(TEMP dst, TEMP t1, TEMP t2) {
		/*******************************/
		/* [1] Allocate 2 fresh labels */
		/*******************************/
		String label_end        = IRcommand.getFreshLabel("end");
		String label_assign_one  = IRcommand.getFreshLabel("assign_one");
		String label_assign_zero = IRcommand.getFreshLabel("assign_zero");

		/******************************************/
		/* [2] if (t1> t2) goto label_AssignOne;  */
		/*     if (t1<=t2) goto label_AssignZero; */
		/******************************************/
		bgt(t1,t2,label_assign_one);
		ble(t1,t2,label_assign_zero);

		/************************/
		/* [3] label_AssignOne: */
		/*                      */
		/*         t3 := 1      */
		/*         goto end;    */
		/*                      */
		/************************/
		label(label_assign_one);
		li(dst,1);
		jump(label_end);

		/*************************/
		/* [4] label_AssignZero: */
		/*                       */
		/*         t3 := 1       */
		/*         goto end;     */
		/*                       */
		/*************************/
		label(label_assign_zero);
		li(dst,0);
		jump(label_end);

		/******************/
		/* [5] label_end: */
		/******************/
		label(label_end);
	}

	public void LTIntegers(TEMP dst, TEMP t1, TEMP t2) {
		/*******************************/
		/* [1] Allocate 2 fresh labels */
		/*******************************/
		String label_end        = IRcommand.getFreshLabel("end");
		String label_assign_one  = IRcommand.getFreshLabel("assign_one");
		String label_assign_zero = IRcommand.getFreshLabel("assign_zero");

		/******************************************/
		/* [2] if (t1< t2) goto label_AssignOne;  */
		/*     if (t1>=t2) goto label_AssignZero; */
		/******************************************/
		blt(t1,t2,label_assign_one);
		ble(t1,t2,label_assign_zero);

		/************************/
		/* [3] label_AssignOne: */
		/*                      */
		/*         t3 := 1      */
		/*         goto end;    */
		/*                      */
		/************************/
		label(label_assign_one);
		li(dst,1);
		jump(label_end);

		/*************************/
		/* [4] label_AssignZero: */
		/*                       */
		/*         t3 := 1       */
		/*         goto end;     */
		/*                       */
		/*************************/
		label(label_assign_zero);
		li(dst,0);
		jump(label_end);

		/******************/
		/* [5] label_end: */
		/******************/
		label(label_end);
	}

	public void EQIntegers(TEMP dst, TEMP t1, TEMP t2) {
		/*******************************/
		/* [1] Allocate 3 fresh labels */
		/*******************************/
		String label_end        = IRcommand.getFreshLabel("end");
		String label_assign_one  = IRcommand.getFreshLabel("assign_one");
		String label_assign_zero = IRcommand.getFreshLabel("assign_zero");

		/******************************************/
		/* [2] if (t1==t2) goto label_AssignOne;  */
		/*     if (t1!=t2) goto label_AssignZero; */
		/******************************************/
		beq(t1,t2,label_assign_one);
		bne(t1,t2,label_assign_zero);

		/************************/
		/* [3] label_AssignOne: */
		/*                      */
		/*         t3 := 1      */
		/*         goto end;    */
		/*                      */
		/************************/
		label(label_assign_one);
		li(dst,1);
		jump(label_end);

		/*************************/
		/* [4] label_AssignZero: */
		/*                       */
		/*         t3 := 1       */
		/*         goto end;     */
		/*                       */
		/*************************/
		label(label_assign_zero);
		li(dst,0);
		jump(label_end);

		/******************/
		/* [5] label_end: */
		/******************/
		label(label_end);
	}

	public void allocateVT(String label_vt, List<String> methodLabels) {
		open_segment(SegmentType.DATA);

		label(label_vt);

		for (String l : methodLabels) {
			globalWord(l);
		}
	}

	private void globalWord(String s) {
		fileWriter.format("\t.word %s\n", s);
	}
	
	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static MIPSGenerator instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected MIPSGenerator() {}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static MIPSGenerator getInstance()
	{
		if (instance == null)
		{
			/*******************************/
			/* [0] The instance itself ... */
			/*******************************/
			instance = new MIPSGenerator();

			try
			{
				/*********************************************************************************/
				/* [1] Open the MIPS text file and write data section with error message strings */
				/*********************************************************************************/
				String dirname="./output/";
				String filename=String.format("MIPS.txt");

				/***************************************/
				/* [2] Open MIPS text file for writing */
				/***************************************/
				instance.fileWriter = new PrintWriter(dirname+filename);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			/*****************************************************/
			/* [3] Print data section with error message strings */
			/*****************************************************/
			instance.fileWriter.print(".data\n");
			instance.fileWriter.print("string_access_violation: .asciiz \"Access Violation\"\n");
			instance.fileWriter.print("string_illegal_div_by_0: .asciiz \"Illegal Division By Zero\"\n");
			instance.fileWriter.print("string_invalid_ptr_dref: .asciiz \"Invalid Pointer Dereference\"\n");
		}
		return instance;
	}
}
