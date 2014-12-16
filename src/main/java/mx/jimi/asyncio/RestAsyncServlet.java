package mx.jimi.asyncio;

import java.io.IOException;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Raul Guerrero
 */
@WebServlet(urlPatterns =
{
	//whatever pattern specified for JAX-RS root
	"/rest"
}, asyncSupported = true)
public class RestAsyncServlet extends HttpServlet
{

	/**
	 * Processes all async I/O requests
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
					throws ServletException, IOException
	{
		AsyncContext context = request.startAsync();

		//initialize based on whatever is needed from the JAX-RS application as well, for now just the basics
		RestAsyncContext ctx = new RestAsyncContext(context, request, response);

		request.getInputStream().setReadListener(ctx);
		response.getOutputStream().setWriteListener(ctx);

		//after here, the rest async context object (ctx) has to be injected automagically to the REST resource using the
		//Async annotation in the REST resource
	}

	/**
	 * Add support for all verbs and whatever, this is also a JAX-RS implementation thing, for this sample just GET and
	 * PUT
	 */

	/**
	 * Send the GET verb to processRequest
	 *
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
					throws ServletException, IOException
	{
		processRequest(request, response);
	}

	/**
	 * Send the PUT verb to processRequest
	 *
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
					throws ServletException, IOException
	{
		processRequest(request, response);
	}

}
