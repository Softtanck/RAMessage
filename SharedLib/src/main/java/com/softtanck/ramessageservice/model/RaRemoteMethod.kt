package com.softtanck.ramessageservice.model

import com.softtanck.model.RaRequestTypeParameter

/**
 * @author Softtanck
 * @date 2022/3/23
 * Description: TODO
 */
data class RaRemoteMethod(val methodName: String, val methodRequestParams: ArrayList<RaRequestTypeParameter>)