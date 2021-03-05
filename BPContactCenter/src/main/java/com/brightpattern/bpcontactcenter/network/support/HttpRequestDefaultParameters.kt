package com.brightpattern.bpcontactcenter.network.support

data class HttpRequestDefaultParameters(
        /// Identifies your contact center. It corresponds to the domain name of your contact center that you see in the upper right corner of the Contact Center Administrator application after login.
        val tenantUrl: String
)
