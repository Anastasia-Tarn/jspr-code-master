package ru.netology;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URISyntaxException;
import java.util.*;

public class Request {

    private String method;
    private final String path;
    private final String protocolVersion;
    private final List<String> headers;
    private final byte[] body;
    private final List<NameValuePair> params;

    public Request(String method, String path, String protocolVersion, List<String> headers, byte[] body) {
        this.method = method;
        this.path = path;
        this.protocolVersion = protocolVersion;
        this.headers = headers;
        this.body = body;
        this.params = this.getQueryParams();
    }

    public List<String> getQueryParam (String paramName) {
        List<String> param = null;
        for (NameValuePair pair : params) {
            if (!pair.getName().equals(paramName)) {
                continue;
            }
            param = Collections.singletonList(pair.getValue());
        }
        return param;
    }


    public List<NameValuePair> getQueryParams () {
        try {
            List<NameValuePair> params = new URIBuilder(path).getQueryParams();
            return params;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }





}
