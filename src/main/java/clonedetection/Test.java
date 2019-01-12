package clonedetection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    public static void main(String[] args) {
        String code = "findx \\( l , \\( \\( r + l \\) / 2 \\) - 1 , key \\)";
        String all = "return findx ( l , ( ( r + l ) / 2 ) - 1 , key ) ;\n";
        Pattern pattern = Pattern.compile(code);
        Matcher matcher = pattern.matcher(all);
        while (matcher.find()) {
            System.out.println(matcher.start());
            System.out.println(matcher.group());
        }
    }
}
