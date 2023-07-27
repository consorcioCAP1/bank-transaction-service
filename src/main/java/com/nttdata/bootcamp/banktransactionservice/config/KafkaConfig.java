package com.nttdata.bootcamp.banktransactionservice.config;

import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;


@Configuration
public class KafkaConfig {

    @Bean
    public KafkaSender<String, String> kafkaSender() {
        SenderOptions<String, String> senderOptions = SenderOptions.<String, String>create()
                .producerProperty("bootstrap.servers", "localhost:29092")
                .producerProperty("key.serializer", StringSerializer.class)
                .producerProperty("value.serializer", StringSerializer.class);
        return KafkaSender.create(senderOptions);
    }

}