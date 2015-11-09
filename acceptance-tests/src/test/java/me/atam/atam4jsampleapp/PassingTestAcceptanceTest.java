package me.atam.atam4jsampleapp;

import jdk.nashorn.internal.ir.annotations.Ignore;
import me.atam.atam4j.PollingPredicate;
import me.atam.atam4j.dummytests.PassingTest;
import me.atam.atam4j.health.AcceptanceTestsHealthCheck;
import me.atam.atam4jdomain.TestsRunResult;
import me.atam.atam4jsampleapp.testsupport.AcceptanceTest;
import me.atam.atam4jsampleapp.testsupport.Atam4jApplicationStarter;
import me.atam.atam4jsampleapp.testsupport.HealthCheckResponseChecker;
import me.atam.atam4jsampleapp.testsupport.HealthCheckResult;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class PassingTestAcceptanceTest extends AcceptanceTest {

    public static final int MAX_ATTEMPTS = 2000;
    public static final int RETRY_POLL_INTERVAL = 1;
    public static final int TEN_SECONDS_IN_MILLIS = 10000;



    @Test
    public void givenSampleApplicationStartedWithPassingTest_whenHealthCheckCalledBeforeTestRun_thenTooEarlyMessageReceived(){
        applicationConfigurationDropwizardTestSupport = Atam4jApplicationStarter.startApplicationWith(PassingTest.class, TEN_SECONDS_IN_MILLIS);
        TestsRunResult testRunResultFromServer = getTestRunResultFromServer();
        assertThat(testRunResultFromServer.getStatus(), is(TestsRunResult.Status.TOO_EARLY));
    }

    @Test
    @Ignore
    public void givenSampleApplicationStartedWithPassingTest_whenHealthCheckCalledAfterTestRUn_thenOKMessageReceived(){

        applicationConfigurationDropwizardTestSupport = Atam4jApplicationStarter.startApplicationWith(PassingTest.class, 0);

        PollingPredicate<Response> responsePollingPredicate = new PollingPredicate<>(
                MAX_ATTEMPTS,
                RETRY_POLL_INTERVAL,
                response -> response.readEntity(HealthCheckResult.class)
                        .getAcceptanceTestsHealthCheckResult()
                        .getMessage()
                        .equals(AcceptanceTestsHealthCheck.OK_MESSAGE),
                this::getResponseFromTestsEndpoint);

        responsePollingPredicate.pollUntilPassedOrMaxAttemptsExceeded();
        new HealthCheckResponseChecker(getResponseFromTestsEndpoint()).checkResponseIsOKAndWithMessage(AcceptanceTestsHealthCheck.OK_MESSAGE);
    }

}
