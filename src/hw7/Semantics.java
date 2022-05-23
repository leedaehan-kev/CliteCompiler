package hw7;


public class Semantics {

	
	State M (Program prg) {                              // Program = Declarations declr ; Block block �߻� ������ �ṉ̀��� ����
		return M(prg.body, initialState(prg.decpart));   // Program�� �ṉ̀����� �ʱ���¿� ����, Block (��ü)�� �ṉ̀����� ����
	}                                                    // Ȥ�� ��ȯ�Լ� M �� �������� ���ڸ�, �ʱ���¸� Block ������� ���� ��ȯ�� ���� ���� ���·� ��ȯ���ִ� ���� Program�� �ṉ̀������ �� �� ����
	
	
	State initialState (Declarations dclrs) {           // Program�� ������ ���¸� ��ȯ�Ѵ�
		State state = new State();
		for(Declaration dclr : dclrs)                   // Program�� ����θ� ������ State �ʱ�ȭ  
			state.put(dclr.v, Value.mkValue(dclr.t));   // ����϶�!!!, ����η� �ʱ�ȭ�� ������ ������ undefined!!	
		                                                // ����, int c; Float d; �� ���, �ʱ� ���� = <c : IntValue(undef), d : FloatValue(undef)>
		return state;                                   // ����θ� �̿��Ͽ� �ʱ�ȭ�� ������ State ��ȯ
	}
	
	State M (Statement stmt, State state) {             // �� ���๮��(Skip, Assignment, Conditional, Loop, Expression)�� ���� �ǹ� ��ȯ ����
		
		if(stmt instanceof Skip) return M((Skip)stmt,state);                 // Skip ������ ���� ���� ��ȯ
		if(stmt instanceof Assignment) return M((Assignment)stmt, state);    // Assignment ������ ���� ���� ��ȯ
		if(stmt instanceof Conditional) return M((Conditional) stmt, state); // If-Condition ������ ���� ���� ��ȯ
		if(stmt instanceof PrintStatement) return M((PrintStatement)stmt, state);  // Print ������ ���� ���� ��ȯ
		if(stmt instanceof Loop) return M((Loop) stmt, state);               // While Loop ���� ���� ���� ��ȯ 
		if(stmt instanceof Block) return M((Block) stmt, state);                // Block ���� ���� ���� ��ȯ
		
		
		throw new IllegalArgumentException("should never reach here!!");
	}
	
	State M (PrintStatement print, State state) {
		Value v = M(print.output,state);
		print.run(v);                                    // print Statement ���� �ڵ�
		
		return state;
	}
	
	State M (Block block, State state) {          // Block ���� ���� ���� ��ȯ
		for(Statement stmt : block.members)  
			state = M(stmt,state);                // Block ������ ������ ���๮�鿡 ���� ���� ��ȯ
		return state;                             
	}
	 
	State M(Loop loop, State state) {             // While �ݺ����� ���� ���� ��ȯ	
		if(M(loop.test,state).boolValue())             // While ���� �ݺ� ���ǿ� ���� ������ ����� True ���
			return M(loop,M(loop.body,state));         // While �ݺ����̹Ƿ�, �ٽ�, Loop�� �ݺ��������� ���ư�, ���� ��ȯ
		
		return state;                                  // While ���� �ݺ� ���ǿ� ���� ������ ����� False ���, ���� Statement�� �̵�
	}
	
	State M (Conditional con, State state) {            // If-Condition���� ���� ���� ��ȯ	                                        
		if(M(con.test,state).boolValue())               // ����, test Expression�� ���� �ǹ�(���� ������ ���)�� ���
			return M(con.thenbranch,state);             // true ���, then Statement �� ���� ���� ��ȯ (Conditional�� �ǹ̴�, thenBranch�� �ǹ̰� �ȴ�)
		else                                            // false ���, else Statement �� ���� ���� ��ȯ (Conditional�� �ǹ̴�, elseBranch�� �ǹ̰� �ȴ�)
			return M(con.elsebranch,state);
	}
	
