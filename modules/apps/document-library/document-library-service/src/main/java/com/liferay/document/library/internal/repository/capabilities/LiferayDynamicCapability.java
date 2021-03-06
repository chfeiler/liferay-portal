/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.document.library.internal.repository.capabilities;

import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.repository.LocalRepository;
import com.liferay.portal.kernel.repository.Repository;
import com.liferay.portal.kernel.repository.capabilities.Capability;
import com.liferay.portal.kernel.repository.capabilities.DynamicCapability;
import com.liferay.portal.kernel.repository.event.RepositoryEventAware;
import com.liferay.portal.kernel.repository.event.RepositoryEventListener;
import com.liferay.portal.kernel.repository.event.RepositoryEventType;
import com.liferay.portal.kernel.repository.registry.RepositoryEventRegistry;
import com.liferay.portal.repository.util.LocalRepositoryWrapper;
import com.liferay.portal.repository.util.RepositoryWrapper;
import com.liferay.portal.repository.util.RepositoryWrapperAware;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @author Alejandro Tardín
 */
public class LiferayDynamicCapability
	implements DynamicCapability, RepositoryEventAware, RepositoryWrapperAware {

	public LiferayDynamicCapability(
		BundleContext bundleContext, String repositoryClassName) {

		Filter filter = null;

		try {
			filter = bundleContext.createFilter(
				StringBundler.concat(
					"(&(objectClass=", Capability.class.getName(),
					")(|(repository.class.name=", repositoryClassName,
					")(repository.class.name=ALL)))"));
		}
		catch (InvalidSyntaxException ise) {
			ReflectionUtil.throwException(ise);
		}

		_serviceTracker = new ServiceTracker<>(
			bundleContext, filter,
			new ServiceTrackerCustomizer<Capability, Capability>() {

				@Override
				public Capability addingService(
					ServiceReference<Capability> serviceReference) {

					Capability capability = bundleContext.getService(
						serviceReference);

					synchronized (LiferayDynamicCapability.this) {
						if (capability instanceof RepositoryEventAware) {
							_registerRepositoryEventListeners(
								(RepositoryEventAware & Capability)capability);
						}

						_capabilities.add(capability);

						_updateRepositoryWrappers();
					}

					return capability;
				}

				@Override
				public void modifiedService(
					ServiceReference<Capability> serviceReference,
					Capability capability) {
				}

				@Override
				public void removedService(
					ServiceReference<Capability> serviceReference,
					Capability capability) {

					synchronized (LiferayDynamicCapability.this) {
						_unregisterRepositoryEventListeners(capability);

						_capabilities.remove(capability);

						_updateRepositoryWrappers();
					}
				}

			});

		_serviceTracker.open();
	}

	public void clear() {
		_serviceTracker.close();
		_capabilities.clear();
	}

	@Override
	public void registerRepositoryEventListeners(
		RepositoryEventRegistry repositoryEventRegistry) {

		_repositoryEventRegistry = repositoryEventRegistry;
	}

	@Override
	public synchronized LocalRepository wrapLocalRepository(
		LocalRepository localRepository) {

		_originalLocalRepository = localRepository;

		_liferayDynamicCapabilityLocalRepositoryWrapper =
			new LiferayDynamicCapabilityLocalRepositoryWrapper(
				_wrapLocalRepository(localRepository));

		return _liferayDynamicCapabilityLocalRepositoryWrapper;
	}

	@Override
	public synchronized Repository wrapRepository(Repository repository) {
		_originalRepository = repository;

		_liferayDynamicCapabilityRepositoryWrapper =
			new LiferayDynamicCapabilityRepositoryWrapper(
				_wrapRepository(repository));

		return _liferayDynamicCapabilityRepositoryWrapper;
	}

	private <T extends Capability & RepositoryEventAware> void
		_registerRepositoryEventListeners(T capability) {

		CapabilityRegistration capabilityRegistration =
			new CapabilityRegistration();

		_capabilityRegistrations.put(capability, capabilityRegistration);

		capability.registerRepositoryEventListeners(
			new DynamicCapabilityRepositoryEventRegistryWrapper(
				capabilityRegistration, _repositoryEventRegistry));
	}

	private void _unregisterRepositoryEventListeners(Capability capability) {
		CapabilityRegistration capabilityRegistration =
			_capabilityRegistrations.remove(capability);

		if (capabilityRegistration != null) {
			for (RepositoryEventListenerRegistration
					repositoryEventListenerRegistration :
						capabilityRegistration.
							_repositoryEventListenerRegistrations) {

				_repositoryEventRegistry.unregisterRepositoryEventListener(
					repositoryEventListenerRegistration.
						getRepositoryEventTypeClass(),
					repositoryEventListenerRegistration.getModelClass(),
					repositoryEventListenerRegistration.
						getRepositoryEventListener());
			}
		}
	}

	private void _updateRepositoryWrappers() {
		if (_liferayDynamicCapabilityLocalRepositoryWrapper != null) {
			_liferayDynamicCapabilityLocalRepositoryWrapper.setLocalRepository(
				_wrapLocalRepository(_originalLocalRepository));
		}

		if (_liferayDynamicCapabilityRepositoryWrapper != null) {
			_liferayDynamicCapabilityRepositoryWrapper.setRepository(
				_wrapRepository(_originalRepository));
		}
	}

	private LocalRepository _wrapLocalRepository(
		LocalRepository localRepository) {

		LocalRepository wrappedLocalRepository = localRepository;

		for (Capability capability : _capabilities) {
			if (capability instanceof RepositoryWrapperAware) {
				RepositoryWrapperAware repositoryWrapperAware =
					(RepositoryWrapperAware)capability;

				wrappedLocalRepository =
					repositoryWrapperAware.wrapLocalRepository(
						wrappedLocalRepository);
			}
		}

		return wrappedLocalRepository;
	}

	private Repository _wrapRepository(Repository repository) {
		Repository wrappedRepository = repository;

		for (Capability capability : _capabilities) {
			if (capability instanceof RepositoryWrapperAware) {
				RepositoryWrapperAware repositoryWrapperAware =
					(RepositoryWrapperAware)capability;

				wrappedRepository = repositoryWrapperAware.wrapRepository(
					wrappedRepository);
			}
		}

		return wrappedRepository;
	}

	private final Set<Capability> _capabilities = new HashSet<>();
	private final Map<Capability, CapabilityRegistration>
		_capabilityRegistrations = new ConcurrentHashMap<>();
	private LiferayDynamicCapabilityLocalRepositoryWrapper
		_liferayDynamicCapabilityLocalRepositoryWrapper;
	private LiferayDynamicCapabilityRepositoryWrapper
		_liferayDynamicCapabilityRepositoryWrapper;
	private LocalRepository _originalLocalRepository;
	private Repository _originalRepository;
	private volatile RepositoryEventRegistry _repositoryEventRegistry;
	private final ServiceTracker<Capability, Capability> _serviceTracker;

	private static class CapabilityRegistration {

		public <S extends RepositoryEventType, T> void
			addRepositoryEventListenerRegistration(
				Class<S> repositoryEventTypeClass, Class<T> modelClass,
				RepositoryEventListener<S, T> repositoryEventListeners) {

			_repositoryEventListenerRegistrations.add(
				new RepositoryEventListenerRegistration(
					repositoryEventTypeClass, modelClass,
					repositoryEventListeners));
		}

		private final List<RepositoryEventListenerRegistration>
			_repositoryEventListenerRegistrations = new ArrayList<>();

	}

	private static class DynamicCapabilityRepositoryEventRegistryWrapper
		implements RepositoryEventRegistry {

		public DynamicCapabilityRepositoryEventRegistryWrapper(
			CapabilityRegistration capabilityRegistration,
			RepositoryEventRegistry repositoryEventRegistry) {

			_capabilityRegistration = capabilityRegistration;
			_repositoryEventRegistry = repositoryEventRegistry;
		}

		@Override
		public <S extends RepositoryEventType, T> void
			registerRepositoryEventListener(
				Class<S> repositoryEventTypeClass, Class<T> modelClass,
				RepositoryEventListener<S, T> repositoryEventListeners) {

			_capabilityRegistration.addRepositoryEventListenerRegistration(
				repositoryEventTypeClass, modelClass, repositoryEventListeners);

			_repositoryEventRegistry.registerRepositoryEventListener(
				repositoryEventTypeClass, modelClass, repositoryEventListeners);
		}

		@Override
		public <S extends RepositoryEventType, T> void
			unregisterRepositoryEventListener(
				Class<S> repositoryEventTypeClass, Class<T> modelClass,
				RepositoryEventListener<S, T> repositoryEventListener) {
		}

		private final CapabilityRegistration _capabilityRegistration;
		private final RepositoryEventRegistry _repositoryEventRegistry;

	}

	private static class LiferayDynamicCapabilityLocalRepositoryWrapper
		extends LocalRepositoryWrapper {

		public LiferayDynamicCapabilityLocalRepositoryWrapper(
			LocalRepository localRepository) {

			super(localRepository);
		}

	}

	private static class LiferayDynamicCapabilityRepositoryWrapper
		extends RepositoryWrapper {

		public LiferayDynamicCapabilityRepositoryWrapper(
			Repository repository) {

			super(repository);
		}

	}

	private static class RepositoryEventListenerRegistration
		<S extends RepositoryEventType, T> {

		public RepositoryEventListenerRegistration(
			Class<S> repositoryEventTypeClass, Class<T> modelClass,
			RepositoryEventListener<S, T> repositoryEventListener) {

			_repositoryEventTypeClass = repositoryEventTypeClass;
			_modelClass = modelClass;
			_repositoryEventListener = repositoryEventListener;
		}

		public Class<T> getModelClass() {
			return _modelClass;
		}

		public RepositoryEventListener<S, T> getRepositoryEventListener() {
			return _repositoryEventListener;
		}

		public Class<S> getRepositoryEventTypeClass() {
			return _repositoryEventTypeClass;
		}

		private final Class<T> _modelClass;
		private final RepositoryEventListener<S, T> _repositoryEventListener;
		private final Class<S> _repositoryEventTypeClass;

	}

}