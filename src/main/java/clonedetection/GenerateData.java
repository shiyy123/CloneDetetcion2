package clonedetection;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.neo4j.cypher.internal.compiler.v2_0.ast.In;
import scala.Int;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class GenerateData {
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
                } else {
                    if (s.charAt(i) == '/' && i < s.length() - 1 && s.charAt(i + 1) == '/') {
                        break;      //ignore remaining characters on line s
                    } else if (s.charAt(i) == '/' && i < s.length() - 1 && s.charAt(i + 1) == '*') {
                        mode = true;
                        i++;           //skip '*' on next iteration of i
                    } else sb.append(s.charAt(i));     //not a comment
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

    void rename4Src() {
        Tool tool = new Tool();
        List<List<File>> allFiles = tool.getFile("/home/cary/Documents/Data/CloneData/src_google/NoComment/");

        allFiles.forEach(x -> {
            x.forEach(y -> {
                String folder = y.getParent();
                folder = folder.substring(folder.lastIndexOf("/") + 1);
                y.renameTo(new File(y.getParent() + "/" + folder + "_" + y.getName()));
            });
        });
    }

    void generateSampleCSV() {
        try {
            Writer writer = Files.newBufferedWriter(Paths.get("/home/cary/Documents/Code/CDLHDetector-master/sample_submission.csv"));
            CSVWriter csvWriter = new CSVWriter(writer);

            String[] header = {"id1___id2", "predictions"};
            csvWriter.writeNext(header);

            File[] files = new File("/home/cary/Documents/Code/CDLHDetector-master/train/1").listFiles();

            File[] files1 = new File("/home/cary/Documents/Code/CDLHDetector-master/test/18").listFiles();

            for (int i = 0; i < files.length; i++) {
                for (int j = i + 1; j < files.length; j++) {
                    String id1 = files[i].getName().substring(0, files[i].getName().indexOf("."));
                    String id2 = files[j].getName().substring(0, files[j].getName().indexOf("."));

                    String[] data = {id1 + "___" + id2, "1"};
                    csvWriter.writeNext(data);
                }
            }

//            for (int i = 0; i < files.length; i++) {
//                for (int j = 0; j < files1.length; j++) {
//                    String id1 = files[i].getName().substring(0, files[i].getName().indexOf("."));
//                    String id2 = files1[j].getName().substring(0, files1[j].getName().indexOf("."));
//
//                    String[] data = {id1 + "___" + id2, "0"};
//                    csvWriter.writeNext(data);
//                }
//            }
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void mergeDifferentFiles() {
        Tool tool = new Tool();
        String path = "/home/cary/Data/src";
        List<List<File>> fileLists = tool.getFile(path);
        fileLists.forEach(x -> {
            x.forEach(y -> {
                String folderName = y.getParentFile().getName();
                String newFileName = folderName.concat("___").concat(y.getName());
                try {
                    FileUtils.copyFile(y, new File("/home/cary/Data/merge/".concat(newFileName)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    long countClone(long bags, long numinBags) {
        return bags * (numinBags * (numinBags - 1) / 2);
    }

    long countDifferent(long bags, long numinBags) {
        return numinBags * numinBags * (bags * (bags - 1) / 2);
    }

    void validationSourcererCCResult() {
        String fileStatsPath = "/home/cary/Documents/Code/SourcererCC/tokenizers/file-level/files_stats/files-stats-0.stats";
        String resultPairPath = "/home/cary/Data/results5.pairs";

        try {
            List<String> fileStatsList = FileUtils.readLines(new File(fileStatsPath), "utf-8");
            List<String> resultPairList = FileUtils.readLines(new File(resultPairPath), "utf-8");

            Map<String, String> fileId2Label = new HashMap<>();

            fileStatsList.forEach(x -> {
                String[] tmps = x.split(",");
                String label = tmps[2];
                label = label.substring(label.lastIndexOf("/") + 1, label.indexOf("___"));
                fileId2Label.put(tmps[0].concat(",").concat(tmps[1]), label);
            });

            int cnt = 0;
            for (String pair : resultPairList) {
                String[] tmp = pair.split(",");
                String first = tmp[0] + "," + tmp[1];
                String second = tmp[2] + "," + tmp[3];

                if (fileId2Label.get(first).equals(fileId2Label.get(second))) {
                    cnt++;
                }
            }

            System.out.println("precision:" + (cnt * 1.0 / resultPairList.size()));
            System.out.println("recall:" + (cnt * 1.0 / countClone(15, 500)));
            double p = (cnt * 1.0 / resultPairList.size());
            double r = (cnt * 1.0 / countClone(15, 500));
            System.out.println("F1:" + (2 * p * r / (p + r)));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void getAllJavaFiles() {
        String path = "/home/cary/Data/JHotDraw7.0.6/";

        List<File> fileList = (List<File>) FileUtils.listFiles(new File(path), null, true);

        Set<File> fileSet = new HashSet<>();

        for (File file : fileList) {
            if (file.getAbsolutePath().endsWith(".java")) {
                fileSet.add(file);
            }
        }
        System.out.println(fileSet.size());

        Set<String> stringSet = new HashSet<>();

        fileSet.forEach(x -> {
            String name = RandomStringUtils.randomAlphanumeric(3);

//            try {
//                FileUtils.copyFile(x, new File("/home/cary/Data/JavaCode/".concat(name).concat("_").concat(x.getName())));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            stringSet.add(x.getName());
        });

        System.out.println(stringSet.size());

        System.out.println(stringSet.size() == fileSet.size());
    }

    /**
     * 生成特征以及标签的数据
     * @param isTrain
     */
    void generateHOPEDataCSV(boolean isTrain) {
        Tool tool = new Tool();

        String subPath = isTrain ? "train" : "test";

//        String HOPEEmbed = "/media/cary/DATA/CloneData/emdtest/";

//        String HOPEEmbed = "/media/cary/DATA/CloneData/emdtrain/";
        String HOPEEmbed = "/home/cary/Documents/Data/CloneData/emd" + subPath + "/cfg/";

        List<List<File>> featureHOPEFiles = tool.getFile(HOPEEmbed);

        StringBuilder sb = new StringBuilder();
        for (File f : Objects.requireNonNull(new File(HOPEEmbed).listFiles())) {
            sb.append(f.getName()).append(",");
        }
        String name = sb.toString().substring(0, sb.toString().length() - 1);

        try {
            Writer writer = Files.newBufferedWriter(Paths.get("/home/cary/Documents/Data/CloneData/csv/"
                    + name + "_" + subPath + "_cfg.csv"));
            CSVWriter csvWriter = new CSVWriter(writer);
//            String[] header = {"f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "predictions"};
            String[] header = {"1000000", "8", "similar", "dissimilar"};

            csvWriter.writeNext(header);

            int cnt = 0;

            for (List<File> embedFolderList : featureHOPEFiles) {
//                System.out.println(cnt++ + "/" + featureHOPEFiles.size());
                for (int i = 0; i < embedFolderList.size(); i++) {
                    List<Double> data1 = tool.getVecFromCFG(Objects.requireNonNull(embedFolderList.get(i).listFiles())[0]);

                    for (int j = 0; j < embedFolderList.size(); j++) {
                        List<Double> data2 = tool.getVecFromCFG(Objects.requireNonNull(embedFolderList.get(j).listFiles())[0]);

                        String[] data = {data1.get(0) + "", data1.get(1) + "", data1.get(2) + "", data1.get(3) + "",
                                data2.get(0) + "", data2.get(1) + "", data2.get(2) + "", data2.get(3) + "", "0"};
                        csvWriter.writeNext(data);
                    }
                }
            }

            File[] embedFolders = new File(HOPEEmbed).listFiles();
            assert embedFolders != null;

            cnt = 0;
            for (int i = 0; i < embedFolders.length; i++) {
//                System.out.println(cnt++ + "/" + embedFolders.length);
                for (int j = 0; j < embedFolders.length; j++) {
                    if (i != j) {
                        File[] emdList1 = embedFolders[i].listFiles();
                        File[] emdList2 = embedFolders[j].listFiles();

                        assert emdList1 != null;
                        for (File emdFolder1 : emdList1) {
                            assert emdList2 != null;
                            for (File emdFolder2 : emdList2) {
                                File emd1 = Objects.requireNonNull(emdFolder1.listFiles())[0];
                                File emd2 = Objects.requireNonNull(emdFolder2.listFiles())[0];

                                List<Double> data1 = tool.getVecFromCFG(emd1);
                                List<Double> data2 = tool.getVecFromCFG(emd2);

                                String[] data = {data1.get(0) + "", data1.get(1) + "", data1.get(2) + "", data1.get(3) + "",
                                        data2.get(0) + "", data2.get(1) + "", data2.get(2) + "", data2.get(3) + "", "1"};
                                csvWriter.writeNext(data);
                            }
                        }
                    }
                }
            }

            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成ident的csv文件
     *
     * @param isTrain
     */
    void generateWord2VecDataCSV(boolean isTrain) {
        Tool tool = new Tool();

        String subPath = isTrain ? "train" : "test";

//        String HOPEEmbed = "/media/cary/DATA/CloneData/emdtest/";

        String HOPEEmbed = "/home/cary/Documents/Data/CloneData/emd" + subPath + "/ident/";

        List<List<File>> featureHOPEFiles = tool.getFile(HOPEEmbed);

        StringBuilder sb = new StringBuilder();
        for (File f : Objects.requireNonNull(new File(HOPEEmbed).listFiles())) {
            sb.append(f.getName()).append(",");
        }
        String name = sb.toString().substring(0, sb.toString().length() - 1);

        try {
            Writer writer = Files.newBufferedWriter(Paths.get("/home/cary/Documents/Data/CloneData/csv/" + name + "_" + subPath + "_word.csv"));

            CSVWriter csvWriter = new CSVWriter(writer);
            String[] header = {"1000000", "32", "similar", "dissimilar"};

            csvWriter.writeNext(header);

            //0: similar
            for (List<File> embedFolderList : featureHOPEFiles) {
                for (int i = 0; i < embedFolderList.size(); i++) {
                    List<Double> data1 = tool.getVecFromIdent(Objects.requireNonNull(embedFolderList.get(i).listFiles())[0]);

                    for (int j = 0; j < embedFolderList.size(); j++) {
                        List<Double> data2 = tool.getVecFromIdent(Objects.requireNonNull(embedFolderList.get(j).listFiles())[0]);

                        String[] data = new String[data1.size() + data2.size() + 1];
                        for (int k = 0; k < data1.size(); k++) {
                            data[k] = data1.get(k) + "";
                        }
                        for (int k = 0; k < data2.size(); k++) {
                            data[k + data1.size()] = data2.get(k) + "";
                        }
                        data[data.length - 1] = "0";

                        csvWriter.writeNext(data);
                    }
                }
            }

            //1:dissimilar
            File[] embedFolders = new File(HOPEEmbed).listFiles();
            assert embedFolders != null;
            for (int i = 0; i < embedFolders.length; i++) {
                for (int j = 0; j < embedFolders.length; j++) {
                    if (i != j) {
                        File[] emdList1 = embedFolders[i].listFiles();
                        File[] emdList2 = embedFolders[j].listFiles();

                        assert emdList1 != null;
                        for (File emdFolder1 : emdList1) {
                            assert emdList2 != null;
                            for (File emdFolder2 : emdList2) {
                                File emd1 = Objects.requireNonNull(emdFolder1.listFiles())[0];
                                File emd2 = Objects.requireNonNull(emdFolder2.listFiles())[0];

                                List<Double> data1 = tool.getVecFromIdent(emd1);
                                List<Double> data2 = tool.getVecFromIdent(emd2);

                                String[] data = new String[data1.size() + data2.size() + 1];
                                for (int k = 0; k < data1.size(); k++) {
                                    data[k] = data1.get(k) + "";
                                }
                                for (int k = 0; k < data2.size(); k++) {
                                    data[k + data2.size()] = data2.get(k) + "";
                                }
                                data[data.length - 1] = "1";

                                csvWriter.writeNext(data);
                            }
                        }
                    }
                }
            }

            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 合并cfg和ident的数据，
     *
     * @param cfgPath
     * @param identPath
     */
    void mergeCFGAndIdent(String cfgPath, String identPath) {
        try {
            Reader cfgReader = Files.newBufferedReader(Paths.get(cfgPath));
            CSVReader cfgCSV = new CSVReader(cfgReader);

            Reader identReader = Files.newBufferedReader(Paths.get(identPath));
            CSVReader identCSV = new CSVReader(identReader);

            System.out.println("begin");

            cfgCSV.readNext();

            String[] cfg;
            String[] ident;

            CSVWriter csvWriter = new CSVWriter(Files.newBufferedWriter(Paths.get("/home/cary/Documents/Data/CloneData/csv/train_merge.csv")));

            cfgCSV.readNext();
            identCSV.readNext();
            int cnt = 0;

            while ((cfg = cfgCSV.readNext()) != null && (ident = identCSV.readNext()) != null) {
                String[] data = new String[cfg.length + ident.length - 1];

                System.arraycopy(cfg, 0, data, 0, (cfg.length - 1) / 2);
                System.arraycopy(ident, 0, data, (cfg.length - 1) / 2, (ident.length - 1) / 2);
                if (cfg.length - 1 - (cfg.length - 1) / 2 >= 0)
                    System.arraycopy(cfg, (cfg.length - 1) / 2, data, (cfg.length - 1) / 2 + (ident.length - 1) / 2, cfg.length - 1 - (cfg.length - 1) / 2);
                if (ident.length - 1 - (ident.length - 1) / 2 >= 0)
                    System.arraycopy(ident, (ident.length - 1) / 2, data, (ident.length - 1) / 2 + (cfg.length - 1), ident.length - 1 - (ident.length - 1) / 2);

                data[data.length - 1] = cfg[cfg.length - 1];
                csvWriter.writeNext(data);
                csvWriter.flush();

                System.out.println(cnt++);
            }

            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    double cal(double p, double r) {
        return 2 * p * r / (p + r);
    }

    //均衡数据，让标签为1的数据和标签为0的数据均衡
    void balanceDataLabel(String csvPath) {
        String balancedCsvPath = csvPath.substring(0, csvPath.lastIndexOf('/') + 1).concat("b_").
                concat(csvPath.substring(csvPath.lastIndexOf('/') + 1));

        int zeroCnt = 0;
        int oneCnt = 0;
        try {
            Reader reader = Files.newBufferedReader(Paths.get(csvPath));
            CSVReader csvReader = new CSVReader(reader);

            String[] content;
            while ((content = csvReader.readNext()) != null) {
                if (!content[content.length - 1].contains("similar")) {
                    if (content[content.length - 1].equals("0")) {
                        zeroCnt++;
                    } else if (content[content.length - 1].equals("1")) {
                        oneCnt++;
                    }
                }
            }
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int smallCnt = zeroCnt < oneCnt ? zeroCnt : oneCnt;
        int bigCnt = zeroCnt > oneCnt ? zeroCnt : oneCnt;
        int[] arr = new int[bigCnt];
        for (int i = 0; i < bigCnt; i++) {
            arr[i] = i;
        }
        HashSet<Integer> set = new HashSet<>();
        while (set.size() < smallCnt) {
            int idx = (int) (Math.random() * arr.length);
            while (!set.isEmpty() && set.contains(idx)) {
                idx = (int) (Math.random() * arr.length);
            }
            set.add(idx);
        }

        boolean zero = (smallCnt == zeroCnt);

        zeroCnt = 0;
        oneCnt = 0;

        try {
            Writer writer = Files.newBufferedWriter(Paths.get(balancedCsvPath));
            CSVWriter csvWriter = new CSVWriter(writer);

            Reader reader = Files.newBufferedReader(Paths.get(csvPath));
            CSVReader csvReader = new CSVReader(reader);

            String[] content;

            //写头部
            content = csvReader.readNext();
            csvWriter.writeNext(content);

            while ((content = csvReader.readNext()) != null) {
                if (content[content.length - 1].equals("0")) {
                    if (zero) {
                        csvWriter.writeNext(content);
                    } else {
                        if (set.contains(zeroCnt)) {
                            csvWriter.writeNext(content);
                        }
                    }
                    zeroCnt++;
                } else if (content[content.length - 1].equals("1")) {
                    if (!zero) {
                        csvWriter.writeNext(content);
                    } else {
                        if (set.contains(oneCnt)) {
                            csvWriter.writeNext(content);
                        }
                    }
                    oneCnt++;
                }
            }
            csvWriter.flush();
            csvReader.close();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //手动对数据顺序进行打乱处理
    void randomOrder(String csvPath, String randomCsvPath) {
        try {
            String[] content;
            CSVReader reader = new CSVReader(Files.newBufferedReader(Paths.get(csvPath)));
            content = reader.readNext();

            CSVWriter writer = new CSVWriter(Files.newBufferedWriter(Paths.get(randomCsvPath)));
            writer.writeNext(content);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //获取rest数量的，[0, length - 1]范围内的数
    HashSet<Integer> getFixedRandomNumbers(int rest, int length) {
        HashSet<Integer> set = new HashSet<>();
        int curSize = length;
        while (curSize > rest) {
            int idx = (int) (Math.random() * length);
            if (set.isEmpty()) {
                set.add(idx);
            } else {
                while (set.contains(idx)) {
                    idx = (int) (Math.random() * length);
                }
                set.add(idx);
            }
            curSize--;
        }

        return set;
    }

    //从训练集和测试集中随机删除部分
    void deleteFromFolder(int rest, String identFolderPath, String cfgFolderPath) {
        File[] identFolders = new File(identFolderPath).listFiles();
        File[] cfgFolders = new File(cfgFolderPath).listFiles();
        assert identFolders != null;
        assert cfgFolders != null;
        assert identFolders.length == cfgFolders.length;

        int len = identFolders.length;
        HashSet<Integer> set = getFixedRandomNumbers(rest, len);

        for (int i = 0; i < len; i++) {
            try {
                if (set.contains(i)) {
                    FileUtils.deleteDirectory(new File(identFolders[i].getAbsolutePath()));
                    FileUtils.deleteDirectory(new File(cfgFolders[i].getAbsolutePath()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        System.out.println(set.size());
    }

    //删除多余的数据，使数据量大小合适
    void deleteFolders(int rest, String identFoldersPath, String cfgFoldersPath) {
        File[] identFiles = new File(identFoldersPath).listFiles();
        File[] cfgFiles = new File(cfgFoldersPath).listFiles();

        assert identFiles != null;
        assert cfgFiles != null;
        assert identFiles.length == cfgFiles.length;

        int len = identFiles.length;

        for (int i = 0; i < len; i++) {
            deleteFromFolder(rest, identFiles[i].getAbsolutePath(), cfgFiles[i].getAbsolutePath());
        }
    }

    //交互数据集中的0和1
    void exchangeOneZero(String csvPath) {
        String newCsvPath = csvPath.substring(0, csvPath.lastIndexOf("/") + 1).concat("e_").concat(csvPath.substring(csvPath.lastIndexOf("/") + 1));

        try {
            CSVReader reader = new CSVReader(Files.newBufferedReader(Paths.get(csvPath)));
            CSVWriter writer = new CSVWriter(Files.newBufferedWriter(Paths.get(newCsvPath)));
            String[] content;
            while ((content = reader.readNext()) != null) {
                if (content[content.length - 1].equals("0")) {
                    content[content.length - 1] = "1";
                } else if (content[content.length - 1].equals("1")) {
                    content[content.length - 1] = "0";
                }
                writer.writeNext(content);
            }
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        GenerateData generateData = new GenerateData();
//        generateData.deleteFolders(100, "/home/cary/Documents/Data/CloneData/emdtest/ident/",
//                "/home/cary/Documents/Data/CloneData/emdtest/cfg");
//        generateData.generateWord2VecDataCSV(false);
        generateData.generateHOPEDataCSV(false);

        generateData.balanceDataLabel("/home/cary/Documents/Data/CloneData/csv/17,14,15,16,18_test_word.csv");
//        generateData.exchangeOneZero("/home/cary/Documents/Data/CloneData/csv/17,14,15,16,18_test_word.csv");


//        System.out.println(generateData.cal(0.5470176, 0.5564));

//        generateData.mergeCFGAndIdent("/home/cary/Documents/Data/CloneData/csv/test_cfg.csv", "/home/cary/Documents/Data/CloneData/csv/test_word.csv");

//        int bags = 2;
//        int numinBags = 500;

//        System.out.println(generateData.countClone(bags, numinBags));
//        System.out.println(generateData.countDifferent(bags, numinBags));

//        generateData.generateHOPEDataCSV();

//        generateData.generateSampleCSV();
//        generateData.calculateDistanceBetweenEmd(new File("/home/cary/Documents/Data/generateData/cfg_emd/3246696_simplify.emd"), new File("/home/cary/Documents/Data/generateData/cfg_emd/3320433_simplify.emd"));
//        generateData.generateEdge(new File("/home/cary/Documents/Data/generateData/cfg/173.dot"));
    }


}
