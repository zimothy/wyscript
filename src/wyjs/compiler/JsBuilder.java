package wyjs.compiler;

import java.util.ArrayList;
import java.util.List;

import wyjs.ast.JsBase;
import wyjs.ast.JsNode;
import wyjs.ast.expr.JsAccess;
import wyjs.ast.expr.JsBinOp;
import wyjs.ast.expr.JsExpr;
import wyjs.ast.expr.JsInvoke;
import wyjs.ast.expr.JsLiteral;
import wyjs.ast.expr.JsUnOp;
import wyjs.ast.expr.JsVariable;
import wyjs.ast.stmt.JsConstant;
import wyjs.ast.stmt.JsFunctionStmt;
import wyjs.ast.stmt.JsStmt;
import wyjs.ast.util.JsHelpers;
import wyjs.lang.Expr;
import wyjs.lang.Expr.BOp;
import wyjs.lang.Expr.BinOp;
import wyjs.lang.Expr.Constant;
import wyjs.lang.Expr.Invoke;
import wyjs.lang.Expr.ListAccess;
import wyjs.lang.Expr.NamedConstant;
import wyjs.lang.Expr.RecordAccess;
import wyjs.lang.Expr.TypeConst;
import wyjs.lang.Expr.UOp;
import wyjs.lang.Expr.UnOp;
import wyjs.lang.Expr.Variable;
import wyjs.lang.WhileyFile;
import wyjs.lang.WhileyFile.ConstDecl;
import wyjs.lang.WhileyFile.Decl;
import wyjs.lang.WhileyFile.FunDecl;
import wyjs.lang.WhileyFile.ImportDecl;
import wyjs.lang.WhileyFile.TypeDecl;
import wyjs.util.SyntaxError;

public class JsBuilder {

  public JsNode build(WhileyFile wfile) {
    List<JsStmt> nodes = new ArrayList<JsStmt>();
    for (Decl decl : wfile.declarations) {
      nodes.add(doDecl(wfile, decl));
    }
    return new JsBase(nodes);
  }

  public JsStmt doDecl(WhileyFile wfile, Decl decl) {
    if (decl instanceof ImportDecl) {
      return doImport(wfile, (ImportDecl) decl);
    } else if (decl instanceof ConstDecl) {
      return doConst(wfile, (ConstDecl) decl);
    } else if (decl instanceof TypeDecl) {
      return doType(wfile, (TypeDecl) decl);
    } else if (decl instanceof FunDecl) {
      return doFun(wfile, (FunDecl) decl);
    }

    throw new SyntaxError("Unrecognised top-level declaration " + decl.name(),
        wfile.filename, 0, 0);
  }

  public JsStmt doImport(WhileyFile wfile, ImportDecl decl) {
    throw new SyntaxError("No Javascript equivalent to import.",
        wfile.filename, 0, 0);
  }

  public JsStmt doConst(WhileyFile wfile, ConstDecl decl) {
    return new JsConstant(decl.name, doExpr(wfile, decl.constant));
  }

  public JsStmt doType(WhileyFile wfile, TypeDecl decl) {
    throw new SyntaxError("No Javascript equivalent to types.", wfile.filename,
        0, 0);
  }

  public JsStmt doFun(WhileyFile wfile, FunDecl decl) {
    return new JsFunctionStmt(decl.name);
  }

  public JsExpr doExpr(WhileyFile wfile, Expr expr) {
    if (expr instanceof Variable) {
      return doVariable(wfile, (Variable) expr);
    } else if (expr instanceof NamedConstant) {
      return doNamedConstant(wfile, (NamedConstant) expr);
    } else if (expr instanceof Constant) {
      return doConstant(wfile, (Constant) expr);
    } else if (expr instanceof TypeConst) {
      return doTypeConst(wfile, (TypeConst) expr);
    } else if (expr instanceof BinOp) {
      return doBinOp(wfile, (BinOp) expr);
    } else if (expr instanceof ListAccess) {
      return doListAccess(wfile, (ListAccess) expr);
    } else if (expr instanceof UnOp) {
      return doUnOp(wfile, (UnOp) expr);
      // } else if (expr instanceof NaryOp) {
      // } else if (expr instanceof Comprehension) {
    } else if (expr instanceof RecordAccess) {
      return doRecordAccess(wfile, (RecordAccess) expr);
      // } else if (expr instanceof DictionaryGen) {
      // } else if (expr instanceof RecordGen) {
      // } else if (expr instanceof TupleGen) {
    } else if (expr instanceof Invoke) {
      return doInvoke(wfile, (Invoke) expr);
    }

    throw new SyntaxError("Unrecognised expression " + expr, wfile.filename, 0,
        0);
  }

