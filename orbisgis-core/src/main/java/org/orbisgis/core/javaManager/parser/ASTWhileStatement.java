/* Generated By:JJTree: Do not edit this line. ASTWhileStatement.java Version 4.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY= */
package org.orbisgis.core.javaManager.parser;

public class ASTWhileStatement extends SimpleNode {
  public ASTWhileStatement(int id) {
    super(id);
  }

  public ASTWhileStatement(JavaParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(JavaParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=e7163eb33d4dbd42d4d04ce609e3e63b (do not edit this line) */
