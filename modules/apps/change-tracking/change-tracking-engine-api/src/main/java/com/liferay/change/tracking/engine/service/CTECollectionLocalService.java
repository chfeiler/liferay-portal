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

package com.liferay.change.tracking.engine.service;

import aQute.bnd.annotation.ProviderType;

import com.liferay.change.tracking.engine.model.CTECollection;

import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.Projection;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.model.PersistedModel;
import com.liferay.portal.kernel.search.Indexable;
import com.liferay.portal.kernel.search.IndexableType;
import com.liferay.portal.kernel.service.BaseLocalService;
import com.liferay.portal.kernel.service.PersistedModelLocalService;
import com.liferay.portal.kernel.transaction.Isolation;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.io.Serializable;

import java.util.List;

/**
 * Provides the local service interface for CTECollection. Methods of this
 * service will not have security checks based on the propagated JAAS
 * credentials because this service can only be accessed from within the same
 * VM.
 *
 * @author Brian Wing Shun Chan
 * @see CTECollectionLocalServiceUtil
 * @see com.liferay.change.tracking.engine.service.base.CTECollectionLocalServiceBaseImpl
 * @see com.liferay.change.tracking.engine.service.impl.CTECollectionLocalServiceImpl
 * @generated
 */
@ProviderType
@Transactional(isolation = Isolation.PORTAL, rollbackFor =  {
	PortalException.class, SystemException.class})
