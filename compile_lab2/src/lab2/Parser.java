package lab2;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import lab1.Lex;
import lab1.Token;

public class Parser {
	static int count = 0;
	List<Production> production_list = new ArrayList<Production>();
	Set<String> terminal_set = new HashSet<String>();
	Set<String> non_terminal_set = new HashSet<String>();
	List<ItemSet> item_set_list = new ArrayList<ItemSet>();
	AnalysisTable table;
	int stateNum;
	List<Integer> line_index = new ArrayList<Integer>();
	List<String> error = new ArrayList<String>();
	List<String> output = new ArrayList<String>();
	
	
	//////////////
	public Set<Item> getClosure(Set<Item> result){
		int size = result.size();
		while(true){
			result = this.countClosure(result);
			if(size == result.size()){
				break;
			} else{
				size = result.size();
			}
		}
		return result;
	}
	
	public Set<Item> countClosure(Set<Item> item){
		List<Item> it_list = new ArrayList<Item>(item);
		
		for(int j=0;j<it_list.size();j++){
			if(it_list.get(j).after_point.size()==0){
				continue;
			}
			if(this.isNonTerminal(it_list.get(j).after_point.get(0))){
				List<Production> plist = this.getProduction(it_list.get(j).after_point.get(0));
				List<String> search = this.getStringFirst(it_list.get(j).after_point, it_list.get(j).search_character);
				for(int i=0;i<plist.size();i++){
					Item it = new Item();
					it.left = it_list.get(j).after_point.get(0);
					it.before_point = new ArrayList<String>();
					it.after_point = plist.get(i).right;
					it.search_character = search;
					item.add(it);
				}
			}
		}
		return item;
	}

	
	public List<String> getFirst(String s){
		Set<String> result = new HashSet<String>();
		if(this.isTerminal(s)){
			result.add(s);
			return new ArrayList<String>(result);
		}
		for(int i=0;i<this.production_list.size();i++){
			if(this.production_list.get(i).left.equals(s)){
				Production p = this.production_list.get(i);
				if(this.terminal_set.contains(p.right.get(0))){
					result.add(p.right.get(0));
					continue;
				}
				if(this.non_terminal_set.contains(p.right.get(0)) && (!this.isNull(p.right.get(0)))){
					if(p.right.get(0).equals(s)){
						continue;
					}
					result.addAll(getFirst(p.right.get(0)));
					continue;
				}
				int j = 0;
				while(j<p.right.size() && this.isNull(p.right.get(j))){
					if(this.isNonTerminal(p.right.get(j))){
//						System.out.println(p.right.get(j));
						if(p.right.get(j+1).equals(s)){
							break;
						}
						List<String> result_right = getFirst(p.right.get(j));
						for(int k=0;k<result_right.size();k++){
							if(!result_right.get(k).equals("��")){
								result.add(result_right.get(k));
							}
						}
						j++;
					}
				}
				if(j==p.right.size()){
					result.add("��");
				} else{
					result.addAll(getFirst(p.right.get(j)));
					result.add("��");
				}
			}
		}
		return new ArrayList<String>(result);
	}
	
	public boolean isTerminal(String s){
		return this.terminal_set.contains(s);
	}
	
	public boolean isNonTerminal(String s){
		return this.non_terminal_set.contains(s);
	}
	
	public boolean isNull(String s){
		for(int i=0;i<this.production_list.size();i++){
			if(this.production_list.get(i).left.equals(s)){
				//TODO ��α�ʾ��
				if(this.production_list.get(i).right.contains("��")){
					return true;
				}
			}
		}
		return false;
	}
	
	public void readFromFile(String path){
		List<String> lines = new ArrayList<String>();
		try {
			FileInputStream fis = new FileInputStream(path);   
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);  
			String str;
			while((str=br.readLine())!=null){
				lines.add(str);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int i=0;i<lines.size();i++){
			this.non_terminal_set.add(lines.get(i).split("->")[0]);
		}
		for(int i=0;i<lines.size();i++){
			
			
			/*String[] words = lines.get(i).split("->");
			p.left = words[0];
			p.right = new ArrayList<String>();
			String[] content = words[1].split(" ");
			for(int j=0;j<content.length;j++){
				p.right.add(content[j]);
			}
//			System.out.println(p);
			this.production_list.add(p);*/
			String left = lines.get(i).split("->")[0];
			String[] right = lines.get(i).split("->")[1].split("\\|");
			for(int j=0;j<right.length;j++){
				Production p = new Production();
				p.left = left;
				p.right = new ArrayList<String>();
				String[] content = right[j].split(" ");
				for(int k=0;k<content.length;k++){
					if(!this.isNonTerminal(content[k])){
						this.terminal_set.add(content[k]);
					}
					p.right.add(content[k]);
				}
				this.production_list.add(p);
			}
		}
	}
	
