package alexanders.applications.gpprocessor;

import alexanders.api.gpprocessor.Reference;
import alexanders.applications.gpprocessor.plugin.PluginLoader;
import alexanders.applications.gpprocessor.plugin.PluginSecurityManager;
import java.io.File;
import java.util.logging.Level;

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
        instance = new Entry();
    }

    public Entry()
    {
        // Setup threading
        scheduler = new Scheduler();
        scheduler.init();

        // Load plugins
        loader = new PluginLoader();
        loader.loadPlugins(new File("./plugins"));

        // Open GUI
    }
}
