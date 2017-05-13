package alexanders.applications.gpprocessor.plugin.impl;

import alexanders.api.gpprocessor.event.IPCEvent;
import alexanders.api.gpprocessor.plugin.*;
import alexanders.applications.gpprocessor.plugin.PluginPermission;
import alexanders.applications.gpprocessor.plugin.PluginSecurityManager;

public class PluginContainerImpl extends PluginContainer
{
    private final PluginMetadata metadata;
    private final LoadState state;
    private final String[] dependencies;
    private final Class<?> pluginClass;
    private final Object instance;
    private final ClassLoader classLoader;

    public PluginContainerImpl(PluginMetadata metadata, LoadState state, String[] dependencies, Class<?> pluginClass, Object instance, ClassLoader classLoader)
    {
        this.metadata = metadata.clone(); // Disallow metadata editing
        this.state = state;
        this.dependencies = dependencies.clone();
        this.pluginClass = pluginClass;
        this.instance = instance;
        this.classLoader = classLoader;
    }

    @Override
    public String getName()
    {
        return metadata.name;
    }

    @Override
    public String getID()
    {
        return metadata.ID;
    }

    @Override
    public LoadState getState()
    {
        return state;
    }

    @Override
    public void sendMessage(String sender, IPCMessage message)
    {
        PluginManager.instance.getMainEventBus().fireEvent(new IPCEvent(message, sender));
    }

    @Override
    public String[] getDependencies()
    {
        return dependencies;
    }

    @Override
    public Class<?> getPluginClass()
    {
        return pluginClass;
    }

    @Override
    public Object getInstance()
    {
        return instance;
    }

    @Override
    public PluginMetadata getMetadata()
    {
        return metadata;
    }

    @Override
    public ClassLoader getClassLoader()
    {
        PluginSecurityManager.checkPermissionS(PluginPermission.canGetPluginLoader, metadata.ID);
        return classLoader;
    }
}
