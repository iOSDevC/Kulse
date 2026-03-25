package com.iosdevc.android.logger

import okhttp3.Call
import okhttp3.Connection
import okhttp3.EventListener
import okhttp3.Handshake
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy

internal class KulseEventListener(
    private val logger: Kulse,
) : EventListener() {

    private val callStartTime = System.currentTimeMillis()

    private var dnsStartMs: Long? = null
    private var dnsEndMs: Long? = null
    private var connectStartMs: Long? = null
    private var connectEndMs: Long? = null
    private var tlsStartMs: Long? = null
    private var tlsEndMs: Long? = null
    private var requestStartMs: Long? = null
    private var requestEndMs: Long? = null
    private var responseStartMs: Long? = null
    private var responseEndMs: Long? = null

    private fun elapsed(): Long = System.currentTimeMillis() - callStartTime

    override fun dnsStart(call: Call, domainName: String) {
        dnsStartMs = elapsed()
    }

    override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {
        dnsEndMs = elapsed()
    }

    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        connectStartMs = elapsed()
    }

    override fun connectEnd(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy, protocol: Protocol?) {
        connectEndMs = elapsed()
    }

    override fun connectFailed(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy, protocol: Protocol?, ioe: IOException) {
        connectEndMs = elapsed()
    }

    override fun secureConnectStart(call: Call) {
        tlsStartMs = elapsed()
    }

    override fun secureConnectEnd(call: Call, handshake: Handshake?) {
        tlsEndMs = elapsed()
    }

    override fun requestHeadersStart(call: Call) {
        if (requestStartMs == null) requestStartMs = elapsed()
    }

    override fun requestBodyStart(call: Call) {
        if (requestStartMs == null) requestStartMs = elapsed()
    }

    override fun requestHeadersEnd(call: Call, request: Request) {
        requestEndMs = elapsed()
    }

    override fun requestBodyEnd(call: Call, byteCount: Long) {
        requestEndMs = elapsed()
    }

    override fun responseHeadersStart(call: Call) {
        responseStartMs = elapsed()
    }

    override fun responseHeadersEnd(call: Call, response: Response) {
        if (responseEndMs == null) responseEndMs = elapsed()
    }

    override fun responseBodyStart(call: Call) {
        if (responseStartMs == null) responseStartMs = elapsed()
    }

    override fun responseBodyEnd(call: Call, byteCount: Long) {
        responseEndMs = elapsed()
    }

    override fun callEnd(call: Call) {
        flushTimings(call)
    }

    override fun callFailed(call: Call, ioe: IOException) {
        flushTimings(call)
    }

    private fun flushTimings(call: Call) {
        val transactionId = logger.callTransactionMap.remove(call) ?: return
        logger.updateTransactionTimings(
            transactionId = transactionId,
            dnsStart = dnsStartMs,
            dnsEnd = dnsEndMs,
            connectStart = connectStartMs,
            connectEnd = connectEndMs,
            tlsStart = tlsStartMs,
            tlsEnd = tlsEndMs,
            requestStart = requestStartMs,
            requestEnd = requestEndMs,
            responseStart = responseStartMs,
            responseEnd = responseEndMs,
        )
    }

    class Factory(private val logger: Kulse) : EventListener.Factory {
        override fun create(call: Call): EventListener = KulseEventListener(logger)
    }
}
