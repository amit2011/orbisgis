/* Generated By:JJTree: Do not edit this line. ASTSQLSelect.java Version 4.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.gdms.sql.parser;

public
class ASTSQLSelect extends SimpleNode {
  public ASTSQLSelect(int id) {
    super(id);
  }

  public ASTSQLSelect(SQLEngine p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SQLEngineVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=344f7b145a171b83896d0d20bc94eee9 (do not edit this line) */
