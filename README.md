jcs
===

Java class scanner.

## Example

```java
package net.ihiroky.jcs;

import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class ClassScannerTest {

    @Test
    public void testScanAnnotated() throws Exception {
        List<Class<?>> actual = ClassScanner.scanAnnotated(ClassScannerTest.class.getPackage(), SomeAnnotation.class);

        assertThat(actual, is(Arrays.asList(SomeAnnotatedClass.class, SomeAnnotatedClass2.class)));
    }

    @Retention(RetentionPolicy.RUNTIME)
    private @interface SomeAnnotation {
    }

    @SomeAnnotation
    private static class SomeAnnotatedClass {
    }

    @SomeAnnotation
    private static class SomeAnnotatedClass2 {
    }
}
```
