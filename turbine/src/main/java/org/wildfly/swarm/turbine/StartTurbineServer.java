package org.wildfly.swarm.turbine;

import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.netflix.turbine.plugins.PluginsFactory;

import io.fabric8.kubeflix.discovery.OpenShiftDiscovery;

import com.netflix.turbine.init.TurbineInit;

public class StartTurbineServer implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(StartTurbineServer.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initing Turbine server");
        PluginsFactory.setInstanceDiscovery(new OpenShiftDiscovery());
        TurbineInit.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Stopping Turbine server");
        TurbineInit.stop();
    }
}
