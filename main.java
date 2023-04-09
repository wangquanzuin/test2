import java.io.*;
import java.util.*;

//定义一个水流类，包含编号，宽度，抵达时间，通过时间，进入管道的时间和管道编号
class Flow {
    int id;
    int width;
    int arrivalTime;
    int passingTime;
    int enterTime;
    int pipeId;

    public Flow(int id, int width, int arrivalTime, int passingTime) {
        this.id = id;
        this.width = width;
        this.arrivalTime = arrivalTime;
        this.passingTime = passingTime;
        this.enterTime = -1; //初始值为-1，表示还未进入管道
        this.pipeId = -1; //初始值为-1，表示还未分配管道
    }
}

//定义一个管道类，包含编号，宽度，当前占用的宽度和排队区的水流列表
class Pipe {
    int id;
    int width;
    int occupiedWidth;
    List<Flow> queue;

    public Pipe(int id, int width) {
        this.id = id;
        this.width = width;
        this.occupiedWidth = 0; //初始值为0，表示没有占用的宽度
        this.queue = new ArrayList<>(); //初始为空列表，表示没有排队的水流
    }
}

public class main {

    //定义一个比较器，按照水流的进入管道时间和编号排序
    static class WaterFlowComparator implements Comparator<Flow> {
        @Override
        public int compare(Flow w1, Flow w2) {
            if (w1.enterTime != w2.enterTime) {
                return w1.enterTime - w2.enterTime; //先按照进入时间升序排序
            } else {
                return w1.id - w2.id; //再按照编号升序排序
            }
        }
    }

/*    //读取水流数据文件，返回一个水流列表
    public static List<Flow> readWaterFlows(String fileName) throws IOException {
        List<Flow> flows = new ArrayList<>();
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);
        String line = null;
        while ((line = br.readLine()) != null) {
            String[] data = line.split(","); //按照逗号分隔数据
            int id = Integer.parseInt(data[0]); //第一列为水流编号
            int width = Integer.parseInt(data[1]); //第二列为水流宽度
            int arrivalTime = Integer.parseInt(data[2]); //第三列为水流抵达时间
            int passingTime = Integer.parseInt(data[3]); //第四列为水流通过时间
            Flow flow = new Flow(id, width, arrivalTime, passingTime); //创建一个水流对象
            flows.add(flow); //添加到水流列表中
        }
        br.close();
        fr.close();
        return flows;
    }*/

