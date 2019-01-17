package clonedetection;

public class Main {
    public static void processCode() {
        CodeRepresentation codeRepresentation = new CodeRepresentation();
        codeRepresentation.processCode();
    }

    public static void main(String[] args) {
        Tool tool = new Tool();
        tool.generateAST();
    }
}
