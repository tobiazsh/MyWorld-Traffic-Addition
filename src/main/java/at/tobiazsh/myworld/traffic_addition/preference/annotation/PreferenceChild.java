package at.tobiazsh.myworld.traffic_addition.preference.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreferenceChild {
    String value() default ""; // Optional: specify a custom name for the child class in the configuration
}
