package org.jugru.miniJUnit.helpers;import sun.misc.Unsafe;import java.io.File;import java.io.IOException;import java.lang.reflect.Field;import java.net.URI;import java.net.URISyntaxException;import java.net.URL;import java.util.ArrayList;import java.util.Enumeration;import java.util.List;import java.util.Vector;@SuppressWarnings({"unchecked"})public class ClassFinder {    private static Unsafe unsafeHolder = null;    private static Unsafe getUnsafe() throws NoSuchFieldException, IllegalAccessException {        if(unsafeHolder == null) {            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");            theUnsafe.setAccessible(true);            unsafeHolder = (Unsafe) theUnsafe.get(null);        }        return unsafeHolder;    }    public static List<Class> findAllClasses() {        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();        List<Class> classList = new ArrayList<>();        while (classLoader != null) {            classList.addAll(extractClasses(classLoader));            classLoader = classLoader.getParent();        }        return classList;    }    private static List<Class> extractClasses(ClassLoader cl)  {        Class clClass = cl.getClass();        while (clClass != java.lang.ClassLoader.class) {            clClass = clClass.getSuperclass();        }        try {            Field clClassField = clClass.getDeclaredField("classes");            Vector<Class> classes =  (Vector<Class>) getUnsafe().getObject(cl, getUnsafe().objectFieldOffset(clClassField));            return new ArrayList<>(classes);        } catch (IllegalAccessException | NoSuchFieldException e) {            e.printStackTrace();            return new ArrayList<>();        }    }    public static List<Class> findAllClassesInPackage(String packageName) {        try {            return findAllClassesInPackageOrThrow(packageName);        } catch (ClassNotFoundException | IOException | URISyntaxException e) {            e.printStackTrace();        }        return new ArrayList<>();    }    private static List<Class> findAllClassesInPackageOrThrow(String packageName) throws ClassNotFoundException,                                                                                  IOException, URISyntaxException {        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();        String path = packageName.replace('.', '/');        Enumeration<URL> resources = classLoader.getResources(path);        List<File> dirs = new ArrayList<File>();        while (resources.hasMoreElements()) {            URL resource = resources.nextElement();            URI uri = new URI(resource.toString());            dirs.add(new File(uri.getPath()));        }        List<Class> classes = new ArrayList<>();        for (File directory : dirs) {            classes.addAll(findClasses(directory, packageName));        }        return classes;    }    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {        List<Class> classes = new ArrayList<>();        if (!directory.exists()) {            return classes;        }        File[] files = directory.listFiles();        if (files != null) {            for (File file : files)            {                if (file.isDirectory())                {                    classes.addAll(findClasses(file, packageName + "." + file.getName()));                }                else if (file.getName().endsWith(".class"))                {                    classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));                }            }        }        return classes;    }}