	State M (Skip skip, State state) {return state;}           // skip ���� ���α׷��� State �� ��ȭ��Ű�� ����!!
		
	
	State M (Assignment asnmnt, State state) {                               // ���Թ��� ���� ���� ��ȯ	(Assignment�� �ṉ̀�����, ���� State�� ����, Expression�� �ǹ�(Value)�� .����ϰ�, �̸� �Է� State�� �����Ѵ� )	
		 return state.onion(asnmnt.target, M(asnmnt.source,state));          // ����, Expression�� ���� �ǹ�(���� ������ �����)�� ���ϰ�, �ش� ������ �� ���� ���� 
	}
	
	
	Value M (Expression expr, State state) {             // Expression �� ��ü��, ���¸� ��ȭ��Ű�� ������ �����϶�!!
		                                                 // ���� Expression�� ���� �ǹ̴� �� ������ ��Ÿ���� ���� �����ϴ°��̴�!!
		if(expr instanceof Value)                        // Literal Value �� �� ��ü�� �ٷ� Expression�� �ǹ�
			return (Value)expr;
		
		if(expr instanceof Variable)                     // Variable�� ���ε� Value �� ��ü�� Expression�� �ǹ�
			return state.get((Variable)expr);
		
		if(expr instanceof Binary) {                     // ���� ������� ���, ����, �� ��(term1, term2)�� ���� �ǹ� ��������, �ش� ���� �������� 
			Binary bin = (Binary) expr;                  
			return applyBinary(bin.op,M(bin.term1,state),M(bin.term2,state));
		}
		
		if(expr instanceof Unary) {
			Unary un = (Unary) expr;
			return applyUnary(un.op,M(un.term,state));
		}
		
		throw new IllegalArgumentException("should never reach here");
	}
	
	
	Value applyBinary(Operator op, Value val1, Value val2) {   // ���� ������ �ǹ� ����
		
		
		// ����, �ΰ��� term(��)�� ���� ���� ���θ� Ȯ��
		// ����Ǿ� ���� �ʴٸ� (!val1.isUndef() && !val2.isUndef() �� false ���,), Error �߻� �� �ý��� ���� ����
		TypeSystemChecker.check(!val1.isUndef() && !val2.isUndef(), "reference to undef value!");
		// ���� ���� ������, ���� ���� ���� ����, ��, ���� �ʱ�ȭ ���� ���� �������� ����ϴ� ������ ��Ƴ���!!
		
		if(op.val.equals(Operator.INT_PLUS))                          // ���� �������� �־��� �����ڰ� int+ ��� 
			return new IntValue(val1.intValue() + val2.intValue());   // ���� int ���鳢���� ���� ����� ��ȯ
		
		if(op.val.equals(Operator.INT_MINUS))                         // ���� �������� �־��� �����ڰ� int- ���
			return new IntValue(val1.intValue() - val2.intValue());   // ���� int ���鳢���� �E�� ����� ��ȯ
		
		if(op.val.equals(Operator.INT_TIMES))                         // ���� �������� �־��� �����ڰ� int* ���
			return new IntValue(val1.intValue() * val2.intValue());   // ���� int ���鳢���� ���� ��� ��ȯ
		
		if(op.val.equals(Operator.INT_DIV))                           // ���� �������� �־��� �����ڰ� int/ ���
			return new IntValue(val1.intValue() / val2.intValue());   // ���� int ���鳢���� ������ ��� ��ȯ
		
		if(op.val.equals(Operator.INT_EQ))
			return new BoolValue(val1.intValue() == val2.intValue()); // dlgkd
		
		if(op.val.equals(Operator.INT_NE))
			return new BoolValue(val1.intValue() != val2.intValue());
		
		if(op.val.equals(Operator.INT_LT))
			return new BoolValue(val1.intValue() < val2.intValue());
		
		if(op.val.equals(Operator.INT_LE))
			return new BoolValue(val1.intValue() <= val2.intValue());
		
		if(op.val.equals(Operator.INT_GT))
			return new BoolValue(val1.intValue() > val2.intValue());
		
		if(op.val.equals(Operator.INT_GE))
			return new BoolValue(val1.intValue() >= val2.intValue());
		
		
		if(op.val.equals(Operator.FLOAT_PLUS))
			return new FloatValue(val1.floatValue() + val2.floatValue());
		
		if(op.val.equals(Operator.FLOAT_MINUS))
			return new FloatValue(val1.floatValue() - val2.floatValue());
		
		if(op.val.equals(Operator.FLOAT_TIMES))
			return new FloatValue(val1.floatValue() * val2.floatValue());
		
		if(op.val.equals(Operator.FLOAT_DIV))
			return new FloatValue(val1.floatValue() / val2.floatValue());
		
		if(op.val.equals(Operator.FLOAT_EQ))
			return new BoolValue(val1.floatValue() == val2.floatValue());
		
		if(op.val.equals(Operator.FLOAT_NE))
			return new BoolValue(val1.floatValue() != val2.floatValue());
		
		if(op.val.equals(Operator.FLOAT_LT))
			return new BoolValue(val1.floatValue() < val2.floatValue());
		
		if(op.val.equals(Operator.FLOAT_LE))
			return new BoolValue(val1.floatValue() <= val2.floatValue());
		
		if(op.val.equals(Operator.FLOAT_GT))
			return new BoolValue(val1.floatValue() > val2.floatValue());
		
		if(op.val.equals(Operator.FLOAT_GE))
			return new BoolValue(val1.floatValue() >= val2.floatValue());
		
		
		if(op.val.equals(Operator.CHAR_EQ))
			return new BoolValue(val1.charValue() == val2.charValue());
		
		if(op.val.equals(Operator.CHAR_NE))
			return new BoolValue(val1.charValue() != val2.charValue());
		
		if(op.val.equals(Operator.CHAR_LT))
			return new BoolValue(val1.charValue() < val2.charValue());
		
		if(op.val.equals(Operator.CHAR_LE))
			return new BoolValue(val1.charValue() <= val2.charValue());
		
		if(op.val.equals(Operator.CHAR_GT))
			return new BoolValue(val1.charValue() > val2.charValue());
		
		if(op.val.equals(Operator.CHAR_GE))
			return new BoolValue(val1.charValue() >=  val2.charValue());
		
		
		if(op.val.equals(Operator.AND)) 
			return new BoolValue(val1.boolValue() && val2.boolValue());
		
		if(op.val.equals(Operator.OR))
			return new BoolValue(val1.boolValue() || val2.boolValue());
		
		if(op.val.equals(Operator.BOOL_EQ))
			return new BoolValue(val1.boolValue() == val2.boolValue());
		
		if(op.val.equals(Operator.BOOL_NE))
			return new BoolValue(val1.boolValue() != val2.boolValue());
		
		
			
		throw new IllegalArgumentException("should never reach here!!");
	}
	
