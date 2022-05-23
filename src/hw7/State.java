package hw7;


import java.util.HashMap;

public class State extends HashMap<Variable,Value> {
	/*
	 State = <���� : ��> �ֵ��� ����	 
	 */
	
	public State() {}                                       // �� State(����) ���� 
	
	public State(Variable key, Value val) {put(key,val);}
	
	public State onion(Variable key, Value val) {           // �־��� key �� ���� val ����  
		put(key,val);
		return this;
	}
	
	public State onion(State t) {                            // State�� ��ü(��� ����:�� ��)�� ����
		for(Variable key : t.keySet())
			put(key,t.get(key)); 
		return this;
	}
}
