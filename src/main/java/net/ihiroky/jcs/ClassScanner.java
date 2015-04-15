package net.ihiroky.jcs;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;

/**
 *
 */
public class ClassScanner {

    private static final String PROTOCOL_FILE = "file";
    private static final String PROTOCOL_JAR = "jar";
    private static final String CLASS_FILE_SUFFIX = ".class";
    private static final Pattern BACK_SLASH_PATTERN = Pattern.compile("\\\\");
    private static final Pattern PATH_SEPARATOR_PATTERN = Pattern.compile("[\\\\/]");

    /**
     * Scans classes which annotated with the condition class in the root.
     * The invocation of this method behaves in exactly the same way as the invocation
     * <code>
     *     ClassScanner.Filter filter = AnnotationFilter.INSTANCE;
     *     ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     *     scan(root, conditionClass, filter, Integer.MAX_COUNT, classLoader)
     * </code>
     *
     * @param root the root package
     * @param conditionClass the annotation class
     * @return the scanned class which is annotated with the condition class
     * @throws IOException if I/O error occurs
     * @throws java.lang.IllegalStateException if unsupported kind of the classpath is used
     */
    public static List<Class<?>> scanAnnotated(Package root, Class<? extends Annotation> conditionClass)
            throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return scan(root, conditionClass, AnnotationFilter.INSTANCE, Integer.MAX_VALUE, classLoader);
    }

    /**
     * Scans classes which annotated with the condition class in the root.
     * The invocation of this method behaves in exactly the same way as the invocation
     * <code>
     *     ClassScanner.Filter filter = AnnotationFilter.INSTANCE;
     *     ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     *     scan(root, conditionClass, filter, maxCount, classLoader)
     * </code>
     *
     * @param root the root package
     * @param conditionClass the annotation class
     * @param maxCount the maximum count of the result
     * @return the scanned class which is annotated with the condition class
     * @throws IOException if I/O error occurs
     * @throws java.lang.IllegalStateException if unsupported kind of the classpath is used
     */
    public static List<Class<?>> scanAnnotated(Package root, Class<? extends Annotation> conditionClass, int maxCount)
            throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return scan(root, conditionClass, AnnotationFilter.INSTANCE, maxCount, classLoader);
    }

    /**
     * Scans classes which is instance of the condition class in the root.
     * The invocation of this method behaves in exactly the same way as the invocation
     * <code>
     *     ClassScanner.Filter filter = InstanceOfFilter.INSTANCE;
     *     ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     *     scan(root, conditionClass, filter, Integer.MAX_COUNT, classLoader)
     * </code>
     *
     * @param root the root package
     * @param conditionClass the condition (parent) class
     * @return the scanned class which is the instance of the condition class
     * @throws IOException if I/O error occurs
     * @throws java.lang.IllegalStateException if unsupported kind of the classpath is used
     */
    public static List<Class<?>> scanInstanceOf(Package root, Class<?> conditionClass)
            throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return scan(root, conditionClass, InstanceOfFilter.INSTANCE, Integer.MAX_VALUE, classLoader);
    }

    /**
     * Scans classes which is instance of the condition class in the root.
     * The invocation of this method behaves in exactly the same way as the invocation
     * <code>
     *     ClassScanner.Filter filter = InstanceOfFilter.INSTANCE;
     *     ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     *     scan(root, conditionClass, filter, maxCount, classLoader)
     * </code>
     *
     * @param root the root package
     * @param conditionClass the condition (parent) class
     * @param maxCount the maximum count of the result
     * @return the scanned class which is the instance of the condition class
     * @throws IOException if I/O error occurs
     * @throws java.lang.IllegalStateException if unsupported kind of the classpath is used
     */
    public static List<Class<?>> scanInstanceOf(Package root, Class<?> conditionClass, int maxCount)
            throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return scan(root, conditionClass, InstanceOfFilter.INSTANCE, maxCount, classLoader);
    }

    /**
     * Scans classes which is matched the condition class by the filter in the root.
     *
     * @param root the root package
     * @param conditionClass the condition class
     * @param filter the filter to check the classes in the root is matched the condition class
     * @param maxCount the maximum count of the result
     * @param <T> the type of the type parameter of the condition class
     * @return the scanned classes
     * @throws IOException if I/O error occurs
     * @throws java.lang.IllegalStateException if unsupported kind of the classpath is used
     */
    public static <T> List<Class<?>> scan(Package root, Class<T> conditionClass,
            Filter<T> filter, int maxCount) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return scan(root, conditionClass, filter, maxCount, classLoader);
    }

    /**
     * Scans classes which matches a condition class and a filter in a root package.
     *
     * The range of the scan is in a jar file or directory which contains the root only.
     *
     * @param root the root package
     * @param conditionClass the condition class
     * @param filter the filter to decide scanned classes is matched the condition class
     * @param maxCount the maximum count of the result
     * @param classLoader the class loader to load the result classes
     * @param <T> the type of the type parameter of the condition class
     * @return the scanned classes
     * @throws IOException if I/O error occurs
     * @throws java.lang.IllegalStateException if unsupported kind of the classpath is used
     */
    public static <T> List<Class<?>> scan(Package root, Class<? extends T> conditionClass,
            Filter<T> filter, int maxCount, ClassLoader classLoader) throws IOException {

        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(conditionClass, "conditionClass");
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(classLoader, "classLoader");

        String rootPath = root.getName().replaceAll("\\.", "/");
        URL rootUrl = classLoader.getResource(rootPath);
        String protocol = rootUrl.getProtocol();

        if (PROTOCOL_FILE.equals(protocol)) {
            String file = rootUrl.getFile();
            String base = file.substring(0, file.length() - rootPath.length());
            Path basePath = Paths.get(base);
            List<String> classNameList = new ArrayList<>();
            Files.walkFileTree(basePath, new ClassFileVisitor(classNameList, maxCount, base.length()));
            return scan(classNameList, conditionClass, filter, maxCount, classLoader);
        }

        if (PROTOCOL_JAR.equals(protocol)) {
            List<String> classNameList = new ArrayList<String>();
            JarInputStream in = null;
            try {
                URL fileUrl = new URL(rootUrl.getFile());
                String jarFilePath = fileUrl.getFile().split("!")[0];
                in = new JarInputStream(new BufferedInputStream(new FileInputStream(jarFilePath)));
                for (;;) {
                    JarEntry entry = in.getNextJarEntry();
                    if (entry == null) {
                        break;
                    }
                    String entryName = BACK_SLASH_PATTERN.matcher(entry.getName()).replaceAll("/");
                    if (entryName.startsWith(rootPath) && entryName.endsWith(CLASS_FILE_SUFFIX)) {
                        String dotted = PATH_SEPARATOR_PATTERN.matcher(entryName).replaceAll(".");
                        String className = dotted.substring(0, dotted.length() - CLASS_FILE_SUFFIX.length());
                        classNameList.add(className);
                    }
                }
            } catch (IOException ignored) {
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return scan(classNameList, conditionClass, filter, maxCount, classLoader);
        }

        throw new IllegalStateException("Unsupported resource protocol: " + protocol + ". "
                + "The " + PROTOCOL_FILE + " and " + PROTOCOL_JAR + " are supported.");
    }

    static <T> List<Class<?>> scan(
            List<String> classNameList, Class<? extends T> conditionClass,
            Filter<T> filter, int maxCount, ClassLoader classLoader) throws IOException {

        if (maxCount <= 0) {
            return Collections.emptyList();
        }

        List<Class<?>> resultList = new ArrayList<>(maxCount <= 10 ? maxCount : 10);
        try {
            @SuppressWarnings("unchecked")
            Class<? extends T> cc = (Class<? extends T>) classLoader.loadClass(conditionClass.getName());
            for (String className : classNameList) {
                Class<?> sc = classLoader.loadClass(className);
                if (filter.matches(sc, cc)) {
                    Class<?> annotatedClass = Class.forName(sc.getName(), true, classLoader);
                    resultList.add(annotatedClass);
                    if (resultList.size() == maxCount) {
                        return resultList;
                    }
                }
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new AssertionError("Unexpected error.", e);
        }

        return resultList;
    }

    private static class ClassFileVisitor extends SimpleFileVisitor<Path> {

        final List<String> classNameList_;
        final int maxCount_;
        final int baseLength_;

        ClassFileVisitor(List<String> classNameList, int maxCount, int baseLength) {
            classNameList_ = classNameList;
            maxCount_ = maxCount;
            baseLength_ = baseLength;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) {
            String absPath = path.toAbsolutePath().toString();
            if (!absPath.endsWith(CLASS_FILE_SUFFIX)) {
                return FileVisitResult.CONTINUE;
            }

            List<String> classNameList = classNameList_;

            String c = absPath.substring(baseLength_, absPath.length() - CLASS_FILE_SUFFIX.length());
            classNameList.add(PATH_SEPARATOR_PATTERN.matcher(c).replaceAll("."));
            return (classNameList.size() < maxCount_) ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
        }
    }

    /**
     * The filter to decide the scanned class is matched the condition class.
     *
     * @param <T> the type of the condition class
     */
    public interface Filter<T> {
        boolean matches(Class<?> scannedClass, Class<? extends T> conditionClass);
    }

}
