/*******************************************************************************
 * Copyright (c) 2004, 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.tasks.bugs;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.mylyn.tasks.bugs.IProduct;
import org.eclipse.mylyn.tasks.bugs.IProvider;

/**
 * @author Steffen Pingel
 */
public class SupportProduct extends AbstractSupportElement implements IProduct {

	private IBundleGroup bundleGroup;

	private boolean installed;

	TreeMap<String, ProductRepositoryMapping> mappingByNamespace;

	private String pluginId;

	private IProvider provider;

	private IBundleGroup versioningBundleGroup;

	public SupportProduct() {
		mappingByNamespace = new TreeMap<String, ProductRepositoryMapping>();
	}

	public void addRepositoryMapping(ProductRepositoryMapping mapping) {
		ProductRepositoryMapping existingMapping = mappingByNamespace.get(mapping.getNamespace());
		if (existingMapping != null) {
			existingMapping.getAttributes().putAll(mapping.getAttributes());
		} else {
			mappingByNamespace.put(mapping.getNamespace(), mapping);
		}
	}

	public Map<String, String> getAllAttributes(String prefix) {
		Map<String, String> attributes = null;
		for (int i = 0; i <= prefix.length(); i++) {
			ProductRepositoryMapping mapping = getMapping(prefix.substring(0, i));
			if (mapping != null) {
				if (attributes == null) {
					attributes = new HashMap<String, String>();
				}
				attributes.putAll(mapping.getAttributes());
			}
		}
		if (attributes != null) {
			return attributes;
		} else {
			return new HashMap<String, String>();
		}
	}

	public String getAttribute(String prefix, String key) {
		for (int i = prefix.length() - 1; i >= 0; i--) {
			ProductRepositoryMapping mapping = getMapping(prefix.substring(0, i));
			if (mapping != null) {
				String value = mapping.getAttributes().get(key);
				if (value != null) {
					return value;
				}
			}
		}
		return null;
	}

	public IBundleGroup getBundleGroup() {
		return bundleGroup;
	}

	public ProductRepositoryMapping getMapping(String prefix) {
		return mappingByNamespace.get(prefix);
	}

	public String getPluginId() {
		return pluginId;
	}

	public IProvider getProvider() {
		return provider;
	}

	public boolean hasMappings() {
		return !mappingByNamespace.isEmpty();
	}

	public IBundleGroup getVersioningBundleGroup() {
		return versioningBundleGroup;
	}

	/**
	 * @deprecated Use {@link #isInstalled()} instead
	 */
	@Deprecated
	public boolean isEnabled() {
		return isInstalled();
	}

	public boolean isInstalled() {
		return installed;
	}

	public void setBundleGroup(IBundleGroup bundleGroup) {
		this.bundleGroup = bundleGroup;
	}

	public void setVersioningBundleGroup(IBundleGroup versioningBundleGroup) {
		this.versioningBundleGroup = versioningBundleGroup;
	}

	/**
	 * @deprecated Use {@link #setInstalled(boolean)} instead
	 */
	@Deprecated
	public void setEnabled(boolean enabled) {
		setInstalled(enabled);
	}

	public void setInstalled(boolean enabled) {
		this.installed = enabled;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	public void setProvider(IProvider provider) {
		this.provider = provider;
	}

}
