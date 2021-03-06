package uk.ac.diamond.json.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.dawnsci.commandserver.mx.beans.ProjectBean;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.diamond.json.JsonMarshaller;
import uk.ac.diamond.json.api.IJsonMarshaller;

import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Unit tests for the Jackson JSON marshaller which check (de)serialization of a range of types, but without the
 * complication of checking classes are loaded from the correct OSGi bundles.
 * <p>
 * If the marshaller settings are changed, the new JSON string produced in each test can be written to std out by
 * uncommenting the relevant line in tearDown(), allowing it to be copied into the Java code to update the tests.
 *
 * @author Colin Palmer
 *
 */
public class JsonMarshallerNonOSGiTest {

	private static final String TEST_STRING = "Hello world!";
	private static final String JSON_FOR_TEST_STRING = "\"Hello world!\"";
	private static final int TEST_INT = -56;
	private static final String JSON_FOR_TEST_INT = "-56";
	private static final long TEST_LONG = 1234567890L;
	private static final String JSON_FOR_TEST_LONG = "[ \"bundle=&version=&class=java.lang.Long\", 1234567890 ]";

	// An example of a bean used by Xia2 which could be sent by another process and must deserialize correctly in current version
	private static final String JSON_FOR_PROJECT_BEAN = "{\"status\":\"COMPLETE\",\"name\":\"X1_weak_M1S1_1 - X1_weak_M1S1_1\",\"message\":\"Xia2 run completed normally\",\"percentComplete\":100.0,\"userName\":\"awa25\",\"hostName\":\"cs04r-sc-vserv-45.diamond.ac.uk\",\"runDirectory\":\"/dls/i03/data/2016/cm14451-1/processed/tmp/2016-01-27/fake085224/MultiCrystal_1\",\"uniqueId\":\"1453910139320_94ed2a2b-997e-4dbc-ad6e-0c3c04bb2c82\",\"submissionTime\":1453910139340,\"properties\":null,\"projectName\":\"MultiCrystalRerun\",\"cystalName\":\"fake085224\",\"sweeps\":[{\"name\":\"X1_weak_M1S1_1\",\"sessionId\":\"55167\",\"dataCollectionId\":\"1007379\",\"imageDirectory\":\"/dls/i03/data/2016/cm14451-1/tmp/2016-01-27/fake085224/\",\"firstImageName\":\"X1_weak_M1S1_1_0001.cbf\",\"start\":1,\"end\":900,\"wavelength\":0.979493,\"xBeam\":212.51,\"yBeam\":219.98,\"resolution\":null}],\"wavelength\":\"NaN\",\"commandLineSwitches\":\"\",\"anomalous\":true,\"spaceGroup\":null,\"unitCell\":null,\"resolution\":null}";

	// Example of JSON produced for an ROI
	private static final String JSON_FOR_RECTANGULAR_ROI = "{\n  \"@bundle_and_class\" : \"bundle=&version=&class=org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI\",\n  \"lengths\" : [ 8.0, 6.1 ],\n  \"angle\" : 0.0,\n  \"point\" : [ -3.5, 4.0 ]\n}";

	private static final String JSON_FOR_WRAPPED_RECTANGULAR_ROI = "{\n  \"@bundle_and_class\" : \"bundle=&version=&class=uk.ac.diamond.json.test.ROIWrapper\",\n  \"object\" : {\n    \"@bundle_and_class\" : \"bundle=&version=&class=org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI\",\n    \"lengths\" : [ 8.0, 6.1 ],\n    \"angle\" : 0.0,\n    \"point\" : [ -3.5, 4.0 ]\n  }\n}";
	private static final String JSON_FOR_GENERIC_WRAPPED_RECTANGULAR_ROI = "{\n  \"@bundle_and_class\" : \"bundle=&version=&class=uk.ac.diamond.json.test.ObjectWrapper\",\n  \"object\" : {\n    \"@bundle_and_class\" : \"bundle=&version=&class=org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI\",\n    \"lengths\" : [ 8.0, 6.1 ],\n    \"angle\" : 0.0,\n    \"point\" : [ -3.5, 4.0 ]\n  }\n}";
	private static final String JSON_FOR_WRAPPED_RECTANGULAR_ROI_LIST = "{\n  \"@bundle_and_class\" : \"bundle=&version=&class=uk.ac.diamond.json.test.ObjectWrapper\",\n  \"object\" : [ \"bundle=&version=&class=java.util.ArrayList\", [ {\n    \"@bundle_and_class\" : \"bundle=&version=&class=org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI\",\n    \"lengths\" : [ 8.0, 6.1 ],\n    \"angle\" : 0.0,\n    \"point\" : [ -3.5, 4.0 ]\n  } ] ]\n}";

