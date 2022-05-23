package hw7;


import java.util.HashMap;

public class State extends HashMap<Variable,Value> {
	/*
	 State = <변수 : 값> 쌍들의 집합	 
	 */
	
	public State() {}                                       // 빈 State(집합) 생성 
	
	public State(Variable key, Value val) {put(key,val);}
	
	public State onion(Variable key, Value val) {           // 주어진 key 에 대한 val 세팅  
		put(key,val);
		return this;
	}
	
	public State onion(State t) {                            // State의 전체(모든 변수:값 쌍)를 수정
		for(Variable key : t.keySet())
			put(key,t.get(key)); 
		return this;
	}
}
