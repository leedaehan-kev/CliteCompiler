package hw7;


public class Semantics {

	
	State M (Program prg) {                              // Program = Declarations declr ; Block block 추상 구분의 의미구조 정의
		return M(prg.body, initialState(prg.decpart));   // Program의 의미구조는 초기상태에 따른, Block (본체)의 의미구조와 같다
	}                                                    // 혹은 변환함수 M 의 관점에서 보자면, 초기상태를 Block 문장들의 상태 변환을 거쳐 최종 상태로 변환해주는 것이 Program의 의미구조라고도 할 수 있음
	
	
	State initialState (Declarations dclrs) {           // Program의 최초의 상태를 반환한다
		State state = new State();
		for(Declaration dclr : dclrs)                   // Program의 선언부를 가지고 State 초기화  
			state.put(dclr.v, Value.mkValue(dclr.t));   // 기억하라!!!, 선언부로 초기화된 상태의 값들은 undefined!!	
		                                                // 가령, int c; Float d; 인 경우, 초기 상태 = <c : IntValue(undef), d : FloatValue(undef)>
		return state;                                   // 선언부를 이용하여 초기화된 최초의 State 반환
	}
	
	State M (Statement stmt, State state) {             // 각 실행문들(Skip, Assignment, Conditional, Loop, Expression)에 대한 의미 변환 시작
		
		if(stmt instanceof Skip) return M((Skip)stmt,state);                 // Skip 문으로 인한 상태 변환
		if(stmt instanceof Assignment) return M((Assignment)stmt, state);    // Assignment 문으로 인한 상태 변환
		if(stmt instanceof Conditional) return M((Conditional) stmt, state); // If-Condition 문으로 인한 상태 변환
		if(stmt instanceof PrintStatement) return M((PrintStatement)stmt, state);  // Print 문으로 인한 상태 변환
		if(stmt instanceof Loop) return M((Loop) stmt, state);               // While Loop 문에 대한 상태 변환 
		if(stmt instanceof Block) return M((Block) stmt, state);                // Block 문에 대한 상태 변환
		
		
		throw new IllegalArgumentException("should never reach here!!");
	}
	
	State M (PrintStatement print, State state) {
		Value v = M(print.output,state);
		print.run(v);                                    // print Statement 실행 코드
		
		return state;
	}
	
	State M (Block block, State state) {          // Block 문에 대한 상태 변환
		for(Statement stmt : block.members)  
			state = M(stmt,state);                // Block 내부의 각각의 실행문들에 대한 상태 변환
		return state;                             
	}
	 
	State M(Loop loop, State state) {             // While 반복문에 대한 상태 변환	
		if(M(loop.test,state).boolValue())             // While 문의 반복 조건에 대한 연산의 결과가 True 라면
			return M(loop,M(loop.body,state));         // While 반복문이므로, 다시, Loop의 반복조건으로 돌아가, 상태 변환
		
		return state;                                  // While 문의 반복 조건에 대한 연산의 결과가 False 라면, 다음 Statement로 이동
	}
	
	State M (Conditional con, State state) {            // If-Condition으로 인한 상태 변환	                                        
		if(M(con.test,state).boolValue())               // 먼저, test Expression에 대한 의미(수식 연산의 결과)를 계산
			return M(con.thenbranch,state);             // true 라면, then Statement 에 대한 상태 변환 (Conditional의 의미는, thenBranch의 의미가 된다)
		else                                            // false 라면, else Statement 에 대한 상태 변환 (Conditional의 의미는, elseBranch의 의미가 된다)
			return M(con.elsebranch,state);
	}
	
