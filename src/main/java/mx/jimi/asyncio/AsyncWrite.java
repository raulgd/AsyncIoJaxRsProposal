package mx.jimi.asyncio;

import java.io.OutputStream;

/**
 *
 * @author Raul Guerrero
 */
@FunctionalInterface
public interface AsyncWrite
{

	/**
	 * @param out
	 * @return true if it will continue to write, if false, then finishes writing and AsyncReadDone gets called
	 */
	public boolean onWritePossible(OutputStream out);
}
