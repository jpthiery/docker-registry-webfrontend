package in.thiery.docker.registry.web.model;

import java.io.Serializable;
import java.util.Set;

public class Configuration implements Serializable {

    private final int majorVersion;

    private final int minorVersion;

    private final int patchVersion;

    private final Set<String> registries;

    public Configuration(int majorVersion, int minorVersion, int patchVersion, Set<String> registries) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
        this.registries = registries;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public int getPatchVersion() {
        return patchVersion;
    }

    public Set<String> getRegistries() {
        return registries;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "majorVersion=" + majorVersion +
                ", minorVersion=" + minorVersion +
                ", patchVersion=" + patchVersion +
                ", registries=" + registries +
                '}';
    }
}
