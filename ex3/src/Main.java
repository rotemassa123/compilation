
import java.io.*;
import java.io.PrintWriter;
import java_cup.runtime.Symbol;
import AST.*;

public class Main
{
	static public void main(String argv[])
	{
		Lexer l;
		Parser p;
		Symbol s;
		AST_LIST<AST_STMT> AST;
		FileReader file_reader;
		PrintWriter file_writer;
		String inputFilename = argv[0];
		String outputFilename = argv[1];
		String outputText = "";

		try
		{
			/********************************/
			/* [1] Initialize a file reader */
			/********************************/
			file_reader = new FileReader(inputFilename);

			/********************************/
			/* [2] Initialize a file writer */
			/********************************/
			file_writer = new PrintWriter(outputFilename);

			try{
				/******************************/
				/* [3] Initialize a new lexer */
				/******************************/
				l = new Lexer(file_reader);

				/*******************************/
				/* [4] Initialize a new parser */
				/*******************************/
				p = new Parser(l, outputFilename);

				/***********************************/
				/* [5] 3 ... 2 ... 1 ... Parse !!! */
				/***********************************/
				AST = (AST_LIST<AST_STMT>) p.parse().value;

				/*************************/
				/* [6] Print the AST ... */
				/*************************/
				AST.PrintMe();

				System.out.println("SEMANTME ==================================================================================================");

				/**************************/
				/* [7] Semant the AST ... */
				/**************************/
				AST.SemantMe();
				System.out.println("DONE SEMANTME ==================================================================================================");

				/*************************************/
				/* [8] Finalize AST GRAPHIZ DOT file */
				/*************************************/
				AST_GRAPHVIZ.getInstance().finalizeFile();

				file_writer.write("OK");
			}

			catch (SemanticException e) {
				file_writer.write(String.format("ERROR(%d)", e.node.line + 1));
				//file_writer.write(String.format("ERROR(%s)", e.node.getClass()));
			}

			catch (Error e)
			{
				file_writer.write("ERROR");
			}
			file_writer.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

