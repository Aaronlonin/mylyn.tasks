/*******************************************************************************
* Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.tasks.core.deprecated;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @deprecated Do not use. This class is pending for removal: see bug 237552.
 */
@Deprecated
public class FileAttachment implements ITaskAttachment {

	private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

	private String contentType = APPLICATION_OCTET_STREAM;

	private String filename;

	private String description;

	private boolean patch;

	private final File file;

	public FileAttachment(File file) {
		if (file == null) {
			throw new IllegalArgumentException();
		}

		this.file = file;
		this.filename = file.getName();
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isPatch() {
		return patch;
	}

	public void setPatch(boolean patch) {
		this.patch = patch;
	}

	public InputStream createInputStream() throws IOException {
		return new FileInputStream(file);
	}

	public long getLength() {
		return file.length();
	}

}