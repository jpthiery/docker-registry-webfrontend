package in.thiery.docker.registry.web.service;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Exchange interface which describe how to communicate with Docker regsitry.
 */
public interface RegistryConnector {

    @GET("/v1/search")
    Response search(@Query("q") String query);

    @GET("/v1/repositories/{name}/tags/latest")
    Response tag(@Path("name") String imageName);

    @GET("/v1/images/{tag}/json")
    Response images(@Path("tag") String tag);

}
