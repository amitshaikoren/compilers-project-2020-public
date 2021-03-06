package ast;

import java.io.PrintWriter;
import java.util.*;

public class SemanticClassAndVarCheckVisitor implements Visitor{

    Map<String,Set<String>> childrenHierarchyMap;
    Map<String,Set<String>> fathersHierarchyMap;
    Map<String, ArrayList<MethodOfClass>> methodOfClasses;

    //STATE VARIABLES
    private String mainClassName;
    private Set<String> classes ;
    private Set<String> allClasses ;
    private boolean setClasses;
    private Map<String, Set<String>> classFields ;
    private SymbolTable currSymbolTable;
    private LookupTable lookupTable;


    private String callMethod;

    private boolean updatingClassFields;
    private boolean updatingMethodFields;
    private boolean methodCallExpr;
    private boolean arraylengthexp;
    private  boolean systemOutCheck;
    private boolean arrayAccessCheck;
    private boolean arrayIndexCheck;
    private  boolean arrayAssignmentindexCheck;
    private boolean arrayAssignmentrvCheck;
    private boolean arrayLengthCheck;
    private String arrayLengthType="";
    private  String arrayAssignmentindex="";
    private String arrayAssignmentrv="";
    private PrintWriter outfile;

    private  String arrayAccessType="";
    private  String arrayIndexType="";

    private String systemOutType="";

    private boolean rvTypeCheck;
    private String rvType="";

    private ExprTranslation currExpr;

    private boolean formalsRedeclarationCheck;
    private Set<String> currFormals = new HashSet<>();
    private boolean localsRedeclarationCheck;
    private Set<String> currLocals = new HashSet<>();




    private String currClassCheck;

    public SemanticClassAndVarCheckVisitor(LookupTable lookupTable, Map<String,Set<String>> childrenHierarchyMap, Map<String,Set<String>> fathersHierarchyMap,  Map<String, ArrayList<MethodOfClass>> methodOfClasses,PrintWriter outfile){
        this.classes = new HashSet<>();
        this.allClasses = new HashSet<>();
        this.classFields = new HashMap<>();
        this.setClasses=false;
        this.lookupTable=lookupTable;
        this.callMethod=null;
        this.childrenHierarchyMap = childrenHierarchyMap;
        this.fathersHierarchyMap = fathersHierarchyMap;
        this.methodOfClasses= methodOfClasses;
        this.outfile = outfile;
    }
    public void RaiseError(){
        throw new RuntimeException();


    };
    public Set<String> getFathers(String className){
        return fathersHierarchyMap.get(className);
    }

    public Set<String> getChildren(String className){
        return childrenHierarchyMap.get(className);
    }

    private SymbolTable getSTnameResolution(SymbolTable symbolTableOfDecl,String name)
    {    if(symbolTableOfDecl==null){
        RaiseError();
    }
        if(symbolTableOfDecl.isInVarEntries(name))
        {
            return symbolTableOfDecl;
        }
        SymbolTable fatherSymbolTable=symbolTableOfDecl.getFatherSymbolTable();
        while (fatherSymbolTable!=null)
        {
            if(fatherSymbolTable.isInVarEntries(name))
            {
                return fatherSymbolTable;
            }
            fatherSymbolTable=fatherSymbolTable.getFatherSymbolTable();
        }
        return null;//error
    }

    //Returns type of reference identifier
    private String findType(IdentifierExpr e)
    {
        if(currSymbolTable==null){
        RaiseError();
    }
        SymbolTable stOfDecl=getSTnameResolution(currSymbolTable,e.id());
        if (stOfDecl==null){ //(14)
            RaiseError();
        }
        return stOfDecl.getSymbolinfo(e.id(), false).getRefType();
    }

    private String findType(String id)
    {

        if(currSymbolTable==null){
            RaiseError();
        }
        SymbolTable stOfDecl=getSTnameResolution(currSymbolTable, id);
        if (stOfDecl==null){ //(14)
            RaiseError();
        }
        return stOfDecl.getSymbolinfo(id, false).getRefType();
    }

    //Returns type of reference method
    private String findType(MethodCallExpr e)
    {

        if(currSymbolTable==null){
            RaiseError();
        }
        SymbolTable stOfDecl=getSTnameResolution(currSymbolTable,e.methodId());
        if (stOfDecl==null){ //(14)
            RaiseError();
        }
        return stOfDecl.getSymbolinfo(e.methodId(), true).getRefType();
    }



