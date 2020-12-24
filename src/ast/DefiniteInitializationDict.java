package ast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefiniteInitializationDict {

    private DefiniteInitializationDict outerBlock;
    private Map<String, Boolean> definiteInitializationMap = new HashMap<>();
    private DefiniteInitializationDict ifSplitDict ;
    private DefiniteInitializationDict elseSplitDict ;
    private DefiniteInitializationDict whileSplitDict ;


    public DefiniteInitializationDict(Map<String, Boolean> definiteInitializationMap, DefiniteInitializationDict outerBlock){
        this.definiteInitializationMap = definiteInitializationMap;
        this.outerBlock = outerBlock;
    }

    public DefiniteInitializationDict(){

    }

    public void AddVar(String varName, Boolean isInitialized){
        definiteInitializationMap.put(varName, isInitialized);
    }

    public void ChangeVarState(String varName, Boolean changeTo){
        definiteInitializationMap.remove(varName);
        definiteInitializationMap.put(varName, changeTo);
    }

    public void IfSplit(){
        Map<String, Boolean> daddyMap1 = new HashMap<>();
        daddyMap1.putAll(definiteInitializationMap);
        Map<String, Boolean> daddyMap2 = new HashMap<>();
        daddyMap2.putAll(definiteInitializationMap);

        ifSplitDict = new DefiniteInitializationDict(daddyMap1, this);
        elseSplitDict = new DefiniteInitializationDict(daddyMap2, this);
    }

    public void WhileSplit(){
        Map<String, Boolean> daddyMap1 = new HashMap<>();
        daddyMap1.putAll(definiteInitializationMap);
        whileSplitDict = new DefiniteInitializationDict(daddyMap1, this);
    }

    public boolean isEmpty(){
        return definiteInitializationMap.isEmpty();
    }

    public boolean get(String varName){
        return definiteInitializationMap.get(varName);
    }

    public DefiniteInitializationDict getOuterBlock(){
        return outerBlock;
    }

    public DefiniteInitializationDict getIfBlock(){
        return ifSplitDict;
    }

    public DefiniteInitializationDict getElseBlock(){
        return elseSplitDict;
    }

    public DefiniteInitializationDict getWhileSplitDict(){
        return whileSplitDict;
    }

    //To be executed when exiting an IF/Else clause
    public void IfElseUnion(){
        if(!ifSplitDict.isEmpty()){

            for(String varName : ifSplitDict.definiteInitializationMap.keySet()){
                this.definiteInitializationMap.remove(varName);
                Boolean ifBool = ifSplitDict.get(varName);
                Boolean elseBool = elseSplitDict.get(varName);
                Boolean newBool = elseBool.booleanValue() && ifBool.booleanValue();
                this.definiteInitializationMap.put(varName, newBool);
            }
        }
    }

}
