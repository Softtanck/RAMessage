package com.softtanck.ramessageservice.model

import com.softtanck.model.RaRequestTypeParameter
import java.lang.reflect.Method

/**
 * @author Softtanck
 * @date 2022/3/23
 * Description: TODO
 */
internal data class RaRemoteMethod(val methodName: String, val methodRequestParams: ArrayList<RaRequestTypeParameter>, val method: Method)