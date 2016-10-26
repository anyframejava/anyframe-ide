package org.anyframe.plugin.common;

import org.springframework.context.MessageSource;

import anyframe.common.exception.BaseException;

/**
 * This MovieFinderException class contains various constructor for different
 * functionality on this project.
 * 
 * @author anyframe
 */
public class MovieFinderException extends BaseException {

	private static final long serialVersionUID = 1L;

	/**
	 * The constructor with a message key, with parameters, and with a wrapped
	 * exception (with all the formal parameters).
	 * 
	 * @param messageSource
	 *            message management service to extract a message
	 * @param messageKey
	 *            the message key of this exception
	 * @param messageParameters
	 *            the parameters to substitute in the message
	 * @param wrappedException
	 *            the exception that is wrapped in this exception
	 */
	public MovieFinderException(final MessageSource messageSource,
			final String messageKey, final Object[] messageParameters,
			final Throwable wrappedException) {
		super(messageSource, messageKey, messageParameters, "Occured Error",
				wrappedException);
	}

	/**
	 * The constructor with a message key, with parameters, and with a wrapped
	 * exception (with all the formal parameters).
	 * 
	 * @param messageSource
	 *            message management service to extract a message
	 * @param messageParameters
	 *            the parameters to substitute in the message
	 * @param messageKey
	 *            the message key of this exception
	 */
	public MovieFinderException(final MessageSource messageSource,
			final String messageKey, final Object[] messageParameters) {
		super(messageSource, messageKey, messageParameters);
	}

	/**
	 * The constructor with a message key, with parameters, and with a wrapped
	 * exception (with all the formal parameters).
	 * 
	 * @param messageSource
	 *            message management service to extract a message
	 * @param messageKey
	 *            the message key of this exception
	 */
	public MovieFinderException(final MessageSource messageSource,
			final String messageKey) {
		super(messageSource, messageKey);
		super.getMessageKey();
	}

	/**
	 * The constructor with a message key, with parameters, and with a wrapped
	 * exception (with all the formal parameters).
	 * 
	 * @param messageSource
	 *            message management service to extract a message
	 * @param messageKey
	 *            the message key of this exception
	 * @param wrappedException
	 *            the exception that is wrapped in this exception
	 */
	public MovieFinderException(final MessageSource messageSource,
			final String messageKey, final Throwable wrappedException) {
		super(messageSource, messageKey, wrappedException);
	}

	/**
	 * Constructor with a message key and an exception.
	 * 
	 * @param message
	 *            the message of this exception
	 * @param exception
	 *            the exception that is wrapped in this exception
	 */
	public MovieFinderException(final String message, final Throwable exception) {
		super(message, null, exception);
	}

	/**
	 * Constructor with a message key and an exception.
	 * 
	 * @param message
	 *            the message of this exception
	 */
	public MovieFinderException(final String message) {
		super(message);
	}
}
