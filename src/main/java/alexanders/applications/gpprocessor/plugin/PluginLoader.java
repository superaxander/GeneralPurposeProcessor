package alexanders.applications.gpprocessor.plugin;

import alexanders.api.gpprocessor.Container;
import alexanders.api.gpprocessor.MapUtil;
import alexanders.api.gpprocessor.Triplet;
import alexanders.api.gpprocessor.annotation.AutoPopulate;
import alexanders.api.gpprocessor.annotation.AutoPopulationType;
import alexanders.api.gpprocessor.annotation.EventHandler;
import alexanders.api.gpprocessor.annotation.Plugin;
import alexanders.api.gpprocessor.capability.Capability;
import alexanders.api.gpprocessor.event.GPPEvent;
import alexanders.api.gpprocessor.event.InitializationEvent;
import alexanders.api.gpprocessor.event.PostInitializationEvent;
import alexanders.api.gpprocessor.event.PreInitializationEvent;
import alexanders.api.gpprocessor.plugin.LoadState;
import alexanders.api.gpprocessor.plugin.PluginContainer;
import alexanders.api.gpprocessor.plugin.PluginManager;
import alexanders.api.gpprocessor.plugin.PluginMetadata;
import alexanders.applications.gpprocessor.plugin.impl.PluginContainerImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class PluginLoader
{
    public final ArrayList<PluginContainer> plugins;
    private final Map<String, List<Triplet<Field, Object, AutoPopulate>>> autoPopulateMap;

    public PluginLoader()
    {
        plugins = new ArrayList<>();
        autoPopulateMap = new HashMap<>();
    }

    public PluginContainer getPluginByID(String pluginID)
    {
        Container<PluginContainer> found = new Container<>();
        plugins.forEach(pluginContainer ->
                        {
                            if (pluginContainer.getID().equals(pluginID))
                                found.value = pluginContainer;
                        });
        return found.value;
    }

    public Map<String, LoadState> getLoadStates()
    {
        Map<String, LoadState> map = new HashMap<>(plugins.size());
        plugins.forEach(pluginContainer -> map.put(pluginContainer.getID(), pluginContainer.getState()));
        return map;
    }

    public void autoPopulateInitial()
    {
        autoPopulateMap.forEach((pluginID, fields) ->
                                {
                                    fields.forEach(triplet -> triplet.forEach((field, object, annotation) ->
                                                                              {
                                                                                  try
                                                                                  {
                                                                                      switch (annotation.populationType())
                                                                                      {
                                                                                          case PLUGIN_CONTAINER:
                                                                                              field.set(object, getPluginByID(pluginID));
                                                                                              break;
                                                                                          case INSTANCE:
                                                                                              field.set(object, getPluginByID(pluginID).getInstance());
                                                                                              break;
                                                                                          case METADATA:
                                                                                              field.set(object, getPluginByID(pluginID).getMetadata());
                                                                                              break;
                                                                                          case AUTO_DETECT:
                                                                                              throw new RuntimeException("This is not supposed to happen please report!");
                                                                                      }
                                                                                  } catch (IllegalAccessException e)
                                                                                  {
                                                                                      throw new RuntimeException(e);
                                                                                  }
                                                                              }));
                                });
    }

    public void autoPopulateCapabilities(Capability capability)
    {

    }

    public void preInitialize()
    {
        plugins.forEach(container -> PluginManager.instance.getMainEventBus().fireEvent(new PreInitializationEvent(Logger.getLogger(container.getID()), new File("./config/" + container.getID()), new File("./config/" + container.getID() + ".cfg"), container.getMetadata(), getLoadStates())));
    }

    public void initialize()
    {
        plugins.forEach(container -> PluginManager.instance.getMainEventBus().fireEvent(new InitializationEvent(getLoadStates())));
    }

    public void postInitialize()
    {
        plugins.forEach(container -> PluginManager.instance.getMainEventBus().fireEvent(new PostInitializationEvent(getLoadStates())));

    }

    public void loadPlugins(File pluginDirectory)
    {
        try
        {
            if (!pluginDirectory.exists() || !pluginDirectory.isDirectory())
            {
                throw new FileNotFoundException("Specified plugin directory: " + pluginDirectory.toString() + " not found");
            }

            File[] files = pluginDirectory.listFiles((dir, name) -> name.endsWith(".jar")); // Only load .jar files
            if (files == null)
            {
                throw new FileNotFoundException("Specified plugin directory: " + pluginDirectory.toString() + " contains no plugins");
            }

            for (File file : files)
            {
                loadPlugin(file);
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void loadPlugin(File file) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        String abs_path = file.getAbsolutePath();
        URLClassLoader loader = URLClassLoader.newInstance(new URL[]{new URL("jar:file:" + abs_path + "!/")});
        JarFile jarFile = new JarFile(abs_path, false);
        Enumeration<JarEntry> jarEntries = jarFile.entries();

        while (jarEntries.hasMoreElements())
        {
            JarEntry entry = jarEntries.nextElement();
            String entryName = entry.getName();
            if (entry.isDirectory() || !entryName.endsWith(".class")) // Skip directories and non-source files
                continue;

            // Load class
            String className = entryName.substring(0, entry.getName().length() - 6); // Get rid of .class
            className = className.replaceAll("/", ".");
            Class c = loader.loadClass(className);
            Object instance = null;
            PluginContainerImpl pluginFound = null;
            boolean acceptingIPCMessages = false;

            // Find and act upon annotations on the class
            Annotation[] annotations = c.getAnnotations();
            for (Annotation annotation : annotations)
            {
                if (annotation instanceof Plugin)
                {
                    Plugin pluginAnnotation = (Plugin) annotation;
                    String[] dependencies = pluginAnnotation.dependencies();
                    PluginMetadata metadata = new PluginMetadata();
                    metadata.author = pluginAnnotation.author();
                    metadata.description = pluginAnnotation.description();
                    metadata.ID = pluginAnnotation.ID();
                    metadata.version = pluginAnnotation.version();
                    metadata.name = pluginAnnotation.name();
                    instance = c.newInstance();
                    pluginFound = new PluginContainerImpl(metadata, LoadState.PRE_INIT, dependencies, c, instance, loader);
                    plugins.add(pluginFound);
                }
            }

            // Find and act upon annotations on the class's methods
            Method[] methods = c.getMethods();
            for (Method method : methods)
            {
                annotations = method.getAnnotations();
                for (Annotation annotation : annotations)
                {
                    if (pluginFound != null && annotation instanceof EventHandler)
                    {
                        EventHandler eventHandler = (EventHandler) annotation;
                        PluginManager.instance.getMainEventBus().register(pluginFound.getID(), pluginFound.getInstance(), method);
                    }
                }
            }

            // Find an act upon annotations on the class's fields
            Field[] fields = c.getFields();
            for (Field field : fields)
            {
                annotations = field.getAnnotations();
                for (Annotation annotation : annotations)
                {
                    if (annotation instanceof AutoPopulate)
                    {
                        boolean isStatic = Modifier.isStatic(field.getModifiers());
                        if (instance == null && !isStatic)
                            instance = c.newInstance();
                        AutoPopulate autoPopulate = (AutoPopulate) annotation;
                        if (autoPopulate.populationType() == AutoPopulationType.AUTO_DETECT && Modifier.isStatic(field.getModifiers()) || field.isAccessible())
                        {
                            if (pluginFound != null && field.getType().isAssignableFrom(pluginFound.getPluginClass())) // TODO: Make this work outside of the plugins main class

                                MapUtil.addToEmbeddedList(autoPopulateMap, autoPopulate.pluginID(), new Triplet<>(field, instance, AnnotationFactory.buildAutoPopulate(AutoPopulationType.INSTANCE, autoPopulate.pluginID(), autoPopulate.injectedClass())));
                            else if (field.getType().isAssignableFrom(Capability.class))
                                MapUtil.addToEmbeddedList(autoPopulateMap, autoPopulate.pluginID(), new Triplet<>(field, instance, AnnotationFactory.buildAutoPopulate(AutoPopulationType.CAPABILITY, autoPopulate.pluginID(), autoPopulate.injectedClass())));
                            else if (field.getType().isAssignableFrom(PluginContainer.class))
                                MapUtil.addToEmbeddedList(autoPopulateMap, autoPopulate.pluginID(), new Triplet<>(field, instance, AnnotationFactory.buildAutoPopulate(AutoPopulationType.PLUGIN_CONTAINER, autoPopulate.pluginID(), autoPopulate.injectedClass())));
                            else if (field.getType().isAssignableFrom(PluginMetadata.class))
                                MapUtil.addToEmbeddedList(autoPopulateMap, autoPopulate.pluginID(), new Triplet<>(field, instance, AnnotationFactory.buildAutoPopulate(AutoPopulationType.METADATA, autoPopulate.pluginID(), autoPopulate.injectedClass())));
                        } else
                            MapUtil.addToEmbeddedList(autoPopulateMap, autoPopulate.pluginID(), new Triplet<>(field, instance, autoPopulate));
                    }
                }
            }
        }
    }

    public static HashMap<String, List<Method>> loadEventHandlersFromClass(Class<?> eventHandlerClass) throws ClassCastException, IllegalAccessException, InstantiationException, MalformedParametersException
    {
        HashMap<String, List<Method>> foundEventHandlers = new HashMap<>();
        Method[] methods = eventHandlerClass.getMethods();
        for (Method method : methods)
        {
            EventHandler annotation = method.getAnnotation(EventHandler.class);
            if (annotation != null)
            {

                MapUtil.addToEmbeddedList(foundEventHandlers, getEventFromMethod(method).getType(), method);
            }
        }
        return foundEventHandlers;
    }

    public static GPPEvent getEventFromMethod(Method method) throws IllegalAccessException, InstantiationException
    {
        Parameter[] parameters = method.getParameters();
        if (parameters.length != 1)
            throw new MalformedParametersException("Method: " + method.getName() + "in class: " + method.getDeclaringClass().getName() + " has too many parameters! Found: " + parameters.length + " required: 1");
        Class<? extends GPPEvent> eventClass = parameters[0].getType().asSubclass(GPPEvent.class);
        return eventClass.newInstance();
    }
}
