package mx.jimi.asyncio;

import javax.ws.rs.core.Response;

/**
 *
 * @author Raul Guerrero
 */
@FunctionalInterface
public interface AsyncBeforeWrite
{

	public Response onBeforeWrite();
}
