    package com.github.tbspoat.ff.client.module;

    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
public @interface Setting {
    String name() default "";
    double min() default 0.0D;
    double max() default 1000.0D;
    double step() default 1.0D;
}
