package lab3;

import java.util.ArrayList;
import java.util.List;

public class SymbolTable {
	String name;
	List<Symbol> table;
	SymbolTable father;
	List<SymbolTable> sons;
	int offset;
	
	public SymbolTable(){
		this.table = new ArrayList<Symbol>();
		this.sons = new ArrayList<SymbolTable>();
	}
	
	
	@Override
	public String toString(){
		String result = "";
		result += name + "\n";
		for(int i=0;i<table.size();i++){
			result += table.get(i) + "\n";
		}
		for(int i=0;i<this.sons.size();i++){
			result += "(" + sons.get(i).name + ")\n";
		}
		if(father!=null){
			result += "father:" + father.name + "\n";
		}
		return result;
	}
}
