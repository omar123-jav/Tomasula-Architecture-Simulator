import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner;
public class Main {
public static void Interpret(String Instruction) {
	String[] instruction=Instruction.split(" ");
	String type="";

	if(instruction.length==2) {
		
		if(instruction[0].equals("L.D")) {
			   type="load";
			}else if(instruction[0].equals("S.D")) {
				type="store";
			}else if(instruction[0].equals("ADD.D")) {
				type="add";
			}else if(instruction[0].equals("SUB.D")) {
				type="sub";
			}else if(instruction[0].equals("MUL.D")) {
				
				type="mult";
			}else if(instruction[0].equals("DIV.D")) {
				type="div";
			}
		int cycles=Integer.valueOf(instruction[1]);
		
	}else {
		
	
	if(instruction[0].equals("L.D")) {
	   type="load";
	}else if(instruction[0].equals("S.D")) {
		type="store";
	}else if(instruction[0].equals("ADD.D")) {
		type="add";
	}else if(instruction[0].equals("SUB.D")) {
		type="sub";
	}else if(instruction[0].equals("MUL.D")) {
		type="mult";
	}else if(instruction[0].equals("DIV.D")) {
		type="div";
	}
	if(type.equals("load")||type.equals("store")) {
		String dstsrc=instruction[1].substring(0,instruction[1].length()-1);
		int address=Integer.valueOf(instruction[2]);
		String add=instruction[2];
		if(type.equals("load")) {
		TableEntity ld=new TableEntity(type,dstsrc,add,null);
		}else {
			TableEntity st=new TableEntity(type,add,dstsrc,null);
		}
	}else {
		String destination=instruction[1].substring(0,instruction[1].length()-1);
		String operand1=instruction[2].substring(0,instruction[2].length()-1);
		String operand2=instruction[3];
		TableEntity alu=new TableEntity(type,destination,operand1,operand2);
	}
	
	}
}
public static void main(String[] args) throws FileNotFoundException {
	 File instructions = new File("input3.txt");
     Scanner myReader = new Scanner(instructions);
     while (myReader.hasNextLine()) {
       String instruction = myReader.nextLine();
       if(instruction.length()>0) {
    	Interpret(instruction);
       }     
     }
     myReader.close();
}
}
