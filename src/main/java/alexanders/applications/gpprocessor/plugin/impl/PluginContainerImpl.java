package alexanders.applications.gpprocessor.plugin.impl;

import alexanders.api.gpprocessor.event.IPCEvent;
import alexanders.api.gpprocessor.plugin.*;

import java.util.List;

public class PluginContainerImpl extends PluginContainer
{
    private PluginMetadata metadata;
    private LoadState state;
    private boolean acceptingIPCMessages;
    private String[] dependencies;
    private Class<?> pluginClass;

    public PluginContainerImpl(PluginMetadata metadata, LoadState state, boolean acceptingIPCMessages, String[] dependencies, Class<?> pluginClass)
    {
        this.metadata = metadata;
        this.state = state;
        this.acceptingIPCMessages = acceptingIPCMessages;
        this.dependencies = dependencies;
        this.pluginClass = pluginClass;
    }

    private void setAcceptingIPCMessages(boolean value)
    {
        this.acceptingIPCMessages = value;
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
    public boolean isAcceptingMessages()
    {
        return acceptingIPCMessages;
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
}
