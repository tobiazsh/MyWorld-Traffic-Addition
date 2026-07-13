package at.tobiazsh.myworld.traffic_addition.preference;

import at.tobiazsh.myworld.traffic_addition.toml.TomlLong;
import at.tobiazsh.myworld.traffic_addition.toml.TomlValue;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("deprecation")
public class PreferenceSetHelper {
    private final PreferenceJsonLoader loader;

    public PreferenceSetHelper(PreferenceJsonLoader loader) {
        this.loader = loader;
    }

    /**
     * Helper method for setting a value on a preference from the old loader method.
     * @param preference The preference to set
     * @param key The key of the old preference
     * @param getter The getter for the preference
     * @param constructor The constructor for the TomlValue
     * @param <P> The primitive type of the value
     * @param <T> The TomlValue type
     */
    public <P, T extends TomlValue<P>> void setPreference(
            @NotNull Preference<T> preference,
            @NotNull String key,
            @NotNull BiFunction<PreferenceJsonLoader, String, P> getter,
            @NotNull Function<P, T> constructor
    ) {
        P value = Objects.requireNonNullElse(
                getter.apply(loader, key),
                preference.getDefault().value()
        );

        preference.set(constructor.apply(value));
    }

    /**
     * Helper method to quickly set a long preference from the old loader method.
     * @param preference The preference to set
     * @param key The key of the old preference
     */
    public void setLongPreference(
            Preference<TomlLong> preference,
            String key
    ) {
        setPreference(
                preference,
                key,
                PreferenceJsonLoader::getLong,
                TomlLong::new
        );
    }
}
