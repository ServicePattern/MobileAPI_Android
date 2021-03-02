package com.brightpattern.bpcontactcenter.interfaces

import com.brightpattern.bpcontactcenter.entity.ContactCenterEvent
import com.brightpattern.bpcontactcenter.utils.Result

interface ContactCenterEventsInterface {
    fun chatSessionEvents(result: Result<List<ContactCenterEvent>, Error>)
}