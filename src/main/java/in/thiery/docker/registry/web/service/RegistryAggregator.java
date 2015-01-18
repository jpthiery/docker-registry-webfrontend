package in.thiery.docker.registry.web.service;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import retrofit.client.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Aggregate call to Docker registries.
 */
public class RegistryAggregator {

    private static final Logger LOGGER = getLogger(RegistryAggregator.class);

    private final Map<String , RegistryConnector> connectors;

    public RegistryAggregator(Map<String, RegistryConnector> connectors) {
        this.connectors = connectors;
    }

    /**
     * Search image by name criteria.
     * @param term The name we looking for.
     * @return A list of result aggregate about the search done.
     */
    public List<String> search(String term) {
        Callback callback = c -> Optional.of(c.search(term));
        return template(callback);
    }

    /**
     *
     * @param name
     * @return
     */
    public List<String> getImagesInformations(String name) {
        List<String> res = new ArrayList<>();

        for (Map.Entry<String, RegistryConnector> entry : connectors.entrySet()) {
            RegistryConnector connector = entry.getValue();
            try {
                Response tagResponse = connector.tag(name);

                Optional.of(extractBody(tagResponse).replaceAll("\"", "")).ifPresent(tag -> {
                    try {
                        String details = extractBody(connector.images(tag));
                        res.add(String.format("{\"registryUrl\":\"%s\", \"response\": %s}",entry.getKey(), details));
                    } catch (IOException e) {
                        LOGGER.error("Unable to get Image details for " + name);
                    }
                });
            } catch (IOException e) {
                LOGGER.error("Unable to get tag for image {}.", name);
            }
        }

        return res;
    }


    private List<String> template(Callback callback) {
        List<String> res = new ArrayList<>();
        for (Map.Entry<String, RegistryConnector> entry : connectors.entrySet()) {
            try {
                Optional<Response> result = callback.execute(entry.getValue());
                if (result.isPresent()) {
                    String body = IOUtils.toString(result.get().getBody().in());
                    res.add(String.format("{\"registryUrl\":\"%s\", \"response\": %s}",entry.getKey(), body));
                }
            } catch (IOException e) {
                LOGGER.error(String.format("Unable to execute the request on %s",entry.getKey()), e);
            }
        }
        return res;
    }

    private interface Callback {
        Optional<Response> execute(RegistryConnector connector) throws IOException;
    }

    private static String extractBody(Response response) throws IOException{
        if (response == null) {
            throw new IllegalArgumentException("response can't be null.");
        }
        return IOUtils.toString(response.getBody().in());
    }

}
