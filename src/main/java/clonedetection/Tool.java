package clonedetection;

import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Created by Cary on 19-1-10
 * Email: yangyangshi@smail.nju.edu.cn
 */
public class Tool {
    public void RefactorDot() {
        try {
            List<String> funcList = FileUtils.readLines(new File(Config.basePath + "func.txt"), "utf-8");
            for (int i = 0; i < funcList.size(); i++) {
                String[] items = funcList.get(i).split("\t");
                String name = items[1];
                String path = items[2];

                String oldDotPath = Config.basePath + "embedding/" + (i / 500) + "/" + name + ".txt";

                String[] folders = path.split("/");

                String newDotDirPath = Config.basePath + "temp/" + folders[folders.length - 2] + "/" + folders[folders.length - 1].substring(0, folders[folders.length - 1].indexOf("."));

                if (!new File(newDotDirPath).exists()) {
                    new File(newDotDirPath).mkdirs();
                }

                FileUtils.copyFile(new File(oldDotPath), new File(newDotDirPath + "/" + name + ".txt"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkNum() {
        for (int i = 1; i < 105; i++) {
            String path = Config.basePath + "edge/" + i;
            if (new File(path).listFiles().length != 500) {
                System.out.println(path);
                System.out.println(new File(path).listFiles().length);
            }
        }
    }

    public void checkEqual() {

        for (int i = 1; i <= 90; i++) {
            if (i == 56 || i == 64) {
                continue;
            }
            File[] files1 = new File(Config.basePath + "dot/" + i).listFiles();
            File[] files2 = new File(Config.basePath + "edge/" + i).listFiles();
            Set<String> set = new HashSet<>();
            for (File file1 : files1) {
                set.add(file1.getName());
            }
            for (File file2 : files2) {
                if (!set.contains(file2.getName())) {
                    System.out.println(file2.getAbsolutePath());
                }
            }
        }
    }

    public List<List<File>> getFile(String path) {
        List<List<File>> res = new ArrayList<>();
        File[] files = new File(path).listFiles();

        assert files != null;
        for (File f : files) {
            File[] ffs = f.listFiles();
            assert ffs != null;
            List<File> tmp = new ArrayList<>(Arrays.asList(ffs));
            res.add(tmp);
        }
        return res;
    }

    public void findMissing() {
        for (int i = 1; i < 105; i++) {
            Set<String> set = new HashSet<>();

            String path = Config.basePath + "src/" + i;
            File[] files = new File(path).listFiles();
            for (File file : files) {
                set.add(file.getName().substring(0, file.getName().indexOf(".")));
            }

            String dotPath = Config.basePath + "dot/" + i;
            File[] files1 = new File(dotPath).listFiles();
            for (File file : files1) {
                if (!set.contains(file.getName())) {
                    System.out.println(file.getAbsolutePath());
                }
            }
        }
    }

    public void generateFunc() {
        try {
            Set<String> set = new HashSet<>();
            List<List<File>> files = getFile(Config.basePath + "dot");
            for (List<File> tmp : files) {
                for (File f : tmp) {
                    File[] ffs = f.listFiles();
                    for (File ff : ffs) {
                        set.add(ff.getName().substring(0, ff.getName().indexOf(".")));
                    }
                }
            }

            List<String> oldFuncList = FileUtils.readLines(new File(Config.basePath + "func_old.txt"), "utf-8");
            List<String> res = new ArrayList<>();
            for (String oldFunc : oldFuncList) {
                if (set.contains(oldFunc.split("\t")[1])) {
                    res.add(oldFunc);
                }
            }
            FileUtils.writeLines(new File(Config.basePath + "func.txt"), res, "\n");
            System.out.println(res.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void processFDG() {
        Set<String> set = new HashSet<>();
        File[] files = new File(Config.basePath + "dot").listFiles();

        for (File file : files) {
            set.add(file.getName());
        }

        File[] file2s = new File(Config.basePath + "FDG").listFiles();

        for (File file2 : file2s) {
            if (!set.contains(file2.getName())) {
                try {
                    FileUtils.deleteDirectory(file2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void findFuncOverload() {
        List<List<File>> files = getFile(Config.basePath + "FDG");
        for (List<File> folder : files) {
            for (File file : folder) {
                File func = new File(file.getAbsolutePath() + File.separator + "func.txt");
                try {
                    List<String> lines = FileUtils.readLines(func, "utf-8");
                    Set<String> set = new HashSet<>();
                    for (String line : lines) {
                        String name = line.split("\t")[0];
                        if (set.contains(name)) {
                            System.out.println(func.getAbsolutePath());
                        } else {
                            set.add(name);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    File getSourceCode(File file) {
        for (File listFile : Objects.requireNonNull(file.listFiles())) {
            if (listFile.getName().endsWith(".c") || listFile.getName().endsWith(".cpp")) {
                return listFile;
            }
        }
        return null;
    }

    String getLastTwo(String path) {
        String[] names = path.split("/");
        return names[names.length - 2] + File.separator + names[names.length - 1];
    }

    //将func.txt文件
    void splitFuncFile() {
        String funcPath = Config.basePath + "func.txt";

        Map<String, List<String>> map = new HashMap<>();

        try {
            List<String> funcList = FileUtils.readLines(new File(funcPath), "utf-8");
            for (String func : funcList) {
                String[] tmp = func.split("\t")[2].split("/");
                String key = tmp[tmp.length - 2].concat(File.separator).concat(tmp[tmp.length - 1]);
                List<String> list = map.getOrDefault(key.substring(0, key.indexOf(".")), new ArrayList<>());
                list.add(func);
                map.put(key.substring(0, key.indexOf(".")), list);
            }

            map.forEach((key, val) -> {
                File f = new File(Config.basePath.concat("func").concat(File.separator.concat(key)));
                if (!f.exists()) {
                    f.mkdirs();
                }
                try {
                    FileUtils.writeLines(new File(f.getAbsolutePath().concat(File.separator.
                            concat("func.txt"))), val, "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void checkUniqueFuncCall() {

        List<List<File>> files = getFile(Config.basePath + "FDG");
        for (List<File> folder : files) {
            for (File file : folder) {
                String name = getLastTwo(file.getAbsolutePath());

                //函数id和name的映射，单独处理的，id不是全局唯一
                Map<String, String> id2name = new HashMap<>();

                //函数name和id的映射
                Map<String, String> name2id = new HashMap<>();
                try {
                    List<String> funcList = FileUtils.readLines(new File(file.getAbsolutePath().concat(File.separator.concat("func.txt"))), "utf-8");
                    for (String func : funcList) {
                        id2name.put(func.split("\t")[1], func.split("\t")[0]);
                    }

                    List<String> funcList2 = FileUtils.readLines(new File(Config.basePath.concat("func".concat(File.separator).concat(name).concat(File.separator.concat("func.txt")))), "utf-8");
                    funcList2.forEach(item -> {
                        String[] tmp = item.split("\t");
                        name2id.put(tmp[0], tmp[1]);
                    });

                    File[] callFiles = new File(file.getAbsolutePath().concat(File.separator.concat("call"))).listFiles();
                    //文件名为被调用的函数id
                    for (File beCalledFile : callFiles) {
                        List<String> callLineList = FileUtils.readLines(beCalledFile, "utf-8");

                        //获得被调用函数名（因为不存在函数重载，所以函数名具有唯一性）
                        String beCalledFuncName = id2name.get(beCalledFile.getName().substring(0, beCalledFile.getName().indexOf(".")));
                        for (String callLine : callLineList) {
                            Matcher matcher = Config.callPattern.matcher(callLine);
                            if (matcher.find()) {
                                String s = matcher.group(0);
                                JSONObject jsonObject = new JSONObject(s);

                                //获得发起调用的函数名
                                String callFuncName = id2name.get(jsonObject.getString("functionId"));
                                //获得具体调用实施处的代码
                                String callCode = jsonObject.getString("code");

                                String callFuncId = name2id.get(callFuncName);
                                String beCalledFuncId = name2id.get(beCalledFuncName);

                                //TODO
                                //读取相应的dot文件，根据callCode找到具体是哪个代码片段进行调用操作。
                                File dotFile = new File(Config.basePath.concat("dot").concat(File.separator.concat(name).concat(File.separator.concat(callFuncId).concat(".dot"))));
                                System.out.println(callCode);

                                GraphParser parser = new GraphParser(new FileInputStream(dotFile));
                                Map<String, GraphNode> nodes = parser.getNodes();
//                                Map<String, GraphEdge> edges = parser.getEdges();

                                int cnt = 0;
                                for (GraphNode node : nodes.values()) {
                                    Matcher codeMatcher = Config.codePatterh.matcher(node.getAttribute("label").toString());
                                    while (codeMatcher.find()) {
                                        String curCode = codeMatcher.group(1);
                                        if (curCode.contains(callCode)) {
                                            cnt++;
                                            System.out.println(node.getId());
                                        }
                                    }

//                                    System.out.println(node.getId() + "," + node.getAttribute("label"));
                                }
                                if (cnt > 1) {
                                    System.out.println("More than one, " + dotFile.getAbsolutePath());
                                    System.exit(1);
                                }

                                System.exit(1);
                                //然后根据那个代码片段的id，产生新的edge
                            }
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
