package server.tracks;
//
//import jp.fun.Search;
//import jp.fun.SearchImpl;
//import org.eclipse.jetty.server.Server;
//import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
//import org.glassfish.jersey.server.ResourceConfig;
//import org.reflections.Reflections;
//
//import javax.ws.rs.Path;
//import javax.ws.rs.core.UriBuilder;
//import java.net.URI;
//import java.util.HashSet;
//import java.util.Set;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Created by Jean-Philippe Belanger on 11/3/16.
 * Just potential zeroes and ones
 */
public class StartServer {

    public static void main(String[] args) throws Exception  {

        URI baseUri = UriBuilder.fromUri("http://localhost/").port(9998).build();
        ResourceConfig config = new ResourceConfig(TracksImpl.class);
        Server server = JettyHttpContainerFactory.createServer(baseUri, config);
        server.start();
    }
}
