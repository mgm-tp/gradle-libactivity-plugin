package com.mgmtp.gradle.libactivity.plugin


import groovy.json.JsonSlurper
import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PACKAGE_PRIVATE)
class HttpClient {

    private final OkHttpClient okHttpClient = new OkHttpClient()

    private final JsonSlurper jsonSlurper = new JsonSlurper()

    static final String HTTPS_SCHEME = 'https'

    @PackageScope
    Object sendRequestAndParseJsonResponse(final HttpUrl httpUrl) {
        return sendRequestAndParseJsonResponse(httpUrl, [:])
    }

    @PackageScope
    Object sendRequestAndParseJsonResponse(
            final HttpUrl httpUrl,
            final Map<String, String> requestHeaderParams) {

        final Request.Builder requestBuilder = new Request.Builder().url(httpUrl)
        requestHeaderParams.forEach { final String key, final String value -> requestBuilder.addHeader(key, value) }

        try (final Response response = okHttpClient.newCall(requestBuilder.build()).execute()) {

            final int responseCode = response.code()

            if (responseCode != 200) {
                throw new HttpResponseNotOkException(httpUrl as String, responseCode)
            }

            return jsonSlurper.parse(response.body().byteStream())
        }
    }
}