    @Override
    public void visit(Program program)  {
        this.mainClassName = program.mainClass().name();

        //Get main class name
        setClasses=true;
        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
        setClasses=false;

        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
        program.mainClass().accept(this);

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
        if(classes.contains(currClassCheck) || (mainClassName.equals(currClassCheck))){
            RaiseError();
        }

        classes.add(currClassCheck);

        if(superClassName == null) {
            classFields.put(currClassCheck, new HashSet<>());
        }

        // class extends, and we take its set of field names.
        else{
            Set<String> copyofset = new HashSet<>();
            copyofset.addAll(classFields.get(superClassName));
            classFields.put(currClassCheck,copyofset );
        }

        updatingClassFields = true;
        updatingMethodFields = false;

        for(var fieldDecl : classDecl.fields()){
            this.currSymbolTable=lookupTable.getSymbolTable(fieldDecl);
            fieldDecl.accept(this);
        }
        updatingClassFields = false;

        for (var methodDecl : classDecl.methoddecls()) {
            this.currSymbolTable=lookupTable.getSymbolTable(methodDecl);
            methodDecl.accept(this);
            currLocals.clear();
            currFormals.clear();
        }

    }

    @Override
    public void visit(MainClass mainClass) {
        currClassCheck = mainClassName;
        mainClass.mainStatement().accept(this);
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        methodDecl.returnType().accept(this);

        for (var formal : methodDecl.formals()) {
            this.currSymbolTable=lookupTable.getSymbolTable(formal);
            formalsRedeclarationCheck = true;
            formal.accept(this);
            formalsRedeclarationCheck = false;
        }

        for (var varDecl : methodDecl.vardecls()) {
            this.currSymbolTable=lookupTable.getSymbolTable(varDecl);
            localsRedeclarationCheck = true;
            varDecl.accept(this);
            localsRedeclarationCheck = false;
        }

        for (var stmt : methodDecl.body()) {
            stmt.accept(this);
        }
        methodDecl.ret().accept(this);

    }

    @Override
    public void visit(FormalArg formalArg) {
        formalArg.type().accept(this);//(8)

        if(formalsRedeclarationCheck) {//(24)
            if (!currFormals.contains(formalArg.name()) ) {
                currFormals.add(formalArg.name());
            }
            else{
                RaiseError();
            }
        }
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
        if(localsRedeclarationCheck) {//(24)
            if (!currLocals.contains(varDecl.name())) {
                currLocals.add(varDecl.name());
            } else {
                RaiseError();
            }
        }

            if (currFormals.contains(varDecl.name())){
                RaiseError();
            }


        varDecl.type().accept(this);//(8)
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        for (var stmt : blockStatement.statements()) {
            stmt.accept(this);
        }
    }

    @Override
    public void visit(IfStatement ifStatement) {
        currExpr=null;
        //In if and while, the condition is boolean.(17)

        ifStatement.cond().accept(this);
        if (!currExpr.getResult().equals("bool"))
        {
            RaiseError();
        }
        currExpr=null;
        ifStatement.elsecase().accept(this);
        ifStatement.thencase().accept(this);
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        currExpr=null;
        //In if and while, the condition is boolean.(17)
        whileStatement.cond().accept(this);
        if (!currExpr.getResult().equals("bool"))
        {
            RaiseError();
        }
        currExpr=null;
        whileStatement.body().accept(this);
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        systemOutCheck=true;
        systemOutType="";
        sysoutStatement.arg().accept(this);
        systemOutCheck=false;
        if (!systemOutType.equals("int")){
            //The argument to System.out.println is of type int (20)
        RaiseError();
        }
    }

