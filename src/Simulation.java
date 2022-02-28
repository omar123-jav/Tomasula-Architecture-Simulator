import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
public class Simulation {
	Hashtable<String, Integer> type_cycles = new Hashtable<String, Integer>();
	ArrayList<MemoryEntity> loadStation = new ArrayList<MemoryEntity>();
	ArrayList<MemoryEntity> storeStation = new ArrayList<MemoryEntity>();
	ArrayList<ALUEntity> addSubStation = new ArrayList<ALUEntity>();
	ArrayList<ALUEntity> mulDivStation = new ArrayList<ALUEntity>();
	ArrayList<TableEntity> instructionTable = new ArrayList<TableEntity>();
	ArrayList<RegisterEntity> registerFile = new ArrayList<RegisterEntity>();
	ArrayList<Double> memory = new ArrayList<Double>();
	
	int clock = 1;
	
	public void initialize() {
		
		for (int i = 0; i < 1024; i++) {
			memory.add(0.0);
		}
		for (int i = 0; i < 11; i++) {
			registerFile.add(new RegisterEntity("F"+i,"0",i));
		}
		System.out.println("added");
		for (int i = 0;i<2;i++) {
			System.out.println("added");
			loadStation.add( new MemoryEntity());
			storeStation.add( new MemoryEntity());
		}
		for (int i = 0; i < 3; i++) {
			addSubStation.add( new ALUEntity());
			mulDivStation.add( new ALUEntity());
		}
		//type_cycles.put("L.D", 2);
		//type_cycles.put("S.D", 1);
		//type_cycles.put("MUL.D", 10);
		//type_cycles.put("DIV.D", 40);
		//type_cycles.put("ADD.D", 2);
		//type_cycles.put("SUB.D", 2);
	}
	
	public static int isFull(ArrayList<MemoryEntity> array,int size) {
		for (int i =0;i<size;i++) {
			if(array.get(i).busy==false) {
				return i;
			}
		}
		return -1;
	}
	public static int isFull2(ArrayList<ALUEntity> array,int size) {
		for (int i =0;i<size;i++) {
			if(array.get(i).busy==false) {
				return i;
			}
		}
		return -1;
	}
	public void whoNeedsMe(String opName,String required){
          String op=opName.charAt(0)+"";
          int index=Integer.parseInt(opName.substring(1,opName.length()));
          switch(op){
			  case "M": mulDivStation.get(index).whoNeedsMe.add(required);
			  break;
			  case "A": addSubStation.get(index).whoNeedsMe.add(required);
			  break;
			  case "L": loadStation.get(index).whoNeedsMe.add(required);
			  break;
		  }
	}
	
	public void occupyInRegFile(String regName,String occupiedBy) {
		for (int i = 0; i < registerFile.size(); i++) {
			if(registerFile.get(i).regName.equals(regName)) {
				registerFile.get(i).Qi=occupiedBy;
			}
		}
	}
	public String searchInRegFile(String regName){
		for (int i = 0; i < registerFile.size(); i++) {
			if(regName.equals(registerFile.get(i).regName)){
				if(registerFile.get(i).Qi.equals("0")){
                     return registerFile.get(i).value+"";
				}
				else
					return registerFile.get(i).Qi;
			}

		}
		return "instruction is invalid";
	}
public double performOp(double reg1,double reg2,String op){
		switch(op){
			case "ADD": return reg1+reg2;

			case "SUB": return reg1-reg2;

			case "MUL": return reg1*reg2;

			case "DIV": return reg1/reg2;

			case "L": return memory.get((int)reg1);


			default : return 0.0;
		}
}
//bus method
public void whoNeedsMeAssign(ArrayList<String> whoNM,String required,double value){
	for (int i = 0; i < whoNM.size(); i++) {
		String op=whoNM.get(i).charAt(0)+"";
		int index=Integer.parseInt(whoNM.get(i).substring(1,whoNM.get(i).length()));
		switch(op){
			// assiging values to the whoneeds me
			// simulating writing values on the bus
			case "M":if(mulDivStation.get(index).Qj.equals(required)){
				mulDivStation.get(index).Qj="";
				mulDivStation.get(index).Vj= value;
			}
				if(mulDivStation.get(index).Qk.equals(required)){
					mulDivStation.get(index).Qk="";
					mulDivStation.get(index).Vk= value;
				}
				whoNM.remove(i--);
          break;
			case "A":if(addSubStation.get(index).Qj.equals(required)){
				addSubStation.get(index).Qj="";
				addSubStation.get(index).Vj= value;
			}
				if(addSubStation.get(index).Qk.equals(required)){
					addSubStation.get(index).Qk="";
					addSubStation.get(index).Vk= value;
				}
				whoNM.remove(i--);
				break;
		}

	}
}

