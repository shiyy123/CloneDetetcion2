package clonedetection;

import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphParser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test {
    String testBasePath = "/home/cary/Documents/Data/test";

    void generateEdge(File dot) {
        try {
            String edgePath = testBasePath.concat(File.separator).concat("edge");
            File edgeFolder = new File(edgePath);
            if (!edgeFolder.exists()) {
                edgeFolder.mkdirs();
            }

            File edge = new File(edgeFolder.getAbsolutePath().concat(File.separator.concat(dot.getName().substring(0, dot.getName().indexOf(".")).concat(".edgelist"))));

            GraphParser graphParser = new GraphParser(new FileInputStream(dot));
            Map<String, GraphEdge> edges = graphParser.getEdges();
            edges.forEach((key, val) -> {
                String s = key.replace("-", " ");
                try {
                    FileUtils.write(edge, s + "\n", "utf-8", true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    List<Double> readFromEmd(File emd) {
        List<Double> res = new ArrayList<>();
        try {
            String data = FileUtils.readFileToString(emd, "utf-8");
            System.out.println(data);
            String[] tmp = data.split(" ");
            for (String s : tmp) {
                if (!s.isEmpty()) {
                    res.add(Double.parseDouble(s));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    void calculateDistanceBetweenEmd(File emd1, File emd2) {
        List<Double> doubleList1 = readFromEmd(emd1);
        List<Double> doubleList2 = readFromEmd(emd2);

        double res = 0;
        for (int i = 0; i < doubleList1.size(); i++) {
            res += (doubleList1.get(i) - doubleList2.get(i)) * (doubleList1.get(i) - doubleList2.get(i));
        }
        System.out.println(Math.sqrt(res));
    }

    List<String> removeComment(List<String> source) {
        List<String> res = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean mode = false;
        for (String s : source) {
            for (int i = 0; i < s.length(); i++) {
                if (mode) {
                    if (s.charAt(i) == '*' && i < s.length() - 1 && s.charAt(i + 1) == '/') {
                        mode = false;
                        i++;        //skip '/' on next iteration of i
                    }
                }
                else {
                    if (s.charAt(i) == '/' && i < s.length() - 1 && s.charAt(i + 1) == '/') {
                        break;      //ignore remaining characters on line s
                    }
                    else if (s.charAt(i) == '/' && i < s.length() - 1 && s.charAt(i + 1) == '*') {
                        mode = true;
                        i++;           //skip '*' on next iteration of i
                    }
                    else    sb.append(s.charAt(i));     //not a comment
                }
            }
            if (!mode && sb.length() > 0) {
                res.add(sb.toString());
                sb = new StringBuilder();   //reset for next line of source code
            }
        }
        return res;
    }

    void removeAllComment() {
        Tool tool = new Tool();
        List<List<File>> allFiles = tool.getFile("/home/cary/Documents/Data/CloneData/src_google/ProgramData");
        allFiles.forEach(x -> x.forEach(y -> {
            try {
                List<String> content = FileUtils.readLines(y, "utf-8");
                List<String> res = removeComment(content);

                String path = "/home/cary/Documents/Data/CloneData/src_google/NoComment/" + tool.getLastTwo(y.getAbsolutePath());

                FileUtils.writeLines(new File(path), res, "\n");
//                System.out.println(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    public static void main(String[] args) {
        Test test = new Test();
        test.removeAllComment();
//        test.calculateDistanceBetweenEmd(new File("/home/cary/Documents/Data/test/cfg_emd/3246696_simplify.emd"), new File("/home/cary/Documents/Data/test/cfg_emd/3320433_simplify.emd"));
//        test.generateEdge(new File("/home/cary/Documents/Data/test/cfg/173.dot"));
    }


}
