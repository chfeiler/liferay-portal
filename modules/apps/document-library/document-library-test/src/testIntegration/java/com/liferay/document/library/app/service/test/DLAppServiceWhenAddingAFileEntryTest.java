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

package com.liferay.document.library.app.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.service.AssetEntryLocalServiceUtil;
import com.liferay.document.library.kernel.exception.DuplicateFileEntryException;
import com.liferay.document.library.kernel.exception.FileExtensionException;
import com.liferay.document.library.kernel.exception.FileNameException;
import com.liferay.document.library.kernel.exception.FileSizeException;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryConstants;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppServiceUtil;
import com.liferay.document.library.sync.constants.DLSyncConstants;
import com.liferay.document.library.workflow.WorkflowHandlerInvocationCounter;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.messaging.proxy.ProxyModeThreadLocal;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.AssertUtils;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.security.permission.DoAsUserThread;
import com.liferay.portal.service.test.ServiceTestUtil;
import com.liferay.portal.test.rule.ExpectedLog;
import com.liferay.portal.test.rule.ExpectedLogs;
import com.liferay.portal.test.rule.ExpectedType;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.util.PropsValues;
import com.liferay.portlet.documentlibrary.service.test.BaseDLAppTestCase;

import java.io.File;
import java.io.InputStream;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.util.JDBCExceptionReporter;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alexander Chow
 */
@RunWith(Arquillian.class)
public class DLAppServiceWhenAddingAFileEntryTest extends BaseDLAppTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void assetEntryShouldHavePublishDate() throws Exception {
		String fileName = RandomTestUtil.randomString();

		FileEntry fileEntry = addFileEntry(
			group.getGroupId(), parentFolder.getFolderId(), fileName);

		AssetEntry assetEntry = AssetEntryLocalServiceUtil.fetchEntry(
			DLFileEntryConstants.getClassName(), fileEntry.getFileEntryId());

