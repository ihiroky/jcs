package net.ihiroky.jcs;

import java.lang.annotation.Annotation;

/**
 * The filter to decide the scanned class is annotated with the condition class.
 */
public class AnnotationFilter implements ClassScanner.Filter<Annotation> {

    /**
     * The instance of this class.
     */
    public static final AnnotationFilter INSTANCE = new AnnotationFilter();

    AnnotationFilter() {
    }

    @Override
    public boolean matches(Class<?> scannedClass, Class<? extends Annotation> conditionClass) {
        return scannedClass.isAnnotationPresent(conditionClass);
    }
}
