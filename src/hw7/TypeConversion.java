package hw7;

public class TypeConversion {

	public static Program T (Program p, TypeMap tm) {
		Block body = (Block)T(p.body,tm);      // Assignment 들에 대한 Type 변환 시작
		return new Program (p.decpart, body);  // Type 변환된 새로운 AST 구성
	}
	
	public static Statement T (Statement s, TypeMap tm) { 
		
		if(s instanceof Block) {                                   // 전체 Statement 들에 대한, AST 변환
			Block block = (Block) s;
			Block xform = new Block();
			
			for(Statement stmt : block.members)      
				xform.members.add(T(stmt,tm));                     // 각각의 Statement 들의 AST 변환환
			return xform;
		}
		
		
		else if (s instanceof Assignment) {
			Assignment a = (Assignment)s;
		    Variable target = a.target;
			
			Expression src = T(a.source,tm);                       // Expression에 대한 AST 변환
			
			Type ttype = (Type)tm.get(a.target);                   // target 변수의 Type
			Type srctype = TypeSystemChecker.typeOf(a.source, tm); // Source Expression의 Type
			
			if(ttype ==Type.FLOAT) {                
				if(srctype==Type.INT) {                                // Float = Int 
					src = new Unary (new Operator(Operator.I2F),src);  // Float = Float(Int)
					srctype = Type.FLOAT;
				}
			}
			
			else if (ttype == Type.INT) {                           
				if(srctype == Type.CHAR) {                             // Int = Char
					src = new Unary (new Operator(Operator.C2I),src);  // Int = Int(Char)
					srctype = Type.INT;
				}
			}
			
			TypeSystemChecker.check(ttype == srctype,"type error in assignment to " + target);   // 자동 형변환 이후, target 변수와, Source Expression의 타입이 같은지 확인
			return new Assignment(target,src);                         // 자동 형변환된 Assignment AST 반환        
		}
		
		else if (s instanceof Conditional) {                 // if 조건문에 대한 AST 변환
			
			Conditional c = (Conditional) s;
			
			Expression expr = T(c.test,tm);                  // 조건식(Expression)에 대한 AST 변환 
			Statement then_s = T(c.thenbranch,tm);           // then 실행문에 대한 AST 변환
			Statement else_s = T(c.elsebranch,tm);           // else 실행문에 대한 AST 변환
			
			return new Conditional (expr,then_s,else_s);     // if 조건문에 대한 변환된 AST 반환
		}
		
		else if (s instanceof Loop) {                        // While 문에 대한 AST 변환
			
			Loop loop = (Loop)s;
			Expression expr = T(loop.test, tm);             // 조건식 (Expression)에 대한, AST 변환
			Statement stmt = T(loop.body,tm);               // While 실행문에 대한 AST 변환
			return new Loop(expr,stmt);                     // While 문에 대한 변환된 AST 반환
		}
		
		else if (s instanceof PrintStatement) {             // print Statement 는 형변환을 필요로 하지않지만, 내부 연산자의 타입을 명시할 필요는 있음!!
			
			PrintStatement print = (PrintStatement)s;
			
			Type outputType = TypeSystemChecker.typeOf(print.output, tm);
			Expression expr = T(print.output, tm);
			
			if(outputType.equals(Type.CHAR)) 
				return new PrintCharStatement(expr);
				
			else if (outputType.equals(Type.BOOL)) 
				return new PrintBoolStatement(expr);
			
            else if (outputType.equals(Type.INT)) 
            	return new PrintIntStatement(expr);
			
            else if (outputType.equals(Type.FLOAT)) 
            	return new PrintFloatStatement(expr);

		}
		
		 throw new IllegalArgumentException("should never reach here");
	}
	
