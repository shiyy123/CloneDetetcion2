package clonedetection;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {

        String baseDir = "/home/cary/Documents/Data/CloneData/edge/";
        List<String> dots = new ArrayList<>();
        dots.add("93/927593.edgelist");
        dots.add("93/929726.edgelist");
        dots.add("122/6176678.edgelist");
        dots.add("37/8896765.edgelist");
        dots.add("13/10264090.edgelist");
        dots.add("20/9116989.edgelist");
        dots.add("20/9138493.edgelist");
        dots.add("1/4609816.edgelist");
        dots.add("1/4619877.edgelist");
        dots.add("58/5352572.edgelist");
        dots.add("100/441162.edgelist");
        dots.add("31/5757344.edgelist");
        dots.add("57/5331073.edgelist");
        dots.add("21/9246614.edgelist");
        dots.add("21/9270508.edgelist");

        String func = "/home/cary/Documents/Data/CloneData/cfg/func.txt";
        try {
            List<String> lines = FileUtils.readLines(new File(func), "utf-8");
            for (String dot : dots) {
                String s = dot.substring(dot.indexOf("/") + 1, dot.indexOf("."));
                for (String line : lines) {
                    if(line.contains(s)) {
                        System.out.println(line);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
