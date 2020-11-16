import ast.*;

import java.io.*;


public class Main {

    public static ClassDecl searchRootDeclOfMethod(MethodDecl method, LookupTable lookupTable){
        SymbolTable symbolTable = lookupTable.getSymbolTable(method) ;
        SymbolTable fatherSymoblTable = symbolTable.getFatherSymbolTable();

        while(fatherSymoblTable != null){
            if (fatherSymoblTable.getSymbolinfo(method.name(), true) != null){
                symbolTable = fatherSymoblTable;
            }
            fatherSymoblTable = fatherSymoblTable.getFatherSymbolTable();
        }

        return (ClassDecl) lookupTable.getClassDeclName(symbolTable.getNameOfClass());
    }

    public static void main(String[] args) {
        try {
            var inputMethod = args[0];
            var action = args[1];
            var filename = args[args.length - 2];
            var outfilename = args[args.length - 1];

            Program prog;

            if (inputMethod.equals("parse")) {
                throw new UnsupportedOperationException("TODO - Ex. 4");
            } else if (inputMethod.equals("unmarshal")) {
                AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                prog = xmlSerializer.deserialize(new File(filename));
            } else {
                throw new UnsupportedOperationException("unknown input method " + inputMethod);
            }

            var outFile = new PrintWriter(outfilename);
            try {

                if (action.equals("marshal")) {
                    AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                    xmlSerializer.serialize(prog, outfilename);
                } else if (action.equals("print")) {
                    AstPrintVisitor astPrinter = new AstPrintVisitor();
                    astPrinter.visit(prog);
                    outFile.write(astPrinter.getString());

                } else if (action.equals("semantic")) {
                    throw new UnsupportedOperationException("TODO - Ex. 3");

                } else if (action.equals("compile")) {
                    throw new UnsupportedOperationException("TODO - Ex. 2");

                } else if (action.equals("rename")) {
                    var type = args[2];
                    var originalName = args[3];
                    var originalLine = args[4];
                    var newName = args[5];

                    boolean isMethod;
                    if (type.equals("var")) {
                        isMethod = false;
                    } else if (type.equals("method")) {
                        isMethod = true;
                    } else {
                        throw new IllegalArgumentException("unknown rename type " + type);
                    }

                    LookupTable lookupTable = new LookupTable();

                    AstCreateSymbolTableVisitor symbolTableVistor  = new AstCreateSymbolTableVisitor(lookupTable);
                    symbolTableVistor.visit(prog);

                    SymbolTable symbolTableOfOriginalName;

                    for(var astNode : lookupTable.getLookupTable().keySet()) {
                        if (astNode.lineNumber.equals(Integer.valueOf(originalLine))) {
                            symbolTableOfOriginalName = lookupTable.getSymbolTable(astNode);
                            break;
                        }
                    }

                    AstRenamingVisitor renamingVisitor = new AstRenamingVisitor(originalName, newName, lookupTable, symbolTableOfOriginalName, isMethod);



                    renamingVisitor.visit(prog);


                            /*
                            if (astNode instanceof MethodDecl){
                                ClassDecl astNodeOfOriginalLineNumber = searchRootDeclOfMethod((MethodDecl)astNode,lookupTable);
                                renamingVisitor.visit(astNodeOfOriginalLineNumber);
                                break;

                            }
                            if (astNode instanceof FormalArg){
                                FormalArg astNodeOfOriginalLineNumber = (FormalArg)astNode;
                                renamingVisitor.visit(astNodeOfOriginalLineNumber);
                                break;
                            }

                           if  (astNode instanceof  VarDecl){
                               VarDecl astNodeOfOriginalLineNumber = (VarDecl)astNode;
                               renamingVisitor.visit(astNodeOfOriginalLineNumber);
                               break;

                           }
                            break;
                             */


                } else {
                    throw new IllegalArgumentException("unknown command line action " + action);
                }
            } finally {
                outFile.flush();
                outFile.close();
            }

        } catch (FileNotFoundException e) {
            System.out.println("Error reading file: " + e);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("General error: " + e);
            e.printStackTrace();
        }
    }
}