	public List<Production> getProduction(String head){
		List<Production> plist = new ArrayList<Production>();
		for(int i=0;i<this.production_list.size();i++){
			if(this.production_list.get(i).left.equals(head)){
				plist.add(this.production_list.get(i));
			}
		}
		return plist;
	}
	
	public List<String> getStringFirst(List<String> input1, List<String> input2){
		int i = 1;
		List<String> result = new ArrayList<String>();
		int flag = 0;
		while(i<input1.size()){
			List<String> first = this.getFirst(input1.get(i));
			if(first.contains("��")){
				for(int j=0;j<first.size();j++){
					if(!first.get(j).equals("��")){
						result.add(first.get(j));
					}
				}
				i++;
			} else{
				result.addAll(first);
				flag = 1;
				break;
			}
		}
		if(flag == 0){
			result.addAll(input2);
		}
		return result;
	}
	
	public ItemSet goTo(ItemSet i, String x){
		ItemSet j = new ItemSet();
		Set<Item> iset = new HashSet<Item>();
		List<Item> itlist = new ArrayList<Item>(i.itemSet);
		for(int k=0;k<itlist.size();k++){
			if(itlist.get(k).after_point.size() == 0){
				continue;
			} else{
				if(itlist.get(k).after_point.get(0).equals(x)){
					Item it = new Item();
					it.before_point = new ArrayList<String>();
					it.after_point = new ArrayList<String>();
					it.left = itlist.get(k).left;
					for(int m=0;m<itlist.get(k).before_point.size();m++){
						it.before_point.add(itlist.get(k).before_point.get(m));
					}
					it.before_point.add(itlist.get(k).after_point.get(0));
					for(int n=1;n<itlist.get(k).after_point.size();n++){
						it.after_point.add(itlist.get(k).after_point.get(n));
					}
					it.search_character = itlist.get(k).search_character;
					iset.add(it);
				}
			}
		}
		j.itemSet = this.getClosure(iset);
		return j;
	}
	
	public void getItemSet(){
		Item start = new Item();
		start.left = "S1";
		start.before_point = new ArrayList<String>();
		start.after_point = new ArrayList<String>();
		start.search_character = new ArrayList<String>();
		start.after_point.add("P");
		start.search_character.add("#");
		Set<Item> start1 = new HashSet<Item>();
		start1.add(start);
		Set<Item> start_set = this.getClosure(start1);
		ItemSet start_itemset = new ItemSet();
		start_itemset.num = 0;
		start_itemset.itemSet = start_set;
		this.item_set_list.add(start_itemset);
		int count = 1;
		while(true){
			int size = this.item_set_list.size();
			for(int i=0;i<this.item_set_list.size();i++){
				List<String> not = new ArrayList<String>(this.non_terminal_set);
				for(int j=0;j<not.size();j++){
					if(this.goTo(this.item_set_list.get(i), not.get(j)).itemSet.size() > 0 && !this.item_set_list.contains(this.goTo(this.item_set_list.get(i), not.get(j)))){
						ItemSet p = new ItemSet();
						p = this.goTo(this.item_set_list.get(i), not.get(j));
						p.num = count;
						this.item_set_list.get(i).go.put(not.get(j), count);
						this.item_set_list.add(p);
						count++;
					}
					if(this.item_set_list.contains(this.goTo(this.item_set_list.get(i), not.get(j)))){
						int index = 0;
						for(int a=0;a<this.item_set_list.size();a++){
							if(this.item_set_list.get(a).equals(this.goTo(this.item_set_list.get(i), not.get(j)))){
								index = a;
							}
						}
						this.item_set_list.get(i).go.put(not.get(j), index);
					}
				}
				List<String> t = new ArrayList<String>(this.terminal_set);
				for(int j=0;j<t.size();j++){
					if(!t.get(j).equals("��")){
						if(this.goTo(this.item_set_list.get(i), t.get(j)).itemSet.size() > 0 && !this.item_set_list.contains(this.goTo(this.item_set_list.get(i), t.get(j)))){
							ItemSet p = new ItemSet();
							p = this.goTo(this.item_set_list.get(i), t.get(j));
							p.num = count;
							this.item_set_list.get(i).go.put(t.get(j), count);
							this.item_set_list.add(p);
							count++;
						}
						if(this.item_set_list.contains(this.goTo(this.item_set_list.get(i), t.get(j)))){
							int index = 0;
							for(int a=0;a<this.item_set_list.size();a++){
								if(this.item_set_list.get(a).equals(this.goTo(this.item_set_list.get(i), t.get(j)))){
									index = a;
								}
							}
							this.item_set_list.get(i).go.put(t.get(j), index);
						}
					}
				}
			}
			if(this.item_set_list.size() == size){
				break;
			}
		}
		this.stateNum = count - 1;
	}
	
