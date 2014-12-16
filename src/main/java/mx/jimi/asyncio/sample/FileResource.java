package mx.jimi.asyncio.sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import mx.jimi.asyncio.Async;
import mx.jimi.asyncio.RestAsyncContext;

/**
 *
 * @author Raul Guerrero
 */
@Path("/file")
public class FileResource
{

	/**
	 * Download a file using Async I/O, uses Java 8 lambdas, but could also create a class that implements the
	 * AsyncBeforeWrite, AsyncWrite and AsyncError interfaces
	 *
	 * @param path the file with the full path to download: i.e. C:\\uploads\some-file.txt
	 * @param ctx the possible JAX-RS async I/O context
	 */
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public void download(@QueryParam("path") final String path, @Async final RestAsyncContext ctx)
	{
		ctx.setTimeout(3600000);
		try
		{
			final byte[] buffer = new byte[1024 * 1024];
			final File file = new File(path);
			final InputStream in = new FileInputStream(file);

			ctx.output(
							//async before body write
							() ->
							{
								try
								{
									return Response.ok().type(MediaType.APPLICATION_OCTET_STREAM)
									.header("Content-Disposition", "attachment; filename=\"" + FileUtils.getFileName(path) + "\"")
									.header("Content-Length", String.valueOf(file.length()))
									.build();
								}
								catch (UnsupportedEncodingException ex)
								{
									Logger.getLogger(FileResource.class.getName()).log(Level.SEVERE, null, ex);
								}
								return null;
							},
							//async write
							(OutputStream out) ->
							{
								try
								{
									int length = in.read(buffer);
									if (length == -1)
									{
										//finished writing out the file
										in.close();
										return false;
									}
									else
									{
										out.write(buffer, 0, length);
										return true;
									}
								}
								catch (IOException ex)
								{
									Logger.getLogger(FileResource.class.getName()).log(Level.SEVERE, null, ex);
								}
								return false;
							},
							//async error
							(Throwable t) ->
							{
								String error = "{\"error\":\"There was a problem downloading the file: " + t.getMessage() + "\"}";
								return Response.serverError().entity(error).type(MediaType.APPLICATION_JSON).build();
							});
		}
		catch (FileNotFoundException ex)
		{
			Logger.getLogger(FileResource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Upload a file using Async I/O, uses Java 8 lambdas, but could also create a class that implements the AsyncRead,
	 * AsyncReadDone and AsyncError interfaces
	 *
	 * @param path the path to upload the file to: i.e. C:\\uploads
	 * @param name the name of the file to upload: i.e. some-file.txt
	 * @param ctx the possible JAX-RS async I/O context
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public void upload(@QueryParam("path") final String path, @QueryParam("name") final String name, @Async final RestAsyncContext ctx)
	{
		ctx.setTimeout(3600000);

		try
		{
			final byte[] buffer = new byte[1024 * 1024];
			final File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
			final OutputStream out = new FileOutputStream(tempFile, true);

			ctx.input(
							//async read
							(InputStream in) ->
							{
								try
								{
									int length = in.read(buffer);
									out.write(buffer, 0, length);
								}
								catch (IOException ex)
								{
									Logger.getLogger(FileResource.class.getName()).log(Level.SEVERE, null, ex);
								}
							},
							//async read done
							() ->
							{
								out.close();

								final File target = new File(path + File.separator + name);

								new Thread(() ->
												{
													try
													{
														Files.move(tempFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
													}
													catch (IOException ex)
													{
														Logger.getLogger(FileResource.class.getName()).log(Level.SEVERE, null, ex);
													}
								}).start();

								return Response.ok("{\"success\":true}").type(MediaType.APPLICATION_JSON).build();
							},
							//async error
							(Throwable t) ->
							{
								try
								{
									out.close();
								}
								catch (IOException ex)
								{
									Logger.getLogger(FileResource.class.getName()).log(Level.SEVERE, null, ex);
								}

								String error = "{\"error\":\"There was a problem uploading the file: " + t.getMessage() + "\"}";
								return Response.serverError().entity(error).type(MediaType.APPLICATION_JSON).build();
							});
		}
		catch (IOException ex)
		{
			Logger.getLogger(FileResource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
