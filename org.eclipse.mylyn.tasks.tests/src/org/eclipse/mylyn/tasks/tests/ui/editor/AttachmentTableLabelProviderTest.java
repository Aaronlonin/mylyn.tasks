/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.tasks.tests.ui.editor;

import org.eclipse.mylyn.internal.tasks.core.TaskAttachment;
import org.eclipse.mylyn.internal.tasks.ui.editors.AttachmentTableLabelProvider;
import org.eclipse.mylyn.tasks.tests.TaskTestUtil;

import junit.framework.TestCase;

/**
 * @author Thomas Ehrnhoefer
 * @author Steffen Pingel
 */
public class AttachmentTableLabelProviderTest extends TestCase {

	public void testGetAttachmentId() {
		TaskAttachment attachment = TaskTestUtil.createMockTaskAttachment("1");
		AttachmentTableLabelProvider labelProvider = new AttachmentTableLabelProvider();

		attachment.setUrl(null);
		assertEquals("", labelProvider.buildTextFromEventIndex(5, attachment).getString());

		attachment.setUrl("");
		assertEquals("", labelProvider.buildTextFromEventIndex(5, attachment).getString());

		attachment.setUrl("http://testi.ng");
		assertEquals("", labelProvider.buildTextFromEventIndex(5, attachment).getString());

		attachment.setUrl("http://testi.ng/?some=parameter");
		assertEquals("", labelProvider.buildTextFromEventIndex(5, attachment).getString());

		attachment.setUrl("http://testi.ng/?id=myid");
		assertEquals("myid", labelProvider.buildTextFromEventIndex(5, attachment).getString());
	}

}
