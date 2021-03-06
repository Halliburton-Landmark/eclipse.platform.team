/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.filesystem.ui;


/**
 * A replace is simply a get that overwrite local changes
 */
public class ReplaceAction extends GetAction {
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.examples.filesystem.ui.GetAction#isOverwriteOutgoing()
	 */
	protected boolean isOverwriteOutgoing() {
		return true;
	}
}
