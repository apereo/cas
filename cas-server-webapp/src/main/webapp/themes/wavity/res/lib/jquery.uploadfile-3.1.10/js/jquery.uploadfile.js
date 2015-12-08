/*!
 * jQuery Upload File Plugin
 * version: 3.1.10
 * @requires jQuery v1.5 or later & form plugin
 * Copyright (c) 2013 Ravishanker Kusuma
 * http://hayageek.com/
 */
(function ($) {
   /* if($.fn.ajaxForm == undefined) {
        $.getScript("jquery.form-3.51.0/jquery.form.js");
    }*/
    var feature = {};
    feature.fileapi = $("<input type='file'/>").get(0).files !== undefined;
    feature.formdata = window.FormData !== undefined;
    $.fn.uploadFile = function (options) {
        // This is the easiest way to have default options.
        var s = $.extend({
            // These are the defaults.
            url: "",
            method: "POST",
            enctype: "multipart/form-data",
            returnType: null,
            allowDuplicates: true,
            duplicateStrict: false,
            allowedTypes: "*",
            //For list of acceptFiles
            // http://stackoverflow.com/questions/11832930/html-input-file-accept-attribute-file-type-csv
            acceptFiles: "*",
            fileName: "file",
            formData: {},
            dynamicFormData: function () {
                return {};
            },
            maxFileSize: -1,
            maxFileCount: -1,
            multiple: true,
            dragDrop: true,
            autoSubmit: true,
            showCancel: true,
            showAbort: true,
            showDone: true,
            showDelete: true,
            showError: true,
            showStatusAfterSuccess: true,
            showStatusAfterError: true,
            showFileCounter: true,
            fileCounterStyle: "). ",
            showProgress: false,
            nestedForms: true,
            showDownload: false,
            onLoad: function (obj) {},
            onSelect: function (files) {
                return true;
            },
            onSubmit: function (files, xhr) {},
            onSuccess: function (files, response, xhr, pd) {},
            onError: function (files, status, message, pd) {},
            onCancel: function (files, pd) {},
            downloadCallback: false,
            deleteCallback: false,
            afterUploadAll: false,
            abortButtonClass: "ajax-file-upload-abort",
            cancelButtonClass: "ajax-file-upload-cancel",
            dragDropContainerClass: "ajax-upload-dragdrop",
            dragDropHoverClass: "state-hover",
            errorClass: "ajax-file-upload-error pull-left m-t-10 m-l-10",
            uploadButtonClass: "ajax-file-upload m-l-5",
            dragDropStr: "<span><b>Drag &amp; Drop Files</b></span>",
            abortStr: "Abort",
            cancelStr: "Cancel",
            deletelStr: "<i class=\"fa fa-times\"></i>",
            doneStr: "Done",
            multiDragErrorStr: "Multiple File Drag &amp; Drop is not allowed.",
            extErrorStr: "is not allowed. Allowed extensions: ",
            duplicateErrorStr: "is not allowed. File already exists.",
            sizeErrorStr: "Not allowed. Allowed Max size: ",
            uploadErrorStr: "Upload is not allowed",
            maxFileCountErrorStr: "Not allowed. Maximum allowed files are:",
            downloadStr: "Download",
            customErrorKeyStr: "jquery-upload-file-error",
            showQueueDiv: false,
            statusBarWidth: 500,
            dragdropWidth: 500,
            showPreview: false,
            previewHeight: "auto",
            previewWidth: "100%",
            uploadFolder:"uploads/",
            fileCount : 0,
            fileUnqId : "",
            fileEdit  : "No",
            resourceArr : [],
            datacontrolId :"",
            taskDetails : "",
            taskResourceArr : [],
            module : ""
        }, options);

        this.fileCounter = 1;
        this.selectedFiles = s.fileCount;
        this.fCounter = 0; //failed uploads
        this.sCounter = 0; //success uploads
        this.tCounter = 0; //total uploads
        this.upldCnt  = "upldCnt-" + (new Date().getTime());
        var formGroup = "ajax-file-upload-" + (new Date().getTime());
        this.formGroup = formGroup;
        this.hide();
        this.errorLog = $("<div></div>"); //Writing errors
        this.after(this.errorLog);
        this.responses = [];
        this.existingFileNames = [];
        if(!feature.formdata) //check drag drop enabled.
        {
            s.dragDrop = false;
        }
        if(!feature.formdata) {
            s.multiple = false;
        }

        var obj = this;
        var uploadLabel = $('<div>' + $(this).html() + '</div>');
        $(uploadLabel).addClass(s.uploadButtonClass);

        // wait form ajax Form plugin and initialize
        (function checkAjaxFormLoaded() {
            if($.fn.ajaxForm) {

                if(s.dragDrop) {
                    var dragDrop = $('<div class="' + s.dragDropContainerClass + '" style="vertical-align:top;"></div>').width(s.dragdropWidth);
                    $(obj).before(dragDrop);
                    $(dragDrop).append(uploadLabel);
                    $(dragDrop).append($(s.dragDropStr));
                    setDragDropHandlers(obj, s, dragDrop);

                } else {
                    $(obj).before(uploadLabel);
                }
                s.onLoad.call(this, obj);
                createCutomInputFile(obj, formGroup, s, uploadLabel);

            } else window.setTimeout(checkAjaxFormLoaded, 10);
        })();

        this.startUpload = function () {
            $("." + this.formGroup).each(function (i, items) {
                if($(this).is('form')) $(this).submit();
            });
        }

        this.getFileCount = function () {
            return obj.selectedFiles;

        }
        this.stopUpload = function () {
            $("." + s.abortButtonClass).each(function (i, items) {
                if($(this).hasClass(obj.formGroup)) $(this).click();
            });
        }
        this.cancelAll = function () {
            $("." + s.cancelButtonClass).each(function (i, items) {
                if($(this).hasClass(obj.formGroup)) $(this).click();
            });
        }
        this.update = function (settings) {
            //update new settings
            s = $.extend(s, settings);
        }

        //This is for showing Old files to user.
        this.createProgress = function (filename) {
            var pd = new createProgressDiv(this, s);
            pd.progressDiv.show();
            pd.progressbar.width('100%');

            var fileNameStr = "";
            //if(s.showFileCounter) fileNameStr = obj.fileCounter + s.fileCounterStyle + filename;
            if(s.showFileCounter) fileNameStr = filename;
            else fileNameStr = filename;

            pd.filename.html(fileNameStr);
            obj.fileCounter++;
            obj.selectedFiles++;
            if(s.showPreview)
            {
                pd.preview.attr('src',s.uploadFolder+filename);
                pd.preview.show();
            }
            
            if(s.showDownload) {
                pd.download.show();
                pd.download.click(function () {
                    if(s.downloadCallback) s.downloadCallback.call(obj, [filename]);
                });
            }
            pd.del.show();

            pd.del.click(function () {
            	$(".ajax-file-upload-error").remove();
                pd.statusbar.hide().remove();
                var arr = [filename];
                if(s.deleteCallback) s.deleteCallback.call(this, arr, pd);
                obj.selectedFiles -= 1;
                updateFileCounter(s, obj);
            });

        }

        this.getResponses = function () {
            return this.responses;
        }
        var checking = false;

        function checkPendingUploads() {
            if(s.afterUploadAll && !checking) {
                checking = true;
                (function checkPending() {
                    if(obj.sCounter != 0 && (obj.sCounter + obj.fCounter == obj.tCounter)) {
                        s.afterUploadAll(obj);
                        checking = false;
                    } else window.setTimeout(checkPending, 100);
                })();
            }

        }

        function setDragDropHandlers(obj, s, ddObj) {
            ddObj.on('dragenter', function (e) {
                e.stopPropagation();
                e.preventDefault();
                $(this).addClass(s.dragDropHoverClass);
            });
            ddObj.on('dragover', function (e) {
                e.stopPropagation();
                e.preventDefault();
                var that = $(this);
                if (that.hasClass(s.dragDropContainerClass) && !that.hasClass(s.dragDropHoverClass)) {
                    that.addClass(s.dragDropHoverClass);
                }
            });
            ddObj.on('drop', function (e) {
                e.preventDefault();
                $(this).removeClass(s.dragDropHoverClass);
                obj.errorLog.html("");
                var files = e.originalEvent.dataTransfer.files;
                if(!s.multiple && files.length > 1) {
                    if(s.showError) {                    	
                    	$("<div class='" + s.errorClass + "'>" + s.multiDragErrorStr + "</div>").appendTo(obj.errorLog);

                        return;
                    }
                }
                if(s.onSelect(files) == false) return;
                serializeAndUploadFiles(s, obj, files);
            });
            ddObj.on('dragleave', function (e) {
                $(this).removeClass(s.dragDropHoverClass);
            });

            $(document).on('dragenter', function (e) {
                e.stopPropagation();
                e.preventDefault();
            });
            $(document).on('dragover', function (e) {
                e.stopPropagation();
                e.preventDefault();
                var that = $(this);
                if (!that.hasClass(s.dragDropContainerClass)) {
                    that.removeClass(s.dragDropHoverClass);
                }
            });
            $(document).on('drop', function (e) {
                e.stopPropagation();
                e.preventDefault();
                $(this).removeClass(s.dragDropHoverClass);
            });

        }

        function getSizeStr(size) {
            var sizeStr = "";
            var sizeKB = size / 1024;
            if(parseInt(sizeKB) > 1024) {
                var sizeMB = sizeKB / 1024;
                sizeStr = sizeMB.toFixed(2) + " MB";
            } else {
                sizeStr = sizeKB.toFixed(2) + " KB";
            }
            return sizeStr;
        }

        function serializeData(extraData) {
            var serialized = [];
            if(jQuery.type(extraData) == "string") {
                serialized = extraData.split('&');
            } else {
                serialized = $.param(extraData).split('&');
            }
            var len = serialized.length;
            var result = [];
            var i, part;
            for(i = 0; i < len; i++) {
                serialized[i] = serialized[i].replace(/\+/g, ' ');
                part = serialized[i].split('=');
                result.push([decodeURIComponent(part[0]), decodeURIComponent(part[1])]);
            }
            return result;
        }

        function serializeAndUploadFiles(s, obj, files) {
            for(var i = 0; i < files.length; i++) {
                if(!isFileTypeAllowed(obj, s, files[i].name)) {
                	if(s.module == ""){
                		if(s.showError) $("<div class='" + s.errorClass + "'><b>" + files[i].name + "</b> " + s.extErrorStr + s.allowedTypes + "</div>").appendTo(obj.errorLog);
                	}else{
                		if(s.showError) $("<div class='" + s.errorClass + "'>" + s.extErrorStr + "</div>").appendTo(obj.errorLog);
                	}
                    continue;
                }
                if(!s.allowDuplicates && isFileDuplicate(obj, files[i].name)) {
                    if(s.showError) $("<div class='" + s.errorClass + "'><b>" + files[i].name + "</b> " + s.duplicateErrorStr + "</div>").appendTo(obj.errorLog);
                    continue;
                }
                if(s.maxFileSize != -1 && files[i].size > s.maxFileSize) {
                    if(s.showError) $("<div class='" + s.errorClass + "'>"+ s.sizeErrorStr + getSizeStr(s.maxFileSize) + "</div>").appendTo(
                        obj.errorLog);
                    continue;
                }
                if(s.multiple == false && obj.selectedFiles >= s.maxFileCount) {
                	if(s.module != ""){
                		$(".ajax-file-upload-statusbar").remove();
                		$(".imageIcon").css("margin-right","-50px");
                	}else{
                        if(s.showError) $("<div class='" + s.errorClass + "' style='color:red'>" + s.maxFileCountErrorStr +"</div>").appendTo(
                            obj.errorLog);
                        continue;
                	}
                }
                if(s.maxFileCount != -1 && obj.selectedFiles >= s.maxFileCount) {
	                if(s.module != ""){
	            		$(".ajax-file-upload-statusbar").remove();
	            		$(".imageIcon").css("margin-right","-50px");
	            	}else{
	                    if(s.showError) $("<div class='" + s.errorClass + "'>" + s.maxFileCountErrorStr + s.maxFileCount + "</div>").appendTo(
	                        obj.errorLog);
	                    continue;
	            	}
                }
                if(s.multiple == false)
                {
                	if(s.module != ""){
 	            		$(".ajax-file-upload-statusbar").remove();
 	            		$(".imageIcon").css("margin-right","-50px");
 	            	}else{
 	                	if($(".ajax-file-upload-statusbar").find(".deleteresponse").attr("id") != undefined){
 	                    	if(s.showError) $("<div class='" + s.errorClass + "' style='color:red'>" + s.maxFileCountErrorStr +"</div>").appendTo(
 	                                   obj.errorLog);
 	                    	continue;
 	                    }
 	            	}
                }
                obj.selectedFiles++;
                obj.existingFileNames.push(files[i].name);
                var ts = s;
                var fd = new FormData();
                var fileName = s.fileName.replace("[]", "");
                fd.append(fileName, files[i]);
                var extraData = s.formData;
                if(extraData) {
                    var sData = serializeData(extraData);
                    for(var j = 0; j < sData.length; j++) {
                        if(sData[j]) {
                            fd.append(sData[j][0], sData[j][1]);
                        }
                    }
                }
                ts.fileData = fd;

                var pd = new createProgressDiv(obj, s);
                var fileNameStr = "";
                //if(s.showFileCounter) fileNameStr = obj.fileCounter + s.fileCounterStyle + files[i].name
                if(s.showFileCounter) fileNameStr =  files[i].name
                else fileNameStr = files[i].name;

                pd.filename.html(fileNameStr);
                var form = $("<form id='appBuilderUpld' style='display:block; position:absolute;left: 150px;' class='" + obj.formGroup + "' method='" + s.method + "' action='" +
                    s.url + "' enctype='" + s.enctype + "'></form>");
                form.appendTo('body');
                var fileArray = [];
                fileArray.push(files[i].name);
                ajaxFormSubmit(form, ts, pd, fileArray, obj, files[i]);
                obj.fileCounter++;
            }
        }

        function isFileTypeAllowed(obj, s, fileName) {
            var fileExtensions = s.allowedTypes.toLowerCase().split(",");
            var ext = fileName.split('.').pop().toLowerCase();
            if(s.allowedTypes != "*" && jQuery.inArray(ext, fileExtensions) < 0) {
                return false;
            }
            return true;
        }

        function isFileDuplicate(obj, filename) {
            var duplicate = false;
            if (obj.existingFileNames.length) {
                for (var x=0; x<obj.existingFileNames.length; x++) {
                    if (obj.existingFileNames[x] == filename
                        || s.duplicateStrict && obj.existingFileNames[x].toLowerCase() == filename.toLowerCase()
                    ) {
                        duplicate = true;
                    }
                }
            }
            return duplicate;
        }

        function removeExistingFileName(obj, fileArr) {
            if (obj.existingFileNames.length) {
                for (var x=0; x<fileArr.length; x++) {
                    var pos = obj.existingFileNames.indexOf(fileArr[x]);
                    if (pos != -1) {
                        obj.existingFileNames.splice(pos, 1);
                    }
                }
            }
        }

        function getSrcToPreview(file, obj) {
            if(file) {
                obj.show();
                var reader = new FileReader();
                reader.onload = function (e) {
                    obj.attr('src', e.target.result);
                };
                reader.readAsDataURL(file);
            }
        }

        function updateFileCounter(s, obj) {
            if(s.showFileCounter) {
                var count = $(".ajax-file-upload-filename").length;
                obj.fileCounter = count + 1;
                $(".ajax-file-upload-filename").each(function (i, items) {
                    /*var arr = $(this).html().split(s.fileCounterStyle);
                    var fileNum = parseInt(arr[0]) - 1; //decrement;
                    var name = count + s.fileCounterStyle + arr[1];
                    $(this).html(name);
                    count--;*/
                });
            }
        }

        function createCutomInputFile(obj, group, s, uploadLabel) {

            var fileUploadId = "ajax-upload-id-" + (new Date().getTime());

            var form = $("<form id='appBuilderUpld' method='" + s.method + "' action='" + s.url + "' enctype='" + s.enctype + "'></form>");
            var fileInputStr = "<input type='file' id='" + fileUploadId + "' name='" + s.fileName + "' accept='" + s.acceptFiles + "'/>";
            if(s.multiple) {
                if(s.fileName.indexOf("[]") != s.fileName.length - 2) // if it does not endwith
                {
                    s.fileName += "[]";
                }
                fileInputStr = "<input type='file' id='" + fileUploadId + "' name='" + s.fileName + "' accept='" + s.acceptFiles + "' multiple/>";
            }
            var fileInput = $(fileInputStr).appendTo(form);

            fileInput.change(function () {

                obj.errorLog.html("");
                var fileExtensions = s.allowedTypes.toLowerCase().split(",");
                var fileArray = [];
                if(this.files) //support reading files
                {
                    for(i = 0; i < this.files.length; i++) {
                        fileArray.push(this.files[i].name);
                    }

                    if(s.onSelect(this.files) == false) return;
                } else {
                    var filenameStr = $(this).val();
                    var flist = [];
                    fileArray.push(filenameStr);
                    if(!isFileTypeAllowed(obj, s, filenameStr)) {
                    	if(s.module == ""){
                            if(s.showError) $("<div class='" + s.errorClass + "'><b>" + filenameStr + "</b> " + s.extErrorStr + s.allowedTypes + "</div>").appendTo(
                                obj.errorLog);
                    	}else{
                            if(s.showError) $("<div class='" + s.errorClass + "'>" + s.extErrorStr + "</div>").appendTo(
                                obj.errorLog);
                    	}
                        return;
                    }
                    //fallback for browser without FileAPI
                    flist.push({
                        name: filenameStr,
                        size: 'NA'
                    });
                    if(s.onSelect(flist) == false) return;

                }
                updateFileCounter(s, obj);

                uploadLabel.unbind("click");
                form.hide();
                createCutomInputFile(obj, group, s, uploadLabel);

                form.addClass(group);
                if(feature.fileapi && feature.formdata) //use HTML5 support and split file submission
                {
                    form.removeClass(group); //Stop Submitting when.
                    var files = this.files;
                    serializeAndUploadFiles(s, obj, files);
                } else {
                    var fileList = "";
                    for(var i = 0; i < fileArray.length; i++) {
                        if(s.showFileCounter) fileList += obj.fileCounter + s.fileCounterStyle + fileArray[i] + "<br>";
                        else fileList += fileArray[i] + "<br>";;
                        obj.fileCounter++;

                    }
                    if(s.maxFileCount != -1 && (obj.selectedFiles + fileArray.length) > s.maxFileCount) {
                        if(s.showError) $("<div class='" + s.errorClass + "'>" + s.maxFileCountErrorStr + s.maxFileCount + "</div>").appendTo(
                            obj.errorLog);
                        return;
                    }
                    obj.selectedFiles += fileArray.length;

                    var pd = new createProgressDiv(obj, s);
                    pd.filename.html(fileList);
                    ajaxFormSubmit(form, s, pd, fileArray, obj, null);
                }



            });

            if(s.nestedForms) {
                form.css({
                    'margin': 0,
                    'padding': 0
                });
                uploadLabel.css({
                    position: 'relative',
                    overflow: 'hidden',
                    cursor: 'default'
                });
                fileInput.css({
                    position: 'absolute',
                    'cursor': 'pointer',
                    'top': '0px',
                    'width': '100%',
                    'height': '100%',
                    'left': '0px',
                    'z-index': '100',
                    'opacity': '0.0',
                    'filter': 'alpha(opacity=0)',
                    '-ms-filter': "alpha(opacity=0)",
                    '-khtml-opacity': '0.0',
                    '-moz-opacity': '0.0'
                });
                form.appendTo(uploadLabel);

            } else {
                form.appendTo($('body'));
                form.css({
                    margin: 0,
                    padding: 0,
                    display: 'block',
                    position: 'absolute',
                    left: '-250px'
                });
                if(navigator.appVersion.indexOf("MSIE ") != -1) //IE Browser
                {
                    uploadLabel.attr('for', fileUploadId);
                } else {
                    uploadLabel.click(function () {
                        fileInput.click();
                    });
                }
            }
        }


        function createProgressDiv(obj, s) {
        	if(s.fileEdit == "No"){
                this.statusbar = $("<div class='ajax-file-upload-statusbar " + obj.upldCnt + "' id='" + obj.upldCnt + "'></div>").width(s.statusBarWidth);
			}else{
	            this.statusbar = $("<div class='ajax-file-upload-statusbar " + s.fileUnqId + "' id='" + s.fileUnqId + "'></div>").width(s.statusBarWidth);
			}
            this.preview = $("<img class='ajax-file-upload-preview' />").width(s.previewWidth).height(s.previewHeight).appendTo(this.statusbar).hide();
            this.filename = $("<div class='ajax-file-upload-filename'></div>").appendTo(this.statusbar);
            this.progressDiv = $("<div class='ajax-file-upload-progress'>").appendTo(this.statusbar).hide();
            this.progressbar = $("<div class='ajax-file-upload-bar " + obj.formGroup + "'></div>").appendTo(this.progressDiv);
            this.abort = $("<div class='ajax-file-upload-red " + s.abortButtonClass + " " + obj.formGroup + "'>" + s.abortStr + "</div>").appendTo(this.statusbar)
                .hide();
            this.cancel = $("<div class='ajax-file-upload-red " + s.cancelButtonClass + " " + obj.formGroup + "'>" + s.cancelStr + "</div>").appendTo(this.statusbar)
                .hide();
            this.done = $("<div class='ajax-file-upload-green'>" + s.doneStr + "</div>").appendTo(this.statusbar).hide();
            this.download = $("<div class='ajax-file-upload-green'>" + s.downloadStr + "</div>").appendTo(this.statusbar).hide();
            this.del = $("<div class='deleteresponse ajax-file-upload-red'>" + s.deletelStr + "</div>").appendTo(this.statusbar).hide();
            if(s.showQueueDiv)
                $("#" + s.showQueueDiv).append(this.statusbar);
            else
                obj.errorLog.before(this.statusbar);
            return this;
        }


        function ajaxFormSubmit(form, s, pd, fileArray, obj, file) {
            var currentXHR = null;
            var options = {
                cache: false,
                contentType: false,
                processData: false,
                forceSync: false,
                type: s.method,
                data: s.formData,
                formData: s.fileData,
                dataType: s.returnType,
                beforeSubmit: function (formData, $form, options) {
                    if(s.onSubmit.call(this, fileArray) != false) {
                        var dynData = s.dynamicFormData();
                        if(dynData) {
                            var sData = serializeData(dynData);
                            if(sData) {
                                for(var j = 0; j < sData.length; j++) {
                                    if(sData[j]) {
                                        if(s.fileData != undefined) options.formData.append(sData[j][0], sData[j][1]);
                                        else options.data[sData[j][0]] = sData[j][1];
                                    }
                                }
                            }
                        }
                        obj.tCounter += fileArray.length;
                        //window.setTimeout(checkPendingUploads, 1000); //not so critical
                        checkPendingUploads();
                        return true;
                    }
                    pd.statusbar.append("<div class='" + s.errorClass + "'>" + s.uploadErrorStr + "</div>");
                    pd.cancel.show()
                    form.remove();
                    pd.cancel.click(function () {
                        removeExistingFileName(obj, fileArray);
                        pd.statusbar.remove();
                        s.onCancel.call(obj, fileArray, pd);
                        obj.selectedFiles -= fileArray.length; //reduce selected File count
                        updateFileCounter(s, obj);
                    });
                    return false;
                },
                beforeSend: function (xhr, o) {

                    pd.progressDiv.show();
                    pd.cancel.hide();
                    pd.done.hide();
                    if(s.showAbort) {
                        pd.abort.show();
                        pd.abort.click(function () {
                            removeExistingFileName(obj, fileArray);
                            xhr.abort();
                            obj.selectedFiles -= fileArray.length; //reduce selected File count
                        });
                    }
                    if(!feature.formdata) //For iframe based push
                    {
                        pd.progressbar.width('5%');
                    } else pd.progressbar.width('1%'); //Fix for small files
                },
                uploadProgress: function (event, position, total, percentComplete) {
                    //Fix for smaller file uploads in MAC
                    if(percentComplete > 98) percentComplete = 98;

                    var percentVal = percentComplete + '%';
                    if(percentComplete > 1) pd.progressbar.width(percentVal)
                    if(s.showProgress) {
                        pd.progressbar.html(percentVal);
                        pd.progressbar.css('text-align', 'center');
                    }
        			if(/chrom(e|ium)/.test(navigator.userAgent.toLowerCase())){
                		$(".imageIcon").css("margin-right","0px");
        			}else if(navigator.userAgent.search("Safari") >= 0 && navigator.userAgent.search("Chrome") < 0){
        				$(".imageIcon").css("-webkit-margin-end","0px");
        			}
                },
                success: function (data, message, xhr) {
            			var response = new Object();
            			response.name 	= data.caption;
            			response.value 	= data.id;
            			if(s.datacontrolId != ""){
            				s.resourceArr.push(response);//alert(JSON.stringify(s.resourceArr,null,2));
            				var uploadId = $('.'+s.datacontrolId).find(".ajax-file-upload-statusbar").attr('id');
            				var i = 0;
            				$('.'+uploadId).each(function(index){
                    			i=i+1;                    		
            				});
            				$('.'+uploadId).each(function(index){ 
                        		if(s.resourceArr.length == i){
                        			$(this).find(".deleteresponse").attr("id",s.resourceArr[index].value); 
            						$(this).find(".ajax-file-upload-filename").text(s.resourceArr[index].name); 
            						//$(this).find(".ajax-file-upload-preview").attr("src",s.url+"/"+s.resourceArr[index].value); 
            						$(this).find(".ajax-file-upload-preview").remove();
            						$(this).prepend("<img class=\"ajax-file-upload-preview\" style=\"width: 50px; height: 50px;\" src=\""+s.url+"/"+s.resourceArr[index].value+"\">");
            					}
            				});
                		}else if(s.taskDetails == "Task"){               		    
                			s.taskResourceArr.push(data.id);
                    		var uploadId = $("#notes").find(".ajax-file-upload-statusbar").attr("id");
                    		$('.'+uploadId).each(function(index){    		
                    			var current = $(this);
                    			$(current).children().next().next().next().next().next().next().next().attr("class","deleteTaskImage ajax-file-upload-red");
                        		$(current).children().next().next().next().next().next().next().next().attr("id",s.taskResourceArr[index]);                			
                    		});
                		}else{   	    
                    		$('.ajax-file-upload-filename').each(function(index){    	
                    			var current = $(this);
                    			$(current).next().next().next().next().next().next().attr("class","deleteresponse ajax-file-upload-red");
                        		$(current).next().next().next().next().next().next().attr("id",data.id);                			
                    		}); 
                		}             		
                		
                    //For custom errors.
                    if(s.returnType == "json" && $.type(data) == "object" && data.hasOwnProperty(s.customErrorKeyStr)) {
                        pd.abort.hide();
                        var msg = data[s.customErrorKeyStr];
                        s.onError.call(this, fileArray, 200, msg, pd);
                        if(s.showStatusAfterError) {
                            pd.progressDiv.hide();
                            pd.statusbar.append("<span class='" + s.errorClass + "'>ERROR: " + msg + "</span>");
                        } else {
                            pd.statusbar.hide();
                            pd.statusbar.remove();
                        }
                        obj.selectedFiles -= fileArray.length; //reduce selected File count
                        form.remove();
                        obj.fCounter += fileArray.length;
                        return;
                    }
                    obj.responses.push(data);
                    pd.progressbar.width('100%')
                    if(s.showProgress) {
                        pd.progressbar.html('100%');
                        pd.progressbar.css('text-align', 'center');
                    }

                    pd.abort.hide();
                    s.onSuccess.call(this, fileArray, data, xhr, pd);
                    if(s.showStatusAfterSuccess) {
                        if(s.showDone) {
                            pd.done.show();
                            pd.done.click(function () {
                                pd.statusbar.hide("slow");
                                pd.statusbar.remove();
                            });
                        } else {
                            pd.done.hide();
                        }
                        if(s.showDelete) {
                            pd.del.show();
                            pd.del.click(function () {
                            	$(".ajax-file-upload-error").remove();
                                //pd.statusbar.hide().remove();
                            	if(s.datacontrolId != ""){
                            		var newArr = [];
                            		var resourcesArr	=	s.resourceArr;
                            		for(var i=0;i<resourcesArr.length;i++){
                            			if($(this).attr("id") != resourcesArr[i]){
                            				newArr.push(resourcesArr[i]);
                            			}
                            		}
                            		s.resourceArr = newArr;
                            	}
                                if(s.deleteCallback) s.deleteCallback.call(this, data, pd);
                                obj.selectedFiles -= fileArray.length; //reduce selected File count
                                updateFileCounter(s, obj);

                            });
                        } else {
                            pd.del.hide();
                        }
                    } else {
                        pd.statusbar.hide("slow");
                        pd.statusbar.remove();

                    }
                    if(s.showDownload) {
                        pd.download.show();
                        pd.download.click(function () {
                            if(s.downloadCallback) s.downloadCallback(data);
                        });
                    }
                    form.remove();
                    obj.sCounter += fileArray.length;
                },
                error: function (xhr, status, errMsg) {
                    pd.abort.hide();
                    if(xhr.statusText == "abort") //we aborted it
                    {
                        pd.statusbar.hide("slow").remove();
                        updateFileCounter(s, obj);

                    } else {
                        s.onError.call(this, fileArray, status, errMsg, pd);
                        if(s.showStatusAfterError) {
                            pd.progressDiv.hide();
                            pd.statusbar.append("<span class='" + s.errorClass + "'>ERROR: " + errMsg + "</span>");
                        } else {
                            pd.statusbar.hide();
                            pd.statusbar.remove();
                        }
                        obj.selectedFiles -= fileArray.length; //reduce selected File count
                    }

                    form.remove();
                    obj.fCounter += fileArray.length;

                }
            };

            if(s.showPreview && file != null) {
                if(file.type.toLowerCase().split("/").shift() == "image") getSrcToPreview(file, pd.preview);
            }

            if(s.autoSubmit) {
                form.ajaxSubmit(options);
            } else {
                if(s.showCancel) {
                    pd.cancel.show();
                    pd.cancel.click(function () {
                        removeExistingFileName(obj, fileArray);
                        form.remove();
                        pd.statusbar.remove();
                        s.onCancel.call(obj, fileArray, pd);
                        obj.selectedFiles -= fileArray.length; //reduce selected File count
                        updateFileCounter(s, obj);
                    });
                }
                form.ajaxForm(options);

            }

        }
        return this;

    }
}(jQuery));