	State M (Skip skip, State state) {return state;}           // skip 문은 프로그램의 State 를 변화시키지 않음!!
		
	
	State M (Assignment asnmnt, State state) {                               // 대입문에 대한 상태 변환	(Assignment의 의미구조는, 먼저 State에 따라, Expression의 의미(Value)를 .계산하고, 이를 입력 State에 저장한다 )	
		 return state.onion(asnmnt.target, M(asnmnt.source,state));          // 먼저, Expression에 대한 의미(수식 연산의 결과값)을 구하고, 해당 변수에 이 값을 매핑 
	}
	
	
	Value M (Expression expr, State state) {             // Expression 그 자체는, 상태를 변화시키지 않음에 유의하라!!
		                                                 // 또한 Expression에 대한 의미는 그 수식이 나타내는 값을 연산하는것이다!!
		if(expr instanceof Value)                        // Literal Value 값 그 자체가 바로 Expression의 의미
			return (Value)expr;
		
		if(expr instanceof Variable)                     // Variable에 매핑된 Value 그 자체가 Expression의 의미
			return state.get((Variable)expr);
		
		if(expr instanceof Binary) {                     // 이항 연산식의 경우, 먼저, 각 항(term1, term2)에 대한 의미 결정이후, 해당 이항 연산적용 
			Binary bin = (Binary) expr;                  
			return applyBinary(bin.op,M(bin.term1,state),M(bin.term2,state));
		}
		
		if(expr instanceof Unary) {
			Unary un = (Unary) expr;
			return applyUnary(un.op,M(un.term,state));
		}
		
		throw new IllegalArgumentException("should never reach here");
	}
	
	
	Value applyBinary(Operator op, Value val1, Value val2) {   // 이항 연산의 의미 결정
		
		
		// 먼저, 두개의 term(값)에 대한 선언 여부를 확인
		// 선언되어 있지 않다면 (!val1.isUndef() && !val2.isUndef() 가 false 라면,), Error 발생 후 시스템 강제 종료
		TypeSystemChecker.check(!val1.isUndef() && !val2.isUndef(), "reference to undef value!");
		// 위의 오류 점검은, 정의 되지 않은 변수, 즉, 아직 초기화 되지 않은 변수들을 사용하는 오류를 잡아낸다!!
		
		if(op.val.equals(Operator.INT_PLUS))                          // 이항 연산자의 주어진 연산자가 int+ 라면 
			return new IntValue(val1.intValue() + val2.intValue());   // 실제 int 값들끼리의 덧셈 결과값 반환
		
		if(op.val.equals(Operator.INT_MINUS))                         // 이항 연산자의 주어진 연산자가 int- 라면
			return new IntValue(val1.intValue() - val2.intValue());   // 실제 int 값들끼리의 뺼셈 결과값 반환
		
		if(op.val.equals(Operator.INT_TIMES))                         // 이항 연산자의 주어진 연산자가 int* 라면
			return new IntValue(val1.intValue() * val2.intValue());   // 실제 int 값들끼리의 곱셈 결과 반환
		
		if(op.val.equals(Operator.INT_DIV))                           // 이항 연산자의 주어진 연산자가 int/ 라면
			return new IntValue(val1.intValue() / val2.intValue());   // 실제 int 값들끼리의 나눗셈 결과 반환
		
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
		

		// 먼저, term(값)에 대한 선언 여부를 확인
		TypeSystemChecker.check(!val.isUndef(), "reference to undef value!");
		// 선언되어 있지 않다면 (!val.isUndef()가 false 라면,), Error 발생 후 시스템 강제 종료
		
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
		
		TypeMap map = TypeSystemChecker.typing(prog.decpart);        // Declarations -> TypeMap 변환 
		
		System.out.println(map);                            // 변환된 TypeMap 출력
		TypeSystemChecker.V(prog);                          // 주어진 AST에 대하여, Static Type Check!!
		Program xform_AST = TypeConversion.T(prog,map);                    // AST 형변환!!!
		
		System.out.println("\n------------" + "Type Conversioned Abstract Syntax Tree of" + "<"+ args[0] + ">" + "-------------\n");
		xform_AST.display();                                // 형변환된 AST 출력!!!
		
		Semantics semantics = new Semantics();
		State state = semantics.M(xform_AST);
		
		System.out.println("\n------------------------Final State is-----------------------------");
		System.out.println(state);
	}

}
