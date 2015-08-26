package org.amirk.website.config;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = {
                   "org.amirk.website",
                   "org.amirk.games.connectfour.entities",
                   "org.amirk.games.connectfour.db"
               },
               excludeFilters = {
                   @ComponentScan.Filter(type=FilterType.ANNOTATION, value=EnableWebMvc.class)
               })
public class AppConfig {
    
    @Bean
    @Profile("dev")
    public Logger devLogger(){
        return LoggerFactory.getLogger("dev");
    }
    
    @Bean
    @Profile("default")
    public Logger defaultLogger(){
        return LoggerFactory.getLogger("root");
    }
    
    @Bean
    public DataSource jndiDataSource() throws Exception{
        Context ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/connectfour");
        
        return ds;
    }
    
    @Bean
    public LocalSessionFactoryBean localSessionFactoryBean(DataSource ds){
        
        LocalSessionFactoryBean lsfb = new LocalSessionFactoryBean();
        
        lsfb.setDataSource(ds);
        
        // have to scan the beans, since there are no mapping files
        lsfb.setPackagesToScan("org.amirk.games.connectfour.entities");
        Properties props = new Properties();
        
        // from hibernate manual section 3.3:
        // For use inside an application server, you should almost always configure Hibernate 
        // to obtain connections from an application server javax.sql.Datasource registered in JNDI. 
        // You will need to set at least one of the following properties ...
        props.setProperty("hibernate.connection.datasource", "java:comp/env/jdbc/connectfour");

        // probably will always need the dialect set
        props.setProperty("hibernate.dialect","org.hibernate.dialect.MySQLDialect");

        // meh - don't care about these for now.  use the sql for debugging if you want
        props.setProperty("hibernate.cache.provider_class","org.hibernate.cache.internal.NoCacheProvider");
        props.setProperty("hibernate.show_sql", "false");

        lsfb.setHibernateProperties(props);
        
        return lsfb;
    }
    
    // Notice that the sesion factory is being wired-up from LocalSessionFactoryBean
    // in this class.
    // TODO - figure out how that works - spring somehow resolves the LSFB to a session factory ...?
    @Bean
    public PlatformTransactionManager txManager(SessionFactory sf){
        HibernateTransactionManager txManager = new HibernateTransactionManager();
        txManager.setSessionFactory(sf);
        return txManager;
    }
    
    /*
     * In conjunction with @Repository annotations, this bean will provide
     * automatic exception mapping.  It basically creates aop advisors for
     * anything with a @Repository annotation that will give you exception
     * mapping for hibernate exceptions.
     */
    @Bean
    public BeanPostProcessor persistenceTranslation(){
        return new PersistenceExceptionTranslationPostProcessor();
    }
}