	public void removeFromRegFile(String Qi, double value) {
		for (int i = 0; i < registerFile.size(); i++) {
			if(registerFile.get(i).Qi.equals(Qi)) {
				registerFile.get(i).Qi="0";
				registerFile.get(i).value=value;
			}
		}
	}
	public void executeStageALU(ArrayList<ALUEntity> station, String st, ArrayList<ArrayList<String>> whoNeedsMeList
			,ArrayList<String> opNameList , ArrayList<Double> valuesList) {
		for (int i = 0; i < station.size(); i++) {
			if(station.get(i).busy) {
			int instIndex=station.get(i).index;
			String opName= instructionTable.get(instIndex).opCode;
			if(station.get(i).Qj.equals("")&& station.get(i).Qk.equals("")&& instructionTable.get(instIndex).issueCycle !=0){
				if(instructionTable.get(instIndex).eEndCycle!=0){
					if(instructionTable.get(instIndex).writeResult==0){
						double value=performOp(station.get(i).Vj,station.get(i).Vk,station.get(i).op);
						//whoNeedsMeAssign(station.get(i).whoNeedsMe,st+i,value);
						whoNeedsMeList.add(station.get(i).whoNeedsMe);
						opNameList.add(st+i);
						valuesList.add(value);
						instructionTable.get(instIndex).writeResult=clock;
						//station.set(i, new ALUEntity());
						removeFromRegFile(st+i,value);
					}
				}else {
					if (instructionTable.get(instIndex).eStartCycle == 0) {
						instructionTable.get(instIndex).eStartCycle = clock;
					}
						int cycles = type_cycles.get(opName);
						if (clock - instructionTable.get(instIndex).eStartCycle + 1 == cycles) {
							instructionTable.get(instIndex).eEndCycle = clock;
						}

				}
			}
			}
		}
	}
	public void executeStageMemS(ArrayList<MemoryEntity> station,String st,ArrayList<String> opNameList){
		for (int i = 0; i < station.size(); i++) {
			if(station.get(i).busy) {
				int instIndex=station.get(i).index;
				String opName= instructionTable.get(instIndex).opCode;
				if(instructionTable.get(instIndex).issueCycle !=0){
					if(instructionTable.get(instIndex).eEndCycle!=0){
						if(instructionTable.get(instIndex).writeResult==0){
							String regName = instructionTable.get(instIndex).dist;
							for (int j = 0; j < registerFile.size(); j++) {
								if(regName.equals(registerFile.get(j).regName) && registerFile.get(j).Qi=="0"){
									memory.set(station.get(i).address,registerFile.get(j).value);
								}
							}
							//whoNeedsMeAssign(station.get(i).whoNeedsMe,st+i,value);
							opNameList.add(st+i);
							instructionTable.get(instIndex).writeResult=clock;
							//station.set(i, new ALUEntity());
						}
					}else {
						if (instructionTable.get(instIndex).eStartCycle == 0) {
							instructionTable.get(instIndex).eStartCycle = clock;
						}
						int cycles = type_cycles.get(opName);
						if (clock - instructionTable.get(instIndex).eStartCycle + 1 == cycles) {
							instructionTable.get(instIndex).eEndCycle = clock;
						}

					}
				}
			}
		}

	}
	public void executeStageMemL(ArrayList<MemoryEntity> station,String st, ArrayList<ArrayList<String>> whoNeedsMeList
			,ArrayList<String> opNameList , ArrayList<Double> valuesList){
		for (int i = 0; i < station.size(); i++) {
			if(station.get(i).busy) {
				int instIndex=station.get(i).index;
				String opName= instructionTable.get(instIndex).opCode;
				if(instructionTable.get(instIndex).issueCycle !=0){
					if(instructionTable.get(instIndex).eEndCycle!=0){
						if(instructionTable.get(instIndex).writeResult==0){
							double value=performOp(station.get(i).address,0,st);
							//whoNeedsMeAssign(station.get(i).whoNeedsMe,st+i,value);
							whoNeedsMeList.add(station.get(i).whoNeedsMe);
							opNameList.add(st+i);
							valuesList.add(value);
							instructionTable.get(instIndex).writeResult=clock;
							//station.set(i, new ALUEntity());
							removeFromRegFile(st+i,value);
						}
					}else {
						if (instructionTable.get(instIndex).eStartCycle == 0) {
							instructionTable.get(instIndex).eStartCycle = clock;
						}
							int cycles = type_cycles.get(opName);
							if (clock - instructionTable.get(instIndex).eStartCycle + 1 == cycles) {
								instructionTable.get(instIndex).eEndCycle = clock;
							}

					}
				}
			}
		}
	}
	// add F3,F2,F1
	// add F4,F3,F1
	public void simulate() {
		// this function represents the flow 
		//TODO make sure to print at the end of each cycle 
		initialize();
		int instTableIndex = 0;
		//boolean isIssued = false;

		while(!isExecutionDone()) {
			// Execution Section :(
			ArrayList<ArrayList<String>> whoNeedsMeList = new ArrayList<ArrayList<String>>();
			ArrayList<String> opNameList = new ArrayList<String>();
			ArrayList<Double> valuesList = new ArrayList<Double>();
			executeStageALU(addSubStation, "A",whoNeedsMeList,opNameList,valuesList);
			executeStageALU(mulDivStation, "M",whoNeedsMeList,opNameList,valuesList);
			executeStageMemL(loadStation, "L",whoNeedsMeList,opNameList,valuesList);
			executeStageMemS(storeStation, "S",opNameList);

			for (int i = 0; i < whoNeedsMeList.size(); i++) {
				whoNeedsMeAssign(whoNeedsMeList.get(i),opNameList.get(i),valuesList.get(i));
			}
            //issue section :)
			if(instTableIndex<instructionTable.size()) {
				TableEntity currentTableRow = instructionTable.get(instTableIndex);
				int index;
				switch (currentTableRow.opCode) {
					case "L.D":
						index = isFull(loadStation, 2);
						if (index != -1) {
							loadStation.get(index).busy = true;
							loadStation.get(index).address = Integer.parseInt(currentTableRow.j);
							currentTableRow.issueCycle = clock;
							occupyInRegFile(currentTableRow.dist, "L" + index);
							loadStation.get(index).index = instTableIndex;
							instTableIndex++;
						}
						break;
					case "S.D":
						index = isFull(storeStation, 2);
						if (index != -1) {
							storeStation.get(index).busy = true;
							storeStation.get(index).address = Integer.parseInt(currentTableRow.j);
							currentTableRow.issueCycle = clock;
							storeStation.get(index).index = instTableIndex;
							instTableIndex++;
						}
						break;
					case "MUL.D":
						index = isFull2(mulDivStation, 3);
						if (index != -1) {
							currentTableRow.issueCycle = clock;
							mulDivStation.get(index).busy = true;
							mulDivStation.get(index).op = "MUL";
							String op1 = searchInRegFile(currentTableRow.j);
							String op2 = searchInRegFile(currentTableRow.k);
							try {
								double value = Double.parseDouble(op1);
								mulDivStation.get(index).Vj = value;
							} catch (Exception e) {
								mulDivStation.get(index).Qj = op1;
								whoNeedsMe(op1, "M" + index);
							}
							try {
								double value = Double.parseDouble(op2);
								mulDivStation.get(index).Vk = value;
							} catch (Exception e) {
								mulDivStation.get(index).Qk = op2;
								whoNeedsMe(op2, "M" + index);

							}
							occupyInRegFile(currentTableRow.dist, "M" + index);
							mulDivStation.get(index).index = instTableIndex;
							instTableIndex++;
						}
						break;
					case "DIV.D":
						index = isFull2(mulDivStation, 3);
						if (index != -1) {
							currentTableRow.issueCycle = clock;

							mulDivStation.get(index).busy = true;
							mulDivStation.get(index).op = "DIV";
							String op1 = searchInRegFile(currentTableRow.j);
							String op2 = searchInRegFile(currentTableRow.k);
							try {
								double value = Double.parseDouble(op1);
								mulDivStation.get(index).Vj = value;
							} catch (Exception e) {
								mulDivStation.get(index).Qj = op1;
								whoNeedsMe(op1, "M" + index);

							}
							try {
								double value = Double.parseDouble(op2);
								mulDivStation.get(index).Vk = value;
							} catch (Exception e) {
								mulDivStation.get(index).Qk = op2;
								whoNeedsMe(op2, "M" + index);


							}
							occupyInRegFile(currentTableRow.dist, "M" + index);
							mulDivStation.get(index).index = instTableIndex;
							instTableIndex++;
						}
						break;
					case "ADD.D":
						index = isFull2(addSubStation, 3);
						if (index != -1) {
							currentTableRow.issueCycle = clock;
							occupyInRegFile(currentTableRow.dist, "A" + index);
							addSubStation.get(index).busy = true;
							addSubStation.get(index).op = "ADD";
							String op1 = searchInRegFile(currentTableRow.j);
							String op2 = searchInRegFile(currentTableRow.k);
							try {
								double value = Double.parseDouble(op1);
								addSubStation.get(index).Vj = value;
							} catch (Exception e) {
								addSubStation.get(index).Qj = op1;
								whoNeedsMe(op1, "A" + index);
							}
							try {
								double value = Double.parseDouble(op2);
								addSubStation.get(index).Vk = value;
							} catch (Exception e) {
								addSubStation.get(index).Qk = op2;
								whoNeedsMe(op2, "A" + index);
							}
							occupyInRegFile(currentTableRow.dist, "A" + index);
							addSubStation.get(index).index = instTableIndex;
							instTableIndex++;
						}

						break;
					case "SUB.D":
						index = isFull2(addSubStation, 3);
						if (index != -1) {
							currentTableRow.issueCycle = clock;
							occupyInRegFile(currentTableRow.dist, "A" + index);
							addSubStation.get(index).busy = true;
							addSubStation.get(index).op = "SUB";
							String op1 = searchInRegFile(currentTableRow.j);
							String op2 = searchInRegFile(currentTableRow.k);
							try {
								double value = Double.parseDouble(op1);
								addSubStation.get(index).Vj = value;
							} catch (Exception e) {
								addSubStation.get(index).Qj = op1;
								whoNeedsMe(op1, "A" + index);
							}
							try {
								double value = Double.parseDouble(op2);
								addSubStation.get(index).Vk = value;
							} catch (Exception e) {
								addSubStation.get(index).Qk = op2;
								whoNeedsMe(op2, "A" + index);
							}
							occupyInRegFile(currentTableRow.dist, "A" + index);
							addSubStation.get(index).index = instTableIndex;
							instTableIndex++;
						}
						break;
				}
			}
			//Do the whole logic here
			//go Over the instructions table from start to end 
			// on each instructions see if there anything that can be done t it (i.e. issue, execute, writeback)
			// in case there's an action on it, go do the action and update who needs me mechanism
			// else skip the the instruction and go to the one after it 
			//things to pay attention to: 
			
			
			
			//remove after issue
			
			for (int i = 0; i < opNameList.size(); i++) {
				String op=opNameList.get(i).charAt(0)+"";
				int index=Integer.parseInt(opNameList.get(i).substring(1,opNameList.get(i).length()));
				switch(op){
					case "A":addSubStation.set(index, new ALUEntity());break;
					case "M":mulDivStation.set(index, new ALUEntity());break;
					case "L":loadStation.set(index, new MemoryEntity());break;
					case "S":storeStation.set(index,new MemoryEntity());break;
				}
			}
			
			printOutput();
			clock++;	
		}

		
		
		
		
	}
	public boolean isExecutionDone() {
		for (int i = 0; i < instructionTable.size(); i++) {
			if(instructionTable.get(i).writeResult==0){
				return false;
			}
		}
		
		return true;
	}
	
