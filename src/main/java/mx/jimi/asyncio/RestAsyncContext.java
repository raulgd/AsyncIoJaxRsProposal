package mx.jimi.asyncio;

import java.io.IOException;
import javax.servlet.AsyncContext;
import javax.servlet.ReadListener;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

/**
 * This class is created by the RestAsyncServlet whenever an async I/O context is requested by a Resource, this class
 * has to be injected to the REST Resource through the @Async parameter annotation
 *
 * @author Raul Guerrero
 */
public class RestAsyncContext implements ReadListener, WriteListener
{

	private final AsyncContext ctx;
	private final HttpServletRequest req;
	private final HttpServletResponse res;

	//this is the async interface objects that are implemented by JAX-RS users and come from their REST Resource
	private AsyncRead asyncRead = null;
	private AsyncReadDone asyncReadDone = null;
	private AsyncBeforeWrite asyncBeforeWrite = null;
	private AsyncWrite asyncWrite = null;
	private AsyncError asyncError = null;

	//flag to see if it's the first time it enters the write, call first so user can do write initialization
	private boolean firstWrite = true;

	public RestAsyncContext(AsyncContext context, HttpServletRequest request, HttpServletResponse response)
	{
		//this is the async context, request and response objects from the servlet
		ctx = context;
		req = request;
		res = response;
	}

	/**
	 * Set the async I/O timeout, if not specified, the default depends on your web container
	 *
	 * @param t the timeout in milliseconds
	 */
	public void setTimeout(long t)
	{
		ctx.setTimeout(t);
	}

	/**
	 * Send data received to whatever method that implements the AsyncRead interface
	 *
	 * @throws IOException
	 */
	@Override
	public void onDataAvailable() throws IOException
	{
		while (req.getInputStream().isReady())
		{
			asyncRead.onDataAvailable(req.getInputStream());
		}
	}

	/**
	 * When all data is read, call whatever method that implements the AsyncReadDone interface
	 *
	 * @throws IOException
	 */
	@Override
	public void onAllDataRead() throws IOException
	{
		//Here you can send through the Servlet response, whatever the JAX-RS response the user created
		//so the response is deferred to this method instead of the original resource
		Response response = asyncReadDone.onAllDataRead();
	}

	/**
	 * Whenever there's an async error, call whatever method that implements the AsyncError interface
	 *
	 * @param t
	 */
	@Override
	public void onError(Throwable t)
	{
		//Here you can send through the Servlet response, whatever the JAX-RS response the user created to inform the error
		//so the response is deferred to this method instead of the original resource
		Response response = asyncError.onError(t);
	}

	/**
	 * Call the method to send data from whatever method that implements the AsyncWrite interface
	 *
	 * @throws IOException
	 */
	@Override
	public void onWritePossible() throws IOException
	{
		if (firstWrite)
		{
			//Here you can send through the Servlet response, whatever the JAX-RS response the user created
			//so the response is deferred to this method instead of the original resource
			//In this case, it could be starting headers before beginning to write the response body
			//You could append the response so that you have both headers at the beginning of the resource method and also
			//when handling this event, check the FileResource sample's comments
			Response response = asyncBeforeWrite.onBeforeWrite();

			firstWrite = false;
		}
		while (res.getOutputStream().isReady())
		{
			if (!asyncWrite.onWritePossible(res.getOutputStream()))
			{
				//end the async write context
				ctx.complete();
				break;
			}
		}
	}

	/**
	 * Assigns the objects for when you need to do async I/O read
	 *
	 * @param read the interface object that implements async reading
	 * @param done the interface object that implements async done reading
	 * @param error the interface object that implements async error handling
	 */
	public void input(AsyncRead read, AsyncReadDone done, AsyncError error)
	{
		asyncRead = read;
		asyncReadDone = done;
		asyncError = error;
	}

	/**
	 * Assigns the objects for when you need to do async I/O write
	 *
	 * @param write the interface object that implements async writing
	 * @param before the interface object that implements async before writing
	 * @param error the interface object that implements async error handling
	 */
	public void output(AsyncBeforeWrite before, AsyncWrite write, AsyncError error)
	{
		asyncBeforeWrite = before;
		asyncWrite = write;
		asyncError = error;
	}

	/**
	 * Assigns objects for when you need to do both async I/O read and write
	 *
	 * @param read the interface object that implements async reading
	 * @param doneRead the interface object that implements async done reading
	 * @param beforeWrite the interface object that implements async before writing
	 * @param write the interface object that implements async writing
	 * @param error the interface object that implements async error handling
	 */
	public void io(AsyncRead read, AsyncReadDone doneRead, AsyncBeforeWrite beforeWrite, AsyncWrite write, AsyncError error)
	{
		asyncRead = read;
		asyncReadDone = doneRead;
		asyncBeforeWrite = beforeWrite;
		asyncWrite = write;
		asyncError = error;
	}
}
