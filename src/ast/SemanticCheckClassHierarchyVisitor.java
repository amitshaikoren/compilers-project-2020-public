package ast;

import java.util.*;

public class SemanticCheckClassHierarchyVisitor implements Visitor{

    Map<String, Set<String>> childrenMap = new HashMap<>();
    Map<String, Set<String>> fathersMap = new HashMap<>();


    private void PutClass(String className, String superClass){
        childrenMap.put(className, new HashSet<>());
        fathersMap.put(className, new HashSet<>());
        if(superClass != null){
            childrenMap.get(superClass).add(className);
            for(var classVar : fathersMap.get(superClass)){
                childrenMap.get(classVar).add(className);
            }
            fathersMap.get(className).add(superClass);
            for(var classVar : fathersMap.get(superClass)){
                fathersMap.get(className).add(classVar);
            }
        }
    }

    public Map<String, Set<String>> getFathersMap(){
        return fathersMap;
    }

    public Map<String, Set<String>> getChildrenMap(){
        return childrenMap;
    }

    @Override
    public void visit(Program program) {

        for (ClassDecl classdecl : program.classDecls()) {
            PutClass(classdecl.name(),classdecl.superName());
        }
    }

    @Override
    public void visit(ClassDecl classDecl) {

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

    }

    @Override
    public void visit(BoolAstType t) {

    }

    @Override
    public void visit(IntArrayAstType t) {

    }

    @Override
    public void visit(RefType t) {

    }
}
