/* Generated By:JJTree: Do not edit this line. OUpdateEdgeStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.orientechnologies.orient.core.sql.parser;

public
class OUpdateEdgeStatement extends OUpdateStatement {
  public OUpdateEdgeStatement(int id) {
    super(id);
  }

  public OUpdateEdgeStatement(OrientSql p, int id) {
    super(p, id);
  }

  protected String getStatementType() {
    return "UPDATE EDGE ";
  }

  /** Accept the visitor. **/
  public Object jjtAccept(OrientSqlVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=496f32976ee84e3a3a89d1410dc134c5 (do not edit this line) */
