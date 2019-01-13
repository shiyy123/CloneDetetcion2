package clonedetection;

public class Test {

    public static void main(String[] args) {
        String s = "z = ( year - 1 ) + ( ( year - 1 ) / 4 ) - ( ( year - 1 ) / 100 ) + ( ( year - 1 ) / 400 ) + DiJiTian ( year , month , day )\\\n" +
                "sss\\n";
        System.out.println(s);
        Tool tool=new Tool();
        System.out.println(tool.toOneLine(s));
    }
}
