package alexanders.applications.gpprocessor.event;

import alexanders.api.gpprocessor.MapUtil;
import alexanders.api.gpprocessor.Pair;
import alexanders.api.gpprocessor.Reference;
import alexanders.api.gpprocessor.Triplet;
import alexanders.api.gpprocessor.event.EventBus;
import alexanders.api.gpprocessor.event.GPPEvent;
import alexanders.applications.gpprocessor.plugin.PluginLoader;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class MainEventBus extends EventBus
{
    public MainEventBus()
    {
        this.eventHandlerMap = new HashMap<>();
    }

    @Override
    public void register(String pluginID, Object eventHandler)
    {
        try
        {
            HashMap<String, List<Method>> eventHandlers = PluginLoader.loadEventHandlersFromClass(eventHandler.getClass());
            eventHandlers.forEach((event, methods) -> methods.forEach(method -> register(pluginID, eventHandler, method, event)));
        } catch (IllegalAccessException | InstantiationException e)
        {
            Reference.logger.logp(Level.SEVERE, "MainEventBus", "register", "Failed to load event handlers from class!", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(String pluginID, Object eventHandler, Method eventHandlerMethod)
    {
        try
        {
            MapUtil.addToEmbeddedListInEmbeddedMap(eventHandlerMap, PluginLoader.getEventFromMethod(eventHandlerMethod).getType(), pluginID, new Pair<>(eventHandler, eventHandlerMethod));
        } catch (IllegalAccessException | InstantiationException e)
        {
            Reference.logger.logp(Level.SEVERE, "MainEventBus", "register", "Failed to load event handler from class!", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void register(String pluginID, Object eventHandler, Method method, String event)
    {
        MapUtil.addToEmbeddedListInEmbeddedMap(eventHandlerMap, event, pluginID, new Pair<>(eventHandler, method));
    }

    @Override
    public void fireEvent(GPPEvent event)
    {
        Map<String, List<Pair<Object, Method>>> pluginList;
        if ((pluginList = eventHandlerMap.get(event.getType())) == null)
        {
            Reference.logger.logp(Level.WARNING, "MainEventBus", "fireEvent", "Event: " + event.getType() + " was fired but no handlers were registered for it");
        } else
        {
            synchronized (scheduledEvents)
            {
                pluginList.forEach((plugin, pairs) -> pairs.forEach(pair -> pair.forEach((object, method) ->
                                                                                         {
                                                                                             scheduledEvents.add(new Triplet<>(event, object, method));
                                                                                         })));
            }
        }
    }

    @Override
    public void fireEventAt(String pluginID, GPPEvent event)
    {
        Map<String, List<Pair<Object, Method>>> pluginList;
        if ((pluginList = eventHandlerMap.get(event.getType())) == null)
        {
            Reference.logger.logp(Level.WARNING, "MainEventBus", "fireEventAt", "Event: " + event.getType() + " was fired at: " + pluginID + "but no handlers were registered for it");
        } else
        {
            List<Pair<Object, Method>> pluginSpecificList;
            if ((pluginSpecificList = pluginList.get(pluginID)) == null)
            {
                Reference.logger.logp(Level.WARNING, "MainEventBus", "fireEventAt", "Event: " + event.getType() + " was fired at: " + pluginID + "but no handlers were registered for it");
            } else
            {
                synchronized (scheduledEvents)
                {
                    pluginSpecificList.forEach(pair -> pair.forEach((object, method) ->
                                                                    {
                                                                        scheduledEvents.add(new Triplet<>(event, object, method));
                                                                    }));
                }
            }
        }
    }
}
