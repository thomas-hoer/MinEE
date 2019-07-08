package de.minee.rest.annotation;

import de.minee.rest.parser.Parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Consumes {

	Class<? extends Parser>[] value() default {};

}