	/*public void getItemSet(){
		Item start = new Item();
		start.left = "S1";
		start.before_point = new ArrayList<String>();
		start.after_point = new ArrayList<String>();
		start.search_character = new ArrayList<String>();
		start.after_point.add("P");
		start.search_character.add("#");
		Set<Item> start1 = new HashSet<Item>();
		start1.add(start);
		Set<Item> start_set = this.getClosure(start1);
		ItemSet start_itemset = new ItemSet();
		start_itemset.num = 0;
		start_itemset.itemSet = start_set;
		this.item_set_list.add(start_itemset);
//		System.out.println(start_itemset);
		
		int count = 1;
		while(true){
			int loop_flag = 0;
			ItemSet set = new ItemSet();
			set.num = count;
			for(int i=0;i<count;i++){
				ItemSet current = this.item_set_list.get(i);
				List<Item> itlist = new ArrayList<Item>(current.itemSet);
				for(int j=0;j<itlist.size();j++){
					if(itlist.get(j).flag == 0){
						
						int repeat_flag = 0;
						Item current_item = itlist.get(j);
						current_item.flag = 1;
						if(current_item.after_point.size() == 0){
							continue;
						} else{
							Item new_item = new Item();
							String search_character = current_item.after_point.get(0);
							if(search_character.equals("��")){
								continue;
							}
							new_item.left = current_item.left;
							List<String> before = new ArrayList<String>();
							List<String> after = new ArrayList<String>();
							for(int k=0;k<current_item.before_point.size();k++){
								before.add(current_item.before_point.get(k));
							}
							before.add(current_item.after_point.get(0));
							for(int k=1;k<current_item.after_point.size();k++){
								after.add(current_item.after_point.get(k));
							}
							new_item.before_point = before;
							new_item.after_point = after;
							new_item.search_character = current_item.search_character;
							int repeat_num = 0;
							for(int m=0;m<count;m++){
								List<Item> itlist1 = new ArrayList<Item>(this.item_set_list.get(m).itemSet);
								for(int n=0;n<itlist1.size();n++){
									if(new_item.equals(itlist1.get(n))){
										repeat_flag = 1;
										repeat_num = this.item_set_list.get(m).num;
									}
								}
							}
							if(repeat_flag == 1){
								current.go.put(search_character, repeat_num);
								continue;
							} else{
								Set<Item> is = new HashSet<Item>();
								is.add(new_item);
								if(current.go.containsKey(search_character)){
									this.item_set_list.get(current.go.get(search_character)).itemSet.addAll(this.getClosure(is));
									break;
								}else{
									set.itemSet = this.getClosure(is);
									set.num = count;
									this.item_set_list.add(set);
									current.go.put(search_character, count);
									count ++;
									loop_flag = 1;
									break;
								}
							}
						}
					}
				}
				if(loop_flag == 1){
					break;
				}
			}
			if(loop_flag == 0){
				break;
			}
		}
		this.stateNum = count-1;
	}*/
	
