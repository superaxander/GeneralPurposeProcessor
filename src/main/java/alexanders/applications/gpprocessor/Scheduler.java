package alexanders.applications.gpprocessor;

import alexanders.api.gpprocessor.MapUtil;
import alexanders.api.gpprocessor.PrioritizedThreads;
import alexanders.api.gpprocessor.Reference;
import alexanders.api.gpprocessor.event.EventBus;
import alexanders.api.gpprocessor.event.ShutdownEvent;
import alexanders.api.gpprocessor.plugin.PluginManager;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scheduler
{
    private final Map<String, List<Thread>> threadMap;
    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public Scheduler()
    {
        this.threadMap = new HashMap<>();
        this.uncaughtExceptionHandler = (t, e) ->
        {
            //TODO: Handle exceptions
        };
    }

    public void addThread(String pluginID, Thread thread)
    {
        thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        thread.setDaemon(true);
        thread.setPriority(PrioritizedThreads.getPriorityForThread(thread));
        thread.start();
        MapUtil.addToEmbeddedList(threadMap, pluginID, thread);
    }

    public void init()
    {
        addThread("gpp", new PrioritizedThreads.EventThread(() ->
                                                            {
                                                                while (Reference.running)
                                                                {
                                                                    while (EventBus.scheduledEvents.isEmpty())
                                                                        Thread.yield();
                                                                    synchronized (EventBus.scheduledEvents)
                                                                    {
                                                                        EventBus.scheduledEvents.forEach(triplet -> triplet.forEach((event, object, method) ->
                                                                                                                                    {
                                                                                                                                        try
                                                                                                                                        {
                                                                                                                                            method.invoke(object, event);
                                                                                                                                        } catch (IllegalAccessException | InvocationTargetException e)
                                                                                                                                        {
                                                                                                                                            e.printStackTrace();
                                                                                                                                        }
                                                                                                                                    }));
                                                                    }
                                                                }
                                                            }));
    }

    public void shutdown()
    {
        PluginManager.instance.getMainEventBus().fireEvent(new ShutdownEvent());
        threadMap.forEach((id, threads) ->
                          {
                              if (!id.equals("gpp"))
                                  threads.forEach(thread ->
                                                  {
                                                      try
                                                      {
                                                          thread.join(1000); // Could be tweaked
                                                      } catch (InterruptedException e)
                                                      {
                                                          e.printStackTrace();
                                                          System.exit(1);
                                                      }
                                                  });
                          });
    }
}
