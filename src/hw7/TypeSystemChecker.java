package hw7;


public class TypeSystemChecker {

	public static void check (boolean err_con, String str) {
		if(!err_con) {                                        // check �޼ҵ��, ������� Ÿ�� ��ȿ �˻翡, ���๮�� Ÿ�� ��ȿ �˻翡 ���� ����                                    
			System.err.println(str);
			System.exit(1);
		}
		return;		
	}
	
	
	public static void V (Program prg) {                  // �߻� ���� Ʈ���� ���� ���� TypeChecking ����
		V(prg.decpart);                                   // �����(Declarations = Declaration*) �� Type-Validity �� �˻�
		V(prg.body, typing(prg.decpart));                 // ���๮�� (Block = Statement*) �� Type-Validity �� �˻� 
	}
	
	public static void V (Declarations declrs) {          // �����(Declarations = Declaration*) �� Type-Validity �� �˻� 
		
		for(int i=0; i<declrs.size()-1;i++)             
			for(int j=i+1; j<declrs.size();j++) {
				Declaration di = declrs.get(i);                                 // ������ �������� �̸��� �ϳ��� ���Ͽ�
				Declaration dj = declrs.get(j);
				check(!(di.v.equals(dj.v)),"duplicate declaraion : " + dj.v);   // ������ �������� �̸��� �ϳ��� ��
     			                                                                // ���ٸ�(�������� �̸��� �ߺ��ǹǷ�, Type Error �߻�!!), check �޼ҵ�� ���������� Static TypeChecking�� ���� ���� 
			}              
	}
	

	public static TypeMap typing (Declarations declrs) {  // typing �޼ҵ��, ���α׷��� ����� ������(������ : Ÿ��)�� �迭(Arraylist)�� TypeMap ���� ��ȯ
		TypeMap map = new TypeMap();                      
		
		for(Declaration declr : declrs)                   // Decalarations �迭�� ����:Ÿ�� �� �ϳ��� ������, TypeMap�� ����
			map.put(declr.v, declr.t);
		
		return map;                                       // ������� TypeMap ��ȯ 
	}
	
	
	