	private static final String[] STRING_ARRAY = { "a", "b", "c" };
	private static final String JSON_FOR_STRING_ARRAY = "[ \"bundle=&version=&class=[Ljava.lang.String;\", [ \"a\", \"b\", \"c\" ] ]";
	private static final String JSON_FOR_WRAPPED_STRING_ARRAY = "{\n  \"@bundle_and_class\" : \"bundle=&version=&class=uk.ac.diamond.json.test.ObjectWrapper\",\n  \"object\" : [ \"bundle=&version=&class=[Ljava.lang.String;\", [ \"a\", \"b\", \"c\" ] ]\n}";

	private IJsonMarshaller marshaller;
	private String json;

	@Before
	public void setUp() throws Exception {
		marshaller = new JsonMarshaller();
	}

	@After
	public void tearDown() throws Exception {
		if (json != null) {
			// So we can see what's going on
//			System.out.println("JSON: " + json);

			// To make it easy to replace expected JSON values in the code when we're sure they're correct
			@SuppressWarnings("unused")
			String javaLiteralForJSONString = '"' + StringEscapeUtils.escapeJava(json) + '"';
//			System.out.println("Java literal:\n" + javaLiteralForJSONString);
		}
		json = null;
		marshaller = null;
	}

	@Test
	public void testIntSerialization() throws Exception {
		json = marshaller.marshal(TEST_INT);
		assertEquals(JSON_FOR_TEST_INT, json);
	}

	@Test
	public void testIntDeserialization() throws Exception {
		Object result = marshaller.unmarshal(JSON_FOR_TEST_INT, Object.class);
		assertThat(result, is(equalTo(TEST_INT)));
	}

	@Test
	public void testLongSerialization() throws Exception {
		json = marshaller.marshal(TEST_LONG);
		assertEquals(JSON_FOR_TEST_LONG, json);
	}

	@Test
	public void testLongDeserialization() throws Exception {
		Object result = marshaller.unmarshal(JSON_FOR_TEST_LONG, Object.class);
		assertThat(result, is(equalTo(TEST_LONG)));
	}

	@Test
	public void testStringSerialization() throws Exception {
		json = marshaller.marshal(TEST_STRING);
		assertEquals(JSON_FOR_TEST_STRING, json);
	}

	@Test
	public void testStringDeserialization() throws Exception {
		Object result = marshaller.unmarshal(JSON_FOR_TEST_STRING, Object.class);
		assertEquals(TEST_STRING, result);
	}

	@Test
	public void testStringArraySerialization() throws Exception {
		json = marshaller.marshal(STRING_ARRAY);
		assertEquals(JSON_FOR_STRING_ARRAY, json);
	}

	@Test
	public void testStringArrayDeserialization() throws Exception {
		String[] actual = marshaller.unmarshal(JSON_FOR_STRING_ARRAY, String[].class);
		assertArrayEquals(actual, STRING_ARRAY);
	}

	@Test
	public void testSimpleStringArrayDeserialization() throws Exception {
		String[] actual = marshaller.unmarshal("[ \"a\", \"b\", \"c\" ]", String[].class);
		assertArrayEquals(actual, STRING_ARRAY);
	}

	@Test
	public void testStringArrayFieldSerialization() throws Exception {
		ObjectWrapper<String[]> arrayWrapper = new ObjectWrapper<>(STRING_ARRAY);
		json = marshaller.marshal(arrayWrapper);
		assertEquals(JSON_FOR_WRAPPED_STRING_ARRAY, json);
	}

	@Test
	public void testStringArrayFieldDeserialization() throws Exception {
		ObjectWrapper<String[]> expected = new ObjectWrapper<>(STRING_ARRAY);
		ObjectWrapper<?> actual = marshaller.unmarshal(JSON_FOR_WRAPPED_STRING_ARRAY, ObjectWrapper.class);
		assertArrayEquals(expected.getObject(), (Object[]) actual.getObject());
	}

	@Test
	public void testObjectArrayDeserialization() throws Exception {
		Object[] actual = marshaller.unmarshal("[ \"a\", \"b\", 5 ]", Object[].class);
		assertArrayEquals(actual, new Object[] { "a", "b", 5 });
	}

