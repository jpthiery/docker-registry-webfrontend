package in.thiery.docker.registry.web;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.ResponseTransformer;
public class JsonTransformer implements ResponseTransformer {

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    @Override
    public String render(Object model) throws Exception {
        return gson.toJson(model);
    }
}
