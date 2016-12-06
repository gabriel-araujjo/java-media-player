package br.imd.mediaplayer.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by gabriel on 02/12/16.
 */
@Retention(SOURCE)
@Target(TYPE)
public @interface Model {

    /**
     * File where the models must be persisted
     * @return
     */
    String value();
}
