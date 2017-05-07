package alexanders.applications.gpprocessor.plugin.impl;

import alexanders.api.gpprocessor.IComponent;
import alexanders.api.gpprocessor.Reference;
import alexanders.api.gpprocessor.Registry;
import alexanders.api.gpprocessor.capability.Capability;
import alexanders.api.gpprocessor.event.EventBus;
import alexanders.api.gpprocessor.event.EventBusRegistrationEvent;
import alexanders.api.gpprocessor.plugin.PluginContainer;
import alexanders.api.gpprocessor.plugin.PluginManager;
import alexanders.applications.gpprocessor.Entry;
import alexanders.applications.gpprocessor.event.MainEventBus;

import java.util.List;

public class PluginManagerImpl extends PluginManager
{
    private MainEventBus mainEventBus;

    public PluginManagerImpl()
    {
        super();
        mainEventBus = new MainEventBus();
        PluginManager.instance = this;
        this.hasRegistered = true;
    }

    @Override
    public PluginContainer getPlugin(String pluginID)
    {
        return Entry.instance.loader.getPluginByID(pluginID);
    }

    @Override
    public EventBus getMainEventBus()
    {
        return mainEventBus;
    }

    @Override
    public EventBus getEventBus(String eventBusID)
    {
        return registry.get(eventBusID);
    }

    @Override
    public void registerEventBus(EventBus eventBus)
    {
        add(eventBus.getID(), eventBus);
    }

    @Override
    public Registry<Capability> getCapabilityRegistry()
    {
        return new Registry<Capability>("CapabilityRegistry", Reference.logger)
        {
            @Override
            protected void registerInternal(String name, Capability registrant)
            {
                Entry.instance.loader.autoPopulateCapabilities(registrant);
            }
        };
    }

    @Override
    public Registry<IComponent> componentRegistry()
    {
        return new Registry<IComponent>("ComponentRegistry", Reference.logger)
        {
            @Override
            protected void registerInternal(String name, IComponent registrant)
            {
                // TODO: Should I do something here?
            }
        };
    }

    @Override
    public List<Thread> getAssociatedThreads(String pluginID)
    {
        return Entry.instance.scheduler.getThreads(pluginID);
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
