package net.xzh.pulsar.consumer;


import org.apache.pulsar.client.api.*;

// 演示 Pulsar的消费者的使用
public class PulsarConsumerTest {

    public static void main(String[] args)  throws Exception{

        //1. 创建pulsar的客户端的对象
        PulsarClient pulsarClient = PulsarClient.builder().serviceUrl("pulsar://node01:6650,node02:6650,node03:6650").build();

        //2. 基于客户端构建消费者对象

        Consumer<String> consumer = pulsarClient.newConsumer(Schema.STRING)
                .topic("persistent://my-tenant/test-namespace/my-topic1")
                .subscriptionName("my-topic1")
                .subscriptionType(SubscriptionType.Exclusive)
                .subscribe();

        //3. 循环从消费者读取数据

        while(true) {
            //3.1: 接收消息
            Message<String> message = consumer.receive();
            //3.2: 获取消息
            String msg = message.getValue();
            //3.3: 处理数据--- 业务操作
            System.out.println("消息数据为:"+msg);
            //3.4: ack确认操作
            consumer.acknowledge(message);
            // 如果消费失败了, 可以采用try catch方式进行捕获异常, 捕获后, 可以进行告知没有消费
            //consumer.negativeAcknowledge(message);
        }



    }
}