	@Test
	public void testSimpleMapDeserialization() throws Exception {
		Map<String, Object> expected = new HashMap<>();
		expected.put("String key", "String value");
		expected.put("Int key", 5);
		Map<?,?> actual = marshaller.unmarshal("{ \"String key\" : \"String value\", \"Int key\" : 5 }", Map.class);
		assertEquals(expected, actual);
	}

	@Test
	public void testProjectBeanDeserialization() throws Exception {
		marshaller.unmarshal(JSON_FOR_PROJECT_BEAN, ProjectBean.class);
	}

	@Test(expected = JsonMappingException.class)
	public void testProjectBeanDeserializationWithWrongType() throws Exception {
		marshaller.unmarshal(JSON_FOR_PROJECT_BEAN, StatusBean.class);
	}

	@Test
	public void testProjectBeanSerialization() throws Exception {
		ProjectBean bean = marshaller.unmarshal(JSON_FOR_PROJECT_BEAN, ProjectBean.class);
		json = marshaller.marshal(bean);
		// New json is different from original because it is indented and has type info
		assertEquals("{\n  \"@bundle_and_class\" : \"bundle=&version=&class=org.dawnsci.commandserver.mx.beans.ProjectBean\",\n  \"uniqueId\" : \"1453910139320_94ed2a2b-997e-4dbc-ad6e-0c3c04bb2c82\",\n  \"status\" : [ \"bundle=&version=&class=org.eclipse.scanning.api.event.status.Status\", \"COMPLETE\" ],\n  \"name\" : \"X1_weak_M1S1_1 - X1_weak_M1S1_1\",\n  \"message\" : \"Xia2 run completed normally\",\n  \"percentComplete\" : 100.0,\n  \"userName\" : \"awa25\",\n  \"hostName\" : \"cs04r-sc-vserv-45.diamond.ac.uk\",\n  \"runDirectory\" : \"/dls/i03/data/2016/cm14451-1/processed/tmp/2016-01-27/fake085224/MultiCrystal_1\",\n  \"submissionTime\" : 1453910139340,\n  \"projectName\" : \"MultiCrystalRerun\",\n  \"cystalName\" : \"fake085224\",\n  \"sweeps\" : [ \"bundle=&version=&class=java.util.ArrayList\", [ {\n    \"@bundle_and_class\" : \"bundle=&version=&class=org.dawnsci.commandserver.mx.beans.SweepBean\",\n    \"name\" : \"X1_weak_M1S1_1\",\n    \"sessionId\" : \"55167\",\n    \"dataCollectionId\" : \"1007379\",\n    \"imageDirectory\" : \"/dls/i03/data/2016/cm14451-1/tmp/2016-01-27/fake085224/\",\n    \"firstImageName\" : \"X1_weak_M1S1_1_0001.cbf\",\n    \"start\" : 1,\n    \"end\" : 900,\n    \"wavelength\" : 0.979493,\n    \"xBeam\" : 212.51,\n    \"yBeam\" : 219.98\n  } ] ],\n  \"wavelength\" : \"NaN\",\n  \"commandLineSwitches\" : \"\",\n  \"anomalous\" : true\n}", json);
	}

	@Test
	public void testROISerialization() throws Exception {
		IROI roi = new RectangularROI(-3.5, 4.0, 8.0, 6.1, 0.0);
		json = marshaller.marshal(roi);
		assertEquals(JSON_FOR_RECTANGULAR_ROI, json);
	}

	@Test
	public void testROIDeserialization() throws Exception {
		IROI actual = marshaller.unmarshal(JSON_FOR_RECTANGULAR_ROI, IROI.class);
		IROI expected = new RectangularROI(-3.5, 4.0, 8.0, 6.1, 0.0);
		assertEquals(expected, actual);
	}

	@Test
	public void testROIFieldSerialization() throws Exception {
		IROI roi = new RectangularROI(-3.5, 4.0, 8.0, 6.1, 0.0);
		ROIWrapper roiWrapper = new ROIWrapper(roi);
		json = marshaller.marshal(roiWrapper);
		assertEquals(JSON_FOR_WRAPPED_RECTANGULAR_ROI, json);
	}

	@Test
	public void testROIFieldDeserialization() throws Exception {
		IROI roi = new RectangularROI(-3.5, 4.0, 8.0, 6.1, 0.0);
		ROIWrapper expected = new ROIWrapper(roi);
		ROIWrapper actual = marshaller.unmarshal(JSON_FOR_WRAPPED_RECTANGULAR_ROI, ROIWrapper.class);
		assertEquals(expected, actual);
	}

