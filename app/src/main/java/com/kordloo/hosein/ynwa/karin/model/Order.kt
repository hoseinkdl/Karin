package com.kordloo.hosein.ynwa.karin.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Order(var Ware: Ware, var count: Int = 0) : Parcelable