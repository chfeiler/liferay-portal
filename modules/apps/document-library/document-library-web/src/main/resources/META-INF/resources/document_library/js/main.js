AUI.add(
	'liferay-document-library',
	function(A) {
		var Lang = A.Lang;

		var WIN = A.config.win;

		var HTML5_UPLOAD = WIN && WIN.File && WIN.FormData && WIN.XMLHttpRequest;

		var DocumentLibrary = A.Component.create(
			{
				ATTRS: {
					downloadEntryUrl: {
						validator: Lang.isString
					},

					editEntryUrl: {
						validator: Lang.isString
					},

					editTagsUrl: {
						validator: Lang.isString
					},

					form: {
						validator: Lang.isObject
					},

					moveEntryUrl: {
						validator: Lang.isString
					},

					searchContainerId: {
						validator: Lang.isString
					},

					selectFileEntryTypeURL: {
						validator: Lang.isString
					},

					trashEnabled: {
						validator: Lang.isBoolean
					},

					viewFileEntryURL: {
						validator: Lang.isString
					}
				},

				AUGMENTS: [Liferay.PortletBase],

				EXTENDS: A.Base,

				NAME: 'documentlibrary',

				prototype: {
					initializer: function(config) {
						var instance = this;

						var documentLibraryContainer = instance.byId('documentLibraryContainer');

						instance._documentLibraryContainer = documentLibraryContainer;

						instance._eventDataRequest = instance.ns('dataRequest');
						instance._entriesContainer = instance.byId('entriesContainer');

						var namespace = instance.NS;

						var searchContainer = Liferay.SearchContainer.get(namespace + instance.get('searchContainerId'));

						searchContainer.registerAction('move-to-folder', A.bind('_moveToFolder', instance));
						searchContainer.registerAction('move-to-trash', A.bind('_moveToTrash', instance));

						instance._searchContainer = searchContainer;

						var foldersConfig = config.folders;

						instance._folderId = foldersConfig.defaultParentFolderId;

						var eventHandles = [];

						instance._config = config;

						if (config.uploadable && HTML5_UPLOAD && themeDisplay.isSignedIn() && instance._entriesContainer.inDoc()) {
							config.appViewEntryTemplates = instance.byId('appViewEntryTemplates');

							eventHandles.push(A.getDoc().once('dragenter', instance._plugUpload, instance, config));
						}

						instance._eventHandles = eventHandles;
					},

					destructor: function() {
						var instance = this;

						A.Array.invoke(instance._eventHandles, 'detach');

						instance._documentLibraryContainer.purge(true);
					},

					getFolderId: function() {
						var instance = this;

						return instance._folderId;
					},

					onActionItemClicked: function(event) {
						var instance = this;

						var action = event.data.item.data.action;

						var url = instance.get('editEntryUrl');

						if (action === 'editTags') {
							url = instance.get('editTagsUrl');
						}

						if (action === 'move' || action === 'moveEntries') {
							url = instance.get('moveEntryUrl');
						}

						if (action === 'download') {
							url = instance.get('downloadEntryUrl');
						}

						if (action === 'deleteEntries') {
							if (instance.get('trashEnabled')) {
								action = 'move_to_trash';
							}
							else if (confirm(Liferay.Language.get('are-you-sure-you-want-to-delete-the-selected-entries'))) {
								action = 'delete';
							}
							else {
								action = null;
							}
						}

						if (action) {
							instance._processAction(action, url);
						}
					},

					onFilterItemClicked: function(event) {
						var instance = this;

						var itemData = event.data.item.data;

						if (itemData.action === 'openDocumentTypesSelector') {
							var itemSelectorDialog = new A.LiferayItemSelectorDialog(
								{
									eventName: instance.ns('selectFileEntryType'),
									on: {
										selectedItemChange: function(event) {
											var selectedItem = event.newVal;

											if (selectedItem) {
												var uri = instance.get('viewFileEntryTypeURL');

												uri = Liferay.Util.addParams(instance.ns('fileEntryTypeId=') + selectedItem, uri);

												location.href = uri;
											}
										}
									},
									'strings.add': Liferay.Language.get('select'),
									title: Liferay.Language.get('select-document-type'),
									url: instance.get('selectFileEntryTypeURL')
								}
							);

							itemSelectorDialog.open();
						}
					},

					_moveToFolder: function(obj) {
						var instance = this;

						var namespace = instance.NS;

						var dropTarget = obj.targetItem;

						var selectedItems = obj.selectedItems;

						var folderId = dropTarget.attr('data-folder-id');

						if (folderId) {
							if (!instance._searchContainer.select ||
								selectedItems.indexOf(dropTarget.one('input[type=checkbox]'))
							) {
								var form = instance.get('form').node;

								form.get(namespace + 'newFolderId').val(folderId);

								instance._processAction('move', instance.get('moveEntryUrl'));
							}
						}
					},

					_moveToTrash: function() {
						var instance = this;

						instance._processAction('move_to_trash', instance.get('editEntryUrl'));
					},

					_openDocument: function(event) {
						var instance = this;

						Liferay.Util.openDocument(
							event.webDavUrl,
							null,
							function(exception) {
								var errorMessage = Lang.sub(
									Liferay.Language.get('cannot-open-the-requested-document-due-to-the-following-reason'),
									[exception.message]
								);

								var openMSOfficeError = instance.ns('openMSOfficeError');

								if (openMSOfficeError) {
									openMSOfficeError.setHTML(errorMessage);

									openMSOfficeError.removeClass('hide');
								}

							}
						);
					},

					_plugUpload: function(event, config) {
						var instance = this;

						instance.plug(
							Liferay.DocumentLibraryUpload,
							{
								appViewEntryTemplates: config.appViewEntryTemplates,
								columnNames: config.columnNames,
								dimensions: config.folders.dimensions,
								displayStyle: config.displayStyle,
								entriesContainer: instance._entriesContainer,
								folderId: instance._folderId,
								maxFileSize: config.maxFileSize,
								redirect: config.redirect,
								scopeGroupId: config.scopeGroupId,
								uploadURL: config.uploadURL,
								viewFileEntryURL: config.viewFileEntryURL
							}
						);
					},

					_processAction: function(action, url, redirectUrl) {
						var instance = this;

						var namespace = instance.NS;

						var form = instance.get('form').node;

						redirectUrl = redirectUrl || location.href;

						form.attr('method', instance.get('form').method);

						if (form.get(namespace + 'javax-portlet-action')) {
							form.get(namespace + 'javax-portlet-action').val(action);
						}
						else {
							form.get(namespace + 'cmd').val(action);
						}

						form.get(namespace + 'redirect').val(redirectUrl);

						submitForm(form, url, false);
					}
				}
			}
		);

		Liferay.Portlet.DocumentLibrary = DocumentLibrary;
	},
	'',
	{
		requires: ['document-library-upload', 'liferay-item-selector-dialog', 'liferay-message', 'liferay-portlet-base']
	}
);