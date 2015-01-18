package in.thiery.docker.registry.web.service;


import com.squareup.okhttp.OkHttpClient;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class RegistryConnectorProvider {

    private HostnameVerifier hostnameVerifier;

    private SSLContext sslContext;

    public RegistryConnectorProvider(TrustManager[] trustManagers, HostnameVerifier hostnameVerifier) {
        if (trustManagers == null) {
            throw new IllegalArgumentException("trustManagers can't be null.");
        }
        if (hostnameVerifier == null) {
            throw new IllegalArgumentException("hostnameVerifier can't be null.");
        }
        this.hostnameVerifier = hostnameVerifier;

        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

    }

    public RegistryConnector provide(String basUrl) {
        if (basUrl == null) {
            throw new IllegalArgumentException("basUrl can't be null.");
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setHostnameVerifier(hostnameVerifier);
        okHttpClient.setSslSocketFactory(sslContext.getSocketFactory());

        OkClient okClient = new OkClient(okHttpClient);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(okClient)
                .setEndpoint(basUrl)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        RegistryConnector connector = restAdapter.create(RegistryConnector.class);
        return connector;
    }
}
