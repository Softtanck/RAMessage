/*
 * Copyright (C) 2022 Softtanck.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.softtanck.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Softtanck on 2022/3/12
 * A standard parameter builder.
 * Will be used in [RemoteServiceMethod]
 */
@Parcelize
data class RaRequestTypeParameter(val parameterTypeClasses: Class<*>) : Parcelable