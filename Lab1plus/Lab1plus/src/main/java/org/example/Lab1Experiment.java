package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.stream.file.FileSinkImages;

public class Lab1Experiment {
    private final Map<String, Map<String, Integer>> graph = new HashMap<>();

    public static void main(String[] args) {
        Lab1Experiment experiment = new Lab1Experiment();
        Scanner scanner = new Scanner(System.in);

        while (true) {

            try {
                experiment.readTextFile("C:\\Users\\11255\\Desktop\\1.txt");
                experiment.showDirectedGraph();
                experiment.saveGraphAsImage("C:\\Users\\11255\\Desktop\\graph.png");

                while (true) {
                    System.out.println("请选择功能：");
                    System.out.println("1. 查询桥接词");
                    System.out.println("2. 生成新文本");
                    System.out.println("3. 计算最短路径");
                    System.out.println("4. 随机游走");
                    System.out.println("5. 退出");

                    int choice;
                    String word1;
                    String word2;
                    while (true) {
                        try {
                            choice = Integer.parseInt(scanner.nextLine());
                            break;
                        } catch (NumberFormatException e) {
                            //System.out.println("输入无效，请输入一个整数：");
                        }
                    }

                    switch (choice) {
                        case 1:
                            System.out.println("请输入两个单词：");
                            word1 = scanner.next();
                            word2 = scanner.next();
                            System.out.println(experiment.queryBridgeWords(word1, word2));
                            break;
                        case 2:
                            System.out.println("请输入一行新文本：");
                            String inputText = scanner.nextLine();
                            System.out.println(experiment.generateNewText(inputText));
                            break;
                        case 3:
                            System.out.println("请输入单词（若只输入一个单词，则计算该单词到图中其他任一单词的最短路径）：");
                            word1 = scanner.next();
                            String word2Input = scanner.nextLine().trim();
                            if (word2Input.isEmpty()) {
                                System.out.println(experiment.calcShortestPath(word1, ""));
                            } else {
                                String[] words = word2Input.split("\\s+");
                                word2 = words[0];
                                System.out.println(experiment.calcShortestPath(word1, word2));
                            }
                            break;
                        case 4:
                            System.out.println(experiment.randomWalk());
                            break;
                        case 5:
                            System.exit(0);
                        default:
                            System.out.println("无效选择");
                    }
                }
            } catch (IOException e) {
                System.out.println("输入无效，请输入正确的路径：");
            }
        }
    }

