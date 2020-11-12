package ast;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private SymbolInfo currSymbolInfo;
    private String currSymbolName;
    private Map<String, SymbolInfo> entries = new HashMap<>();
    private SymbolTable parentSymbolTable;

    public SymbolTable(SymbolTable parentSymbolTable)
    {
        this.parentSymbolTable=parentSymbolTable;
    }

    public void updateEntries(){
        entries.put(this.currSymbolName, this.currSymbolInfo);
    }

    public void buildSymbolInfo() {
        this.currSymbolInfo = new SymbolInfo();
    }

    public void setCurrSymbolName(String symbolName){
        this.currSymbolName = symbolName;
    }

    //SYMBOL INFO UPDATE METHODS

    public void setSymbolInfoIsMethod(boolean isMethod){
        this.currSymbolInfo.setIsMethod(isMethod);
    }

    public void setSymbolInfoDecl(String decl){
        this.currSymbolInfo.setDecl(decl);
    }

}