	public static Expression T (Expression e, TypeMap tm) {     // Expression 에 대한 AST 변환
		                                                        // 기억하라!!, 자동 타입 변환은 연산자의 타입을 지정하고, 이를 바탕으로, 묵시적 형변환이 필요한 경우에, 수동 타입 변환 연산자를 삽입하는 것이다!!
		if (e instanceof Value)                                  
			return e;
		
		else if (e instanceof Variable)                      
			return e;
		
		else if (e instanceof Binary){                          // 이항식인 경우 
			Binary b = (Binary) e;
			
			Type typ1 = TypeSystemChecker.typeOf(b.term1, tm);    // 피연산자 1의 타입 확인
			
			Expression t1 = T(b.term1,tm);                        // 피연산자 1에 대한 AST 변환 
			Expression t2 = T(b.term2,tm);                        // 피연산자 2에 대한 AST 변환
			
			if(typ1 == Type.INT)                                  // 피연산자 1의 타입이 int 형이라면
				return new Binary(b.op.intMap(b.op.val),t1,t2);   // 연산자는 int 형 연산자로 변환
			
			else if (typ1 == Type.FLOAT)                          // 피연산자 1의 타입이 float 형이라면
				return new Binary(b.op.floatMap(b.op.val),t1,t2); // 연산자는 float 형 연산자로 변환
			
			else if (typ1 == Type.CHAR)                           // 피연산자 1의 타입이 char 형이라면
				return new Binary(b.op.charMap(b.op.val),t1,t2);  // 연산자는 char 형 연산자로 변환
			
			else if (typ1 == Type.BOOL)                           // 피연산자 1의 타입이 bool 형이라면
				return new Binary (b.op.boolMap(b.op.val),t1,t2); // 연산자는 bool 형 연산자로 변환
			
				
			throw new IllegalArgumentException("should never reach here"); 
			
		}
			
			
		else if (e instanceof Unary) {                            // 딘항 연산의 경우
			
			Unary u = (Unary)e;              
			
			Type typ = TypeSystemChecker.typeOf(u.term,tm);       // 피연산자의 타입 확인
			
			Expression term1 = T(u.term,tm);                      // 피연산자에 대한 AST 변환
			
			if((typ==Type.BOOL) && (u.op.NotOp()))                // !(bool) 인 경우, 
				return new Unary(u.op.boolMap(u.op.val),term1);   // Not 연산자의 형 변환은 x (Because, Not 연산자는, Bool에서만 쓰이는 연산자!!)
			
			else if ((typ==Type.FLOAT) && (u.op.NegateOp()))     // -(float) 인 경우,
				return new Unary(new Operator(Operator.FLOAT_NEG),term1);  // - 연산자는 float 형 연산자로 변환 (FLOAT_NEG)                              
			
			else if ((typ==Type.INT)&& (u.op.NegateOp()))        // -(int) 인 경우
				return new Unary (new Operator(Operator.INT_NEG),term1);  // - 연산자는 int 형 연산자로 변환 (INT_NEG)
			
			else if ((typ==Type.INT) && (u.op.charOp() || u.op.floatOp()))    // float(int), char(int)         	
					return new Unary(u.op.intMap(u.op.val),term1);      // I2C or I2F
					
			else if (typ==Type.FLOAT && u.op.intOp())              // int(float) 인 경우
				return new Unary(u.op.floatMap(u.op.val),term1);   // INT -> F2I
			
			else if (typ==Type.CHAR && u.op.intOp())               // int(char)
				return new Unary(u.op.charMap(u.op.val),term1);    // INT ->C2I
		
		}
		
		throw new IllegalArgumentException("should never Reach Here!!");
	}
	
	
	
	public static void main(String[] args) {
		Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
       
        System.out.println("\n------------" + "Abstract Syntax Tree of" + "<"+ args[0] + ">" + "-------------\n");
        prog.display();
		
		TypeMap map = TypeSystemChecker.typing(prog.decpart);        // Declarations -> TypeMap 변환 
		
		System.out.println(map);                            // 변환된 TypeMap 출력
		TypeSystemChecker.V(prog);                          // 주어진 AST에 대하여, Static Type Check!!
		Program xform_AST = T(prog,map);                    // AST 형변환!!!
		xform_AST.display();                                // 형변환된 AST 출력!!!

	}
}