	public void parser(String path){
		/*FileReader file;
		List<String> lines = new ArrayList<String>();
		try {
			file = new FileReader(path);
			BufferedReader br = new BufferedReader(file);
			String str;
			while((str=br.readLine())!=null){
				lines.add(str);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<String> input = new ArrayList<String>();
		for(int i=0;i<lines.get(0).length();i++){
			input.add(lines.get(0).charAt(i) + "");
		}*/
		List<String> input = new ArrayList<String>();
		Lex lex = new Lex();
		List<Token> token_list = lex.lex(path);
		for(int i=0;i<token_list.size();i++){
			if(token_list.get(i).property.equals("IDN")){
				input.add("id");
				this.line_index.add(token_list.get(i).line);
			} else if(token_list.get(i).property.equals("CONST_INT")||token_list.get(i).property.equals("CONST_FLOAT")){
				input.add("digit");
				this.line_index.add(token_list.get(i).line);
			}else if(token_list.get(i).property.equals("COMMENT")){
				continue;
			}else{
				input.add(token_list.get(i).value);
				this.line_index.add(token_list.get(i).line);
			}
		}
		input.add("#");
		this.line_index.add(this.line_index.get(this.line_index.size()-1));
		Stack<Integer> state_stack = new Stack<Integer>();
		Stack<String> parser_stack = new Stack<String>();
		state_stack.push(0);
		while(true){
			int break_flag = 0;
			String state = this.table.action_table[state_stack.peek()][this.table.terminals.indexOf(input.get(0))];
			if(state == null){
				this.error.add("Error at line " + this.line_index.get(0));
				String top = parser_stack.peek();
				state_stack.pop();
				while(true){
					while(!this.isNonTerminal(parser_stack.peek())){
						state_stack.pop();
						parser_stack.pop();
						if(state_stack.peek() == 0){
							break_flag = 1;
							break;
						}
						
					}
					if(break_flag == 1){
						break;
					}
					if(this.table.goto_table[state_stack.peek()][this.table.nonterminals.indexOf(parser_stack.peek())] == null){
						state_stack.pop();
						parser_stack.pop();
						if(state_stack.peek() == 0){
							break_flag = 1;
							break;
						}
						continue;
					} else{
						break;
					}
				}
				while(this.table.action_table[state_stack.peek()][this.table.terminals.indexOf(input.get(0))] == null){
					input.remove(0);
					this.line_index.remove(0);
					if(input.size() == 0){
						break_flag = 1;
						break;
					}
				}if(break_flag == 1){
					break;
				}
				continue;
			}
			if(break_flag == 1){
				break;
			}
			if(state.charAt(0) == 'S'){
				this.output.add(state);
				int new_state = Integer.parseInt(state.substring(1, state.length()));
				state_stack.push(new_state);
				parser_stack.push(input.get(0));
				input.remove(0);
				this.line_index.remove(0);
			}
			if(state.charAt(0) == 'r' && !state.split("->")[1].equals("��)")){
				this.output.add(state);
				int length = state.split("->")[1].split(" ").length;
				for(int i=0;i<length;i++){
					state_stack.pop();
					parser_stack.pop();
				}
				parser_stack.push(state.split("->")[0].substring(2, state.split("->")[0].length()));
				int new_state = Integer.parseInt(this.table.goto_table[state_stack.peek()][this.table.nonterminals.indexOf(state.split("->")[0].substring(2, state.split("->")[0].length()))]);
				state_stack.push(new_state);
			}
			if(state.charAt(0) == 'r' && state.split("->")[1].equals("��)")){
				this.output.add(state);
				parser_stack.push(state.split("->")[0].substring(2, state.split("->")[0].length()));
				int new_state = Integer.parseInt(this.table.goto_table[state_stack.peek()][this.table.nonterminals.indexOf(parser_stack.peek())]);
				state_stack.push(new_state);
			}
			if(state.equals("acc")){
				this.output.add(state);
				break;
			}
		}
	}
	
