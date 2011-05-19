/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.resources;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class MovedResourceOperation extends ReorganizedResourceOperation {

	public MovedResourceOperation() {
		super();
	}

	public MovedResourceOperation(IResource resource, IPath destination, int updateFlags, boolean success) {
		super(resource, destination, updateFlags, success);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.RESOURCE_MOVED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Moved resource";
	}

	@Override
	public void replayReorganizedResourceOperation(IResource resource) throws CoreException {
		if (resource instanceof Project) {
			Project project= (Project)resource;
			IProjectDescription description= project.getDescription();
			description.setName(destinationPath.substring(1)); //remove leading slash
			project.move(description, updateFlags, null);
		} else {
			resource.move(new Path(destinationPath), updateFlags, null);
		}
	}

}
