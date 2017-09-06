package demo.squid;

import com.dataman.squid.core.SquidService;

import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;

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
    public static final String URL = System.getProperty(NAME_URL, System.getenv(NAME_URL));
    public static final String USERNAME = System.getProperty(NAME_USERNAME, System.getenv(NAME_USERNAME));
    public static final String PASSWORD = System.getProperty(NAME_PASSWORD, System.getenv(NAME_PASSWORD));
    public static final String TOKEN_URI = System.getProperty(NAME_TOKEN_URI, System.getenv(NAME_TOKEN_URI));

    public OrderSquidService() {
        ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();
        resource.setUsername(USERNAME);
        resource.setPassword(PASSWORD);
        resource.setAccessTokenUri(TOKEN_URI);
        oAuth2RestTemplate = new OAuth2RestTemplate(resource);
    }

    @Override
    public Order execute(CreateOrderRequest createOrderRequest, long timeout, long deadline) throws InterruptedException {
        Order order = oAuth2RestTemplate.postForObject(URL,
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
}