	 public static void V (Statement s, TypeMap tm) {
		 	
	        if ( s == null )    
	            throw new IllegalArgumentException( "AST error: null statement");
	        
	        else if (s instanceof Skip) return;           // Skip(;) ���๮�� �׻� Type-Valid!!
	        
	        else if (s instanceof Assignment) {           // ���Թ��� ���� Type-Validity �˻�!!
	            Assignment a = (Assignment)s;
	            
	            check( tm.containsKey(a.target)                            //  �ش� ���Թ��� ������ ����Ǿ��ִ���, TypeMap �� ������
	                   , " undefined target in assignment: " + a.target);  //���� �Ǿ����� �ʴٸ�, Type Checking Error �߻��� ���� ����
	            
	            V(a.source, tm);                                           // Expression �� Type Validity �� �˻�
	       
	            Type ttype = (Type)tm.get(a.target);                       // ���Թ��� ������ Ÿ���� ����
	            Type srctype = typeOf(a.source, tm);                       // ���Թ��� Expression�� Ÿ���� ����
	            
	            if (ttype != srctype) {                                    // ������ Expression�� Type�� �ٸ����..
	  
	                if (ttype == Type.FLOAT)                               // ������ Type�� float �� ���, Expression �� Type�� int ���� Ȯ��  (Widdening �� ���)
	                    check( srctype == Type.INT                         // �׷��� �ʴٸ�, Type-Error �߻���Ŵ
	                           , "mixed mode assignment to " + a.target);  
	                
	                else if (ttype == Type.INT)                            // ������ Type�� Int �� ���, Expression�� Type �� Char ���� Ȯ��   (Widdening �� ���)
	                    check( srctype == Type.CHAR                        // �׷��� �ʴٸ�, Type-Error �߻���Ŵ
	                           , "mixed mode assignment to " + a.target);
	                
	                else                                                  
	                    check( false                                       // �׿��� ���� ���� Type _invalid!!
	                           , "mixed mode assignment to " + a.target);
	            }
	            return;
	        } 
	        
	        else if (s instanceof Conditional) {                          // if ���ǹ��� ���� Type-Validity �˻�
	        	
	            Conditional c = (Conditional)s;                           
	            V(c.test, tm);                                            // ����, ���ǽ��� Type-Valid ���� �˻�
	            Type testType = typeOf(c.test, tm);                       // ����, ���ǽ�(test)�� Type�� boolean ���� �˻�
	            
	            check(testType == Type.BOOL, "test expression must be bool but "+ testType); // ���ǹ��� Type�� boolean�� �ƴ϶��, Type-Error �߻�!!
	            
	                                                                  // ���ǹ��� Type�� boolean�� �´ٸ�
	            V(c.thenbranch, tm);                                  // if �� ���๮�鿡 ����, Type-Validity �˻�
	            V(c.elsebranch, tm);                                  // else ���鿡 ����, Type-Validity �˻�
	             return;
	          
	        }
	        
	        else if (s instanceof Loop) {                                 // While �ݺ����� ����, Type-Validity �˻�
	        	
	            Loop l = (Loop)s;                                         
	            V(l.test, tm);                                            // ����, ���ǹ��� Type-Validity �˻�
	            Type testType = typeOf(l.test, tm);                       // ����, ���ǹ��� Type�� boolean ���� �˻�
	            
	            check (testType==Type.BOOL, "test expression must be bool but "+ testType); // ���ǹ��� Type�� boolean�� �ƴ϶��, Type-Error �߻�
	                                              
	            V(l.body, tm);                                        // �´ٸ�, if �� ������ ���๮���� Type-Validity �˻�

	        }
	        
	        else if (s instanceof PrintStatement) {                  // PrintStatement �� Type Validity �� �˻�
	        	PrintStatement print = (PrintStatement)s;           
	        	V(print.output, tm);                                 // ����, Expression�� Type Validity�� �˻�   
	            
	        	Type outputType = typeOf(print.output, tm);          // ����, Expression�� Type�� Ȯ���ϰ�, ����, �ý��� �� ���ǵ� Ÿ������ Ȯ��
	        	
	        	if(s instanceof PrintCharStatement)                // PrintChar�� ���� Type �˻�
	        		check(outputType == Type.CHAR,"printCh must come with type char but "+outputType+" has come");
	        	
	        	else if(s instanceof PrintBoolStatement) 
	        		check(outputType == Type.BOOL,"printBl must come with type bool but "+outputType+" has come");
	        	
	        	else if (s instanceof PrintIntStatement) 
	        		check(outputType == Type.INT,"printInt must come with type int but "+outputType+" has come");
	        	
	        	else if(s instanceof PrintFloatStatement) 
	        		check(outputType == Type.FLOAT,"printFlt must come with type float but "+outputType+" has come");
	        	
	        	else {                                                          // �Ϲ��� print statement��, Type Casting �ʿ� ����
	        	check(outputType == Type.BOOL || outputType == Type.CHAR 
	        			||outputType == Type.FLOAT || outputType == Type.INT, "Non Defined Type " + outputType+" has come");   
	        	}
	        }
	        
	        else if (s instanceof Block) {                                // {...} �� Block ��  
	            Block b = (Block)s;
	            
	            for(Statement i : b.members)                            // {} ������ ��� ���๮�鿡 ���� Type Validity �˻�
	                V(i, tm);     
	        } 
	        
	        else 
	        	throw new IllegalArgumentException("should never reach here");
	        
	    }
	
	
	 public static void V (Expression e, TypeMap tm) {          // Expression (����, ǥ����) �� Type Validity �� �˻� 
		 
	        if (e instanceof Value)                             // Literal Value�� Ÿ���� �׻� ��ȿ!!
	            return;
	        
	        if (e instanceof Variable) {                        // Variable �� Ÿ���� ��ȿ ���� �˻�
	            Variable v = (Variable)e;        
	            check( tm.containsKey(v)                        // ���๮�� Variable �� Ÿ���� �����ϴ�?? = TypeMap�� �ش� �̸��� ������ �ִ��� ���� 
 	                   , "undeclared variable: " + v);          // ���ٸ�, Type-Error �߻���Ų�� ���� ����
	            return;
	        }
	        
	        if (e instanceof Binary) {                          // Binary(���� ǥ����)�� Ÿ���� ��ȿ���� �˻�
	            Binary b = (Binary) e;                          // Binary = BinaryOP op ; Expression term1,term2
	            
	            Type typ1 = typeOf(b.term1, tm);                // term1 (�ǿ�����1)�� Type �� ���� 
	            Type typ2 = typeOf(b.term2, tm);                // term2 (�ǿ�����2)�� Type �� ����
	            
	            V (b.term1, tm);                                // Expression term1�� Type Validity �˻�
	            V (b.term2, tm);                                // Expression term2�� Type Validity �˻�
	            
	            if (b.op.ArithmeticOp())                        // Binary �����ڰ� ��Ģ�������� ���
	            	
	                check( typ1 == typ2 &&                       // �ݵ�� �� �ǿ������� Type�� ����, int Ȥ�� float ������
	                       (typ1 == Type.INT || typ1 == Type.FLOAT)  // �׷��� ������, Type Checking ����!! 
	                       , "type error for " + b.op);
	            
	            else if (b.op.RelationalOp( ))                   // Binary �����ڰ� ���� �������� ���
	                check( typ1 == typ2 , "type error for " + b.op);    // �� �ǿ������� Type�� ������ Ȯ��
	            
	            else if (b.op.BooleanOp( ))                      // Binary �����ڰ� �� �������� ���
	                check( typ1 == Type.BOOL && typ2 == Type.BOOL, // �� �ǿ������� Type�� boolean ���� ������ Ȯ��
	                       b.op + ": non-bool operand");           // �׷��� ������, Type Checking ����!!
	            else
	                throw new IllegalArgumentException("should never reach here BinaryOp error");
	            return;
	        }
	        
	        if (e instanceof Unary) {                            // Unary(���� ����)�� Type�� ��ȿ���� �˻�
	            Unary u = (Unary) e;
	            
	            Type typ = typeOf(u.term, tm);                  // �ش� ���� ������ �ǿ������� Type �� ����  
	            V(u.term, tm);                                   // �ǿ������� Type-Validity ����
	            
	            if (u.op.NotOp()) {                              // ���� �����ڰ� ! �������� ���
	                check((typ == Type.BOOL), "NotOp type error "); // ���� Term�� Type�� �ݵ�� boolean �̾����
	            } 
	            
	            else if (u.op.NegateOp()) {                      // ���� �����ڰ� - �������� ���
	                check((typ == (Type.INT) || typ == (Type.FLOAT)), "NegateOp type error");  // �ǿ������� Type �� �ݵ�� Float, Int ������
	            }
	            
	            else if (u.op.floatOp() || u.op.charOp()) {                                                  // ���� �����ڰ� ����� ����ȯ (float(), char()) �� ���                                                 
	            	check(typ == (Type.INT)
	            			, "type error for float()/char() : int term must come but " + typ);             // �ǿ������� Type �� �ݵ�� int ���� ��
	            }
	            else if(u.op.intOp()) {                                                                      // ���� �����ڰ� ����� ����ȯ (int()) �� ���
	            	check(typ == (Type.FLOAT) || typ== (Type.CHAR)
	            			, "type error for int() : float/char term must come but " + typ);               // �ǿ������� Type �� �ݵ�� float Ȥ��, char �̾�� ��
	            }
	            
	            else {
	                throw new IllegalArgumentException("should never reach here UnaryOp error");
	            }
	            return;
	        }

	        throw new IllegalArgumentException("should never reach here");
	    }
	 
	 
	
