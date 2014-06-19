package net.ihiroky.jcs;

/**
 * The filter to decide the condition class is assignable from the scanned class.
 */
public class InstanceOfFilter implements ClassScanner.Filter<Object> {

    /**
     * The instance of this class.
     */
    public static final InstanceOfFilter INSTANCE = new InstanceOfFilter();

    InstanceOfFilter() {
    }

    @Override
    public boolean matches(Class<?> scannedClass, Class<? extends Object> conditionClass) {
        return !scannedClass.equals(conditionClass) && conditionClass.isAssignableFrom(scannedClass);
    }
}
