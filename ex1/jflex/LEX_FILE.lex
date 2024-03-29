/***************************/
/* FILE NAME: LEX_FILE.lex */
/***************************/

/*************/
/* USER CODE */
/*************/
import java_cup.runtime.*;
import java.lang.Math;

/******************************/
/* DOLAR DOLAR - DON'T TOUCH! */
/******************************/

%%

/************************************/
/* OPTIONS AND DECLARATIONS SECTION */
/************************************/
   
/*****************************************************/ 
/* Lexer is the name of the class JFlex will create. */
/* The code will be written to the file Lexer.java.  */
/*****************************************************/ 
%class Lexer

/********************************************************************/
/* The current line number can be accessed with the variable yyline */
/* and the current column number with the variable yycolumn.        */
/********************************************************************/
%line
%column

/*******************************************************************************/
/* Note that this has to be the EXACT same name of the class the CUP generates */
/*******************************************************************************/
%cupsym TokenNames

/******************************************************************/
/* CUP compatibility mode interfaces with a CUP generated parser. */
/******************************************************************/
%cup

/****************/
/* DECLARATIONS */
/****************/
/*****************************************************************************/   
/* Code between %{ and %}, both of which must be at the beginning of a line, */
/* will be copied verbatim (letter to letter) into the Lexer class code.     */
/* Here you declare member variables and functions that are used inside the  */
/* scanner actions.                                                          */  
/*****************************************************************************/   
%{
	/*********************************************************************************/
	/* Create a new java_cup.runtime.Symbol with information about the current token */
	/*********************************************************************************/
	private Symbol symbol(int type)               {return new Symbol(type, yyline, yycolumn);}
	private Symbol symbol(int type, Object value) {return new Symbol(type, yyline, yycolumn, value);}

	/*******************************************/
	/* Enable line number extraction from main */
	/*******************************************/
	public int getLine() { return yyline + 1; } 

	/**********************************************/
	/* Enable token position extraction from main */
	/**********************************************/
	public int getTokenStartPosition() { return yycolumn + 1; } 
%}

/***********************/
/* MACRO DECALARATIONS */
/***********************/
LINE_TERMINATOR	= \r|\n|\r\n
WHITE_SPACE		= {LINE_TERMINATOR} | [ \t]
INTEGER			= [0-9]+
ID				= [a-zA-Z]+[0-9a-zA-Z]*
STRING          = \"[a-zA-Z]*\"
START_COMMENT_TYPE_1    = "//"
START_COMMENT_TYPE_2    = "/*"
END_COMMENT_TYPE_2    = "*/"
VALID_COMMENT           = [\(\)\{\}\[\]+\-*/;.?!] | [a-zA-Z] | [0-9] | [ \t]

/**********************/
/* STATE DECLARATIONS */
/**********************/

%state COMMENT_TYPE_1
%state COMMENT_TYPE_2


/******************************/
/* DOLAR DOLAR - DON'T TOUCH! */
/******************************/

%%

/************************************************************/
/* LEXER matches regular expressions to actions (Java code) */
/************************************************************/

/**************************************************************/
/* YYINITIAL is the state at which the lexer begins scanning. */
/* So these regular expressions will only be matched if the   */
/* scanner is in the start state YYINITIAL.                   */
/**************************************************************/

<YYINITIAL> {

    "("					{ return symbol(TokenNames.LPAREN);}
    ")"					{ return symbol(TokenNames.RPAREN);}
    "["                 { return symbol(TokenNames.LBRACK);}
    "]"                 { return symbol(TokenNames.RBRACK);}
    "{"                 { return symbol(TokenNames.LBRACE);}
    "}"                 { return symbol(TokenNames.RBRACE);}
    "nil"               { return symbol(TokenNames.NIL);}
    "+"					{ return symbol(TokenNames.PLUS);}
    "-"					{ return symbol(TokenNames.MINUS);}
    "*"				    { return symbol(TokenNames.TIMES);}
    "/"					{ return symbol(TokenNames.DIVIDE);}
    ","					{ return symbol(TokenNames.COMMA);}
    "."                 { return symbol(TokenNames.DOT);}
    ";"					{ return symbol(TokenNames.SEMICOLON);}
    "int"		    	{ return symbol(TokenNames.TYPE_INT);}
    "string"            { return symbol(TokenNames.TYPE_STRING);}
    "void"				{ return symbol(TokenNames.TYPE_VOID);}
    ":="			    { return symbol(TokenNames.ASSIGN);}
    "="			        { return symbol(TokenNames.EQ);}
    "<"			        { return symbol(TokenNames.LT);}
    ">"		    	    { return symbol(TokenNames.GT);}
    "array"			    { return symbol(TokenNames.ARRAY);}
    "class"			    { return symbol(TokenNames.CLASS);}
    "extends"			{ return symbol(TokenNames.EXTENDS);}
    "return"			{ return symbol(TokenNames.RETURN);}
    "while"			    { return symbol(TokenNames.WHILE);}
    "if"			    { return symbol(TokenNames.IF);}
    "new"			    { return symbol(TokenNames.NEW);}
    {INTEGER}			{
                            if((yytext().charAt(0) != '0' || yytext().length() == 1) && yytext().length() <= 5){
                                int n = new Integer(yytext());
                                if (n < Math.pow(2, 15)) return symbol(TokenNames.INT, n);
                            }
                            throw new Error("Error: could not match input");
                        }
    {STRING}			{ return symbol(TokenNames.STRING, new String(yytext()));}
    {ID}				{ return symbol(TokenNames.ID, new String(yytext()));}
    {WHITE_SPACE}		{ /* just skip what was found, do nothing */ }

    {START_COMMENT_TYPE_1} { yybegin(COMMENT_TYPE_1);}
    {START_COMMENT_TYPE_2} { yybegin(COMMENT_TYPE_2);}

    <<EOF>>				{ return symbol(TokenNames.EOF);}
}

<COMMENT_TYPE_1> {
    {LINE_TERMINATOR}   { yybegin(YYINITIAL);}
    {VALID_COMMENT}     { /* ignore */ }
    <<EOF>>				{ return symbol(TokenNames.EOF);}
    [^]                 { throw new Error("Error: could not match input");}
}

<COMMENT_TYPE_2> {
    {END_COMMENT_TYPE_2}                    { yybegin(YYINITIAL);}
    {VALID_COMMENT} | {LINE_TERMINATOR}     { /* ignore */ }
    <<EOF>>                                 { throw new Error("Error: could not match input");}
    [^]                                     { throw new Error("Error: could not match input");}
}


