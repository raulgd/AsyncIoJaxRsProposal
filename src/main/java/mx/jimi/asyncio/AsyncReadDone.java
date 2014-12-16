package mx.jimi.asyncio;

import java.io.IOException;
import javax.ws.rs.core.Response;

/**
 *
 * @author Raul Guerrero
 */
@FunctionalInterface
public interface AsyncReadDone
{

	public Response onAllDataRead() throws IOException;
}