    //从文件中读取水流数据，返回一个列表
    public static List<Flow> readWaterFlows(String fileName) throws IOException {
        List<Flow> flows = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = null;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length == 4) {
                int id = Integer.parseInt(parts[0]);
                int width = Integer.parseInt(parts[1]);
                int arrival = Integer.parseInt(parts[2]);
                int duration = Integer.parseInt(parts[3]);
                flows.add(new Flow(id, width, arrival, duration));
            }
        }
        br.close();
        return flows;
    }

    //读取管道数据文件，返回一个管道列表
    public static List<Pipe> readPipes(String fileName) throws IOException {
        List<Pipe> pipes = new ArrayList<>();
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);
        String line = null;
        while ((line = br.readLine()) != null) {
            String[] data = line.split(","); //按照逗号分隔数据
            int id = Integer.parseInt(data[0]); //第一列为管道编号
            int width = Integer.parseInt(data[1]); //第二列为管道宽度
            Pipe pipe = new Pipe(id, width); //创建一个管道对象
            pipes.add(pipe); //添加到管道列表中
        }
        br.close();
        fr.close();
        return pipes;
    }

    //写入输出文件，按照格式输出水流的发送顺序和时间
    public static void writeOutput(String fileName, List<Flow> flows) throws IOException {
        FileWriter fw = new FileWriter(fileName);
        BufferedWriter bw = new BufferedWriter(fw);
        for (Flow flow : flows) { //遍历每个水流对象

            bw.write(flow.id + "," + flow.pipeId + "," + flow.enterTime); //按照格式输出水流编号，管道编号和进入时间
            bw.newLine(); //换行
        }
        bw.close();
        fw.close();
    }
  /*  public static void writeOutputForFlow(String fileName, List<Flow> flows) throws IOException {
        FileWriter fw = new FileWriter(fileName);
        BufferedWriter bw = new BufferedWriter(fw);
        for (Flow flow : flows) { //遍历每个水流对象

            bw.write(flow.id + "," + flow.width + "," + flow.arrivalTime+ "," + flow.passingTime); //按照格式输出水流编号，管道编号和进入时间
            bw.newLine(); //换行
        }
        bw.close();
        fw.close();
    }*/


    //寻找最优的发送顺序和时间，返回一个水流列表，按照发送顺序排序
    public static List<Flow> findOptimalOrder(List<Flow> flows, List<Pipe> pipes) {
        List<Flow> result = new ArrayList<>(); //创建一个结果列表
        PriorityQueue<Flow> pq = new PriorityQueue<>(new WaterFlowComparator()); //创建一个优先队列，按照水流的进入时间和编号排序
        int currentTime = 0; //当前时间，初始为0
        int index = 0; //当前处理的水流的索引，初始为0
        while (index < flows.size() || !pq.isEmpty()) { //当还有未处理的水流或者优先队列不为空时，循环执行
            if (pq.isEmpty()) { //如果优先队列为空，说明当前没有正在发送的水流，直接将下一个抵达的水流加入队列
                pq.offer(flows.get(index)); //将下一个抵达的水流加入队列
                index++;
                currentTime = pq.peek().arrivalTime; //更新当前时间为该水流的抵达时间
            }
            Flow current = pq.poll(); //从优先队列中取出一个水流，作为当前要处理的水流
            result.add(current); //将该水流加入结果列表中
            currentTime += current.passingTime; //更新当前时间为该水流通过管道所需的时间
            for (Pipe pipe : pipes) { //遍历每个管道，检查是否有排队等待的水流可以进入
                while (!pipe.queue.isEmpty() && pipe.occupiedWidth + pipe.queue.get(0).width <= pipe.width) { //如果排队区不为空，并且第一个排队的水流可以进入管道，循环执行
                    Flow next = pipe.queue.remove(0); //从排队区中移除第一个水流
                    pipe.occupiedWidth += next.width; //更新管道的占用宽度
                    next.enterTime = currentTime; //更新该水流的进入时间
                    pq.offer(next); //将该水流加入优先队列中
                }
            }
            for (Pipe pipe : pipes) { //遍历每个管道，检查是否有空闲的管道可以分配给未处理的水流
                if (index < flows.size()) { //如果还有未处理的水流，继续执行
                    Flow next = flows.get(index); //获取下一个未处理的水流
                    if (next.arrivalTime <= currentTime) { //如果该水流已经抵达，继续执行
                        if (pipe.occupiedWidth + next.width <= pipe.width) { //如果该管道有足够的空闲宽度可以容纳该水流，继续执行
                            pipe.occupiedWidth += next.width; //更新管道的占用宽度
                            next.enterTime = currentTime; //更新该水流的进入时间
                            next.pipeId = pipe.id; //更新该水流的管道编号
                            pq.offer(next); //将该水流加入优先队列中
                            index++; //更新未处理的水流的索引
                        } else { //否则，说明该管道没有足够的空闲宽度可以容纳该水流，将该水流加入排队区等待
                            next.pipeId = pipe.id; //更新该水流的管道编号
                            pipe.queue.add(next); //将该水流加入排队区中
                            index++; //更新未处理的水流的索引
                        }
                    } else { //否则，说明该水流还未抵达，跳出循环，等待下一个时间点再处理
                        break;
                    }
                }
            }
            for (Pipe pipe : pipes) { //遍历每个管道，更新占用宽度
                if (pipe.occupiedWidth >= current.width) { //如果该管道的占用宽度大于等于当前处理的水流的宽度，说明该水流是从该管道发送的，继续执行
                    pipe.occupiedWidth -= current.width; //更新管道的占用宽度，减去当前处理的水流的宽度
                    break; //跳出循环
                }
            }
        }
        return result; //返回结果列表
    }




    public static void main(String[] args) throws IOException {
        String waterFlowFileName = "flow.txt"; //水流数据文件名
        String pipeFileName = "port.txt"; //管道数据文件名
        String outputFileName = "output.txt"; //输出文件名

        List<Flow> flows = readWaterFlows(waterFlowFileName); //读取水流数据文件，返回一个水流列表
        List<Pipe> pipes = readPipes(pipeFileName); //读取管道数据文件，返回一个管道列表

        List<Flow> result = findOptimalOrder(flows, pipes); //寻找最优的发送顺序和时间，返回一个水流列表，按照发送顺序排序

        writeOutput(outputFileName, result); //写入输出文件，按照格式输出水流的发送顺序和时间

        System.out.println("The output file is generated successfully."); //打印成功信息
    }
}