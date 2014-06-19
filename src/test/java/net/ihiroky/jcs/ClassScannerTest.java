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
    public void testScanList() throws Exception {
        List<String> classNameList = Arrays.asList(
                "net.ihiroky.jcs.ClassScannerTest",
                "net.ihiroky.jcs.ClassScannerTest$SomeAnnotatedClass");

        List<Class<?>> actual = ClassScanner.scan(classNameList, SomeAnnotation.class,
                AnnotationFilter.INSTANCE, 1, Thread.currentThread().getContextClassLoader());

        assertThat(actual.get(0).getName(), is(SomeAnnotatedClass.class.getName()));
        assertThat(actual.size(), is(1));
    }

    @Test
    public void testScanListFirstOne() throws Exception {
        List<String> classNameList = Arrays.asList(
                "net.ihiroky.jcs.ClassScannerTest",
                "net.ihiroky.jcs.ClassScannerTest$SomeAnnotatedClass",
                "net.ihiroky.jcs.ClassScannerTest$SomeAnnotatedClass2");

        List<Class<?>> actual = ClassScanner.scan(classNameList, SomeAnnotation.class,
                AnnotationFilter.INSTANCE, 1, Thread.currentThread().getContextClassLoader());

        assertThat(actual.get(0).getName(), is(SomeAnnotatedClass.class.getName()));
        assertThat(actual.size(), is(1));
    }

    @Test
    public void testScanListMultiple() throws Exception {
        List<String> classNameList = Arrays.asList(
                "net.ihiroky.jcs.ClassScannerTest",
                "net.ihiroky.jcs.ClassScannerTest$SomeAnnotatedClass",
                "net.ihiroky.jcs.ClassScannerTest$SomeAnnotatedClass2");

        List<Class<?>> actual = ClassScanner.scan(classNameList, SomeAnnotation.class,
                AnnotationFilter.INSTANCE, Integer.MAX_VALUE, Thread.currentThread().getContextClassLoader());

        assertThat(actual.get(0).getName(), is(SomeAnnotatedClass.class.getName()));
        assertThat(actual.get(1).getName(), is(SomeAnnotatedClass2.class.getName()));
        assertThat(actual.size(), is(2));
    }

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
