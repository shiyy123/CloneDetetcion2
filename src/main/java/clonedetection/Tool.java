package clonedetection;

import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    String toOneLine(String s) {
        String[] tmps = s.split("\n");

        StringBuilder res = new StringBuilder();
        if (tmps.length > 1) {
            for (int i = 0; i < tmps.length - 1; i++) {
                res.append(tmps[i], 0, tmps[i].length() - 1).append(" ");
            }
            res.append(tmps[tmps.length - 1]);
            String s1 = res.toString();
            s1 = s1.replace("  ", " ");
            return s1;
        } else {
            return s;
        }
    }

    void generateFuncCall() {

        List<List<File>> files = getFile(Config.basePath + "FDG");
        for (List<File> folder : files) {
            for (File file : folder) {
                //get folder name
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

                    File[] callFuncs = new File(file.getAbsolutePath().concat(File.separator.concat("call"))).listFiles();
                    String srcCode = FileUtils.readFileToString(getSourceCode(new File(Config.basePath.concat("FDG".concat(File.separator).concat(name)))), "utf-8");

                    //文件名为被调用的函数id
                    for (File beCalledFunc : callFuncs) {
                        List<String> callLineList = FileUtils.readLines(beCalledFunc, "utf-8");

                        //Call Loc recorder
                        Map<Integer, List<Integer>> callLocMap = new HashMap<>();

                        //获得被调用函数名（因为不存在函数重载，所以函数名具有唯一性）
                        String beCalledFileId = beCalledFunc.getName().substring(0, beCalledFunc.getName().indexOf("."));
                        String beCalledFuncName = id2name.get(beCalledFileId);

                        //循环函数调用语句
                        for (String callLine : callLineList) {
                            //获取调用函数语句
                            Matcher matcher = Config.callPattern.matcher(callLine);
                            if (matcher.find()) {
                                String s = matcher.group(0);
                                JSONObject jsonObject = new JSONObject(s);

                                //获得发起调用的函数名
                                String callFuncName = id2name.get(jsonObject.getString("functionId"));
                                //获得具体调用实施处的代码
                                String callCode = jsonObject.getString("code");
//                                callCode = callCode.replace("(", "\\(").replace(")", "\\)").replace("[", "\\[");

                                String callFuncId = name2id.get(callFuncName);
                                String beCalledFuncId = name2id.get(beCalledFuncName);

                                //TODO
                                //读取相应的dot文件，根据callCode找到具体是哪个代码片段进行调用操作。
                                File dotFile = new File(Config.basePath.concat("dot").concat(File.separator.concat(name).concat(File.separator.concat(callFuncId).concat(".dot"))));
//                                System.out.println(callCode);

                                GraphParser parser = new GraphParser(new FileInputStream(dotFile));
                                Map<String, GraphNode> nodes = parser.getNodes();
//                                Map<String, GraphEdge> edges = parser.getEdges();

                                int cnt = 0;
                                //遍历dot文件中的每一行
                                int callStatementId = -1;
                                for (GraphNode node : nodes.values()) {
                                    //TODO 代码中莫名其妙的换行和空格很头疼
                                    Matcher codeMatcher = Config.codePattern.matcher(node.getAttribute("label").toString());

                                    //一行中有多个调用函数语句
                                    if (codeMatcher.find()) {
                                        String curCode = codeMatcher.group(1);

                                        curCode = toOneLine(curCode);

                                        if (curCode.contains(callCode)) {
                                            int statementId = Integer.parseInt(node.getId());
                                            int idx = curCode.indexOf(callCode);

                                            if (callLocMap.containsKey(statementId)) {
                                                //search next one
                                                List<Integer> tmp = callLocMap.get(statementId);
                                                int startIdx = tmp.get(tmp.size() - 1) + 1;
                                                int findIdx = curCode.indexOf(callCode, startIdx);
                                                tmp.add(findIdx);
                                                callLocMap.put(statementId, tmp);
                                                callStatementId = Integer.parseInt(node.getId());
                                                cnt++;
                                            } else {
                                                boolean flag = true;
//                                                System.out.println("statementId:" + statementId);
                                                for (int key : callLocMap.keySet()) {
//                                                    System.out.println("key=" + key);
                                                    if (key == statementId) {
                                                        flag = false;
                                                    }
                                                }
                                                if (flag) {
                                                    int findIdx = curCode.indexOf(callCode);
                                                    List<Integer> tmp = new ArrayList<>();
                                                    tmp.add(findIdx);
//                                                    System.out.println("putid=" + statementId);
                                                    callLocMap.put(statementId, tmp);
//                                                    System.out.println("curCode:" + curCode);
                                                    callStatementId = Integer.parseInt(node.getId());
                                                    cnt++;
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }

                                if (cnt != 1) {
                                    System.out.println("callCode:" + callCode);
                                    System.out.println("cnt:" + cnt);
                                    System.err.println("Error." + dotFile.getAbsolutePath());
                                    System.exit(1);
                                } else {
                                    File callFolder = new File(Config.basePath.concat("call").concat(File.separator).concat(name));
                                    if (!callFolder.exists()) {
                                        callFolder.mkdirs();
                                    }
                                    File callFile = new File(callFolder.getAbsolutePath().concat(File.separator.concat("call.txt")));

                                    FileUtils.write(callFile, "callStatementId:" + callStatementId + "\n", "utf-8", true);
                                    FileUtils.write(callFile, "callFuncId:" + callFuncId + "\n", "utf-8", true);
                                    FileUtils.write(callFile, "beCalledFuncId:" + beCalledFuncId + "\n", "utf-8", true);
                                    FileUtils.write(callFile, "callDotFile:" + dotFile.getAbsolutePath() + "\n", "utf-8", true);
                                    FileUtils.write(callFile, "---" + "\n", "utf-8", true);
                                }
                            }
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    JSONArray call2JSON(File call) {
        JSONArray res = new JSONArray();
        try {
            List<String> list = FileUtils.readLines(call, "utf-8");
            for (int i = 0; i < list.size(); i += 5) {
                JSONObject item = new JSONObject();
                item.put("callStatementId", list.get(i).substring(list.get(i).indexOf(":") + 1));
                item.put("callFuncId", list.get(i + 1).substring(list.get(i + 1).indexOf(":") + 1));
                item.put("beCalledFuncId", list.get(i + 2).substring(list.get(i + 2).indexOf(":") + 1));
                item.put("callDotFile", list.get(i + 3).substring(list.get(i + 3).indexOf(":") + 1));
                res.put(item);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return res;
    }

    /**
     * 生成CFG的边
     */
    void generateEdge() {
        String dotFolder = Config.basePath.concat("dot");
        List<List<File>> fileList = getFile(dotFolder);
        for (List<File> files : fileList) {
            for (File dotFolderFile : files) {
                String folderName = getLastTwo(dotFolderFile.getAbsolutePath());
                File[] dots = dotFolderFile.listFiles();
                for (File dot : dots) {
                    try {
                        File edgeFolder = new File(Config.basePath.concat("edge").concat(File.separator.concat(folderName)));
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
            }
        }
    }

    void splitByFeature() {
        List<List<File>> callFolder = getFile(Config.basePath.concat("call"));
        for (List<File> file1 : callFolder) {
            for (File file2 : file1) {
                String curFolder = getLastTwo(file2.getAbsolutePath());

                File callFile = new File(file2.getAbsolutePath().concat(File.separator.concat("call.txt")));

                File funcFile = new File(Config.basePath.concat("func").concat(File.separator).concat(curFolder.concat(File.separator.concat("func.txt"))));
                try {
                    List<String> funcLineList = FileUtils.readLines(funcFile, "utf-8");
                    List<String> funcList = new ArrayList<>();
                    funcLineList.forEach(s -> funcList.add(s.split("\t")[1]));

                    List<Set<String>> features = new ArrayList<>();

                    JSONArray array = call2JSON(callFile);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject item = array.getJSONObject(i);
                        String callFuncId = item.getString("callFuncId");
                        String beCalledFuncId = item.getString("beCalledFuncId");

                        boolean find = false;
                        for (int j = 0; j < features.size(); j++) {
                            Set<String> set = features.get(j);
                            if (set.contains(callFuncId) || set.contains(beCalledFuncId)) {
                                find = true;
                                set.add(callFuncId);
                                set.add(beCalledFuncId);
                            }
                            features.set(j, set);
                        }

                        if (!find) {
                            Set<String> set = new HashSet<>();
                            set.add(callFuncId);
                            set.add(beCalledFuncId);
                            features.add(set);
                        }
                    }

                    if (features.size() != 1) {
                        features = mergeSet(features);
                    }

                    System.out.println("features");

                    features.forEach(System.out::println);

                    if (features.size() != 1) {
                        System.out.println(callFile.getAbsolutePath());
                        System.out.println("amazing");
                    }

                    System.out.println("---");

                    File featureFolder = new File(Config.basePath.concat("feature").concat(File.separator).concat(curFolder));
                    if (!featureFolder.exists()) {
                        featureFolder.mkdirs();
                    }
                    File feature = new File(featureFolder.getAbsolutePath().concat(File.separator.concat("feature.txt")));

                    features.forEach(x -> {
                        try {
                            FileUtils.write(feature, x.toString() + "\n", "utf-8", true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    System.out.println(feature.getAbsolutePath());

                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    List<Set<String>> mergeSet(List<Set<String>> features) {

        List<Boolean> removable = new ArrayList<>();

        for (int i = 0; i < features.size(); i++) {
            removable.add(false);
        }

        for (int i = 0; i < features.size() - 1; i++) {
            for (int j = i + 1; j < features.size(); j++) {

                if (removable.get(i).equals(false) && removable.get(j).equals(false)) {
                    Set<String> s1 = new HashSet<>(features.get(i));
                    s1.retainAll(features.get(j));

                    if (!s1.isEmpty()) {
                        removable.set(j, true);

                        Set<String> s2 = new HashSet<>(features.get(i));
                        s2.addAll(features.get(j));
                        features.set(i, s2);
                    }
                }
            }
        }

        for (int i = removable.size() - 1; i >= 0; i--) {
            if (removable.get(i).equals(true)) {
                features.remove(i);
            }
        }
        return features;
    }

}
