package ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SemanticCheckClassHierarchy {
    private Set<SemanticCheckClass> roots;

    public SemanticCheckClass getClass(String classA) {
        for (var root : roots) {
            SemanticCheckClass potentialClass = root.getClass(classA);
            if(potentialClass != null){
                return potentialClass;
            }
        }
        return null;
    }

    public List<String> findFathers(String classA) {
        List<String> fatherList = new ArrayList<>();
        SemanticCheckClass classNode = getClass(classA);
        while(classNode.getSuperClass() != null){
            classNode = classNode.getSuperClass();
            fatherList.add(classNode.getName());
        }
        return fatherList;
    }
}

