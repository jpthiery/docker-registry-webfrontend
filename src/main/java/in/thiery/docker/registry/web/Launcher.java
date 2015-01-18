package in.thiery.docker.registry.web;

import in.thiery.docker.registry.web.model.Configuration;
import in.thiery.docker.registry.web.service.RegistryAggregator;
import in.thiery.docker.registry.web.service.RegistryConnector;
import in.thiery.docker.registry.web.service.RegistryConnectorProvider;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Route;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.*;

import static org.apache.commons.cli.OptionBuilder.*;
import static spark.Spark.*;
import static spark.SparkBase.setPort;

public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    private static final String JSON_MIME_TYPE = "application/json";

    public static final JsonTransformer JSON_TRANSFORMER = new JsonTransformer();

    private final int port;

    private final Configuration configuration;

    private RegistryAggregator registryAggregator;

    public Launcher(int port, Set<String> registries, RegistryAggregator registryAggregator) {
        if (registries == null) {
            throw new IllegalArgumentException("registries can't be null.");
        }
        if (registries.size() < 1) {
            throw new IllegalArgumentException("registries must contain at least one element.");
        }
        this.port = port;
        this.configuration = new Configuration(1,0,0,registries);
        this.registryAggregator = registryAggregator;
    }

    public void run() {

        setPort(port);
        staticFileLocation("/webapp");


        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "*");
            response.header("Access-Control-Allow-Headers", "*");
        });

        get("/api", (request, response) -> "1.0.0");
        get("/api/configuration", JSON_MIME_TYPE, (request, response) -> configuration, JSON_TRANSFORMER);
        get("/api/configuration/version", JSON_MIME_TYPE, (request, response) -> String.format("%s.%s.%s", configuration.getMajorVersion(), configuration.getMinorVersion(), configuration.getPatchVersion()), JSON_TRANSFORMER);

        get("/api/search", JSON_MIME_TYPE,  (request, response) -> registryAggregator.search(request.queryParams("q")));
        Route imageRoute = (request, response) -> {
            String tagName = StringUtils.join(request.splat(), "/");
            System.out.println(tagName);
            return registryAggregator.getImagesInformations(tagName);

        };
        get("/api/image/*/*", JSON_MIME_TYPE, imageRoute);
        get("/api/image/*", JSON_MIME_TYPE, imageRoute);
        before("/api/*", (request, response) -> response.type(JSON_MIME_TYPE));
    }

    public static void main(String[] args) {
        Options options = new Options();
        Option portOption = hasArg().withDescription("The listing port of server").isRequired().withLongOpt("port").create("p");
        Option registryOption = hasArg().withDescription("List of registry url separate by ',' character.").isRequired().withLongOpt("registry").create("r");


        options.addOption(portOption);
        options.addOption(registryOption);

        CommandLineParser parser = new BasicParser();
        try {
            CommandLine line = parser.parse(options, args);
            int port = Integer.parseInt(line.getOptionValue("port"));
            String registryStr = line.getOptionValue("registry");

            Map<String, RegistryConnector> connectors = new HashMap<>();
            Set<String> registries = new HashSet<>(Arrays.asList(registryStr.split(",")));
            RegistryConnectorProvider provider = new RegistryConnectorProvider(new TrustManager[]{new TrustAllX509TrustManager()}, (hostname, verifier) -> true);

            registries.stream().forEach(url -> connectors.put(url, provider.provide(url)));
            RegistryAggregator registryAggregator = new RegistryAggregator(connectors);

            Launcher launcher = new Launcher(port, registries, registryAggregator);
            launcher.run();
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar server.jar", options);
        }
    }


    static class TrustAllX509TrustManager implements X509TrustManager {
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
