package be.fgov.famhp.autocontrol.pharmacy.proxy.config;

import be.fgov.fagg.common.command.CommandService;
import be.fgov.fagg.common.command.results.CommandResultProvider;
import be.fgov.fagg.common.command.routing.CommandRoutingKeyProvider;
import be.fgov.fagg.common.config.FamhpRabbitMQAutoConfiguration;
import be.fgov.fagg.common.outbox.subscriber.JaversOutboxHandler;
import be.fgov.famhp.autocontrol.pharmacy.proxy.esb.outbox.FormCopyCompletedOutboxHandler;
import be.fgov.famhp.autocontrol.pharmacy.proxy.esb.outbox.InspectionDocumentOutboxHandler;
import be.fgov.famhp.autocontrol.pharmacy.proxy.esb.outbox.InspectionReportOutboxHandler;
import be.fgov.famhp.autocontrol.pharmacy.proxy.esb.outbox.NewFormCompletedOutboxHandler;
import be.fgov.famhp.autocontrol.pharmacy.proxy.esb.outbox.PharmacyDocumentOutboxHandler;
import be.fgov.famhp.autocontrol.pharmacy.proxy.service.FileManager;
import be.fgov.famhp.plato.backoffice.domain.InspectionDocument;
import be.fgov.famhp.plato.backoffice.domain.InspectionReport;
import be.fgov.famhp.plato.backoffice.domain.PharmacyDocument;
import be.fgov.famhp.plato.backoffice.domain.form.Form;
import com.google.common.collect.ImmutableMap;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@Import(FamhpRabbitMQAutoConfiguration.class)
@Configuration
@ConditionalOnProperty("application.rabbit.enabled")
public class RabbitMQConfig {

    @Bean
    CommandService commandService(TopicExchange topicExchange, AsyncRabbitTemplate asyncRabbitTemplate, MongoTemplate mongoTemplate, Jackson2JsonMessageConverter jackson2JsonMessageConverter, CommandRoutingKeyProvider commandRoutingKeyProvider, CommandResultProvider commandResultProvider) {
        return new CommandService(topicExchange,
            asyncRabbitTemplate,
            mongoTemplate,
            jackson2JsonMessageConverter,
            commandRoutingKeyProvider,
            commandResultProvider);
    }

    //TODO remove when faggcommon 4.0.6 is tested (and add outbox enable : true)
    @Bean
    public Declarables qs() {
        String dead_letter = "DEAD_LETTER";
        return new Declarables(
            Stream.of("plato_incoming".split(","))
            .map(elem -> QueueBuilder.durable(elem)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", dead_letter)
                .build()).collect(Collectors.toList()));

    }

    @Bean
    public Declarables bs(TopicExchange topic) {
        String routingKey = "PLATO.OUTBOX.BACKOFFICE";
        Declarables qs = qs();

        return new Declarables(
            qs.getDeclarables().stream()
                .map(queue -> BindingBuilder.bind((Queue) queue).to(topic).with(routingKey))
                .collect(Collectors.toList())
        );

    }

    // outbox handler

    @Bean("pharmacyDocumentOutboxHandler")
    public PharmacyDocumentOutboxHandler pharmacyDocumentOutboxHandler(CommandService commandService, FileManager fileManager) {
        return new PharmacyDocumentOutboxHandler(commandService, fileManager);
    }

    @Bean("inspectionDocumentOutboxHandler")
    public InspectionDocumentOutboxHandler inspectionDocumentOutboxHandler(CommandService commandService, FileManager fileManager) {
        return new InspectionDocumentOutboxHandler(commandService, fileManager);
    }

    @Bean("inspectionReportOutboxHandler")
    public InspectionReportOutboxHandler inspectionReportOutboxHandler(CommandService commandService, FileManager fileManager) {
        return new InspectionReportOutboxHandler(commandService, fileManager);
    }

    @Bean("newFormCompletedOutboxHandler")
    public NewFormCompletedOutboxHandler newFormCompletedOutboxHandler(CommandService commandService) {
        return new NewFormCompletedOutboxHandler(commandService);
    }

    @Bean("formCopyCompletedOutboxHandler")
    public FormCopyCompletedOutboxHandler formCopyCompletedOutboxHandler(CommandService commandService) {
        return new FormCopyCompletedOutboxHandler(commandService);
    }

    // Both document processes auto_to_plato & plato_to_auto are handled by PharmacyDocumentOutboxHandler
    @Bean("javersOutboxHandlerMap")
    public Map<String, JaversOutboxHandler> javersOutboxHandlerMap(CommandService
                                                                       commandService, FileManager fileManager) {
        Map<String, JaversOutboxHandler> result = new HashMap<>();
        result.put(PharmacyDocumentOutboxHandler.class.getName(), pharmacyDocumentOutboxHandler(commandService, fileManager));
        result.put(InspectionDocumentOutboxHandler.class.getName(), inspectionDocumentOutboxHandler(commandService, fileManager));
        result.put(InspectionReportOutboxHandler.class.getName(), inspectionReportOutboxHandler(commandService, fileManager));
        result.put(NewFormCompletedOutboxHandler.class.getName(), newFormCompletedOutboxHandler(commandService));
        result.put(FormCopyCompletedOutboxHandler.class.getName(), formCopyCompletedOutboxHandler(commandService));
        return result;
    }
    @Bean("javersClassMapper")
    public Map<String, Class<?>> javersClassMapper() {
        return Stream.of(PharmacyDocument.class, InspectionDocument.class, InspectionReport.class, Form.class)
            .collect(collectingAndThen(
                toMap(Class::getCanonicalName, Function.identity()),
                ImmutableMap::copyOf));
    }

}
