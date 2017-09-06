package demo.squid;

import com.dataman.squid.core.SquidService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.stream.Collectors;

import demo.order.CreateOrderRequest;
import demo.order.Order;

/**
 * @author addozhang 2017/9/4
 */
public class OrderSquidService implements SquidService<CreateOrderRequest, Order> {
    public static final double TAX = .06;
    private OAuth2RestTemplate oAuth2RestTemplate;

    public static final String NAME_URL = "url";
    public static final String NAME_USERNAME = "username";
    public static final String NAME_PASSWORD = "password";
    public static final String NAME_TOKEN_URI = "token_uri";
    public static final String NAME_CLIENT_ID = "client_id";
    public static final String URL = System.getProperty(NAME_URL, System.getenv(NAME_URL));
    public static final String USERNAME = System.getProperty(NAME_USERNAME, System.getenv(NAME_USERNAME));
    public static final String PASSWORD = System.getProperty(NAME_PASSWORD, System.getenv(NAME_PASSWORD));
    public static final String TOKEN_URI = System.getProperty(NAME_TOKEN_URI, System.getenv(NAME_TOKEN_URI));
    public static final String CLIENT_ID = System.getProperty(NAME_CLIENT_ID, System.getenv(NAME_CLIENT_ID));

    private RestTemplate restTemplate = new RestTemplate();

    public OrderSquidService() {
        ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();
        resource.setClientId(CLIENT_ID);
        resource.setClientSecret("acmesecret");
        resource.setUsername(USERNAME);
        resource.setPassword(PASSWORD);
        resource.setAccessTokenUri(TOKEN_URI);
        oAuth2RestTemplate = new OAuth2RestTemplate(resource);
    }

    @Override
    public Order execute(CreateOrderRequest createOrderRequest, long timeout, long deadline) throws InterruptedException {
        Order order = restTemplate.postForObject(URL,
                createOrderRequest.getLineItems().stream()
                        .map(prd ->
                                new demo.order.LineItem(prd.getProduct().getName(),
                                        prd.getProductId(), prd.getQuantity(),
                                        prd.getProduct().getUnitPrice(), TAX))
                        .collect(Collectors.toList()),
                Order.class);
        return order;
    }

    @Override
    public String getServiceName() {
        return "createOrderService";
    }

    @Override
    public String getVersion() {
        return "V1";
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        OrderSquidService orderSquidService = new OrderSquidService();
        String str = "{\"lineItems\":[{\"productId\":\"SKU-24642\",\"product\":{\"id\":0,\"name\":\"Best. Cloud. Ever. (T-Shirt, Men's Large)\",\"productId\":\"SKU-24642\",\"description\":\"<p>Do you love y\n" +
                "our cloud platform? Do you push code continuously into production on a daily basis? Are you living the cloud native microservice dream? Then rain or shine, this T-Shirt is for\n" +
                "you. Show the world you're a stylish cloud platform architect with this cute yet casual tee. <br /><br />&nbsp; <strong>Cloud Native Tee Collection</strong><br />&nbsp; 110% cl\n" +
                "oud stuff, 5% spandex<br />&nbsp; Rain wash only<br />&nbsp; Four nines of " +
                "<em>stylability</em></p>\",\"unitPrice\":21.99},\"quantity\":1}]}";
        CreateOrderRequest createOrderRequest = new ObjectMapper().readValue(str, CreateOrderRequest.class);
        Order order = orderSquidService.execute(createOrderRequest, 0L, 0L);
        System.out.println(order);
    }
}
