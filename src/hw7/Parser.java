package hw7;
import java.util.*;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.tree.*;




public class Parser {
    // Recursive descent parser that inputs a C++Lite program and 
    // generates its abstract syntax.  Each method corresponds to
    // a concrete syntax grammar rule, which appears as a comment
    // at the beginning of the method.
  
    Token token;          // current token from the input stream
    Lexer lexer;
  
    public Parser(Lexer ts) { // Open the C++Lite source program
        lexer = ts;                          // as a token stream, and
        token = lexer.next();            // retrieve its first Token
    }
  
    private String match (TokenType t) { // * return the string of a token if it matches with t *
        String value = token.value();
        if (token.type().equals(t))
            token = lexer.next();
        else
            error(t);
        return value;
    }
  
    private void error(TokenType tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }
  
    private void error(String tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }
  
    public Program program() {
        // Program --> void main ( ) '{' Declarations Statements '}'
        TokenType[ ] header = {TokenType.Int, TokenType.Main,
                          TokenType.LeftParen, TokenType.RightParen};
        for (int i=0; i<header.length; i++)   // bypass "int main ( )"
            match(header[i]);
        match(TokenType.LeftBrace);
        // student exercise
        Declarations d = declarations();
        Block b = statements();
        match(TokenType.RightBrace);
        return new Program(d, b);  // student exercise
    }
  
    private Declarations declarations () {
        // Declarations --> { Declaration }
        Declarations d = new Declarations();
        while(isType()) { 
           declaration(d);
        }
        return d;  // student exercise
    }
  
    private void declaration (Declarations ds) {
        // Declaration  --> Type Identifier { , Identifier } ;
        // student exercise
        Declaration d;
        Variable v;
        Type t = type();
        v = new Variable(match(TokenType.Identifier));
        d = new Declaration(v, t);
        ds.add(d);
        while (token.type().equals(TokenType.Comma)) {
        	token = lexer.next();
        	v = new Variable(match(TokenType.Identifier));
            d = new Declaration(v, t);
            ds.add(d);
        }
        match(TokenType.Semicolon);
    }
  
    private Type type () {
        // Type  -->  int | bool | float | char 
        Type t = null;
        // student exercise
        if (token.type().equals(TokenType.Int)) {
           t = Type.INT;
        } else if (token.type().equals(TokenType.Bool)) {
           t = Type.BOOL;
        } else if (token.type().equals(TokenType.Float)) {
           t = Type.FLOAT;
        } else if (token.type().equals(TokenType.Char)) {
           t = Type.CHAR;
        } else {
           error(token.type());
        }
        token = lexer.next();
        return t;          
    }
  
    private Statement statement() {
        // Statement --> ; | Block | Assignment | IfStatement | WhileStatement
        Statement s = new Skip();
        // student exercise
        if (token.type().equals(TokenType.Semicolon)) {
        	
        	s = new Skip();
        	token = lexer.next();
        } else if (token.type().equals(TokenType.LeftBrace)) {
            match(TokenType.LeftBrace);
            s = statements();
            match(TokenType.RightBrace);
        } else if (token.type().equals(TokenType.Identifier)) {
        	s = assignment();
        } else if (token.type().equals(TokenType.If)) {
        	s = ifStatement();
        } else if (token.type().equals(TokenType.While)) {
        	s = whileStatement();
        } else {
        	error(token.type());
        }
        return s;
    }
    
    private Block statements () {
        // Block --> '{' Statements '}'
        Block b = new Block();
        // student exercise
        while(isStatement()) {
        	
        	b.members.add(statement());
        }
     
        return b;
    }
  
    private Assignment assignment () {
        // Assignment --> Identifier = Expression ;
    	
    	Variable target = new Variable(match(TokenType.Identifier));
    	match(TokenType.Assign);
    	Expression source = expression();
    	match(TokenType.Semicolon);
        return new Assignment(target, source);  // student exercise
    }
  
    private Conditional ifStatement () {
        // IfStatement --> if ( Expression ) Statement [ else Statement ]
    	// student exercise
    	Conditional con;
    	match(TokenType.If);                      
    	match(TokenType.LeftParen);                     
    	Expression expr = expression();          
    	match(TokenType.RightParen);              
    	Statement stmt = statement();                       	
    	
    	if(token.type().equals(TokenType.Else)) {     
    		match(TokenType.Else);                   
    		Statement else_ = statement();           
    		con = new Conditional(expr,stmt,else_);      
    	}    	
    	else {                                         
    		con = new Conditional(expr,stmt);
    	}                                   
        return con;  
    }
  
    private Loop whileStatement () {
        // WhileStatement --> while ( Expression ) Statement
    	
    	match(TokenType.While);
    	match(TokenType.LeftParen);
    	Expression e = expression();
    	match(TokenType.RightParen);
    	Statement s = statement();
    	return new Loop(e, s);
    }

    private Expression expression () {
        // Expression --> Conjunction { || Conjunction }
    	
    	Expression e = conjunction();
        while (token.type().equals(TokenType.Or)) {
        	
            Operator op = new Operator(match(token.type()));
            Expression term2 = conjunction();
            e = new Binary(op, e, term2);
        }
        return e;  // student exercise
    }
  
