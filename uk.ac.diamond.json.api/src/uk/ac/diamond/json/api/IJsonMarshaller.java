package uk.ac.diamond.json.api;

/**
 * Interface for a JSON marshaller which allows objects to be converted to and from JSON strings
 *
 * @author Colin Palmer
 */
public interface IJsonMarshaller {

	/**
	 * Serialize the given object to a JSON string
	 * <p>
	 * Objects to be marshalled should have no-arg constructors, and getters and setters for their fields.
	 * <p>
	 * The default implementation will add type information to the JSON string to allow objects to be deserialized
	 * correctly even in an OSGi environment (where many classes are not visible to this bundle's classloader).
	 *
	 * @param anyObject
	 *            the object to be serialized
	 * @return the JSON string representing the object
	 * @throws Exception
	 *             if the object cannot be marshalled correctly
	 */
	// TODO switch to using a specific wrapper exception? Or throw a RuntimeException instead of checked? Or just return null?
	public String marshal(Object anyObject) throws Exception;

	// TODO add method to marshal objects without type information?

	/**
	 * Deserialize the given JSON string as an instance of the given class
	 * <p>
	 * The default implementation will try to find the correct classes for deserialization if possible, using type
	 * information encoded in the JSON string if available. If you still have problems with ClassNotFoundExceptions,
	 * one option which might help is to try setting the thread context classloader before calling the unmarshal
	 * method:
	 * <pre>
	 * ClassLoader tccl = Thread.currentThread().getContextClassLoader();
	 * try {
	 * 	Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
	 * 	// call the unmarshaller
	 * } finally {
	 * 	Thread.currentThread().setContextClassLoader(tccl);
	 * }
	 * </pre>
	 *
	 * @param string
	 *            the JSON string to be deserialized
	 * @param beanClass
	 *            the expected class of the deserialized object
	 * @return the deserialized object
	 * @throws Exception
	 *             if the object cannot be unmarshalled correctly
	 */
	// TODO what is returned from an empty JSON string? null or exception?
	public <U> U unmarshal(String string, Class<U> beanClass) throws Exception;
}
