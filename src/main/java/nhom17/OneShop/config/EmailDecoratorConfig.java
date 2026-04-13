package nhom17.OneShop.config;

import nhom17.OneShop.service.EmailService;
import nhom17.OneShop.service.decorator.LoggingEmailDecorator;
import nhom17.OneShop.service.decorator.RetryEmailDecorator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class EmailDecoratorConfig {

    @Bean
    @Primary
    public EmailService decoratedEmailService(
        @Qualifier("coreEmailService") EmailService coreEmailService,
        @Value("${app.mail.decorator.max-retries:2}") int maxRetries,
        @Value("${app.mail.decorator.max-send-per-window:5}") int maxSendPerWindow,
        @Value("${app.mail.decorator.limit-window-minutes:15}") long limitWindowMinutes
    ) {
        EmailService chain = new LoggingEmailDecorator(coreEmailService);
        return new RetryEmailDecorator(chain, maxRetries, maxSendPerWindow, limitWindowMinutes);
    }
}