	@Test
	public void testGenericROIFieldSerialization() throws Exception {
		IROI roi = new RectangularROI(-3.5, 4.0, 8.0, 6.1, 0.0);
		ObjectWrapper<IROI> roiWrapper = new ObjectWrapper<>(roi);
		json = marshaller.marshal(roiWrapper);
		assertEquals(JSON_FOR_GENERIC_WRAPPED_RECTANGULAR_ROI, json);
	}

	@Test
	public void testGenericROIFieldDeserialization() throws Exception {
		IROI roi = new RectangularROI(-3.5, 4.0, 8.0, 6.1, 0.0);
		ObjectWrapper<IROI> expected = new ObjectWrapper<>(roi);
		ObjectWrapper<?> actual = marshaller.unmarshal(JSON_FOR_GENERIC_WRAPPED_RECTANGULAR_ROI, ObjectWrapper.class);
		assertEquals(expected, actual);
	}

	@Test
	public void testROIListFieldSerialization() throws Exception {
		IROI roi = new RectangularROI(-3.5, 4.0, 8.0, 6.1, 0.0);
		ObjectWrapper<List<IROI>> roiWrapper = new ObjectWrapper<>(Arrays.asList(roi));
		json = marshaller.marshal(roiWrapper);
		assertEquals(JSON_FOR_WRAPPED_RECTANGULAR_ROI_LIST, json);
	}

	@Test
	public void testROIListFieldDeserialization() throws Exception {
		IROI roi = new RectangularROI(-3.5, 4.0, 8.0, 6.1, 0.0);
		ObjectWrapper<List<IROI>> expected = new ObjectWrapper<>(Arrays.asList(roi));
		ObjectWrapper<?> actual = marshaller.unmarshal(JSON_FOR_WRAPPED_RECTANGULAR_ROI_LIST, ObjectWrapper.class);
		assertEquals(expected.getObject(), actual.getObject());
	}

	@Test
	public void testCircularROI() throws Exception {
		double xCentre = 15.25;
		double yCentre = -23.65;
		double radius = 5.64;

		// Create ROI
		CircularROI circularROI = new CircularROI(radius, xCentre, yCentre);
		Object expected = circularROI.copy();
		json = marshaller.marshal(circularROI);
		Object actual = marshaller.unmarshal(json, Object.class);
		assertEquals(expected, actual);
	}

	@Test
	public void testLinearROI() throws Exception {
		double xStart = 10.23;
		double xStop = 34.25;
		double yStart = -12.3;
		double yStop = 11.4;

		// Create ROI
		LinearROI linearROI = new LinearROI(new double[] { xStart, yStart }, new double[] { xStop, yStop });
		Object expected = linearROI.copy();
		json = marshaller.marshal(linearROI);
		Object actual = marshaller.unmarshal(json, Object.class);
		assertEquals(expected, actual);
	}

	@Test
	public void testPointROI() throws Exception {
		double x = 10.23;
		double y = -12.3;

		// Create ROI
		PointROI pointROI = new PointROI(x, y);
		json = marshaller.marshal(pointROI);
		Object actual = marshaller.unmarshal(json, Object.class);
		assertEquals(pointROI, actual); // PointROI.copy() doesn't work so compare with original object instead
	}

	@Ignore("Currently fails because PolylineROI.equals() causes a stack overflow")
	@Test
	public void testPolygonalROI() throws Exception {
		// Create ROI
		PolygonalROI polygonalROI = new PolygonalROI();
		polygonalROI.insertPoint(-4.5, 8.31);
		polygonalROI.insertPoint(-1.0, 4.5e3);
		polygonalROI.insertPoint(-1.0, 4.5e3);
		polygonalROI.insertPoint(1.325e-12, 100.0);
		polygonalROI.insertPoint(-4.6, 8.32);
		Object expected = polygonalROI.copy();
		json = marshaller.marshal(polygonalROI);
		Object actual = marshaller.unmarshal(json, Object.class);
		assertEquals(expected, actual);
	}
}

class ObjectWrapper<T> {
	T object;
	public ObjectWrapper() {
	}
	public ObjectWrapper(T object) {
		this.object = object;
	}
	public T getObject() {
		return object;
	}
	public void setObject(T object) {
		this.object = object;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObjectWrapper<?> other = (ObjectWrapper<?>) obj;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		return true;
	}
}

class ROIWrapper extends ObjectWrapper<IROI> {
	public ROIWrapper() {
	}
	public ROIWrapper(IROI roi) {
		this.object = roi;
	}
}