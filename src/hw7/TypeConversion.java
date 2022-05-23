package hw7;

public class TypeConversion {

	public static Program T (Program p, TypeMap tm) {
		Block body = (Block)T(p.body,tm);      // Assignment �鿡 ���� Type ��ȯ ����
		return new Program (p.decpart, body);  // Type ��ȯ�� ���ο� AST ����
	}
	
	public static Statement T (Statement s, TypeMap tm) { 
		
		if(s instanceof Block) {                                   // ��ü Statement �鿡 ����, AST ��ȯ
			Block block = (Block) s;
			Block xform = new Block();
			
			for(Statement stmt : block.members)      
				xform.members.add(T(stmt,tm));                     // ������ Statement ���� AST ��ȯȯ
			return xform;
		}
		
		
		else if (s instanceof Assignment) {
			Assignment a = (Assignment)s;
		    Variable target = a.target;
			
			Expression src = T(a.source,tm);                       // Expression�� ���� AST ��ȯ
			
			Type ttype = (Type)tm.get(a.target);                   // target ������ Type
			Type srctype = TypeSystemChecker.typeOf(a.source, tm); // Source Expression�� Type
			
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
			
			TypeSystemChecker.check(ttype == srctype,"type error in assignment to " + target);   // �ڵ� ����ȯ ����, target ������, Source Expression�� Ÿ���� ������ Ȯ��
			return new Assignment(target,src);                         // �ڵ� ����ȯ�� Assignment AST ��ȯ        
		}
		
		else if (s instanceof Conditional) {                 // if ���ǹ��� ���� AST ��ȯ
			
			Conditional c = (Conditional) s;
			
			Expression expr = T(c.test,tm);                  // ���ǽ�(Expression)�� ���� AST ��ȯ 
			Statement then_s = T(c.thenbranch,tm);           // then ���๮�� ���� AST ��ȯ
			Statement else_s = T(c.elsebranch,tm);           // else ���๮�� ���� AST ��ȯ
			
			return new Conditional (expr,then_s,else_s);     // if ���ǹ��� ���� ��ȯ�� AST ��ȯ
		}
		
		else if (s instanceof Loop) {                        // While ���� ���� AST ��ȯ
			
			Loop loop = (Loop)s;
			Expression expr = T(loop.test, tm);             // ���ǽ� (Expression)�� ����, AST ��ȯ
			Statement stmt = T(loop.body,tm);               // While ���๮�� ���� AST ��ȯ
			return new Loop(expr,stmt);                     // While ���� ���� ��ȯ�� AST ��ȯ
		}
		
		else if (s instanceof PrintStatement) {             // print Statement �� ����ȯ�� �ʿ�� ����������, ���� �������� Ÿ���� ����� �ʿ�� ����!!
			
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
	
	public static Expression T (Expression e, TypeMap tm) {     // Expression �� ���� AST ��ȯ
		                                                        // ����϶�!!, �ڵ� Ÿ�� ��ȯ�� �������� Ÿ���� �����ϰ�, �̸� ��������, ������ ����ȯ�� �ʿ��� ��쿡, ���� Ÿ�� ��ȯ �����ڸ� �����ϴ� ���̴�!!
		if (e instanceof Value)                                  
			return e;
		
		else if (e instanceof Variable)                      
			return e;
		
		else if (e instanceof Binary){                          // ���׽��� ��� 
			Binary b = (Binary) e;
			
			Type typ1 = TypeSystemChecker.typeOf(b.term1, tm);    // �ǿ����� 1�� Ÿ�� Ȯ��
			
			Expression t1 = T(b.term1,tm);                        // �ǿ����� 1�� ���� AST ��ȯ 
			Expression t2 = T(b.term2,tm);                        // �ǿ����� 2�� ���� AST ��ȯ
			
			if(typ1 == Type.INT)                                  // �ǿ����� 1�� Ÿ���� int ���̶��
				return new Binary(b.op.intMap(b.op.val),t1,t2);   // �����ڴ� int �� �����ڷ� ��ȯ
			
			else if (typ1 == Type.FLOAT)                          // �ǿ����� 1�� Ÿ���� float ���̶��
				return new Binary(b.op.floatMap(b.op.val),t1,t2); // �����ڴ� float �� �����ڷ� ��ȯ
			
			else if (typ1 == Type.CHAR)                           // �ǿ����� 1�� Ÿ���� char ���̶��
				return new Binary(b.op.charMap(b.op.val),t1,t2);  // �����ڴ� char �� �����ڷ� ��ȯ
			
			else if (typ1 == Type.BOOL)                           // �ǿ����� 1�� Ÿ���� bool ���̶��
				return new Binary (b.op.boolMap(b.op.val),t1,t2); // �����ڴ� bool �� �����ڷ� ��ȯ
			
				
			throw new IllegalArgumentException("should never reach here"); 
			
		}
			
			
		else if (e instanceof Unary) {                            // ���� ������ ���
			
			Unary u = (Unary)e;              
			
			Type typ = TypeSystemChecker.typeOf(u.term,tm);       // �ǿ������� Ÿ�� Ȯ��
			
			Expression term1 = T(u.term,tm);                      // �ǿ����ڿ� ���� AST ��ȯ
			
			if((typ==Type.BOOL) && (u.op.NotOp()))                // !(bool) �� ���, 
				return new Unary(u.op.boolMap(u.op.val),term1);   // Not �������� �� ��ȯ�� x (Because, Not �����ڴ�, Bool������ ���̴� ������!!)
			
			else if ((typ==Type.FLOAT) && (u.op.NegateOp()))     // -(float) �� ���,
				return new Unary(new Operator(Operator.FLOAT_NEG),term1);  // - �����ڴ� float �� �����ڷ� ��ȯ (FLOAT_NEG)                              
			
			else if ((typ==Type.INT)&& (u.op.NegateOp()))        // -(int) �� ���
				return new Unary (new Operator(Operator.INT_NEG),term1);  // - �����ڴ� int �� �����ڷ� ��ȯ (INT_NEG)
			
			else if ((typ==Type.INT) && (u.op.charOp() || u.op.floatOp()))    // float(int), char(int)         	
					return new Unary(u.op.intMap(u.op.val),term1);      // I2C or I2F
					
			else if (typ==Type.FLOAT && u.op.intOp())              // int(float) �� ���
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
		
		TypeMap map = TypeSystemChecker.typing(prog.decpart);        // Declarations -> TypeMap ��ȯ 
		
		System.out.println(map);                            // ��ȯ�� TypeMap ���
		TypeSystemChecker.V(prog);                          // �־��� AST�� ���Ͽ�, Static Type Check!!
		Program xform_AST = T(prog,map);                    // AST ����ȯ!!!
		xform_AST.display();                                // ����ȯ�� AST ���!!!

	}
}
