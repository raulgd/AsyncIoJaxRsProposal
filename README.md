## Async I/O JAX-RS.Next proposal

This is a project that proposes a way to implement non-blocking I/O for REST resources.
It also presents a way to take advantage of Java 8 lambdas to easily handle the async i/o events in a clean way.

## The definition of async I/O in a resource

The way that you access the async I/O streams through a resource is using a RestAsyncContext defined by using the @Async parameter annotation:

Here's an example of a resource method signature:
- public void doGet(@QueryParam("param") String param, @Async RestAsyncContext ctx)

## Implementing RestAsyncContext

To implement correctly this context, first, it needs to be created and initialized through an asynchronous servlet, this is done in RestAsyncServlet.
This servlet, in a JAX-RS implementation, has to be replaced by whatever receives a request for a resource.
On a servlet container, the JAX-RS servlet that maps requests to resources can lookup the resource in its registry, then if it identifies that has the @Async parameter annotation, it can initialize the resource call as an async I/O call, and assign the RestAsyncContext as the ReadListener and WriteListener for the resource, then pass that RestAsyncContext to the resource.

## You receive the context in the resource, now what?

After you got the RestAsyncContext object for the request, the context has a couple of methods where you define how you handle input and output streaming, and the async timeout.

First of all, if your stream takes some time to process (like uploading/downloading big files), then you need to be able to change the async work timeout so that the stream doesn't get cut off, for that the context object has the setTimeout(long) method so you can set it to whatever you need.

For processing async requests, you have to implement the AsyncRead, AsyncReadDone and AsyncError interfaces, this can be done either creating a class that implements such interfaces, or just call the context.input(read, done, error); method and set as the objects a lambda function that handles each of the events, in the sample application contained in the project, there's a FileResource that has a sample using lamdbas.

For processing async responses, you have to implement the AsyncBeforeWrite, AsyncWrite and AsyncError interfaces, it's just like the request, but using context.output(before, write, error) where the beforeWrite event can be used to set headers, then write for writing to the response body, and error to handle any response issues.

Then, there's also the possibity to handle asynchronously both request and response, for that there's the context.io(read, doneRead, beforeWrite, write, error) method, where you can handle everything for the request and response in a non-blocking way.

## What's missing

Pretty much this just defines interfaces and places an example that can be compiled, but the important part missing is how to inject the RestAsyncContext from the RestAsyncServlet into the REST resource method.

Also, there are some interfaces that can return a JAX-RS Response object, but once the RestAsyncServlet receives it, it does nothing with it, so a method has to be created to take the JAX-RS Response and translate it in a way that an HttpServletResponse can use it for user output.

I consider the code commented well enough to understand how the servlet, context and resource interact and work, and also there are comments to know some of the missing parts like the Response just mentioned before, but in case there are any improvements on them or if you need any more help understanding the proposal, just submit an issue or pull request so I can analyze it to further improve the proposal.

I hope this proposal is of any help to take JAX-RS.Next to the non-blocking world, as having the @Suspended annotation is just half way into having full async JAX-RS implementation.