		Assert.assertEquals(
			assetEntry.getCreateDate(), assetEntry.getPublishDate());
	}

	@Test
	public void assetTagsShouldBeOrdered() throws Exception {
		String fileName = RandomTestUtil.randomString();

		String[] assetTagNames = {"hello", "world"};

		FileEntry fileEntry = addFileEntry(
			group.getGroupId(), parentFolder.getFolderId(), fileName, fileName,
			assetTagNames);

		AssetEntry assetEntry = AssetEntryLocalServiceUtil.fetchEntry(
			DLFileEntryConstants.getClassName(), fileEntry.getFileEntryId());

		AssertUtils.assertEqualsSorted(assetTagNames, assetEntry.getTagNames());
	}

	@Test
	public void shouldCallWorkflowHandler() throws Exception {
		try (WorkflowHandlerInvocationCounter<DLFileEntry>
				workflowHandlerInvocationCounter =
				new WorkflowHandlerInvocationCounter<>(
					DLFileEntryConstants.getClassName())) {

			addFileEntry(group.getGroupId(), parentFolder.getFolderId());

			Assert.assertEquals(
				1,
				workflowHandlerInvocationCounter.getCount(
					"updateStatus", int.class, Map.class));
		}
	}

	@Test(expected = DuplicateFileEntryException.class)
	public void shouldFailIfDuplicateNameAndExtensionInFolder1()
		throws Exception {

		addFileEntry(
			group.getGroupId(), parentFolder.getFolderId(), _FILE_NAME,
			_STRIPPED_FILE_NAME, null);
		addFileEntry(
			group.getGroupId(), parentFolder.getFolderId(), _FILE_NAME,
			_FILE_NAME, null);
	}

	@Test(expected = DuplicateFileEntryException.class)
	public void shouldFailIfDuplicateNameAndExtensionInFolder2()
		throws Exception {

		addFileEntry(
			group.getGroupId(), parentFolder.getFolderId(), _FILE_NAME,
			_FILE_NAME, null);
		addFileEntry(
			group.getGroupId(), parentFolder.getFolderId(), _FILE_NAME,
			_STRIPPED_FILE_NAME, null);
	}

	@Test(expected = DuplicateFileEntryException.class)
	public void shouldFailIfDuplicateNameAndExtensionInFolder3()
		throws Exception {

		addFileEntry(
			group.getGroupId(), parentFolder.getFolderId(), _FILE_NAME,
			_STRIPPED_FILE_NAME, null);
		addFileEntry(
			group.getGroupId(), parentFolder.getFolderId(), _STRIPPED_FILE_NAME,
			_FILE_NAME, null);
	}

	@Test(expected = DuplicateFileEntryException.class)
	public void shouldFailIfDuplicateNameInFolder() throws Exception {
		addFileEntry(group.getGroupId(), parentFolder.getFolderId());
		addFileEntry(group.getGroupId(), parentFolder.getFolderId());
	}

	@Test(expected = FileSizeException.class)
	public void shouldFailIfSizeLimitExceeded() throws Exception {
		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				_getConfigurationTemporarySwapper("fileMaxSize", 1L)) {

			String fileName = RandomTestUtil.randomString();

			addFileEntry(
				group.getGroupId(), parentFolder.getFolderId(), fileName);
		}
	}

	@Test(expected = FileNameException.class)
	public void shouldFailIfSourceFileNameContainsBlacklistedChar()
		throws Exception {

		int i =
			RandomTestUtil.randomInt() %
			PropsValues.DL_CHAR_BLACKLIST.length;

		String blackListedChar = PropsValues.DL_CHAR_BLACKLIST[i];

		String sourceFileName =
			RandomTestUtil.randomString() + blackListedChar +
			RandomTestUtil.randomString();

		addFileEntry(
			group.getGroupId(), parentFolder.getFolderId(), sourceFileName);
	}

	@Test(expected = FileNameException.class)
	public void shouldFailIfSourceFileNameEndsWithBlacklistedChar()
		throws Exception {

		int i =
			RandomTestUtil.randomInt() %
			PropsValues.DL_CHAR_LAST_BLACKLIST.length;

		String blackListedChar = PropsValues.DL_CHAR_LAST_BLACKLIST[i];

		String sourceFileName = RandomTestUtil.randomString() + blackListedChar;

		addFileEntry(
			group.getGroupId(), parentFolder.getFolderId(), sourceFileName);
	}

	@Test(expected = FileExtensionException.class)
	public void shouldFailIfSourceFileNameExtensionNotSupported()
		throws Exception {

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				_getConfigurationTemporarySwapper(
					"fileExtensions", new String[0])) {

			String sourceFileName = "file.jpg";

			addFileEntry(
				group.getGroupId(), parentFolder.getFolderId(), sourceFileName);
		}
	}

	@Test(expected = FileNameException.class)
	public void shouldFailIfSourceFileNameIsBlacklisted() throws Exception {
		int i =
			RandomTestUtil.randomInt() %
			PropsValues.DL_NAME_BLACKLIST.length;

		String blackListedName = PropsValues.DL_NAME_BLACKLIST[i];

		addFileEntry(
			group.getGroupId(), parentFolder.getFolderId(), blackListedName);
	}

	@Test
	public void shouldFireSyncEvent() throws Exception {
		AtomicInteger counter = registerDLSyncEventProcessorMessageListener(
			DLSyncConstants.EVENT_ADD);

		addFileEntry(group.getGroupId(), parentFolder.getFolderId());

		Assert.assertEquals(1, counter.get());
	}

	@Test
	public void shouldHaveDefaultVersion() throws Exception {
		String fileName = RandomTestUtil.randomString();

		FileEntry fileEntry = addFileEntry(
			group.getGroupId(), parentFolder.getFolderId(), fileName);

		Assert.assertEquals(
			"Version label incorrect after add", "1.0", fileEntry.getVersion());
	}

	@Test
	public void shouldInferValidMimeType() throws Exception {
		String fileName = RandomTestUtil.randomString();

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		FileEntry fileEntry = DLAppServiceUtil.addFileEntry(
			group.getGroupId(), parentFolder.getFolderId(), fileName,
			ContentTypes.APPLICATION_OCTET_STREAM, fileName, StringPool.BLANK,
			StringPool.BLANK, CONTENT.getBytes(), serviceContext);

		Assert.assertEquals(ContentTypes.TEXT_PLAIN, fileEntry.getMimeType());
	}

	@Test
	public void shouldSucceedIfDuplicateNameInOtherFolder() throws Exception {
		addFileEntry(group.getGroupId(), parentFolder.getFolderId());
		addFileEntry(
			group.getGroupId(), DLFolderConstants.DEFAULT_PARENT_FOLDER_ID);
	}

	@ExpectedLogs(
		expectedLogs = {
			@ExpectedLog(
				expectedLog = "Deadlock found when trying to get lock; try restarting transaction",
				expectedType = ExpectedType.EXACT
			),
			@ExpectedLog(
				expectedLog = "Duplicate entry ",
				expectedType = ExpectedType.PREFIX
			)
		},
		level = "ERROR", loggerClass = JDBCExceptionReporter.class
	)
	@Ignore
	@Test
	public void shouldSucceedWithConcurrentAccess() throws Exception {
		_users = new User[ServiceTestUtil.THREAD_COUNT];

		for (int i = 0; i < ServiceTestUtil.THREAD_COUNT; i++) {
			User user = UserTestUtil.addUser(
				"DLAppServiceTest" + (i + 1), group.getGroupId());

			_users[i] = user;
		}

		DoAsUserThread[] doAsUserThreads = new DoAsUserThread[_users.length];

		_fileEntryIds = new long[_users.length];

		int successCount = 0;

		for (int i = 0; i < doAsUserThreads.length; i++) {
			doAsUserThreads[i] = new AddFileEntryThread(
				_users[i].getUserId(), i);
		}

		successCount = runUserThreads(doAsUserThreads);

		Assert.assertEquals(
			"Only " + successCount + " out of " + _users.length +
			" threads added successfully", _users.length, successCount);

		for (int i = 0; i < doAsUserThreads.length; i++) {
			doAsUserThreads[i] = new GetFileEntryThread(
				_users[i].getUserId(), i);
		}

		successCount = runUserThreads(doAsUserThreads);

		Assert.assertEquals(
			"Only " + successCount + " out of " + _users.length +
			" threads retrieved successfully", _users.length, successCount);
	}

	@Ignore
	@Test
	public void shouldSucceedWithNullBytes() throws Exception {
		String fileName = RandomTestUtil.randomString();

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		DLAppServiceUtil.addFileEntry(
			group.getGroupId(), parentFolder.getFolderId(), fileName,
			ContentTypes.TEXT_PLAIN, fileName, StringPool.BLANK,
			StringPool.BLANK, (byte[])null, serviceContext);
	}

	@Test
	public void shouldSucceedWithNullFile() throws Exception {
		String fileName = RandomTestUtil.randomString();

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		DLAppServiceUtil.addFileEntry(
			group.getGroupId(), parentFolder.getFolderId(), fileName,
			ContentTypes.TEXT_PLAIN, fileName, StringPool.BLANK,
			StringPool.BLANK, (File)null, serviceContext);
	}

	@Test
	public void shouldSucceedWithNullInputStream() throws Exception {
		String fileName = RandomTestUtil.randomString();

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		DLAppServiceUtil.addFileEntry(
			group.getGroupId(), parentFolder.getFolderId(), fileName,
			ContentTypes.TEXT_PLAIN, fileName, StringPool.BLANK,
			StringPool.BLANK, null, 0, serviceContext);
	}

	private long[] _fileEntryIds;

	@DeleteAfterTestRun
	private User[] _users;

	private class AddFileEntryThread extends DoAsUserThread {

		public AddFileEntryThread(long userId, int index) {
			super(userId);

			_index = index;
		}

		@Override
		public boolean isSuccess() {
			return _success;
		}

		@Override
		protected void doRun() throws Exception {
			ProxyModeThreadLocal.setForceSync(true);

			try {
				FileEntry fileEntry = addFileEntry(
					group.getGroupId(), parentFolder.getFolderId(),
					"Test-" + _index + ".txt");

				_fileEntryIds[_index] = fileEntry.getFileEntryId();

				if (_log.isDebugEnabled()) {
					_log.debug("Added file " + _index);
				}

				_success = true;
			}
			catch (Exception e) {
				_log.error("Unable to add file " + _index, e);
			}
		}

		private int _index;
		private boolean _success;

	}

	private class GetFileEntryThread extends DoAsUserThread {

		public GetFileEntryThread(long userId, int index) {
			super(userId);

			_index = index;
		}

		@Override
		public boolean isSuccess() {
			return _success;
		}

		@Override
		protected void doRun() throws Exception {
			try {
				FileEntry fileEntry = DLAppServiceUtil.getFileEntry(
					_fileEntryIds[_index]);

				InputStream is = fileEntry.getContentStream();

				String content = StringUtil.read(is);

				if (CONTENT.equals(content)) {
					if (_log.isDebugEnabled()) {
						_log.debug("Retrieved file " + _index);
					}

					_success = true;
				}
			}
			catch (Exception e) {
				_log.error("Unable to get file " + _index, e);
			}
		}

		private int _index;
		private boolean _success;

	}

}