package clonedetection;

public class Main {
    public static void processCode() {
        CodeRepresentation codeRepresentation = new CodeRepresentation();
        codeRepresentation.processCode();
    }

    public static void main(String[] args) {
        Tool tool = new Tool();
//        tool.processFDG();

        //TODO edge文件夹可能是有问题的（计算embedding时，报了不少错），需要重新跑一次；相应的embedding_func_HOPE最好也重新跑一次
        tool.generateEdge();

//        tool.generateFuncCall();


    }
}
