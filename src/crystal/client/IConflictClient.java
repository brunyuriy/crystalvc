package crystal.client;

import crystal.model.ConflictResult;

/**
 * Interface to allow some backing store to notify the a client that a new analysis result has been computed.
 * 
 * @author rtholmes
 * 
 */
public interface IConflictClient {

	/**
	 * Analysis computed.
	 * 
	 * @param result
	 *            new analysis result.
	 */
	public void setStatus(ConflictResult result);
}
