package com.nicolas.hceinspector

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class Inspector: HostApduService() {
    companion object {
        val TAG               = "Host Card Emulator"
        val STATUS_SUCCESS    = "90AAFF" //this tag is unique for Inspector devices
        val STATUS_FAILED     = "6F00"
        val CLA_NOT_SUPPORTED = "6E00"
        val INS_NOT_SUPPORTED = "6D00"
        val AID               = "A0000002471001"
        val SELECT_INS        = "A4"
        val DEFAULT_CLA       = "00"
        val MIN_APDU_LENGTH   = 12
        var DATA: String      = "0"
        val RECEIVE_FLAG      = "AF10" //this variable is unique for Inspector
        val OK_TRANSFER_DATA  = "AF11"
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Deactivated: " + reason)
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) {
            return Utils.hexStringToByteArray(STATUS_FAILED)
        }

        val hexCommandApdu = Utils.toHex(commandApdu)
        if (hexCommandApdu.length < MIN_APDU_LENGTH) {
            return Utils.hexStringToByteArray(STATUS_FAILED)
        }

        if (hexCommandApdu.substring(0, 2) != DEFAULT_CLA) {
            return Utils.hexStringToByteArray(CLA_NOT_SUPPORTED)
        }

        if (hexCommandApdu.substring(2, 4) != SELECT_INS) {
            return Utils.hexStringToByteArray(INS_NOT_SUPPORTED)
        }

        if (hexCommandApdu.substring(10, 24) == AID)  {
            DATA = hexCommandApdu
            if (DATA.length > 24) {
                if (checkReceiveFlag(DATA, RECEIVE_FLAG) == 1) {
                    println("We receive data with transfer flag")
                    println(DATA)
                    return Utils.hexStringToByteArray(OK_TRANSFER_DATA)
                }else{
                    return Utils.hexStringToByteArray(STATUS_FAILED)
                }
            }else{
                return Utils.hexStringToByteArray(STATUS_SUCCESS)
            }
        } else {
            return Utils.hexStringToByteArray(STATUS_FAILED)
        }

    }

    private fun checkReceiveFlag(data: String?, Flag: String?): Int{
        //00A404 0007A0 000002 471001 apdu
        //00A404 0007A0 000002 471001 AF10 apdu + Receive flag
        var output: Int = 0

        if (data != null) {
            if (data.substring(24, 28) == Flag) {
                output = 1
            }
        }
        return output
    }
}