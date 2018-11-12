/*
    This class provides a recursive descent parser 
    for Corgi (a simple calculator language),
    creating a parse tree which can be interpreted
    to simulate execution of a Corgi program
*/

import java.util.*;
import java.io.*;

public class Parser {

   private Lexer lex;

   public Parser( Lexer lexer ) {
      lex = lexer;
   }

   public Node parseProgram() {
      return parseStatements();
   }

   private Node parseStatements() {
      System.out.println("-----> parsing <statements>:");
 
      Node first = parseStatement();
 
      // look ahead to see if there are more statement's
      Token token = lex.getNextToken();
 
      if ( token.isKind("eof") ) {
         return new Node( "stmts", first );
      }
      else {
         lex.putBackToken( token );
         Node second = parseStatements();
         return new Node( "stmts", first, second );
      }
   }// <statements>

   private Node parseStatement() {
      System.out.println("-----> parsing <statement>:");
 
      Token token = lex.getNextToken();

      System.out.println(token.toString());
 
      // ---------------->>>  print <string>  or   print <expr>
      if ( token.isKind("print") ) {
         token = lex.getNextToken();
 
         if ( token.isKind("string") ) {// print <string>
            return new Node( "prtstr", token.getDetails() );
         }
         else {// must be first token in <expr>
            // put back the token we looked ahead at
            lex.putBackToken( token );
            Node first = parseExpr();
            return new Node( "prtexp", first );
         }
      // ---------------->>>  newline
      }
      else if ( token.isKind("newline") ) {
         return new Node( "nl" );
      }
      else if ( token.isKind("def")) {
        System.out.println("Found def");
        token = lex.getNextToken();
        String funcName = token.getDetails();
        Node node = parseFunction(funcName);
        return null;
      }
      // --------------->>>   <var> = <expr>
      else if ( token.isKind("var") ) {
         String name = token.getDetails();
         token = lex.getNextToken();
         if (token.matches("single", "(")) {
           lex.putBackToken(token);
           Node funcCallNode = parseFuncCall(name);
           return funcCallNode;
         } else {
          errorCheck( token, "single", "=" );
          Node varExprNode = parseExpr();
          return new Node( "sto", name, varExprNode );
         }
      }
      else {
         System.out.println("Token " + token + 
                             " can't begin a statement");
         System.exit(1);
         return null;
      }
 
   }// <statement>

   private Node parseFunction(String funcName) {
      Node paramNode = parseFuncSignature();
      Node funcStatementNode = parseFunctionStatements();
      return new Node(funcName, paramNode, funcStatementNode);
   }

   private Node parseFunctionStatements() {
      Token token = lex.getNextToken();
      if (token.isKind("end")) {
        return null;
      }
      Node statementNode = parseStatements();
      Node funcStatementNode = parseFunctionStatements();
      return new Node("stmts", statementNode, funcStatementNode);
   }

   private Node parseFuncSignature() {
     Token token = lex.getNextToken();
     if (token.matches("single", "(") || token.matches("single", ",")) {
       Node node = parseFuncSignature();
       return node;
     } else if (token.matches("single", ")")) {
       return null;
     } else if (token.isKind("var")) {
       Node varNode = parseFuncSignature();
       Node paramNode = new Node("param", token.getDetails(), varNode);
       return paramNode;
     } else {
       System.out.println("Token " + token + " must be a var/param");
       System.exit(1);
       return null;
     }
   }

   private Node parseFuncCallSignature() {
     Token token = lex.getNextToken();
     if (token.matches("single", "(") || token.matches("single", ",")) {
      Node node = parseFuncCallSignature();
      return node;
    } else if (token.matches("single", ")")) {
      return null;
    } else if (token.isKind("var")) {
      Node varNode = parseFuncCallSignature();
      Node argNode = new Node("arg", token.getDetails(), varNode);
      return argNode;
    } else {
      System.out.println("Token " + token + " must be a var/arg");
      System.exit(1);
      return null;
    }
   }

   private Node parseFuncCall(String funcName) {
      Node argNode = parseFuncCallSignature();
      Node funcCallNode = new Node( "funcCall", funcName, argNode );
      return funcCallNode;
   }

   private Node parseExpr() {
      System.out.println("-----> parsing <expr>");

      Node first = parseTerm();

      // look ahead to see if there's an addop
      Token token = lex.getNextToken();
 
      if ( token.matches("single", "+") ||
           token.matches("single", "-") 
         ) {
         Node second = parseExpr();
         return new Node( token.getDetails(), first, second );
      }
      else {// is just one term
         lex.putBackToken( token );
         return first;
      }

   }// <expr>

   private Node parseTerm() {
      System.out.println("-----> parsing <term>");

      Node first = parseFactor();

      // look ahead to see if there's a multop
      Token token = lex.getNextToken();
 
      if ( token.matches("single", "*") ||
           token.matches("single", "/") 
         ) {
         Node second = parseTerm();
         return new Node( token.getDetails(), first, second );
      }
      else {// is just one factor
         lex.putBackToken( token );
         return first;
      }
      
   }// <term>

   private Node parseFactor() {
      System.out.println("-----> parsing <factor>");

      Token token = lex.getNextToken();

      if ( token.isKind("num") ) {
         return new Node("num", token.getDetails() );
      }
      else if ( token.isKind("var") ) {
         return new Node("var", token.getDetails() );
      }
      else if ( token.matches("single","(") ) {
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         return first;
      }
      else if ( token.isKind("bif0") ) {
         String bifName = token.getDetails();
         token = lex.getNextToken();
         errorCheck( token, "single", "(" );
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         
         return new Node( bifName );
      }
      else if ( token.isKind("bif1") ) {
         String bifName = token.getDetails();
         token = lex.getNextToken();
         errorCheck( token, "single", "(" );
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         
         return new Node( bifName, first );
      }
      else if ( token.isKind("bif2") ) {
         String bifName = token.getDetails();
         token = lex.getNextToken();
         errorCheck( token, "single", "(" );
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", "," );
         Node second = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         
         return new Node( bifName, first, second );
      }
      else if ( token.matches("single","-") ) {
         Node first = parseFactor();
         return new Node("opp", first );
      }
      else if ( token.isKind("if") ) {
        lex.putBackToken(token);
        Node node = parseConditional();
        return node;
      }
      else {
         System.out.println("Can't have factor starting with " + token );
         System.exit(1);
         return null;
      }
      
   }// <factor>

   private Node parseConditional() {
     Token token = lex.getNextToken();
     if (token.isKind("return")) {
       Node returnNode = parseConditional();
       return new Node("return", returnNode);
     }
     else if (token.isKind("if")) {
       Node ifNode = parseConditional();
       return new Node("if", ifNode);
     }
     else if (token.isKind("else")) {
       Node elseNode = parseConditional();
       return new Node("else", elseNode);
     }
     else if (token.isKind("end")) {
       return null;
     }
     else {
       Node exprNode = parseExpr();
       Node condNode = parseConditional();
       return new Node(token.getDetails(), exprNode, condNode);
     }
   }

  // check whether token is correct kind
  private void errorCheck( Token token, String kind ) {
    if( ! token.isKind( kind ) ) {
      System.out.println("Error:  expected " + token + 
                         " to be of kind " + kind );
      System.exit(1);
    }
  }

  // check whether token is correct kind and details
  private void errorCheck( Token token, String kind, String details ) {
    if( ! token.isKind( kind ) || 
        ! token.getDetails().equals( details ) ) {
      System.out.println("Error:  expected " + token + 
                          " to be kind=" + kind + 
                          " and details=" + details );
      System.exit(1);
    }
  }

}