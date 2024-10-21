package example.micronaut;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micronaut.function.aws.MicronautRequestHandler;
import jakarta.inject.Inject;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class FunctionRequestHandler
        extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String SMTP_HOST = "email-smtp.ap-northeast-1.amazonaws.com"; // SESのリージョンに合わせて変更
    private static final String SMTP_PORT = "587"; // 465（SSL）も可
    private static final String SMTP_USERNAME = "aaaa"; // SESのSMTPユーザー名
    private static final String SMTP_PASSWORD = "bbbb"; // SESのSMTPパスワード
    private static final String EMAIL = "sample@sample.com";

    @Inject
    ObjectMapper objectMapper;

    @Override
    public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent request) {

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        try {
            String str = run(request);
            response.setStatusCode(200);
            response.setBody(str);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(500);
            response.setBody(new JSONObject().toString());
            return response;
        }
    }

    private String run(APIGatewayProxyRequestEvent request) {
        // 送信先メールリスト（例：500通の宛先）
        List<String> recipients = Arrays.asList(
                EMAIL);

        // スレッドプールの設定（例として10スレッドを使用）
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // SMTPセッションの設定
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);

        // 正しいAuthenticatorクラスのインスタンスを使用
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });

        sendEmail(session, EMAIL);

        //        // 各宛先に対してメール送信を実行
        //        for (String recipient : recipients) {
        //            executor.submit(() -> sendEmail(session, recipient));
        //        }

        // スレッドプールのシャットダウン
        executor.shutdown();

        JSONObject body = new JSONObject();
        body.put("res", "hello");
        return body.toString();
    }

    private static void sendEmail(Session session, String recipient) {
        try {
            // メール内容の設定
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL)); // 送信元のアドレス
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject("Jakarta Mail - AWS SES Bulk Email Test");
            message.setText("Hello! This email is sent using AWS SES SMTP via Jakarta Mail API.");

            // メール送信
            Transport.send(message);
            System.out.println("Email sent successfully to " + recipient);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}