package hw7;


public class TypeSystemChecker {

	public static void check (boolean err_con, String str) {
		if(!err_con) {                                        // check 메소드는, 선언부의 타입 유효 검사에, 실행문의 타입 유효 검사에 각각 사용됨                                    
			System.err.println(str);
			System.exit(1);
		}
		return;		
	}
	
	
	public static void V (Program prg) {                  // 추상 구문 트리에 재한 정적 TypeChecking 시작
		V(prg.decpart);                                   // 선언부(Declarations = Declaration*) 의 Type-Validity 를 검사
		V(prg.body, typing(prg.decpart));                 // 실행문들 (Block = Statement*) 의 Type-Validity 를 검사 
	}
	
	public static void V (Declarations declrs) {          // 선언부(Declarations = Declaration*) 의 Type-Validity 를 검사 
		
		for(int i=0; i<declrs.size()-1;i++)             
			for(int j=i+1; j<declrs.size();j++) {
				Declaration di = declrs.get(i);                                 // 각각의 변수들의 이름을 하나씩 비교하여
				Declaration dj = declrs.get(j);
				check(!(di.v.equals(dj.v)),"duplicate declaraion : " + dj.v);   // 각각의 변수들의 이름을 하나씩 비교
     			                                                                // 같다면(변수들의 이름이 중복되므로, Type Error 발생!!), check 메소드는 내부적으로 Static TypeChecking을 강제 종료 
			}              
	}
	

	public static TypeMap typing (Declarations declrs) {  // typing 메소드는, 프로그램의 선언된 변수들(변수명 : 타입)의 배열(Arraylist)을 TypeMap 으로 변환
		TypeMap map = new TypeMap();                      
		
		for(Declaration declr : declrs)                   // Decalarations 배열의 변수:타입 을 하나씩 꺼내어, TypeMap에 담음
			map.put(declr.v, declr.t);
		
		return map;                                       // 선언부의 TypeMap 반환 
	}
	
	
	
