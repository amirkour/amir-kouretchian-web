package org.amirk.website.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;


public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer{
    
    /*
     * identify one or more paths that DispatcherServlet will be mapped to.
     */
    @Override
    protected String[] getServletMappings(){
        
        // in this case, DispatcherServlet will handle everything
        // (ie: it's the application's default servlet.)
        return new String[]{"/"};
    }
    
    /*
     * Specify which configuration class specifies bussiness-object beans.
     * The class you return from this guy is the equivalent of an app context
     * that you'd configure in a regular console app.
     */
    @Override
    protected Class<?>[] getRootConfigClasses(){
        return new Class<?>[] { AppConfig.class };
    }
    
    /*
     * Specify which configuration class specifies web-related beans.
     * TODO - read up on it a bit ...
     */
    @Override
    protected Class<?>[] getServletConfigClasses(){
        return new Class<?>[]{WebConfig.class};
    }
}