	Value applyUnary(Operator op, Value val) {
		

		// ����, term(��)�� ���� ���� ���θ� Ȯ��
		TypeSystemChecker.check(!val.isUndef(), "reference to undef value!");
		// ����Ǿ� ���� �ʴٸ� (!val.isUndef()�� false ���,), Error �߻� �� �ý��� ���� ����
		
		if(op.val.equals(Operator.NOT))
			return new BoolValue(!val.boolValue());
		
		else if(op.val.equals(Operator.INT_NEG))
			return new IntValue(-val.intValue());
		
		else if(op.val.equals(Operator.FLOAT_NEG))
			return new FloatValue(-val.floatValue());
		
		else if(op.val.equals(Operator.F2I))
			return new IntValue((int)val.floatValue());
		
		else if(op.val.equals(Operator.I2F))
			return new FloatValue((float)val.intValue());
		
		else if(op.val.equals(Operator.C2I))
			return new IntValue((int)val.charValue());
		
		else if(op.val.equals(Operator.I2C))
			return new CharValue((char)val.intValue());
		
		throw new IllegalArgumentException("should never reach here");
	}
	
	
	
	public static void main(String[] args) {		
		Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
       
        System.out.println("\n------------" + "Abstract Syntax Tree of" + "<"+ args[0] + ">" + "-------------\n");
        prog.display();
		
		TypeMap map = TypeSystemChecker.typing(prog.decpart);        // Declarations -> TypeMap ��ȯ 
		
		System.out.println(map);                            // ��ȯ�� TypeMap ���
		TypeSystemChecker.V(prog);                          // �־��� AST�� ���Ͽ�, Static Type Check!!
		Program xform_AST = TypeConversion.T(prog,map);                    // AST ����ȯ!!!
		
		System.out.println("\n------------" + "Type Conversioned Abstract Syntax Tree of" + "<"+ args[0] + ">" + "-------------\n");
		xform_AST.display();                                // ����ȯ�� AST ���!!!
		
		Semantics semantics = new Semantics();
		State state = semantics.M(xform_AST);
		
		System.out.println("\n------------------------Final State is-----------------------------");
		System.out.println(state);
	}

}