	 public static void V (Statement s, TypeMap tm) {
		 	
	        if ( s == null )    
	            throw new IllegalArgumentException( "AST error: null statement");
	        
	        else if (s instanceof Skip) return;           // Skip(;) 실행문은 항상 Type-Valid!!
	        
	        else if (s instanceof Assignment) {           // 대입문에 대한 Type-Validity 검사!!
	            Assignment a = (Assignment)s;
	            
	            check( tm.containsKey(a.target)                            //  해당 대입문의 변수가 선언되어있는지, TypeMap 을 뒤진다
	                   , " undefined target in assignment: " + a.target);  //선언 되어있지 않다면, Type Checking Error 발생후 강제 종료
	            
	            V(a.source, tm);                                           // Expression 의 Type Validity 를 검사
	       
	            Type ttype = (Type)tm.get(a.target);                       // 대입문의 변수의 타입을 결정
	            Type srctype = typeOf(a.source, tm);                       // 대입문의 Expression의 타입을 결정
	            
	            if (ttype != srctype) {                                    // 변수와 Expression의 Type이 다른경우..
	  
	                if (ttype == Type.FLOAT)                               // 변수의 Type이 float 인 경우, Expression 의 Type이 int 인지 확인  (Widdening 만 허용)
	                    check( srctype == Type.INT                         // 그렇지 않다면, Type-Error 발생시킴
	                           , "mixed mode assignment to " + a.target);  
	                
	                else if (ttype == Type.INT)                            // 변수의 Type이 Int 인 경우, Expression의 Type 이 Char 인지 확인   (Widdening 만 허용)
	                    check( srctype == Type.CHAR                        // 그렇지 않다면, Type-Error 발생시킴
	                           , "mixed mode assignment to " + a.target);
	                
	                else                                                  
	                    check( false                                       // 그외의 경우는 전부 Type _invalid!!
	                           , "mixed mode assignment to " + a.target);
	            }
	            return;
	        } 
	        
	        else if (s instanceof Conditional) {                          // if 조건문에 대한 Type-Validity 검사
	        	
	            Conditional c = (Conditional)s;                           
	            V(c.test, tm);                                            // 먼저, 조건식이 Type-Valid 한지 검사
	            Type testType = typeOf(c.test, tm);                       // 이후, 조건식(test)의 Type이 boolean 인지 검사
	            
	            check(testType == Type.BOOL, "test expression must be bool but "+ testType); // 조건문의 Type이 boolean이 아니라면, Type-Error 발생!!
	            
	                                                                  // 조건문의 Type이 boolean이 맞다면
	            V(c.thenbranch, tm);                                  // if 의 실행문들에 대한, Type-Validity 검사
	            V(c.elsebranch, tm);                                  // else 문들에 대한, Type-Validity 검사
	             return;
	          
	        }
	        
	        else if (s instanceof Loop) {                                 // While 반복문에 대한, Type-Validity 검사
	        	
	            Loop l = (Loop)s;                                         
	            V(l.test, tm);                                            // 먼저, 조건문의 Type-Validity 검사
	            Type testType = typeOf(l.test, tm);                       // 이후, 조건문의 Type이 boolean 인지 검사
	            
	            check (testType==Type.BOOL, "test expression must be bool but "+ testType); // 조건문의 Type이 boolean이 아니라면, Type-Error 발생
	                                              
	            V(l.body, tm);                                        // 맞다면, if 문 내부의 실행문들의 Type-Validity 검사

	        }
	        
	        else if (s instanceof PrintStatement) {                  // PrintStatement 의 Type Validity 를 검사
	        	PrintStatement print = (PrintStatement)s;           
	        	V(print.output, tm);                                 // 이후, Expression의 Type Validity를 검사   
	            
	        	Type outputType = typeOf(print.output, tm);          // 이후, Expression의 Type을 확인하고, 현재, 시스템 상에 정의된 타입인지 확인
	        	
	        	if(s instanceof PrintCharStatement)                // PrintChar에 대한 Type 검사
	        		check(outputType == Type.CHAR,"printCh must come with type char but "+outputType+" has come");
	        	
	        	else if(s instanceof PrintBoolStatement) 
	        		check(outputType == Type.BOOL,"printBl must come with type bool but "+outputType+" has come");
	        	
	        	else if (s instanceof PrintIntStatement) 
	        		check(outputType == Type.INT,"printInt must come with type int but "+outputType+" has come");
	        	
	        	else if(s instanceof PrintFloatStatement) 
	        		check(outputType == Type.FLOAT,"printFlt must come with type float but "+outputType+" has come");
	        	
	        	else {                                                          // 일반적 print statement로, Type Casting 필요 없음
	        	check(outputType == Type.BOOL || outputType == Type.CHAR 
	        			||outputType == Type.FLOAT || outputType == Type.INT, "Non Defined Type " + outputType+" has come");   
	        	}
	        }
	        
	        else if (s instanceof Block) {                                // {...} 의 Block 문  
	            Block b = (Block)s;
	            
	            for(Statement i : b.members)                            // {} 내부의 모든 실행문들에 대한 Type Validity 검사
	                V(i, tm);     
	        } 
	        
	        else 
	        	throw new IllegalArgumentException("should never reach here");
	        
	    }
	
	
	 public static void V (Expression e, TypeMap tm) {          // Expression (수식, 표현식) 의 Type Validity 를 검사 
		 
	        if (e instanceof Value)                             // Literal Value의 타입은 항상 유효!!
	            return;
	        
	        if (e instanceof Variable) {                        // Variable 의 타입이 유효 한지 검사
	            Variable v = (Variable)e;        
	            check( tm.containsKey(v)                        // 실행문의 Variable 의 타입이 유요하다?? = TypeMap에 해당 이름의 변수가 있는지 조사 
 	                   , "undeclared variable: " + v);          // 없다면, Type-Error 발생시킨후 강제 종료
	            return;
	        }
	        
	        if (e instanceof Binary) {                          // Binary(이항 표현식)의 타입이 유효한지 검사
	            Binary b = (Binary) e;                          // Binary = BinaryOP op ; Expression term1,term2
	            
	            Type typ1 = typeOf(b.term1, tm);                // term1 (피연산자1)의 Type 을 결정 
	            Type typ2 = typeOf(b.term2, tm);                // term2 (피연산자2)의 Type 을 결정
	            
	            V (b.term1, tm);                                // Expression term1의 Type Validity 검사
	            V (b.term2, tm);                                // Expression term2의 Type Validity 검사
	            
	            if (b.op.ArithmeticOp())                        // Binary 연산자가 사칙연산자인 경우
	            	
	                check( typ1 == typ2 &&                       // 반드시 두 피연산자의 Type이 같고, int 혹은 float 여야함
	                       (typ1 == Type.INT || typ1 == Type.FLOAT)  // 그렇지 않으면, Type Checking 에러!! 
	                       , "type error for " + b.op);
	            
	            else if (b.op.RelationalOp( ))                   // Binary 연산자가 관계 연산자인 경우
	                check( typ1 == typ2 , "type error for " + b.op);    // 두 피연산자의 Type이 같은지 확인
	            
	            else if (b.op.BooleanOp( ))                      // Binary 연산자가 논리 연산자인 경우
	                check( typ1 == Type.BOOL && typ2 == Type.BOOL, // 두 피연산자의 Type이 boolean 으로 같은지 확인
	                       b.op + ": non-bool operand");           // 그렇지 않으면, Type Checking 에러!!
	            else
	                throw new IllegalArgumentException("should never reach here BinaryOp error");
	            return;
	        }
	        
	        if (e instanceof Unary) {                            // Unary(단항 연산)의 Type이 유효한지 검사
	            Unary u = (Unary) e;
	            
	            Type typ = typeOf(u.term, tm);                  // 해당 단항 연산의 피연산자의 Type 을 결정  
	            V(u.term, tm);                                   // 피연산자의 Type-Validity 결정
	            
	            if (u.op.NotOp()) {                              // 단항 연산자가 ! 연산자인 경우
	                check((typ == Type.BOOL), "NotOp type error "); // 뒤의 Term의 Type은 반드시 boolean 이어야함
	            } 
	            
	            else if (u.op.NegateOp()) {                      // 단항 연산자가 - 연산자인 경우
	                check((typ == (Type.INT) || typ == (Type.FLOAT)), "NegateOp type error");  // 피연산자의 Type 은 반드시 Float, Int 여야함
	            }
	            
	            else if (u.op.floatOp() || u.op.charOp()) {                                                  // 단항 연산자가 명시적 형변환 (float(), char()) 인 경우                                                 
	            	check(typ == (Type.INT)
	            			, "type error for float()/char() : int term must come but " + typ);             // 피연산자의 Type 은 반드시 int 여야 함
	            }
	            else if(u.op.intOp()) {                                                                      // 단항 연산자가 명시적 형변환 (int()) 인 경우
	            	check(typ == (Type.FLOAT) || typ== (Type.CHAR)
	            			, "type error for int() : float/char term must come but " + typ);               // 피연산자의 Type 은 반드시 float 혹은, char 이어야 함
	            }
	            
	            else {
	                throw new IllegalArgumentException("should never reach here UnaryOp error");
	            }
	            return;
	        }

	        throw new IllegalArgumentException("should never reach here");
	    }
	 
	 
	
