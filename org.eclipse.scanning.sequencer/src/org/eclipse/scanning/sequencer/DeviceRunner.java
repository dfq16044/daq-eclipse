package org.eclipse.scanning.sequencer;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.IRunnableEventDevice;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * 
 * Runs detectors in a task.
 * 
 * This is the equivalent to the GDA8 collectData() called on multiple
 * detectors with multiple threads and waiting for isBusy to be unset.
 * 
 * @author Matthew Gerring
 *
 */
class DeviceRunner extends LevelRunner<IRunnableDevice<?>> {

	private Collection<IRunnableDevice<?>>  devices;

	DeviceRunner(Collection<IRunnableDevice<?>> devices) {	
		this.devices = devices;
	}

	@Override
	protected Callable<IPosition> create(IRunnableDevice<?> detector, IPosition position) throws ScanningException {
		return new RunTask(detector, position);
	}
	
	@Override
	protected Collection<IRunnableDevice<?>> getObjects() {
		return devices;
	}

	private final class RunTask implements Callable<IPosition> {

		private IRunnableDevice<?>   detector;
		private IPosition            position;

		public RunTask(IRunnableDevice<?> detector, IPosition position) {
			this.detector = detector;
			this.position = position;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public IPosition call() throws Exception {
			if (detector instanceof IRunnableEventDevice) {
				((IRunnableEventDevice)detector).fireRunWillPerform(position);
			}
			try {
			    detector.run(position);
			} catch (Exception ne) {
				abort(detector, null, position, ne);
			}
			if (detector instanceof IRunnableEventDevice) {
				((IRunnableEventDevice)detector).fireRunPerformed(position);
			}
			return position;
		}

	}

}
