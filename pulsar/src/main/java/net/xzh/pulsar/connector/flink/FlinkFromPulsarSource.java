package net.xzh.pulsar.connector.flink;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.pulsar.FlinkPulsarSource;
import org.apache.flink.streaming.util.serialization.PulsarDeserializationSchema;

import java.util.Properties;

/**
 * Pulsar整合flink, 完成让flink从Pulsar中读取消息的操作
 */
public class FlinkFromPulsarSource {

    public static void main(String[] args) throws Exception {

        //1. 创建Flink的流式计算的核心环境类对象
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //2. 添加source数据源, 用于读取数据 : Pulsar
        Properties props = new Properties();
        props.setProperty("topic","persistent://my-tenant/test-namespace/my-topic");

        FlinkPulsarSource<String> pulsarSource = new FlinkPulsarSource<String>(
                "pulsar://node01:6650,node2:6650,node03:6650",
                "http://node01:8080,node02:8080,node03:8080",
                PulsarDeserializationSchema.valueOnly(new SimpleStringSchema()),
                props
        );
        pulsarSource.setStartFromLatest();

        DataStreamSource<String> streamSource = env.addSource(pulsarSource);

        //3. 添加一些转换处理的操作, 对数据进行统计分析

        //4. 添加 sink的组件, 将计算的结果进行输出操作
        streamSource.print();

        //5. 启动flink程序
        env.execute("FinkFromPulsar");


    }

}
