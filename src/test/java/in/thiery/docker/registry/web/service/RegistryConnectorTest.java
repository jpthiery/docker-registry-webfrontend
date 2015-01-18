package in.thiery.docker.registry.web.service;

import com.google.gson.*;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import retrofit.client.Response;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;

@Ignore
//Running test class. not able to run them in CI.
public class RegistryConnectorTest {
    @Test
    public void testSearch() throws Exception {

        RegistryConnectorProvider provider = new RegistryConnectorProvider(new TrustManager[]{new TrustAllX509TrustManager()}, (hostname, verifier) -> true);
        RegistryConnector connector = provider.provide("https://gringer.socrate.vsct.fr:50000");
        Response results = connector.search("");

        System.out.println("Results : " + IOUtils.toString(results.getBody().in()));

        Response tagRes = connector.tag("vsct/puppetmasterrhel5");
        String tag = IOUtils.toString(tagRes.getBody().in());
        System.out.println("Tag for centos6 : " + tag);

        Response informations = connector.images(tag.replaceAll("\"", ""));
        System.out.println(IOUtils.toString(informations.getBody().in()));

    }
    @Test
    public void test() {
        RegistryConnectorProvider provider = new RegistryConnectorProvider(new TrustManager[]{new TrustAllX509TrustManager()}, (hostname, verifier) -> true);
        RegistryConnector connector = provider.provide("https://gringer.socrate.vsct.fr:50000");

        HashMap<String, RegistryConnector> connectors = new HashMap<>();
        connectors.put("gringer", connector);
        RegistryAggregator agregator = new RegistryAggregator(connectors);

        List<String> result = agregator.search("");
        JsonParser parser = new JsonParser();
        System.out.println(result);
        result.stream().forEach(imageJson -> {
            JsonArray images = parser.parse(imageJson).getAsJsonObject().getAsJsonArray("results");
            images.forEach(image -> {
                String name = image.getAsJsonObject().get("name").getAsString();
                System.out.println(agregator.getImagesInformations(name));
            });
        });

    }

    class TrustAllX509TrustManager implements X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
                                       String authType) {
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
                                       String authType) {
        }

    }
}