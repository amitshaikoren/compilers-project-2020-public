package ast;

import java.util.HashMap;
import java.util.Map;

public class LookupTable {
    private Map<AstNode, SymbolTable> lookupTable = new HashMap<>();

    public void updateLookupTable(AstNode astNode, SymbolTable symbolTable){
        lookupTable.put(astNode, symbolTable);
    }
}
