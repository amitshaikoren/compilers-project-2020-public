package ast;

public class ExprTranslation {
   private ExprTranslation father;
    private ExprTranslation e1;
    private ExprTranslation e2;
    private String result;

    public ExprTranslation getFather() {
        return father;
    }

    public void setFather(ExprTranslation father) {
        this.father = father;
    }

    public ExprTranslation getE1() {
        return e1;
    }

    public void setE1(ExprTranslation e1) {
        this.e1 = e1;
    }

    public ExprTranslation getE2() {
        return e2;
    }

    public void setE2(ExprTranslation e2) {
        this.e2 = e2;
    }



    public ExprTranslation(ExprTranslation father, ExprTranslation e1, ExprTranslation e2, String result) {
        this.father = father;
        this.e1 = e1;
        this.e2 = e2;
        this.result = result;
    }


    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }



 /*   String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
*/
}
