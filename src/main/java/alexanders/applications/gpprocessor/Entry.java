package alexanders.applications.gpprocessor;

import alexanders.api.gpprocessor.Reference;
import alexanders.api.gpprocessor.event.InitializationEvent;
import alexanders.api.gpprocessor.event.PostInitializationEvent;
import alexanders.api.gpprocessor.event.PreInitializationEvent;
import alexanders.api.gpprocessor.plugin.PluginContainer;
import alexanders.api.gpprocessor.plugin.PluginManager;
import alexanders.api.gpprocessor.plugin.PluginMetadata;
import alexanders.applications.gpprocessor.plugin.PluginLoader;
import alexanders.applications.gpprocessor.plugin.PluginSecurityManager;
import alexanders.applications.gpprocessor.plugin.impl.PluginManagerImpl;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Entry
{
    public static Entry instance;

    public Scheduler scheduler;
    public PluginLoader loader;

    public static void main(String[] args)
    {
        try
        {
            System.setSecurityManager(new PluginSecurityManager());
        } catch (SecurityException e)
        {
            Reference.logger.logp(Level.SEVERE, "Entry", "main", "Failed to set our securityManager!", e);
        }
        Reference.running = true;
        instance = new Entry();
    }

    public Entry()
    {
        // Setup threading
        scheduler = new Scheduler();
        scheduler.init();

        // Load plugins
        new PluginManagerImpl();
        loader = new PluginLoader();
        loader.loadPlugins(new File("./plugins"));
        loader.autoPopulateInital();
        PluginContainer container = loader.plugins.get(0);
        PluginManager.instance.getMainEventBus().fireEvent(new PreInitializationEvent(Logger.getLogger(container.getID()), new File("./config/" + container.getID()), new File("./config/" + container.getID() + ".cfg"), new PluginMetadata(container.getName(), container.getID(), /*TODO:LOL forgot this*/null, /*TODO:LOL forgot this*/null, /*TODO:LOL forgot this*/null), null));
        PluginManager.instance.getMainEventBus().fireEvent(new InitializationEvent(loader.getLoadStates()));
        PluginManager.instance.getMainEventBus().fireEvent(new PostInitializationEvent(loader.getLoadStates()));
        // Open GUI
    }
}
