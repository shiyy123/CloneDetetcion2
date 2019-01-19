package clonedetection;

public class Main {
    public static void processCode() {
        CodeRepresentation codeRepresentation = new CodeRepresentation();
        codeRepresentation.processCode();
    }

    /**
     * 80/1376
     * @param args
     */
    public static void main(String[] args) {
        Tool tool = new Tool();
//        tool.generateASTLeaves();
        tool.mergeIdent();
    }
}