	 public static Type typeOf (Expression e, TypeMap tm) {                // Expression (표현식)의 타입을 결정
			
	        if (e instanceof Value)          // Literal Value의 타입은, 해당 Literal Value의 타입과 같음                                
	        	return ((Value)e).type; 
	        
	        if (e instanceof Variable) {     // Variable (변수)의 타입은, TypeMap 에서, 해당 변수와 매핑된 타입과 같음 
	            Variable v = (Variable)e; 
	            check (tm.containsKey(v), "undefined variable: " + v);
	            return (Type) tm.get(v);
	        }
	        
	        if (e instanceof Binary) {      // Binary (이항 수식)의 타입은, 두 피연산자 항의 Type에 따라 결정 
	            Binary b = (Binary)e;
	            
	            if (b.op.ArithmeticOp())                   // 사칙 연산자 (BinaryOP 가 + - * /)인 경우
	                if (typeOf(b.term1,tm)== Type.FLOAT)   // term1 의 Type 이 float 인지, int 인지 확인   
	                    return (Type.FLOAT);               // float 인 경우, 해당 Expression의 Type은 float 
	                else return (Type.INT);                // 그렇지 않은 경우, 해당 Expression의 Type은 int
	            
	            else if (b.op.RelationalOp() || b.op.BooleanOp())  // 관계 연산자 (BinaryOP가 &&, ||, <, >, <=, >=, !=, ==)의 논리,관계 연산자인 경우
	                return (Type.BOOL);                         // Expression의 Type은 boolean
	        }
	        
	        if (e instanceof Unary) {                           // 단항 연산자의 경우
	            Unary u = (Unary)e;
	            
	            if (u.op.NotOp( ))        return (Type.BOOL);           // 해당 단항 연산자가 ! 인 경우, 해당 Expression 은 boolean   
	            else if (u.op.NegateOp( )) return typeOf(u.term,tm);    // 해당 단항 연산자가 - 인 경우, 해당 Expression 은 피연산자항의 Type에 따라 결정 
	            else if (u.op.intOp( ))    return (Type.INT);           // 해당 단항 연산자가 int() 명시적 캐스팅 연산자인 경우, 해당 Expression은 int
	            else if (u.op.floatOp( )) return (Type.FLOAT);          // 해당 단항 연산자가 float() 명시적 캐스팅 연산자인 경우, 해당 Expression은 float
	            else if (u.op.charOp( ))  return (Type.CHAR);           // 해당 단항 연산자가 char() 명시적 캐스팅 연산자인 경우, 해당 Expression은 char
	        }
	        
	        throw new IllegalArgumentException("should never reach here");
	    } 
	

	public static void main(String[] args) {
		
		Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
       
        System.out.println("\n------------" + "Abstract Syntax Tree of" + "<"+ args[0] + ">" + "-------------\n");
        prog.display();
		
		TypeMap map = typing(prog.decpart);        // Declarations -> TypeMap 변환 
		
		System.out.println(map);                            // 변환된 TypeMap 출력
		V(prog);
	}
}