    @Override
    public void visit(AssignStatement assignStatement) { //(16)
        rvTypeCheck = true;
        assignStatement.rv().accept(this);//(9)
        rvTypeCheck = false;

        if ( getSTnameResolution(currSymbolTable, assignStatement.lv())==null){
            //A reference in an expression to a variable  is to a local variable or formal parameter defined in the current method,
            // or to a field defined in the current class or its superclasses. //(14)
            RaiseError();
        }

        String lvType = findType(assignStatement.lv());

        if(     lvType.equals("int") && !rvType.equals("int")||

                lvType.equals("bool") && !rvType.equals("bool")){

                RaiseError();
        }
        if(     (!lvType.equals("int")&&!lvType.equals("intArr")) && rvType.equals("int")||
                !lvType.equals("intArr") && rvType.equals("intArr")||
                !lvType.equals("bool") && rvType.equals("bool")){

            RaiseError();
        }
    if(!lvType.equals(rvType) && !(lvType.equals("intArr") && rvType.equals("int"))){
            if(!getFathers(rvType).contains(lvType)){
                RaiseError();
            }
        }

    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        //In an assignment to an array x[e1] = e2, x is int[], e1 is an int and also e2 is an int.(23)
        if (getSTnameResolution(currSymbolTable,assignArrayStatement.lv())==null){ //(14)
            RaiseError();
        }
        String type = findType(assignArrayStatement.lv());
        if(!type.equals("intArr")){//(23)
            RaiseError();
        }
        arrayAssignmentindexCheck=true;
        assignArrayStatement.index().accept(this);
        arrayAssignmentindexCheck=false;
        arrayAssignmentrvCheck=true;
        assignArrayStatement.rv().accept(this);
        arrayAssignmentrvCheck=false;
        if(!arrayAssignmentindex.equals("int")){
            RaiseError();
        }
        if (!arrayAssignmentrv.equals("int")){
            RaiseError();
        }

    }

    private void visitBinaryExpr(BinaryExpr e) {
        ExprTranslation exp,e1,e2;
        exp=new ExprTranslation(null,null,null,null);
        currExpr=exp;
        e.e1().accept(this);
        exp.setE1(currExpr);
        currExpr=exp;
        e.e2().accept(this);
        exp.setE2(currExpr);
        currExpr=exp;
        if(currExpr.getE2().getResult()==null || currExpr.getE1().getResult()==null){
            RaiseError();
        }

    }

    @Override
    public void visit(AndExpr e) {
        visitBinaryExpr(e);
        if(rvTypeCheck){
            rvType = "bool";
        }
            if (!currExpr.getE1().getResult().equals("bool")||
                    !currExpr.getE2().getResult().equals("bool")){ ///(21)
                RaiseError();
            }

        else{
            currExpr.setResult("bool"); //(17)
        }
        if(arrayIndexCheck){ //(22)
            arrayIndexType="bool";
        }
        if (arrayAccessCheck){ //(22)
            arrayAccessType="bool";
        }
        if(arrayAssignmentrvCheck){ //(23)
            arrayAssignmentrv="bool";
        }
        if(arrayAssignmentindexCheck){ //(23)
            arrayAssignmentindex="bool";
        }
        if(arrayLengthCheck){ //(25)
            arrayLengthType="bool";
        }
    }

    @Override
    public void visit(LtExpr e) {
        visitBinaryExpr(e);
        if(rvTypeCheck){
            rvType = "bool";
        }
            if (!currExpr.getE1().getResult().equals("int")||
                    !currExpr.getE2().getResult().equals("int")){ //(21)
                RaiseError();
            }
            else{
                currExpr.setResult("bool"); //(17)
            }
        if(arrayIndexCheck){ //(22)
            arrayIndexType="bool";
        }
        if (arrayAccessCheck){ //(22)
            arrayAccessType="bool";
        }
        if(arrayAssignmentrvCheck){ //(23)
            arrayAssignmentrv="bool";
        }
        if(arrayAssignmentindexCheck){ //(23)
            arrayAssignmentindex="bool";
        }
        if(arrayLengthCheck){ //(25)
            arrayLengthType="bool";
        }

    }

    @Override
    public void visit(AddExpr e) {
        visitBinaryExpr(e);
        if(rvTypeCheck){
            rvType = "int";
        }
            if (!currExpr.getE1().getResult().equals("int")||
                    !currExpr.getE2().getResult().equals("int")){ //(21)
                RaiseError();
            }
            else {
                currExpr.setResult("int"); //(17)
            }
        if(systemOutCheck) //(20)
        {
            systemOutType="int";
        }
        if(arrayIndexCheck){ //(22)
            arrayIndexType="int";
        }
        if (arrayAccessCheck){ //(22)
            arrayAccessType="int";
        }
        if(arrayAssignmentrvCheck){ //(23)
            arrayAssignmentrv="int";
        }
        if(arrayAssignmentindexCheck){ //(23)
            arrayAssignmentindex="int";
        }
        if(arrayLengthCheck){ //(25)
            arrayLengthType="int";
        }


    }

