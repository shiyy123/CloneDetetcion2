package clonedetection;

import java.util.regex.Pattern;

/**
 * Created by Cary on 19-1-10
 * Email: yangyangshi@smail.nju.edu.cn
 */
public class Config {
    public static String basePath = "/home/cary/Documents/Data/CloneData/";
    public static Pattern pattern=Pattern.compile("\\{childNum:(.*)\"CallExpression\"}");
}
