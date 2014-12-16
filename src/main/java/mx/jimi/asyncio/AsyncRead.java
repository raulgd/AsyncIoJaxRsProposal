package mx.jimi.asyncio;

import java.io.InputStream;

/**
 *
 * @author Raul Guerrero
 */
@FunctionalInterface
public interface AsyncRead
{

	public void onDataAvailable(InputStream in);
}