	public void loadInput(String Instruction) {
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
			type_cycles.put(instruction[0],cycles);
			
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
			//TableEntity ld=new TableEntity(instruction[0],dstsrc,add,"0");
			instructionTable.add(new TableEntity(instruction[0],dstsrc,add,"0"));
			}else {
				//TableEntity st=new TableEntity(instruction[0],add,dstsrc,"0");
			 instructionTable.add(new TableEntity(instruction[0],dstsrc,add,"0"));
			}
		
		}
		else {
			String destination=instruction[1].substring(0,instruction[1].length()-1);
			String operand1=instruction[2].substring(0,instruction[2].length()-1);
			String operand2=instruction[3];
			//TableEntity alu=new TableEntity(instruction[0],destination,operand1,operand2);
			instructionTable.add(new TableEntity(instruction[0],destination,operand1,operand2));
			
		}
	
		}
		
	}
	public  void printOutput() {
		System.out.println("Clock Cycle: " + clock);
		System.out.println();
		System.out.println("RegFileContent");
		for (int i = 0; i < registerFile.size(); i++) {
			System.out.println(registerFile.get(i).regName + " " +"Availability:"+" "+ registerFile.get(i).Qi + " " +"Value: "+ registerFile.get(i).value + " ");
		}
		System.out.println();
		System.out.println("TableContent");
		for (int i = 0; i < instructionTable.size(); i++) {
			if(!(instructionTable.get(i).opCode.equals("L.D")||instructionTable.get(i).opCode.equals("S.D"))) {
			System.out.println("opCode: "+instructionTable.get(i).opCode + " " +"destination: "+instructionTable.get(i).dist 
					+ " " +"operand1: "+instructionTable.get(i).j + " operand2: " + instructionTable.get(i).k + " issueCycle: "
					+ instructionTable.get(i).issueCycle + " StartCycle: " + instructionTable.get(i).eStartCycle + " EndCycle: "
					+ instructionTable.get(i).eEndCycle + " writeResult: " + instructionTable.get(i).writeResult + " ");
			}else {
				System.out.println("opCode: "+instructionTable.get(i).opCode + " " +"Register: "+instructionTable.get(i).dist 
						+ " " +"address: "+instructionTable.get(i).j  + " issueCycle: "
						+ instructionTable.get(i).issueCycle + " StartCycle: " + instructionTable.get(i).eStartCycle + " EndCycle: "
						+ instructionTable.get(i).eEndCycle + " writeResult: " + instructionTable.get(i).writeResult + " ");
			}
		}
		System.out.println();
		System.out.println("AddSubStation");
		for (int i = 0; i < addSubStation.size(); i++) {
			System.out.println("Busy: "+addSubStation.get(i).busy + " operation: " + addSubStation.get(i).op + " Vj: " 
					+ addSubStation.get(i).Vj + " Vk: " + addSubStation.get(i).Vk + " Qj: " 
					+ addSubStation.get(i).Qj + " Qk: " + addSubStation.get(i).Qk + " WhoNeedsMe: "
					+ addSubStation.get(i).whoNeedsMe + " Index: " + addSubStation.get(i).index + " ");
		}
		System.out.println();
		System.out.println("MulDivStation");
		for (int i = 0; i < mulDivStation.size(); i++) {
			System.out.println("Busy: "+mulDivStation.get(i).busy + " operation: " + mulDivStation.get(i).op + " Vj: " 
					+ mulDivStation.get(i).Vj + " Vk: " + mulDivStation.get(i).Vk + " Qj: " 
					+ mulDivStation.get(i).Qj + " Qk: " + mulDivStation.get(i).Qk + " WhoNeedsMe: "
					+ mulDivStation.get(i).whoNeedsMe + " Index: " + mulDivStation.get(i).index + " ");
		}
		System.out.println();
		System.out.println("LoadStation");
		for(int i=0;i<loadStation.size();i++) {
			System.out.println("Index: "+loadStation.get(i).index+" Busy: "+loadStation.get(i).busy+" Address: "+loadStation.get(i).address+" WhoNeedsMe: "+loadStation.get(i).whoNeedsMe);
		}
		System.out.println();
		System.out.println("StoreStation");
		for(int i=0;i<loadStation.size();i++) {
			System.out.println("Index: "+storeStation.get(i).index+" Busy: "+storeStation.get(i).busy+" Address: "+storeStation.get(i).address+" WhoNeedsMe: "+storeStation.get(i).whoNeedsMe);
		}
	}

	public static void main (String [] args) throws FileNotFoundException {
		//String instructionsFile = File.read;
		Simulation s =new Simulation();
	File instructions = new File("input3.txt");
	     Scanner myReader = new Scanner(instructions);
	     while (myReader.hasNextLine()) {
	       String instruction = myReader.nextLine();
	       if(instruction.length()>0) {
	    	s.loadInput(instruction);
	       }     
	     }
	     myReader.close();
	    /*
		ArrayList<TableEntity> test=new ArrayList<TableEntity>();
		//test.add(new TableEntity("S.D","F7","7","0"));
		test.add(new TableEntity("L.D","F6","32","0"));
		test.add(new TableEntity("L.D","F2","44","0"));
		test.add(new TableEntity("MUL.D","F0","F2","F4"));
		//.add(new TableEntity("ADD.D","F6","F4","F1"));
		//test.add(new TableEntity("DIV.D","F1","F3","F1"));
		test.add(new TableEntity("SUB.D","F8","F2","F6"));
		test.add(new TableEntity("DIV.D","F10","F0","F6"));
		//test.add(new TableEntity("SUB.D","F5","F6","F1"));
		//test.add(new TableEntity("SUB.D","F10","F4","F2"));
		test.add(new TableEntity("ADD.D","F6","F8","F2"));
		*/
		
		
		s.simulate();
		System.out.println(s.memory.get(300));
		//System.out.println(s.memory.get(7));
		
	}
}
