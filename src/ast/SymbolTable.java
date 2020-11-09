package ast;

import java.util.Map;

public class SymbolTable {
    private SymbolInfo currSymbolInfo;
    private String currSymbolName;
    private Map<String, SymbolInfo> entries;
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

    public void setSymbolInfoType(String type){
        this.currSymbolInfo.setType(type);
    }

    public void setSymbolInfoDecl(String decl){
        this.currSymbolInfo.setDecl(decl);
    }

}


