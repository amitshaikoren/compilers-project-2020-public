package ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SemanticClassAndVarCheckVisitor implements Visitor{

    //STATE VARIABLES
    private String mainClassName;
    private Set<String> classes = new HashSet<>();
    private Set<String> allClasses = new HashSet<>();
    private boolean setClasses=false;
    private Map<String, Set<String>> classFields = new HashMap<>();

    private boolean updatingClassFields;
    private boolean updatingMethodFields;

    private String currClassCheck;

    public static void RaiseError(){};

    @Override
    public void visit(Program program) {

        //Get main class name
        program.mainClass().accept(this);
        setClasses=true;
        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
        setClasses=false;

        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
    }

    @Override
    public void visit(ClassDecl classDecl) {
        if(setClasses)
        {
            allClasses.add(classDecl.name());
            return;
        }

        currClassCheck = classDecl.name();
        String superClassName = classDecl.superName();


        // Making sure there are no cycles in the inheritance graphs. (1)
        // Making sure no one extends from Main class. (2)
        if(!classes.contains(superClassName) && superClassName != null){
            RaiseError();
        }

        // Making sure no two classes are named the same. (3)
        if(classes.contains(currClassCheck) && !(mainClassName.equals(currClassCheck))){
            RaiseError();
        }

        classes.add(currClassCheck);

        if(superClassName == null) {
            classFields.put(currClassCheck, new HashSet<>());
        }

        // class extends, and we take its set of field names.
        else{
            classFields.put(currClassCheck, classFields.get(superClassName));
        }

        updatingClassFields = true;
        updatingMethodFields = false;

        for(var fieldDecl : classDecl.fields()){
            fieldDecl.accept(this);
        }

        for (var methodDecl : classDecl.methoddecls()) {
            methodDecl.accept(this);
        }

    }

    @Override
    public void visit(MainClass mainClass) {
        this.mainClassName = mainClass.name();
    }

    @Override
    public void visit(MethodDecl methodDecl) {

        methodDecl.returnType().accept(this);

        for (var formal : methodDecl.formals()) {
            formal.accept(this);
        }

        for (var varDecl : methodDecl.vardecls()) {
            varDecl.accept(this);
        }

        for (var stmt : methodDecl.body()) {
            stmt.accept(this);
        }
    }

    @Override
    public void visit(FormalArg formalArg) {
        formalArg.type().accept(this);//(8)
    }

    @Override
    public void visit(VarDecl varDecl) {
        if(updatingClassFields){
            Set classFieldSet = classFields.get(currClassCheck);

            // Check no two fields with the same name. (4)
            if(classFieldSet.contains(varDecl.name())){
                RaiseError();
            }

            classFieldSet.add(varDecl.name());

        }
        varDecl.type().accept(this);//(8)
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
        assignStatement.rv().accept(this);//(9)
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
        if(!allClasses.contains(e.classId()))
        {
            //new A() is invoked for a class A that is defined somewhere in the file
            //(either before or after the same class, or to the same class itself) (9)
            RaiseError();
        }
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
        if(!allClasses.contains(t.id()))
        {
            //A type declaration of a reference type of A refers to classes that are defined somewhere in the file
            // (either before or after the same class, or to the same class itself).(8)
            RaiseError();
        }
    }
}
