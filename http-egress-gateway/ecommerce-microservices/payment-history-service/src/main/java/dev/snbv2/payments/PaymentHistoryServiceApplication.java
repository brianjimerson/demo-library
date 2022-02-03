package dev.snbv2.payments;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PaymentHistoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentHistoryServiceApplication.class, args);
	}

	@Bean
	Queue queue() {
		return new Queue("payments");
	}

	@Bean
	MessageListenerAdapter listnerAdapter(PaymentMessageReceiver receiver) {
		return new MessageListenerAdapter(receiver, "receiveMessage");
	}

	@Bean
	SimpleMessageListenerContainer container(
		ConnectionFactory connectionFactory,
		MessageListenerAdapter listenerAdapter) {
		
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames("payments");
		container.setMessageListener(listenerAdapter);
		return container;
	}

}
