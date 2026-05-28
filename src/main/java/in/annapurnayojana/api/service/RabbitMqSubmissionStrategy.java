package in.annapurnayojana.api.service;

import in.annapurnayojana.api.dto.FormSubmissionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.features.useRabbitMq", havingValue = "true")
public class RabbitMqSubmissionStrategy implements FormSubmissionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqSubmissionStrategy.class);
    private final RabbitTemplate rabbitTemplate;
    private static final String QUEUE_NAME = "form_submissions";

    public RabbitMqSubmissionStrategy(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        logger.info(">>> [Feature] Form submission strategy: RabbitMQ (async)");
    }

    @Override
    public void submit(FormSubmissionPayload payload) {
        rabbitTemplate.convertAndSend(QUEUE_NAME, payload);
        logger.info("[RabbitMq] Payload published for ApplicationId={}", payload.getApplicationId());
    }
}