    @Override
    public void visit(SubtractExpr e) {
        visitBinaryExpr(e);
        if(rvTypeCheck){
            rvType = "int";
        }
        //The arguments to the predefined operators (&&, <, !, +, -, * etc.) are of the correct type
            if (!currExpr.getE1().getResult().equals("int")|| //(21)
                    !currExpr.getE2().getResult().equals("int")){
                RaiseError();
            }
            else {
                currExpr.setResult("int"); //(17)
            }
            if(systemOutCheck) //(20)
            {
                systemOutType="int";
            }
        if(arrayIndexCheck){ //(22)
            arrayIndexType="int";
        }
        if (arrayAccessCheck){ //(22)
            arrayAccessType="int";
        }
        if(arrayAssignmentrvCheck){ //(23)
            arrayAssignmentrv="int";
        }
        if(arrayAssignmentindexCheck){ //(23)
            arrayAssignmentindex="int";
        }
        if(arrayLengthCheck){ //(25)
            arrayLengthType="int";
        }


    }

    @Override
    public void visit(MultExpr e) {
        visitBinaryExpr(e);
        if(rvTypeCheck){
            rvType = "int";
        }
            if (!currExpr.getE1().getResult().equals("int")||
                    !currExpr.getE2().getResult().equals("int")){ //(21)
                RaiseError();
            }
            else {
                currExpr.setResult("int"); //(17)
            }
        if(systemOutCheck) //(20)
        {
            systemOutType="int";
        }
        if(arrayIndexCheck){ //(22)
            arrayIndexType="int";
        }
        if (arrayAccessCheck){ //(22)
            arrayAccessType="int";
        }
        if(arrayAssignmentrvCheck){ //(23)
            arrayAssignmentrv="int";
        }
        if(arrayAssignmentindexCheck){ //(23)
            arrayAssignmentindex="int";
        }
        if(arrayLengthCheck){ //(25)
            arrayLengthType="int";
        }


    }

    @Override
    public void visit(ArrayAccessExpr e) {
        //In an array access x[e], x is int[] and e is an int.(22)
        arrayAccessCheck=true;
        e.arrayExpr().accept(this);
        arrayAccessCheck=false;
        if (!arrayAccessType.equals("intArr")) //(22)
        {
            RaiseError();
        }
        arrayIndexCheck=true;
        e.indexExpr().accept(this);
        arrayIndexCheck=false;
        if (!arrayIndexType.equals("int")){ //(22)
            RaiseError();
        }
        if(rvTypeCheck){
            rvType = "int";
        }
         //(17) + (21)
            ExprTranslation exp;

            if (currExpr == null) {
                exp = new ExprTranslation(null, null, null, "int");
            } else {
                exp = new ExprTranslation(currExpr, null, null, "int");
            }
            currExpr = exp;
        if(systemOutCheck) //(20)
        {
            systemOutType="int";
        }
        if(arrayAssignmentrvCheck){ //(23)
            arrayAssignmentrv="int";
        }
        if(arrayAssignmentindexCheck){ //(23)
            arrayAssignmentindex="int";
        }
        if(arrayLengthCheck){ //(25)
            arrayLengthType="int";
        }


    }

    @Override
    public void visit(ArrayLengthExpr e) {
        //The static type of the object on which length invoked is int[] (13)
        arraylengthexp=true;
        e.arrayExpr().accept(this);
        arraylengthexp=false;

        if(rvTypeCheck){ //(9)
            rvType = "int";
        }
        //(17) + (21)

        ExprTranslation exp;

            if (currExpr == null) {
                exp = new ExprTranslation(null, null, null, "int");
            } else {
                exp = new ExprTranslation(currExpr, null, null, "int");
            }
            currExpr = exp;
        if(systemOutCheck) //(20)
        {
            systemOutType="int";
        }
        if(arrayIndexCheck){ //(22)
            arrayIndexType="int";
        }
        if (arrayAccessCheck){ //(22)
            arrayAccessType="int";
        }
        if(arrayAssignmentrvCheck){ //(23)
            arrayAssignmentrv="int";
        }
        if(arrayAssignmentindexCheck){ //(23)
            arrayAssignmentindex="int";
        }
        if(arrayLengthCheck){ //(25)
            arrayLengthType="int";
        }


    }

