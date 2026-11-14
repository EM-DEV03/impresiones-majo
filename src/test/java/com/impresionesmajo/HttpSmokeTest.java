package com.impresionesmajo;

import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.assertj.core.api.Assertions.*;

/** Servidor real, cookies reales y tokens extraídos del HTML; base efímera independiente. */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={
    "spring.datasource.url=jdbc:h2:mem:impresiones_majo_http_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
    "app.admin.username=http-admin"
})
@ActiveProfiles("test")
class HttpSmokeTest {
    static final String password=UUID.randomUUID().toString();
    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {registry.add("app.admin.password",()->password);}
    @Value("${local.server.port}") int port;
    private final HttpClient client=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).connectTimeout(Duration.ofSeconds(10)).build();
    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+path)).timeout(Duration.ofSeconds(10)).GET().build(),HttpResponse.BodyHandlers.ofString());
    }
    private HttpResponse<String> post(String path,String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+path)).timeout(Duration.ofSeconds(10)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    private String token(String html) {
        var matcher=Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(html);
        assertThat(matcher.find()).as("Token CSRF renderizado en el formulario").isTrue();
        return URLEncoder.encode(matcher.group(1),StandardCharsets.UTF_8);
    }
    @Test void arrancaAutenticaRenderizaYFinalizaSesion() throws Exception {
        assertThat(get("/").statusCode()).isEqualTo(302);
        var login=get("/login");assertThat(login.statusCode()).isEqualTo(200);
        assertThat(login.body()).contains("Impresiones Majo","/images/majo-logo.png");
        assertThat(get("/images/majo-logo.png").statusCode()).isEqualTo(200);
        var incorrecto=post("/login","username=http-admin&password=incorrecta&_csrf="+token(login.body()));
        assertThat(incorrecto.statusCode()).isEqualTo(302);
        assertThat(incorrecto.headers().firstValue("location").orElseThrow()).endsWith("/login?error");
        assertThat(get("/inventario").statusCode()).isEqualTo(302);
        login=get("/login");
        var auth=post("/login","username=http-admin&password="+URLEncoder.encode(password,StandardCharsets.UTF_8)+"&_csrf="+token(login.body()));
        assertThat(auth.statusCode()).isEqualTo(302);assertThat(auth.headers().firstValue("location").orElseThrow()).endsWith("/");
        var dashboard=get("/");assertThat(dashboard.statusCode()).isEqualTo(200);assertThat(dashboard.body()).contains("Panel principal","http-admin");
        for(String path:new String[]{"/ventas/nueva","/compras/nueva","/inventario","/reportes","/usuarios","/css/app.css","/js/app.js","/webjars/bootstrap/5.3.3/css/bootstrap.min.css"})
            assertThat(get(path).statusCode()).as(path).isEqualTo(200);
        assertThat(post("/logout","_csrf="+token(dashboard.body())).statusCode()).isEqualTo(302);
        assertThat(get("/inventario").statusCode()).isEqualTo(302);
    }
}
