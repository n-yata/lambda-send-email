package example.micronaut;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
public class FunctionRequestHandlerTest {

    @Inject
    private static FunctionRequestHandler handler;

    @BeforeAll
    public static void setupSpec() {
        handler = new FunctionRequestHandler();
    }

    @AfterAll
    public static void cleanupSpec() {
        handler.getApplicationContext().close();
    }

    @Test
    public void testHandler() {
        /* 引数設定 */
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();

        /* 呼び出し */
        APIGatewayProxyResponseEvent response = handler.execute(request);

        /* 判定 */
        System.out.println(response.getBody());
        assertEquals(200, response.getStatusCode().intValue());
    }
}
