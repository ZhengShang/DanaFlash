package com.ecreditpal.danaflash.net

import java.io.IOException

class DanaException(val code: Int, desc: String?) : IOException(desc) {

}