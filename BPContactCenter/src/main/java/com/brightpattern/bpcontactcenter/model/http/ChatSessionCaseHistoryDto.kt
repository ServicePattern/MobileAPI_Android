package com.brightpattern.bpcontactcenter.model.http

import com.brightpattern.bpcontactcenter.entity.ContactCenterEvent
import com.brightpattern.bpcontactcenter.entity.FieldName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatSessionCaseHistoryDto(@SerialName(FieldName.SESSIONS) val sessions: List<ContactCenterChatSession>)

@Serializable
data class ContactCenterChatSession(@SerialName(FieldName.CHAT_ID) val chatID: String,
                                    @SerialName(FieldName.CREATED_TIME) val createdTime: Long,
                                    @SerialName(FieldName.EVENTS) val events: List<ContactCenterEvent>)