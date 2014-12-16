package mx.jimi.asyncio.sample;

import org.glassfish.jersey.server.ResourceConfig;

@javax.ws.rs.ApplicationPath("/rest")
public class ApplicationConfig extends ResourceConfig
{

	public ApplicationConfig()
	{
		//scan VEGOS for providers and resources
		packages(true, "mx.jimi");
	}
}
