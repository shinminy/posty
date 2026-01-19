package com.posty.postingapi.aspect;

import com.posty.postingapi.controller.AccountController;
import com.posty.postingapi.dto.account.AccountDetailResponse;
import com.posty.postingapi.properties.ApiProperties;
import com.posty.postingapi.service.application.AccountService;
import com.posty.postingapi.support.TestSecurityConfig;
import com.posty.postingapi.support.TestTimeConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import({RequestLogger.class, ResponseLogger.class, TestSecurityConfig.class, TestTimeConfig.class, ApiProperties.class})
@EnableAspectJAutoProxy
class LoggingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    private ListAppender listAppender;

    @BeforeEach
    void setup() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        listAppender = new ListAppender("ListAppender");
        listAppender.start();
        config.addAppender(listAppender);
        updateLoggers(config, listAppender);
    }

    @AfterEach
    void tearDown() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        listAppender.stop();
        config.getRootLogger().removeAppender("ListAppender");
        updateLoggers(config, null);
    }

    private void updateLoggers(final Configuration config, final Appender appender) {
        if (appender != null) {
            config.getRootLogger().addAppender(appender, Level.ALL, null);
            config.getLoggers().values().forEach(l -> l.addAppender(appender, Level.ALL, null));
        } else {
            config.getRootLogger().removeAppender("ListAppender");
            config.getLoggers().values().forEach(l -> l.removeAppender("ListAppender"));
        }
    }

    @Test
    @DisplayName("RequestLogger와 ResponseLogger가 로그를 남기는지 확인")
    void loggingTest() throws Exception {
        // given
        Long accountId = 1L;
        AccountDetailResponse response = new AccountDetailResponse(
                accountId, "test@example.com", "tester", "+82-10-1234-5678",
                null, null, null, null, null
        );
        given(accountService.getAccountDetail(accountId)).willReturn(response);

        // when
        mockMvc.perform(get("/accounts/{accountId}", accountId))
                .andExpect(status().isOk());

        // then
        List<LogEvent> events = listAppender.getEvents();
        
        // RequestLogger 검증
        boolean requestLogged = events.stream()
                .anyMatch(event -> event.getMessage().getFormattedMessage().contains("[Request info]"));
        
        // ResponseLogger 검증
        boolean responseLogged = events.stream()
                .anyMatch(event -> event.getMessage().getFormattedMessage().contains("[Response Info]"));

        assertThat(requestLogged).isTrue();
        assertThat(responseLogged).isTrue();
    }

    private static class ListAppender extends AbstractAppender {
        private final List<LogEvent> events = new ArrayList<>();

        protected ListAppender(String name) {
            super(name, null, null, true, null);
        }

        @Override
        public void append(LogEvent event) {
            events.add(event.toImmutable());
        }

        public List<LogEvent> getEvents() {
            return new ArrayList<>(events);
        }
    }
}