    private Expression conjunction () {
        // Conjunction --> Equality { && Equality }
    	
    	Expression e = equality();
        while (token.type().equals(TokenType.And)) {
        	
            Operator op = new Operator(match(token.type()));
            Expression term2 = equality();
            e = new Binary(op, e, term2);
        }
        return e; // student exercise
    }
  
    private Expression equality () {
        // Equality --> Relation [ EquOp Relation ]
    	
    	Expression rel = relation();
    	if (isEqualityOp()) {
    		
            Operator op = new Operator(match(token.type()));
            Expression rel2 = relation();
            rel = new Binary(op, rel, rel2);
       }
        return rel;// student exercise
    }

    private Expression relation (){
        // Relation --> Addition [RelOp Addition] 
    	Expression a = addition();
    	if (isRelationalOp()) {
    		
            Operator op = new Operator(match(token.type()));
            Expression a2 = addition();
            a = new Binary(op, a, a2);
       }
        return a; // student exercise
    }
  
    private Expression addition () {
        // Addition --> Term { AddOp Term }
        Expression e = term();
        while (isAddOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = term();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression term () {
        // Term --> Factor { MultiplyOp Factor }
        Expression e = factor();
        while (isMultiplyOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = factor();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression factor() {
        // Factor --> [ UnaryOp ] Primary 
        if (isUnaryOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term = primary();
            return new Unary(op, term);
        }
        else return primary();
    }
  
    private Expression primary () {
        // Primary --> Identifier | Literal | ( Expression )
        //             | Type ( Expression )
        Expression e = null;
        if (token.type().equals(TokenType.Identifier)) {
            e = new Variable(match(TokenType.Identifier));
        } else if (isLiteral()) {
            e = literal();
        } else if (token.type().equals(TokenType.LeftParen)) {
            token = lexer.next();
            e = expression();       
            match(TokenType.RightParen);
        } else if (isType( )) {
            Operator op = new Operator(match(token.type()));
            match(TokenType.LeftParen);
            Expression term = expression();
            match(TokenType.RightParen);
            e = new Unary(op, term);
        } else error("Identifier | Literal | ( | Type");
        return e;
    }
    
    

    private Value literal( ) {
       Value v = null;
       if (token.type().equals(TokenType.IntLiteral)) {
          v = new IntValue(Integer.parseInt(match(TokenType.IntLiteral)));
       }
       else if (token.type().equals(TokenType.FloatLiteral)) {
          v = new FloatValue(Float.parseFloat(match(TokenType.FloatLiteral)));
       } 
       else if (token.type().equals(TokenType.CharLiteral)) {
       v = new CharValue(match(TokenType.CharLiteral).charAt(0));
       } 
       else if (isBooleanLiteral( )) { 
          if (token.type().equals(TokenType.True)) {
             match(TokenType.True);
          } 
          else if (token.type().equals(TokenType.False)) {
             match(TokenType.False);
          }
          v = new BoolValue(Boolean.valueOf(token.value()));
       } else {
          error ("Literal Error");
       }
       return v;// student exercise
    }
  

    private boolean isAddOp( ) {
        return token.type().equals(TokenType.Plus) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isMultiplyOp( ) {
        return token.type().equals(TokenType.Multiply) ||
               token.type().equals(TokenType.Divide);
    }
    
    private boolean isUnaryOp( ) {
        return token.type().equals(TokenType.Not) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isEqualityOp( ) {
        return token.type().equals(TokenType.Equals) ||
            token.type().equals(TokenType.NotEqual);
    }
    
    private boolean isRelationalOp( ) {
        return token.type().equals(TokenType.Less) ||
               token.type().equals(TokenType.LessEqual) || 
               token.type().equals(TokenType.Greater) ||
               token.type().equals(TokenType.GreaterEqual);
    }
    
    private boolean isType( ) {
        return token.type().equals(TokenType.Int)
            || token.type().equals(TokenType.Bool) 
            || token.type().equals(TokenType.Float)
            || token.type().equals(TokenType.Char);
    }
    
    private boolean isLiteral( ) {
        return token.type().equals(TokenType.IntLiteral) ||
            isBooleanLiteral() ||
            token.type().equals(TokenType.FloatLiteral) ||
            token.type().equals(TokenType.CharLiteral);
    }
    
    private boolean isBooleanLiteral( ) {
        return token.type().equals(TokenType.True) ||
            token.type().equals(TokenType.False);
    }
    
    private boolean isStatement( ) {
       return (token.type().equals(TokenType.Semicolon) ||
             token.type().equals(TokenType.If) ||
             token.type().equals(TokenType.While) ||
             token.type().equals(TokenType.LeftBrace) ||
             token.type().equals(TokenType.Identifier));
    }
    
    public static void main(String args[]) {
    	
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        JTree tree;
        JFrame frame = new JFrame();
        tree = new JTree(prog.makeRoot());
        tree.setVisibleRowCount(10);
       
        JScrollPane treeScroll = new JScrollPane(tree);
        frame.add(treeScroll);
       
        frame.setTitle("Abstract Syntax Tree");
        frame.setSize(400, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        frame.setVisible(true);  
    } //main

} // Parser
