package org.eclipse.scanning.api.scan;

import org.eclipse.scanning.api.IConfigurable;
import org.eclipse.scanning.api.ILevel;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;


/**
 * 
 * An IDevice is the runner for the whole scan but also for individual
 * detectors. Detectors, for instance an IMalcolmDevice can be run in 
 * the system as if it were an IDetector.
 * 
 * Anatomy of a CPU scan (non-malcolm)
 * 
 *  <br>
 *&nbsp;_________<br>
 *_|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|________  collectData() Tell detector to collect<br>
 *&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;_________<br>
 *_________|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|_  readout() Tell detector to readout<br>
 *&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;_______<br>
 *_________|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|___  moveTo()  Scannables move motors to new position<br>
 * <br>
 *<br>
 * A MalcolmDevice is also an IDetector which may operate with an arbitrary model, usually driving hardware.<br>
 * <br>
 * <usage><code>
 * IParserService pservice = ...// OSGi<br>
 * <br>
 * // Parse the scan command, throws an exception<br>
 * IParser<StepModel> parser = pservice.createParser(...)<br>
 * // e.g. "scan x 0 5 0.1 analyser"<br>
 * <br>
 * // Now use the parser to create a generator<br>
 * IPointGeneratorService gservice = ...// OSGi<br>
 * StepModel model = parser.getModel("x");<br>
 * Iterable<IPosition> gen = gservice.createGenerator(model)<br>
 * <br>
 * // Now scan the point iterator<br>
 * IDeviceService sservice = ...// OSGi<br>
 * IRunnableDevice<ScanModel> scanner = sservice.createScanner(...);<br>
 * scanner.configure(model);<br>
 * scanner.run();<br>
 * 
 * </code></usage>
 *
 * <img src="./doc/device_state.png" />
 * 
 * @author Matthew Gerring
 *
 */
public interface IRunnableDevice<T> extends INameable, ILevel, IConfigurable<T>, IResetableDevice {
	
	/**
	 * 
	 * @return the current device State. This is not the same as the Status of the scan.
	 */
	public DeviceState getDeviceState() throws ScanningException;

	/**
	 * Blocking call to execute the scan. The position specified may be null.
	 * 
	 * @throws ScanningException
	 */
	public void run(IPosition position) throws ScanningException, InterruptedException;

	/**
	 * Call to terminate the scan before it has finished.
	 * 
	 * @throws ScanningException
	 */
	public void abort() throws ScanningException;
	
	/**
	 * If the device is a virtual device which like a scan device controlling other
	 * hardware, it will return true for virtual. Normally hardware which is wrapped by
	 * a single java class will return false. It is not virtual and one instance of the wrapping
	 * class should exist. For standard non-virtual devices the IDeviceService will 
	 * cache the connection to the device such that it only has one connection and configuration.
	 * 
	 * @return
	 */
	default boolean isVirtual() {
		return false;
	}
}
