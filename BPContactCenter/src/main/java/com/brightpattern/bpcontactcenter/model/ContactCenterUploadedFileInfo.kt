package com.brightpattern.bpcontactcenter.model

import com.brightpattern.bpcontactcenter.entity.FieldName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Describes uploaded file information.
 * ContactCenterUploadedFileInfo
 **/
@Serializable
data class ContactCenterUploadedFileInfo (
    @SerialName(FieldName.FILE_NAME)
    val fileName: String,
    @SerialName(FieldName.FILE_ID)
    val fileUUID: String
)