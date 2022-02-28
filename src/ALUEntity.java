

import java.util.ArrayList;

class ALUEntity{
	int index;
	boolean busy;
	String op;
	double Vj;
	double Vk;
	String Qj;
	String Qk;
	ArrayList<String> whoNeedsMe;
	public ALUEntity(){
		busy = false;
		op = "";Vj=0;Vk=0;Qj="";Qk="";
		whoNeedsMe = new ArrayList<String>();
	}
}