    // 读取文本文件并生成有向图
    public void readTextFile(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        String text = String.join(" ", lines).toLowerCase();
        text = text.replaceAll("[^a-z ]", " ");
        String[] words = text.split("\\s+");

        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];
            graph.putIfAbsent(word1, new HashMap<>());
            Map<String, Integer> edges = graph.get(word1);
            edges.put(word2, edges.getOrDefault(word2, 0) + 1);
        }
    }

    // 展示有向图
    public void showDirectedGraph() {
        System.out.println("有向图：");
        for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
            String word = entry.getKey();
            Map<String, Integer> edges = entry.getValue();
            for (Map.Entry<String, Integer> edge : edges.entrySet()) {
                System.out.println(word + " -> " + edge.getKey() + " (权重: " + edge.getValue() + ")");
            }
        }
    }

    // 功能1：查询桥接词
    public String queryBridgeWords(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }
        List<String> bridgeWords = new ArrayList<>();
        for (String middleWord : graph.get(word1).keySet()) {
            if (graph.containsKey(middleWord) && graph.get(middleWord).containsKey(word2)) {
                bridgeWords.add(middleWord);
            }
        }
        if (bridgeWords.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        }
        return "The bridge words from " + word1 + " to " + word2 + " are: " + String.join(", ", bridgeWords);
    }

    // 功能2：根据桥接词生成新文本
    public String generateNewText(String inputText) {
        String[] words = inputText.toLowerCase().replaceAll("[^a-z ]", "").split("\\s+");
        StringBuilder newText = new StringBuilder();
        Random rand = new Random();

        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];
            newText.append(word1).append(" ");
            List<String> bridgeWords = new ArrayList<>();
            if (graph.containsKey(word1)) {
                for (String middleWord : graph.get(word1).keySet()) {
                    if (graph.containsKey(middleWord) && graph.get(middleWord).containsKey(word2)) {
                        bridgeWords.add(middleWord);
                    }
                }
            }
            if (!bridgeWords.isEmpty()) {
                newText.append(bridgeWords.get(rand.nextInt(bridgeWords.size()))).append(" ");
            }
        }
        newText.append(words[words.length - 1]);
        return newText.toString();
    }

    // 功能3：计算两个单词之间的最短路径
    public String calcShortestPath(String word1, String word2) {
        if (!graph.containsKey(word1)) {
            return "No " + word1 + " in the graph!";
        }

        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

        for (String word : graph.keySet()) {
            dist.put(word, Integer.MAX_VALUE);
        }
        dist.put(word1, 0);
        pq.add(word1);

        while (!pq.isEmpty()) {
            String current = pq.poll();
            if (graph.containsKey(current)) {
                for (Map.Entry<String, Integer> edge : graph.get(current).entrySet()) {
                    String neighbor = edge.getKey();
                    int weight = edge.getValue();
                    int alt = dist.get(current) + weight;
                    if (alt < dist.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                        dist.put(neighbor, alt);
                        prev.put(neighbor, current);
                        pq.add(neighbor);
                    }
                }
            }
        }

        if (word2.isEmpty()) {
            StringBuilder result = new StringBuilder();
            for (String word : dist.keySet()) {
                if (!word.equals(word1) && dist.get(word) != Integer.MAX_VALUE) {
                    List<String> path = new LinkedList<>();
                    for (String at = word; at != null; at = prev.get(at)) {
                        path.add(at);
                    }
                    Collections.reverse(path);
                    result.append("The shortest path from ").append(word1).append(" to ").append(word)
                            .append(" is: ").append(String.join(" -> ", path))
                            .append(" (总权重: ").append(dist.get(word)).append(")\n");
                }
            }
            return result.toString();
        } else {
            if (!dist.containsKey(word2) || dist.get(word2) == Integer.MAX_VALUE) {
                return "No path from " + word1 + " to " + word2 + "!";
            }
            List<String> path = new LinkedList<>();
            for (String at = word2; at != null; at = prev.get(at)) {
                path.add(at);
            }
            Collections.reverse(path);
            return "The shortest path from " + word1 + " to " + word2 + " is: " + String.join(" -> ", path) +
                    " (总权重: " + dist.get(word2) + ")";
        }
    }

    // 功能4：随机游走
    public String randomWalk() {
        Random rand = new Random();
        List<String> words = new ArrayList<>(graph.keySet());
        if (words.isEmpty()) return "图中没有节点";

        String current = words.get(rand.nextInt(words.size()));
        Set<String> visitedEdges = new HashSet<>();
        StringBuilder walk = new StringBuilder(current);

        while (true) {
            if (!graph.containsKey(current) || graph.get(current).isEmpty()) break; // 检查当前节点是否有邻接词
            Map<String, Integer> edges = graph.get(current);
            List<String> neighbors = new ArrayList<>(edges.keySet());
            String next = neighbors.get(rand.nextInt(neighbors.size()));
            String edge = current + " -> " + next;
            if (visitedEdges.contains(edge)) break;
            visitedEdges.add(edge);
            walk.append(" ").append(next);
            current = next;
        }
        return walk.toString();
    }

    // 功能8：保存有向图为图形文件
    public void saveGraphAsImage(String filePath) {
        Graph graphStream = new SingleGraph("Graph");
        System.setProperty("org.graphstream.ui", "swing");

        // 使用 Set 来存储所有的节点，以避免重复
        Set<String> nodes = new HashSet<>();

        // 添加所有节点
        for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
            String node1 = entry.getKey();
            nodes.add(node1); // 添加外层键

            // 添加内层键
            nodes.addAll(entry.getValue().keySet());
        }

        // 将所有节点添加到 graphStream
        for (String node : nodes) {
            graphStream.addNode(node);
            System.out.println(node);
        }

        // 添加所有边
        for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
            String node1 = entry.getKey();
            for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
                String node2 = edge.getKey();
                String edgeId = node1 + "-" + node2;
                if (graphStream.getEdge(edgeId) == null) {
                    graphStream.addEdge(edgeId, node1, node2, true).setAttribute("ui.label", edge.getValue());
                }
            }
        }

        // 为每个节点设置标签
        for (Node node : graphStream) {
            node.setAttribute("ui.label", node.getId());
        }

        // 创建 FileSinkImages 对象并保存图像
        FileSinkImages pic = new FileSinkImages(FileSinkImages.OutputType.PNG, FileSinkImages.Resolutions.VGA);
        pic.setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);

        try {
            pic.writeAll(graphStream, filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