    @Override
    public void visit(MethodCallExpr e) {
        //In method invocation, the static type of the object is a reference type (not int, bool, or int[]) (10)
        //method call is invoked on expression e which is either this, a new expression, or a reference to a local variable,
        // formal parameter or a field. (12)
        methodCallExpr=true;
        e.ownerExpr().accept(this);
        methodCallExpr=false;
        if (callMethod==null) // (if its int , int[] or bool we raise error earlier .
        {
            RaiseError();
        }
        if (callMethod.equals("this")){
            if (currClassCheck.equals(mainClassName)){
                RaiseError();
            }
            callMethod=currClassCheck;
        }

        if (systemOutCheck){ //(20)
            for ( var check : methodOfClasses.get(callMethod)){
                if (check.getMethodName().equals(e.methodId())){
                    systemOutType=check.getDecl();
                }
            }
        }
        if(rvTypeCheck){ //(16)
            for ( var check : methodOfClasses.get(callMethod)){
                if (check.getMethodName().equals(e.methodId())){
                    rvType=check.getDecl();
                }
            }
        }
        //(17) + (21)

        ExprTranslation exp;
            String type=null;
            for ( var check : methodOfClasses.get(callMethod)){
                if (check.getMethodName().equals(e.methodId())){
                     type=check.getDecl();
                }
            }
            if(type == null){
                RaiseError();
            }
            if (currExpr == null) {
                exp = new ExprTranslation(null, null, null, type);
            } else {
                exp = new ExprTranslation(currExpr, null, null, type);
            }
            currExpr = exp;

        if(arrayIndexCheck){ //(22)
            arrayIndexType=type;
        }
        if (arrayAccessCheck){ //(22)
            arrayAccessType=type;
        }
        if(arrayAssignmentrvCheck){ //(23)
            arrayAssignmentrv=type;
        }
        if(arrayAssignmentindexCheck){ //(23)
            arrayAssignmentindex=type;
        }
        if(arrayLengthCheck){ //(25)
            arrayLengthType=type;
        }
        if(rvTypeCheck){
            rvType=type;
        }



    }

    @Override
    public void visit(IntegerLiteralExpr e) {
        if(rvTypeCheck){
            rvType = "int";
        }

        //(17) + (21)

        ExprTranslation exp;

            if (currExpr == null) {
                exp = new ExprTranslation(null, null, null, "int");
            } else {
                exp = new ExprTranslation(currExpr, null, null, "int");
            }
            currExpr = exp;

            if(systemOutCheck){ //(20)
                systemOutType="int";
            }
        if(arrayIndexCheck){ //(22)
            arrayIndexType="int";
        }
        if (arrayAccessCheck){ //(22)
            arrayAccessType="int";
        }
        if(arrayAssignmentrvCheck){ //(23)
            arrayAssignmentrv="int";
        }
        if(arrayAssignmentindexCheck){ //(23)
            arrayAssignmentindex="int";
        }
        if(arrayLengthCheck){ //(25)
            arrayLengthType="int";
        }
    }

    @Override
    public void visit(TrueExpr e) {
        if(rvTypeCheck){
            rvType = "bool";
        }
        //(17) + (21)
            ExprTranslation exp;

            if (currExpr == null) {
                exp = new ExprTranslation(null, null, null, "bool");
            } else {
                exp = new ExprTranslation(currExpr, null, null, "bool");
            }
            currExpr = exp;
        if(arrayIndexCheck){ //(22)
            arrayIndexType="bool";
        }
        if (arrayAccessCheck){ //(22)
            arrayAccessType="bool";
        }
        if(arrayAssignmentrvCheck){ //(23)
            arrayAssignmentrv="bool";
        }
        if(arrayAssignmentindexCheck){ //(23)
            arrayAssignmentindex="bool";
        }
        if(arrayLengthCheck){ //(25)
            arrayLengthType="bool";
        }
        if(systemOutCheck){ //(20)
            systemOutType="bool";
        }

    }