public interface CTECollectionLocalService extends BaseLocalService,
	PersistedModelLocalService {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. Always use {@link CTECollectionLocalServiceUtil} to access the cte collection local service. Add custom service methods to {@link com.liferay.change.tracking.engine.service.impl.CTECollectionLocalServiceImpl} and rerun ServiceBuilder to automatically copy the method declarations to this interface.
	 */

	/**
	* Adds the cte collection to the database. Also notifies the appropriate model listeners.
	*
	* @param cteCollection the cte collection
	* @return the cte collection that was added
	*/
	@Indexable(type = IndexableType.REINDEX)
	public CTECollection addCTECollection(CTECollection cteCollection);

	public void addCTEEntryCTECollection(long cteEntryId,
		CTECollection cteCollection);

	public void addCTEEntryCTECollection(long cteEntryId, long cteCollectionId);

	public void addCTEEntryCTECollections(long cteEntryId,
		List<CTECollection> cteCollections);

	public void addCTEEntryCTECollections(long cteEntryId,
		long[] cteCollectionIds);

	public void clearCTEEntryCTECollections(long cteEntryId);

	/**
	* Creates a new cte collection with the primary key. Does not add the cte collection to the database.
	*
	* @param cteCollectionId the primary key for the new cte collection
	* @return the new cte collection
	*/
	@Transactional(enabled = false)
	public CTECollection createCTECollection(long cteCollectionId);

	/**
	* Deletes the cte collection from the database. Also notifies the appropriate model listeners.
	*
	* @param cteCollection the cte collection
	* @return the cte collection that was removed
	*/
	@Indexable(type = IndexableType.DELETE)
	public CTECollection deleteCTECollection(CTECollection cteCollection);

	/**
	* Deletes the cte collection with the primary key from the database. Also notifies the appropriate model listeners.
	*
	* @param cteCollectionId the primary key of the cte collection
	* @return the cte collection that was removed
	* @throws PortalException if a cte collection with the primary key could not be found
	*/
	@Indexable(type = IndexableType.DELETE)
	public CTECollection deleteCTECollection(long cteCollectionId)
		throws PortalException;

	public void deleteCTEEntryCTECollection(long cteEntryId,
		CTECollection cteCollection);

	public void deleteCTEEntryCTECollection(long cteEntryId,
		long cteCollectionId);

	public void deleteCTEEntryCTECollections(long cteEntryId,
		List<CTECollection> cteCollections);

	public void deleteCTEEntryCTECollections(long cteEntryId,
		long[] cteCollectionIds);

	/**
	* @throws PortalException
	*/
	@Override
	public PersistedModel deletePersistedModel(PersistedModel persistedModel)
		throws PortalException;

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public DynamicQuery dynamicQuery();

	/**
	* Performs a dynamic query on the database and returns the matching rows.
	*
	* @param dynamicQuery the dynamic query
	* @return the matching rows
	*/
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public <T> List<T> dynamicQuery(DynamicQuery dynamicQuery);

	/**
	* Performs a dynamic query on the database and returns a range of the matching rows.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS}), then the query will include the default ORDER BY logic from {@link com.liferay.change.tracking.engine.model.impl.CTECollectionModelImpl}. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	* </p>
	*
	* @param dynamicQuery the dynamic query
	* @param start the lower bound of the range of model instances
	* @param end the upper bound of the range of model instances (not inclusive)
	* @return the range of matching rows
	*/
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public <T> List<T> dynamicQuery(DynamicQuery dynamicQuery, int start,
		int end);

	/**
	* Performs a dynamic query on the database and returns an ordered range of the matching rows.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS}), then the query will include the default ORDER BY logic from {@link com.liferay.change.tracking.engine.model.impl.CTECollectionModelImpl}. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	* </p>
	*
	* @param dynamicQuery the dynamic query
	* @param start the lower bound of the range of model instances
	* @param end the upper bound of the range of model instances (not inclusive)
	* @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	* @return the ordered range of matching rows
	*/
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public <T> List<T> dynamicQuery(DynamicQuery dynamicQuery, int start,
		int end, OrderByComparator<T> orderByComparator);

	/**
	* Returns the number of rows matching the dynamic query.
	*
	* @param dynamicQuery the dynamic query
	* @return the number of rows matching the dynamic query
	*/
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public long dynamicQueryCount(DynamicQuery dynamicQuery);

	/**
	* Returns the number of rows matching the dynamic query.
	*
	* @param dynamicQuery the dynamic query
	* @param projection the projection to apply to the query
	* @return the number of rows matching the dynamic query
	*/
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public long dynamicQueryCount(DynamicQuery dynamicQuery,
		Projection projection);

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public CTECollection fetchCTECollection(long cteCollectionId);

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public ActionableDynamicQuery getActionableDynamicQuery();

	/**
	* Returns the cte collection with the primary key.
	*
	* @param cteCollectionId the primary key of the cte collection
	* @return the cte collection
	* @throws PortalException if a cte collection with the primary key could not be found
	*/
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public CTECollection getCTECollection(long cteCollectionId)
		throws PortalException;

	/**
	* Returns a range of all the cte collections.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS}), then the query will include the default ORDER BY logic from {@link com.liferay.change.tracking.engine.model.impl.CTECollectionModelImpl}. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	* </p>
	*
	* @param start the lower bound of the range of cte collections
	* @param end the upper bound of the range of cte collections (not inclusive)
	* @return the range of cte collections
	*/
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<CTECollection> getCTECollections(int start, int end);

	/**
	* Returns the number of cte collections.
	*
	* @return the number of cte collections
	*/
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public int getCTECollectionsCount();

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<CTECollection> getCTEEntryCTECollections(long cteEntryId);

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<CTECollection> getCTEEntryCTECollections(long cteEntryId,
		int start, int end);

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<CTECollection> getCTEEntryCTECollections(long cteEntryId,
		int start, int end, OrderByComparator<CTECollection> orderByComparator);

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public int getCTEEntryCTECollectionsCount(long cteEntryId);

	/**
	* Returns the cteEntryIds of the cte entries associated with the cte collection.
	*
	* @param cteCollectionId the cteCollectionId of the cte collection
	* @return long[] the cteEntryIds of cte entries associated with the cte collection
	*/
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public long[] getCTEEntryPrimaryKeys(long cteCollectionId);

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public IndexableActionableDynamicQuery getIndexableActionableDynamicQuery();

	/**
	* Returns the OSGi service identifier.
	*
	* @return the OSGi service identifier
	*/
	public String getOSGiServiceIdentifier();

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public PersistedModel getPersistedModel(Serializable primaryKeyObj)
		throws PortalException;

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public boolean hasCTEEntryCTECollection(long cteEntryId,
		long cteCollectionId);

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public boolean hasCTEEntryCTECollections(long cteEntryId);

	public void setCTEEntryCTECollections(long cteEntryId,
		long[] cteCollectionIds);

	/**
	* Updates the cte collection in the database or adds it if it does not yet exist. Also notifies the appropriate model listeners.
	*
	* @param cteCollection the cte collection
	* @return the cte collection that was updated
	*/
	@Indexable(type = IndexableType.REINDEX)
	public CTECollection updateCTECollection(CTECollection cteCollection);
}