/* Generated By:JJTree: Do not edit this line. ASTExtendsList.java Version 4.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY= */
package org.orbisgis.core.javaManager.parser;

public class ASTExtendsList extends SimpleNode {
  public ASTExtendsList(int id) {
    super(id);
  }

  public ASTExtendsList(JavaParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(JavaParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=ff46502773352d3323aeada8abcc921c (do not edit this line) */
