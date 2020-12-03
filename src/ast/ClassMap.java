package ast;

import java.util.HashMap;
import java.util.Map;

public class ClassMap {
   private Map<String,String> methodMap;
   private Map<String,String> varMap;

    public ClassMap(Map<String, String> methodMap, Map<String, String> varMap) {
        this.methodMap = methodMap;
        this.varMap = varMap;
    }

    public ClassMap() {
        methodMap=new HashMap<>();
        varMap=new HashMap<>();
    }

    public Map<String, String> getMethodMap() {
        return methodMap;
    }

    public void setMethodMap(Map<String, String> methodMap) {
        this.methodMap = methodMap;
    }

    public Map<String, String> getVarMap() {
        return varMap;
    }

    public void setVarMap(Map<String, String> varMap) {
        this.varMap = varMap;
    }
}