  public JsExpr doVariable(WhileyFile wfile, Variable expr) {
    return new JsVariable(expr.var);
  }

  public JsExpr doNamedConstant(WhileyFile wfile, NamedConstant expr) {
    return new JsVariable(expr.var);
  }

  public JsExpr doConstant(WhileyFile wfile, Constant expr) {
    return new JsLiteral(expr.value.toString());
  }

  public JsExpr doTypeConst(WhileyFile wfile, TypeConst expr) {
    throw new SyntaxError("Unrecognised expression " + expr, wfile.filename, 0,
        0);
  }

  public JsExpr doBinOp(WhileyFile wfile, BinOp expr) {
    JsBinOp bop = doBinOp(expr.op);
    JsExpr lhs = doExpr(wfile, expr.lhs), rhs = doExpr(wfile, expr.rhs);

    if (bop != null) {
      return bop.newNode(lhs, rhs);
    }

    switch (expr.op) {
    case UNION:
      return new JsInvoke(new JsAccess(lhs, "concat"), rhs);
    case INTERSECTION:
      return JsHelpers.intersect(lhs, rhs);
    case ELEMENTOF:
      return JsHelpers.in(lhs, rhs);
    case SUBSET:
      return JsHelpers.subset(lhs, rhs, false);
    case SUBSETEQ:
      return JsHelpers.subset(lhs, rhs, true);
      // case LISTRANGE:
      // case TYPEIMPLIES:
    }

    throw new SyntaxError("Unrecognised binary operator " + expr.op,
        wfile.filename, 0, 0);
  }

  public JsBinOp doBinOp(BOp bop) {
    switch (bop) {
    case AND:
      return JsBinOp.AND;
    case OR:
      return JsBinOp.OR;
    case ADD:
      return JsBinOp.ADD;
    case SUB:
      return JsBinOp.SUB;
    case MUL:
      return JsBinOp.MUL;
    case DIV:
      return JsBinOp.DIV;
    case EQ:
      return JsBinOp.EQ;
    case NEQ:
      return JsBinOp.NEQ;
    case LT:
      return JsBinOp.LT;
    case LTEQ:
      return JsBinOp.LTE;
    case GT:
      return JsBinOp.GT;
    case GTEQ:
      return JsBinOp.GTE;
    case TYPEEQ:
      return JsBinOp.IOF;
    default:
      return null;
    }
  }

  public JsExpr doListAccess(WhileyFile wfile, ListAccess expr) {
    return new JsAccess(doExpr(wfile, expr.src), doExpr(wfile, expr.index));
  }

  public JsExpr doUnOp(WhileyFile wfile, UnOp expr) {
    JsUnOp uop = doUnOp(expr.op);
    JsExpr mhs = doExpr(wfile, expr.mhs);

    if (uop != null) {
      return uop.newNode(mhs);
    }

    switch (expr.op) {
    case LENGTHOF:
      return new JsAccess(mhs, "length");
    }

    throw new SyntaxError("Unrecognised unary operator " + expr.op,
        wfile.filename, 0, 0);
  }

  public JsUnOp doUnOp(UOp uop) {
    switch (uop) {
    case NOT:
      return JsUnOp.NOT;
    case NEG:
      return JsUnOp.NEG;
    default:
      return null;
    }
  }

  public JsExpr doRecordAccess(WhileyFile wfile, RecordAccess expr) {
    return new JsAccess(doExpr(wfile, expr.lhs), expr.name);
  }

  public JsExpr doInvoke(WhileyFile wfile, Invoke expr) {
    List<JsExpr> arguments = new ArrayList<JsExpr>();

    for (Expr argument : expr.arguments) {
      arguments.add(doExpr(wfile, argument));
    }

    JsExpr function = expr.receiver == null ? new JsVariable(expr.name)
        : new JsAccess(doExpr(wfile, expr.receiver), expr.name);

    return new JsInvoke(function, arguments);
  }

}
