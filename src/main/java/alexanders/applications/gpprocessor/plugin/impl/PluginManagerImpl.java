package alexanders.applications.gpprocessor.plugin.impl;

import alexanders.api.gpprocessor.IComponent;
import alexanders.api.gpprocessor.Registry;
import alexanders.api.gpprocessor.capability.Capability;
import alexanders.api.gpprocessor.event.EventBus;
import alexanders.api.gpprocessor.event.EventBusRegistrationEvent;
import alexanders.api.gpprocessor.plugin.PluginContainer;
import alexanders.api.gpprocessor.plugin.PluginManager;
import alexanders.applications.gpprocessor.Entry;

import java.util.List;

public class PluginManagerImpl extends PluginManager
{

    public PluginManagerImpl()
    {
        super();
        PluginManager.instance = this;
        this.hasRegistered = true;
    }

    @Override
    public PluginContainer getPlugin(String pluginID)
    {
        return null;
    }

    @Override
    public EventBus getMainEventBus()
    {
        return null;
    }

    @Override
    public EventBus getEventBus(String eventBusID)
    {
        return null;
    }

    @Override
    public void registerEventBus(EventBus eventBus)
    {
        add(eventBus.getID(), eventBus);
    }

    @Override
    public Registry<Capability> getCapabilityRegistry()
    {
        return null;
    }

    @Override
    public Registry<IComponent> componentRegistry()
    {
        return null;
    }

    @Override
    public List<Thread> getAssociatedThreads(String pluginID)
    {
        return null;
    }

    @Override
    public void requestShutdown()
    {
        Entry.instance.scheduler.shutdown();
    }

    @Override
    protected void registerInternal(String name, EventBus registrant)
    {
        getMainEventBus().fireEvent(new EventBusRegistrationEvent(registrant));
    }
}