	public void fillTable(){
		table = new AnalysisTable(new ArrayList<String>(this.terminal_set),new ArrayList<String>(this.non_terminal_set),stateNum);
		System.out.println(this.terminal_set.size());
		for(int i=0;i<this.item_set_list.size();i++){
			ItemSet current = this.item_set_list.get(i);
			Iterator<Item> itr1 = current.itemSet.iterator();
			while(itr1.hasNext()){
				
				Item item = itr1.next();
				if(item.after_point.size() == 0 && (!item.left.equals("S1"))){
					for(int j=0;j<item.search_character.size();j++){
						Production p = new Production();
						p.left = item.left;
						p.right = item.before_point;
						table.action_table[current.num][table.terminals.indexOf(item.search_character.get(j))] = "r(" + p.toString() + ")";
					}
				}
				if(item.after_point.size() == 0 && item.left.equals("S1")){
					for(int j=0;j<item.search_character.size();j++){
						table.action_table[current.num][table.terminals.indexOf(item.search_character.get(j))] = "acc";
					}
				}
				if(item.after_point.size()>0 && this.isTerminal(item.after_point.get(0)) && current.go.containsKey(item.after_point.get(0))){
					//System.out.println(item.after_point.get(0));
					table.action_table[current.num][table.terminals.indexOf(item.after_point.get(0))] = "S" + current.go.get(item.after_point.get(0));
				}
				if(item.after_point.size()>0 && item.after_point.get(0).equals("��")){
					for(int j=0;j<item.search_character.size();j++){
						Production p = new Production();
						p.left = item.left;
						p.right = item.after_point;
						System.out.println(item.search_character.get(j));
						System.out.println(current.num);
						table.action_table[current.num][table.terminals.indexOf(item.search_character.get(j))] = "r(" + p.toString() + ")";
					}
				}
			}
			for (Map.Entry<String, Integer> entry : current.go.entrySet()) { 
				if(this.isNonTerminal(entry.getKey())){
					table.goto_table[current.num][table.nonterminals.indexOf(entry.getKey())] = entry.getValue() + "";
				}
			}
			
		}
	}
	
	public static void main(String args[]){
		Parser parser = new Parser();
		parser.readFromFile("input_wbh.txt");
		
		/*for(int i=0;i<parser.production_list.size();i++){
			for(int j=0;j<parser.production_list.get(i).right.size();j++){
				if(parser.isNonTerminal(parser.production_list.get(i).right.get(j))){
					
				} else if(parser.isTerminal(parser.production_list.get(i).right.get(j))){
					
				}else{
					System.out.println(parser.production_list.get(i).right.get(j));
				}
			}
		}*/
		
		parser.getItemSet();
		/*System.out.println(parser.item_set_list.size());
		for(int i=0;i<parser.item_set_list.size();i++){
			System.out.print(parser.item_set_list.get(i));
		}*/
		/*for(int i=0;i<parser.production_list.size();i++){
			System.out.println(parser.production_list.get(i));
		}*/
		parser.fillTable();
		//System.out.println(parser.table.action_table[0][parser.table.terminals.indexOf("id")]);
		
		parser.parser("test.txt");
		//System.out.println(parser.table);
		for(int i=0;i<parser.output.size();i++){
			System.out.println(parser.output.get(i));
		}
		for(int i=0;i<parser.error.size();i++){
			System.out.println(parser.error.get(i));
		}

		
		/*Item it = new Item();
		it.left = "A";
		it.before_point = new ArrayList<String>();
		it.after_point = new ArrayList<String>();
		it.before_point.add("B");
		it.after_point.add("A");
		it.search_character = new ArrayList<String>();
		it.search_character.add("#");
		Set<Item> it_set = new HashSet<Item>();
		it_set.add(it);
		System.out.println(parser.getClosure(it));
		
		/*List<String> l1 = new ArrayList<String>();
		l1.add("L");
		l1.add("=");
		l1.add("R");
		List<String> l2 = new ArrayList<String>();
		l2.add("#");
		System.out.println(parser.getStringFirst(l1, l2));
		System.out.println(parser.getFirst("="));*/
		
		/*Item it1 = new Item();
		it1.left = "S";
		it1.before_point = new ArrayList<String>();
		it1.before_point.add("");
		it1.after_point = new ArrayList<String>();
		it1.after_point.add("CC");
		it1.search_character = new ArrayList<String>();
		it1.search_character.add("#");
		Item it2 = new Item();
		it2.left = "S";
		it2.before_point = new ArrayList<String>();
		it2.before_point.add("");
		it2.after_point = new ArrayList<String>();
		it2.after_point.add("CC");
		it2.search_character = new ArrayList<String>();
		it2.search_character.add("#");
		System.out.println(it1.equals(it2));
		Set<Item> test = new HashSet<Item>();
		test.add(it1);
		test.add(it2);
		System.out.println(test);*/
		
	}
}
