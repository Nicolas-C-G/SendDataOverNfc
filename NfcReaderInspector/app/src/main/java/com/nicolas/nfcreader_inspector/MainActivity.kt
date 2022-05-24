package com.nicolas.nfcreader_inspector

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    public override fun onResume() {
        super.onResume()
        nfcAdapter?.enableReaderMode(this, this,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null)
    }

    public override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        val isoDep = IsoDep.get(tag)
        isoDep.connect()
        val response = isoDep.transceive(Utils.hexStringToByteArray(
            "00A4040007A0000002471001"))

        var outputResponse = ""
        if (Utils.toHex(response) == "90AAFF"){
            val CHARS = "0123456789ABCDEF".toCharArray()
            outputResponse = "Inspector detected, Sending data..."

            for (i in 0 until 15){
                val dataStream: String = "00A4040007A0000002471001AF10FFFFDDDD" + CHARS[i] + CHARS[i]
                val response2 = isoDep.transceive(Utils.hexStringToByteArray(dataStream))
            }

            println("max bytes: " + isoDep.maxTransceiveLength)
        }else{
            outputResponse = Utils.toHex(response)
        }
        runOnUiThread { textView.append("\nCard Response: "
                + outputResponse) }
        isoDep.close()

    }
}