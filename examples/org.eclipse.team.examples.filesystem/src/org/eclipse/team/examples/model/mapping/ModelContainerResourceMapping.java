/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.model.mapping;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.examples.model.ModelContainer;
import org.eclipse.team.examples.model.ModelObject;
import org.eclipse.team.examples.model.ModelResource;

public class ModelContainerResourceMapping extends ModelResourceMapping {

	public ModelContainerResourceMapping(ModelContainer container) {
		super(container);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ResourceMapping#getTraversals(org.eclipse.core.resources.mapping.ResourceMappingContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ResourceTraversal[] getTraversals(ResourceMappingContext context,
			IProgressMonitor monitor) {
		return new ResourceTraversal[] { 
				new ResourceTraversal(new IResource[] { 
						getResource()
				}, IResource.DEPTH_INFINITE, IResource.NONE)
			};
	}

	private IResource getResource() {
		return ((ModelContainer)getModelObject()).getResource();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ResourceMapping#contains(org.eclipse.core.resources.mapping.ResourceMapping)
	 */
	public boolean contains(ResourceMapping mapping) {
		if (mapping instanceof ModelResourceMapping) {
			ModelObject object = (ModelObject)mapping.getModelObject();
			if (object instanceof ModelResource) {
				IResource resource = ((ModelResource) object).getResource();
				return getResource().getFullPath().isPrefixOf(resource.getFullPath());
			}
		}
		return false;
	}

}
