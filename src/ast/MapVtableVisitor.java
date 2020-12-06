package ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapVtableVisitor implements Visitor{

    Map<String,ClassMap> classMaps;
    private String currClassName;
    private ClassMap currClassMap;
    private int currVarSpace=0;
    private Map<String,String> lastLocation;

    public Map<String, String> getLastLocation() {
        return lastLocation;
    }


    public MapVtableVisitor() {
        this.classMaps = new HashMap<>();
        this.lastLocation=new HashMap<>();
    }

    public Map<String, ClassMap> getClassMaps() {
        return classMaps;
    }

    @Override
    public void visit(Program program) {
        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
    }

    @Override
    public void visit(ClassDecl classDecl) {
        currClassName=classDecl.name();
        if(classDecl.superName()!=null)
        {
            currVarSpace=Integer.parseInt(lastLocation.get(classDecl.superName()));
            currClassMap=new ClassMap(classMaps.get(classDecl.superName()).getMethodMap(),classMaps.get(classDecl.superName()).getVarMap());
        }
        else
        {
            currVarSpace=8;
            currClassMap=new ClassMap();
        }
        classMaps.put(classDecl.name(),currClassMap);
        for (var methodDecl : classDecl.methoddecls()) {
            Map<String,String> methodMap=classMaps.get(classDecl.name()).getMethodMap();
            if(!methodMap.containsKey(methodDecl.name()))
            {
                int location=classMaps.get(classDecl.name()).getMethodMap().size();
                classMaps.get(classDecl.name()).getMethodMap().put(methodDecl.name(),Integer.toString(location));
            }
        }

        for (var fieldDecl : classDecl.fields()) {
            Map<String,String> varMap=classMaps.get(classDecl.name()).getVarMap();
            if(!varMap.containsKey(fieldDecl.name()))
            {
                int location=currVarSpace;
                fieldDecl.accept(this);
                classMaps.get(classDecl.name()).getVarMap().put(fieldDecl.name(),Integer.toString(location));
            }
            lastLocation.put(classDecl.name(),Integer.toString(currVarSpace));
        }
        lastLocation.put(classDecl.name(),Integer.toString(currVarSpace));


    }

    @Override
    public void visit(MainClass mainClass) {

    }

    @Override
    public void visit(MethodDecl methodDecl) {

    }

    @Override
    public void visit(FormalArg formalArg) {

    }

    @Override
    public void visit(VarDecl varDecl) {
    varDecl.type().accept(this);
    }

    @Override
    public void visit(BlockStatement blockStatement) {

    }

    @Override
    public void visit(IfStatement ifStatement) {

    }

    @Override
    public void visit(WhileStatement whileStatement) {

    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {

    }

    @Override
    public void visit(AssignStatement assignStatement) {

    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {

    }

    @Override
    public void visit(AndExpr e) {

    }

    @Override
    public void visit(LtExpr e) {

    }

    @Override
    public void visit(AddExpr e) {

    }

    @Override
    public void visit(SubtractExpr e) {

    }

    @Override
    public void visit(MultExpr e) {

    }

    @Override
    public void visit(ArrayAccessExpr e) {

    }

    @Override
    public void visit(ArrayLengthExpr e) {

    }

    @Override
    public void visit(MethodCallExpr e) {

    }

    @Override
    public void visit(IntegerLiteralExpr e) {

    }

    @Override
    public void visit(TrueExpr e) {

    }

    @Override
    public void visit(FalseExpr e) {

    }

    @Override
    public void visit(IdentifierExpr e) {

    }

    @Override
    public void visit(ThisExpr e) {

    }

    @Override
    public void visit(NewIntArrayExpr e) {

    }

    @Override
    public void visit(NewObjectExpr e) {

    }

    @Override
    public void visit(NotExpr e) {

    }

    @Override
    public void visit(IntAstType t) {
currVarSpace=currVarSpace+4;
    }

    @Override
    public void visit(BoolAstType t) {
        currVarSpace=currVarSpace+1;

    }

    @Override
    public void visit(IntArrayAstType t) {
        currVarSpace=currVarSpace+8;

    }

    @Override
    public void visit(RefType t) {
        currVarSpace=currVarSpace+8;

    }
}