	 public static Type typeOf (Expression e, TypeMap tm) {                // Expression (ǥ����)�� Ÿ���� ����
			
	        if (e instanceof Value)          // Literal Value�� Ÿ����, �ش� Literal Value�� Ÿ�԰� ����                                
	        	return ((Value)e).type; 
	        
	        if (e instanceof Variable) {     // Variable (����)�� Ÿ����, TypeMap ����, �ش� ������ ���ε� Ÿ�԰� ���� 
	            Variable v = (Variable)e; 
	            check (tm.containsKey(v), "undefined variable: " + v);
	            return (Type) tm.get(v);
	        }
	        
	        if (e instanceof Binary) {      // Binary (���� ����)�� Ÿ����, �� �ǿ����� ���� Type�� ���� ���� 
	            Binary b = (Binary)e;
	            
	            if (b.op.ArithmeticOp())                   // ��Ģ ������ (BinaryOP �� + - * /)�� ���
	                if (typeOf(b.term1,tm)== Type.FLOAT)   // term1 �� Type �� float ����, int ���� Ȯ��   
	                    return (Type.FLOAT);               // float �� ���, �ش� Expression�� Type�� float 
	                else return (Type.INT);                // �׷��� ���� ���, �ش� Expression�� Type�� int
	            
	            else if (b.op.RelationalOp() || b.op.BooleanOp())  // ���� ������ (BinaryOP�� &&, ||, <, >, <=, >=, !=, ==)�� ��,���� �������� ���
	                return (Type.BOOL);                         // Expression�� Type�� boolean
	        }
	        
	        if (e instanceof Unary) {                           // ���� �������� ���
	            Unary u = (Unary)e;
	            
	            if (u.op.NotOp( ))        return (Type.BOOL);           // �ش� ���� �����ڰ� ! �� ���, �ش� Expression �� boolean   
	            else if (u.op.NegateOp( )) return typeOf(u.term,tm);    // �ش� ���� �����ڰ� - �� ���, �ش� Expression �� �ǿ��������� Type�� ���� ���� 
	            else if (u.op.intOp( ))    return (Type.INT);           // �ش� ���� �����ڰ� int() ����� ĳ���� �������� ���, �ش� Expression�� int
	            else if (u.op.floatOp( )) return (Type.FLOAT);          // �ش� ���� �����ڰ� float() ����� ĳ���� �������� ���, �ش� Expression�� float
	            else if (u.op.charOp( ))  return (Type.CHAR);           // �ش� ���� �����ڰ� char() ����� ĳ���� �������� ���, �ش� Expression�� char
	        }
	        
	        throw new IllegalArgumentException("should never reach here");
	    } 
	

	public static void main(String[] args) {
		
		Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
       
        System.out.println("\n------------" + "Abstract Syntax Tree of" + "<"+ args[0] + ">" + "-------------\n");
        prog.display();
		
		TypeMap map = typing(prog.decpart);        // Declarations -> TypeMap ��ȯ 
		
		System.out.println(map);                            // ��ȯ�� TypeMap ���
		V(prog);
	}
}
