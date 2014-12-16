package mx.jimi.asyncio;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation used to get the RestAsyncContext
 *
 * @author Raul Guerrero
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Async
{
//do whatever to inject the RestAsyncContext to a Resource
}