    @Override
    public void visit(FalseExpr e) {
        if(rvTypeCheck){
            rvType = "bool";
        }
        //(17) + (21)

        ExprTranslation exp;

            if (currExpr == null) {
                exp = new ExprTranslation(null, null, null, "bool");
            } else {
                exp = new ExprTranslation(currExpr, null, null, "bool");
            }
            currExpr = exp;
        if(arrayIndexCheck){ //(22)
            arrayIndexType="bool";
        }
        if (arrayAccessCheck){ //(22)
            arrayAccessType="bool";
        }
        if(arrayAssignmentrvCheck){ //(23)
            arrayAssignmentrv="bool";
        }
        if(arrayAssignmentindexCheck){ //(23)
            arrayAssignmentindex="bool";
        }
        if(arrayLengthCheck){ //(25)
            arrayLengthType="bool";
        }
        if(systemOutCheck){ //(20)
            systemOutType="bool";
        }

    }

    @Override
    public void visit(IdentifierExpr e) {
        String type =findType(e);
    if(rvTypeCheck){
        rvType=type;
    }
        if (methodCallExpr){ //(10)
            if (!allClasses.contains(type)){ // check if the type is int , int [] , boolean
                RaiseError();
            }
            else{
                callMethod=type; //is legal type
            }
        }
        if (arraylengthexp && !methodCallExpr){ //(13)
            if (!type.equals("intArr")){
                RaiseError();
            }

        }
        //(17) + (21)

            ExprTranslation exp;

            if (currExpr == null) {
                exp = new ExprTranslation(null, null, null, type);
            } else {
                exp = new ExprTranslation(currExpr, null, null, type);
            }
            currExpr = exp;

        if(systemOutCheck) //(20)
        {
            if (type.equals("int")){
                systemOutType="int";

            }
        }
        if(arrayIndexCheck){ //(22)
            arrayIndexType=type;
        }
        if (arrayAccessCheck){ //(22)
            arrayAccessType=type;
        }
        if(arrayAssignmentrvCheck){ //(23)
            arrayAssignmentrv=type;
        }
        if(arrayAssignmentindexCheck){ //(23)
            arrayAssignmentindex=type;
        }
        if(arrayLengthCheck){ //(25)
            arrayLengthType=type;
        }

        }




    @Override
    public void visit(ThisExpr e) {
        if (methodCallExpr){ //(12)
            callMethod = "this"; //this is legal type for call method
        }
        if (rvTypeCheck){
            rvType=currClassCheck;
        }
        if(arrayIndexCheck){ //(22)
            arrayIndexType=currClassCheck;
        }
        if (arrayAccessCheck){ //(22)
            arrayAccessType=currClassCheck;
        }
        if(arrayAssignmentrvCheck){ //(23)
            arrayAssignmentrv=currClassCheck;
        }
        if(arrayAssignmentindexCheck){ //(23)
            arrayAssignmentindex=currClassCheck;
        }
        if(arrayLengthCheck){ //(25)
            arrayLengthType=currClassCheck;
        }

    }

    @Override
    public void visit(NewIntArrayExpr e) {
        //updated In an array allocation new int[e], e is an int. (25)

        if(arrayIndexCheck)
        {
            RaiseError();
        }
        arrayLengthCheck=true;
        e.lengthExpr().accept(this);
        arrayLengthCheck=false;
        if(arrayLengthType==null){
            RaiseError();
        }
        if(!arrayLengthType.equals("int")){
            RaiseError();
        }
        if(arrayAccessCheck){
            arrayAccessType="intArr";
        }


    }

    @Override
    public void visit(NewObjectExpr e) {
        if(!allClasses.contains(e.classId()))
        {
            //new A() is invoked for a class A that is defined somewhere in the file
            //(either before or after the same class, or to the same class itself) (9)
            RaiseError();
        }
        if (methodCallExpr){ // new object is legal for call method (12)
            callMethod = e.classId();
        }
        if (rvTypeCheck){
            rvType=e.classId();
        }
        ExprTranslation exp;

        if (currExpr == null) {
            exp = new ExprTranslation(null, null, null, e.classId());
        } else {
            exp = new ExprTranslation(currExpr, null, null, e.classId());
        }
        currExpr = exp;
    }

    @Override
    public void visit(NotExpr e) { //(17)
        ExprTranslation exp;
        exp=new ExprTranslation(null,null,null,null);
        e.e().accept(this);
        exp.setResult(currExpr.getResult());
        if (!currExpr.getResult().equals("bool")){
            RaiseError();
        }

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
